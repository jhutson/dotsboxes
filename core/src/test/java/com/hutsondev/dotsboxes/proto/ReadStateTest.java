package com.hutsondev.dotsboxes.proto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.hutsondev.dotsboxes.core.Board;
import com.hutsondev.dotsboxes.core.BoardView;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Player;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.stream.IntStream;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

public class ReadStateTest {

  // Method that generates the state verified by readGameStateFromFile test.
  private Game getSampleGame() {
    Board board = new Board(12, 12);
    final int lineRowCount = board.getRowCount() * 2;

    for (int row = 0; row < lineRowCount; row++) {
      board.markLine(row, 0, Player.ONE);
      board.markLine(row, 1, Player.TWO);
    }

    return new Game(board);
  }

  @Test
  void readGameStateFromFile() throws IOException {
    GameState gameState;

    try (InputStream inputStream = getClass().getResourceAsStream("/game.state")) {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
      gameState = GameState.newBuilder().mergeFrom(codedInputStream).build();
    }

    assertThat(gameState).isNotNull();
    assertThat(gameState.getCurrentPlayer()).isEqualTo(0);

    BoardState boardState = gameState.getBoard();
    assertThat(boardState).isNotNull();
    assertThat(boardState.getRowCount()).isEqualTo(12);
    assertThat(boardState.getColumnCount()).isEqualTo(12);

    assertSetBits(boardState.getPlayerOneLines()).containsExactlyElementsOf(
        IntStream.range(0, 24).map(x -> x * 13)::iterator
    );

    assertSetBits(boardState.getPlayerTwoLines()).containsExactlyElementsOf(
        IntStream.range(0, 24).map(x -> x * 13 + 1)::iterator
    );

    assertSetBits(boardState.getPlayerOneBoxes()).containsExactlyElementsOf(
        IntStream.range(0, 11).map(x -> x * 12)::iterator
    );

    assertSetBits(boardState.getPlayerTwoBoxes()).isEmpty();
  }

  @Test
  void readGameStateFromFileAndConvertToGame() throws IOException {
    Game game;

    try (InputStream inputStream = getClass().getResourceAsStream("/game.state")) {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
      GameState gameState = GameState.newBuilder().mergeFrom(codedInputStream).build();
      game = StateConverter.toGame(gameState);
    }

    assertThat(game.getCurrentPlayer()).isEqualTo(Player.ONE);

    BoardView board = game.board();

    assertThat(board.getRowCount()).isEqualTo(12);
    assertThat(board.getColumnCount()).isEqualTo(12);
    assertThat(board.getMarkedLines(Player.ONE)).containsExactlyElementsOf(
        IntStream.range(0, 24).map(x -> x * 13)::iterator
    );
    assertThat(board.getMarkedLines(Player.TWO)).containsExactlyElementsOf(
        IntStream.range(0, 24).map(x -> x * 13 + 1)::iterator
    );
    assertThat(board.getFilledBoxes(Player.ONE)).containsExactlyElementsOf(
        IntStream.range(0, 11).map(x -> x * 12)::iterator
    );
    assertThat(board.getFilledBoxes(Player.TWO)).isEmpty();
  }

  private ListAssert<Integer> assertSetBits(ByteString bytes) {
    BitSet bitset = BitSet.valueOf(bytes.asReadOnlyByteBuffer());
    return assertThat(getSetBitIndices(bitset));
  }

  private static IntStream getSetBitIndices(BitSet bitSet) {
    return IntStream.iterate(
        bitSet.nextSetBit(0),
        index -> index >= 0,
        index -> bitSet.nextSetBit(index + 1)
    );
  }
}
