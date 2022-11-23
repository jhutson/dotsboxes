package com.hutsondev.dotsboxes.core;

import lombok.Getter;

public enum Player {
  ONE(0),
  TWO(1);

  @Getter
  private final int index;

  Player(int index) {
    this.index = index;
  }
}
