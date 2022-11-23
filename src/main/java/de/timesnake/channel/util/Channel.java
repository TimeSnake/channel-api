/*
 * workspace.channel-api.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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
