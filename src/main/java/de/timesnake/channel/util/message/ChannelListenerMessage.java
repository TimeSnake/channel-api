/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import de.timesnake.channel.core.Host;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class ChannelListenerMessage<Value extends Serializable> extends ChannelMessage<Host, Value> {

  public ChannelListenerMessage(@NotNull Host host, @NotNull MessageType<Value> type, Value value) {
    super(ChannelType.LISTENER, host, type, value);
    if (!MessageType.Listener.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }

  public ChannelListenerMessage(@NotNull Host host, @NotNull MessageType<Value> type) {
    super(ChannelType.LISTENER, host, type);
    if (!MessageType.Listener.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }
}
