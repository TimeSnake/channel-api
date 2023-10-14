/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.UUID;

public class ChannelUserMessage<Value extends Serializable> extends ChannelMessage<UUID, Value> {

  public ChannelUserMessage(@NotNull UUID uuid, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.USER, uuid, type, value);
    if (!MessageType.User.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelUserMessage(@NotNull UUID uuid, @NotNull MessageType<Value> type) {
    super(ChannelType.USER, uuid, type);
    if (!MessageType.User.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  @NotNull
  public UUID getUniqueId() {
    return this.getIdentifier();
  }

  public enum Sound {
    PLING,
    PLONG
  }

}
