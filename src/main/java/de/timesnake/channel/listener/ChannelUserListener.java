package de.timesnake.channel.listener;

import de.timesnake.channel.api.message.ChannelUserMessage;

@FunctionalInterface
public interface ChannelUserListener extends ChannelListener {

    /**
     * @param msg value is always a string
     */
    void onUserMessage(ChannelUserMessage msg);
}
