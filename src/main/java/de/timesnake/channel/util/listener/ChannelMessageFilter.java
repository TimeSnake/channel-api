/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util.listener;

import java.util.Collection;

@FunctionalInterface
public interface ChannelMessageFilter<Identifier> {

    Collection<Identifier> getIdentifierFilter();
}
