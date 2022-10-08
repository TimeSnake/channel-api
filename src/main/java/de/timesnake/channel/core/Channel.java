/*
 * channel-api.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.channel.core;

import de.timesnake.channel.util.listener.*;
import de.timesnake.channel.util.message.ChannelListenerMessage;
import de.timesnake.channel.util.message.ChannelMessage;
import de.timesnake.channel.util.message.ChannelPingMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class manages all local channel listeners
 */
public abstract class Channel extends ChannelServer implements de.timesnake.channel.util.Channel {

    protected ConcurrentHashMap<Tuple<ChannelType<?>, MessageType<?>>, ConcurrentHashMap<ChannelListener,
            Set<Tuple<ChannelMessageFilter<?>, Method>>>> listeners = new ConcurrentHashMap<>();

    public Channel(Thread mainThread, String serverName, Integer serverPort, Integer proxyPort, ChannelLogger logger) {
        super(mainThread, serverName, serverPort, proxyPort, logger);
    }

    public void addListener(ChannelListener listener) {
        this.addListener(listener, null);
    }

    public void addListener(ChannelListener listener, ChannelMessageFilter<?> filter) {

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

                        if (type.getMessageClass() != null && !type.getMessageClass().equals(method.getParameterTypes()[0])) {
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

                        if (type.getChannelType().equals(ChannelType.SERVER)) {
                            if (filter != null && filter.getIdentifierFilter() != null) {
                                Collection<String> identifiers;

                                try {
                                    identifiers = (Collection<String>) filter.getIdentifierFilter();
                                } catch (ClassCastException e) {
                                    throw new InconsistentChannelListenerException("invalid filter type");
                                }

                                for (String name : identifiers) {
                                    if (this.sendListenerNames.contains(name)) {
                                        continue;
                                    }

                                    this.sendListenerNames.add(name);

                                    this.sendMessage(new ChannelListenerMessage<>(this.self,
                                            MessageType.Listener.SERVER_NAME, name));
                                }
                            } else {
                                if (this.sendListenerMessageTypeAll || this.sendListenerMessageTypes.contains(type.getMessageType())) {
                                    continue;
                                }

                                if (type.getMessageType() == null) {
                                    this.sendListenerMessageTypeAll = true;
                                } else {
                                    this.sendListenerMessageTypes.add(type.getMessageType());
                                }

                                this.sendMessage(new ChannelListenerMessage<>(this.self,
                                        MessageType.Listener.SERVER_MESSAGE_TYPE, type.getMessageType()));
                            }
                        }

                    }
                }
            }

            clazz = clazz.getSuperclass();


        } while (clazz != null && ChannelListener.class.isAssignableFrom(clazz));
    }

    public void removeListener(ChannelListener listener, ListenerType... types) {
        Collection<ConcurrentHashMap<ChannelListener, ?>> listeners =
                this.listeners.entrySet().stream().filter(t -> types.length == 0
                        || Arrays.stream(types).anyMatch(type -> t.getKey().equals(type.getTypeTuple()))).map(Map.Entry::getValue).collect(Collectors.toList());

        for (ConcurrentHashMap<ChannelListener, ?> listenerMethods : listeners) {
            listenerMethods.remove(listener);
        }
    }

    public Host getSelf() {
        return self;
    }

    public Host getProxy() {
        return proxy;
    }

    @Deprecated
    public Integer getServerPort() {
        return serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getProxyName() {
        return PROXY_NAME;
    }

    @Override
    protected void handleMessage(ChannelMessage<?, ?> msg) {

        Set<Map.Entry<ChannelListener, Set<Tuple<ChannelMessageFilter<?>, Method>>>> set =
                this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), msg.getMessageType()),
                        new ConcurrentHashMap<>()).entrySet();
        set.addAll(this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), null), new ConcurrentHashMap<>()).entrySet());
        set.addAll(this.listeners.getOrDefault(new Tuple<>(null, null), new ConcurrentHashMap<>()).entrySet());

        for (Map.Entry<ChannelListener, Set<Tuple<ChannelMessageFilter<?>, Method>>> subSet : set) {
            for (Tuple<ChannelMessageFilter<?>, Method> entry : subSet.getValue()) {
                ChannelListener listener = subSet.getKey();
                ChannelMessageFilter<?> filter = entry.getA();
                Method method = entry.getB();

                if (filter == null || filter.getIdentifierFilter() == null || filter.getIdentifierFilter().contains(msg.getIdentifier())) {
                    try {
                        method.invoke(listener, msg);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void handlePingMessage(ChannelPingMessage message) {
        this.sendMessageToProxy(new ChannelPingMessage(this.getServerName(), MessageType.Ping.PONG));
    }
}
