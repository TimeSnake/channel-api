/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChannelServer implements Runnable {

  public final Logger logger = LoggerFactory.getLogger("channel.server");

  protected final Channel manager;

  protected ChannelServer(Channel manager) {
    this.manager = manager;
  }

  @Override
  public void run() {
    this.startServer();
  }

  private void startServer() {
    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(this.manager.getSelf().getListenPort(), 100,
          InetAddress.getByName(this.manager.getListenHostName()));
    } catch (IOException e) {
      logger.error("Error while starting channel server/receiver: {}", e.getMessage());
      return;
    }

    while (true) {
      final Socket activeSocket;
      try {
        activeSocket = serverSocket.accept();
      } catch (IOException e) {
        logger.warn("Error while accepting message, restarting socket ...");
        try {
          serverSocket.close();
        } catch (IOException ex) {
          logger.warn("Error while closing socket, continue with restart ...");
        }

        this.startServer();
        return;
      }

      logger.debug("Accepting connection from '{}:{}'", activeSocket.getInetAddress().getHostName(),
          activeSocket.getPort());
      this.manager.acceptConnection(activeSocket);
    }

  }

}
