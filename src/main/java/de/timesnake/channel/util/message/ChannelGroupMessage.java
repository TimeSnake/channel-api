/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelGroupMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelGroupMessage(String... args) {
        super(args);
    }

    public ChannelGroupMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.GROUP, name, type, value);
        if (!MessageType.Group.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelGroupMessage(String name, MessageType<Value> type) {
        super(ChannelType.GROUP, name, type);
        if (!MessageType.Group.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }

    }

    public String getName() {
        return super.getIdentifier();
    }
}
