package de.timesnake.channel.channel;

public class ChannelInfo {

    private static boolean broadcast = false;

    public static void broadcastMessage(String msg) {
        if (broadcast) {
            System.out.println("[Channel] " + msg);
        }
    }

    public static void setBroadcast(boolean broadcast) {
        ChannelInfo.broadcast = broadcast;
    }

    public static boolean getBroadcast() {
        return broadcast;
    }
}
