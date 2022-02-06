package de.timesnake.channel.api.message;

public interface ChannelGroupMessage extends ChannelMessage {
    static ChannelGroupMessage getPermissionMessage(String groupName) {
        return new de.timesnake.channel.message.ChannelGroupMessage(groupName, MessageType.PERMISSION);
    }

    static ChannelGroupMessage getAliasMessage(String groupName) {
        return new de.timesnake.channel.message.ChannelGroupMessage(groupName, MessageType.ALIAS);
    }

    String getName();

    MessageType getType();

    enum MessageType implements de.timesnake.channel.MessageType {
        ALIAS, PERMISSION;
    }
}
