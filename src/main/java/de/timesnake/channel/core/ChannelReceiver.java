/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.SocketException;

public class ChannelReceiver {

  public final Logger logger = LoggerFactory.getLogger("channel.receiver");

  protected final Channel manager;
  protected ChannelConnection connection;
  protected Thread thread;

  protected ChannelReceiver(Channel manager, ChannelConnection connection) {
    this.manager = manager;
    this.connection = connection;
  }

  public void start() {
    this.thread = new Thread(this::listenToSocket);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  public void stop() {
    if (this.thread != null && this.thread.isAlive()) {
      this.thread.interrupt();
    }
  }

  private void listenToSocket() {
    try {
      while (true) {
        ChannelMessage<?, ?> msg;
        try {
          msg = (ChannelMessage<?, ?>) connection.getInputStream().readObject();
        } catch (StreamCorruptedException e) {
          logger.warn("Exception while reading message: {}: {}", e.getClass().getSimpleName(), e.getMessage());
          continue;
        } catch (OptionalDataException e) {
          logger.warn("Exception while reading message: {} object read failure: {}", e.getClass().getSimpleName(),
              e.eof);
          continue;
        }

        if (msg == null) {
          logger.debug("Received from '{}': null", connection.getParticipant());
          continue;
        }

        logger.debug("Received from '{}': {}", connection.getParticipant(), msg);

        this.handleMessage(msg);
      }
    } catch (EOFException ignored) {

    } catch (SocketException e) {
      logger.info("Socket exception for '{}:{}': {}", connection.getSocket().getInetAddress().getHostName(),
          connection.getSocket().getPort(), e.getMessage());
    } catch (Exception e) {
      logger.warn("Exception while handling message from '{}:{}': {}",
          connection.getSocket().getInetAddress().getHostName(),
          connection.getSocket().getPort(), e.getMessage());
    }
  }

  private void handleMessage(ChannelMessage<?, ?> msg) {
    ChannelType<?> type = msg.getChannelType();

    if (ChannelType.CONTROL.equals(type)) {
      this.manager.getControlMessageManager().handleControlMessage(connection, (ChannelControlMessage<?>) msg);
      return;
    }

    this.manager.getLocalListenerManager().invokeLocalListeners(msg);
  }
}
