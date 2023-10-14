/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.ChannelConfig;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Tuple;
import org.jetbrains.annotations.NotNull;

public abstract class ServerChannel extends Channel {

  public static ServerChannel getInstance() {
    return (ServerChannel) Channel.getInstance();
  }

  protected final @NotNull String serverName;

  protected ServerChannel(@NotNull Thread mainThread, @NotNull ChannelConfig config, @NotNull String serverName, int serverPort) {
    super(mainThread,
        new Host(config.getServerHostName(), serverPort + config.getPortOffset()),
        new Host(config.getProxyHostName(), config.getProxyPort()),
        config.getListenHostName(), config.getProxyServerName());

    this.serverName = serverName;
  }

  @Override
  public void stop() {
    this.client.sendMessageToProxy(new ChannelListenerMessage<>(this.getSelf(), MessageType.Listener.UNREGISTER_SERVER, this.getServerName()));
    if (this.serverThread.isAlive()) {
      this.serverThread.interrupt();
      Loggers.CHANNEL.info("Network-channel stopped");
    }
  }

  @Override
  protected void loadChannelServer() {
    this.server = new ServerChannelServer(this);
  }

  @Override
  protected void loadChannelClient() {
    this.client = new ServerChannelClient(this);
  }

  public @NotNull String getServerName() {
    return serverName;
  }

  public static class ServerChannelServer extends ChannelServer {

    protected ServerChannelServer(ServerChannel manager) {
      super(manager);
    }

    @Override
    public void runSync(SyncRun syncRun) {
      this.manager.runSync(syncRun);
    }
  }

  public static class ServerChannelClient extends ChannelClient {

    public ServerChannelClient(ServerChannel manager) {
      super(manager);
    }

    @Override
    public void addRemoteListener(ChannelListenerMessage<?> msg) {
      if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
        Tuple<ChannelType<?>, ?> identifierListener = (Tuple<ChannelType<?>, ?>) msg.getValue();

        if (identifierListener.getA().equals(ChannelType.SERVER)
            && !identifierListener.getB().equals(((ServerChannel) this.manager).getServerName())) {
          return;
        }
      }

      super.addRemoteListener(msg);
    }
  }
}
