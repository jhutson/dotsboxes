package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;

public class GameEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(GameEventPublisher.class);

  // TODO: Change to unicast and publish source with auto-connect. Store reference to composed Flux.
  private final Many<TurnResponse> turnSink = Sinks.many().multicast().onBackpressureBuffer();

  private static void logSignal(String component, SignalType signalType) {
    logger.debug("{} publisher completed with signalType {}", component, signalType);
  }

  public Flux<TurnResponse> getTurnEvents() {
    return turnSink.asFlux().doFinally(s -> logSignal("turnSink", s)).cache(0);
  }

  public void publishTurn(TurnResponse turnResponse) {
    turnSink.emitNext(turnResponse, EmitFailureHandler.FAIL_FAST);
  }
}
