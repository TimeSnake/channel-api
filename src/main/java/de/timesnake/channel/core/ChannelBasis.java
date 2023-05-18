/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelHeartbeatMessage;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.library.basic.util.Loggers;
import java.net.Socket;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public abstract class ChannelBasis implements de.timesnake.channel.util.Channel {

    public static ChannelBasis getInstance() {
        return instance;
    }

    public static void setInstance(ChannelBasis channel) {
        if (instance == null) {
            instance = channel;
        }
    }

    protected static final String LISTEN_IP = "0.0.0.0";
    protected static final String SERVER_IP = "127.0.0.1";
    protected static final int CONNECTION_RETRIES = 3;
    private static ChannelBasis instance;

    protected final Thread mainThread;

    protected final Host self;

    protected final Host proxy;

    protected ChannelServer server;
    protected Thread serverThread;

    protected ChannelClient client;

    protected Duration timeOut = null;
    protected Thread timeOutThread;
    protected final Set<Host> pingedHosts = new HashSet<>();

    protected ChannelBasis(Thread mainThread, int socketPort, int proxySocketPort) {
        this(mainThread, new Host(SERVER_IP, socketPort),
                new Host(SERVER_IP, proxySocketPort));
    }

    protected ChannelBasis(Thread mainThread, Host self, Host proxy) {
        this.mainThread = mainThread;

        this.self = self;
        this.proxy = proxy;

        this.loadChannelClient();
        this.loadChannelServer();
        this.startTimeOutThread();
    }

    protected void loadChannelServer() {
        this.server = new ChannelServer(this) {
            @Override
            public void runSync(SyncRun syncRun) {
                ChannelBasis.this.runSync(syncRun);
            }
        };
    }

    protected void loadChannelClient() {
        this.client = new ChannelClient(this);
    }

    public void setTimeOut(Duration duration) {
        this.timeOut = duration;

        if (this.timeOutThread != null && this.timeOutThread.isAlive()) {
            this.timeOutThread.interrupt();
        }
        this.startTimeOutThread();
    }

    public void start() {
        this.serverThread = new Thread(this.server);
        this.serverThread.start();
        Loggers.CHANNEL.info("Network-channel started");
    }

    public void stop() {
        this.client.sendMessageToProxy(new ChannelListenerMessage<>(this.getSelf(),
                Listener.UNREGISTER_HOST));
        if (this.serverThread.isAlive()) {
            this.serverThread.interrupt();
            Loggers.CHANNEL.info("Network-channel stopped");
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

    public void sendMessage(ChannelMessage<?, ?> message) {
        this.client.sendMessage(message);
    }

    public void sendMessageToProxy(ChannelMessage<?, ?> message) {
        this.client.sendMessageToProxy(message);
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
    public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
        this.client.sendMessageSynchronized(message);
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

    }

    public void onHostTimeOut(Host host) {
        Loggers.CHANNEL.warning("Lost connection to '" + host + "'");
        ChannelBasis.this.client.disconnectHost(host);
    }

    public void onPingMessage(ChannelHeartbeatMessage<?> msg) {
        if (msg.getMessageType().equals(Heartbeat.CHANNEL_PONG)) {
            this.pingedHosts.remove(msg.getSender());
        } else if (msg.getMessageType().equals(Heartbeat.CHANNEL_PING)) {
            this.client.sendMessageSynchronized(msg.getSender(),
                    new ChannelHeartbeatMessage<>(this.self, Heartbeat.CHANNEL_PONG));
        }
    }

    protected void startTimeOutThread() {
        if (this.timeOut == null) {
            return;
        }

        this.timeOutThread = new Thread(() -> {
            ChannelHeartbeatMessage<Void> msg = new ChannelHeartbeatMessage<>(this.self,
                    Heartbeat.CHANNEL_PING);

            for (Entry<Host, Socket> entry : ChannelBasis.this.client.getSocketByHost()
                    .entrySet()) {
                Host host = entry.getKey();
                Socket socket = entry.getValue();

                if (!socket.isConnected()) {
                    ChannelBasis.this.onHostTimeOut(host);
                } else {
                    ChannelBasis.this.client.sendMessageSynchronized(host, msg);
                    ChannelBasis.this.pingedHosts.add(host);
                }
            }

            try {
                Thread.sleep(this.timeOut.toMillis());
            } catch (InterruptedException ignored) {
            }

            for (Host host : ChannelBasis.this.pingedHosts) {
                ChannelBasis.this.onHostTimeOut(host);
            }

            ChannelBasis.this.pingedHosts.clear();
            this.startTimeOutThread();
        });
        this.timeOutThread.start();
        Loggers.CHANNEL.fine("Started channel time out ping");
    }
}
