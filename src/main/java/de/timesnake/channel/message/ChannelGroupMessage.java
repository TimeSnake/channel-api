package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

public class ChannelGroupMessage extends ChannelMessage implements de.timesnake.channel.api.message.ChannelGroupMessage {

    private final String name;
    private final MessageType type;

    public ChannelGroupMessage(String... args) {
        super(args);
        this.name = args[1];
        this.type = MessageType.valueOf(args[2]);
    }


    public ChannelGroupMessage(String name, MessageType type) {
        super(ChannelType.GROUP, name, type.name());
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

}
