package com.hutsondev.dotsboxes.state;

import java.io.PrintWriter;
import java.util.List;

public interface AppInterfaceState {

  void display(PrintWriter writer);

  AppInterfaceState processArguments(List<String> arguments);

  Iterable<InterfaceOption> options();
}
