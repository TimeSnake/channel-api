/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelTemplatesMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelTemplatesMessage(String... args) {
        super(args);
    }

    public ChannelTemplatesMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.TEMPLATES, name, type, value);
        if (!MessageType.Templates.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }
}
