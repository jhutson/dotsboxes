package com.hutsondev.dotsboxes.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GameTest {

  private void assertValidMoveWithNoFilledBoxes(TurnResult result, Player current) {
    assertThat(result)
        .usingRecursiveComparison()
        .isEqualTo(
            new TurnResult(current.other(), current, Optional.of(Collections.emptyList()))
        );
  }

  @Test
  void markLineOne() {
    Game game = new Game(1, 1);
    assertThat(game.getCurrentPlayer()).isEqualTo(Player.ONE);
    assertValidMoveWithNoFilledBoxes(game.markLine(0, 0), Player.ONE);
    assertThat(game.getCurrentPlayer()).isEqualTo(Player.TWO);
  }

  @Test
  void markLineTwiceNoPlayerChange() {
    Game game = new Game(1, 1);
    assertValidMoveWithNoFilledBoxes(game.markLine(0, 0), Player.ONE);

    assertThat(game.markLine(0, 0))
        .usingRecursiveComparison()
        .isEqualTo(
            new TurnResult(Player.TWO, Player.TWO, Optional.empty())
        );
    assertThat(game.getCurrentPlayer()).isEqualTo(Player.TWO);
  }

  @Test
  void markLineForEachPlayer() {
    Game game = new Game(1, 1);
    assertValidMoveWithNoFilledBoxes(game.markLine(0, 0), Player.ONE);
    assertValidMoveWithNoFilledBoxes(game.markLine(1, 0), Player.TWO);
    assertThat(game.getCurrentPlayer()).isEqualTo(Player.ONE);
  }

  @Test
  void markLineBoxFilledGoesAgain() {
    Board board = new Board(1, 2);
    board.markLine(0, 0, Player.ONE);
    board.markLine(1, 0, Player.TWO);
    board.markLine(1, 1, Player.ONE);

    Game game = new Game(board, Player.TWO);

    assertThat(game.markLine(2, 0))
        .usingRecursiveComparison()
        .isEqualTo(
            new TurnResult(Player.TWO, Player.TWO, Optional.of(List.of(0)))
        );
    assertThat(game.getCurrentPlayer()).isEqualTo(Player.TWO);
  }

  @Test
  void outcomeWithOpenBoxes() {
    Game game = new Game(1, 1);
    assertThat(game.getOutcome()).isEmpty();
  }

  @Test
  void outcomeWithWinner() {
    Board board = new Board(1, 1);
    board.markLine(0, 0, Player.ONE);
    board.markLine(1, 0, Player.TWO);
    board.markLine(1, 1, Player.ONE);
    board.markLine(2, 0, Player.TWO);

    Game game = new Game(board, Player.TWO);
    assertThat(game.getOutcome()).hasValue(new Outcome(0, 1));
  }
}
