package de.timesnake.channel.util.listener;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.util.message.*;
import de.timesnake.library.basic.util.Tuple;

import java.util.UUID;

public enum ListenerType {

    SERVER_STATUS(ChannelType.SERVER, MessageType.Server.STATUS, ChannelServerMessage.class, Integer.class),
    SERVER_ONLINE_PLAYERS(ChannelType.SERVER, MessageType.Server.ONLINE_PLAYERS, ChannelServerMessage.class,
            Integer.class), SERVER_MAX_PLAYERS(ChannelType.SERVER, MessageType.Server.MAX_PLAYERS,
            ChannelServerMessage.class, Integer.class),
    SERVER_COMMAND(ChannelType.SERVER, MessageType.Server.COMMAND
            , ChannelServerMessage.class, Integer.class),
    SERVER_PERMISSION(ChannelType.SERVER,
            MessageType.Server.PERMISSION, ChannelServerMessage.class, Integer.class),
    SERVER_MAP(ChannelType.SERVER,
            MessageType.Server.MAP, ChannelServerMessage.class, Integer.class),
    SERVER_OLD_PVP(ChannelType.SERVER,
            MessageType.Server.OLD_PVP, ChannelServerMessage.class, Integer.class),
    SERVER_PASSWORD(ChannelType.SERVER, MessageType.Server.PASSWORD, ChannelServerMessage.class, Integer.class),
    SERVER_STATE(ChannelType.SERVER, MessageType.Server.STATE, ChannelServerMessage.class, Integer.class),
    SERVER_CUSTOM(ChannelType.SERVER, MessageType.Server.CUSTOM, ChannelServerMessage.class, Integer.class),
    SERVER_RESTART(ChannelType.SERVER, MessageType.Server.RESTART, ChannelServerMessage.class, Integer.class),
    SERVER_DISCORD(ChannelType.SERVER, MessageType.Server.DISCORD, ChannelServerMessage.class, Integer.class),
    SERVER_USER_STATS(ChannelType.SERVER, MessageType.Server.USER_STATS, ChannelServerMessage.class, Integer.class),

    SERVER(ChannelType.SERVER, null, ChannelServerMessage.class, Integer.class),


    USER_STATUS(ChannelType.USER, MessageType.User.STATUS, ChannelUserMessage.class, UUID.class),
    USER_SERVICE(ChannelType.USER, MessageType.User.SERVICE, ChannelUserMessage.class, UUID.class),
    USER_SWITCH_PORT(ChannelType.USER, MessageType.User.SWITCH_PORT, ChannelUserMessage.class, UUID.class),
    USER_SWITCH_NAME(ChannelType.USER, MessageType.User.SWITCH_NAME, ChannelUserMessage.class, UUID.class),
    USER_PERMISSION(ChannelType.USER, MessageType.User.PERMISSION, ChannelUserMessage.class, UUID.class),
    USER_PUNISH(ChannelType.USER, MessageType.User.PUNISH, ChannelUserMessage.class, UUID.class),
    USER_ALIAS(ChannelType.USER, MessageType.User.ALIAS, ChannelUserMessage.class, UUID.class),
    USER_TASK(ChannelType.USER, MessageType.User.TASK, ChannelUserMessage.class, UUID.class),
    USER_COMMAND(ChannelType.USER, MessageType.User.COMMAND, ChannelUserMessage.class, UUID.class),
    USER_GROUP(ChannelType.USER, MessageType.User.GROUP, ChannelUserMessage.class, UUID.class),
    USER_TEAM(ChannelType.USER, MessageType.User.TEAM, ChannelUserMessage.class, UUID.class),
    USER_STATISTICS(ChannelType.USER, MessageType.User.STATISTICS, ChannelUserMessage.class, UUID.class),
    USER_CUSTOM(ChannelType.USER, MessageType.User.CUSTOM, ChannelUserMessage.class, UUID.class),
    USER_SOUND(ChannelType.USER, MessageType.User.SOUND, ChannelUserMessage.class, UUID.class),

