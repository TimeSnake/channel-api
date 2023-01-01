/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.ChannelPingMessage;
import de.timesnake.channel.util.message.MessageType;

public abstract class Channel implements de.timesnake.channel.util.Channel {

    public static final String PROXY_NAME = "proxy";

    public static final Integer ADD = 10000;

    public static Channel getInstance() {
        return instance;
    }

    public static void setInstance(Channel channel) {
        if (instance == null) {
            instance = channel;
        }
    }

    protected static final String LISTEN_IP = "0.0.0.0";
    protected static final String SERVER_IP = "127.0.0.1";
    protected static final int CONNECTION_RETRIES = 3;
    private static Channel instance;

    protected final Thread mainThread;

    protected final String serverName;
    protected final Integer serverPort;
    protected final Host self;

    protected final Integer proxyPort;
    protected final Host proxy;

    protected ChannelServer server;
    protected Thread serverThread;

    protected ChannelClient client;

    protected Channel(Thread mainThread, String serverName, int serverPort, int proxy) {
        this.mainThread = mainThread;

        this.serverName = serverName;
        this.serverPort = serverPort;
        this.self = new Host(SERVER_IP, serverPort + ADD);

        this.proxyPort = proxy;
        this.proxy = new Host(SERVER_IP, proxy + ADD);

        this.loadChannelServer();
        this.loadChannelClient();
    }

    protected void loadChannelServer() {
        this.server = new ChannelServer(this) {
            @Override
            public void runSync(SyncRun syncRun) {
                Channel.this.runSync(syncRun);
            }

            @Override
            protected void handlePingMessage(ChannelPingMessage msg) {
                Channel.this.handlePingMessage(msg);
            }

            @Override
            protected void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
                Channel.this.handleRemoteListenerMessage(msg);
            }
        };
    }

    public void start() {
        this.serverThread = new Thread(this.server);
        this.serverThread.start();
        de.timesnake.channel.util.Channel.LOGGER.info("Network-channel started");
    }

    public void stop() {
        this.client.sendMessageToProxy(new ChannelListenerMessage<>(this.getSelf(),
                MessageType.Listener.UNREGISTER_SERVER, this.getServerName()));
        if (this.serverThread.isAlive()) {
            this.serverThread.interrupt();
            de.timesnake.channel.util.Channel.LOGGER.info("Network-channel stopped");
        }
    }

    protected void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
        this.client.handleRemoteListenerMessage(msg);
    }

    protected void loadChannelClient() {
        this.client = new ChannelClient(this);
    }

    protected abstract void runSync(SyncRun syncRun);

    protected void handlePingMessage(ChannelPingMessage msg) {
        this.client.sendPongMessage();
    }

    public Thread getMainThread() {
        return mainThread;
    }

    public String getServerName() {
        return serverName;
    }

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
}
