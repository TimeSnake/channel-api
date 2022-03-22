package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelPingMessage extends ChannelMessage<Integer, Void> {

    public ChannelPingMessage(String... args) {
        super(args);
    }

    public ChannelPingMessage(Integer senderPort, MessageType<Void> type) {
        super(ChannelType.PING, senderPort, type);
        if (!MessageType.Ping.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public Integer getSenderPort() {
        return this.getIdentifier();
    }

    @Override
    public MessageType<Void> getMessageType() {
        return null;
    }
}
