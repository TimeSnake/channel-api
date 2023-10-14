/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.ChannelHeartbeatMessage;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.channel.util.message.VoidMessage;
import de.timesnake.library.basic.util.Loggers;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

public abstract class Channel implements de.timesnake.channel.util.Channel {

  public static Channel getInstance() {
    return instance;
  }

  public static void setInstance(Channel channel) {
    if (instance == null) {
      instance = channel;
    }
  }

  protected static final Duration DEFAULT_TIME_OUT = Duration.ofSeconds(40);
  protected static final int CONNECTION_RETRIES = 3;
  private static Channel instance;

  protected final Thread mainThread;

  protected String listenHostName;
  protected final Host self;

  protected final Host proxy;
  protected final String proxyName;

  protected ChannelServer server;
  protected Thread serverThread;

  protected ChannelClient client;

  protected Duration timeOut = null;
  protected Thread timeOutThread;
  protected final Set<Host> pingedHosts = new HashSet<>();

  protected Channel(@NotNull Thread mainThread, @NotNull Host self, @NotNull Host proxy,
                    @NotNull String listenHostName, @NotNull String proxyName) {
    this.mainThread = mainThread;

    this.self = self;
    this.proxy = proxy;
    this.listenHostName = listenHostName;
    this.proxyName = proxyName;
    this.load();
    this.setTimeOut(DEFAULT_TIME_OUT);
  }

  private void load() {
    this.loadChannelClient();
    this.loadChannelServer();
  }

  protected void loadChannelServer() {
    this.server = new ChannelServer(this) {
      @Override
      public void runSync(SyncRun syncRun) {
        Channel.this.runSync(syncRun);
      }
    };
  }

  protected void loadChannelClient() {
    this.client = new ChannelClient(this);
  }

  protected void setTimeOut(Duration duration) {
    this.timeOut = duration;

    if (this.timeOutThread != null && this.timeOutThread.isAlive()) {
      this.timeOutThread.interrupt();
    }
    this.startTimeOutThread();
  }

  public void start() {
    this.serverThread = new Thread(this.server);
    this.serverThread.start();
    Loggers.CHANNEL.info("Channel started, listening on " + this.self);
  }

  public void stop() {
    final ChannelListenerMessage<VoidMessage> message = new ChannelListenerMessage<>(this.getSelf(), Listener.UNREGISTER_HOST);
    this.client.sendMessageToProxy(message);
    if (this.serverThread.isAlive()) {
      this.serverThread.interrupt();
      Loggers.CHANNEL.info("Channel stopped");
    }

    if (this.timeOutThread.isAlive()) {
      this.timeOutThread.interrupt();
    }
  }

  protected abstract void runSync(SyncRun syncRun);

  public Host getSelf() {
    return self;
  }

  public Host getProxy() {
    return proxy;
  }

  public String getListenHostName() {
    return listenHostName;
  }

  @Override
  public String getProxyName() {
    return proxyName;
  }

  public Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message) {
    return this.client.sendMessage(message);
  }

  @Override
  public ResultMessage sendMessageSynchronized(ChannelMessage<?, ?> message) {
    return this.client.sendMessageSynchronized(message);
  }

  public Future<ResultMessage> sendMessageToProxy(ChannelMessage<?, ?> message) {
    return this.client.sendMessageToProxy(message);
  }

  protected void sendListenerMessage(ListenerType type, ChannelMessageFilter<?> filter) {
    this.client.sendListenerMessage(type, filter);
  }

  @Override
  public void addListener(ChannelListener listener) {
    this.server.addLocalListener(listener);
  }

  @Override
  public void addListener(ChannelListener listener, ChannelMessageFilter<?> filter) {
    this.server.addLocalListener(listener, filter);
  }

  @Override
  public void removeListener(ChannelListener listener, ListenerType... types) {
    this.server.removeListener(listener, types);
  }

  @Override
  public Host getHost() {
    return this.self;
  }

  public ChannelServer getServer() {
    return server;
  }

  public ChannelClient getClient() {
    return client;
  }

  public void connectToProxy(ChannelListenerMessage<?> msg, Duration retryPeriod) {
    this.client.connectToProxy(msg, retryPeriod);
  }

  public void onProxyConnected() {
    Loggers.CHANNEL.info("Connected to proxy server");
  }

  public void onHostTimeOut(Host host) {
    Loggers.CHANNEL.warning("Lost connection to '" + host + "'");
    this.client.disconnectHost(host);
  }

  public void onHeartBeatMessage(ChannelHeartbeatMessage<?> msg) {
    if (msg.getMessageType().equals(Heartbeat.PONG)) {
      this.pingedHosts.remove(msg.getIdentifier());
    } else if (msg.getMessageType().equals(Heartbeat.PING)) {
      this.client.sendMessageSynchronizedToHost(msg.getIdentifier(), new ChannelHeartbeatMessage<>(this.self, Heartbeat.PONG));
    }
  }

  protected void startTimeOutThread() {
    if (this.timeOut == null) {
      return;
    }

    this.timeOutThread = new Thread(() -> {
      while (true) {
        ChannelHeartbeatMessage<VoidMessage> msg = new ChannelHeartbeatMessage<>(this.self, Heartbeat.PING);

        for (Entry<Host, ChannelClient.ChannelConnection> entry : Channel.this.client.getChannelByHost().entrySet()) {
          Host host = entry.getKey();
          Socket socket = entry.getValue().getSocket();

          if (!socket.isConnected()) {
            Channel.this.onHostTimeOut(host);
          } else {
            Channel.this.client.sendMessageSynchronizedToHost(host, msg);
            Channel.this.pingedHosts.add(host);
          }
        }

        try {
          Thread.sleep(this.timeOut.toMillis());
        } catch (InterruptedException ignored) {
          break;
        }

        for (Host host : Channel.this.pingedHosts) {
          Channel.this.onHostTimeOut(host);
        }

        Channel.this.pingedHosts.clear();
      }
    });
    this.timeOutThread.setDaemon(true);
    this.timeOutThread.start();
    Loggers.CHANNEL.info("Started channel time out ping");
  }
}
