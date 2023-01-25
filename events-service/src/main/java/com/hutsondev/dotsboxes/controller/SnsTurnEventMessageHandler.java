package com.hutsondev.dotsboxes.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.model.SnsNotification;
import com.hutsondev.dotsboxes.model.SnsSubscriptionConfirmation;
import com.hutsondev.dotsboxes.proto.TurnEvent;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.ConfirmSubscriptionRequest;

// TODO: Verify authenticity of notification call?
// via steps outlined in https://docs.aws.amazon.com/sns/latest/dg/sns-verify-signature-of-message.html.

@RestController
@RequestMapping("/events/v1/notification")
public class SnsTurnEventMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(SnsTurnEventMessageHandler.class);

  private final SnsAsyncClient snsClient;
  private final String topicArn;
  private final GameEventPublisher gameEventPublisher;

  private final Gson gson;

  public SnsTurnEventMessageHandler(
      SnsAsyncClient snsClient,
      @Value("${com.hutsondev.sns.event-topic-arn}") String topicArn,
      GameEventPublisher gameEventPublisher) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
    this.gameEventPublisher = gameEventPublisher;
    this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
  }

  @PostMapping(value = "/turn/{gameId}",
      headers = {"x-amz-sns-message-type=SubscriptionConfirmation"},
      consumes = MediaType.TEXT_PLAIN_VALUE)
  public Mono<Void> confirmSubscription(@PathVariable String gameId,
      @RequestHeader(value = "x-amz-sns-topic-arn") String requestTopicArn,
      @RequestBody String notificationString) {

    if (!topicArn.equals(requestTopicArn)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    SnsSubscriptionConfirmation notification = gson.fromJson(notificationString,
        SnsSubscriptionConfirmation.class);

    ConfirmSubscriptionRequest confirmRequest = ConfirmSubscriptionRequest.builder()
        .topicArn(notification.topicArn())
        .token(notification.token())
        .build();

    // TODO: Save mapping of gameId to SNS subscription.
    // This will be important later when unsubscribing on game end,
    // and avoiding duplicate subscriptions.
    return Mono.fromFuture(snsClient.confirmSubscription(confirmRequest))
        .doOnNext(response ->
            logger.info("Subscribed to events for game {} with ARN {}.",
                topicArn,
                response.subscriptionArn()))
        .then();
  }

  @PostMapping(value = "/turn/{gameId}",
      headers = {"x-amz-sns-message-type=Notification"},
      consumes = MediaType.TEXT_PLAIN_VALUE)
  void handleNotification(@PathVariable String gameId,
      @RequestHeader(value = "x-amz-sns-topic-arn") String requestTopicArn,
      @RequestBody String notificationString) {

    if (!topicArn.equals(requestTopicArn)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    SnsNotification notification = gson.fromJson(notificationString, SnsNotification.class);

    TurnEvent turnEvent = turnEventFromJsonString(notification.message());
    if (gameId.equals(turnEvent.getGameId())) {
      gameEventPublisher.publishTurn(turnEvent);
    }
  }

  private static TurnEvent turnEventFromJsonString(@NonNull String jsonString) {
    try {
      TurnEvent.Builder builder = TurnEvent.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(jsonString, builder);
      return builder.build();
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
