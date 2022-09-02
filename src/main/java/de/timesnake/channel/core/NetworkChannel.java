package de.timesnake.channel.core;

import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.MessageType;

public class NetworkChannel {

    public static void start(Channel channel) {
        NetworkChannel.channel = channel;
        thread = new Thread(channel);
        thread.start();
        channel.logInfo("Network-channel started", true);
    }

    public static void stop() {
        if (!channel.getProxyName().equals(channel.getServerName())) {
            channel.sendMessageToProxy(new ChannelListenerMessage<>(NetworkChannel.getChannel().getSelf(),
                    MessageType.Listener.UNREGISTER_SERVER, channel.getServerName()));
        }
        if (thread.isAlive()) {
            thread.interrupt();
            channel.logInfo("[Channel] Network-channel stopped", true);
        }
    }

    public static Channel getChannel() {
        return channel;
    }

    private static Channel channel;
    private static Thread thread;

}
