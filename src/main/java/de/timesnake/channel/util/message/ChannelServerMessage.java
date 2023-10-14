/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ChannelServerMessage<Value extends Serializable> extends ChannelMessage<String, Value> {

  public ChannelServerMessage(@NotNull String name, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.SERVER, name, type, value);
    if (!MessageType.Server.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelServerMessage(@NotNull String name, @NotNull MessageType<Value> type) {
    super(ChannelType.SERVER, name, type);
    if (!MessageType.Server.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  @NotNull
  public String getName() {
    return super.getIdentifier();
  }

  public enum State {
    READY
  }
}
