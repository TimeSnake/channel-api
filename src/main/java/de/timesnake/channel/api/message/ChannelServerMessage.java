package de.timesnake.channel.api.message;

public interface ChannelServerMessage extends ChannelMessage {

    static ChannelServerMessage getStatusMessage(Integer port, Status status) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.STATUS, status.getDatabaseValue());
    }

    static ChannelServerMessage getOnlinePlayersMessage(Integer port, Integer players) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.ONLINE_PLAYERS, String.valueOf(players));
    }

    static ChannelServerMessage getCommandMessage(Integer port, String command) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.COMMAND, command);
    }

    static ChannelServerMessage getPermissionMessage(Integer port) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.COMMAND);
    }

    static ChannelServerMessage getMaxPlayersMessage(Integer port, Integer players) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.MAX_PLAYERS, String.valueOf(players));
    }

    static ChannelServerMessage getMapMessage(Integer port, String name) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.MAP, name);
    }

    static ChannelServerMessage getPasswordMessage(Integer port, String password) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.PASSWORD, password);
    }

    static ChannelServerMessage getPasswordMessage(Integer port, boolean oldPvP) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.PVP, String.valueOf(oldPvP));
    }

    static ChannelServerMessage getPvPMessage(Integer port, boolean oldPvP) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.PVP, String.valueOf(oldPvP));
    }

    static ChannelServerMessage getStateMessage(Integer port, State state) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.STATE, String.valueOf(state));
    }

    static ChannelServerMessage getRestartMessage(Integer port, int delaySec) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.RESTART, String.valueOf(delaySec));
    }

    static ChannelServerMessage getCustomMessage(Integer port, String value) {
        return new de.timesnake.channel.message.ChannelServerMessage(port, MessageType.CUSTOM, value);
    }

    Integer getPort();

    MessageType getType();

    String getValue();

    enum MessageType implements de.timesnake.channel.MessageType {
        STATUS, ONLINE_PLAYERS, MAX_PLAYERS, COMMAND, PERMISSION, MAP, PASSWORD, PVP, STATE, CUSTOM, RESTART
    }

    enum State {
        READY
    }
}
