/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelMessageFilter;
import de.timesnake.channel.util.listener.InconsistentChannelListenerException;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.library.basic.util.Loggers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

public class ChannelClient {

  /**
   * Checks if server is interested in message.
   * <p>
   * If message is server identifier listener, when check if listener identifier is equals with
   * receiver server name.
   * </p>
   *
   * @param host       Host to send to
   * @param serverName Name of server to send to
   * @param msg        Listener message, which is sent
   * @return true if message is interesting for server, else false
   */
  public static boolean isInterestingForServer(Host host, String serverName,
                                               ChannelListenerMessage<?> msg) {
    if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
      if (((MessageType.MessageIdentifierListener<?>) msg.getValue()).getChannelType().equals(ChannelType.SERVER)) {
        if (!serverName.equals(((MessageType.MessageIdentifierListener<?>) msg.getValue()).getIdentifier())) {
          return false;
        }
      }
    }
    return !msg.getIdentifier().equals(host);
  }

  protected final Channel manager;

  protected ConcurrentHashMap<ChannelType<?>, Set<?>> sentListenerIdentifiers = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<ChannelType<?>, Set<MessageType<?>>> sentListenerMessageTypes = new ConcurrentHashMap<>();
  protected Set<String> sentLoggingListeners = ConcurrentHashMap.newKeySet();

  /**
   * Only for servers, which are interested
   */
  protected final ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<Object, Set<Host>>>
      listenerHostByIdentifierByChannelType = new ConcurrentHashMap<>();
  protected final ConcurrentHashMap<ChannelType<?>, ConcurrentHashMap<MessageType<?>, Set<Host>>>
      listenerHostByMessageTypeByChannelType = new ConcurrentHashMap<>();


  protected ConcurrentHashMap<Host, Socket> socketByHost = new ConcurrentHashMap<>();
  protected Set<Socket> loggingSockets = ConcurrentHashMap.newKeySet();


  protected boolean connectedToProxy = false;
  protected boolean listenerLoaded = false;

  protected Set<ChannelListenerMessage<?>> listenerMessageStash = ConcurrentHashMap.newKeySet();
  protected Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();


  public ChannelClient(Channel manager) {
    this.manager = manager;

    for (ChannelType<?> type : ChannelType.TYPES) {
      this.listenerHostByIdentifierByChannelType.put(type, new ConcurrentHashMap<>());
    }

    for (ChannelType<?> type : ChannelType.TYPES) {
      this.listenerHostByMessageTypeByChannelType.put(type, new ConcurrentHashMap<>());
    }
  }

  public void connectToProxy(ChannelListenerMessage<?> msg, Duration retryPeriod) {
    boolean successful = this.sendMessageSynchronized(this.manager.getProxy(), msg);

    if (successful) {
      this.connectedToProxy = true;
      this.sendStashedListenerMessages();
      this.manager.onProxyConnected();
    } else {
      Loggers.CHANNEL.warning("Failed to connect to proxy, retrying ...");
      new Thread(() -> {
        try {
          Thread.sleep(retryPeriod.toMillis());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        this.connectToProxy(msg, retryPeriod);
      }).start();
    }
  }

  public void sendListenerMessage(ListenerType type, ChannelMessageFilter<?> filter) {
    if (type.getChannelType().equals(ChannelType.LOGGING)) {
      if (filter != null && filter.getIdentifierFilter() != null) {
        Collection<String> identifiers;

        try {
          identifiers = (Collection<String>) filter.getIdentifierFilter();
        } catch (ClassCastException e) {
          throw new InconsistentChannelListenerException("invalid filter type");
        }

        for (String name : identifiers) {
          if (this.sentLoggingListeners.contains(name)) {
            continue;
          }

          this.sentLoggingListeners.add(name);

          this.sendListenerMessage(new ChannelListenerMessage<>(this.manager.getSelf(), Listener.IDENTIFIER_LISTENER,
              new MessageType.MessageIdentifierListener<>(ChannelType.LOGGING, name)));
        }
      }
    } else {
      if (filter != null && filter.getIdentifierFilter() != null) {
        this.sendIdentifierListener(type.getChannelType(), filter.getIdentifierFilter());
      } else {
        Set<MessageType<?>> sentMessageTypes = this.sentListenerMessageTypes
            .computeIfAbsent(type.getChannelType(), k -> ConcurrentHashMap.newKeySet());

        Collection<MessageType<?>> messageTypes = type.getMessageType() != null ? Set.of(type.getMessageType()) :
            type.getChannelType().getMessageTypes();

        for (MessageType<?> messageType : messageTypes) {
          if (sentMessageTypes.contains(messageType)) {
            continue;
          }

          sentMessageTypes.add(messageType);

          Loggers.CHANNEL.info("Sending '" + type.getChannelType().getName() + " " + messageType + "' message type listener message");
          this.sendListenerMessage(new ChannelListenerMessage<>(this.manager.getSelf(), MessageType.Listener.MESSAGE_TYPE_LISTENER,
              new MessageType.MessageTypeListener(type.getChannelType(), messageType)));
        }
      }
    }
  }

  protected <Identifier> void sendIdentifierListener(ChannelType<Identifier> channelType, Collection<?> ids) {
    Collection<Identifier> identifiers;

    try {
      identifiers = ((Collection<Identifier>) ids);
    } catch (ClassCastException e) {
      throw new InconsistentChannelListenerException("invalid filter type");
    }


    Set<Identifier> sentIdentifiers = (Set<Identifier>) this.sentListenerIdentifiers
        .computeIfAbsent(channelType, k -> ConcurrentHashMap.newKeySet());

    for (Identifier identifier : identifiers) {
      if (sentIdentifiers.contains(identifier)) {
        continue;
      }

      sentIdentifiers.add(identifier);

      Loggers.CHANNEL.info("Sending '" + channelType.getName() + " " + identifier + "' identifier listener message");
      this.sendListenerMessage(new ChannelListenerMessage<>(this.manager.getSelf(), MessageType.Listener.IDENTIFIER_LISTENER,
          new MessageType.MessageIdentifierListener<>(channelType, identifier)));
    }
  }

  protected void sendStashedListenerMessages() {
    this.listenerMessageStash.forEach(this::sendMessageSynchronizedToProxy);
    Loggers.CHANNEL.info("Send stashed listener messages");
  }

  protected void sendStashedMessages() {
    this.messageStash.forEach(this.manager::sendMessage);
    this.messageStash.clear();
    Loggers.CHANNEL.info("Send stashed messages");
  }

  protected void addLogListener(Host host) {
    Logger logger = Logger.getLogger("");
    try {
      logger.addHandler(new SocketHandler(host.getHostname(), host.getPort()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void disconnectHost(Host host) {
    try {
      Socket socket = this.socketByHost.remove(host);
      if (socket != null) {
        socket.close();
      }
      Loggers.CHANNEL.info("Closed socket to " + host);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void sendListenerMessage(ChannelListenerMessage<?> msg) {
    if (!this.connectedToProxy) {
      this.listenerMessageStash.add(msg);
      return;
    }

    this.sendMessageToProxy(msg);
  }

  public void sendMessage(ChannelMessage<?, ?> message) {
    new Thread(() -> this.sendMessageSynchronized(message)).start();
  }

  public void sendMessageSynchronized(ChannelMessage<?, ?> message) {
    boolean sendToProxy = false;

    if (this.listenerLoaded) {
      Set<Host> messageTypeListenerHosts = this.listenerHostByMessageTypeByChannelType
          .get(message.getChannelType()).get(message.getMessageType());
      Set<Host> identifierListenerHosts = this.listenerHostByIdentifierByChannelType
          .get(message.getChannelType()).get(message.getIdentifier());

      Set<Host> listenerHosts = new HashSet<>();

      if (messageTypeListenerHosts != null) {
        listenerHosts = messageTypeListenerHosts;
      }
      if (identifierListenerHosts != null) {
        listenerHosts.addAll(identifierListenerHosts);
      }

      for (Host host : listenerHosts) {
        if (host.equals(this.manager.getProxy())) {
          sendToProxy = true;
        }
        this.sendMessageSynchronized(host, message);
      }
    } else {
      this.messageStash.add(message);
    }

    if (!sendToProxy && !this.manager.getProxy().equals(this.manager.getSelf())) {
      this.sendMessageSynchronized(this.manager.getProxy(), message);
    }
  }

  public void sendMessageToProxy(ChannelMessage<?, ?> message) {
    this.sendMessage(this.manager.getProxy(), message);
  }

  public void sendMessageSynchronizedToProxy(ChannelMessage<?, ?> message) {
    this.sendMessageSynchronized(this.manager.getProxy(), message);
  }


  public void sendMessage(Host host, ChannelMessage<?, ?> message) {
    new Thread(() -> this.sendMessageSynchronized(host, message)).start();
  }

  public boolean sendMessageSynchronized(Host host, ChannelMessage<?, ?> message) {
    try {
      this.sendMessageSynchronized(host, message, 0, null);
      return true;
    } catch (IOException e) {
      Loggers.CHANNEL.warning("Failed to setup connection to '" + host.getHostname() + ":" + host.getPort() + "'");
      return false;
    }
  }

  public final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message, int retry, Exception lastException)
      throws IOException {
    AtomicReference<Exception> exception = new AtomicReference<>();

    Socket socket = this.socketByHost.computeIfAbsent(host, h -> {
      try {
        return new Socket(host.getHostname(), host.getPort());
      } catch (IOException e) {
        exception.set(e);
        return null;
      }
    });

    if (retry >= ServerChannel.CONNECTION_RETRIES) {
      throw new IOException("Unable to establish connection to '" + host + "': " + lastException.getMessage());
    }

    if (socket == null) {
      this.socketByHost.remove(host);
      this.sendMessageSynchronized(host, message, retry + 1, exception.get());
      return;
    }

    try {
      if (socket.isConnected()) {
        OutputStreamWriter socketWriter = new OutputStreamWriter(socket.getOutputStream());
        socketWriter.write(message.toStream());
        socketWriter.write(System.lineSeparator());
        socketWriter.flush();
        Loggers.CHANNEL.info("Message send to " + host + ": '" + message.toStream() + "'");
      } else {
        this.sendMessageSynchronized(host, message, retry + 1, new ConnectException("socket is not connected"));
      }
    } catch (IOException e) {
      this.sendMessageSynchronized(host, message, retry + 1, e);
    }
  }

  public ConcurrentHashMap<Host, Socket> getSocketByHost() {
    return socketByHost;
  }
}
