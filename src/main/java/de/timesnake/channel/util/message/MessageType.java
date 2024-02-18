/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelParticipant;
import de.timesnake.channel.core.MessageListenerData;
import de.timesnake.library.basic.util.Punishment;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;

import java.io.Serializable;
import java.util.*;

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
    public static final MessageType<Integer> GAME_PLAYERS = new MessageType<>("game_players");
    public static final MessageType<String> PASSWORD = new MessageType<>("password");
    public static final MessageType<Boolean> OLD_PVP = new MessageType<>("old_pvp");
    public static final MessageType<ChannelServerMessage.State> STATE = new MessageType<>("state");
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
    public static final MessageType<VoidMessage> PING = new MessageType<>("ping");
    public static final MessageType<VoidMessage> PONG = new MessageType<>("pong");
    public static final MessageType<String> CUSTOM = new MessageType<>("custom");

    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, ONLINE_PLAYERS, MAX_PLAYERS, COMMAND, PERMISSION,
        GAME_MAP, GAME_WORLD, GAME_PLAYERS, PASSWORD, OLD_PVP, STATE, CUSTOM, RESTART, DESTROY, KILL_DESTROY, DISCORD,
        USER_STATS, LOAD_WORLD, UNLOAD_WORLD, LOADED_WORLD, UNLOADED_WORLD, UNLOADED_ALL_WORLDS, PING, PONG);

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
    public static final MessageType<Punishment> PUNISH = new MessageType<>("punish");
    public static final MessageType<VoidMessage> ALIAS = new MessageType<>("alias");
    public static final MessageType<String> TASK = new MessageType<>("task");
    public static final MessageType<String> COMMAND = new MessageType<>("command");
    public static final MessageType<String> PROXY_COMMAND = new MessageType<>("proxy_command");
    public static final MessageType<String> PERM_GROUP = new MessageType<>("perm_group");
    public static final MessageType<VoidMessage> DISPLAY_GROUP = new MessageType<>("display_group");
    public static final MessageType<String> TEAM = new MessageType<>("team");
    public static final MessageType<String> STATISTICS = new MessageType<>("statistics");
    public static final MessageType<String> CUSTOM = new MessageType<>("custom");
    public static final MessageType<ChannelUserMessage.Sound> SOUND = new MessageType<>("sound");
    public static final MessageType<Tuple<String, String>> STORY_START = new MessageType<>("story_start");
    public static final MessageType<VoidMessage> STORY_END = new MessageType<>("story_end");
    public static final MessageType<String> STORY_AUDIO_PLAY = new MessageType<>("story_audio_play");
    public static final MessageType<String> STORY_AUDIO_END = new MessageType<>("story_audio_end");
    public static final MessageType<String> STORY_AUDIO_FAIL = new MessageType<>("story_audio_fail");
    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, SERVICE, SWITCH_PORT, SWITCH_NAME, PERMISSION,
        PUNISH, ALIAS, TASK, COMMAND, PROXY_COMMAND, PERM_GROUP, DISPLAY_GROUP, TEAM, STATISTICS, CUSTOM, SOUND,
        STORY_START, STORY_END, STORY_AUDIO_PLAY, STORY_AUDIO_END, STORY_AUDIO_FAIL);

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

  public abstract static class Control<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<FilterMessage<MessageListenerData<?>>> INIT = new MessageType<>("init");
    public static final MessageType<FilterMessage<MessageListenerData<?>>> INIT_ACK = new MessageType<>("init_ack");
    public static final MessageType<VoidMessage> HOSTS_REQUEST = new MessageType<>("hosts_request");
    public static final MessageType<ArrayList<ChannelParticipant>> HOSTS_LIST = new MessageType<>("hosts_list");
    public static final MessageType<VoidMessage> INIT_FIN = new MessageType<>("init_fin");
    public static final MessageType<VoidMessage> RECONNECT = new MessageType<>("reconnect");
    public static final MessageType<VoidMessage> CLOSE = new MessageType<>("close");

    public static final MessageType<MessageListenerData<?>> LISTENER_ADD = new MessageType<>("listener_add");
    public static final MessageType<MessageListenerData<?>> LISTENER_REMOVE = new MessageType<>("listener_remove");

    public static final Set<MessageType<?>> TYPES = Set.of(
        INIT, INIT_ACK, HOSTS_REQUEST, HOSTS_LIST, INIT_FIN, RECONNECT, CLOSE,
        LISTENER_ADD, LISTENER_REMOVE);

    public Control(String name) {
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

  public abstract static class Discord<Value extends Serializable> extends MessageType<Value> {

    public static final MessageType<LinkedList<String>> DESTROY_CHANNELS = new MessageType<>("destroy_teams") {
    };

    public static final MessageType<VoidMessage> DELETE_UNUSED = new MessageType<>("delete_unused");
    public static final MessageType<Boolean> HIDE_CHANNELS = new MessageType<>("hide_channels");
    public static final MessageType<String> MUTE_CHANNEL = new MessageType<>("mute_channel");
    public static final MessageType<UUID> DISCONNECT_MEMBER = new MessageType<>("disconnect_member");

    public static final MessageType<ChannelDiscordMessage.Allocation> MOVE_MEMBERS = new MessageType<>("move_members") {

    };

    public static final Set<MessageType<?>> TYPES = Set.of(MOVE_MEMBERS, DELETE_UNUSED, HIDE_CHANNELS,
        DESTROY_CHANNELS, MUTE_CHANNEL);

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
}
