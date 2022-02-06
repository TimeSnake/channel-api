package de.timesnake.channel.api.message;

import de.timesnake.channel.MessageType;

public interface ChannelMessage {

    @Override
    String toString();

    MessageType getType();
}
