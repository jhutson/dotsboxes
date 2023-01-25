package com.hutsondev.dotsboxes.events.impl;

import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnEvent;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import java.util.Map;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class SnsGameEventPublisher implements GameEventPublisher {

  private static final String ATTRIBUTE_GAME_ID = "game-id";

  private final SnsAsyncClient snsClient;
  private final String topicArn;

  public SnsGameEventPublisher(
      @NonNull SnsAsyncClient snsClient,
      @Value("${com.hutsondev.sns.event-topic-arn}") String topicArn) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
  }

  private PublishRequest createRequest(String gameId, TurnResponse turnResponse) {
    String turnEventBody = StateConverter.toJsonString(
        TurnEvent.newBuilder()
            .setGameId(gameId)
            .setTurn(turnResponse));

    return PublishRequest.builder()
        .topicArn(topicArn)
        .message(turnEventBody)
        .build();
  }

  @Override
  public Mono<Void> publishTurn(String gameId, TurnResponse turnResponse) {
    return Mono.fromFuture(snsClient.publish(createRequest(gameId, turnResponse))).then();
  }
}
