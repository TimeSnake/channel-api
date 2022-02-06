package de.timesnake.channel.api;

import de.timesnake.channel.api.message.ChannelMessage;
import de.timesnake.channel.api.message.ChannelServerMessage;
import de.timesnake.channel.api.message.ChannelUserMessage;
import de.timesnake.channel.listener.ChannelGroupListener;
import de.timesnake.channel.listener.ChannelServerListener;
import de.timesnake.channel.listener.ChannelSupportListener;
import de.timesnake.channel.listener.ChannelUserListener;

import java.util.UUID;

public interface Channel {
    /**
     * Adds the user-channel-listener to the channel
     *
     * @param listener The {@link ChannelUserListener} to add
     * @param uuid     The listen {@link UUID}
     */
    void addUserListener(ChannelUserListener listener, UUID uuid);

    /**
     * Adds the user-channel-listener to the channel
     *
     * @param listener The {@link ChannelUserListener} to add
     * @param types    The listen {@link ChannelUserMessage.MessageType}s
     */
    void addUserListener(ChannelUserListener listener, ChannelUserMessage.MessageType... types);

    /**
     * Removes the user-channel-listener completely from the set
     *
     * @param listener The {@link ChannelUserListener} to remove
     */
    void removeUserListener(ChannelUserListener listener);

    /**
     * Adds the server-channel-listener
     *
     * @param listener The {@link ChannelServerListener} to add
     * @param port     The listen {@link Integer} of the port
     */
    void addServerListener(ChannelServerListener listener, Integer port);

    /**
     * Adds the server-channel-listener
     *
     * @param listener The {@link ChannelServerListener} to add
     * @param types    The listen {@link ChannelServerMessage.MessageType}s
     */
    void addServerListener(ChannelServerListener listener, ChannelServerMessage.MessageType... types);

    /**
     * Removes server-channel-listener from set
     * <p>
     * Not sends unregister to other servers
     *
     * @param listener The {@link ChannelServerListener} to remove
     */
    void removeServerListener(ChannelServerListener listener);

    /**
     * Adds the group-channel-listener
     *
     * @param listener The {@link ChannelGroupListener} to add
     */
    void addGroupListener(ChannelGroupListener listener);

    /**
     * Removes group-channel-listener from set
     *
     * @param listener The {@link ChannelGroupListener} to remove
     */
    void removeGroupListener(ChannelGroupListener listener);

    /**
     * Adds the group-channel-listener
     *
     * @param listener The {@link ChannelSupportListener} to add
     */
    void addSupportListener(ChannelSupportListener listener);

    /**
     * Removes group-channel-listener from set
     *
     * @param listener The {@link ChannelSupportListener} to remove
     */
    void removeSupportListener(ChannelSupportListener listener);

    void sendMessage(ChannelMessage message);

    void sendMessageSynchronized(ChannelMessage message);
}
