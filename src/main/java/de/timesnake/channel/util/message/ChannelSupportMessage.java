/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;

public class ChannelSupportMessage<Value> extends ChannelMessage<String, Value> {

  public ChannelSupportMessage(String... args) {
    super(args);
  }

  public ChannelSupportMessage(@NotNull String name, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.SUPPORT, name, type, value);
    if (!MessageType.Support.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  @NotNull
  public String getName() {
    return super.getIdentifier();
  }

}
