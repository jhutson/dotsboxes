package com.hutsondev.dotsboxes.state;

import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Outcome;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.TurnResult;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public class PlayerTurnState implements AppInterfaceState {

  private final Game game;
  private final Player player;

  private static final List<InterfaceOption> options = List.of(
      new InterfaceOption("Row", OptionType.Number),
      new InterfaceOption("Column", OptionType.Number)
  );

  public PlayerTurnState(@NonNull Game game, @NonNull Player player) {
    this.game = game;
    this.player = player;
  }

  @Override
  public void display(PrintWriter writer) {
    BoardDisplay.display(writer, game.board());

    writer.println();

    if (player == Player.ONE) {
      writer.printf("Player ONE Turn (X)");
    } else {
      writer.printf("Player TWO Turn (O)");
    }
  }

  @Override
  public AppInterfaceState processArguments(List<String> arguments) {
    int row = InterfaceOption.parseNumber(arguments.get(0)).orElseThrow();
    int column = InterfaceOption.parseNumber(arguments.get(1)).orElseThrow();

    TurnResult result;

    try {
      result = game.markLine(row, column);
    } catch (IllegalArgumentException e) {
      return new ErrorState(this, e.getMessage());
    }

    Optional<Outcome> outcome = game.getOutcome();
    if (outcome.isPresent()) {
      return new GameEndState(game.board(), outcome.get());
    } else {
      if (result.currentPlayer() != result.lastPlayer()) {
        return new PlayerTurnState(game, result.currentPlayer());

      } else if (result.filledBoxes().isEmpty()) {
        return new ErrorState(this, "Line is already marked, or the position is invalid.");
      }
    }

    return this;
  }

  @Override
  public Iterable<InterfaceOption> options() {
    return options;
  }
}
