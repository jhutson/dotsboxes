package com.hutsondev.dotsboxes.events.impl;

import com.hutsondev.dotsboxes.events.TurnEventPublisher;
import com.hutsondev.dotsboxes.events.TurnEvents;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;

public class GameEvents implements TurnEventPublisher, TurnEvents {

  private static final Logger logger = LoggerFactory.getLogger(GameEvents.class);

  private record TurnEvent(String gameId, TurnResponse turn) {

  }

  private final Many<TurnEvent> turnSink;
  private final Flux<TurnEvent> turnEvents;

  public GameEvents() {
    this.turnSink = Sinks.many().multicast().onBackpressureBuffer();
    this.turnEvents = turnSink.asFlux().doFinally(s -> logSignal("turnSink", s)).cache(0);
  }

  private static void logSignal(String component, SignalType signalType) {
    logger.debug("{} publisher completed with signalType {}", component, signalType);
  }

  public Mono<Void> publishTurn(String gameId, TurnResponse turn) {
    turnSink.emitNext(new TurnEvent(gameId, turn), EmitFailureHandler.FAIL_FAST);
    return Mono.empty();
  }

  @Override
  public Flux<TurnResponse> getGameTurns(String gameId) {
    return turnEvents.filter(e -> gameId.equals(e.gameId())).map(e -> e.turn());
  }
}
