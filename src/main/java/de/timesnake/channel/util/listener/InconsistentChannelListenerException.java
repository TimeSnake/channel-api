/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.listener;

public class InconsistentChannelListenerException extends RuntimeException {

    public InconsistentChannelListenerException() {

    }

    public InconsistentChannelListenerException(String message) {
        super(message);
    }
}
