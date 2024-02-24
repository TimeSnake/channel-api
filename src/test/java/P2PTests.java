/*
 * Copyright (C) 2023 timesnake
 */

import de.timesnake.channel.core.Channel;
import de.timesnake.channel.util.listener.ChannelListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;

import java.time.Duration;

public class P2PTests extends TestNetwork {

  private Channel core;
  private Channel client;

  @BeforeAll
  static void logInfo() {
    Configurator.setAllLevels("channel", Level.INFO);
  }

  @BeforeEach
  void setupChannels() {
    int corePort = anyPort();
    core = createChannelInstance(corePort);
    core.start();
    core.selfInit();

    client = createChannelInstance(anyPort());
    client.start();
    client.registerToNetwork(core.getSelf(), Duration.ofSeconds(10));
  }

  @AfterEach
  void closeChannels() throws InterruptedException {
    core.stop();
    client.stop();
    sleep();
  }

  @Test
  void checkListenerAddAndRemove() throws InterruptedException {
    final int[] recvMsg = {0};

    ChannelListener listener = listener(msg -> recvMsg[0]++);

    core.addListener(listener);
    sleep();

    client.sendMessage(anyMessage());
    sleep();

    core.removeListener(listener);
    sleep();

    client.sendMessageSync(anyMessage());
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size());
    Assertions.assertEquals(1, client.getChannelConnections().size());
    Assertions.assertEquals(1, recvMsg[0]);
  }

  @Test
  void checkDuplicateListenerAdd() throws InterruptedException {
    final int[] recvMsg = {0};

    ChannelListener listener1 = listener(msg -> recvMsg[0]++);
    ChannelListener listener2 = listener(msg -> recvMsg[0]++);

    core.addListener(listener1);
    sleep();

    client.sendMessage(anyMessage());
    sleep();

    core.addListener(listener2);
    sleep();

    client.sendMessage(anyMessage());
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size());
    Assertions.assertEquals(1, client.getChannelConnections().size());
    Assertions.assertEquals(1, client.getSender().getListenerParticipants(anyMessage()).size());
    Assertions.assertEquals(3, recvMsg[0]);
  }

  @Test
  void checkDuplicateListenerAddAndOneRemove() throws InterruptedException {
    final int[] recvMsg = {0};

    ChannelListener listener1 = listener(msg -> recvMsg[0]++);
    ChannelListener listener2 = listener(msg -> recvMsg[0]++);

    core.addListener(listener1);
    core.addListener(listener2);
    sleep();

    client.sendMessage(anyMessage());
    sleep();

    core.removeListener(listener2);
    sleep();

    client.sendMessage(anyMessage());
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size());
    Assertions.assertEquals(1, client.getChannelConnections().size());
    Assertions.assertEquals(1, client.getSender().getListenerParticipants(anyMessage()).size());
    Assertions.assertEquals(3, recvMsg[0]);
  }
}
