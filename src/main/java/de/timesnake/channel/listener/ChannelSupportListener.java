package de.timesnake.channel.listener;

import de.timesnake.channel.api.message.ChannelSupportMessage;

@FunctionalInterface
public interface ChannelSupportListener {

    void onSupportMessage(ChannelSupportMessage msg);
}
