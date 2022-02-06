package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

import java.util.UUID;

public class ChannelUserMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelUserMessage {

    private final UUID uuid;
    private final MessageType type;
    private String value;

    public ChannelUserMessage(String[] args) {
        super(args);
        this.uuid = UUID.fromString(args[1]);
        this.type = MessageType.valueOf(args[2]);
        if (args.length >= 4) {
            if (args[3] == null || args[3].equals("null")) {
                this.value = null;
            } else {
                this.value = args[3];
            }
        }
    }

    public ChannelUserMessage(UUID uuid, MessageType type, String value) {
        super(ChannelType.USER, uuid.toString(), type.name(), value);
        this.uuid = uuid;
        this.type = type;
        this.value = value;
    }

    public ChannelUserMessage(UUID uuid, MessageType type) {
        super(ChannelType.USER, uuid.toString(), type.name());
        this.uuid = uuid;
        this.type = MessageType.SWITCH;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

    @Override
    public Object getValue() {
        return this.value;
    }


}
