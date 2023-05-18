/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.core.Host;
import de.timesnake.channel.util.message.MessageType.Heartbeat;

public class ChannelHeartbeatMessage<Value> extends ChannelMessage<Host, Value> {

  public ChannelHeartbeatMessage(String... args) {
    super(args);
  }

  public ChannelHeartbeatMessage(Host sender, MessageType<Value> type) {
    super(ChannelType.HEARTBEAT, sender, type);
    if (!Heartbeat.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelHeartbeatMessage(Host sender, MessageType<Value> type, Value value) {
    super(ChannelType.HEARTBEAT, sender, type, value);
    if (!Heartbeat.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public Host getSender() {
    return this.getIdentifier();
  }

}
