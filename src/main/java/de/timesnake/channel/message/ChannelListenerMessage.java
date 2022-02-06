package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

public class ChannelListenerMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelListenerMessage {


    private final Integer port;
    private final MessageType type;
    private String value;

    public ChannelListenerMessage(String[] args) {
        super(args);
        this.port = Integer.valueOf(args[1]);
        this.type = MessageType.valueOf(args[2]);
        if (args.length >= 4) {
            if (args[3] == null || args[3].equals("null")) {
                this.value = null;
            } else {
                this.value = args[3];
            }
        }
    }

    public ChannelListenerMessage(Integer port, MessageType type, String value) {
        super(ChannelType.LISTENER, port.toString(), type.name(), value);
        this.port = port;
        this.type = type;
        this.value = value;
    }

    public ChannelListenerMessage(Integer port, MessageType type) {
        this(port, type, null);
    }

    /**
     * Get the listener sender port
     *
     * @return The sender port
     */
    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

}
