package com.hutsondev.dotsboxes.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.hutsondev.dotsboxes.core.Board;
import com.hutsondev.dotsboxes.core.BoardBuilder;
import com.hutsondev.dotsboxes.core.BoardView;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.Optional;
import lombok.NonNull;

public class StateConverter {

  private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
      .omittingInsignificantWhitespace();

  public static BoardState toBoardState(@NonNull BoardView board) {
    return BoardState.newBuilder()
        .setRowCount(board.getRowCount())
        .setColumnCount(board.getColumnCount())
        .setPlayerOneLines(ByteString.copyFrom(board.getLineState(Player.ONE)))
        .setPlayerTwoLines(ByteString.copyFrom(board.getLineState(Player.TWO)))
        .setPlayerOneBoxes(ByteString.copyFrom(board.getBoxState(Player.ONE)))
        .setPlayerTwoBoxes(ByteString.copyFrom(board.getBoxState(Player.TWO)))
        .build();
  }

  public static GameState toGameState(@NonNull Game game, int sequenceNumber) {
    GameState.Builder builder = GameState.newBuilder()
        .setCurrentPlayer(game.getCurrentPlayer().getIndex())
        .setBoard(toBoardState(game.board()))
        .setSequenceNumber(sequenceNumber);

    Optional<Outcome> maybeOutcome = game.getOutcome();
    if (maybeOutcome.isPresent()) {
      builder.setOutcome(StateConverter.toGameOutcome(maybeOutcome.get()));
    }

    return builder.build();
  }

  public static Board toBoard(@NonNull BoardState boardState) {
    return new BoardBuilder()
        .setRowCount(boardState.getRowCount())
        .setColumnCount(boardState.getColumnCount())
        .setPlayerOneLines(BitSet.valueOf(boardState.getPlayerOneLines().asReadOnlyByteBuffer()))
        .setPlayerTwoLines(BitSet.valueOf(boardState.getPlayerTwoLines().asReadOnlyByteBuffer()))
        .setPlayerOneBoxes(BitSet.valueOf(boardState.getPlayerOneBoxes().asReadOnlyByteBuffer()))
        .setPlayerTwoBoxes(BitSet.valueOf(boardState.getPlayerTwoBoxes().asReadOnlyByteBuffer()))
        .build();
  }

  public static Game toGame(@NonNull GameState gameState) {
    return new Game(
        toBoard(gameState.getBoard()),
        gameState.getCurrentPlayer() == 0 ? Player.ONE : Player.TWO
    );
  }

  public static GameOutcome toGameOutcome(@NonNull Outcome outcome) {
    return GameOutcome.newBuilder()
        .setPlayerOneScore(outcome.playerOneScore())
        .setPlayerTwoScore(outcome.playerTwoScore())
        .build();
  }

  public static void write(@NonNull Message message, @NonNull OutputStream output) {
    CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(output);

    try {
      message.writeTo(codedOutputStream);
      codedOutputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toJsonString(@NonNull MessageOrBuilder message) {
    try {
      return jsonPrinter.print(message);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
