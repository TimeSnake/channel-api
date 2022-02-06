package de.timesnake.channel.listener;

import de.timesnake.channel.api.message.ChannelGroupMessage;

@FunctionalInterface
public interface ChannelGroupListener extends ChannelListener {

    /**
     * @param msg value is always a string
     */
    void onGroupMessage(ChannelGroupMessage msg);
}
