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

package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.MessageType;

public class NetworkChannel {

    public static void start(Channel channel) {
        NetworkChannel.channel = channel;
        thread = new Thread(channel);
        thread.start();
        Channel.LOGGER.info("Network-channel started");
    }

    public static void stop() {
        channel.sendMessageToProxy(new ChannelListenerMessage<>(NetworkChannel.getChannel().getSelf(),
                MessageType.Listener.UNREGISTER_SERVER, channel.getServerName()));
        if (thread.isAlive()) {
            thread.interrupt();
            Channel.LOGGER.info("Network-channel stopped");
        }
    }

    public static Channel getChannel() {
        return channel;
    }

    private static Channel channel;
    private static Thread thread;

}
