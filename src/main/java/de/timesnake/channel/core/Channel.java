/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelHeartbeatMessage;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import de.timesnake.channel.util.message.MessageType.MessageIdentifierListener;
import de.timesnake.channel.util.message.MessageType.MessageTypeListener;
import de.timesnake.library.basic.util.Loggers;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

public abstract class Channel extends ChannelBasis {

    public static final String PROXY_NAME = "proxy";

    public static final Integer ADD = 10000;

    public static Channel getInstance() {
        return (Channel) ChannelBasis.getInstance();
    }

    protected final String serverName;
    protected final Integer serverPort;

    protected Channel(Thread mainThread, String serverName, int serverPort, int proxy) {
        super(mainThread, serverPort + ADD, proxy + ADD);

        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    @Override
    public void stop() {
        this.client.sendMessageToProxy(new ChannelListenerMessage<>(this.getSelf(),
                MessageType.Listener.UNREGISTER_SERVER, this.getServerName()));
        if (this.serverThread.isAlive()) {
            this.serverThread.interrupt();
            Loggers.CHANNEL.info("Network-channel stopped");
        }
    }

    @Override
    protected void loadChannelServer() {
        this.server = new ServerChannelServer(this);
    }

    @Override
    protected void loadChannelClient() {
        this.client = new ServerChannelClient(this);
    }

    public String getServerName() {
        return serverName;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public static class ServerChannelServer extends ChannelServer {

        protected ServerChannelServer(Channel manager) {
            super(manager);
        }

        @Override
        public void runSync(SyncRun syncRun) {
            this.manager.runSync(syncRun);
        }

        @Override
        protected void handlePingMessage(ChannelHeartbeatMessage<?> msg) {
            super.handlePingMessage(msg);
            if (msg.getMessageType().equals(Heartbeat.PING)) {
                ((ServerChannelClient) this.manager.client).sendPongMessage();
            }
        }

        @Override
        protected void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
            super.handleRemoteListenerMessage(msg);
            ((ServerChannelClient) this.manager.client).handleRemoteListenerMessage(msg);
        }
    }

    public static class ServerChannelClient extends ChannelClient {

        public ServerChannelClient(Channel manager) {
            super(manager);
        }

        public synchronized void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
            if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)
                    || msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
                this.addRemoteListener(msg);
            } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER_SERVER)
                    && msg.getSenderHost().equals(this.manager.getProxy())) {
                Loggers.CHANNEL.info("Receiving of listeners finished");
                this.listenerLoaded = true;
                for (ChannelMessage<?, ?> serverMsg : this.messageStash) {
                    this.manager.sendMessage(serverMsg);
                }
                this.messageStash.clear();
            } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER_SERVER)) {
                Host host = msg.getSenderHost();

                if (host != null) {
                    this.listenerHostByMessageTypeByChannelType.forEach((k, v) -> v.remove(host));
                    Loggers.CHANNEL.info("Removed listener " + host);
                }

                this.disconnectHost(host);
            }
        }

        public void addRemoteListener(ChannelListenerMessage<?> msg) {
            Host senderHost = msg.getSenderHost();

            if (senderHost.equals(this.manager.getSelf())) {
                return;
            }

            if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
                MessageIdentifierListener<?> identifierListener = (MessageIdentifierListener<?>) msg.getValue();

                if (identifierListener.getChannelType().equals(ChannelType.LOGGING)) {
                    this.addLogListener(msg.getSenderHost());
                    return;
                }
                // prevent registration of server listener, which not belongs to this server
                else if (identifierListener.getChannelType().equals(ChannelType.SERVER)
                        && !((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier()
                        .equals(((Channel) this.manager).getServerName())) {
                    return;
                }

                this.listenerHostByIdentifierByChannelType.get(
                                identifierListener.getChannelType())
                        .computeIfAbsent(identifierListener.getIdentifier(),
                                k -> ConcurrentHashMap.newKeySet()).add(senderHost);
            } else if (msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
                MessageTypeListener typeListener = (MessageTypeListener) msg.getValue();

                if (typeListener.getChannelType().equals(ChannelType.LOGGING)) {
                    this.addLogListener(msg.getSenderHost());
                    return;
                }

                this.listenerHostByMessageTypeByChannelType.get(typeListener.getChannelType())
                        .computeIfAbsent(typeListener.getMessageType(),
                                k -> ConcurrentHashMap.newKeySet()).add(senderHost);
            }

            Loggers.CHANNEL.info("Added remote listener from '" + msg.getSenderHost() + "'");
        }

        protected void addLogListener(Host host) {
            Logger logger = Logger.getLogger("");
            try {
                logger.addHandler(new SocketHandler(host.getHostname(), host.getPort()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Loggers.CHANNEL.info("Added remote log listener from '" + host + "'");
        }

        protected void sendPongMessage() {
            this.sendMessageToProxy(new ChannelHeartbeatMessage(this.manager.getSelf(),
                    Heartbeat.PONG, ((Channel) this.manager).getServerName()));
        }
    }
}
