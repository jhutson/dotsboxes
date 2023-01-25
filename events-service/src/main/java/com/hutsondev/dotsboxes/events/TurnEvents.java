package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnEvent;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import reactor.core.publisher.Flux;

public interface TurnEvents {
  Flux<TurnResponse> getGameTurns(String gameId);
}
