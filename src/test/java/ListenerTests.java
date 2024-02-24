/*
 * Copyright (C) 2023 timesnake
 */

import de.timesnake.channel.core.Channel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

public class ListenerTests extends TestNetwork {

  @BeforeAll
  static void logInfo() {
    Configurator.setAllLevels("channel", Level.INFO);
  }

  @Test
  public void listener3Clients() throws InterruptedException {
    List<Channel> clients = createChannelInstances(3);

    int[] listenerCounter = new int[3];

    clients.get(0).start();
    clients.get(0).selfInit();

    clients.get(1).start();
    clients.get(1).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));

    clients.get(2).start();
    clients.get(2).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));
    sleep();

    clients.get(0).addListener(listener(msg -> listenerCounter[0]++));
    clients.get(1).addListener(listener(msg -> listenerCounter[1]++));
    clients.get(2).addListener(listener(msg -> listenerCounter[1]++));
    sleep();

    clients.get(2).sendMessage(anyMessage());
    sleep();

    Assertions.assertEquals(2, clients.get(0).getChannelConnections().size(), clients.get(0).getSelf().getName());
    Assertions.assertEquals(2, clients.get(1).getChannelConnections().size(), clients.get(1).getSelf().getName());
    Assertions.assertEquals(2, clients.get(2).getChannelConnections().size(), clients.get(2).getSelf().getName());

    Assertions.assertEquals(1, listenerCounter[0], clients.get(0).getSelf().getName());
    Assertions.assertEquals(1, listenerCounter[1], clients.get(1).getSelf().getName());
    Assertions.assertEquals(0, listenerCounter[2], clients.get(1).getSelf().getName());

    clients.forEach(Channel::stop);
  }
}
