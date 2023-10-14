/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class ChannelType<Identifier extends Serializable> implements Serializable {

  public static final ChannelType<UUID> USER = new ChannelType<>("user") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return User.TYPES;
    }
  };
  public static final ChannelType<String> SERVER = new ChannelType<>("server") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Server.TYPES;
    }
  };
  public static final ChannelType<Host> LISTENER = new ChannelType<>("listener") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Listener.TYPES;
    }
  };
  public static final ChannelType<String> GROUP = new ChannelType<>("group") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Group.TYPES;
    }
  };
  public static final ChannelType<Host> HEARTBEAT = new ChannelType<>("heartbeat") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Heartbeat.TYPES;
    }
  };
  public static final ChannelType<String> SUPPORT = new ChannelType<>("support") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Support.TYPES;
    }
  };

  public static final ChannelType<String> DISCORD = new ChannelType<>("discord") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Discord.TYPES;
    }
  };
  public static final ChannelType<String> TEMPLATES = new ChannelType<>("templates") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Templates.TYPES;
    }
  };
  public static final ChannelType<String> LOGGING = new ChannelType<>("logging") {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Logging.TYPES;
    }
  };

  public static final List<ChannelType<?>> TYPES = List.of(USER, SERVER, LISTENER, GROUP,
      HEARTBEAT, SUPPORT, DISCORD, TEMPLATES, LOGGING);

  private final String name;

  public ChannelType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract Collection<MessageType<?>> getMessageTypes();

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChannelType<?> that = (ChannelType<?>) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}

