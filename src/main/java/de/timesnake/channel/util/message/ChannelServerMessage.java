/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelServerMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelServerMessage(String... args) {
        super(args);
    }

    public ChannelServerMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.SERVER, name, type, value);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelServerMessage(String name, MessageType<Value> type) {
        super(ChannelType.SERVER, name, type);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public String getName() {
        return super.getIdentifier();
    }

    public enum State {
        READY
    }
}
