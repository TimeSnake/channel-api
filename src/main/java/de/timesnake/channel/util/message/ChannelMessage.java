/*
 * timesnake.channel-api.main
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

public abstract class ChannelMessage<Identifier, Value> {

    public static final String DIVIDER = ";";

    protected ChannelType<Identifier> channelType;
    protected MessageType<Value> messageType;
    protected Identifier identifier;
    protected Value value;

    public ChannelMessage(String... args) {
        this.channelType = (ChannelType<Identifier>) ChannelType.valueOf(args[0]);
        this.messageType = (MessageType<Value>) channelType.parseMessageType(args[1]);
        this.identifier = this.channelType.parseIdentifier(args[2]);
        if (args.length >= 4) {
            this.value = this.messageType.parseValue(args[3]);
        } else {
            this.value = this.messageType.parseValue("");
        }
    }

    public ChannelMessage(ChannelType<Identifier> channelType, Identifier identifier, MessageType<Value> messageType,
                          Value value) {
        this.channelType = channelType;
        this.messageType = messageType;
        this.identifier = identifier;
        this.value = value;
    }


    public ChannelMessage(ChannelType<Identifier> channelType, Identifier identifier, MessageType<Value> messageType) {
        this.channelType = channelType;
        this.messageType = messageType;
        this.identifier = identifier;
    }

    public String toStream() {
        return this.channelType.getName() +
                ChannelMessage.DIVIDER + this.messageType.getName() +
                ChannelMessage.DIVIDER + this.channelType.identifierToString(this.identifier) +
                ChannelMessage.DIVIDER + this.messageType.valueToString(this.value);
    }

    public ChannelType<Identifier> getChannelType() {
        return channelType;
    }

    public MessageType<Value> getMessageType() {
        return messageType;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Value getValue() {
        return value;
    }
}
