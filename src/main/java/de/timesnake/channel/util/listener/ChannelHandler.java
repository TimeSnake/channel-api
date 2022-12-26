/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.channel.util.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChannelHandler {

    ListenerType[] type();

    boolean filtered() default false;

}
