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
        private static final String NAME_DELIMITER = "\\.";

        private final Map<String, List<UUID>> uuidsByTeam;

        protected Allocation(String toParse){
            uuidsByTeam = new HashMap<>();

            for(String teamAllocation : toParse.split(TEAM_DELIMITER)){
                String[] values = teamAllocation.split(NAME_DELIMITER);
                List<UUID> uuids = new LinkedList<>();

                for(int i = 1; i < values.length; i++){
                    uuids.add(UUID.fromString(values[i]));
                }

                uuidsByTeam.put(values[0], uuids);

            }
        }

        public Allocation(Map<String, List<UUID>> uuidsByTeam){
            this.uuidsByTeam = uuidsByTeam;
        }


        public Map<String, List<UUID>> getAllocation(){
            return uuidsByTeam;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();

            for(Map.Entry<String, List<UUID>> entry : uuidsByTeam.entrySet()){
                sb.append(entry.getKey()); // Team

                if(!entry.getValue().isEmpty()){
                    sb.append(NAME_DELIMITER);

                    for(UUID player : entry.getValue()){
                        sb.append(player.toString());
                        sb.append(NAME_DELIMITER);
                    }

                    sb.deleteCharAt(sb.length()-1); // Remove last NAME_DELIMITER

                }

                sb.append(TEAM_DELIMITER); // End of team
            }

            sb.deleteCharAt(sb.length()-1); // Remove last TEAM_DELIMITER
            return sb.toString();
        }


    }


}
