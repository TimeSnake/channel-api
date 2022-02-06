package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

public class ChannelServerMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelServerMessage {

    private final Integer port;
    private final MessageType type;
    private String value;

    public ChannelServerMessage(String[] args) {
        super(args);
        this.port = Integer.valueOf(args[1]);
        this.type = MessageType.valueOf(args[2]);
        if (args[3] == null || args[3].equals("null")) {
            this.value = null;
        } else {
            this.value = args[3];
        }
    }

    public ChannelServerMessage(Integer port, MessageType type, String value) {
        super(ChannelType.SERVER, port.toString(), type.name(), value);
        this.port = port;
        this.type = type;
        this.value = value;
    }

    public ChannelServerMessage(Integer port, MessageType type) {
        super(ChannelType.SERVER, port.toString(), type.name());
        this.port = port;
        this.type = type;
    }

    @Override
    public Integer getPort() {
        return this.port;
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

    @Override
    public String getValue() {
        return this.value;
    }

}
