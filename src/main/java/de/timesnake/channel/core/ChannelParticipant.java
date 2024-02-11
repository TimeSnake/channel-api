/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import java.io.Serializable;
import java.util.Objects;

public class ChannelParticipant implements Serializable {

  private final String hostname;
  private final int listenPort;

  public ChannelParticipant(String hostname, int listenPort) {
    this.hostname = hostname;
    this.listenPort = listenPort;
  }

  public String getHostname() {
    return hostname;
  }

  public int getListenPort() {
    return listenPort;
  }

  public String getName() {
    return this.getHostname() + ":" + this.getListenPort();
  }

  @Override
  public String toString() {
    return this.getHostname() + ":" + this.getListenPort();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.hostname, this.listenPort);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof ChannelParticipant)) {
      return false;
    }
    return this.hostname.equals(((ChannelParticipant) o).getHostname()) && this.listenPort == ((ChannelParticipant) o).getListenPort();
  }
}
