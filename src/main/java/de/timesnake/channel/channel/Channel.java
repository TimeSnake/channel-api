package de.timesnake.channel.channel;

import de.timesnake.channel.api.message.*;
import de.timesnake.channel.listener.ChannelGroupListener;
import de.timesnake.channel.listener.ChannelServerListener;
import de.timesnake.channel.listener.ChannelSupportListener;
import de.timesnake.channel.listener.ChannelUserListener;

import java.util.*;

public abstract class Channel extends ChannelServer implements de.timesnake.channel.api.Channel {

    public static final Integer ADD = 10000;

    //local user listeners
    protected ChannelMap<ChannelUserListener, List<Object>> userListeners = new ChannelMap<>();

    //local server listeners
    protected ChannelMap<ChannelServerListener, List<Object>> serverListeners = new ChannelMap<>();
    //already send server listeners
    protected Set<Object> sendServerListener = new HashSet<>();

    //local group listeners
    protected ChannelMap<ChannelGroupListener, Object> groupListeners = new ChannelMap<>();

    //local support listeners
    protected ChannelMap<ChannelSupportListener, Object> supportListeners = new ChannelMap<>();

    public Channel(Thread mainThread, Integer serverPort, Integer proxyPort) {
        super(mainThread, serverPort, proxyPort);
        this.serverPort = serverPort;
        this.proxyPort = proxyPort;
    }

    /**
     * Adds the user-channel-listener to the channel
     *
     * @param listener The {@link ChannelUserListener} to add
     * @param uuid     The listen {@link UUID}
     */
    @Override
    public void addUserListener(ChannelUserListener listener, UUID uuid) {
        this.addUserListener(listener, (Object) uuid);
    }

    /**
     * Adds the user-channel-listener to the channel
     *
     * @param listener The {@link ChannelUserListener} to add
     * @param types    The listen {@link ChannelUserMessage.MessageType}s
     */
    @Override
    public void addUserListener(ChannelUserListener listener, ChannelUserMessage.MessageType... types) {
        this.addUserListener(listener, types);
    }

    private void addUserListener(ChannelUserListener listener, Object... objs) {
        for (Object obj : objs) {
            List<Object> list = this.userListeners.get(listener);
            if (list == null) {
                list = new ArrayList<>();
                this.userListeners.put(listener, list);
            }

            if (!list.contains(obj)) {
                list.add(obj);
            }
        }
    }

    /**
     * Removes the user-channel-listener completely from the set
     *
     * @param listener The {@link ChannelUserListener} to remove
     */
    @Override
    public void removeUserListener(ChannelUserListener listener) {
        this.userListeners.remove(listener);
    }

    /**
     * Adds the server-channel-listener
     *
     * @param listener The {@link ChannelServerListener} to add
     * @param port     The listen {@link Integer} of the port
     */
    @Override
    public void addServerListener(ChannelServerListener listener, Integer port) {

        List<Object> list = this.serverListeners.get(listener);
        if (list == null) {
            list = new ArrayList<>();
            this.serverListeners.put(listener, list);
        }

        if (!list.contains(port)) {
            list.add(port);
        }

        if (this.serverPort.equals(port)) {
            return;
        }

        if (!this.sendServerListener.contains(port)) {
            this.sendMessage(ChannelListenerMessage.getServerMessage(this.serverPort, port));
        }

        this.sendServerListener.add(port);

    }

    /**
     * Adds the server-channel-listener
     *
     * @param listener The {@link ChannelServerListener} to add
     * @param types    The listen {@link ChannelServerMessage.MessageType}s
     */
    @Override
    public void addServerListener(ChannelServerListener listener, ChannelServerMessage.MessageType... types) {

        List<Object> list = this.serverListeners.get(listener);
        if (list == null) {
            list = new ArrayList<>();
            this.serverListeners.put(listener, list);
        }
        for (ChannelServerMessage.MessageType type : types) {
            if (!list.contains(type)) {
                list.add(type);
                this.serverListeners.put(listener, list);
            }

            if (!this.sendServerListener.contains(type)) {
                this.sendMessage(ChannelListenerMessage.getServerMessage(this.serverPort, type));
            }

            this.sendServerListener.add(type);
        }
    }

    /**
     * Removes server-channel-listener from set
     * <p>
     * Not sends unregister to other servers
     *
     * @param listener The {@link ChannelServerListener} to remove
     */
    @Override
    public void removeServerListener(ChannelServerListener listener) {
        this.serverListeners.remove(listener);
    }

    /**
     * Adds the group-channel-listener
     *
     * @param listener The {@link ChannelGroupListener} to add
     */
    @Override
    public void addGroupListener(ChannelGroupListener listener) {
        this.groupListeners.put(listener, null);
    }

    /**
     * Removes group-channel-listener from set
     *
     * @param listener The {@link ChannelGroupListener} to remove
     */
    @Override
    public void removeGroupListener(ChannelGroupListener listener) {
        this.groupListeners.remove(listener);
    }

    /**
     * Adds the group-channel-listener
     *
     * @param listener The {@link ChannelSupportListener} to add
     */
    @Override
    public void addSupportListener(ChannelSupportListener listener) {
        this.supportListeners.put(listener, null);
    }

    /**
     * Removes group-channel-listener from set
     *
     * @param listener The {@link ChannelSupportListener} to remove
     */
    @Override
    public void removeSupportListener(ChannelSupportListener listener) {
        this.supportListeners.remove(listener);
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    @Override
    protected void handleServerMessage(ChannelServerMessage msg) {

        this.serverListeners.iterateEntries((listener, set) -> {

            for (Object value : set) {
                if (value == null) {
                    listener.onServerMessage(msg);
                } else if (value instanceof Integer) {
                    if (value.equals(msg.getPort())) {
                        listener.onServerMessage(msg);
                    }
                } else if (value instanceof ChannelServerMessage.MessageType) {
                    if (value.equals(msg.getType())) {
                        listener.onServerMessage(msg);
                    }
                }
            }
        });
    }

    @Override
    protected void handleUserMessage(ChannelUserMessage msg) {
        this.userListeners.iterateEntries((listener, set) -> {
            for (Object value : set) {
                if (value == null) {
                    listener.onUserMessage(msg);
                }
                if (value instanceof UUID) {
                    if (value.equals(msg.getUniqueId())) {
                        listener.onUserMessage(msg);
                    }
                } else if (value instanceof ChannelUserMessage.MessageType) {
                    if (value.equals(msg.getType())) {
                        listener.onUserMessage(msg);
                    }
                }
            }
        });
    }

    @Override
    protected void handleGroupMessage(ChannelGroupMessage msg) {
        this.groupListeners.iterateEntries((key, value) -> {
            if (value == null) {
                key.onGroupMessage(msg);
            }
        });
    }

    @Override
    protected void handleSupportMessage(ChannelSupportMessage msg) {
        this.supportListeners.iterateEntries((key, value) -> {
            if (value == null) {
                key.onSupportMessage(msg);
            }
        });
    }

    @Override
    protected void handlePingMessage(ChannelPingMessage message) {
        this.sendMessageToProxy(ChannelPingMessage.getPingMessage(this.serverPort));
    }
}
