/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.InvalidMessageTypeException;
import de.timesnake.channel.util.message.MessageType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ChannelControlMessage<Value extends Serializable> extends ChannelMessage<ChannelParticipant, Value> {

  public ChannelControlMessage(@NotNull ChannelParticipant host, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.CONTROL, host, type, value);
    if (!MessageType.Control.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelControlMessage(@NotNull ChannelParticipant host, @NotNull MessageType<Value> type) {
    super(ChannelType.CONTROL, host, type);
    if (!MessageType.Control.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }
}
