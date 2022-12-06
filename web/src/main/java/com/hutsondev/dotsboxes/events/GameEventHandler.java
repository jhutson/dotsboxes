package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.StateConverter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class GameEventHandler implements WebSocketHandler {

  @Autowired
  private GameEventPublisher gameEventPublisher;

  private Optional<String> getGameIdFromPath(String path) {
    if (path != null) {
      int lastSlash = path.lastIndexOf('/');

      if (lastSlash >= 0 && lastSlash + 1 < path.length()) {
        return Optional.of(path.substring(lastSlash + 1));
      }
    }

    return Optional.empty();
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    Optional<String> maybeGameId = getGameIdFromPath(session.getHandshakeInfo().getUri().getPath());

    if (maybeGameId.isEmpty()) {
      return session.close(CloseStatus.POLICY_VIOLATION);
    }

    return session.send(
        gameEventPublisher.getTurnEvents()
            .map(e ->
                session.binaryMessage(factory -> {
                  DataBuffer buffer = factory.allocateBuffer();
                  StateConverter.write(e, buffer.asOutputStream());
                  return buffer;
                })));
  }
}
