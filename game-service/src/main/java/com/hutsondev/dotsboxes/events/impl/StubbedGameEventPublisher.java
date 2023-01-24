package com.hutsondev.dotsboxes.events.impl;

import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.proto.TurnResponse;

public class StubbedGameEventPublisher implements GameEventPublisher {

  @Override
  public void publishTurn(String gameId, TurnResponse turnResponse) {
    // Do nothing
  }
}
