package de.timesnake.channel.api.message;

public interface ChannelPingMessage extends ChannelMessage {

    static ChannelPingMessage getPingMessage(Integer senderPort) {
        return new de.timesnake.channel.message.ChannelPingMessage(senderPort);
    }

    Integer getSenderPort();
}
