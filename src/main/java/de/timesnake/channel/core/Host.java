/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import java.io.Serializable;
import java.util.Objects;

public class Host implements Serializable {

  private final String hostname;
  private final int port;

  public Host(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "Host{" +
        "hostname='" + hostname + '\'' +
        ", port=" + port +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.hostname, this.port);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Host)) {
      return false;
    }
    return this.hostname.equals(((Host) o).getHostname()) && this.port == ((Host) o).getPort();
  }

  public String getName() {
    return this.getHostname() + ":" + this.getPort();
  }
}
