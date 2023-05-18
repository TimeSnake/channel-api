/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelLoggingMessage<Value> extends ChannelMessage<String, Value> {

  public ChannelLoggingMessage(String... args) {
    super(args);
  }

  public ChannelLoggingMessage(String name, MessageType<Value> type, Value value) {
    super(ChannelType.LOGGING, name, type, value);
    if (!MessageType.Logging.TYPES.contains(type)) {
      throw new InvalidMessageTypeException();
    }
  }
}
