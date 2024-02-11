/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class ChannelConnection {

  private final Channel manager;

  private final String hostname;
  private ChannelParticipant participant;
  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private ChannelReceiver receiver;

  private Predicate<MessageListenerData<?>> listenerFilter;
  private final ReentrantLock writeLock = new ReentrantLock();


  public ChannelConnection(Channel manager, ChannelParticipant participant) {
    this(manager, participant.getHostname());
    this.participant = participant;
  }

  public ChannelConnection(Channel manager, String hostname) {
    this.manager = manager;
    this.hostname = hostname;
  }

  public String getHostname() {
    return hostname;
  }

  public ChannelParticipant getParticipant() {
    return participant;
  }

  public void setParticipant(ChannelParticipant participant) {
    this.participant = participant;
  }

  public Socket getSocket() {
    return socket;
  }

  public void updateSocket(Socket socket, boolean reconnect) throws IOException {
    this.close();

    this.socket = socket;
    this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    this.inputStream = new ObjectInputStream(socket.getInputStream());

    if (reconnect) {
      this.manager.getSender().sendMessageSync(this.participant, new ChannelControlMessage<>(this.manager.getSelf(),
          MessageType.Control.RECONNECT));
    }

    this.updateReceiver();
  }

  public void updateToReconnectedConnection(ChannelConnection connection) throws IOException {
    this.close();

    this.socket = connection.socket;
    this.outputStream = connection.outputStream;
    this.inputStream = connection.inputStream;
    this.receiver = connection.receiver;
    this.receiver.connection = this;
  }

  public void closeSocket() throws IOException {
    if (this.socket != null) {
      this.socket.close();
    }
  }

  public void close() throws IOException {
    this.stopReceiver();
    this.closeSocket();
  }

  public ObjectOutputStream getOutputStream() {
    return outputStream;
  }

  public ObjectInputStream getInputStream() {
    return inputStream;
  }

  public void updateReceiver() {
    this.receiver = new ChannelReceiver(this.manager, this);
    this.receiver.start();
  }

  public void stopReceiver() {
    if (this.receiver != null) {
      this.receiver.stop();
    }
  }

  public Predicate<MessageListenerData<?>> getListenerFilter() {
    return listenerFilter;
  }

  public void setListenerFilter(Predicate<MessageListenerData<?>> listenerFilter) {
    this.listenerFilter = listenerFilter;
  }

  public ReentrantLock getWriteLock() {
    return writeLock;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChannelConnection that = (ChannelConnection) o;
    return Objects.equals(hostname, that.hostname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostname);
  }
}
