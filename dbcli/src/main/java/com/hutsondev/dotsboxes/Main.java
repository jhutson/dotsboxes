package com.hutsondev.dotsboxes;

import com.hutsondev.dotsboxes.state.AppInterfaceState;
import com.hutsondev.dotsboxes.state.InterfaceOption;
import com.hutsondev.dotsboxes.state.OptionType;
import com.hutsondev.dotsboxes.state.StartState;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

  Main() {
  }

  public static void main(String[] args) {
    new Main().run();
  }

  public void run() {
    PrintWriter writer = new PrintWriter(System.out, false);
    AppInterfaceState state = new StartState();

    while (state != null) {
      state = runState(state, writer);
    }
  }

  private AppInterfaceState runState(AppInterfaceState state, PrintWriter writer) {
    state.display(writer);
    writer.println();
    writer.flush();

    List<String> arguments = new ArrayList<>();

    for (InterfaceOption option : state.options()) {
      String argument = null;

      while (argument == null) {
        String input = System.console().readLine("%s? ", option.prompt());
        if (input.length() > 0) {
          if ("q".equalsIgnoreCase(input)) {
            return null;
          }

          if (option.type() == OptionType.Number) {
            if (InterfaceOption.parseNumber(input).isPresent()) {
              argument = input;
            } else {
              writer.println("ERROR: Not a valid number.");
            }
          } else if (option.type() == OptionType.YesNo) {
            if (InterfaceOption.parseYesNo(input).isPresent()) {
              argument = input;
            } else {
              writer.println("ERROR: Must answer Yes or No.");
            }
          }
        }
      }
      arguments.add(argument);
    }

    return state.processArguments(arguments);
  }
}