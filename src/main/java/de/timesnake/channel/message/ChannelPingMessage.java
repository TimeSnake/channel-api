package de.timesnake.channel.message;

import de.timesnake.channel.MessageType;
import de.timesnake.channel.channel.ChannelType;

public class ChannelPingMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelPingMessage {

    private final Integer senderPort;

    public ChannelPingMessage(Integer senderPort) {
        super(ChannelType.PING, String.valueOf(senderPort));
        this.senderPort = senderPort;
    }

    public ChannelPingMessage(String[] args) {
        super(args);
        this.senderPort = Integer.valueOf(args[1]);
    }

    public Integer getSenderPort() {
        return this.senderPort;
    }

    @Override
    public MessageType getType() {
        return null;
    }
}
