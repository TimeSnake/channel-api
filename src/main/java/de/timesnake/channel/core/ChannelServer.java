/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.*;
import de.timesnake.channel.util.message.*;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Tuple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

  protected final ChannelBasis manager;

  protected ConcurrentHashMap<Tuple<ChannelType<?>, MessageType<?>>, ConcurrentHashMap<ChannelListener,
      Set<Tuple<ChannelMessageFilter<?>, Method>>>> listeners = new ConcurrentHashMap<>();

  protected ChannelServer(ChannelBasis manager) {
    this.manager = manager;
  }

  @Override
  public void run() {
    try {
      this.startServer();
    } catch (Exception e) {
      Loggers.CHANNEL.severe("Error while starting channel-server");
    }
  }

  @SuppressWarnings("resource")
  private void startServer() throws Exception {
    ServerSocket serverSocket = new ServerSocket(this.manager.getSelf().getPort(), 100,
        InetAddress.getByName(this.manager.getListenHostName()));

    while (true) {
      final Socket activeSocket = serverSocket.accept();
      Runnable runnable = () -> handleMessage(activeSocket);
      new Thread(runnable).start();
    }
  }

  private void handleMessage(Socket socket) {
    try {
      BufferedReader socketReader;
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      Host sender = new Host(socket.getInetAddress().getHostName(), socket.getPort());

      String inMsg;
      while ((inMsg = socketReader.readLine()) != null) {
        Loggers.CHANNEL.info("Message received: " + inMsg);
        String[] args = inMsg.split(ChannelMessage.DIVIDER, 4);

        ChannelType<?> type = ChannelType.valueOf(args[0]);
        if (ChannelType.LISTENER.equals(type)) {
          ChannelListenerMessage<?> msg = new ChannelListenerMessage<>(args);
          if (msg.getMessageType().equals(MessageType.Listener.CLOSE_SOCKET)) {
            socketReader.close();
            socket.close();
          }
        }
        this.handleMessage(args);

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handleMessage(String[] args) {
    ChannelType<?> type = ChannelType.valueOf(args[0]);

    if (ChannelType.HEARTBEAT.equals(type)) {
      this.handleHeartBeatMessage(new ChannelHeartbeatMessage<>(args));
      return;
    }

    if (ChannelType.LISTENER.equals(type)) {
      ChannelListenerMessage<?> msg = new ChannelListenerMessage<>(args);
      this.handleRemoteListenerMessage(msg);
      return;
    }

    this.runSync(() -> {
      ChannelMessage<?, ?> msg = null;

      if (ChannelType.SERVER.equals(type)) {
        msg = new ChannelServerMessage<>(args);
      } else if (ChannelType.USER.equals(type)) {
        msg = new ChannelUserMessage<>(args);
      } else if (ChannelType.GROUP.equals(type)) {
        msg = new ChannelGroupMessage<>(args);
      } else if (ChannelType.SUPPORT.equals(type)) {
        msg = new ChannelSupportMessage<>(args);
      } else if (ChannelType.DISCORD.equals(type)) {
        msg = new ChannelDiscordMessage<>(args);
      } else {
        Loggers.CHANNEL.warning("Error while reading channel type: '" + args[0] + "'");
      }

      if (msg != null) {
        this.handleMessage(msg);
      }
    });
  }

  public void handleMessage(ChannelMessage<?, ?> msg) {
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
          try {
            method.invoke(listener, msg);
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
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

  }

  public void addLocalListener(ChannelListener listener) {
    this.addLocalListener(listener, null);
  }

  public void addLocalListener(ChannelListener listener, ChannelMessageFilter<?> filter) {

    Class<?> clazz = listener.getClass();

    do {
      for (Method method : clazz.getDeclaredMethods()) {

        if (method.isAnnotationPresent(ChannelHandler.class)) {

          if (method.getParameters().length != 1) {
            throw new InconsistentChannelListenerException("invalid parameter size");
          }

          ChannelHandler annotation = method.getAnnotation(ChannelHandler.class);
          ListenerType[] methodTypes = annotation.type();
          for (ListenerType type : methodTypes) {

            if (type.getMessageClass() != null
                && !type.getMessageClass().equals(method.getParameterTypes()[0])) {
              throw new InconsistentChannelListenerException("invalid message type");
            }

            Set<Tuple<ChannelMessageFilter<?>, Method>> listenerMethods =
                this.listeners.computeIfAbsent(type.getTypeTuple(), k ->
                    new ConcurrentHashMap<>()).computeIfAbsent(listener,
                    k -> ConcurrentHashMap.newKeySet());

            if (annotation.filtered() && filter != null) {
              listenerMethods.add(new Tuple<>(filter, method));
            } else {
              listenerMethods.add(new Tuple<>(() -> null, method));
            }

            this.manager.sendListenerMessage(type, filter);

          }
        }
      }

      clazz = clazz.getSuperclass();


    } while (clazz != null && ChannelListener.class.isAssignableFrom(clazz));
  }

  public void removeListener(ChannelListener listener, ListenerType... types) {
    Collection<ConcurrentHashMap<ChannelListener, ?>> listeners =
        this.listeners.entrySet().stream().filter(t -> types.length == 0
                || Arrays.stream(types)
                .anyMatch(type -> t.getKey().equals(type.getTypeTuple())))
            .map(Map.Entry::getValue).collect(Collectors.toList());

    for (ConcurrentHashMap<ChannelListener, ?> listenerMethods : listeners) {
      listenerMethods.remove(listener);
    }
  }
}
