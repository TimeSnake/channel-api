/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.InconsistentChannelListenerException;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.Listener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

/**
 * This class manages all and only local channel listeners
 */
public class ChannelClient {

    /**
     * Checks if server is interested in message.
     * <p>
     * If message is server identifier listener, when check if listener identifier is equals with
     * receiver server name.
     * </p>
     *
     * @param host       Host to send to
     * @param serverName Name of server to send to
     * @param msg        Listener message, which is sent
     * @return true if message is interesting for server, else false
     */
    public static boolean isInterestingForServer(Host host, String serverName,
            ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
            if (((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType()
                    .equals(ChannelType.SERVER)) {
                if (!serverName.equals(
                        ((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier())) {
                    return false;
                }
            }
        }
        return !msg.getSenderHost().equals(host);
    }

    protected final ChannelBasis manager;

    // Already send server messages
    protected Set<String> sendListenerNames = ConcurrentHashMap.newKeySet();
    protected boolean sendListenerMessageTypeAll = false;
    protected Set<MessageType<?>> sendListenerMessageTypes = ConcurrentHashMap.newKeySet();

    // Already send log messages
    protected Set<String> sendLoggingListeners = ConcurrentHashMap.newKeySet();
    protected boolean sendLoggingListenerAll = false;

    /**
     * Only for servers, which are interested
     */
    protected ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<Object, Set<Host>>>
            listenerHostByIdentifierByChannelType = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<MessageType<?>, Set<Host>>>
            listenerHostByMessageTypeByChannelType = new ConcurrentHashMap<>();


    protected ConcurrentHashMap<Host, Socket> socketByHost = new ConcurrentHashMap<>();
    protected Set<Socket> loggingSockets = ConcurrentHashMap.newKeySet();

    // stash until server is registered
    protected boolean listenerLoaded = false;
    protected Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();


    public ChannelClient(ChannelBasis manager) {
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
                if (this.sendListenerMessageTypeAll ||
                        (type.getMessageType() != null && this.sendListenerMessageTypes.contains(
                                type.getMessageType()))) {
                    return;
                }

                if (type.getMessageType() == null) {
                    this.sendListenerMessageTypeAll = true;
                } else {
                    this.sendListenerMessageTypes.add(type.getMessageType());
                }

                this.sendMessage(new ChannelListenerMessage<>(this.manager.getSelf(),
                        MessageType.Listener.MESSAGE_TYPE_LISTENER,
                        new MessageType.MessageTypeListener(ChannelType.SERVER,
                                type.getMessageType())));
            }
        } else if (type.getChannelType().equals(ChannelType.LOGGING)) {
            if (filter != null && filter.getIdentifierFilter() != null) {
                Collection<String> identifiers;

                try {
                    identifiers = (Collection<String>) filter.getIdentifierFilter();
                } catch (ClassCastException e) {
                    throw new InconsistentChannelListenerException("invalid filter type");
                }

                for (String name : identifiers) {
                    if (this.sendLoggingListeners.contains(name)) {
                        continue;
                    }

                    this.sendLoggingListeners.add(name);

                    this.sendMessage(new ChannelListenerMessage<>(this.manager.getSelf(),
                            Listener.IDENTIFIER_LISTENER,
                            new MessageType.MessageIdentifierListener<>(ChannelType.LOGGING,
                                    name)));
                }
            } else {
                if (this.sendLoggingListenerAll ||
                        (type.getMessageType() != null && this.sendListenerMessageTypes.contains(
                                type.getMessageType()))) {
                    return;
                }

                if (type.getMessageType() == null) {
                    this.sendLoggingListenerAll = true;
                } else {
                    this.sendListenerMessageTypes.add(type.getMessageType());
                }

                this.sendMessage(new ChannelListenerMessage<>(this.manager.getSelf(),
                        Listener.MESSAGE_TYPE_LISTENER,
                        new MessageType.MessageTypeListener(ChannelType.LOGGING,
                                type.getMessageType())));
            }
        }
    }

    protected void addLogListener(Host host) {
        Logger logger = Logger.getLogger("");
        try {
            logger.addHandler(new SocketHandler(host.getHostname(), host.getPort()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void sendMessage(ChannelMessage<?, ?> message) {
        new Thread(() -> this.sendMessageSynchronized(message)).start();
    }

    public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
        boolean sendToProxy = false;

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
                if (host.equals(this.manager.getProxy())) {
                    sendToProxy = true;
                }
                this.sendMessageSynchronized(host, message);
            }
        } else {
            this.messageStash.add(message);
        }

        if (!sendToProxy && !this.manager.getProxy().equals(this.manager.getSelf())) {
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

    protected final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message,
            int retry) {
        Socket socket = this.socketByHost.computeIfAbsent(host, h -> {
            try {
                return new Socket(host.getHostname(), host.getPort());
            } catch (IOException e) {
                return null;
            }
        });

        if (socket == null) {
            if (retry >= Channel.CONNECTION_RETRIES) {
                de.timesnake.channel.util.Channel.LOGGER.warning(
                        "Failed to setup connection to '" + host.getHostname() + ":"
                                + host.getPort() + "'");
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
                de.timesnake.channel.util.Channel.LOGGER.info(
                        "Message send to " + host + ": '" + message.toStream() + "'");
            } else {
                this.sendMessageSynchronized(host, message, retry + 1);
            }
        } catch (IOException ignored) {
        }
    }
}
