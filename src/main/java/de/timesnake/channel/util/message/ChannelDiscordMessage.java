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

import java.util.*;

public class ChannelDiscordMessage<Value> extends ChannelMessage<String, Value> {

    public ChannelDiscordMessage(String... args) {
        super(args);
    }

    public ChannelDiscordMessage(String category, MessageType<Value> type, Value value) {
        super(ChannelType.DISCORD, category, type, value);
        if (!MessageType.Discord.TYPES.contains(type)) {
            throw new InvalidMessageTypeException();
        }
    }

    public static class Allocation {

        // Example team: Red.UUID1.UUID2.UUID3#Blue.UUID4.UUID5#Spectator
        private static final String TEAM_DELIMITER = "#";
        private static final String NAME_DELIMITER = "/";

        private final Map<String, ? extends Collection<UUID>> uuidsByTeam;

        protected Allocation(String toParse) {
            Map<String, LinkedList<UUID>> uuidsByTeam = new LinkedHashMap<>();

            for (String teamAllocation : toParse.split(TEAM_DELIMITER)) {
                String[] values = teamAllocation.split(NAME_DELIMITER);
                LinkedList<UUID> uuids = uuidsByTeam.compute(values[0], (k, v) -> new LinkedList<>());

                for (int i = 1; i < values.length; i++) {
                    uuids.add(UUID.fromString(values[i]));
                }
            }

            this.uuidsByTeam = uuidsByTeam;
        }

        public Allocation(Map<String, ? extends Collection<UUID>> uuidsByTeam) {
            this.uuidsByTeam = uuidsByTeam;
        }


        public Map<String, ? extends Collection<UUID>> getAllocation() {
            return uuidsByTeam;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, ? extends Collection<UUID>> entry : this.uuidsByTeam.entrySet()) {
                sb.append(entry.getKey()); // Team

                if (!entry.getValue().isEmpty()) {
                    sb.append(NAME_DELIMITER);

                    for (UUID player : entry.getValue()) {
                        sb.append(player.toString());
                        sb.append(NAME_DELIMITER);
                    }

                    sb.deleteCharAt(sb.length() - 1); // Remove last NAME_DELIMITER

                }

                sb.append(TEAM_DELIMITER); // End of team
            }

            if (!this.uuidsByTeam.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1); // Remove last TEAM_DELIMITER
            }
            return sb.toString();
        }


    }


}
