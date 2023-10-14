/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.*;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.channel.util.message.MessageType.Listener;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Tuple;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
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
  public static boolean isInterestingForServer(Host host, String serverName, ChannelListenerMessage<?> msg) {
    if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
      if (((Tuple<ChannelType<?>, ?>) msg.getValue()).getA().equals(ChannelType.SERVER)) {
        if (!serverName.equals(((Tuple<ChannelType<?>, ?>) msg.getValue()).getB())) {
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


  protected ConcurrentHashMap<Host, ChannelConnection> channelByHost = new ConcurrentHashMap<>();
  protected Set<Socket> loggingSockets = ConcurrentHashMap.newKeySet();


  protected boolean connectedToProxy = false;
  protected boolean listenerLoaded = false;

  protected Set<ChannelListenerMessage<?>> listenerMessageStash = ConcurrentHashMap.newKeySet();
  protected Set<ChannelMessage<?, ?>> messageStash = ConcurrentHashMap.newKeySet();

  private final ExecutorService executorService = new ThreadPoolExecutor(5, 100, 5L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>());


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
    ResultMessage resultMessage = this.sendMessageSynchronizedToHost(this.manager.getProxy(), msg);

    if (resultMessage.isSuccessful()) {
      this.connectedToProxy = true;
      this.sendStashedListenerMessages();
      this.manager.onProxyConnected();
    } else {
      Loggers.CHANNEL.warning("Failed to connect to proxy, retrying ...");
      this.executorService.execute(() -> {
        try {
          Thread.sleep(retryPeriod.toMillis());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        this.connectToProxy(msg, retryPeriod);
      });
    }
  }

  public synchronized void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
    if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)
        || msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
      this.addRemoteListener(msg);
    } else if (msg.getMessageType().equals(MessageType.Listener.REGISTER_SERVER)
        && msg.getIdentifier().equals(this.manager.getProxy())) {
      Loggers.CHANNEL.info("Receiving of listeners finished");
      this.listenerLoaded = true;
      this.sendStashedMessages();
    } else if (msg.getMessageType().equals(MessageType.Listener.UNREGISTER_SERVER)) {
      Host host = msg.getIdentifier();

      this.listenerHostByMessageTypeByChannelType.forEach((k, v) -> v.remove(host));
      Loggers.CHANNEL.info("Removed listener " + host);

      this.disconnectHost(host);
    }
  }

  public void addRemoteListener(ChannelListenerMessage<?> msg) {
    Host senderHost = msg.getIdentifier();

    if (senderHost.equals(this.manager.getSelf())) {
      return;
    }

    if (msg.getMessageType().equals(MessageType.Listener.IDENTIFIER_LISTENER)) {
      Tuple<ChannelType<?>, ?> identifierListener = (Tuple<ChannelType<?>, ?>) msg.getValue();

      if (identifierListener.getA().equals(ChannelType.LOGGING)) {
        this.addLogListener(msg.getIdentifier());
        return;
      }

      this.listenerHostByIdentifierByChannelType.get(identifierListener.getA())
          .computeIfAbsent(identifierListener.getB(), k -> ConcurrentHashMap.newKeySet())
          .add(senderHost);

    } else if (msg.getMessageType().equals(MessageType.Listener.MESSAGE_TYPE_LISTENER)) {
      Tuple<ChannelType<?>, MessageType<?>> typeListener = (Tuple<ChannelType<?>, MessageType<?>>) msg.getValue();

      if (typeListener.getA().equals(ChannelType.LOGGING)) {
        this.addLogListener(msg.getIdentifier());
        return;
      }

      this.listenerHostByMessageTypeByChannelType.get(typeListener.getA())
          .computeIfAbsent(typeListener.getB(), k -> ConcurrentHashMap.newKeySet())
          .add(senderHost);
    }

    Loggers.CHANNEL.info("Added remote listener from '" + msg.getIdentifier() + "'");
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
              new Tuple<>(ChannelType.LOGGING, name)));
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
              new Tuple<>(type.getChannelType(), messageType)));
        }
      }
    }
  }

  protected <Identifier extends Serializable> void sendIdentifierListener(ChannelType<Identifier> channelType, Collection<?> ids) {
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
          new Tuple<>(channelType, identifier)));
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
    Loggers.CHANNEL.info("Added remote log listener from '" + host.getName() + "'");
  }

  public void disconnectHost(Host host) {
    try {
      ChannelConnection channelConnection = this.channelByHost.remove(host);
      if (channelConnection != null) {
        if (channelConnection.getSocket() != null) {
          channelConnection.getSocket().close();
        }
      }
      Loggers.CHANNEL.info("Closed socket to " + host.getName());
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

  public Future<ResultMessage> sendMessage(ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSynchronized(message));
  }

  public ResultMessage sendMessageSynchronized(ChannelMessage<?, ?> message) {
    ResultMessage resultMessage = new ResultMessage();
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
        resultMessage.addResult(this.sendMessageSynchronizedToHost(host, message));
      }
    } else {
      this.messageStash.add(message);
    }

    if (!sendToProxy && !this.manager.getProxy().equals(this.manager.getSelf())) {
      resultMessage.addResult(this.sendMessageSynchronizedToHost(this.manager.getProxy(), message));
    }

    return resultMessage;
  }

  public Future<ResultMessage> sendMessageToProxy(ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSynchronizedToProxy(message));
  }

  public ResultMessage sendMessageSynchronizedToProxy(ChannelMessage<?, ?> message) {
    return this.sendMessageSynchronizedToHost(this.manager.getProxy(), message);
  }


  public Future<ResultMessage> sendMessageToHost(Host host, ChannelMessage<?, ?> message) {
    return this.executorService.submit(() -> this.sendMessageSynchronizedToHost(host, message));
  }

  public ResultMessage sendMessageSynchronizedToHost(Host host, ChannelMessage<?, ?> message) {
    try {
      ChannelConnection channelConnection = this.channelByHost.get(host);

      try {
        if (channelConnection != null) {
          if (!channelConnection.getLock().tryLock(3, TimeUnit.SECONDS)) {
            Loggers.CHANNEL.warning("Unable to lock connection to '" + host.getName() + "', due to timeout");
            return new ResultMessage().addResult(host, false,
                new ChannelConnectException("send lock error", new TimeoutException("timed out while locking sending channel thread")));
          }
        }

        this.sendMessageSynchronized(host, message, 0, null);
      } catch (InterruptedException e) {
        Loggers.CHANNEL.warning("Unable to lock connection to '" + host.getName() + "', due to interruption");
        return new ResultMessage().addResult(host, false, new ChannelConnectException("send lock error", e));
      } finally {
        if (channelConnection != null) {
          channelConnection.getLock().unlock();
        }
      }
      return new ResultMessage().addResult(host, true, null);
    } catch (IOException e) {
      Loggers.CHANNEL.warning("Failed to setup connection to '" + host.getName() + "': " + e.getMessage());
      return new ResultMessage().addResult(host, false, new ChannelConnectException("connection setup exception", e));
    }
  }

  public final void sendMessageSynchronized(Host host, ChannelMessage<?, ?> message, int retry, Exception lastException)
      throws IOException {
    AtomicReference<Exception> exception = new AtomicReference<>();

    ChannelConnection channelConnection = this.channelByHost.computeIfAbsent(host, h -> {
      try {
        return new ChannelConnection(host, new Socket(host.getHostname(), host.getPort()));
      } catch (IOException e) {
        exception.set(e);
        return null;
      }
    });

    if (retry > ServerChannel.CONNECTION_RETRIES) {
      throw new IOException("Unable to establish connection to '" + host + "': " + lastException.getMessage());
    }

    if (retry == ServerChannel.CONNECTION_RETRIES) {
      this.disconnectHost(host);
      this.sendMessageSynchronized(host, message, retry + 1, null);
    }

    if (channelConnection == null) {
      this.channelByHost.remove(host);
      this.sendMessageSynchronized(host, message, retry + 1, exception.get());
      return;
    }

    if (!channelConnection.getSocket().isConnected()) {
      Socket socket = new Socket(host.getHostname(), host.getPort());
      channelConnection.setSocket(socket);
      channelConnection.setOutputStream(new ObjectOutputStream(socket.getOutputStream()));
    }

    try {
      if (channelConnection.getSocket().isConnected()) {
        channelConnection.getOutputStream().writeObject(message);
        channelConnection.getOutputStream().flush();
        Loggers.CHANNEL.info("Send to '" + host.getName() + "': '" + message + "'");
      } else {
        this.sendMessageSynchronized(host, message, retry + 1, new ConnectException("socket is not connected"));
      }
    } catch (IOException e) {
      this.sendMessageSynchronized(host, message, retry + 1, e);
    }
  }


  public ConcurrentHashMap<Host, ChannelConnection> getChannelByHost() {
    return channelByHost;
  }

  public static class ChannelConnection {

    private final Host host;
    private Socket socket;
    private ObjectOutputStream outputStream;

    private final ReentrantLock lock = new ReentrantLock();

    public ChannelConnection(Host host, Socket socket) throws IOException {
      this.host = host;
      this.socket = socket;
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public Host getHost() {
      return host;
    }

    public Socket getSocket() {
      return socket;
    }

    public void setSocket(Socket socket) {
      this.socket = socket;
    }

    public ObjectOutputStream getOutputStream() {
      return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
      this.outputStream = outputStream;
    }

    public ReentrantLock getLock() {
      return lock;
    }
  }
}
