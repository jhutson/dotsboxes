package com.hutsondev.dotsboxes.controller;

import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.TurnResult;
import com.hutsondev.dotsboxes.events.TurnEventPublisher;
import com.hutsondev.dotsboxes.model.GameSession;
import com.hutsondev.dotsboxes.proto.CreateGameRequest;
import com.hutsondev.dotsboxes.proto.CreateGameResponse;
import com.hutsondev.dotsboxes.proto.GetGameRequest;
import com.hutsondev.dotsboxes.proto.GetGameResponse;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnRequest;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import com.hutsondev.dotsboxes.repository.GameStore;
import java.security.Principal;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/game")
public class GameStateController {

  public static final String PROTOBUF_MEDIA_TYPE = "application/x-protobuf;charset=UTF-8";

  private final GameStore gameStore;
  private final TurnEventPublisher turnEventPublisher;

  public GameStateController(
      @NonNull GameStore gameStore,
      @NonNull TurnEventPublisher turnEventPublisher) {
    this.gameStore = gameStore;
    this.turnEventPublisher = turnEventPublisher;
  }

  private GameSession getCurrentGame(String gameId) {
    Optional<GameSession> gameSession = gameStore.get(gameId);
    return gameSession.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private Player validatePlayerId(GameSession gameSession, String playerId) {
    Optional<Player> player = gameSession.getPlayerIndex(playerId);
    return player.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Player ID does not represent a player in this game.")
    );
  }

  @PostMapping(value = "/create",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  CreateGameResponse createGame(@RequestBody CreateGameRequest request, Principal principal) {
    if (principal.getName().equals(request.getPlayerOneId()) ||
        principal.getName().equals(request.getPlayerTwoId())) {

      GameSession gameSession = gameStore.create(
          request.getRowCount(),
          request.getColumnCount(),
          request.getPlayerOneId(),
          request.getPlayerTwoId());

      return CreateGameResponse.newBuilder()
          .setGame(StateConverter.toGameState(gameSession.game(), gameSession.sequenceNumber()))
          .setUuid(gameSession.gameId())
          .setThisPlayer(Player.ONE.getIndex())
          .build();
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping(value = "/get",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  GetGameResponse getGame(@RequestBody GetGameRequest request, Principal principal) {
    if (!request.getPlayerId().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    GameSession gameSession = getCurrentGame(request.getUuid());
    Player player = validatePlayerId(gameSession, request.getPlayerId());

    return GetGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(gameSession.game(), gameSession.sequenceNumber()))
        .setThisPlayer(player.getIndex())
        .build();
  }

  @PostMapping(value = "/markline",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  Mono<TurnResponse> markLine(@RequestBody TurnRequest request, Principal principal) {
    if (!request.getPlayerId().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    final String gameId = request.getUuid();
    GameSession gameSession = getCurrentGame(gameId);

    if (request.getSequenceNumber() != gameSession.sequenceNumber()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    if (!gameSession.isPlayerTurn(request.getPlayerId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    TurnResult turnResult;

    try {
      turnResult = gameSession.game().markLine(request.getRow(), request.getColumn());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, e);
    }

    if (turnResult.filledBoxes().isEmpty()
        && turnResult.lastPlayer() == turnResult.currentPlayer()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    gameSession = gameStore.update(gameSession);

    TurnResponse.Builder builder = TurnResponse.newBuilder()
        .setLastPlayer(turnResult.lastPlayer().getIndex())
        .setCurrentPlayer(turnResult.currentPlayer().getIndex())
        .setLineRow(request.getRow())
        .setLineColumn(request.getColumn())
        .setSequenceNumber(gameSession.sequenceNumber());

    if (turnResult.filledBoxes().isPresent()) {
      builder.addAllFilledBoxes(turnResult.filledBoxes().get());
    }

    Optional<Outcome> maybeOutcome = gameSession.game().getOutcome();
    if (maybeOutcome.isPresent()) {
      builder.setOutcome(StateConverter.toGameOutcome(maybeOutcome.get()));
    }

    TurnResponse turnResponse = builder.build();
    return turnEventPublisher.publishTurn(gameId, turnResponse).then(Mono.just(turnResponse));
  }
}
