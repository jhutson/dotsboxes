package com.hutsondev.dotsboxes.controller;

import com.hutsondev.dotsboxes.core.Board;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.proto.StateConverter;
import com.hutsondev.dotsboxes.proto.GameState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameStateController {

  public static final String PROTOBUF_MEDIA_TYPE = "application/x-protobuf;charset=UTF-8";

  private Game getSampleGame() {
    Board board = new Board(12, 12);
    final int lineRowCount = board.getRowCount() * 2;

    for (int row = 0; row < lineRowCount; row++) {
      board.markLine(row, 0, Player.ONE);
      board.markLine(row, 1, Player.TWO);
    }

    return new Game(board);
  }

  @GetMapping(value="/game/sample", produces = PROTOBUF_MEDIA_TYPE)
  GameState getGameState() {
    return StateConverter.toGameState(getSampleGame());
  }
}
