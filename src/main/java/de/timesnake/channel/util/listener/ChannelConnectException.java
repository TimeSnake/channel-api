/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

public class ChannelConnectException extends ChannelException {

  public ChannelConnectException() {
  }

  public ChannelConnectException(String message) {
    super(message);
  }

  public ChannelConnectException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChannelConnectException(Throwable cause) {
    super(cause);
  }
}
