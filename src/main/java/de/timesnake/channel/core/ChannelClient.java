/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.InconsistentChannelListenerException;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.ChannelPingMessage;
import de.timesnake.channel.util.message.MessageType;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages all and only local channel listeners
 */
public class ChannelClient {

    /**
     * Checks if server is interested in message.
     * <p>
     * If message is server identifier listener, when check if listener identifier is equals with receiver server name.
     * </p>
     *
     * @param host       Host to send to
     * @param serverName Name of server to send to
     * @param msg        Listener message, which is sent
     * @return true if message is interesting for server, else false
     */
    public static boolean isInterestingForServer(Host host, String serverName, ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
            if (((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType().equals(ChannelType.SERVER)) {
                if (!serverName.equals(((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier())) {
                    return false;
                }
            }
        }
        return !msg.getSenderHost().equals(host);
    }

    protected final Channel manager;

    // Already send server messages
    protected Set<String> sendListenerNames = ConcurrentHashMap.newKeySet();
    protected boolean sendListenerMessageTypeAll = false;
    protected Set<MessageType<?>> sendListenerMessageTypes = ConcurrentHashMap.newKeySet();

    /**
     * Only for servers, which are interested
     */
    protected ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<Object, Set<Host>>>
            listenerHostByIdentifierByChannelType = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<MessageType<?>, Set<Host>>>
            listenerHostByMessageTypeByChannelType = new ConcurrentHashMap<>();


    protected ConcurrentHashMap<Host, Socket> socketByHost = new ConcurrentHashMap<>();

    // stash until server is registered
    protected boolean listenerLoaded = false;
    protected Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();


    public ChannelClient(Channel manager) {
        this.manager = manager;

        for (ChannelType<?> type : ChannelType.TYPES) {
            this.listenerHostByIdentifierByChannelType.put(type, new ConcurrentHashMap<>());
        }

        for (ChannelType<?> type : ChannelType.TYPES) {
            this.listenerHostByMessageTypeByChannelType.put(type, new ConcurrentHashMap<>());
        }
    }

    public void sendListenerMessage(ListenerType type, ChannelMessageFilter<?> filter) {
        if (type.getChannelType().equals(ChannelType.SERVER)) {
            if (filter != null && filter.getIdentifierFilter() != null) {
                Collection<String> identifiers;

                try {
                    identifiers = (Collection<String>) filter.getIdentifierFilter();
                } catch (ClassCastException e) {
                    throw new InconsistentChannelListenerException("invalid filter type");
                }

                for (String name : identifiers) {
                    if (this.sendListenerNames.contains(name)) {
                        continue;
                    }

                    this.sendListenerNames.add(name);

                    this.sendMessage(new ChannelListenerMessage<>(this.manager.getSelf(),
                            MessageType.Listener.IDENTIFIER_LISTENER,
                            new MessageType.MessageIdentifierListener<>(ChannelType.SERVER, name)));
                }
            } else {
                if (this.sendListenerMessageTypeAll || this.sendListenerMessageTypes.contains(type.getMessageType())) {
                    return;
                }

                if (type.getMessageType() == null) {
                    this.sendListenerMessageTypeAll = true;
                } else {
                    this.sendListenerMessageTypes.add(type.getMessageType());
                }

                this.sendMessage(new ChannelListenerMessage<>(this.manager.getSelf(),
                        MessageType.Listener.MESSAGE_TYPE_LISTENER,
                        new MessageType.MessageTypeListener(ChannelType.SERVER, type.getMessageType())));
            }
        }
    }

    protected synchronized void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)
                || msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
            this.addRemoteListener(msg);
        } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER_SERVER)
                && msg.getSenderHost().equals(this.manager.getProxy())) {
            de.timesnake.channel.util.Channel.LOGGER.info("Receiving of listeners finished");
            this.listenerLoaded = true;
            for (ChannelMessage<?, ?> serverMsg : this.messageStash) {
                this.manager.sendMessage(serverMsg);
            }
            this.messageStash.clear();
        } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER_SERVER)) {
            Host host = msg.getSenderHost();

            if (host != null) {
                this.listenerHostByMessageTypeByChannelType.forEach((k, v) -> v.remove(host));
                de.timesnake.channel.util.Channel.LOGGER.info("Removed listener " + host);
            }

            this.disconnectHost(host);
        }
    }

    protected void addRemoteListener(ChannelListenerMessage<?> msg) {
        Host senderHost = msg.getSenderHost();

        if (senderHost.equals(this.manager.getSelf())) {
            return;
        }

        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
            // prevent registration of server listeners, which are not belonging to this server
            if (((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType().equals(ChannelType.SERVER)
                    && !((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier().equals(this.manager.getServerName())) {
                return;
            }

            this.listenerHostByIdentifierByChannelType.get(((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType())
                    .computeIfAbsent(((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier(),
                            k -> ConcurrentHashMap.newKeySet()).add(senderHost);
        } else if (msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
            this.listenerHostByMessageTypeByChannelType.get(((MessageType.MessageTypeListener) msg.getValue()).getChannelType())
                    .computeIfAbsent(((MessageType.MessageTypeListener) msg.getValue()).getMessageType(),
                            k -> ConcurrentHashMap.newKeySet()).add(senderHost);
        }
        de.timesnake.channel.util.Channel.LOGGER.info("Added remote listener from '" + msg.getSenderHost() + "'");
    }

    protected void disconnectHost(Host host) {
        try {
            Socket socket = this.socketByHost.remove(host);
            if (socket != null) {
                socket.close();
            }
            de.timesnake.channel.util.Channel.LOGGER.info("Closed socket to " + host);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendPongMessage() {
        this.sendMessageToProxy(new ChannelPingMessage(this.manager.getServerName(), MessageType.Ping.PONG));
    }

    public void sendMessage(ChannelMessage<?, ?> message) {
        new Thread(() -> this.sendMessageSynchronized(message)).start();
    }

    public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
        if (this.listenerLoaded) {
            Set<Host> messageTypeListenerHosts = this.listenerHostByMessageTypeByChannelType
                    .get(message.getChannelType()).get(message.getMessageType());
            Set<Host> identifierListenerHosts = this.listenerHostByIdentifierByChannelType
                    .get(message.getChannelType()).get(message.getIdentifier());

            Set<Host> listenerHosts = new HashSet<>();

            if (messageTypeListenerHosts != null) {
                listenerHosts = messageTypeListenerHosts;
            }
            if (identifierListenerHosts != null) {
                listenerHosts.addAll(identifierListenerHosts);
            }

            for (Host host : listenerHosts) {
                this.sendMessageSynchronized(host, message);
            }
        } else {
            this.messageStash.add(message);
        }

        if (!this.manager.getProxy().equals(this.manager.getSelf())) {
            this.sendMessageSynchronized(this.manager.getProxy(), message);
        }
    }

    public void sendMessageToProxy(ChannelMessage<?, ?> message) {
        sendMessage(this.manager.getProxy(), message);
    }

    public final void sendMessage(Host host, ChannelMessage<?, ?> message) {
        new Thread(() -> this.sendMessageSynchronized(host, message)).start();
    }

    protected final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message) {
        this.sendMessageSynchronized(host, message, 0);
    }

    protected final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message, int retry) {
        Socket socket = this.socketByHost.computeIfAbsent(host, h -> {
            try {
                return new Socket(host.getHostname(), host.getPort());
            } catch (IOException e) {
                return null;
            }
        });

        if (socket == null) {
            if (retry >= Channel.CONNECTION_RETRIES) {
                de.timesnake.channel.util.Channel.LOGGER.warning("Failed to setup connection to '" + host.getHostname() + ":" + host.getPort() + "'");
                return;
            }
            this.sendMessageSynchronized(host, message, retry + 1);
            return;
        }

        try {
            if (socket.isConnected()) {
                OutputStreamWriter socketWriter = new OutputStreamWriter(socket.getOutputStream());
                socketWriter.write(message.toStream());
                socketWriter.write(System.lineSeparator());
                socketWriter.flush();
                de.timesnake.channel.util.Channel.LOGGER.info("Message send to " + host + ": '" + message.toStream() + "'");
            } else {
                this.sendMessageSynchronized(host, message, retry + 1);
            }
        } catch (IOException ignored) {
        }
    }
}
