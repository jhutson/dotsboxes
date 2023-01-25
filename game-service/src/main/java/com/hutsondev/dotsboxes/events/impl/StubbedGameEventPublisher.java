package com.hutsondev.dotsboxes.events.impl;

import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import reactor.core.publisher.Mono;

public class StubbedGameEventPublisher implements GameEventPublisher {

  @Override
  public Mono<Void> publishTurn(String gameId, TurnResponse turnResponse) {
    // Do nothing
    return Mono.empty().then();
  }
}
