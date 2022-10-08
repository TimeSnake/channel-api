/*
 * channel-api.main
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

package de.timesnake.channel.util.message;

import de.timesnake.channel.core.ChannelType;

public class ChannelServerMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelServerMessage(String... args) {
        super(args);
    }

    public ChannelServerMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.SERVER, name, type, value);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public ChannelServerMessage(String name, MessageType<Value> type) {
        super(ChannelType.SERVER, name, type);
        if (!MessageType.Server.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public String getName() {
        return super.getIdentifier();
    }

    public enum State {
        READY
    }
}
