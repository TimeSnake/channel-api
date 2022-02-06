package de.timesnake.channel.listener;

import de.timesnake.channel.api.message.ChannelServerMessage;

@FunctionalInterface
public interface ChannelServerListener extends ChannelListener {

    /**
     * @param msg value is always a string
     */
    void onServerMessage(ChannelServerMessage msg);

}