    USER(ChannelType.USER, null, ChannelUserMessage.class, UUID.class),


    SUPPORT_TICKET_LOCK(ChannelType.SUPPORT, MessageType.Support.TICKET_LOCK, ChannelSupportMessage.class,
            Integer.class), SUPPORT_SUBMIT(ChannelType.SUPPORT, MessageType.Support.SUBMIT,
            ChannelSupportMessage.class, Integer.class), SUPPORT_REJECT(ChannelType.SUPPORT,
            MessageType.Support.REJECT, ChannelSupportMessage.class, Integer.class),
    SUPPORT_ACCEPT(ChannelType.SUPPORT, MessageType.Support.ACCEPT, ChannelSupportMessage.class, Integer.class),
    SUPPORT_CREATION(ChannelType.SUPPORT, MessageType.Support.CREATION, ChannelSupportMessage.class, Integer.class),

    SUPPORT(ChannelType.SUPPORT, null, ChannelSupportMessage.class, Integer.class),


    LISTENER_SERVER_PORT(ChannelType.LISTENER, MessageType.Listener.SERVER_PORT, ChannelListenerMessage.class,
            Integer.class),
    LISTENER_SERVER_MESSAGE_TYPE(ChannelType.LISTENER, MessageType.Listener.SERVER_MESSAGE_TYPE,
            ChannelListenerMessage.class, MessageType.class),
    LISTENER_REGISTER(ChannelType.LISTENER, MessageType.Listener.REGISTER_SERVER, ChannelListenerMessage.class,
            Integer.class),
    LISTENER_UNREGISTER(ChannelType.LISTENER, MessageType.Listener.UNREGISTER_SERVER, ChannelListenerMessage.class,
            Integer.class),

    LISTENER(ChannelType.LISTENER, null, ChannelListenerMessage.class, Void.class),


    GROUP_ALIAS(ChannelType.GROUP, MessageType.Group.ALIAS, ChannelGroupMessage.class, String.class),
    GROUP_PERMISSION(ChannelType.GROUP, MessageType.Group.PERMISSION, ChannelGroupMessage.class, String.class),

    GROUP(ChannelType.GROUP, null, ChannelGroupMessage.class, String.class),

    PING_PING(ChannelType.PING, MessageType.Ping.PING, ChannelPingMessage.class, Void.class),
    PING_PONG(ChannelType.PING, MessageType.Ping.PONG, ChannelPingMessage.class, Void.class),

    PING(ChannelType.PING, null, ChannelPingMessage.class, Void.class),


    DISCORD_MOVE_TEAMS(ChannelType.DISCORD, MessageType.Discord.MOVE_TEAMS, ChannelDiscordMessage.class,
            String.class), DISCORD_DESTROY_TEAMS(ChannelType.DISCORD, MessageType.Discord.DESTROY_TEAMS,
            ChannelDiscordMessage.class, String.class),


    DISCORD(ChannelType.DISCORD, null, ChannelDiscordMessage.class, String.class),

    ALL(null, null, null, null);


    private final ChannelType<?> channelType;
    private final MessageType<?> messageType;
    private final Tuple<ChannelType<?>, MessageType<?>> typeTuple;

    private final Class<? extends ChannelMessage> messageClass;
    private final Class<?> filterClass;

    ListenerType(ChannelType<?> channelType, MessageType<?> messageType, Class<? extends ChannelMessage> messageClass, Class<?> filterClass) {
        this.channelType = channelType;
        this.messageType = messageType;
        this.typeTuple = new Tuple<>(this.channelType, this.messageType);
        this.messageClass = messageClass;
        this.filterClass = filterClass;
    }

    public ChannelType<?> getChannelType() {
        return channelType;
    }

    public MessageType<?> getMessageType() {
        return messageType;
    }

    public Class<? extends ChannelMessage> getMessageClass() {
        return messageClass;
    }

    public Tuple<ChannelType<?>, MessageType<?>> getTypeTuple() {
        return typeTuple;
    }

    public Class<?> getFilterClass() {
        return filterClass;
    }
}
