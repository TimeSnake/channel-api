/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.FilterMessage;
import de.timesnake.channel.util.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ControlMessageReceiver {

  public final Logger logger = LoggerFactory.getLogger("channel.receiver.control");

  private final Channel manager;

  public ControlMessageReceiver(Channel manager) {
    this.manager = manager;
  }

  public synchronized void handleControlMessage(ChannelConnection connection, ChannelControlMessage<?> msg) {
    MessageType<?> messageType = msg.getMessageType();
    if (messageType.equals(MessageType.Control.LISTENER_ADD)) {
      this.manager.getSender().addReceiverHost(msg.getIdentifier(), (MessageListenerData<?>) msg.getValue());
    } else if (messageType.equals(MessageType.Control.LISTENER_REMOVE)) {
      this.manager.getSender().removeReceiverHost(msg.getIdentifier(), (MessageListenerData<?>) msg.getValue());
    } else if (messageType.equals(MessageType.Control.INIT)) {
      this.handleInitMessage(connection, (ChannelControlMessage<FilterMessage<MessageListenerData<?>>>) msg);
    } else if (messageType.equals(MessageType.Control.INIT_ACK)) {
      this.handleInitAckMessage((ChannelControlMessage<FilterMessage<MessageListenerData<?>>>) msg);
    } else if (messageType.equals(MessageType.Control.INIT_FIN)) {
      this.handleFinalizeMessage(msg);
    } else if (messageType.equals(MessageType.Control.RECONNECT)) {
      this.handleReconnectMessage(connection, msg);
    } else if (messageType.equals(MessageType.Control.CLOSE)) {
      this.handleCloseMessage(msg.getIdentifier());
    } else if (messageType.equals(MessageType.Control.HOSTS_REQUEST)) {
      this.handleHostsRequestMessage(connection);
    } else if (messageType.equals(MessageType.Control.HOSTS_LIST)) {
      this.handleHostsListMessage((List<ChannelParticipant>) msg.getValue());
    }
  }

  public void handleHostsRequestMessage(ChannelConnection connection) {
    this.manager.getSender().sendMessageSync(connection.getParticipant(),
        new ChannelControlMessage<>(this.manager.getSelf(),
        MessageType.Control.HOSTS_LIST, new ArrayList<>(this.manager.getKnownParticipants())));
  }

  public void handleHostsListMessage(List<ChannelParticipant> participants) {
    for (ChannelParticipant participant : participants) {
      if (!participant.equals(this.manager.getSelf()) && !this.manager.getKnownParticipants().contains(participant)) {
        this.initConnectionToHost(participant);
      }
    }
  }

  protected ResultMessage initConnectionToHost(ChannelParticipant participant) {
    logger.info("Initializing connection to '{}'", participant);
    return this.manager.getSender().sendMessageSync(participant, new ChannelControlMessage<>(this.manager.self,
        MessageType.Control.INIT, this.manager.getListenerFilter()));
  }

  private void handleInitMessage(ChannelConnection connection,
                                 ChannelControlMessage<FilterMessage<MessageListenerData<?>>> msg) {
    ChannelParticipant sender = msg.getIdentifier();
    logger.info("Initializing connection to '{}'", sender);

    Predicate<MessageListenerData<?>> predicate = msg.getValue();
    connection.setParticipant(sender);
    connection.setListenerFilter(predicate != null ? predicate : m -> true);
    this.manager.getChannelByParticipant().put(sender, connection);

    this.manager.getSender().sendMessageSync(connection, new ChannelControlMessage<>(this.manager.self,
        MessageType.Control.INIT_ACK, this.manager.getListenerFilter()));
    this.manager.getSender().sendAllListenerMessagesTo(connection.getParticipant(), connection.getListenerFilter());
    this.manager.getSender().sendMessageSync(connection, new ChannelControlMessage<>(this.manager.self,
        MessageType.Control.INIT_FIN));
  }

  private void handleInitAckMessage(ChannelControlMessage<FilterMessage<MessageListenerData<?>>> msg) {
    Predicate<MessageListenerData<?>> predicate = msg.getValue();
    ChannelConnection connection = this.manager.getChannelConnection(msg.getIdentifier());
    connection.setParticipant(msg.getIdentifier());
    connection.setListenerFilter(predicate != null ? predicate : m -> true);

    this.manager.getSender().sendAllListenerMessagesTo(connection.getParticipant(), connection.getListenerFilter());
    this.manager.getSender().sendMessageSync(connection.getParticipant(), new ChannelControlMessage<>(this.manager.self,
        MessageType.Control.INIT_FIN));
  }

  private void handleFinalizeMessage(ChannelControlMessage<?> msg) {
    ChannelParticipant participant = msg.getIdentifier();

    logger.info("Finalized connection to '{}'", participant);

    if (this.manager.getChannelConnections().stream().noneMatch(c -> c.getListenerFilter() == null)) {
      this.manager.getSender().unstash();
      logger.info("Finalized all connections");
    }
  }

  private void handleReconnectMessage(ChannelConnection newConnection, ChannelControlMessage<?> msg) {
    ChannelParticipant participant = msg.getIdentifier();

    try {
      this.manager.getChannelConnection(participant).updateToReconnectedConnection(newConnection);
      logger.info("Updated socket of '{}', due to reconnection", participant.getName());
    } catch (IOException e) {
      logger.warn("Failed to handle reconnection of '{}': {}", participant.getName(), e.getMessage());
    }
  }

  private void handleCloseMessage(ChannelParticipant participant) {
    this.manager.getSender().removeReceiverHost(participant);
    ChannelConnection connection = this.manager.getChannelConnection(participant);

    if (connection != null) {
      this.manager.disconnectHost(connection);
    }
  }
}
