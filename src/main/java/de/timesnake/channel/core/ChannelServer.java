/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.*;
import de.timesnake.channel.util.message.ChannelHeartbeatMessage;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Tuple;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class ChannelServer implements Runnable {

  protected final Channel manager;

  protected ConcurrentHashMap<Tuple<ChannelType<?>, MessageType<?>>, ConcurrentHashMap<ChannelListener,
      Set<Tuple<ChannelMessageFilter<?>, Method>>>> listeners = new ConcurrentHashMap<>();

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
      serverSocket = new ServerSocket(this.manager.getSelf().getPort(), 100,
          InetAddress.getByName(this.manager.getListenHostName()));
    } catch (IOException e) {
      Loggers.CHANNEL.severe("Error while starting channel server");
      return;
    }

    while (true) {
      final Socket activeSocket;
      try {
        activeSocket = serverSocket.accept();
      } catch (IOException e) {
        Loggers.CHANNEL.warning("Error while accepting message, restarting socket ...");
        try {
          serverSocket.close();
        } catch (IOException ex) {
          Loggers.CHANNEL.warning("Error while closing socket, continue with restart ...");
        }

        this.startServer();
        return;
      }

      new Thread(() -> handleMessage(activeSocket)).start();
    }
  }

  private void handleMessage(Socket socket) {
    ObjectInputStream socketReader;
    try {
      InputStream inputStream = socket.getInputStream();
      socketReader = new ObjectInputStream(inputStream);

      while (true) {
        ChannelMessage<?, ?> msg;
        try {
          msg = (ChannelMessage<?, ?>) socketReader.readObject();
        } catch (StreamCorruptedException e) {
          Loggers.CHANNEL.warning("Exception while reading message: " + e.getClass().getSimpleName() + ": " + e.getMessage());
          continue;
        } catch (OptionalDataException e) {
          Loggers.CHANNEL.warning("Exception while reading message: " + e.getClass().getSimpleName() + ": object read failure: " + e.eof);
          continue;
        }

        Loggers.CHANNEL.info("Message received: " + msg);

        if (msg == null) {
          continue;
        }

        if (ChannelType.LISTENER.equals(msg.getChannelType())) {
          if (msg.getMessageType().equals(MessageType.Listener.CLOSE_SOCKET)) {
            socket.close();
            return;
          }
        }

        this.handleMessage(msg);
      }
    } catch (EOFException ignored) {

    } catch (Exception e) {
      Loggers.CHANNEL.warning("Exception while handling message: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    try {
      socket.close();
    } catch (IOException ex) {
      Loggers.CHANNEL.warning("Exception while closing socket: " + ex.getMessage());
    }
  }

  public void handleMessage(ChannelMessage<?, ?> msg) {
    ChannelType<?> type = msg.getChannelType();

    if (ChannelType.HEARTBEAT.equals(type)) {
      this.handleHeartBeatMessage((ChannelHeartbeatMessage<?>) msg);
      return;
    }

    if (ChannelType.LISTENER.equals(type)) {
      this.handleRemoteListenerMessage((ChannelListenerMessage<?>) msg);
      return;
    }

    this.invokeLocalListeners(msg);
  }

  public void invokeLocalListeners(ChannelMessage<?, ?> msg) {
    Set<Map.Entry<ChannelListener, Set<Tuple<ChannelMessageFilter<?>, Method>>>> set =
        this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), msg.getMessageType()),
            new ConcurrentHashMap<>()).entrySet();
    set.addAll(this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), null),
        new ConcurrentHashMap<>()).entrySet());
    set.addAll(this.listeners.getOrDefault(new Tuple<>(null, null), new ConcurrentHashMap<>())
        .entrySet());

    for (Map.Entry<ChannelListener, Set<Tuple<ChannelMessageFilter<?>, Method>>> subSet : set) {
      for (Tuple<ChannelMessageFilter<?>, Method> entry : subSet.getValue()) {
        ChannelListener listener = subSet.getKey();
        ChannelMessageFilter<?> filter = entry.getA();
        Method method = entry.getB();

        if (filter == null || filter.getIdentifierFilter() == null
            || filter.getIdentifierFilter().contains(msg.getIdentifier())) {
          if (method.getAnnotation(ChannelHandler.class).async()) {
            try {
              method.invoke(listener, msg);
            } catch (IllegalAccessException | InvocationTargetException e) {
              Loggers.CHANNEL.warning("Unable to invoke listener of '" + method.getClass().getName() + "': " + e.getMessage());
            }
          } else {
            this.runSync(() -> {
              try {
                method.invoke(listener, msg);
              } catch (IllegalAccessException | InvocationTargetException e) {
                Loggers.CHANNEL.warning("Unable to invoke listener of '" + method.getClass().getName() + "': " + e.getMessage());
              }
            });
          }

        }
      }
    }
  }

  public abstract void runSync(SyncRun syncRun);

  protected void handleHeartBeatMessage(ChannelHeartbeatMessage<?> msg) {
    this.manager.onHeartBeatMessage(msg);
  }

  protected void handleRemoteListenerMessage(ChannelListenerMessage<?> msg) {
    this.manager.getClient().handleRemoteListenerMessage(msg);
  }

  public void addLocalListener(ChannelListener listener) {
    this.addLocalListener(listener, null);
  }

  public void addLocalListener(ChannelListener listener, ChannelMessageFilter<?> filter) {

    Class<?> clazz = listener.getClass();

    do {
      for (Method method : clazz.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(ChannelHandler.class)) {
          continue;
        }

        if (method.getParameters().length != 1) {
          throw new InconsistentChannelListenerException("invalid parameter size");
        }

        ChannelHandler annotation = method.getAnnotation(ChannelHandler.class);
        ListenerType[] methodTypes = annotation.type();
        for (ListenerType type : methodTypes) {

          if (type.getMessageClass() != null && !type.getMessageClass().equals(method.getParameterTypes()[0])) {
            throw new InconsistentChannelListenerException("invalid message type");
          }

          Set<Tuple<ChannelMessageFilter<?>, Method>> listenerMethods =
              this.listeners.computeIfAbsent(type.getTypeTuple(), k -> new ConcurrentHashMap<>())
                  .computeIfAbsent(listener, k -> ConcurrentHashMap.newKeySet());

          if (annotation.filtered() && filter != null) {
            listenerMethods.add(new Tuple<>(filter, method));
          } else {
            listenerMethods.add(new Tuple<>(() -> null, method));
          }

          Loggers.CHANNEL.info("Added listener '" + type.name().toLowerCase() + "' of class '" + clazz.getSimpleName() + "'");
          this.manager.sendListenerMessage(type, filter);
        }
      }

      clazz = clazz.getSuperclass();
    } while (clazz != null && ChannelListener.class.isAssignableFrom(clazz));
  }

  public void removeListener(ChannelListener listener, ListenerType... types) {
    Collection<ConcurrentHashMap<ChannelListener, ?>> listeners = this.listeners.entrySet().stream()
        .filter(t -> types.length == 0 || Arrays.stream(types).anyMatch(type -> t.getKey().equals(type.getTypeTuple())))
        .map(Map.Entry::getValue).collect(Collectors.toList());

    for (ConcurrentHashMap<ChannelListener, ?> listenerMethods : listeners) {
      listenerMethods.remove(listener);
    }
  }
}
