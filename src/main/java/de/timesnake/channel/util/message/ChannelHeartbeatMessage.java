/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelParticipant;
import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.util.message.MessageType.Heartbeat;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ChannelHeartbeatMessage<Value extends Serializable> extends ChannelMessage<ChannelParticipant, Value> {

  public ChannelHeartbeatMessage(@NotNull ChannelParticipant sender, @NotNull MessageType<Value> type) {
    super(ChannelType.HEARTBEAT, sender, type);
    if (!Heartbeat.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelHeartbeatMessage(@NotNull ChannelParticipant sender, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.HEARTBEAT, sender, type, value);
    if (!Heartbeat.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }
}
