package de.timesnake.channel.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

public class ChannelMessageSender implements Runnable {

    private final String message;
    private Socket socket;
    private boolean connected = true;

    public ChannelMessageSender(Host host, String message) {
        this.message = message;
        try {
            this.socket = new Socket(host.getHostname(), host.getPort());
        } catch (ConnectException e) {
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (socket != null) {
            try {
                if (socket.isConnected()) {
                    BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    // senderPort/type/task</value></value>
                    socketWriter.write(this.message);
                    socketWriter.flush();
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public boolean isConnected() {
        if (connected) {
            return this.socket.isConnected();
        }
        return false;
    }

}
