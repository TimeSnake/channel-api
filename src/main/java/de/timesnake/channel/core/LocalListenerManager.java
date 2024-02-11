/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.InconsistentChannelListenerException;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalListenerManager {

  public final Logger logger = LoggerFactory.getLogger("channel.local_listener");

  protected Channel manager;

  protected ConcurrentHashMap<MessageListenerData<?>, ConcurrentHashMap<ChannelListener,
      Set<Method>>> listeners = new ConcurrentHashMap<>();

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
    this.addLocalListener(listener, List.of());
  }

  public <Identifier extends Serializable> void addLocalListener(ChannelListener listener,
                                                                 @NotNull Collection<Identifier> identifiers) {

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
            this.manager.getSender().broadcastListener(type.getChannelType(), type.getMessageType(), identifiers);
          }
          logger.info("Added listener '{}' of class '{}'", type.name().toLowerCase(), clazz.getSimpleName());
        }
      }

      clazz = clazz.getSuperclass();
    } while (clazz != null && ChannelListener.class.isAssignableFrom(clazz));
  }

  public void removeListener(ChannelListener listener) {
    Set<Map.Entry<MessageListenerData<?>, ConcurrentHashMap<ChannelListener, Set<Method>>>> entries =
        this.listeners.entrySet();

    for (Map.Entry<MessageListenerData<?>, ConcurrentHashMap<ChannelListener, Set<Method>>> entry : entries) {
      MessageListenerData<?> data = entry.getKey();

      entry.getValue().remove(listener);

      if (entry.getValue().isEmpty()) {
        this.manager.getSender().revokeListener(data.getChannelType(), data.getMessageType(),
            data.getIdentifier() != null ? List.of(data.getIdentifier()) : List.of());
        logger.info("Revoke listener for {}", data);
      }
    }
  }
}
