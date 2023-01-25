package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnEvent;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;

public class GameEventPublisher implements TurnEvents {

  private static final Logger logger = LoggerFactory.getLogger(GameEventPublisher.class);

  private final Many<TurnEvent> turnSink;
  private final Flux<TurnEvent> turnEvents;

  public GameEventPublisher() {
    this.turnSink = Sinks.many().multicast().onBackpressureBuffer();
    this.turnEvents = turnSink.asFlux().doFinally(s -> logSignal("turnSink", s)).cache(0);
  }

  private static void logSignal(String component, SignalType signalType) {
    logger.debug("{} publisher completed with signalType {}", component, signalType);
  }

  public void publishTurn(TurnEvent turnEvent) {
    turnSink.emitNext(turnEvent, EmitFailureHandler.FAIL_FAST);
  }

  @Override
  public Flux<TurnResponse> getGameTurns(String gameId) {
    return turnEvents.filter(e -> gameId.equals(e.getGameId())).map(e -> e.getTurn());
  }
}
