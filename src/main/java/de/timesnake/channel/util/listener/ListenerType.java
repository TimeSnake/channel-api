/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.util.message.*;
import de.timesnake.channel.util.message.MessageType.Server;

public enum ListenerType {

  SERVER_STATUS(ChannelType.SERVER, MessageType.Server.STATUS, ChannelServerMessage.class),
  SERVER_ONLINE_PLAYERS(ChannelType.SERVER, MessageType.Server.ONLINE_PLAYERS, ChannelServerMessage.class),
  SERVER_MAX_PLAYERS(ChannelType.SERVER, MessageType.Server.MAX_PLAYERS, ChannelServerMessage.class),
  SERVER_COMMAND(ChannelType.SERVER, MessageType.Server.COMMAND, ChannelServerMessage.class),
  SERVER_PERMISSION(ChannelType.SERVER, MessageType.Server.PERMISSION, ChannelServerMessage.class),
  SERVER_GAME_MAP(ChannelType.SERVER, MessageType.Server.GAME_MAP, ChannelServerMessage.class),
  SERVER_GAME_WORLD(ChannelType.SERVER, Server.GAME_WORLD, ChannelServerMessage.class),
  SERVER_GAME_PLAYERS(ChannelType.SERVER, Server.GAME_PLAYERS, ChannelServerMessage.class),
  SERVER_OLD_PVP(ChannelType.SERVER, MessageType.Server.OLD_PVP, ChannelServerMessage.class),
  SERVER_PASSWORD(ChannelType.SERVER, MessageType.Server.PASSWORD, ChannelServerMessage.class),
  SERVER_STATE(ChannelType.SERVER, MessageType.Server.STATE, ChannelServerMessage.class),
  SERVER_CUSTOM(ChannelType.SERVER, MessageType.Server.CUSTOM, ChannelServerMessage.class),
  SERVER_RESTART(ChannelType.SERVER, MessageType.Server.RESTART, ChannelServerMessage.class),
  SERVER_DESTROY(ChannelType.SERVER, MessageType.Server.DESTROY, ChannelServerMessage.class),
  SERVER_KILL_DESTROY(ChannelType.SERVER, MessageType.Server.KILL_DESTROY, ChannelServerMessage.class),
  SERVER_DISCORD(ChannelType.SERVER, MessageType.Server.DISCORD, ChannelServerMessage.class),
  SERVER_USER_STATS(ChannelType.SERVER, MessageType.Server.USER_STATS, ChannelServerMessage.class),
  SERVER_LOAD_WORLD(ChannelType.SERVER, MessageType.Server.LOAD_WORLD, ChannelServerMessage.class),
  SERVER_UNLOAD_WORLD(ChannelType.SERVER, MessageType.Server.UNLOAD_WORLD, ChannelServerMessage.class),
  SERVER_LOADED_WORLD(ChannelType.SERVER, MessageType.Server.LOADED_WORLD, ChannelServerMessage.class),
  SERVER_UNLOADED_WORLD(ChannelType.SERVER, MessageType.Server.UNLOADED_WORLD, ChannelServerMessage.class),
  SERVER_UNLOADED_ALL_WORLDS(ChannelType.SERVER, MessageType.Server.UNLOADED_ALL_WORLDS, ChannelServerMessage.class),
  SERVER_PING(ChannelType.SERVER, Server.PING, ChannelServerMessage.class),
  SERVER_PONG(ChannelType.SERVER, Server.PONG, ChannelServerMessage.class),

