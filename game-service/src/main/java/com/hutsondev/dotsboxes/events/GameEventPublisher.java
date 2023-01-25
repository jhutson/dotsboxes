package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnResponse;
import reactor.core.publisher.Mono;

public interface GameEventPublisher {

  public Mono<Void> publishTurn(String gameId, TurnResponse turnResponse);
}
