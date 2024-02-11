/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

import de.timesnake.channel.core.ChannelControlMessage;
import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.util.message.*;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import de.timesnake.channel.util.message.MessageType.Server;

import java.util.List;
import java.util.UUID;

public enum ListenerType {

  SERVER_STATUS(ChannelType.SERVER, MessageType.Server.STATUS, ChannelServerMessage.class, Integer.class),
  SERVER_ONLINE_PLAYERS(ChannelType.SERVER, MessageType.Server.ONLINE_PLAYERS, ChannelServerMessage.class, Integer.class),
  SERVER_MAX_PLAYERS(ChannelType.SERVER, MessageType.Server.MAX_PLAYERS, ChannelServerMessage.class, Integer.class),
  SERVER_COMMAND(ChannelType.SERVER, MessageType.Server.COMMAND, ChannelServerMessage.class, Integer.class),
  SERVER_PERMISSION(ChannelType.SERVER, MessageType.Server.PERMISSION, ChannelServerMessage.class, Integer.class),
  SERVER_GAME_MAP(ChannelType.SERVER, MessageType.Server.GAME_MAP, ChannelServerMessage.class, Integer.class),
  SERVER_GAME_WORLD(ChannelType.SERVER, Server.GAME_WORLD, ChannelServerMessage.class, Integer.class),
  SERVER_GAME_PLAYERS(ChannelType.SERVER, Server.GAME_PLAYERS, ChannelServerMessage.class, Integer.class),
  SERVER_OLD_PVP(ChannelType.SERVER, MessageType.Server.OLD_PVP, ChannelServerMessage.class, Integer.class),
  SERVER_PASSWORD(ChannelType.SERVER, MessageType.Server.PASSWORD, ChannelServerMessage.class, Integer.class),
  SERVER_STATE(ChannelType.SERVER, MessageType.Server.STATE, ChannelServerMessage.class, Integer.class),
  SERVER_CUSTOM(ChannelType.SERVER, MessageType.Server.CUSTOM, ChannelServerMessage.class, String.class),
  SERVER_RESTART(ChannelType.SERVER, MessageType.Server.RESTART, ChannelServerMessage.class, Integer.class),
  SERVER_DESTROY(ChannelType.SERVER, MessageType.Server.DESTROY, ChannelServerMessage.class, Integer.class),
  SERVER_KILL_DESTROY(ChannelType.SERVER, MessageType.Server.KILL_DESTROY, ChannelServerMessage.class, Long.class),
  SERVER_DISCORD(ChannelType.SERVER, MessageType.Server.DISCORD, ChannelServerMessage.class, Boolean.class),
  SERVER_USER_STATS(ChannelType.SERVER, MessageType.Server.USER_STATS, ChannelServerMessage.class, String.class),
  SERVER_LOAD_WORLD(ChannelType.SERVER, MessageType.Server.LOAD_WORLD, ChannelServerMessage.class, String.class),
  SERVER_UNLOAD_WORLD(ChannelType.SERVER, MessageType.Server.UNLOAD_WORLD, ChannelServerMessage.class, String.class),
  SERVER_LOADED_WORLD(ChannelType.SERVER, MessageType.Server.LOADED_WORLD, ChannelServerMessage.class, String.class),
  SERVER_UNLOADED_WORLD(ChannelType.SERVER, MessageType.Server.UNLOADED_WORLD, ChannelServerMessage.class, String.class),
  SERVER_UNLOADED_ALL_WORLDS(ChannelType.SERVER, MessageType.Server.UNLOADED_ALL_WORLDS, ChannelServerMessage.class, Void.class),

  USER_STATUS(ChannelType.USER, MessageType.User.STATUS, ChannelUserMessage.class, UUID.class),
  USER_SERVICE(ChannelType.USER, MessageType.User.SERVICE, ChannelUserMessage.class, UUID.class),
  USER_SWITCH_PORT(ChannelType.USER, MessageType.User.SWITCH_PORT, ChannelUserMessage.class, UUID.class),
  USER_SWITCH_NAME(ChannelType.USER, MessageType.User.SWITCH_NAME, ChannelUserMessage.class, UUID.class),
  USER_PERMISSION(ChannelType.USER, MessageType.User.PERMISSION, ChannelUserMessage.class, UUID.class),
  USER_PUNISH(ChannelType.USER, MessageType.User.PUNISH, ChannelUserMessage.class, UUID.class),
  USER_ALIAS(ChannelType.USER, MessageType.User.ALIAS, ChannelUserMessage.class, UUID.class),
  USER_TASK(ChannelType.USER, MessageType.User.TASK, ChannelUserMessage.class, UUID.class),
  USER_COMMAND(ChannelType.USER, MessageType.User.COMMAND, ChannelUserMessage.class, UUID.class),
  USER_PROXY_COMMAND(ChannelType.USER, MessageType.User.PROXY_COMMAND, ChannelUserMessage.class, UUID.class),
  USER_PERM_GROUP(ChannelType.USER, MessageType.User.PERM_GROUP, ChannelUserMessage.class, UUID.class),
  USER_DISPLAY_GROUP(ChannelType.USER, MessageType.User.DISPLAY_GROUP, ChannelUserMessage.class, UUID.class),
  USER_TEAM(ChannelType.USER, MessageType.User.TEAM, ChannelUserMessage.class, UUID.class),
  USER_STATISTICS(ChannelType.USER, MessageType.User.STATISTICS, ChannelUserMessage.class, UUID.class),
  USER_CUSTOM(ChannelType.USER, MessageType.User.CUSTOM, ChannelUserMessage.class, UUID.class),
  USER_SOUND(ChannelType.USER, MessageType.User.SOUND, ChannelUserMessage.class, UUID.class),
  USER_STORY_START(ChannelType.USER, MessageType.User.STORY_START, ChannelUserMessage.class, UUID.class),
  USER_STORY_END(ChannelType.USER, MessageType.User.STORY_END, ChannelUserMessage.class, UUID.class),
  USER_STORY_AUDIO_PLAY(ChannelType.USER, MessageType.User.STORY_AUDIO_PLAY, ChannelUserMessage.class, UUID.class),
  USER_STORY_AUDIO_END(ChannelType.USER, MessageType.User.STORY_AUDIO_END, ChannelUserMessage.class, UUID.class),
  USER_STORY_AUDIO_FAIL(ChannelType.USER, MessageType.User.STORY_AUDIO_FAIL, ChannelUserMessage.class, UUID.class),

