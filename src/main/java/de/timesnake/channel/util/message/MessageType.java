package de.timesnake.channel.util.message;

import de.timesnake.library.basic.util.Status;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class MessageType<Value> {

    public abstract static class Server<Value> extends MessageType<Value> {
        public static final MessageType<Status.Server> STATUS = new MessageType.MessageTypeStatus<>("status");
        public static final MessageType<Integer> ONLINE_PLAYERS = new MessageType.MessageTypeInteger("online_players");
        public static final MessageType<Integer> MAX_PLAYERS = new MessageType.MessageTypeInteger("max_players");
        public static final MessageType<String> COMMAND = new MessageType.MessageTypeString("command");
        public static final MessageType<Void> PERMISSION = new MessageType.MessageTypeVoid("permission");
        public static final MessageType<String> MAP = new MessageType.MessageTypeString("map");
        public static final MessageType<String> PASSWORD = new MessageType.MessageTypeString("password");
        public static final MessageType<Boolean> OLD_PVP = new MessageType.MessageTypeBoolean("old_pvp");
        public static final MessageType<ChannelServerMessage.State> STATE = new MessageType<>("state") {
            @Override
            public String valueToString(ChannelServerMessage.State state) {
                return state.name();
            }

            @Override
            public ChannelServerMessage.State parseValue(String value) {
                return ChannelServerMessage.State.valueOf(value);
            }
        };
        public static final MessageType<String> CUSTOM = new MessageType.MessageTypeString("custom");
        public static final MessageType<Integer> RESTART = new MessageType.MessageTypeInteger("restart");
        public static final MessageType<Boolean> DISCORD = new MessageTypeBoolean("discord");

        public static final Set<MessageType<?>> TYPES = Set.of(STATUS, ONLINE_PLAYERS, MAX_PLAYERS, COMMAND, PERMISSION, MAP, PASSWORD, OLD_PVP, STATE, CUSTOM, RESTART, DISCORD);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
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
        public static final MessageType<Status.User> STATUS = new MessageType.MessageTypeStatus<>("status");
        public static final MessageType<Boolean> SERVICE = new MessageType.MessageTypeBoolean("service");
        public static final MessageType<Integer> SWITCH_PORT = new MessageType.MessageTypeInteger("switch_port");
        public static final MessageType<String> SWITCH_NAME = new MessageType.MessageTypeString("switch_name");
        public static final MessageType<Void> PERMISSION = new MessageType.MessageTypeVoid("permission");
        public static final MessageType<Void> PUNISH = new MessageType.MessageTypeVoid("punish");
        public static final MessageType<Void> ALIAS = new MessageType.MessageTypeVoid("alias");
        public static final MessageType<String> TASK = new MessageType.MessageTypeString("task");
        public static final MessageType<String> COMMAND = new MessageType.MessageTypeString("command");
        public static final MessageType<String> GROUP = new MessageType.MessageTypeString("group");
        public static final MessageType<String> TEAM = new MessageType.MessageTypeString("team");
        public static final MessageType<String> STATISTICS = new MessageType.MessageTypeString("statistics");
        public static final MessageType<String> CUSTOM = new MessageType.MessageTypeString("custom");
        public static final MessageType<ChannelUserMessage.Sound> SOUND = new MessageType<>("sound") {
            @Override
            public String valueToString(ChannelUserMessage.Sound s) {
                return s.name();
            }

            @Override
            public ChannelUserMessage.Sound parseValue(String value) {
                return ChannelUserMessage.Sound.valueOf(value);
            }
        };

        public static final Set<MessageType<?>> TYPES = Set.of(STATUS, SERVICE, SWITCH_PORT, SWITCH_NAME, PERMISSION, PUNISH, ALIAS, TASK, COMMAND, GROUP, TEAM, STATISTICS, CUSTOM, SOUND);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
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

        public static final MessageType<Integer> TICKET_LOCK = new MessageTypeInteger("ticket_lock");
        public static final MessageType<Integer> SUBMIT = new MessageTypeInteger("submit");
        public static final MessageType<Integer> REJECT = new MessageTypeInteger("reject");
        public static final MessageType<Integer> ACCEPT = new MessageTypeInteger("accept");
        public static final MessageType<Integer> CREATION = new MessageTypeInteger("creation");

        public static final Set<MessageType<?>> TYPES = Set.of(TICKET_LOCK, SUBMIT, REJECT, ACCEPT, CREATION);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
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


        public static final MessageType<Integer> SERVER_PORT = new MessageTypeInteger("server_port");
        public static final MessageType<MessageType<?>> SERVER_MESSAGE_TYPE = new MessageType<>("server_message_type") {

            @Override
            public String valueToString(MessageType<?> server) {
                return server != null ? server.getName() : null;
            }

            @Override
            public MessageType<?> parseValue(String value) {
                return value != null ? Server.valueOf(value) : null;
            }
        };

        public static final MessageType<Integer> REGISTER_SERVER = new MessageTypeInteger("register_server");
        public static final MessageType<Integer> UNREGISTER_SERVER = new MessageTypeInteger("unregister_server");

        public static final MessageType<Void> REGISTER_HOST = new MessageTypeVoid("register_host");
        public static final MessageType<Void> UNREGISTER_HOST = new MessageTypeVoid("unregister_host");

        public static final Set<MessageType<?>> TYPES = Set.of(SERVER_PORT, SERVER_MESSAGE_TYPE, REGISTER_SERVER,
                UNREGISTER_SERVER, REGISTER_HOST, UNREGISTER_HOST);

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

        public static final MessageType<String> ALIAS = new MessageTypeString("alias");
        public static final MessageType<String> PERMISSION = new MessageTypeString("permission");

        public static final Set<MessageType<?>> TYPES = Set.of(ALIAS, PERMISSION);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
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

    public abstract static class Ping extends MessageType<Void> {

        public static final MessageType<Void> PING = new MessageTypeVoid("ping");
        public static final MessageType<Void> PONG = new MessageTypeVoid("pong");

        public static final Set<MessageType<?>> TYPES = Set.of(PING, PONG);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
            for (MessageType<?> type : TYPES) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public Ping(String name) {
            super(name);
        }
    }

    public abstract static class Discord<Value> extends MessageType<Value> {
        public static final MessageType<List<String>> DESTROY_TEAMS = new MessageType<List<String>>("destroy_teams") {

            private static final String DELIMITER = "#";

            @Override
            public String valueToString(List<String> strings) {
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
            public List<String> parseValue(String value) {
                List<String> res = new LinkedList<>();

                if (value != null && !value.equals("")) {
                    Collections.addAll(res, value.split(DELIMITER));
                }

                return res;
            }
        };
        public static final MessageType<ChannelDiscordMessage.Allocation> MOVE_TEAMS = new MessageType<>("move_teams") {

            @Override
            public String valueToString(ChannelDiscordMessage.Allocation allocation) {
                return allocation.toString();
            }

            @Override
            public ChannelDiscordMessage.Allocation parseValue(String value) {
                return new ChannelDiscordMessage.Allocation(value);
            }
        };

        public static final Set<MessageType<?>> TYPES = Set.of(MOVE_TEAMS, DESTROY_TEAMS);

        public static MessageType<?> valueOf(String name) {
            if (name == null) return null;
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


    private final String name;

    public MessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String valueToString(Value value);

    public abstract Value parseValue(String value);

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
            return Status.parseStatus(value);
        }
    }

}
