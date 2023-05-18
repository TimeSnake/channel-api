/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.core.UnknownTypeException;

public abstract class ChannelMessage<Identifier, Value> {

    public static final String DIVIDER = ";";

    protected ChannelType<Identifier> channelType;
    protected MessageType<Value> messageType;
    protected Identifier identifier;
    protected Value value;

    public ChannelMessage(String... args) {
        this.channelType = (ChannelType<Identifier>) ChannelType.valueOf(args[0]);

        if (this.channelType == null) {
            throw new UnknownTypeException("Unknown channel type '" + args[0] + "'");
        }

        this.messageType = (MessageType<Value>) channelType.parseMessageType(args[1]);

        if (messageType == null) {
            throw new UnknownTypeException("Unknown message type '" + args[1] + "' for channel '"
                    + args[0] + "'");
        }

        this.identifier = this.channelType.parseIdentifier(args[2]);
        if (args.length >= 4) {
            this.value = this.messageType.parseValue(args[3]);
        } else {
            this.value = this.messageType.parseValue("");
        }
    }

    public ChannelMessage(ChannelType<Identifier> channelType, Identifier identifier,
            MessageType<Value> messageType,
            Value value) {
        this.channelType = channelType;
        this.messageType = messageType;
        this.identifier = identifier;
        this.value = value;
    }


    public ChannelMessage(ChannelType<Identifier> channelType, Identifier identifier,
            MessageType<Value> messageType) {
        this.channelType = channelType;
        this.messageType = messageType;
        this.identifier = identifier;
    }

    public String toStream() {
        return String.join(DIVIDER,
                this.channelType.getName(),
                this.messageType.getName(),
                this.channelType.identifierToString(this.identifier),
                this.messageType.valueToString(this.value));
    }

    public ChannelType<Identifier> getChannelType() {
        return channelType;
    }

    public MessageType<Value> getMessageType() {
        return messageType;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Value getValue() {
        return value;
    }
}
