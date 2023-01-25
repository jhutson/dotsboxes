package com.hutsondev.dotsboxes.events;

import java.net.URI;
import java.util.Optional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

public class SnsTurnEventSubscriber implements WebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(SnsTurnEventSubscriber.class);

  private final SnsAsyncClient snsClient;
  private final String topicArn;
  private final WebSocketHandler innerHandler;
  private final URI subscriptionBaseUri;

  public SnsTurnEventSubscriber(
      @NonNull SnsAsyncClient snsClient,
      @NonNull String topicArn,
      @NonNull String subscriptionBaseUri,
      @NonNull WebSocketHandler innerHandler) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
    this.innerHandler = innerHandler;
    this.subscriptionBaseUri = URI.create(subscriptionBaseUri);
  }

  private String getSubscriptionUri(String gameId) {
    return subscriptionBaseUri.resolve("/events/v1/notification/turn/" + gameId).toString();
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

  private Mono<Void> subscribeToGameEvents(String endpointUri) {
    return Mono.fromFuture(
            snsClient.subscribe(
                SubscribeRequest.builder()
                    .topicArn(topicArn)
                    .protocol(subscriptionBaseUri.getScheme())
                    .endpoint(endpointUri)
                    .build()))
        .doOnNext(r -> logger.info("Created game event subscription using endpoint {}", endpointUri))
        .then();
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    Optional<String> maybeGameId = getGameIdFromPath(session.getHandshakeInfo().getUri().getPath());

    if (maybeGameId.isPresent()) {
      String gameId = maybeGameId.get();
      String endpointUri = getSubscriptionUri(gameId);
      String scheme = subscriptionBaseUri.getScheme();

      ListSubscriptionsByTopicRequest listRequest =
          ListSubscriptionsByTopicRequest.builder().topicArn(topicArn).build();

      Mono<Void> subscribe = Flux.from(snsClient.listSubscriptionsByTopicPaginator(listRequest))
          .flatMapIterable(r -> r.subscriptions())
          .filter(s ->
              scheme.equals(s.protocol()) &&
                  endpointUri.equals(s.endpoint()))
          .hasElements()
          .flatMap(hasSubscription ->
              hasSubscription ? Mono.empty().then() : subscribeToGameEvents(endpointUri)
          );

      return subscribe.then(innerHandler.handle(session));
    } else {
      return innerHandler.handle(session);
    }
  }
}
