/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.FilterMessage;
import de.timesnake.channel.util.message.MessageType.Control;
import de.timesnake.channel.util.message.VoidMessage;
import de.timesnake.library.basic.util.Loggers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public abstract class Channel implements de.timesnake.channel.util.Channel {

  public final Logger logger = LogManager.getLogger("channel");

  public static Channel getInstance() {
    return instance;
  }

  public static void setInstance(Channel channel) {
    if (instance == null) {
      instance = channel;
    }
  }

  protected static final int CONNECTION_RETRIES = 3;
  private static Channel instance;

  protected final Thread mainThread;

  protected String listenHostName;
  protected final ChannelParticipant self;

  protected ChannelServer server;
  protected Thread serverThread;

  protected ListenerBasedChannelSender sender;
  protected ControlMessageManager controlMessageManager;
  protected LocalListenerManager localListenerManager;

  protected ConcurrentHashMap<ChannelParticipant, ChannelConnection> channelByParticipant = new ConcurrentHashMap<>();

  private Thread initConnectThread;

  protected Channel(@NotNull Thread mainThread, @NotNull ChannelParticipant self, @NotNull String listenHostName) {
    this.mainThread = mainThread;
    this.self = self;
    this.listenHostName = listenHostName;
    this.load();
  }

  private void load() {
    this.server = new ChannelServer(this);
    this.controlMessageManager = new ControlMessageManager(this);
    this.localListenerManager = new LocalListenerManager(this);
    this.sender = new ListenerBasedChannelSender(this);
  }

  public void start() {
    this.serverThread = new Thread(this.server);
    this.serverThread.setDaemon(true);
    this.serverThread.start();
    logger.info("Channel started, listening on {}", this.self);
  }

  public void registerToNetwork(ChannelParticipant networkMember, Duration retryPeriod) {
    this.initConnectThread = new Thread(() -> {
      this.connectToInitHost(networkMember, retryPeriod);
      this.getSender().sendMessageSync(networkMember, new ChannelControlMessage<>(this.self, Control.HOSTS_REQUEST));
    });
    this.initConnectThread.start();
  }

  private void connectToInitHost(ChannelParticipant networkMember, Duration retryPeriod) {
    ResultMessage resultMessage = this.getControlMessageManager().initConnectionToHost(networkMember);

    if (resultMessage.isSuccessful()) {
      logger.info("Connected to channel network");
      this.onNetworkConnected();
    } else {
      logger.warn("Failed to connect to init host, retrying ...");
      try {
        Thread.sleep(retryPeriod.toMillis());
      } catch (InterruptedException ignored) {
      }

      this.connectToInitHost(networkMember, retryPeriod);
    }
  }

  public void selfInit() {
    this.sender.unstash();
  }

  public void stop() {
    ChannelControlMessage<VoidMessage> msg = new ChannelControlMessage<>(this.getSelf(), Control.CLOSE);
    this.getKnownParticipants().forEach(p -> this.getSender().sendMessageSync(p, msg));

    if (this.serverThread.isAlive()) {
      this.serverThread.interrupt();
    }

    for (ChannelConnection connection : this.getChannelConnections()) {
      try {
        connection.close();
      } catch (IOException e) {
        logger.warn("Exception while closing socket to '{}'", connection.getParticipant());
      }
    }

    if (this.initConnectThread != null && this.initConnectThread.isAlive()) {
      this.initConnectThread.interrupt();
    }

    Loggers.CHANNEL.info("Channel stopped");
  }

  protected void acceptConnection(Socket socket) {
    ChannelConnection connection = new ChannelConnection(this, socket.getInetAddress().getHostName());
    this.updateConnectionSocket(socket, connection, false);
  }

  protected void updateConnectionSocket(Socket socket, ChannelConnection connection, boolean reconnect) {
    try {
      connection.updateSocket(socket, reconnect);
    } catch (IOException e) {
      logger.warn("Failed to add connection of host '{}': {}", connection.getHostname(), e.getMessage());
    }
  }

  public void disconnectHost(ChannelConnection connection) {
    try {
      if (connection.getParticipant() != null) {
        this.channelByParticipant.remove(connection.getParticipant());
      }
      connection.close();
      logger.info("Closed socket to '{}'", connection.getParticipant().getName());
      this.onConnectionClose(connection);
    } catch (IOException e) {
      logger.warn("Exception while closing socket to '{}': {}", connection.getParticipant().getName(), e.getMessage());
    }
  }

  protected abstract void runSync(Runnable runnable);

  public ChannelParticipant getSelf() {
    return self;
  }

  public String getListenHostName() {
    return listenHostName;
  }

  @Override
  public Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message) {
    return this.sender.sendMessageStashed(message);
  }

  @Override
  public ResultMessage sendMessageSync(ChannelMessage<?, ?> message) {
    return this.sender.sendMessageSyncAndStashed(message);
  }

  @Override
  public void addListener(ChannelListener listener) {
    this.localListenerManager.addLocalListener(listener);
  }

  @Override
  public <Identifier extends Serializable> void addListener(ChannelListener listener,
                                                            @NotNull Set<Identifier> identifiers) {
    this.localListenerManager.addLocalListener(listener, identifiers);
  }

  @Override
  public void addListenerSync(ChannelListener listener) {
    this.localListenerManager.addLocalListenerSync(listener);
  }

  @Override
  public <Identifier extends Serializable> void addListenerSync(ChannelListener listener,
                                                                @NotNull Set<Identifier> identifiers) {
    this.localListenerManager.addLocalListenerSync(listener, identifiers);
  }

  @Override
  public void removeListener(ChannelListener listener) {
    this.localListenerManager.removeListener(listener);
  }

  @Override
  public void removeListenerSync(ChannelListener listener) {
    this.localListenerManager.removeListenerSync(listener);
  }

  public Logger getLogger() {
    return logger;
  }

  public ConcurrentHashMap<ChannelParticipant, ChannelConnection> getChannelByParticipant() {
    return channelByParticipant;
  }

  public Collection<ChannelParticipant> getKnownParticipants() {
    return channelByParticipant.values().stream().map(ChannelConnection::getParticipant).toList();
  }

  public Collection<ChannelConnection> getChannelConnections() {
    return channelByParticipant.values();
  }

  public ChannelConnection getChannelConnection(ChannelParticipant participant) {
    return this.channelByParticipant.get(participant);
  }

  public void onNetworkConnected() {

  }

  public void onConnectionClose(ChannelConnection connection) {

  }

  protected ChannelServer getServer() {
    return server;
  }

  public ListenerBasedChannelSender getSender() {
    return sender;
  }

  protected LocalListenerManager getLocalListenerManager() {
    return localListenerManager;
  }

  public ControlMessageManager getControlMessageManager() {
    return controlMessageManager;
  }

  protected FilterMessage<MessageListenerData<?>> getListenerFilter() {
    return messageListenerData -> true;
  }
}
