package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

public class ChannelSupportMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelSupportMessage {

    private final Integer port;
    private final ChannelSupportMessage.MessageType type;
    private final String value;

    public ChannelSupportMessage(String... args) {
        super(args);
        this.port = Integer.valueOf(args[1]);
        this.type = ChannelSupportMessage.MessageType.valueOf(args[2]);
        if (args[3] == null || args[3].equals("null")) {
            this.value = null;
        } else {
            this.value = args[3];
        }
    }

    public ChannelSupportMessage(Integer port, ChannelSupportMessage.MessageType type, String value) {
        super(ChannelType.SUPPORT, port.toString(), type.name(), value);
        this.port = port;
        this.type = type;
        this.value = value;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public ChannelSupportMessage.MessageType getType() {
        return this.type;
    }

    @Override
    public String getValue() {
        return value;
    }
}
