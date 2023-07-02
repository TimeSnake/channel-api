/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;

public class ChannelGroupMessage<Value> extends ChannelMessage<String, Value> {

  public ChannelGroupMessage(String... args) {
    super(args);
  }

  public ChannelGroupMessage(@NotNull String name, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.GROUP, name, type, value);
    if (!MessageType.Group.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelGroupMessage(@NotNull String name, @NotNull MessageType<Value> type) {
    super(ChannelType.GROUP, name, type);
    if (!MessageType.Group.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }

  }

  @NotNull
  public String getName() {
    return super.getIdentifier();
  }
}
