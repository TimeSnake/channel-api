package de.timesnake.channel.core;

import de.timesnake.channel.util.message.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * This class manages the communication with other sockets
 */
public abstract class ChannelServer implements Runnable {

    public static final Integer ADD = 10000;

    protected static final String LISTEN_IP = "0.0.0.0";
    protected static final String SERVER_IP = "127.0.0.1";

    protected final Thread mainThread;

    protected final Integer serverPort;
    protected final Host self;

    protected final Integer proxyPort;
    protected final Host proxy;

    // stash until server is registered
    protected boolean serverMessageServersRegistered = false;
    protected List<ChannelServerMessage<?>> serverMessages = new LinkedList<>();

    // Already send server messages
    protected List<Integer> sendListenerPorts = new LinkedList<>();
    protected boolean sendListenerMessageTypeAll = false;
    protected List<MessageType<?>> sendListenerMessageTypes = new LinkedList<>();

    /**
     * Only for servers, which are interested
     */
    protected HashMap<Host, Set<MessageType<?>>> receiverServerListeners = new HashMap<>();

    protected ChannelServer(Thread mainThread, int serverPort, int proxy) {
        this.mainThread = mainThread;

        this.serverPort = serverPort;
        this.self = new Host(SERVER_IP, serverPort + Channel.ADD);

        this.proxyPort = proxy;
        this.proxy = new Host(SERVER_IP, proxy + Channel.ADD);
    }

    @Override
    public void run() {
        try {
            this.startServer();
        } catch (Exception e) {
            System.out.println("[Channel] Error while starting channel-server");
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

    protected void addServerListener(ChannelListenerMessage<?> msg) {
        Host senderHost = msg.getSenderHost();

        if (msg.getMessageType().equals(MessageType.Listener.SERVER_PORT)) {
            this.receiverServerListeners.put(senderHost, null);
        } else if (msg.getMessageType().equals(MessageType.Listener.SERVER_MESSAGE_TYPE)) {
            if (receiverServerListeners.containsKey(senderHost)) {
                Set<MessageType<?>> typeSet = receiverServerListeners.get(msg.getSenderHost());
                if (typeSet != null) {
                    if (msg.getValue() != null) {
                        typeSet.add(((MessageType<?>) msg.getValue()));
                    } else {
                        this.receiverServerListeners.put(msg.getSenderHost(), null);
                    }
                }
            } else {
                Set<MessageType<?>> typeSet = new HashSet<>();

                if (msg.getValue() != null) {
                    typeSet.add(((MessageType<?>) msg.getValue()));
                } else {
                    typeSet = null;
                }

                receiverServerListeners.put(senderHost, typeSet);
            }
        }
        ChannelInfo.broadcastMessage("Server listener added");
    }

    private void handleMessage(Socket socket) {
        try {
            BufferedReader socketReader;
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inMsg;
            while ((inMsg = socketReader.readLine()) != null) {
                ChannelInfo.broadcastMessage("Message received: " + inMsg);
                String[] args = inMsg.split(ChannelMessage.DIVIDER);
                ChannelType<?> type = ChannelType.valueOf(args[0]);

                if (ChannelType.PING.equals(type)) {
                    this.handlePingMessage(new ChannelPingMessage(args));
                    continue;
                }

                if (ChannelType.LISTENER.equals(type)) {
                    this.runSync(() -> {
                        ChannelListenerMessage<?> listenerMessage = new ChannelListenerMessage<>(args);
                        this.handleListenerMessage(listenerMessage);
                    });
                    continue;
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
                        ChannelInfo.broadcastMessage("[Channel] Error while reading channel type");
                    }

                    if (msg != null) {
                        this.handleMessage(msg);
                    }
                });

            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ChannelMessage<?, ?> message) {
        if (message instanceof ChannelServerMessage) {
            if (serverMessageServersRegistered) {
                for (Map.Entry<Host, Set<MessageType<?>>> entry : this.receiverServerListeners.entrySet()) {
                    Set<MessageType<?>> typeSet = entry.getValue();
                    Host host = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessage(host, message);
                    } else if (typeSet.contains(message.getMessageType())) {
                        this.sendMessage(host, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage<?>) message);
            }
        }
        if (!this.proxy.equals(this.self)) {
            this.sendMessageToProxy(message);
        }
    }


    public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
        if (message instanceof ChannelServerMessage) {
            if (serverMessageServersRegistered) {
                for (Map.Entry<Host, Set<MessageType<?>>> entry : this.receiverServerListeners.entrySet()) {
                    Set<MessageType<?>> typeSet = entry.getValue();
                    Host host = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessageSynchronized(host, message);
                    } else if (typeSet.contains(message.getMessageType())) {
                        this.sendMessageSynchronized(host, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage<?>) message);
            }
        }
        if (!this.proxy.equals(this.self)) {
            this.sendMessageSynchronized(proxy, message);
        }
    }

    public void sendMessageToProxy(ChannelMessage<?, ?> message) {
        sendMessage(proxy, message);
    }

    public final void sendMessage(Host host, ChannelMessage<?, ?> message) {
        Thread sender = new Thread(new ChannelMessageSender(host, message.toStream()));
        sender.start();
        ChannelInfo.broadcastMessage("Message send: " + message.toStream() + " to " + host);
    }

    public final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message) {
        Socket socket = null;
        try {
            socket = new Socket(host.getHostname(), host.getPort());
        } catch (IOException ignored) {
        }

        if (socket != null) {
            try {
                if (socket.isConnected()) {
                    BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    socketWriter.write(message.toStream());
                    socketWriter.flush();
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
        ChannelInfo.broadcastMessage("Message send: " + message.toStream() + " to " + host);
    }

    public abstract void runSync(SyncRun syncRun);

    protected abstract void handleMessage(ChannelMessage<?, ?> msg);

    protected abstract void handlePingMessage(ChannelPingMessage msg);

    protected synchronized void handleListenerMessage(ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.SERVER_MESSAGE_TYPE) || (msg.getMessageType().equals(MessageType.Listener.SERVER_PORT) && !msg.getSenderHost().equals(this.self))) {
            this.addServerListener(msg);
        } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER_SERVER) && msg.getSenderHost().equals(this.proxy)) {
            this.serverMessageServersRegistered = true;
            for (ChannelServerMessage<?> serverMsg : serverMessages) {
                this.sendMessage(serverMsg);
            }
            ChannelInfo.broadcastMessage("Receiving of listeners finished");
        } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER_SERVER)) {
            Host host = msg.getSenderHost();

            if (host != null) {
                this.receiverServerListeners.remove(host);
                ChannelInfo.broadcastMessage("Removed server-listener " + host);
            }
        }
    }
}
