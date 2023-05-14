/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.library.basic.util.Loggers;

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

    protected ChannelBasis(Thread mainThread, int socketPort, int proxySocketPort) {
        this(mainThread, new Host(SERVER_IP, socketPort),
                new Host(SERVER_IP, proxySocketPort));
    }

    protected ChannelBasis(Thread mainThread, Host self, Host proxy) {
        this.mainThread = mainThread;

        this.self = self;
        this.proxy = proxy;

        this.loadChannelServer();
        this.loadChannelClient();
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

}
