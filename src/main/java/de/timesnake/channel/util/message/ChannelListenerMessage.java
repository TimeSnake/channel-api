package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelListenerMessage<Value> extends ChannelMessage<Integer, Value> {

    public ChannelListenerMessage(String... args) {
        super(args);
    }

    public ChannelListenerMessage(Integer port, MessageType<Value> type, Value value) {
        super(ChannelType.LISTENER, port, type, value);
        if (!MessageType.Listener.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelListenerMessage(Integer port, MessageType<Value> type) {
        super(ChannelType.LISTENER, port, type);
        if (!MessageType.Listener.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    /**
     * Get the listener sender port
     *
     * @return The sender port
     */
    public Integer getSenderPort() {
        return super.getIdentifier();
    }

}
