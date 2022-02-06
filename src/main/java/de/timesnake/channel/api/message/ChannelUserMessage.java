package de.timesnake.channel.api.message;

import java.util.UUID;

public interface ChannelUserMessage extends ChannelMessage {

    static ChannelUserMessage getStatusMessage(UUID uuid, Status status) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.STATUS, status.getDatabaseValue());
    }

    static ChannelUserMessage getServiceMessage(UUID uuid, boolean service) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.SERVICE, String.valueOf(service));
    }

    static ChannelUserMessage getSwitchMessage(UUID uuid, Integer port) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.SWITCH, String.valueOf(port));
    }

    static ChannelUserMessage getSwitchMessage(UUID uuid, String name) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.SWITCH, name);
    }

    static ChannelUserMessage getPermissionMessage(UUID uuid) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.PERMISSION);
    }

    static ChannelUserMessage getPunishMessage(UUID uuid) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.PUNISH);
    }

    static ChannelUserMessage getAliasMessage(UUID uuid) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.ALIAS);
    }

    static ChannelUserMessage getTaskMessage(UUID uuid, String task) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.TASK, task);
    }

    static ChannelUserMessage getCommandMessage(UUID uuid, String command) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.COMMAND, command);
    }

    static ChannelUserMessage getGroupMessage(UUID uuid, String groupName) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.GROUP, groupName);
    }

    static ChannelUserMessage getSoundMessage(UUID uuid, Sound sound) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.SOUND, sound.name());
    }

    static ChannelUserMessage getTeamMessage(UUID uuid, String team) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.TEAM, team);
    }

    static ChannelUserMessage getStatisticsMessage(UUID uuid, String statType) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.STATISTICS, statType);
    }

    static ChannelUserMessage getCustomMessage(UUID uuid, String value) {
        return new de.timesnake.channel.message.ChannelUserMessage(uuid, MessageType.CUSTOM, value);
    }

    UUID getUniqueId();

    MessageType getType();

    String getValue();

    enum MessageType implements de.timesnake.channel.MessageType {
        STATUS, SERVICE, SWITCH, PERMISSION, PUNISH, ALIAS, TASK, COMMAND, GROUP, SOUND, TEAM, STATISTICS, CUSTOM
    }

    enum Sound {
        PLING, PLONG;
    }
}
