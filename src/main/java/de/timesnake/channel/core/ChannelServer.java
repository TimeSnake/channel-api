/*
 * workspace.channel-api.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages the communication with other sockets
 */
public abstract class ChannelServer implements Runnable {

    public static final String PROXY_NAME = "proxy";

    public static final Integer ADD = 10000;

    public static boolean isInterestingForServer(Host host, String serverName, ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
            if (serverName.equals(((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier())) {
                return false;
            }
        }
        return !msg.getSenderHost().equals(host);
    }

    protected static final String LISTEN_IP = "0.0.0.0";
    protected static final String SERVER_IP = "127.0.0.1";
    private static final int CONNECTION_RETRIES = 3;
    protected final Thread mainThread;

    protected final String serverName;
    protected final Integer serverPort;
    protected final Host self;

    protected final Integer proxyPort;
    protected final Host proxy;


    protected ConcurrentHashMap<Host, Socket> socketByHost = new ConcurrentHashMap<>();

    // stash until server is registered
    protected boolean listenerLoaded = false;
    protected Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();

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

    protected ChannelServer(Thread mainThread, String serverName, int serverPort, int proxy) {
        this.mainThread = mainThread;

        this.serverName = serverName;
        this.serverPort = serverPort;
        this.self = new Host(SERVER_IP, serverPort + Channel.ADD);

        this.proxyPort = proxy;
        this.proxy = new Host(SERVER_IP, proxy + Channel.ADD);

        for (ChannelType<?> type : ChannelType.TYPES) {
            this.listenerHostByIdentifierByChannelType.put(type, new ConcurrentHashMap<>());
        }

        for (ChannelType<?> type : ChannelType.TYPES) {
            this.listenerHostByMessageTypeByChannelType.put(type, new ConcurrentHashMap<>());
        }
    }

    @Override
    public void run() {
        try {
            this.startServer();
        } catch (Exception e) {
            Channel.LOGGER.warning("Error while starting channel-server");
        }
    }

    @SuppressWarnings("resource")
    private void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(this.self.getPort(), 100, InetAddress.getByName(LISTEN_IP));

        while (true) {
            final Socket activeSocket = serverSocket.accept();
            Runnable runnable = () -> handleMessage(activeSocket);
            new Thread(runnable).start();
        }
    }

    protected void addListener(ChannelListenerMessage<?> msg) {
        Host senderHost = msg.getSenderHost();

        if (senderHost.equals(this.self)) {
            return;
        }

        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
            // prevent registration of server listeners, which are not belonging to this server
            if (((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType().equals(ChannelType.SERVER)
                    && !((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier().equals(this.serverName)) {
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
        Channel.LOGGER.info("Listener added");
    }

    private void handleMessage(Socket socket) {
        try {

            BufferedReader socketReader;
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inMsg;
            while ((inMsg = socketReader.readLine()) != null) {
                Channel.LOGGER.info("Message received: " + inMsg);
                String[] args = inMsg.split(ChannelMessage.DIVIDER, 4);

                ChannelType<?> type = ChannelType.valueOf(args[0]);
                if (ChannelType.LISTENER.equals(type)) {
                    ChannelListenerMessage<?> msg = new ChannelListenerMessage<>(args);
                    if (msg.getMessageType().equals(MessageType.Listener.CLOSE_SOCKET)) {
                        socketReader.close();
                        socket.close();
                    }
                }
                this.handleMessage(args);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleMessage(String[] args) {
        ChannelType<?> type = ChannelType.valueOf(args[0]);

        if (ChannelType.PING.equals(type)) {
            this.handlePingMessage(new ChannelPingMessage(args));
            return;
        }

        if (ChannelType.LISTENER.equals(type)) {
            ChannelListenerMessage<?> msg = new ChannelListenerMessage<>(args);
            this.handleListenerMessage(msg);
            return;
        }

        this.runSync(() -> {
            ChannelMessage<?, ?> msg = null;

            if (ChannelType.SERVER.equals(type)) {
                msg = new ChannelServerMessage<>(args);
            } else if (ChannelType.USER.equals(type)) {
                msg = new ChannelUserMessage<>(args);
            } else if (ChannelType.GROUP.equals(type)) {
                msg = new ChannelGroupMessage<>(args);
            } else if (ChannelType.SUPPORT.equals(type)) {
                msg = new ChannelSupportMessage<>(args);
            } else if (ChannelType.DISCORD.equals(type)) {
                msg = new ChannelDiscordMessage<>(args);
            } else {
                Channel.LOGGER.warning("Error while reading channel type: '" + args[0] + "'");
            }

            if (msg != null) {
                this.handleMessage(msg);
            }
        });
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

        if (!this.proxy.equals(this.self)) {
            this.sendMessageSynchronized(proxy, message);
        }
    }

    public void sendMessageToProxy(ChannelMessage<?, ?> message) {
        sendMessage(proxy, message);
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
            if (retry >= CONNECTION_RETRIES) {
                Channel.LOGGER.warning("Failed to setup connection to '" + host.getHostname() + ":" + host.getPort() + "'");
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
                Channel.LOGGER.info("Message send to " + host + ": '" + message.toStream() + "'");
            }
        } catch (IOException ignored) {
        }
    }

    public abstract void runSync(SyncRun syncRun);

    protected abstract void handleMessage(ChannelMessage<?, ?> msg);

    protected abstract void handlePingMessage(ChannelPingMessage msg);

    protected synchronized void handleListenerMessage(ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)
                || msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
            this.addListener(msg);
        } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER_SERVER) && msg.getSenderHost().equals(this.proxy)) {
            Channel.LOGGER.info("Receiving of listeners finished");
            this.listenerLoaded = true;
            for (ChannelMessage<?, ?> serverMsg : this.messageStash) {
                this.sendMessage(serverMsg);
            }
            this.messageStash.clear();
        } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER_SERVER)) {
            Host host = msg.getSenderHost();

            if (host != null) {
                this.listenerHostByMessageTypeByChannelType.forEach((k, v) -> v.remove(host));
                Channel.LOGGER.info("Removed listener " + host);
            }

            this.disconnectHost(host);
        }
    }

    protected void disconnectHost(Host host) {
        try {
            Socket socket = this.socketByHost.remove(host);
            if (socket != null) {
                socket.close();
            }
            Channel.LOGGER.info("Closed socket to " + host);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