  USER_STATUS(ChannelType.USER, MessageType.User.STATUS, ChannelUserMessage.class),
  USER_SERVICE(ChannelType.USER, MessageType.User.SERVICE, ChannelUserMessage.class),
  USER_SWITCH_PORT(ChannelType.USER, MessageType.User.SWITCH_PORT, ChannelUserMessage.class),
  USER_SWITCH_NAME(ChannelType.USER, MessageType.User.SWITCH_NAME, ChannelUserMessage.class),
  USER_PERMISSION(ChannelType.USER, MessageType.User.PERMISSION, ChannelUserMessage.class),
  USER_PUNISH(ChannelType.USER, MessageType.User.PUNISH, ChannelUserMessage.class),
  USER_ALIAS(ChannelType.USER, MessageType.User.ALIAS, ChannelUserMessage.class),
  USER_TASK(ChannelType.USER, MessageType.User.TASK, ChannelUserMessage.class),
  USER_COMMAND(ChannelType.USER, MessageType.User.COMMAND, ChannelUserMessage.class),
  USER_PROXY_COMMAND(ChannelType.USER, MessageType.User.PROXY_COMMAND, ChannelUserMessage.class),
  USER_PERM_GROUP(ChannelType.USER, MessageType.User.PERM_GROUP, ChannelUserMessage.class),
  USER_DISPLAY_GROUP(ChannelType.USER, MessageType.User.DISPLAY_GROUP, ChannelUserMessage.class),
  USER_TEAM(ChannelType.USER, MessageType.User.TEAM, ChannelUserMessage.class),
  USER_STATISTICS(ChannelType.USER, MessageType.User.STATISTICS, ChannelUserMessage.class),
  USER_CUSTOM(ChannelType.USER, MessageType.User.CUSTOM, ChannelUserMessage.class),
  USER_SOUND(ChannelType.USER, MessageType.User.SOUND, ChannelUserMessage.class),
  USER_STORY_START(ChannelType.USER, MessageType.User.STORY_START, ChannelUserMessage.class),
  USER_STORY_END(ChannelType.USER, MessageType.User.STORY_END, ChannelUserMessage.class),
  USER_STORY_AUDIO_PLAY(ChannelType.USER, MessageType.User.STORY_AUDIO_PLAY, ChannelUserMessage.class),
  USER_STORY_AUDIO_END(ChannelType.USER, MessageType.User.STORY_AUDIO_END, ChannelUserMessage.class),
  USER_STORY_AUDIO_FAIL(ChannelType.USER, MessageType.User.STORY_AUDIO_FAIL, ChannelUserMessage.class),

  GROUP_ALIAS(ChannelType.GROUP, MessageType.Group.ALIAS, ChannelGroupMessage.class),
  GROUP_PERMISSION(ChannelType.GROUP, MessageType.Group.PERMISSION, ChannelGroupMessage.class),

  DISCORD_MOVE_MEMBERS(ChannelType.DISCORD, MessageType.Discord.MOVE_MEMBERS, ChannelDiscordMessage.class),
  DISCORD_DESTROY_TEAMS(ChannelType.DISCORD, MessageType.Discord.DESTROY_CHANNELS, ChannelDiscordMessage.class),
  DISCORD_DELETE_UNUSED(ChannelType.DISCORD, MessageType.Discord.DELETE_UNUSED, ChannelDiscordMessage.class),
  DISCORD_HIDE_CHANNELS(ChannelType.DISCORD, MessageType.Discord.HIDE_CHANNELS, ChannelDiscordMessage.class),
  DISCORD_MUTE_CHANNEL(ChannelType.DISCORD, MessageType.Discord.MUTE_CHANNEL, ChannelDiscordMessage.class),
  DISCORD_DISCONNECT_MEMBER(ChannelType.DISCORD, MessageType.Discord.DISCONNECT_MEMBER, ChannelDiscordMessage.class),

  TEMPLATES_INIT_PLAYER_SERVER(ChannelType.TEMPLATES, MessageType.Templates.INIT_PLAYER_SERVER,
      ChannelTemplatesMessage.class),
  TEMPLATES_INIT_PUBLIC_PLAYER_SERVER(ChannelType.TEMPLATES, MessageType.Templates.INIT_PUBLIC_PLAYER_SERVER,
      ChannelTemplatesMessage.class),
  TEMPLATES_UPDATE_WORLD(ChannelType.TEMPLATES, MessageType.Templates.UPDATE_WORLD, ChannelTemplatesMessage.class);

  private final ChannelType<?> channelType;
  private final MessageType<?> messageType;

  private final Class<? extends ChannelMessage> messageClass;

  ListenerType(ChannelType<?> channelType, MessageType<?> messageType, Class<? extends ChannelMessage> messageClass) {
    this.channelType = channelType;
    this.messageType = messageType;
    this.messageClass = messageClass;
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
    return channelType.getIdentifierClass();
  }
}
