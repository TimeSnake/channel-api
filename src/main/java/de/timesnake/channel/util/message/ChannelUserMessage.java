/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import java.util.UUID;

public class ChannelUserMessage<Value> extends ChannelMessage<UUID, Value> {

  public ChannelUserMessage(String... args) {
    super(args);
  }

  public ChannelUserMessage(UUID uuid, MessageType<Value> type, Value value) {
    super(ChannelType.USER, uuid, type, value);
    if (!MessageType.User.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelUserMessage(UUID uuid, MessageType<Value> type) {
    super(ChannelType.USER, uuid, type);
    if (!MessageType.User.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public UUID getUniqueId() {
    return this.getIdentifier();
  }

  public enum Sound {
    PLING,
    PLONG
  }

}
