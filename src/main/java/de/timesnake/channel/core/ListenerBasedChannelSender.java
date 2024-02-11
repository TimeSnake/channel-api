/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ListenerBasedChannelSender extends ChannelSender {

  public final Logger logger = LoggerFactory.getLogger("channel.sender.listener");

  protected ConcurrentHashMap<MessageListenerData<?>, Set<ChannelParticipant>> listenerHosts =
      new ConcurrentHashMap<>();

  protected final Set<MessageListenerData<?>> sentListenerMessages = ConcurrentHashMap.newKeySet();

  public ListenerBasedChannelSender(Channel manager) {
    super(manager);
  }

  @Override
  public Collection<ChannelParticipant> getListenerParticipants(ChannelMessage<?, ?> msg) {
    Set<ChannelParticipant> hosts = this.listenerHosts.getOrDefault(new MessageListenerData<>(msg.getChannelType(),
        msg.getMessageType(), msg.getIdentifier()), new HashSet<>());
    hosts.addAll(this.listenerHosts.getOrDefault(new MessageListenerData<>(msg.getChannelType(), msg.getMessageType(),
        null), new HashSet<>()));
    return hosts;
  }

  protected void sendListenerMessage(ChannelControlMessage<MessageListenerData<?>> msg) {
    this.manager.getChannelConnections().stream()
        .filter(c -> c.getListenerFilter() != null && c.getListenerFilter().test(msg.getValue()))
        .forEach(c -> this.manager.getSender().sendMessage(c.getParticipant(), msg));
  }

  public void sendAllListenerMessagesTo(ChannelParticipant participant, Predicate<MessageListenerData<?>> predicate) {
    this.sentListenerMessages.stream()
        .filter(data -> predicate == null || predicate.test(data))
        .forEach(data -> manager.getSender().sendMessageSync(participant,
            new ChannelControlMessage<>(this.manager.getSelf(), MessageType.Control.LISTENER_ADD, data)));
    logger.info("Sent stashed listeners to '{}'", participant);
  }

  public void broadcastListener(@NotNull ChannelType<?> channelType, @NotNull MessageType<?> messageType,
                                @NotNull Collection<? extends Serializable> identifiers) {
    MessageListenerData<?> data = new MessageListenerData<>(channelType, messageType, null);

    if (this.sentListenerMessages.contains(data)) {
      return;
    }

    if (identifiers.isEmpty()) {
      if (this.sentListenerMessages.add(data)) {
        this.sendListenerMessage(new ChannelControlMessage<>(this.manager.getSelf(), MessageType.Control.LISTENER_ADD,
            data));
      }
    } else {
      for (Serializable identifier : identifiers) {
        data = new MessageListenerData<>(channelType, messageType, identifier);

        if (this.sentListenerMessages.add(data)) {
          this.sendListenerMessage(new ChannelControlMessage<>(this.manager.getSelf(),
              MessageType.Control.LISTENER_ADD, data));
        }
      }

    }
  }

  public void revokeListener(@NotNull ChannelType<?> channelType, @NotNull MessageType<?> messageType,
                             @NotNull Collection<? extends Serializable> identifiers) {
    if (identifiers.isEmpty()) {
      MessageListenerData<?> data = new MessageListenerData<>(channelType, messageType, null);
      this.sentListenerMessages.remove(data);
      this.sendListenerMessage(new ChannelControlMessage<>(this.manager.getSelf(),
          MessageType.Control.LISTENER_REMOVE, data));
    } else {
      for (Serializable identifier : identifiers) {
        MessageListenerData<?> data = new MessageListenerData<>(channelType, messageType, identifier);
        this.sentListenerMessages.remove(data);
        this.sendListenerMessage(new ChannelControlMessage<>(this.manager.getSelf(),
            MessageType.Control.LISTENER_REMOVE, data));
      }
    }

    logger.info("Sent '{}' listener remove message", channelType.getName());
  }

  public void addReceiverHost(ChannelParticipant host, MessageListenerData<?> data) {
    if (host.equals(this.manager.getSelf())) {
      return;
    }

    this.listenerHosts.computeIfAbsent(data, k -> ConcurrentHashMap.newKeySet()).add(host);
    logger.info("Added remote listener from '{}'", host);
  }

  public void removeReceiverHost(ChannelParticipant host, MessageListenerData<?> data) {
    this.listenerHosts.remove(data).remove(host);
    logger.info("Removed listener of host '{}'", host);
  }

  public void removeReceiverHost(ChannelParticipant host) {
    this.listenerHosts.values().forEach(v -> v.remove(host));
    logger.info("Removed listener of host '{}'", host);
  }
}
