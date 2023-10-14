/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

public class ChannelException extends RuntimeException {

  public ChannelException() {
  }

  public ChannelException(String message) {
    super(message);
  }

  public ChannelException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChannelException(Throwable cause) {
    super(cause);
  }

  public ChannelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
