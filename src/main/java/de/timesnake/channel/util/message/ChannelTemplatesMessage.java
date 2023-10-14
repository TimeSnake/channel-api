/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ChannelTemplatesMessage<Value extends Serializable> extends ChannelMessage<String, Value> {

  public ChannelTemplatesMessage(@NotNull String name, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.TEMPLATES, name, type, value);
    if (!MessageType.Templates.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }
}
