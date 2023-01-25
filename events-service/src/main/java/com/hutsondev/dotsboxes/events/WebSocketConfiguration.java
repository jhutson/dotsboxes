package com.hutsondev.dotsboxes.events;

import java.util.Map;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

@Configuration
public class WebSocketConfiguration {

  private final WebSocketHandler gameEventHandler;

  public WebSocketConfiguration(
      @NonNull GameEventHandler gameEventHandler,
      @NonNull SnsAsyncClient snsClient,
      @Value("${com.hutsondev.sns.event-topic-arn}") String topicArn,
      @Value("${com.hutsondev.sns.subscription-base-uri}") String subscriptionBaseUri) {
    this.gameEventHandler = new SnsTurnEventSubscriber(snsClient, topicArn, subscriptionBaseUri, gameEventHandler);
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
