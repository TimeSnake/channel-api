/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.core.Host;

public class ChannelListenerMessage<Value> extends ChannelMessage<Host, Value> {

    public ChannelListenerMessage(String... args) {
        super(args);
    }

    public ChannelListenerMessage(Host host, MessageType<Value> type, Value value) {
        super(ChannelType.LISTENER, host, type, value);
        if (!MessageType.Listener.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelListenerMessage(Host host, MessageType<Value> type) {
        super(ChannelType.LISTENER, host, type);
        if (!MessageType.Listener.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    /**
     * Get the listener sender port
     *
     * @return The sender port
     */
    public Host getSenderHost() {
        return super.getIdentifier();
    }

}
