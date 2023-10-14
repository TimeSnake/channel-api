/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MessageType<Value extends Serializable> implements Serializable {

  private final String name;

  public MessageType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageType<?> that = (MessageType<?>) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public abstract static class Server<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<Status.Server> STATUS = new MessageType<>("status");
    public static final MessageType<Integer> ONLINE_PLAYERS = new MessageType<>("online_players");
    public static final MessageType<Integer> MAX_PLAYERS = new MessageType<>("max_players");
    public static final MessageType<String> COMMAND = new MessageType<>("command");
    public static final MessageType<VoidMessage> PERMISSION = new MessageType<>("permission");
    public static final MessageType<String> GAME_MAP = new MessageType<>("game_map");
    public static final MessageType<String> GAME_WORLD = new MessageType<>("game_world");
    public static final MessageType<String> PASSWORD = new MessageType<>("password");
    public static final MessageType<Boolean> OLD_PVP = new MessageType<>("old_pvp");
    public static final MessageType<ChannelServerMessage.State> STATE = new MessageType<>("state") {

    };
    public static final MessageType<String> CUSTOM = new MessageType<>("custom");
    public static final MessageType<Integer> RESTART = new MessageType<>("restart");
    public static final MessageType<Integer> DESTROY = new MessageType<>("destroy");
    public static final MessageType<Long> KILL_DESTROY = new MessageType<>("kill_destroy");
    public static final MessageType<Boolean> DISCORD = new MessageType<>("discord");
    public static final MessageType<String> USER_STATS = new MessageType<>("user_stats");
    public static final MessageType<String> LOAD_WORLD = new MessageType<>("load_world");
    public static final MessageType<String> UNLOAD_WORLD = new MessageType<>("unload_world");
    public static final MessageType<String> LOADED_WORLD = new MessageType<>("loaded_world");
    public static final MessageType<String> UNLOADED_WORLD = new MessageType<>("unloaded_world");
    public static final MessageType<VoidMessage> UNLOADED_ALL_WORLDS = new MessageType<>("unloaded_all_worlds");

    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, ONLINE_PLAYERS, MAX_PLAYERS, COMMAND, PERMISSION, GAME_MAP, GAME_WORLD, PASSWORD, OLD_PVP, STATE, CUSTOM, RESTART, DESTROY, KILL_DESTROY, DISCORD, USER_STATS, LOAD_WORLD, UNLOAD_WORLD, LOADED_WORLD, UNLOADED_WORLD, UNLOADED_ALL_WORLDS);

    public Server(String name) {
      super(name);
    }
  }

  public abstract static class User<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<Status.User> STATUS = new MessageType<>("status");
    public static final MessageType<Boolean> SERVICE = new MessageType<>("service");
    public static final MessageType<Integer> SWITCH_PORT = new MessageType<>("switch_port");
    public static final MessageType<String> SWITCH_NAME = new MessageType<>("switch_name");
    public static final MessageType<VoidMessage> PERMISSION = new MessageType<>("permission");
    public static final MessageType<VoidMessage> PUNISH = new MessageType<>("punish");
    public static final MessageType<VoidMessage> ALIAS = new MessageType<>("alias");
    public static final MessageType<String> TASK = new MessageType<>("task");
    public static final MessageType<String> COMMAND = new MessageType<>("command");
    public static final MessageType<String> PROXY_COMMAND = new MessageType<>("proxy_command");
    public static final MessageType<String> PERM_GROUP = new MessageType<>("perm_group");
    public static final MessageType<VoidMessage> DISPLAY_GROUP = new MessageType<>("display_group");
    public static final MessageType<String> TEAM = new MessageType<>("team");
    public static final MessageType<String> STATISTICS = new MessageType<>("statistics");
    public static final MessageType<String> CUSTOM = new MessageType<>("custom");
    public static final MessageType<ChannelUserMessage.Sound> SOUND = new MessageType<>("sound") {

    };
    public static final MessageType<Tuple<String, String>> STORY_START = new MessageType<>("story_start");
    public static final MessageType<VoidMessage> STORY_END = new MessageType<>("story_end");
    public static final MessageType<String> STORY_PLAY_AUDIO = new MessageType<>("story_play_audio");
    public static final MessageType<String> STORY_END_AUDIO = new MessageType<>("story_end_audio");
    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, SERVICE, SWITCH_PORT, SWITCH_NAME, PERMISSION, PUNISH, ALIAS, TASK, COMMAND, PROXY_COMMAND, PERM_GROUP, DISPLAY_GROUP, TEAM, STATISTICS, CUSTOM, SOUND, STORY_START, STORY_END, STORY_PLAY_AUDIO, STORY_END_AUDIO);

    public User(String name) {
      super(name);
    }
  }

  public abstract static class Support<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<Integer> TICKET_LOCK = new MessageType<>("ticket_lock");
    public static final MessageType<Integer> SUBMIT = new MessageType<>("submit");
    public static final MessageType<Integer> REJECT = new MessageType<>("reject");
    public static final MessageType<Integer> ACCEPT = new MessageType<>("accept");
    public static final MessageType<Integer> CREATION = new MessageType<>("creation");

    public static final Set<MessageType<?>> TYPES = Set.of(TICKET_LOCK, SUBMIT, REJECT, ACCEPT, CREATION);

    public Support(String name) {
      super(name);
    }
  }

  public abstract static class Listener<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<Tuple<ChannelType<?>, ?>> IDENTIFIER_LISTENER = new MessageType<>("identifier_listener");
    public static final MessageType<Tuple<ChannelType<?>, MessageType<?>>> MESSAGE_TYPE_LISTENER = new MessageType<>("message_type_listener");

    public static final MessageType<String> REGISTER_SERVER = new MessageType<>("register_server");
    public static final MessageType<String> UNREGISTER_SERVER = new MessageType<>("unregister_server");

    public static final MessageType<VoidMessage> REGISTER_HOST = new MessageType<>("register_host");
    public static final MessageType<VoidMessage> UNREGISTER_HOST = new MessageType<>("unregister_host");

    public static final MessageType<VoidMessage> CLOSE_SOCKET = new MessageType<>("close_socket");

    public static final Set<MessageType<?>> TYPES = Set.of(IDENTIFIER_LISTENER, MESSAGE_TYPE_LISTENER, REGISTER_SERVER, UNREGISTER_SERVER, REGISTER_HOST, UNREGISTER_HOST, CLOSE_SOCKET);

    public Listener(String name) {
      super(name);
    }
  }

  public abstract static class Group<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<VoidMessage> ALIAS = new MessageType<>("alias");
    public static final MessageType<VoidMessage> PERMISSION = new MessageType<>("permission");

    public static final Set<MessageType<?>> TYPES = Set.of(ALIAS, PERMISSION);

    public Group(String name) {
      super(name);
    }
  }

  public abstract static class Heartbeat extends MessageType<VoidMessage> {

    public static final MessageType<VoidMessage> PING = new MessageType<>("ping");
    public static final MessageType<VoidMessage> PONG = new MessageType<>("pong");
    public static final MessageType<VoidMessage> SERVER_PING = new MessageType<>("server_ping");
    public static final MessageType<String> SERVER_PONG = new MessageType<>("server_pong");

    public static final Set<MessageType<?>> TYPES = Set.of(PING, PONG, SERVER_PING, SERVER_PONG);

    public Heartbeat(String name) {
      super(name);
    }
  }

  public abstract static class Discord<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<LinkedList<String>> DESTROY_CHANNELS = new MessageType<>("destroy_teams") {
    };

    public static final MessageType<VoidMessage> DELETE_UNUSED = new MessageType<>("delete_unused");
    public static final MessageType<Boolean> HIDE_CHANNELS = new MessageType<>("hide_channels");
    public static final MessageType<String> MUTE_CHANNEL = new MessageType<>("mute_channel");
    public static final MessageType<UUID> DISCONNECT_MEMBER = new MessageType<>("disconnect_member");

    public static final MessageType<ChannelDiscordMessage.Allocation> MOVE_MEMBERS = new MessageType<>("move_members") {

    };

    public static final Set<MessageType<?>> TYPES = Set.of(MOVE_MEMBERS, DELETE_UNUSED, HIDE_CHANNELS, DESTROY_CHANNELS, MUTE_CHANNEL);

    public Discord(String name) {
      super(name);
    }
  }

  public abstract static class Templates<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<String> INIT_PLAYER_SERVER = new MessageType<>("init_player_server");
    public static final MessageType<String> INIT_PUBLIC_PLAYER_SERVER = new MessageType<>("init_public_player_server");
    public static final MessageType<LinkedList<String>> UPDATE_WORLD = new MessageType<>("update_world");

    public static final Set<MessageType<?>> TYPES = Set.of(INIT_PLAYER_SERVER, INIT_PUBLIC_PLAYER_SERVER, UPDATE_WORLD);

    public Templates(String name) {
      super(name);
    }
  }

  public abstract static class Logging<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<String> INFO = new MessageType<>("info");
    public static final MessageType<String> WARNING = new MessageType<>("warning");
    public static final MessageType<String> ERROR = new MessageType<>("error");

    public static final Set<MessageType<?>> TYPES = Set.of(INFO, WARNING, ERROR);

    public Logging(String name) {
      super(name);
    }
  }
}
