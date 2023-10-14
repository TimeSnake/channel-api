/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util;

import de.timesnake.channel.core.Host;
import de.timesnake.channel.core.ServerChannel;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.ChannelMessage;

import java.util.concurrent.Future;

public interface Channel {

  static Channel getInstance() {
    return ServerChannel.getInstance();
  }

  void addListener(ChannelListener listener);

  void addListener(ChannelListener listener, ChannelMessageFilter<?> filter);

  void removeListener(ChannelListener listener, ListenerType... types);

  Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message);

  ResultMessage sendMessageSynchronized(ChannelMessage<?, ?> message);

  Host getHost();

  String getProxyName();
}
