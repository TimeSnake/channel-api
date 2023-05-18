/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.core;

public class UnknownTypeException extends RuntimeException {

    public UnknownTypeException() {
    }

    public UnknownTypeException(String message) {
        super(message);
    }
}
