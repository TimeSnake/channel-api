package de.timesnake.channel.api.message;

public interface ChannelListenerMessage extends ChannelMessage {

    static ChannelListenerMessage getServerMessage(Integer senderPort, Integer port) {
        if (port != null) {
            return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.SERVER, String.valueOf(port));
        } else {
            return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.SERVER);
        }
    }

    static ChannelListenerMessage getServerMessage(Integer senderPort, ChannelServerMessage.MessageType type) {
        if (type != null) {
            return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.SERVER, type.name());
        } else {
            return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.SERVER);
        }
    }

    static ChannelListenerMessage getChannelMessage(Integer senderPort) {
        return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.CHANNEL);
    }

    static ChannelListenerMessage getChannelMessage(Integer senderPort, Integer port) {
        return new de.timesnake.channel.message.ChannelListenerMessage(senderPort, MessageType.CHANNEL, String.valueOf(port));
    }

    Integer getPort();

    MessageType getType();

    String getValue();

    enum MessageType implements de.timesnake.channel.MessageType {
        SERVER, CHANNEL
    }
}
