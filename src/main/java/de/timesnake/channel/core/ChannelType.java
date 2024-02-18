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

  public static final ChannelType<UUID> USER = new ChannelType<>("user", UUID.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return User.TYPES;
    }
  };
  public static final ChannelType<String> SERVER = new ChannelType<>("server", String.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Server.TYPES;
    }
  };
  public static final ChannelType<ChannelParticipant> CONTROL = new ChannelType<>("control", ChannelParticipant.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Control.TYPES;
    }
  };
  public static final ChannelType<String> GROUP = new ChannelType<>("group", String.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Group.TYPES;
    }
  };
  public static final ChannelType<String> SUPPORT = new ChannelType<>("support", String.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Support.TYPES;
    }
  };

  public static final ChannelType<String> DISCORD = new ChannelType<>("discord", String.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Discord.TYPES;
    }
  };
  public static final ChannelType<String> TEMPLATES = new ChannelType<>("templates", String.class) {

    @Override
    public Collection<MessageType<?>> getMessageTypes() {
      return Templates.TYPES;
    }
  };

  public static final List<ChannelType<?>> TYPES = List.of(USER, SERVER, CONTROL, GROUP, SUPPORT, DISCORD, TEMPLATES);

  private final String name;
  private final Class<? extends Serializable> identifierClass;

  public ChannelType(String name, Class<? extends Serializable> identifierClass) {
    this.name = name;
    this.identifierClass = identifierClass;
  }

  public String getName() {
    return name;
  }

  public abstract Collection<MessageType<?>> getMessageTypes();

  public Class<? extends Serializable> getIdentifierClass() {
    return identifierClass;
  }

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

