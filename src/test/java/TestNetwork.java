/*
 * Copyright (C) 2023 timesnake
 */

import ch.qos.logback.classic.Logger;
import de.timesnake.channel.core.Channel;
import de.timesnake.channel.core.ChannelParticipant;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Status;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestNetwork {

  public static final Logger LOGGER = (Logger) LoggerFactory.getLogger("test");
  private static int PORT_COUNTER = 10000;

  public static List<Channel> createChannelInstances(int number) {
    int corePort = anyPort();

    List<Channel> channels = new ArrayList<>();
    channels.add(createChannelInstance(corePort, corePort));

    for (int i = 1; i < number; i++) {
      channels.add(createChannelInstance(anyPort(), corePort));
    }
    return channels;
  }

  public static Channel createChannelInstance(int port, int proxyPort) {
    return new Channel(Thread.currentThread(),
        new ChannelParticipant("127.0.0.1", port),
        "0.0.0.0"
    ) {
      @Override
      protected void runSync(Runnable runnable) {
        new Thread(runnable).start();
      }
    };
  }

  public static void sleep() throws InterruptedException {
    LOGGER.info("sleep");
    Thread.sleep(1000);
  }

  public static ChannelMessage<?, ?> anyMessage() {
    return new ChannelServerMessage<>("test", MessageType.Server.STATUS, Status.Server.ONLINE);
  }

  public static ChannelListener listener(Consumer<ChannelMessage<?, ?>> consumer) {
    return new ChannelListener() {
      @ChannelHandler(type = ListenerType.SERVER_STATUS)
      public void onMessage(ChannelServerMessage<Status> msg) {
        consumer.accept(msg);
      }
    };
  }

  public static int anyPort() {
    return PORT_COUNTER++;
  }
}
