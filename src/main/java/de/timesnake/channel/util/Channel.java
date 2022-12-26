/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util;

import de.timesnake.channel.core.Host;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.library.basic.util.LogHelper;

import java.util.logging.Logger;

public interface Channel {

    Logger LOGGER = LogHelper.getLogger("channel");

    static Channel getInstance() {
        return de.timesnake.channel.core.Channel.getInstance();
    }

    void addListener(ChannelListener listener);

    void addListener(ChannelListener listener, ChannelMessageFilter<?> filter);

    void removeListener(ChannelListener listener, ListenerType... types);

    void sendMessage(ChannelMessage<?, ?> message);

    void sendMessageSynchronized(ChannelMessage<?, ?> message);

    Host getHost();
}
