package de.timesnake.channel.core;

import de.timesnake.channel.util.message.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public abstract class ChannelServer implements Runnable {

    public static final Integer ADD = 10000;

    protected static final String SERVER_IP = "localhost";

    protected final Thread mainThread;

    protected Integer serverPort;
    protected Integer channelServerPort;
    protected Integer proxyPort;

    protected boolean serverMessageServersRegistered = false;
    protected List<ChannelServerMessage<?>> serverMessages = new LinkedList<>();

    protected List<Integer> sendListenerPorts = new LinkedList<>();

    protected boolean sendListenerMessageTypeAll = false;
    protected List<MessageType<?>> sendListenerMessageTypes = new LinkedList<>();

    /**
     * Only for servers, which interested
     */
    protected HashMap<Integer, Set<MessageType<?>>> receiverServerListeners = new HashMap<>();

    protected ChannelServer(Thread mainThread, int serverPort, int proxyPort) {
        this.mainThread = mainThread;
        this.serverPort = serverPort;
        this.channelServerPort = serverPort + Channel.ADD;
        this.proxyPort = proxyPort;
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
        ServerSocket serverSocket = new ServerSocket(channelServerPort, 100, InetAddress.getByName(SERVER_IP));

        while (true) {
            final Socket activeSocket = serverSocket.accept();
            Runnable runnable = () -> handleMessage(activeSocket);
            new Thread(runnable).start();
        }
    }

    protected void addServerListener(ChannelListenerMessage<?> msg) {
        Integer port = msg.getSenderPort();

        if (msg.getMessageType().equals(MessageType.Listener.SERVER_PORT)) {
            this.receiverServerListeners.put(port, null);
        } else if (msg.getMessageType().equals(MessageType.Listener.SERVER_MESSAGE_TYPE)) {
            if (receiverServerListeners.containsKey(port)) {
                Set<MessageType<?>> typeSet = receiverServerListeners.get(msg.getSenderPort());
                if (typeSet != null) {
                    if (msg.getValue() != null) {
                        typeSet.add(((MessageType<?>) msg.getValue()));
                    } else {
                        this.receiverServerListeners.put(msg.getSenderPort(), null);
                    }
                }
            } else {
                Set<MessageType<?>> typeSet = new HashSet<>();

                if (msg.getValue() != null) {
                    typeSet.add(((MessageType<?>) msg.getValue()));
                } else {
                    typeSet = null;
                }

                receiverServerListeners.put(port, typeSet);
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
                    } else {
                        ChannelInfo.broadcastMessage("[Channel] Error while reading channel type");
                    }

                    this.handleMessage(msg);
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
                for (Map.Entry<Integer, Set<MessageType<?>>> entry : this.receiverServerListeners.entrySet()) {
                    Set<MessageType<?>> typeSet = entry.getValue();
                    Integer port = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessage(port, message);
                    } else if (typeSet.contains(message.getMessageType())) {
                        this.sendMessage(port, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage<?>) message);
            }
        }
        if (!this.proxyPort.equals(this.serverPort)) {
            this.sendMessageToProxy(message);
        }
    }


    public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
        if (message instanceof ChannelServerMessage) {
            if (serverMessageServersRegistered) {
                for (Map.Entry<Integer, Set<MessageType<?>>> entry : this.receiverServerListeners.entrySet()) {
                    Set<MessageType<?>> typeSet = entry.getValue();
                    Integer port = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessageSynchronized(port, message);
                    } else if (typeSet.contains(message.getMessageType())) {
                        this.sendMessageSynchronized(port, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage<?>) message);
            }
        }
        if (!this.proxyPort.equals(this.serverPort)) {
            this.sendMessageSynchronized(proxyPort, message);
        }
    }

    public void sendMessageToProxy(ChannelMessage<?, ?> message) {
        sendMessage(proxyPort, message);
    }

    public final void sendMessage(int port, ChannelMessage<?, ?> message) {
        Thread sender = new Thread(new ChannelMessageSender(port, message.toStream()));
        sender.start();
        ChannelInfo.broadcastMessage("Message send: " + message.toStream() + " to " + port);
    }

    public final void sendMessageSynchronized(int port, ChannelMessage<?, ?> message) {
        int channelPort = port + Channel.ADD;
        Socket socket = null;
        try {
            socket = new Socket(SERVER_IP, channelPort);
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
        ChannelInfo.broadcastMessage("Message send: " + message.toStream() + " to " + port);
    }

    public abstract void runSync(SyncRun syncRun);

    protected abstract void handleMessage(ChannelMessage<?, ?> msg);

    protected abstract void handlePingMessage(ChannelPingMessage msg);

    protected synchronized void handleListenerMessage(ChannelListenerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Listener.SERVER_MESSAGE_TYPE) || (msg.getMessageType().equals(MessageType.Listener.SERVER_PORT) && !msg.getSenderPort().equals(this.serverPort))) {
            this.addServerListener(msg);
        } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER) && msg.getSenderPort().equals(this.proxyPort)) {
            this.serverMessageServersRegistered = true;
            for (ChannelServerMessage<?> serverMsg : serverMessages) {
                this.sendMessage(serverMsg);
            }
            ChannelInfo.broadcastMessage("Receiving of listeners finished");
        } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER)) {
            Integer port = msg.getSenderPort();

            if (port != null) {
                this.receiverServerListeners.remove(port);
                ChannelInfo.broadcastMessage("Removed server-listener " + port);
            }
        }
    }
}
