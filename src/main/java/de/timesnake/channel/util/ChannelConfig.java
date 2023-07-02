/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util;

public interface ChannelConfig {

  String getServerHostName();

  String getListenHostName();

  String getProxyHostName();

  String getProxyServerName();

  int getPortOffset();

  int getProxyPort();
}
