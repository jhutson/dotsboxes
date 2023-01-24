package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import java.util.Optional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Component
public class GameEventHandler implements WebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(GameEventHandler.class);

  private final TurnEvents turnEvents;

  public GameEventHandler(@NonNull TurnEvents turnEvents) {
    this.turnEvents = turnEvents;
  }

  private Optional<String> getGameIdFromPath(String path) {
    if (path != null) {
      int lastSlash = path.lastIndexOf('/');

      if (lastSlash >= 0 && lastSlash + 1 < path.length()) {
        return Optional.of(path.substring(lastSlash + 1));
      }
    }

    return Optional.empty();
  }

  private static void logSignal(String component, String sessionId, SignalType signalType) {
    logger.debug("[{}] {} publisher completed with signalType {}",
        sessionId, component, signalType);
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    final String sessionId = session.getId();
    logger.debug("[{}] handle new session", sessionId);

    Optional<String> maybeGameId = getGameIdFromPath(session.getHandshakeInfo().getUri().getPath());

    if (maybeGameId.isEmpty()) {
      logger.debug("[{}] closing session due to missing game ID", sessionId);
      return session.close(CloseStatus.POLICY_VIOLATION);
    }

    String gameId = maybeGameId.get();
    logger.debug("[{}] Game ID = {}", sessionId, gameId);

    Flux<TurnResponse> gameTurns = turnEvents.getGameTurns(gameId);

    return Mono.zip(
        session.receive().doFinally(s -> logSignal("receive", sessionId, s)).then(),
        session.send(
            gameTurns
                .map(e ->
                    session.binaryMessage(factory -> {
                      DataBuffer buffer = factory.allocateBuffer(e.getSerializedSize());
                      StateConverter.write(e, buffer.asOutputStream());
                      return buffer;
                    })
                ).doFinally(s -> logSignal("turn events", sessionId, s))
        )
    ).then();
  }
}
