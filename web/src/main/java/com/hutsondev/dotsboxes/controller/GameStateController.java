package com.hutsondev.dotsboxes.controller;

import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.TurnResult;
import com.hutsondev.dotsboxes.events.GameEventPublisher;
import com.hutsondev.dotsboxes.proto.CreateGameRequest;
import com.hutsondev.dotsboxes.proto.CreateGameResponse;
import com.hutsondev.dotsboxes.proto.GetGameRequest;
import com.hutsondev.dotsboxes.proto.GetGameResponse;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnRequest;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/game")
public class GameStateController {

  public static final String PROTOBUF_MEDIA_TYPE = "application/x-protobuf;charset=UTF-8";

  private static Game CURRENT_GAME = null;
  private static String PLAYER_ONE_ID = null;
  private static String PLAYER_TWO_ID = null;
  private static final String GAME_ID = "A33DFDFF-A3C0-4F7F-B4B2-9664E78D111B";

  @Autowired
  private GameEventPublisher gameEventPublisher;

  private Game getCurrentGame(String gameId) {
    if (GAME_ID.equalsIgnoreCase(gameId) && CURRENT_GAME != null) {
      return CURRENT_GAME;
    }

    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }

  private boolean isPlayerTurn(Game game, String playerId) {
    if (game.getCurrentPlayer() == Player.ONE && PLAYER_ONE_ID.equals(playerId)) {
      return true;
    }
    return game.getCurrentPlayer() == Player.TWO && PLAYER_TWO_ID.equals(playerId);
  }

  private Player validatePlayerId(Game game, String playerId) {
    if (PLAYER_ONE_ID.equals(playerId)) {
      return Player.ONE;
    } else if (PLAYER_TWO_ID.equals(playerId)) {
      return Player.TWO;
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Player ID does not represent a player in this game.");
  }

  @PostMapping(value = "/create",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  CreateGameResponse createGame(@RequestBody CreateGameRequest request) {
    Game game = new Game(request.getRowCount(), request.getColumnCount());

    CURRENT_GAME = game;
    PLAYER_ONE_ID = request.getPlayerOneId();
    PLAYER_TWO_ID = request.getPlayerTwoId();

    return CreateGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(game))
        .setUuid(GAME_ID)
        .setThisPlayer(Player.ONE.getIndex())
        .build();
  }

  @PostMapping(value = "/get",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  GetGameResponse getGame(@RequestBody GetGameRequest request) {
    Game game = getCurrentGame(request.getUuid());
    Player player = validatePlayerId(game, request.getPlayerId());

    return GetGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(game))
        .setThisPlayer(player.getIndex())
        .build();
  }

  // Temporary endpoint for testing.
  @PostMapping(value = "/clear")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void clearGame() {
    CURRENT_GAME = null;
    PLAYER_ONE_ID = null;
    PLAYER_TWO_ID = null;
  }

  @PostMapping(value = "/markline",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  TurnResponse markLine(@RequestBody TurnRequest request) {
    Game game = getCurrentGame(request.getUuid());

    if (!isPlayerTurn(game, request.getPlayerId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    TurnResult turnResult;

    try {
      turnResult = game.markLine(request.getRow(), request.getColumn());
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

    Optional<Outcome> maybeOutcome = game.getOutcome();
    if (maybeOutcome.isPresent()) {
      builder.setOutcome(StateConverter.toGameOutcome(maybeOutcome.get()));
    }

    TurnResponse turnResponse = builder.build();
    gameEventPublisher.publishTurn(turnResponse);
    return turnResponse;
  }
}
