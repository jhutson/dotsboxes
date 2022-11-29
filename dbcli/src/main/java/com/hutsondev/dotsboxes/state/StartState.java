package com.hutsondev.dotsboxes.state;

import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Player;
import java.io.PrintWriter;
import java.util.List;

public class StartState implements AppInterfaceState {

  private final List<InterfaceOption> options = List.of(
      new InterfaceOption("Row Count", OptionType.Number),
      new InterfaceOption("Column Count", OptionType.Number)
  );

  @Override
  public void display(PrintWriter writer) {
    writer.println("Dots and Boxes Game");
  }

  @Override
  public AppInterfaceState processArguments(List<String> arguments) {
    int rowCount = InterfaceOption.parseNumber(arguments.get(0)).orElseThrow();
    int columnCount = InterfaceOption.parseNumber(arguments.get(1)).orElseThrow();

    Game game = new Game(rowCount, columnCount);
    return new PlayerTurnState(game, Player.ONE);
  }

  @Override
  public Iterable<InterfaceOption> options() {
    return options;
  }
}
