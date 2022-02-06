package de.timesnake.channel.message;

import de.timesnake.channel.channel.ChannelType;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class ChannelMessage implements de.timesnake.channel.api.message.ChannelMessage {

    public static final String DIVIDER = ";";

    protected ArrayList<String> args = new ArrayList<>();

    public ChannelMessage(String... args) {
        this.args.addAll(Arrays.asList(args));
    }

    public ChannelMessage(ChannelType type, String... args) {
        this.args.add(type.name());
        this.args.addAll(Arrays.asList(args));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String arg : args) {
            s.append(arg).append(ChannelMessage.DIVIDER);
        }
        return s.toString();
    }

}
