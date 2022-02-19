package de.timesnake.channel.core;

import de.timesnake.channel.util.message.MessageType;

import java.util.List;
import java.util.UUID;

public abstract class ChannelType<Identifier> {

    public static final ChannelType<UUID> USER = new ChannelType<>("user") {
        @Override
        public String identifierToString(UUID uuid) {
            return uuid.toString();
        }

        @Override
        public UUID parseIdentifier(String identifier) {
            return UUID.fromString(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.User.valueOf(messageType);
        }
    };
    public static final ChannelType<Integer> SERVER = new ChannelType<>("server") {
        @Override
        public String identifierToString(Integer integer) {
            return String.valueOf(integer);
        }

        @Override
        public Integer parseIdentifier(String identifier) {
            return Integer.parseInt(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Server.valueOf(messageType);
        }
    };
    public static final ChannelType<Integer> LISTENER = new ChannelType<>("listener") {
        @Override
        public String identifierToString(Integer integer) {
            return String.valueOf(integer);
        }

        @Override
        public Integer parseIdentifier(String identifier) {
            return Integer.parseInt(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Listener.valueOf(messageType);
        }
    };
    public static final ChannelType<String> GROUP = new ChannelType<>("group") {
        @Override
        public String identifierToString(String s) {
            return s;
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Group.valueOf(messageType);
        }
    };
    public static final ChannelType<Integer> PING = new ChannelType<>("ping") {
        @Override
        public String identifierToString(Integer integer) {
            return String.valueOf(integer);
        }

        @Override
        public Integer parseIdentifier(String identifier) {
            return Integer.parseInt(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Ping.valueOf(messageType);
        }
    };
    public static final ChannelType<Integer> SUPPORT = new ChannelType<>("support") {
        @Override
        public String identifierToString(Integer s) {
            return String.valueOf(s);
        }

        @Override
        public Integer parseIdentifier(String identifier) {
            return Integer.valueOf(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Support.valueOf(messageType);
        }
    };

    public static final List<ChannelType<?>> TYPES = List.of(USER, SERVER, LISTENER, GROUP, PING, SUPPORT);

    public static ChannelType<?> valueOf(String name) {
        for (ChannelType<?> type : TYPES) {
            if (type.getName().equals(name)) {
                return type;
            }
        }

        return null;
    }

    private final String name;

    public ChannelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String identifierToString(Identifier identifier);

    public abstract Identifier parseIdentifier(String identifier);

    public abstract MessageType<?> parseMessageType(String messageType);
}

