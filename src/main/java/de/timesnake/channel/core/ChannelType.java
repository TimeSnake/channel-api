/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.Discord;
import de.timesnake.channel.util.message.MessageType.Group;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.channel.util.message.MessageType.Logging;
import de.timesnake.channel.util.message.MessageType.Server;
import de.timesnake.channel.util.message.MessageType.Support;
import de.timesnake.channel.util.message.MessageType.Templates;
import de.timesnake.channel.util.message.MessageType.User;
import java.util.Collection;
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

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return User.TYPES;
        }
    };
    public static final ChannelType<String> SERVER = new ChannelType<>("server") {
        @Override
        public String identifierToString(String string) {
            return string;
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Server.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Server.TYPES;
        }
    };
    public static final ChannelType<Host> LISTENER = new ChannelType<>("listener") {
        @Override
        public String identifierToString(Host host) {
            return host.toString();
        }

        @Override
        public Host parseIdentifier(String identifier) {
            return Host.parseHost(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Listener.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Listener.TYPES;
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

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Group.TYPES;
        }
    };
    public static final ChannelType<Host> HEARTBEAT = new ChannelType<>("heartbeat") {
        @Override
        public String identifierToString(Host s) {
            return s.toString();
        }

        @Override
        public Host parseIdentifier(String identifier) {
            return Host.parseHost(identifier);
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return Heartbeat.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Heartbeat.TYPES;
        }
    };
    public static final ChannelType<String> SUPPORT = new ChannelType<>("support") {
        @Override
        public String identifierToString(String string) {
            return string;
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Support.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Support.TYPES;
        }
    };

    public static final ChannelType<String> DISCORD = new ChannelType<>("discord") {
        @Override
        public String identifierToString(String integer) {
            return String.valueOf(integer);
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Discord.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Discord.TYPES;
        }
    };
    public static final ChannelType<String> TEMPLATES = new ChannelType<>("templates") {
        @Override
        public String identifierToString(String string) {
            return string;
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Templates.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Templates.TYPES;
        }
    };
    public static final ChannelType<String> LOGGING = new ChannelType<>("logging") {
        @Override
        public String identifierToString(String string) {
            return string;
        }

        @Override
        public String parseIdentifier(String identifier) {
            return identifier;
        }

        @Override
        public MessageType<?> parseMessageType(String messageType) {
            return MessageType.Logging.valueOf(messageType);
        }

        @Override
        public Collection<MessageType<?>> getMessageTypes() {
            return Logging.TYPES;
        }
    };

    public static final List<ChannelType<?>> TYPES = List.of(USER, SERVER, LISTENER, GROUP,
            HEARTBEAT,
            SUPPORT, DISCORD, TEMPLATES, LOGGING);

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

    public abstract Collection<MessageType<?>> getMessageTypes();
}

