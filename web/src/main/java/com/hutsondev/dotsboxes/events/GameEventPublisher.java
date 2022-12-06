package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;

public class GameEventPublisher {

  private final Many<TurnResponse> turnSink = Sinks.many().multicast().onBackpressureBuffer();

  public Flux<TurnResponse> getTurnEvents() {
    return turnSink.asFlux();
  }

  public void publishTurn(TurnResponse turnResponse) {
    turnSink.emitNext(turnResponse, EmitFailureHandler.FAIL_FAST);
  }
}
