package com.hutsondev.dotsboxes.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class BoardTest {

  private void assertMarkResultNoneFilled(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<List<Integer>> markResult) {
    assertThat(markResult).hasValue(Collections.emptyList());
  }

  @Test
  void boardInitializedCorrectly() {
    Random random = new Random();
    int expectedRowCount = random.nextInt(1, 20);
    int expectedColumnCount = random.nextInt(1, 20);

    Board board = new Board(expectedRowCount, expectedColumnCount);
    assertThat(board.getRowCount()).isEqualTo(expectedRowCount);
    assertThat(board.getColumnCount()).isEqualTo(expectedColumnCount);
    assertThat(board.hasOpenBoxes()).isTrue();
  }

  @Test
  void rowCountToSmall() {
    assertThatIllegalArgumentException().isThrownBy(
        () -> new Board(0, 1)
    ).withMessage(ErrorMessages.ROW_COUNT_GREATER_THAN_ZERO.getMessage());
  }

  @Test
  void columnCountToSmall() {
    assertThatIllegalArgumentException().isThrownBy(
        () -> new Board(1, 0)
    ).withMessage(ErrorMessages.COLUMN_COUNT_GREATER_THAN_ZERO.getMessage());
  }

  @Test
  void rowCountTooLarge() {
    assertThatIllegalArgumentException().isThrownBy(
        () -> new Board(Board.MAX_DIMENSION + 1, 1)
    ).withMessage(ErrorMessages.DIMENSION_TOO_LARGE.getMessage());
  }

  @Test
  void columnCountTooLarge() {
    assertThatIllegalArgumentException().isThrownBy(
        () -> new Board(1, Board.MAX_DIMENSION + 1)
    ).withMessage(ErrorMessages.DIMENSION_TOO_LARGE.getMessage());
  }

  private static Stream<Arguments> outOfRangeLineArgumentsProvider() {
    return Stream.of(
        arguments(-1, 0, ErrorMessages.ROW_GREATER_THAN_ZERO),
        arguments(0, -1, ErrorMessages.COLUMN_GREATER_THAN_ZERO),
        arguments(0, 1, ErrorMessages.COLUMN_EXCEEDS_MAXIMUM_FOR_ROW),
        arguments(1, 2, ErrorMessages.COLUMN_EXCEEDS_MAXIMUM_FOR_ROW),
        arguments(2, 1, ErrorMessages.COLUMN_EXCEEDS_MAXIMUM_FOR_ROW),
        arguments(3, 0, ErrorMessages.ROW_EXCEEDS_MAXIMUM)
    );
  }

  @ParameterizedTest(name = "row {0}, column {1}")
  @MethodSource("outOfRangeLineArgumentsProvider")
  void outOfRangeLineArgument(int row, int column, ErrorMessages errorMessage) {
    assertThatIllegalArgumentException().isThrownBy(
        () -> {
          Board board = new Board(1, 1);
          board.markLine(row, column, Player.ONE);
        }
    ).withMessage(errorMessage.getMessage());
  }

  @Test
  void lineMarkedOnce() {
    Board board = new Board(1, 1);
    assertMarkResultNoneFilled(board.markLine(0, 0, Player.ONE));
    assertThat(board.getMarkedLines(Player.ONE)).containsExactly(0);
    assertThat(board.getMarkedLines(Player.TWO)).isEmpty();

    assertThat(board.markLine(0, 0, Player.ONE)).isEmpty();
    assertThat(board.getMarkedLines(Player.ONE)).containsExactly(0);
    assertThat(board.getMarkedLines(Player.TWO)).isEmpty();
  }

  @Test
  void succeedToMarkVerticalLineInLastColumn() {
    Board board = new Board(1, 1);
    board.markLine(1, 1, Player.ONE);
    assertThat(board.getMarkedLines(Player.ONE)).containsExactly(3);
    assertThat(board.getMarkedLines(Player.TWO)).isEmpty();
  }

  /**
   * The last column available to vertical lines should not be accessible for horizontal ones.
   */
  @Test
  void failToMarkHorizontalLineInLastColumn() {
    Board board = new Board(1, 1);

    assertThatIllegalArgumentException().isThrownBy(
        () -> board.markLine(0, 1, Player.ONE)
    );
  }

  private static Position rc(int row, int column) {
    return new Position(row, column);
  }

  private static Stream<Arguments> makeOneBoxLineProvider() {
    Position[] ps = new Position[]{
        rc(0, 0),
        rc(1, 0),
        rc(1, 1),
        rc(2, 0)
    };

    return Stream.of(
        arguments(ps[0], ps[1], ps[2], ps[3]), // bottom last
        arguments(ps[3], ps[1], ps[2], ps[0]), // top last
        arguments(ps[0], ps[3], ps[2], ps[1]), // left last
        arguments(ps[0], ps[1], ps[3], ps[2])  // right last
    );
  }

  @ParameterizedTest
  @MethodSource("makeOneBoxLineProvider")
  void makeOneBox(Position first, Position second, Position third, Position fourth) {
    Board board = new Board(1, 1);

    assertMarkResultNoneFilled(board.markLine(first.row(), first.column(), Player.ONE));
    assertMarkResultNoneFilled(board.markLine(second.row(), second.column(), Player.TWO));
    assertMarkResultNoneFilled(board.markLine(third.row(), third.column(), Player.ONE));

    assertThat(board.markLine(fourth.row(), fourth.column(), Player.TWO)).contains(List.of(0));
    assertThat(board.getFilledBoxes(Player.TWO)).containsExactly(0);
    assertThat(board.getFilledBoxes(Player.ONE)).isEmpty();
  }

  private void makeTwoBoxes(Board board, Player player, Position... moves) {
    assertThat(moves).hasSize(7);

    Player firstPlayer = player;
    Player secondPlayer = firstPlayer.other();

    for (Position p : Arrays.copyOfRange(moves, 0, moves.length - 1)) {
      assertMarkResultNoneFilled(board.markLine(p.row(), p.column(), player));
      player = player.other();
    }

    Position lastMove = moves[moves.length - 1];
    assertThat(board.markLine(lastMove.row(), lastMove.column(), player)).contains(List.of(0, 1));
    assertThat(board.getFilledBoxes(firstPlayer)).containsExactly(0, 1);
    assertThat(board.getFilledBoxes(secondPlayer)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(Player.class)
  void makeTwoBoxesFromVerticalLine(Player player) {
    Board board = new Board(1, 2);

    makeTwoBoxes(board, player,
        rc(0, 0), rc(0, 1),
        rc(1, 0), rc(1, 2),
        rc(2, 0), rc(2, 1),
        rc(1, 1)
    );
  }

  @ParameterizedTest
  @EnumSource(Player.class)
  void makeTwoBoxesFromHorizontalLine(Player player) {
    Board board = new Board(2, 1);

    makeTwoBoxes(board, player,
        rc(0, 0),
        rc(1, 0), rc(1, 1),
        rc(3, 0), rc(3, 1),
        rc(4, 0),
        rc(2, 0)
    );
  }
}
