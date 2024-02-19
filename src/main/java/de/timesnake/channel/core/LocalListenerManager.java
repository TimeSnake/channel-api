/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.InconsistentChannelListenerException;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class LocalListenerManager {

  public final Logger logger = LogManager.getLogger("channel.local_listener");

  protected Channel manager;

  protected ConcurrentHashMap<MessageListenerData<?>, ConcurrentHashMap<ChannelListener,
      Set<Method>>> listeners = new ConcurrentHashMap<>();

  protected final ExecutorService executorService = new ThreadPoolExecutor(2, 100,
      5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

  public LocalListenerManager(Channel manager) {
    this.manager = manager;
  }

  public <Identifier extends Serializable> void invokeLocalListeners(ChannelMessage<Identifier, ?> msg) {
    Set<Map.Entry<ChannelListener, Set<Method>>> listenerSet = this.listeners.getOrDefault(
        new MessageListenerData<>(msg.getChannelType(), msg.getMessageType(), msg.getIdentifier()),
        new ConcurrentHashMap<>()).entrySet();

    listenerSet.addAll(this.listeners.getOrDefault(
        new MessageListenerData<>(msg.getChannelType(), msg.getMessageType(), null),
        new ConcurrentHashMap<>()).entrySet());

    for (Map.Entry<ChannelListener, Set<Method>> entry : listenerSet) {
      ChannelListener listener = entry.getKey();

      for (Method method : entry.getValue()) {
        method.setAccessible(true);

        if (method.getAnnotation(ChannelHandler.class).async()) {
          try {
            method.invoke(listener, msg);
          } catch (IllegalAccessException | InvocationTargetException e) {
            logger.warn("Unable to invoke listener of '{}': {}", method.getClass().getName(), e.getMessage());
          }
        } else {
          this.manager.runSync(() -> {
            try {
              method.invoke(listener, msg);
            } catch (IllegalAccessException | InvocationTargetException e) {
              logger.warn("Unable to invoke listener of '{}': {}", method.getClass().getName(), e.getMessage());
            }
          });
        }
      }
    }
  }

  public void addLocalListener(ChannelListener listener) {
    this.addLocalListener(listener, Set.of());
  }

  public <Identifier extends Serializable> void addLocalListener(ChannelListener listener,
                                                                 @NotNull Set<Identifier> identifiers) {
    this.executorService.execute(() -> this.addLocalListenerSync(listener, identifiers));
  }

  public void addLocalListenerSync(ChannelListener listener) {
    this.addLocalListenerSync(listener, Set.of());
  }

  public <Identifier extends Serializable> void addLocalListenerSync(ChannelListener listener,
                                                                     @NotNull Set<Identifier> identifiers) {

    HashMap<ChannelType<?>, List<MessageType<?>>> messageData = new HashMap<>();
    HashMap<ChannelType<?>, List<MessageType<?>>> filteredMessageData = new HashMap<>();

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

          boolean newType = false;

          if (annotation.filtered() && !identifiers.isEmpty()) {
            if (identifiers.stream().anyMatch(i -> !type.getFilterClass().isAssignableFrom(i.getClass()))) {
              throw new InconsistentChannelListenerException("invalid filter type for channel type " + type.getChannelType().getName());
            }

            ChannelType<Identifier> channelType = (ChannelType<Identifier>) type.getChannelType();

            for (Identifier identifier : identifiers) {
              MessageListenerData<?> data = new MessageListenerData<>(channelType, type.getMessageType(), identifier);
              Set<Method> methods = this.listeners.computeIfAbsent(data, k -> new ConcurrentHashMap<>())
                  .computeIfAbsent(listener, k -> ConcurrentHashMap.newKeySet());

              newType = newType || methods.isEmpty();
              methods.add(method);
            }
          } else {
            MessageListenerData<?> data = new MessageListenerData<>(type.getChannelType(), type.getMessageType(), null);
            Set<Method> methods = this.listeners.computeIfAbsent(data, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(listener, k -> ConcurrentHashMap.newKeySet());

            newType = methods.isEmpty();
            methods.add(method);
          }

          if (newType) {
            if (annotation.filtered()) {
              filteredMessageData.computeIfAbsent(type.getChannelType(), k -> new ArrayList<>()).add(type.getMessageType());
            } else {
              messageData.computeIfAbsent(type.getChannelType(), k -> new ArrayList<>()).add(type.getMessageType());
            }
          }
          logger.info("Added listener '{}' of class '{}'", type.name().toLowerCase(), clazz.getSimpleName());
        }
      }

      clazz = clazz.getSuperclass();
    } while (clazz != null && ChannelListener.class.isAssignableFrom(clazz));


    for (Map.Entry<ChannelType<?>, List<MessageType<?>>> entry : messageData.entrySet()) {
      this.manager.getSender().broadcastListener(entry.getKey(), entry.getValue(), List.of());
    }
    for (Map.Entry<ChannelType<?>, List<MessageType<?>>> entry : filteredMessageData.entrySet()) {
      this.manager.getSender().broadcastListener(entry.getKey(), entry.getValue(), identifiers);
    }
  }

  public void removeListener(ChannelListener listener) {
    this.executorService.execute(() -> this.removeListenerSync(listener));
  }

  public void removeListenerSync(ChannelListener listener) {
    Set<Map.Entry<MessageListenerData<?>, ConcurrentHashMap<ChannelListener, Set<Method>>>> entries =
        this.listeners.entrySet();

    HashMap<ChannelType<?>, List<MessageType<?>>> messageData = new HashMap<>();
    HashMap<ChannelType<?>, List<MessageType<?>>> filteredMessageData = new HashMap<>();
    Collection<Serializable> identifiers = new HashSet<>();

    for (Map.Entry<MessageListenerData<?>, ConcurrentHashMap<ChannelListener, Set<Method>>> entry : entries) {
      MessageListenerData<?> data = entry.getKey();

      entry.getValue().remove(listener);

      if (entry.getValue().isEmpty()) {
        if (data.getIdentifier() != null) {
          identifiers.add(data.getIdentifier());
          filteredMessageData.computeIfAbsent(data.getChannelType(), k -> new ArrayList<>()).add(data.getMessageType());
        } else {
          messageData.computeIfAbsent(data.getChannelType(), k -> new ArrayList<>()).add(data.getMessageType());
        }
        logger.info("Revoke listener for {}", data);
      }
    }

    for (Map.Entry<ChannelType<?>, List<MessageType<?>>> entry : messageData.entrySet()) {
      this.manager.getSender().revokeListener(entry.getKey(), entry.getValue(), List.of());
    }
    for (Map.Entry<ChannelType<?>, List<MessageType<?>>> entry : filteredMessageData.entrySet()) {
      this.manager.getSender().revokeListener(entry.getKey(), entry.getValue(), identifiers);
    }
  }
}
