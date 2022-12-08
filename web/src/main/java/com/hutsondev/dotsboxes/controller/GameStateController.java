package com.hutsondev.dotsboxes.controller;

import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.TurnResult;
import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.model.GameSession;
import com.hutsondev.dotsboxes.proto.CreateGameRequest;
import com.hutsondev.dotsboxes.proto.CreateGameResponse;
import com.hutsondev.dotsboxes.proto.GetGameRequest;
import com.hutsondev.dotsboxes.proto.GetGameResponse;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnRequest;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import com.hutsondev.dotsboxes.repository.GameStore;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/game")
public class GameStateController {

  public static final String PROTOBUF_MEDIA_TYPE = "application/x-protobuf;charset=UTF-8";

  @Autowired
  private GameEventPublisher gameEventPublisher;

  @Autowired
  private GameStore gameStore;

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
  CreateGameResponse createGame(@RequestBody CreateGameRequest request) {
    GameSession gameSession = gameStore.create(
        request.getRowCount(),
        request.getColumnCount(),
        request.getPlayerOneId(),
        request.getPlayerTwoId());

    return CreateGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(gameSession.game()))
        .setUuid(gameSession.gameId())
        .setThisPlayer(Player.ONE.getIndex())
        .build();
  }

  @PostMapping(value = "/get",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  GetGameResponse getGame(@RequestBody GetGameRequest request) {
    GameSession gameSession = getCurrentGame(request.getUuid());
    Player player = validatePlayerId(gameSession, request.getPlayerId());

    return GetGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(gameSession.game()))
        .setThisPlayer(player.getIndex())
        .build();
  }

  // Temporary endpoint for testing.
  @PostMapping(value = "/clear",
      consumes = {MediaType.TEXT_PLAIN_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void clearGame(@RequestBody String gameId) {
    if (!gameStore.remove(gameId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping(value = "/markline",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  TurnResponse markLine(@RequestBody TurnRequest request) {
    GameSession gameSession = getCurrentGame(request.getUuid());

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

    TurnResponse.Builder builder = TurnResponse.newBuilder()
        .setLastPlayer(turnResult.lastPlayer().getIndex())
        .setCurrentPlayer(turnResult.currentPlayer().getIndex())
        .setLineRow(request.getRow())
        .setLineColumn(request.getColumn());

    if (turnResult.filledBoxes().isPresent()) {
      builder.addAllFilledBoxes(turnResult.filledBoxes().get());
    }

    Optional<Outcome> maybeOutcome = gameSession.game().getOutcome();
    if (maybeOutcome.isPresent()) {
      builder.setOutcome(StateConverter.toGameOutcome(maybeOutcome.get()));
    }

    TurnResponse turnResponse = builder.build();
    gameEventPublisher.publishTurn(turnResponse);
    return turnResponse;
  }
}
