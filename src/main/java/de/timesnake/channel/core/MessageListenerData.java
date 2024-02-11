/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class MessageListenerData<Identifier extends Serializable> implements Serializable {

  private final ChannelType<?> channelType;
  private final MessageType<?> messageType;
  private final Identifier identifier;

  public MessageListenerData(@NotNull ChannelType<?> channelType,
                             @NotNull MessageType<?> messageType,
                             @Nullable Identifier identifier) {
    this.channelType = channelType;
    this.messageType = messageType;
    this.identifier = identifier;
  }

  public @NotNull ChannelType<?> getChannelType() {
    return channelType;
  }

  public @NotNull MessageType<?> getMessageType() {
    return messageType;
  }

  public @Nullable Identifier getIdentifier() {
    return identifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageListenerData<?> that = (MessageListenerData<?>) o;
    return Objects.equals(channelType, that.channelType) && Objects.equals(messageType, that.messageType)
        && Objects.equals(identifier, that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelType, messageType, identifier);
  }

  @Override
  public String toString() {
    return "MessageListenerData{" +
        "channelType=" + channelType +
        ", messageType=" + messageType +
        ", identifier=" + identifier +
        '}';
  }
}
