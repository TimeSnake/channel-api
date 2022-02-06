package de.timesnake.channel.channel;

public class ChannelReceiver {

    public static final ChannelReceiver ALL = new ChannelReceiver("all");

    private String receiver;

    public ChannelReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String toChannelMessage() {
        return this.receiver;
    }

    public String getName() {
        return this.receiver;
    }

    public static ChannelReceiver getServer(int port) {
        return new ChannelReceiver(String.valueOf(port));
    }
}
