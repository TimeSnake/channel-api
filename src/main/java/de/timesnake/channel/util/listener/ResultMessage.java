/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

import de.timesnake.channel.core.Host;
import de.timesnake.library.basic.util.Tuple;

import java.util.HashMap;
import java.util.Map;

public class ResultMessage {

  private boolean successful = true;

  private final Map<Host, Tuple<Boolean, ChannelException>> sendResults = new HashMap<>();

  public ResultMessage() {

  }

  public ResultMessage addResult(Host host, Boolean successful, ChannelException exception) {
    this.sendResults.put(host, new Tuple<>(successful, exception));

    if (!successful) {
      this.successful = false;
    }

    return this;
  }

  public ResultMessage addResult(ResultMessage resultMessage) {
    this.sendResults.putAll(resultMessage.getSendResults());

    if (!resultMessage.isSuccessful()) {
      this.successful = false;
    }

    return this;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Map<Host, Tuple<Boolean, ChannelException>> getSendResults() {
    return sendResults;
  }
}
