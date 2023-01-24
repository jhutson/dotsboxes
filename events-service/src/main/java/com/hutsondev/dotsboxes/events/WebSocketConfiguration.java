package com.hutsondev.dotsboxes.events;

import java.util.Map;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration
public class WebSocketConfiguration {

  private final GameEventHandler gameEventHandler;

  public WebSocketConfiguration(@NonNull GameEventHandler gameEventHandler) {
    this.gameEventHandler = gameEventHandler;
  }

  @Bean
  HandlerMapping webSocketHandlerMapping() {
    Map<String, WebSocketHandler> map = Map.of("/events/v1/game/**", gameEventHandler);

    SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
    handlerMapping.setOrder(1);
    handlerMapping.setUrlMap(map);
    return handlerMapping;
  }
}
