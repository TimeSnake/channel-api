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

public abstract class Channel extends ChannelServer implements de.timesnake.channel.util.Channel {

    protected ConcurrentHashMap<Tuple<ChannelType<?>, MessageType<?>>, ConcurrentHashMap<ChannelListener, Tuple<ChannelMessageFilter<?>, Method>>> listeners = new ConcurrentHashMap<>();

    public Channel(Thread mainThread, Integer serverPort, Integer proxyPort) {
        super(mainThread, serverPort, proxyPort);
        this.serverPort = serverPort;
        this.proxyPort = proxyPort;
    }

    public void addListener(ChannelListener listener) {
        this.addListener(listener, null);
    }

    public void addListener(ChannelListener listener, ChannelMessageFilter<?> filter) {

        for (Method method : listener.getClass().getDeclaredMethods()) {
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

                    ConcurrentHashMap<ChannelListener, Tuple<ChannelMessageFilter<?>, Method>> listenerMethods = this.listeners.computeIfAbsent(type.getTypeTuple(), k -> new ConcurrentHashMap<>());

                    if (annotation.filtered() && filter != null) {
                        listenerMethods.put(listener, new Tuple<>(filter, method));
                    } else {
                        listenerMethods.put(listener, new Tuple<>(() -> null, method));
                    }

                    if (type.getChannelType().equals(ChannelType.SERVER)) {
                        if (filter != null && filter.getIdentifierFilter() != null) {
                            Collection<Integer> identifiers;

                            try {
                                identifiers = (Collection<Integer>) filter.getIdentifierFilter();
                            } catch (ClassCastException e) {
                                throw new InconsistentChannelListenerException("invalid filter type");
                            }

                            for (Integer port : identifiers) {
                                if (this.sendListenerPorts.contains(port)) {
                                    continue;
                                }

                                this.sendListenerPorts.add(port);

                                this.sendMessage(new ChannelListenerMessage<>(this.serverPort, MessageType.Listener.SERVER_PORT, port));
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

                            this.sendMessage(new ChannelListenerMessage<>(this.serverPort, MessageType.Listener.SERVER_MESSAGE_TYPE, type.getMessageType()));
                        }
                    }

                }
            }
        }
    }

    public void removeListener(ChannelListener listener, ListenerType... types) {
        Collection<ConcurrentHashMap<ChannelListener, ?>> listeners = this.listeners.entrySet().stream().filter(t ->
                types.length == 0 || Arrays.stream(types).anyMatch(type ->
                        t.getKey().equals(type.getTypeTuple()))).map(Map.Entry::getValue).collect(Collectors.toList());

        for (ConcurrentHashMap<ChannelListener, ?> listenerMethods : listeners) {
            listenerMethods.remove(listener);
        }
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    @Override
    protected void handleMessage(ChannelMessage<?, ?> msg) {

        Set<Map.Entry<ChannelListener, Tuple<ChannelMessageFilter<?>, Method>>> set = this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), msg.getMessageType()), new ConcurrentHashMap<>()).entrySet();
        set.addAll(this.listeners.getOrDefault(new Tuple<>(msg.getChannelType(), null), new ConcurrentHashMap<>()).entrySet());
        set.addAll(this.listeners.getOrDefault(new Tuple<>(null, null), new ConcurrentHashMap<>()).entrySet());

        for (Map.Entry<ChannelListener, Tuple<ChannelMessageFilter<?>, Method>> entry : set) {
            ChannelListener listener = entry.getKey();
            ChannelMessageFilter<?> filter = entry.getValue().getA();
            Method method = entry.getValue().getB();
            
            if (filter == null || filter.getIdentifierFilter() == null || filter.getIdentifierFilter().contains(msg.getIdentifier())) {
                try {
                    method.invoke(listener, msg);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void handlePingMessage(ChannelPingMessage message) {
        this.sendMessageToProxy(new ChannelPingMessage(this.serverPort, MessageType.Ping.PONG));
    }
}
