package com.hutsondev.dotsboxes.state;

import java.io.PrintWriter;
import java.util.List;
import lombok.NonNull;

public class ErrorState implements AppInterfaceState {

  private final AppInterfaceState innerState;
  private final String errorMessage;
  private boolean displayed = false;

  public ErrorState(@NonNull AppInterfaceState innerState, @NonNull String errorMessage) {
    this.innerState = innerState;
    this.errorMessage = errorMessage;
  }

  @Override
  public void display(PrintWriter writer) {
    if (!displayed) {
      displayed = true;
      writer.println(errorMessage);
      writer.println();
    }

    innerState.display(writer);
  }

  @Override
  public AppInterfaceState processArguments(List<String> arguments) {
    return innerState.processArguments(arguments);
  }

  @Override
  public Iterable<InterfaceOption> options() {
    return innerState.options();
  }
}