  SUPPORT_TICKET_LOCK(ChannelType.SUPPORT, MessageType.Support.TICKET_LOCK, ChannelSupportMessage.class, Integer.class),
  SUPPORT_SUBMIT(ChannelType.SUPPORT, MessageType.Support.SUBMIT, ChannelSupportMessage.class, Integer.class),
  SUPPORT_REJECT(ChannelType.SUPPORT, MessageType.Support.REJECT, ChannelSupportMessage.class, Integer.class),
  SUPPORT_ACCEPT(ChannelType.SUPPORT, MessageType.Support.ACCEPT, ChannelSupportMessage.class, Integer.class),
  SUPPORT_CREATION(ChannelType.SUPPORT, MessageType.Support.CREATION, ChannelSupportMessage.class, Integer.class),

  @Deprecated(forRemoval = true)
  LISTENER_CLOSE(ChannelType.CONTROL, MessageType.Control.CLOSE, ChannelControlMessage.class, Integer.class),

  GROUP_ALIAS(ChannelType.GROUP, MessageType.Group.ALIAS, ChannelGroupMessage.class, String.class),
  GROUP_PERMISSION(ChannelType.GROUP, MessageType.Group.PERMISSION, ChannelGroupMessage.class, String.class),

  PING_PING(ChannelType.HEARTBEAT, Heartbeat.PING, ChannelHeartbeatMessage.class, Void.class),
  PING_PONG(ChannelType.HEARTBEAT, Heartbeat.PONG, ChannelHeartbeatMessage.class, String.class),

  DISCORD_MOVE_MEMBERS(ChannelType.DISCORD, MessageType.Discord.MOVE_MEMBERS, ChannelDiscordMessage.class, String.class),
  DISCORD_DESTROY_TEAMS(ChannelType.DISCORD, MessageType.Discord.DESTROY_CHANNELS, ChannelDiscordMessage.class, String.class),
  DISCORD_DELETE_UNUSED(ChannelType.DISCORD, MessageType.Discord.DELETE_UNUSED, ChannelDiscordMessage.class, Void.class),
  DISCORD_HIDE_CHANNELS(ChannelType.DISCORD, MessageType.Discord.HIDE_CHANNELS, ChannelDiscordMessage.class, Boolean.class),
  DISCORD_MUTE_CHANNEL(ChannelType.DISCORD, MessageType.Discord.MUTE_CHANNEL, ChannelDiscordMessage.class, String.class),
  DISCORD_DISCONNECT_MEMBER(ChannelType.DISCORD, MessageType.Discord.DISCONNECT_MEMBER, ChannelDiscordMessage.class, UUID.class),

  TEMPLATES_INIT_PLAYER_SERVER(ChannelType.TEMPLATES, MessageType.Templates.INIT_PLAYER_SERVER, ChannelTemplatesMessage.class, String.class),
  TEMPLATES_INIT_PUBLIC_PLAYER_SERVER(ChannelType.TEMPLATES, MessageType.Templates.INIT_PUBLIC_PLAYER_SERVER, ChannelTemplatesMessage.class, String.class),
  TEMPLATES_UPDATE_WORLD(ChannelType.TEMPLATES, MessageType.Templates.UPDATE_WORLD, ChannelTemplatesMessage.class,
      List.class);

  private final ChannelType<?> channelType;
  private final MessageType<?> messageType;

  private final Class<? extends ChannelMessage> messageClass;
  private final Class<?> filterClass;

  ListenerType(ChannelType<?> channelType, MessageType<?> messageType,
               Class<? extends ChannelMessage> messageClass
      , Class<?> filterClass) {
    this.channelType = channelType;
    this.messageType = messageType;
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

  public Class<?> getFilterClass() {
    return filterClass;
  }
}
