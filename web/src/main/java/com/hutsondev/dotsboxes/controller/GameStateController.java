package com.hutsondev.dotsboxes.controller;

import com.hutsondev.dotsboxes.core.Board;
import com.hutsondev.dotsboxes.core.BoardView;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.TurnResult;
import com.hutsondev.dotsboxes.proto.CreateGameRequest;
import com.hutsondev.dotsboxes.proto.CreateGameResponse;
import com.hutsondev.dotsboxes.proto.GetGameRequest;
import com.hutsondev.dotsboxes.proto.GetGameResponse;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.TurnRequest;
import com.hutsondev.dotsboxes.proto.TurnResponse;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/game")
public class GameStateController {

  public static final String PROTOBUF_MEDIA_TYPE = "application/x-protobuf;charset=UTF-8";

  private static Game CURRENT_GAME = null;
  private static final String GAME_ID = "A33DFDFF-A3C0-4F7F-B4B2-9664E78D111B";

  private Game getCurrentGame(String gameId) {
    if (GAME_ID.equalsIgnoreCase(gameId) && CURRENT_GAME != null) {
      return CURRENT_GAME;
    }

    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }

  private Game getSampleGame() {
    Board board = new Board(12, 12);
    final int lineRowCount = board.getRowCount() * 2;

    for (int row = 0; row < lineRowCount; row++) {
      board.markLine(row, 0, Player.ONE);
      board.markLine(row, 1, Player.TWO);
    }

    return new Game(board);
  }

  @PostMapping(value = "/create",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  CreateGameResponse createGame(@RequestBody CreateGameRequest request) {
    Game game = new Game(request.getRowCount(), request.getColumnCount());

    BoardView board = game.board();
    if (board.getColumnCount() >= 4 && board.getRowCount() >= 4) {
      game.markLine(2, 1); // one
      game.markLine(3, 2); // two
      game.markLine(4, 1); // one
      game.markLine(3, 1); // two, go again, box 5 filled
    }

    CURRENT_GAME = game;

    return CreateGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(game))
        .setUuid(GAME_ID)
        .build();
  }

  @PostMapping(value = "/get",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  GetGameResponse getGame(@RequestBody GetGameRequest request) {
    Game game = getCurrentGame(request.getUuid());

    return GetGameResponse.newBuilder()
        .setGame(StateConverter.toGameState(game))
        .build();
  }

  @PostMapping(value = "/markline",
      consumes = PROTOBUF_MEDIA_TYPE,
      produces = PROTOBUF_MEDIA_TYPE)
  TurnResponse markLine(@RequestBody TurnRequest request) {
    Game game = getCurrentGame(request.getUuid());
    TurnResult turnResult;

    try {
      turnResult = game.markLine(request.getRow(), request.getColumn());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, e);
    }

    TurnResponse.Builder builder = TurnResponse.newBuilder()
        .setCanTakeTurn(turnResult.lastPlayer() == turnResult.currentPlayer());

    if (turnResult.filledBoxes().isPresent()) {
      builder.addAllFilledBoxes(turnResult.filledBoxes().get());
    }

    Optional<Outcome> maybeOutcome = game.getOutcome();
    if (maybeOutcome.isPresent()) {
      builder.setOutcome(StateConverter.toGameOutcome(maybeOutcome.get()));
    }

    return builder.build();
  }
}
