/*
 * Copyright (C) 2023 timesnake
 */

import de.timesnake.channel.core.Channel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

public class ExceptionTests extends TestNetwork {

  @BeforeAll
  static void logInfo() {
    Configurator.setAllLevels("channel", Level.INFO);
  }

  @Test
  void updatedSocket() throws InterruptedException, IOException {
    List<Channel> clients = createChannelInstances(2);
    Channel core = clients.get(0);
    Channel client = clients.get(1);

    core.start();
    core.selfInit();

    client.start();
    client.registerToNetwork(core.getSelf(), Duration.ofSeconds(10));
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size(), core.getSelf().getName());
    Assertions.assertEquals(1, client.getChannelConnections().size(), client.getSelf().getName());

    sleep();
    core.getChannelConnection(client.getSelf()).updateSocket(new Socket(client.getSelf().getHostname(),
        client.getSelf().getListenPort()), true);
    sleep();

    client.addListener(listener(msg -> {
    }));
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size(), core.getSelf().getName());
    Assertions.assertEquals(1, client.getChannelConnections().size(), client.getSelf().getName());
    Assertions.assertEquals(1, core.getSender().getListenerParticipants(anyMessage()).size(),
        client.getSelf().getName());
  }

  @Test
  void closedSocket() throws InterruptedException, IOException {
    List<Channel> clients = createChannelInstances(2);
    Channel core = clients.get(0);
    Channel client = clients.get(1);

    core.start();
    core.selfInit();

    client.start();
    client.registerToNetwork(core.getSelf(), Duration.ofSeconds(10));
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size(), core.getSelf().getName());
    Assertions.assertEquals(1, client.getChannelConnections().size(), client.getSelf().getName());

    sleep();
    core.getChannelConnection(client.getSelf()).close();
    sleep();

    client.addListener(listener(msg -> {
    }));
    sleep();

    Assertions.assertEquals(1, core.getChannelConnections().size(), core.getSelf().getName());
    Assertions.assertEquals(1, client.getChannelConnections().size(), client.getSelf().getName());
    Assertions.assertEquals(1, core.getSender().getListenerParticipants(anyMessage()).size(),
        client.getSelf().getName());
  }
}
