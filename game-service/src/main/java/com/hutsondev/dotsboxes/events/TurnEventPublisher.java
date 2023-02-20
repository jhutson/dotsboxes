package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.service.TurnResponse;
import reactor.core.publisher.Mono;

public interface TurnEventPublisher {

  public Mono<Void> publishTurn(String gameId, TurnResponse turnResponse);
}
