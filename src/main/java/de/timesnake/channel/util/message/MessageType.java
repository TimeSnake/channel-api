/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Triple;
import de.timesnake.library.basic.util.Tuple;

import java.util.*;

public abstract class MessageType<Value> {

  private final String name;

  public MessageType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract String valueToString(Value value);

  public abstract Value parseValue(String value);

  public abstract static class Server<Value> extends MessageType<Value> {

    public static final MessageType<Status.Server> STATUS = new MessageTypeStatus<>("status");
    public static final MessageType<Integer> ONLINE_PLAYERS = new MessageTypeInteger(
        "online_players");
    public static final MessageType<Integer> MAX_PLAYERS = new MessageTypeInteger(
        "max_players");
    public static final MessageType<String> COMMAND = new MessageTypeString("command");
    public static final MessageType<Void> PERMISSION = new MessageTypeVoid("permission");
    public static final MessageType<String> GAME_MAP = new MessageTypeString("game_map");
    public static final MessageType<String> GAME_WORLD = new MessageTypeString("game_world");
    public static final MessageType<String> PASSWORD = new MessageTypeString("password");
    public static final MessageType<Boolean> OLD_PVP = new MessageTypeBoolean("old_pvp");
    public static final MessageType<ChannelServerMessage.State> STATE = new MessageType<>(
        "state") {
      @Override
      public String valueToString(ChannelServerMessage.State state) {
        return state.name();
      }

      @Override
      public ChannelServerMessage.State parseValue(String value) {
        return ChannelServerMessage.State.valueOf(value);
      }
    };
    public static final MessageType<String> CUSTOM = new MessageTypeString("custom");
    public static final MessageType<Integer> RESTART = new MessageTypeInteger("restart");
    public static final MessageType<Integer> DESTROY = new MessageTypeInteger("destroy");
    public static final MessageType<Long> KILL_DESTROY = new MessageTypeLong("kill_destroy");
    public static final MessageType<Boolean> DISCORD = new MessageTypeBoolean("discord");
    public static final MessageType<String> USER_STATS = new MessageTypeString("user_stats");
    public static final MessageType<String> LOAD_WORLD = new MessageTypeString("load_world");
    public static final MessageType<String> UNLOAD_WORLD = new MessageTypeString(
        "unload_world");
    public static final MessageType<String> LOADED_WORLD = new MessageTypeString(
        "loaded_world");
    public static final MessageType<String> UNLOADED_WORLD = new MessageTypeString(
        "unloaded_world");
    public static final MessageType<Void> UNLOADED_ALL_WORLDS = new MessageTypeVoid(
        "unloaded_all_worlds");

    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, ONLINE_PLAYERS, MAX_PLAYERS,
        COMMAND, PERMISSION, GAME_MAP, GAME_WORLD, PASSWORD, OLD_PVP, STATE, CUSTOM,
        RESTART, DESTROY,
        KILL_DESTROY, DISCORD, USER_STATS, LOAD_WORLD, UNLOAD_WORLD, LOADED_WORLD,
        UNLOADED_WORLD, UNLOADED_ALL_WORLDS);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Server(String name) {
      super(name);
    }
  }

  public abstract static class User<Value> extends MessageType<Value> {

    public static final MessageType<Status.User> STATUS = new MessageTypeStatus<>("status");
    public static final MessageType<Boolean> SERVICE = new MessageTypeBoolean("service");
    public static final MessageType<Integer> SWITCH_PORT = new MessageTypeInteger(
        "switch_port");
    public static final MessageType<String> SWITCH_NAME = new MessageTypeString("switch_name");
    public static final MessageType<Void> PERMISSION = new MessageTypeVoid("permission");
    public static final MessageType<Void> PUNISH = new MessageTypeVoid("punish");
    public static final MessageType<Void> ALIAS = new MessageTypeVoid("alias");
    public static final MessageType<String> TASK = new MessageTypeString("task");
    public static final MessageType<String> COMMAND = new MessageTypeString("command");
    public static final MessageType<String> PROXY_COMMAND = new MessageTypeString(
        "proxy_command");
    public static final MessageType<String> PERM_GROUP = new MessageTypeString("perm_group");
    public static final MessageType<Void> DISPLAY_GROUP = new MessageTypeVoid("display_group");
    public static final MessageType<String> TEAM = new MessageTypeString("team");
    public static final MessageType<String> STATISTICS = new MessageTypeString("statistics");
    public static final MessageType<String> CUSTOM = new MessageTypeString("custom");
    public static final MessageType<ChannelUserMessage.Sound> SOUND = new MessageType<>(
        "sound") {
      @Override
      public String valueToString(ChannelUserMessage.Sound s) {
        return s.name();
      }

      @Override
      public ChannelUserMessage.Sound parseValue(String value) {
        return ChannelUserMessage.Sound.valueOf(value);
      }
    };
    public static final MessageType<Integer> STORY_START = new MessageTypeInteger(
        "story_start");
    public static final MessageType<Integer> STORY_END = new MessageTypeInteger("story_end");
    public static final MessageType<String> STORY_PLAY_AUDIO = new MessageTypeString(
        "story_play_audio");
    public static final MessageType<String> STORY_END_AUDIO = new MessageTypeString(
        "story_end_audio");

    public static final Set<MessageType<?>> TYPES = Set.of(STATUS, SERVICE, SWITCH_PORT,
        SWITCH_NAME, PERMISSION, PUNISH, ALIAS, TASK, COMMAND, PROXY_COMMAND,
        PERM_GROUP, DISPLAY_GROUP, TEAM, STATISTICS, CUSTOM, SOUND, STORY_START, STORY_END,
        STORY_PLAY_AUDIO, STORY_END_AUDIO);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public User(String name) {
      super(name);
    }
  }

  public abstract static class Support<Value> extends MessageType<Value> {

    public static final MessageType<Integer> TICKET_LOCK = new MessageTypeInteger(
        "ticket_lock");
    public static final MessageType<Integer> SUBMIT = new MessageTypeInteger("submit");
    public static final MessageType<Integer> REJECT = new MessageTypeInteger("reject");
    public static final MessageType<Integer> ACCEPT = new MessageTypeInteger("accept");
    public static final MessageType<Integer> CREATION = new MessageTypeInteger("creation");

    public static final Set<MessageType<?>> TYPES = Set.of(TICKET_LOCK, SUBMIT, REJECT, ACCEPT,
        CREATION);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Support(String name) {
      super(name);
    }
  }

  public abstract static class Listener<Value> extends MessageType<Value> {

    public static final MessageType<MessageIdentifierListener<?>> IDENTIFIER_LISTENER = new MessageType<>(
        "identifier_listener") {
      @Override
      public String valueToString(MessageIdentifierListener<?> messageIdentifierListener) {
        return messageIdentifierListener.toString();
      }

      @Override
      public MessageIdentifierListener<?> parseValue(String value) {
        return MessageIdentifierListener.fromString(value);
      }
    };

    public static final MessageType<MessageTypeListener> MESSAGE_TYPE_LISTENER = new MessageType<>(
        "message_type_listener") {
      @Override
      public String valueToString(MessageTypeListener messageTypeListener) {
        return messageTypeListener.toString();
      }

      @Override
      public MessageTypeListener parseValue(String value) {
        return MessageTypeListener.fromString(value);
      }
    };

    public static final MessageType<String> REGISTER_SERVER = new MessageTypeString(
        "register_server");
    public static final MessageType<String> UNREGISTER_SERVER = new MessageTypeString(
        "unregister_server");

    public static final MessageType<Void> REGISTER_HOST = new MessageTypeVoid("register_host");
    public static final MessageType<Void> UNREGISTER_HOST = new MessageTypeVoid("unregister_host");

    public static final MessageType<Void> CLOSE_SOCKET = new MessageTypeVoid("close_socket");

    public static final Set<MessageType<?>> TYPES = Set.of(IDENTIFIER_LISTENER,
        MESSAGE_TYPE_LISTENER, REGISTER_SERVER, UNREGISTER_SERVER, REGISTER_HOST,
        UNREGISTER_HOST, CLOSE_SOCKET);

    public static MessageType<?> valueOf(String name) {
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Listener(String name) {
      super(name);
    }
  }

  public abstract static class Group<Value> extends MessageType<Value> {

    public static final MessageType<Void> ALIAS = new MessageTypeVoid("alias");
    public static final MessageType<Void> PERMISSION = new MessageTypeVoid("permission");

    public static final Set<MessageType<?>> TYPES = Set.of(ALIAS, PERMISSION);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Group(String name) {
      super(name);
    }
  }

  public abstract static class Heartbeat extends MessageType<Void> {

    public static final MessageType<Void> PING = new MessageTypeVoid("ping");
    public static final MessageType<Void> PONG = new MessageTypeVoid("pong");
    public static final MessageType<Void> SERVER_PING = new MessageTypeVoid("server_ping");
    public static final MessageType<String> SERVER_PONG = new MessageTypeString("server_pong");

    public static final Set<MessageType<?>> TYPES = Set.of(PING, PONG, SERVER_PING,
        SERVER_PONG);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Heartbeat(String name) {
      super(name);
    }
  }

  public abstract static class Discord<Value> extends MessageType<Value> {

    public static final MessageType<Collection<String>> DESTROY_CHANNELS = new MessageType<>(
        "destroy_teams") {

      private static final String DELIMITER = "#";

      @Override
      public String valueToString(Collection<String> strings) {
        StringBuilder sb = new StringBuilder();

        if (!strings.isEmpty()) {
          for (String team : strings) {
            sb.append(team);
            sb.append(DELIMITER);
          }

          sb.deleteCharAt(sb.length() - 1); // Remove last DELIMITER
        }

        return sb.toString();
      }

      @Override
      public Collection<String> parseValue(String value) {
        List<String> res = new LinkedList<>();

        if (value != null && !value.equals("")) {
          Collections.addAll(res, value.split(DELIMITER));
        }

        return res;
      }
    };

    public static final MessageType<Void> DELETE_UNUSED = new MessageTypeVoid("delete_unused");
    public static final MessageType<Boolean> HIDE_CHANNELS = new MessageTypeBoolean(
        "hide_channels");
    public static final MessageType<String> MUTE_CHANNEL = new MessageTypeString(
        "mute_channel");
    public static final MessageType<UUID> DISCONNECT_MEMBER = new MessageTypeUUID(
        "disconnect_member");

    public static final MessageType<ChannelDiscordMessage.Allocation> MOVE_MEMBERS = new MessageType<>(
        "move_members") {

      @Override
      public String valueToString(ChannelDiscordMessage.Allocation allocation) {
        return allocation.toString();
      }

      @Override
      public ChannelDiscordMessage.Allocation parseValue(String value) {
        return new ChannelDiscordMessage.Allocation(value);
      }
    };

    public static final Set<MessageType<?>> TYPES = Set.of(MOVE_MEMBERS, DELETE_UNUSED,
        HIDE_CHANNELS, DESTROY_CHANNELS, MUTE_CHANNEL);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Discord(String name) {
      super(name);
    }
  }

  public abstract static class Templates<Value> extends MessageType<Value> {

    public static final MessageType<String> INIT_PLAYER_SERVER = new MessageTypeString(
        "init_player_server");
    public static final MessageType<String> INIT_PUBLIC_PLAYER_SERVER = new MessageTypeString(
        "init_public_player_server");
    public static final MessageType<List<String>> UPDATE_WORLD = new MessageTypeStringList(
        "update_world");

    public static final Set<MessageType<?>> TYPES = Set.of(INIT_PLAYER_SERVER,
        INIT_PUBLIC_PLAYER_SERVER, UPDATE_WORLD);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Templates(String name) {
      super(name);
    }
  }

  public abstract static class Logging<Value> extends MessageType<Value> {

    public static final MessageType<String> INFO = new MessageTypeString("info");
    public static final MessageType<String> WARNING = new MessageTypeString("warning");
    public static final MessageType<String> ERROR = new MessageTypeString("error");

    public static final Set<MessageType<?>> TYPES = Set.of(INFO, WARNING, ERROR);

    public static MessageType<?> valueOf(String name) {
      if (name == null) {
        return null;
      }
      for (MessageType<?> type : TYPES) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

    public Logging(String name) {
      super(name);
    }
  }

  public static class MessageTypeInteger extends MessageType<Integer> {

    public MessageTypeInteger(String name) {
      super(name);
    }

    @Override
    public String valueToString(Integer integer) {
      return String.valueOf(integer);
    }

    @Override
    public Integer parseValue(String value) {
      return Integer.valueOf(value);
    }
  }

  public static class MessageTypeLong extends MessageType<Long> {

    public MessageTypeLong(String name) {
      super(name);
    }

    @Override
    public String valueToString(Long l) {
      return String.valueOf(l);
    }

    @Override
    public Long parseValue(String value) {
      return Long.valueOf(value);
    }
  }

  public static class MessageTypeString extends MessageType<String> {

    public MessageTypeString(String name) {
      super(name);
    }

    @Override
    public String valueToString(String s) {
      return s;
    }

    @Override
    public String parseValue(String value) {
      return value;
    }
  }

  public static class MessageTypeStringList extends MessageType<List<String>> {

    public MessageTypeStringList(String name) {
      super(name);
    }

    @Override
    public String valueToString(List<String> list) {
      if (list.isEmpty()) {
        return "";
      }

      StringBuilder sb = new StringBuilder();

      for (String s : list) {
        sb.append(s).append("#");
      }

      sb.deleteCharAt(sb.length() - 1);

      return sb.toString();
    }

    @Override
    public List<String> parseValue(String value) {
      return Arrays.asList(value.split("#"));
    }
  }

  public static class MessageTypeVoid extends MessageType<Void> {

    public MessageTypeVoid(String name) {
      super(name);
    }

    @Override
    public String valueToString(Void unused) {
      return null;
    }

    @Override
    public Void parseValue(String value) {
      return null;
    }
  }

  public static class MessageTypeBoolean extends MessageType<Boolean> {

    public MessageTypeBoolean(String name) {
      super(name);
    }

    @Override
    public String valueToString(Boolean b) {
      return String.valueOf(b);
    }

    @Override
    public Boolean parseValue(String value) {
      return Boolean.parseBoolean(value);
    }
  }

  public static class MessageTypeStatus<Type extends Status> extends MessageType<Type> {

    public MessageTypeStatus(String name) {
      super(name);
    }

    @Override
    public String valueToString(Type status) {
      return status.getName();
    }

    @Override
    public Type parseValue(String value) {
      return Status.valueOf(value);
    }
  }

  public static class MessageTypeUUID extends MessageType<UUID> {

    public MessageTypeUUID(String name) {
      super(name);
    }

    @Override
    public String valueToString(UUID uuid) {
      return uuid.toString();
    }

    @Override
    public UUID parseValue(String value) {
      return UUID.fromString(value);
    }
  }

  public static class MessageTypeTuple<A, B> extends MessageType<Tuple<A, B>> {

    private final MessageType<A> aMessageType;
    private final MessageType<B> bMessageType;

    public MessageTypeTuple(String name, MessageType<A> aMessageType,
                            MessageType<B> bMessageType) {
      super(name);
      this.aMessageType = aMessageType;
      this.bMessageType = bMessageType;
    }

    @Override
    public String valueToString(Tuple<A, B> tuple) {
      return this.aMessageType.valueToString(tuple.getA()) + ChannelMessage.DIVIDER
          + this.bMessageType.valueToString(tuple.getB());
    }

    @Override
    public Tuple<A, B> parseValue(String tuple) {
      String[] values = tuple.split(ChannelMessage.DIVIDER, 2);
      return new Tuple<>(this.aMessageType.parseValue(values[0]),
          this.bMessageType.parseValue(values[1]));
    }
  }

  public static class MessageTypeTriple<A, B, C> extends MessageType<Triple<A, B, C>> {

    private final MessageType<A> aMessageType;
    private final MessageType<B> bMessageType;
    private final MessageType<C> cMessageType;

    public MessageTypeTriple(String name, MessageType<A> aMessageType,
                             MessageType<B> bMessageType, MessageType<C> cMessageType) {
      super(name);
      this.aMessageType = aMessageType;
      this.bMessageType = bMessageType;
      this.cMessageType = cMessageType;
    }

    @Override
    public String valueToString(Triple<A, B, C> tuple) {
      return this.aMessageType.valueToString(tuple.getA()) + ChannelMessage.DIVIDER
          + this.bMessageType.valueToString(tuple.getB()) + ChannelMessage.DIVIDER
          + this.cMessageType.valueToString(tuple.getC());
    }

    @Override
    public Triple<A, B, C> parseValue(String tuple) {
      String[] values = tuple.split(ChannelMessage.DIVIDER, 3);
      return new Triple<>(this.aMessageType.parseValue(values[0]),
          this.bMessageType.parseValue(values[1]),
          this.cMessageType.parseValue(values[2]));
    }
  }

  public static class MessageIdentifierListener<Identifier> {

    public static <Identifier> MessageIdentifierListener<Identifier> fromString(String value) {
      String[] s = value.split(ChannelMessage.DIVIDER);
      ChannelType<Identifier> channelType = (ChannelType<Identifier>) ChannelType.valueOf(
          s[0]);
      return new MessageIdentifierListener<>(channelType, channelType.parseIdentifier(s[1]));
    }

    private final ChannelType<Identifier> channelType;
    private final Identifier identifier;

    public MessageIdentifierListener(ChannelType<Identifier> channelType,
                                     Identifier identifier) {
      this.channelType = channelType;
      this.identifier = identifier;
    }

    public ChannelType<Identifier> getChannelType() {
      return channelType;
    }

    public Identifier getIdentifier() {
      return identifier;
    }

    @Override
    public String toString() {
      return this.channelType.getName() + ChannelMessage.DIVIDER + this.identifier.toString();
    }
  }

  public static class MessageTypeListener {

    public static MessageTypeListener fromString(String value) {
      String[] s = value.split(ChannelMessage.DIVIDER);
      ChannelType<?> channelType = ChannelType.valueOf(s[0]);
      return new MessageTypeListener(channelType, channelType.parseMessageType(s[1]));
    }

    private final ChannelType<?> channelType;
    private final MessageType<?> messageType;

    public MessageTypeListener(ChannelType<?> channelType, MessageType<?> messageType) {
      this.channelType = channelType;
      this.messageType = messageType;
    }

    public ChannelType<?> getChannelType() {
      return channelType;
    }

    public MessageType<?> getMessageType() {
      return messageType;
    }

    @Override
    public String toString() {
      return this.channelType.getName() + ChannelMessage.DIVIDER + (this.messageType != null
          ? this.messageType.getName() : "null");
    }
  }

}
