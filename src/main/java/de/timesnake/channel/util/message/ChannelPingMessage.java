/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelPingMessage extends ChannelMessage<String, Void> {

    public ChannelPingMessage(String... args) {
        super(args);
    }

    public ChannelPingMessage(String senderName, MessageType<Void> type) {
        super(ChannelType.PING, senderName, type);
        if (!MessageType.Ping.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public String getSenderName() {
        return this.getIdentifier();
    }

    @Override
    public MessageType<Void> getMessageType() {
        return null;
    }
}
