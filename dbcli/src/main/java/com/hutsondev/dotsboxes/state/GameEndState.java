package com.hutsondev.dotsboxes.state;

import com.hutsondev.dotsboxes.core.BoardView;
import com.hutsondev.dotsboxes.core.Outcome;
import java.io.PrintWriter;
import java.util.List;
import lombok.NonNull;

public class GameEndState implements AppInterfaceState {

  private final BoardView board;
  private final Outcome outcome;

  private static final List<InterfaceOption> options = List.of(
      new InterfaceOption("Play again", OptionType.YesNo)
  );

  public GameEndState(@NonNull BoardView board, @NonNull Outcome outcome) {
    this.board = board;
    this.outcome = outcome;
  }

  @Override
  public void display(PrintWriter writer) {
    BoardDisplay.display(writer, board);
    writer.println();

    writer.printf("Player ONE score: %d", outcome.playerOneScore()).println();
    writer.printf("Player TWO score: %d", outcome.playerTwoScore()).println();
    writer.println();

    if (outcome.playerOneScore() == outcome.playerTwoScore()) {
      writer.println("It's a TIE!!!");
    } else {
      String winnerName = outcome.playerOneScore() > outcome.playerTwoScore() ? "ONE" : "TWO";
      writer.printf("Player %s WINS!!!", winnerName).println();
    }
  }

  @Override
  public AppInterfaceState processArguments(List<String> arguments) {
    boolean playAgain = InterfaceOption.parseYesNo(arguments.get(0)).orElseThrow();
    return playAgain ? new StartState() : null;
  }

  @Override
  public Iterable<InterfaceOption> options() {
    return options;
  }
}
