/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListenerBasedChannelSender extends ChannelSender {

  public final Logger logger = LogManager.getLogger("channel.sender.listener");

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

  protected void sendListenerMessage(MessageType<ArrayList<MessageListenerData<?>>> type,
                                     ArrayList<MessageListenerData<?>> data) {
    for (ChannelConnection connection : this.manager.getChannelConnections()) {
      ArrayList<MessageListenerData<?>> filteredData = data.stream()
          .filter(d -> connection.getListenerFilter() == null || connection.getListenerFilter().test(d))
          .collect(Collectors.toCollection(ArrayList::new));

      this.manager.getSender().sendMessage(connection.getParticipant(),
          new ChannelControlMessage<>(this.manager.getSelf(), type, filteredData));
    }
  }

  public void sendAllListenerMessagesTo(ChannelParticipant participant, Predicate<MessageListenerData<?>> predicate) {
    ArrayList<MessageListenerData<?>> data = this.sentListenerMessages.stream()
        .filter(d -> predicate == null || predicate.test(d))
        .collect(Collectors.toCollection(ArrayList::new));
    manager.getSender().sendMessageSync(participant, new ChannelControlMessage<>(this.manager.getSelf(),
        MessageType.Control.LISTENER_ADD, data));
    logger.info("Sent stashed listeners to '{}'", participant);
  }

  public void broadcastListener(@NotNull ChannelType<?> channelType, @NotNull Collection<MessageType<?>> messageTypes,
                                @NotNull Collection<? extends Serializable> identifiers) {

    ArrayList<MessageListenerData<?>> data;
    if (identifiers.isEmpty()) {
      data = messageTypes.stream()
          .map(t -> new MessageListenerData<>(channelType, t, null))
          .filter(this.sentListenerMessages::add)
          .collect(Collectors.toCollection(ArrayList::new));

    } else {
      data = messageTypes.stream()
          .flatMap(t -> identifiers.stream().map(i -> new Tuple<>(t, i)))
          .map(t -> new MessageListenerData<>(channelType, t.getA(), t.getB()))
          .filter(this.sentListenerMessages::add)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    if (!data.isEmpty()) {
      this.sendListenerMessage(MessageType.Control.LISTENER_ADD, data);
      logger.info("Broadcast listener message: {}", data);
    }
  }

  public void revokeListener(@NotNull ChannelType<?> channelType, @NotNull Collection<MessageType<?>> messageTypes,
                             @NotNull Collection<? extends Serializable> identifiers) {

    ArrayList<MessageListenerData<?>> data;
    if (identifiers.isEmpty()) {
      data = messageTypes.stream()
          .map(t -> new MessageListenerData<>(channelType, t, null))
          .peek(this.sentListenerMessages::remove)
          .collect(Collectors.toCollection(ArrayList::new));

    } else {
      data = messageTypes.stream()
          .flatMap(t -> identifiers.stream().map(i -> new Tuple<>(t, i)))
          .map(t -> new MessageListenerData<>(channelType, t.getA(), t.getB()))
          .peek(this.sentListenerMessages::remove)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    this.sendListenerMessage(MessageType.Control.LISTENER_REMOVE, data);
    logger.info("Sent '{}' listener remove message", channelType.getName());
  }

  public void addReceiverHost(ChannelParticipant host, Collection<MessageListenerData<?>> dataCollection) {
    if (host.equals(this.manager.getSelf())) {
      return;
    }

    for (MessageListenerData<?> data : dataCollection) {
      this.listenerHosts.computeIfAbsent(data, k -> ConcurrentHashMap.newKeySet()).add(host);
    }
    logger.info("Added remote listener from '{}'", host);
  }

  public void removeReceiverHost(ChannelParticipant host, Collection<MessageListenerData<?>> dataCollection) {
    for (MessageListenerData<?> data : dataCollection) {
      Set<ChannelParticipant> participants = this.listenerHosts.get(data);
      if (participants != null) {
        participants.remove(host);

        if (participants.isEmpty()) {
          this.listenerHosts.remove(data);
        }
      }
    }
    logger.info("Removed listener of host '{}'", host);
  }

  public void removeReceiverHost(ChannelParticipant host) {
    this.listenerHosts.values().forEach(v -> v.remove(host));
    logger.info("Removed listeners of host '{}'", host);
  }
}
