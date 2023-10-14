/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.channel.util.message;

import java.io.Serializable;

public class VoidMessage implements Serializable {

  private VoidMessage() {

  }

  @Override
  public String toString() {
    return "void";
  }
}
