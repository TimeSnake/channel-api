/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelSupportMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelSupportMessage(String... args) {
        super(args);
    }

    public ChannelSupportMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.SUPPORT, name, type, value);
        if (!MessageType.Support.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public String getName() {
        return super.getIdentifier();
    }

}
