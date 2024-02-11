/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util;

import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ResultMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Future;

public interface Channel {

  static Channel getInstance() {
    return de.timesnake.channel.core.Channel.getInstance();
  }

  void addListener(ChannelListener listener);

  <Identifier extends Serializable> void addListener(ChannelListener listener,
                                                     @NotNull Collection<Identifier> identifiers);

  void removeListener(ChannelListener listener);

  Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message);

  ResultMessage sendMessageSync(ChannelMessage<?, ?> message);

}
