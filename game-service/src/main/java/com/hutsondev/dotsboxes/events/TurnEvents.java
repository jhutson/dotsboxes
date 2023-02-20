package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.service.TurnResponse;
import reactor.core.publisher.Flux;

public interface TurnEvents {
  Flux<TurnResponse> getGameTurns(String gameId);
}
