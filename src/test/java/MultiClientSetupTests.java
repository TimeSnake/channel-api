/*
 * Copyright (C) 2023 timesnake
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.timesnake.channel.core.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class MultiClientSetupTests extends TestNetwork {

  @BeforeAll
  static void logInfo() {
    ((Logger) LoggerFactory.getLogger("channel")).setLevel(Level.INFO);
  }

  @Test
  void check3Clients() throws InterruptedException {
    List<Channel> clients = createChannelInstances(3);

    clients.get(0).start();
    clients.get(0).selfInit();

    clients.get(1).start();
    clients.get(1).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));
    sleep();

    clients.get(2).start();
    clients.get(2).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));
    sleep();

    Assertions.assertEquals(2, clients.get(0).getChannelConnections().size(), clients.get(0).getSelf().getName());
    Assertions.assertEquals(2, clients.get(1).getChannelConnections().size(), clients.get(1).getSelf().getName());
    Assertions.assertEquals(2, clients.get(2).getChannelConnections().size(), clients.get(2).getSelf().getName());

    clients.forEach(Channel::stop);
  }

  @Test
  void check3ClientsNoDelay() throws InterruptedException {
    List<Channel> clients = createChannelInstances(3);

    clients.get(0).start();
    clients.get(0).selfInit();

    clients.get(1).start();
    clients.get(1).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));

    clients.get(2).start();
    clients.get(2).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));
    sleep();

    Assertions.assertEquals(2, clients.get(0).getChannelConnections().size(), clients.get(0).getSelf().getName());
    Assertions.assertEquals(2, clients.get(1).getChannelConnections().size(), clients.get(1).getSelf().getName());
    Assertions.assertEquals(2, clients.get(2).getChannelConnections().size(), clients.get(2).getSelf().getName());

    clients.forEach(Channel::stop);
  }

  @Test
  void check7Clients() throws InterruptedException {
    List<Channel> clients = createChannelInstances(7);

    clients.get(0).start();
    clients.get(0).selfInit();

    for (int i = 1; i < clients.size(); i++) {
      clients.get(i).start();
      clients.get(i).registerToNetwork(clients.get(0).getSelf(), Duration.ofSeconds(10));
      sleep();
    }

    for (Channel client : clients) {
      Assertions.assertEquals(6, client.getChannelConnections().size(), client.getSelf().getName());
    }

    clients.forEach(Channel::stop);
  }
}
