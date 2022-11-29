package com.hutsondev.dotsboxes.core;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

public class Game {

  private final Board board;

  @Getter
  private Player currentPlayer;

  public Game(@NonNull Board board, Player currentPlayer) {
    this.board = board;
    this.currentPlayer = currentPlayer;
  }

  public Game(@NonNull Board board) {
    this(board, Player.ONE);
  }

  public Game(int rowCount, int columnCount) {
    this(new Board(rowCount, columnCount));
  }

  public BoardView board() {
    return board;
  }

  public TurnResult markLine(int row, int column) {
    Player lastPlayer = currentPlayer;

    Optional<List<Integer>> filled = board.markLine(row, column, currentPlayer);

    if (filled.isPresent() && filled.get().isEmpty()) {
      currentPlayer = currentPlayer.other();
    }

    return new TurnResult(currentPlayer, lastPlayer, filled);
  }

  public Optional<Outcome> getOutcome() {
    if (board.hasOpenBoxes()) {
      return Optional.empty();
    }

    return Optional.of(new Outcome(
        board.filledBoxCount(Player.ONE),
        board.filledBoxCount(Player.TWO)
    ));
  }
}
