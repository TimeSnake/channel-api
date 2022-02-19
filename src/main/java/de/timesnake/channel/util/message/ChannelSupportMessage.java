package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelSupportMessage<Value> extends ChannelMessage<Integer, Value> {

    public ChannelSupportMessage(String... args) {
        super(args);
    }

    public ChannelSupportMessage(Integer port, MessageType<Value> type, Value value) {
        super(ChannelType.SUPPORT, port, type, value);
        if (!MessageType.Support.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public Integer getPort() {
        return super.getIdentifier();
    }

}
