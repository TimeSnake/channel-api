/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import java.io.Serializable;
import java.util.function.Predicate;

@FunctionalInterface
public interface FilterMessage<T> extends Predicate<T>, Serializable {

}
