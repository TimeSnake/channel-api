package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelServerMessage<Value> extends ChannelMessage<Integer, Value> {

    public ChannelServerMessage(String... args) {
        super(args);
    }

    public ChannelServerMessage(Integer port, MessageType<Value> type, Value value) {
        super(ChannelType.SERVER, port, type, value);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelServerMessage(Integer port, MessageType<Value> type) {
        super(ChannelType.SERVER, port, type);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public Integer getPort() {
        return super.getIdentifier();
    }

    public enum State {
        READY
    }
}
