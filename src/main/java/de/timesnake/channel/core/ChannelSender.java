/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelException;
import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ChannelSender {

  public final Logger logger = LoggerFactory.getLogger("channel.sender");

  protected final Channel manager;

  protected boolean stashEnabled = true;
  protected final Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();

  protected final ExecutorService executorService = new ThreadPoolExecutor(5, 100,
      5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

  public ChannelSender(Channel manager) {
    this.manager = manager;
  }

  public abstract Collection<ChannelParticipant> getListenerParticipants(ChannelMessage<?, ?> message);

  public void unstash() {
    this.stashEnabled = false;
    this.manager.getKnownParticipants().forEach(this::sendStashedMessages);
    this.messageStash.clear();
  }

  protected void sendStashedMessages(ChannelParticipant participant) {
    this.messageStash.forEach(m -> manager.getSender().sendMessageSync(participant, m));
    logger.info("Send stash to '{}'", participant);
  }

  public Future<ResultMessage> sendMessageStashed(ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSyncAndStashed(message));
  }

  public ResultMessage sendMessageSyncAndStashed(ChannelMessage<?, ?> message) {
    ResultMessage resultMessage = new ResultMessage();

    if (!this.stashEnabled) {
      for (ChannelParticipant participant : this.getListenerParticipants(message)) {
        resultMessage.addResult(this.sendMessageSync(participant, message));
      }
    } else {
      this.messageStash.add(message);
    }

    return resultMessage;
  }

  public Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSync(message));
  }

  public ResultMessage sendMessageSync(ChannelMessage<?, ?> message) {
    ResultMessage resultMessage = new ResultMessage();
    for (ChannelParticipant participant : this.getListenerParticipants(message)) {
      resultMessage.addResult(this.sendMessageSync(participant, message));
    }
    return resultMessage;
  }

  public Future<ResultMessage> sendMessage(ChannelParticipant participant, ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSync(participant, message));
  }

  public ResultMessage sendMessageSync(ChannelParticipant participant, ChannelMessage<?, ?> message) {
    ChannelConnection connection = this.manager.getChannelByParticipant()
        .computeIfAbsent(participant, h -> new ChannelConnection(this.manager, participant));

    return this.sendMessageSync(connection, message);
  }

  public ResultMessage sendMessageSync(ChannelConnection connection, ChannelMessage<?, ?> message) {
    ChannelParticipant participant = connection.getParticipant();
    try {
      ReentrantLock hostLock = connection.getWriteLock();
      boolean locked = false;

      try {
        locked = hostLock.tryLock(3, TimeUnit.SECONDS);
        if (!locked) {
          logger.warn("Unable to lock connection to '{}', due to timeout", connection);
          return new ResultMessage().addResult(participant, false,
              new ChannelException("send lock error", new TimeoutException("timed out while locking")));
        }

        this.sendMessageSync(connection, message, 0, null);
      } catch (InterruptedException e) {
        logger.warn("Unable to lock connection to '{}', due to interruption", participant);
        return new ResultMessage().addResult(participant, false, new ChannelException("send lock error", e));
      } finally {
        if (locked) {
          hostLock.unlock();
        }
      }
      return new ResultMessage().addResult(participant, true, null);
    } catch (IOException e) {
      logger.warn("Failed to setup connection to '{}': {}", participant, e.getMessage());
      return new ResultMessage().addResult(participant, false, new ChannelException("connection setup exception", e));
    }
  }

  private void sendMessageSync(ChannelConnection connection, ChannelMessage<?, ?> message, int retry,
                               Exception lastException) throws IOException {
    if (retry > Channel.CONNECTION_RETRIES) {
      throw new IOException("Unable to establish connection to '" + connection.getParticipant() + "': " + lastException.getMessage(), lastException);
    }

    if (retry == Channel.CONNECTION_RETRIES) {
      this.manager.disconnectHost(connection);
      logger.warn("Unable to send messages to '{}' -> disconnected", connection.getParticipant());
      return;
    }

    if (retry == Channel.CONNECTION_RETRIES - 1) {
      logger.info("Unable to send message to '{}' -> reconnecting", connection.getParticipant());
      Socket socket = new Socket(connection.getHostname(), connection.getParticipant().getListenPort());
      this.manager.updateConnectionSocket(socket, connection, true);
    }

    if (connection.getSocket() == null) {
      Socket socket = new Socket(connection.getHostname(), connection.getParticipant().getListenPort());
      this.manager.updateConnectionSocket(socket, connection, false);
    } else if (!connection.getSocket().isConnected()) {
      logger.info("No open socket to '{}' -> reconnecting", connection.getParticipant().getName());
      Socket socket = new Socket(connection.getHostname(), connection.getParticipant().getListenPort());
      this.manager.updateConnectionSocket(socket, connection, true);
    }

    try {
      if (connection.getSocket().isConnected()) {
        message.setSource(this.manager.getSelf());
        connection.getOutputStream().writeObject(message);
        connection.getOutputStream().flush();
        logger.debug("Sent to '{}': {}", connection.getParticipant(), message);
      } else {
        this.sendMessageSync(connection, message, retry + 1, new ConnectException("socket is not connected"));
      }
    } catch (IOException e) {
      this.sendMessageSync(connection, message, retry + 1, e);
    }
  }
}
