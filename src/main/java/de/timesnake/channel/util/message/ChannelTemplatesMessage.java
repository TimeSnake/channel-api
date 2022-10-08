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

public class ChannelTemplatesMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelTemplatesMessage(String... args) {
        super(args);
    }

    public ChannelTemplatesMessage(String name, MessageType<Value> type, Value value) {
        super(ChannelType.TEMPLATES, name, type, value);
        if (!MessageType.Templates.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }
}
