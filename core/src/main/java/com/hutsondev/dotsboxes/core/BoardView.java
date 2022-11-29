package com.hutsondev.dotsboxes.core;

import java.util.stream.IntStream;

/**
 * Read-only view of {@link Board}.
 */
public interface BoardView {

  int getRowCount();
  int getColumnCount();
  boolean hasOpenBoxes();
  int filledBoxCount(Player player);
  IntStream getMarkedLines(Player player);
  IntStream getFilledBoxes(Player player);
}
