package de.timesnake.channel.channel;

import de.timesnake.channel.api.message.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public abstract class ChannelServer implements Runnable {

    protected static final String SERVER_IP = "localhost";

    protected final Thread mainThread;

    protected Integer serverPort;
    protected Integer channelServerPort;
    protected Integer proxyPort;

    protected boolean serverMessageServersRegistered = false;
    protected List<ChannelServerMessage> serverMessages = new ArrayList<>();

    /**
     * Only for servers, which interested
     */
    protected HashMap<Integer, Set<ChannelServerMessage.MessageType>> receiverServerListeners = new HashMap<>();

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

    protected void addServerListener(ChannelListenerMessage msg) {
        Integer port = null;
        try {
            port = Integer.valueOf(msg.getValue());
        } catch (NumberFormatException ignored) {
        }
        if (port != null) {
            receiverServerListeners.put(msg.getPort(), null);
        } else {
            if (receiverServerListeners.containsKey(msg.getPort())) {
                Set<ChannelServerMessage.MessageType> typeSet = receiverServerListeners.get(msg.getPort());
                if (typeSet != null) {
                    typeSet.add(ChannelServerMessage.MessageType.valueOf(msg.getValue()));
                    receiverServerListeners.put(msg.getPort(), typeSet);
                }
            } else {
                Set<ChannelServerMessage.MessageType> typeSet = new HashSet<>();
                typeSet.add(ChannelServerMessage.MessageType.valueOf(msg.getValue()));
                receiverServerListeners.put(msg.getPort(), typeSet);
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
                String[] args = inMsg.split(de.timesnake.channel.message.ChannelMessage.DIVIDER);
                ChannelType type = ChannelType.valueOf(args[0]);

                switch (type) {
                    case SERVER:
                        this.runSync(() -> {
                            ChannelServerMessage msg = new de.timesnake.channel.message.ChannelServerMessage(args);
                            this.handleServerMessage(msg);
                        });
                        break;
                    case USER:
                        this.runSync(() -> {
                            ChannelUserMessage msg = new de.timesnake.channel.message.ChannelUserMessage(args);
                            this.handleUserMessage(msg);
                        });
                        break;
                    case GROUP:
                        this.runSync(() -> {
                            ChannelGroupMessage msg = new de.timesnake.channel.message.ChannelGroupMessage(args);
                            this.handleGroupMessage(msg);
                        });
                        break;
                    case LISTENER:
                        this.runSync(() -> {
                            ChannelListenerMessage msg = new de.timesnake.channel.message.ChannelListenerMessage(args);
                            this.handleListenerMessage(msg);
                        });
                        break;
                    case SUPPORT:
                        this.runSync(() -> {
                            ChannelSupportMessage msg = new de.timesnake.channel.message.ChannelSupportMessage(args);
                            this.handleSupportMessage(msg);
                        });
                        break;
                    case PING:
                        ChannelPingMessage msg = new de.timesnake.channel.message.ChannelPingMessage(args);
                        this.handlePingMessage(msg);
                        break;
                    default:
                        System.out.println("[Channel] Error while reading channel type");
                        break;
                }

            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ChannelMessage message) {
        if (message instanceof ChannelServerMessage) {
            if (serverMessageServersRegistered) {
                for (Map.Entry<Integer, Set<ChannelServerMessage.MessageType>> entry : receiverServerListeners.entrySet()) {
                    Set<ChannelServerMessage.MessageType> typeSet = entry.getValue();
                    Integer port = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessage(port, message);
                    } else if (typeSet.contains(((ChannelServerMessage) message).getType())) {
                        this.sendMessage(port, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage) message);
            }
        }
        if (!this.proxyPort.equals(this.serverPort)) {
            this.sendMessageToProxy(message);
        }
    }

    public void sendMessageSynchronized(ChannelMessage message) {
        if (message instanceof ChannelServerMessage) {
            if (serverMessageServersRegistered) {
                for (Map.Entry<Integer, Set<ChannelServerMessage.MessageType>> entry : receiverServerListeners.entrySet()) {
                    Set<ChannelServerMessage.MessageType> typeSet = entry.getValue();
                    Integer port = entry.getKey();
                    if (typeSet == null || typeSet.isEmpty()) {
                        this.sendMessageSynchronized(port, message);
                    } else if (typeSet.contains(((ChannelServerMessage) message).getType())) {
                        this.sendMessageSynchronized(port, message);
                    }
                }
            } else {
                this.serverMessages.add((ChannelServerMessage) message);
            }
        }
        if (!this.proxyPort.equals(this.serverPort)) {
            this.sendMessageSynchronized(proxyPort, message);
        }
    }

    public void sendMessageToProxy(ChannelMessage message) {
        sendMessage(proxyPort, message);
    }

    public final void sendMessage(int port, ChannelMessage message) {
        Thread sender = new Thread(new ChannelMessageSender(port, message.toString()));
        sender.start();
        ChannelInfo.broadcastMessage("Message send: " + message.toString() + " to " + port);
    }

    public final void sendMessageSynchronized(int port, ChannelMessage message) {
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
                    // senderPort/type/task</value></value>
                    socketWriter.write(message.toString());
                    socketWriter.flush();
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
        ChannelInfo.broadcastMessage("Message send: " + message.toString() + " to " + port);
    }

    public abstract void runSync(SyncRun syncRun);

    protected abstract void handleServerMessage(ChannelServerMessage msg);

    protected abstract void handleUserMessage(ChannelUserMessage msg);

    protected abstract void handleGroupMessage(ChannelGroupMessage msg);

    protected abstract void handleSupportMessage(ChannelSupportMessage msg);

    protected abstract void handlePingMessage(ChannelPingMessage msg);

    protected synchronized void handleListenerMessage(ChannelListenerMessage msg) {
        if (msg.getType().equals(ChannelListenerMessage.MessageType.SERVER) && !msg.getPort().equals(this.serverPort)) {
            this.addServerListener(msg);
        } else if (msg.getType().equals(ChannelListenerMessage.MessageType.CHANNEL) && msg.getPort().equals(this.proxyPort)) {
            if (msg.getValue() == null) {
                this.serverMessageServersRegistered = true;
                for (ChannelServerMessage serverMsg : serverMessages) {
                    this.sendMessage(serverMsg);
                }
                ChannelInfo.broadcastMessage("Receiving of listeners finished");
            } else {
                Integer port = null;
                try {
                    port = Integer.valueOf(msg.getValue());
                } catch (NumberFormatException ignored) {
                }
                if (port != null) {
                    this.receiverServerListeners.remove(port);
                    ChannelInfo.broadcastMessage("Removed server-listener " + port);
                }
            }
        }
    }
}
