/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public abstract class ChannelMessage<Identifier extends Serializable, Value extends Serializable> implements Serializable {

  protected String source;
  protected final ChannelType<Identifier> channelType;
  protected final MessageType<Value> messageType;
  protected final Identifier identifier;
  protected Value value;

  public ChannelMessage(@NotNull ChannelType<Identifier> channelType, @NotNull Identifier identifier,
                        @NotNull MessageType<Value> messageType,
                        Value value) {
    this.channelType = channelType;
    this.messageType = messageType;
    this.identifier = identifier;
    this.value = value;
  }


  public ChannelMessage(@NotNull ChannelType<Identifier> channelType, @NotNull Identifier identifier,
                        MessageType<Value> messageType) {
    this.channelType = channelType;
    this.messageType = messageType;
    this.identifier = identifier;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public ChannelType<Identifier> getChannelType() {
    return channelType;
  }

  public MessageType<Value> getMessageType() {
    return messageType;
  }

  @NotNull
  public Identifier getIdentifier() {
    return identifier;
  }

  @Nullable
  public Value getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "ChannelMessage{" +
        "source=" + source +
        ", channelType=" + channelType +
        ", messageType=" + messageType +
        ", identifier=" + identifier +
        ", value=" + value +
        '}';
  }
}
