package com.hutsondev.dotsboxes.core;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.Getter;

public class Board {

  @Getter
  private final int rowCount;

  @Getter
  private final int columnCount;

  private final int columnSpan;
  private final int boxCount;

  private final BitSet[] boxStates;
  private final BitSet[] lineStates;

  private int markedBoxCount;

  public Board(int rowCount, int columnCount) {
    if (rowCount < 1) {
      throw new IllegalArgumentException("Value for rowCount must be greater than zero.");
    }

    if (columnCount < 1) {
      throw new IllegalArgumentException("Value for columnCount must be greater than zero.");
    }

    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.columnSpan = columnCount + 1;

    boxCount = rowCount * columnCount;

    boxStates = new BitSet[2];
    boxStates[0] = new BitSet(boxCount);
    boxStates[1] = new BitSet(boxCount);

    final int lineCount = 2 * rowCount * columnCount + 2 * rowCount + columnCount + 1;

    lineStates = new BitSet[2];
    lineStates[0] = new BitSet(lineCount);
    lineStates[1] = new BitSet(lineCount);
  }

  public Optional<List<Integer>> markLine(int row, int column, Player player) {
    int index = getLineIndex(row, column);

    if (lineMarked(index)) {
      return Optional.empty();
    }

    lineStates[player.getIndex()].set(index);
    List<Integer> filledBoxes = checkForNewlyFilledBoxes(row, column, index);

    for (int boxIndex : filledBoxes) {
      markBox(boxIndex, player);
      boxStates[player.getIndex()].set(boxIndex);
    }

    return Optional.of(filledBoxes);
  }

  public boolean hasOpenBoxes() {
    return markedBoxCount < boxCount;
  }

  public int filledBoxCount(Player player) {
    return boxStates[player.getIndex()].cardinality();
  }

  public IntStream getMarkedLines(Player player) {
    final BitSet lineState = lineStates[player.getIndex()];
    return getSetBitIndices(lineState);
  }

  public IntStream getFilledBoxes(Player player) {
    final BitSet boxState = boxStates[player.getIndex()];
    return getSetBitIndices(boxState);
  }

  private static IntStream getSetBitIndices(BitSet bitSet) {
    return IntStream.iterate(
        bitSet.nextSetBit(0),
        index -> index >= 0,
        index -> bitSet.nextSetBit(index + 1)
    );
  }

  private int getLineIndex(int row, int column) {
    if (row < 0) {
      throw new IllegalArgumentException("Value for row must be greater than or equal to zero.");
    }

    if (column < 0) {
      throw new IllegalArgumentException("Value for column must be greater than or equal to zero.");
    }

    if (column >= columnCount + row % 2) {
      throw new IllegalArgumentException("Column exceeds maximum value for row.");
    }

    int index = row * (columnCount + 1) + column;

    if (index >= lineStates[0].size()) {
      throw new IllegalArgumentException("Value for row exceeds maximum value.");
    }

    return index;
  }

  private void markBox(int boxIndex, Player player) {
    boxStates[player.getIndex()].set(boxIndex);
    ++markedBoxCount;
  }

  private int getBoxIndex(int row, int column) {
    return row * columnCount + column;
  }

  private boolean lineMarked(int lineIndex) {
    return lineStates[0].get(lineIndex) || lineStates[1].get(lineIndex);
  }

  private boolean boxMarked(int index) {
    return boxStates[0].get(index) || boxStates[1].get(index);
  }

  private List<Integer> checkForNewlyFilledBoxes(int row, int column, int lineIndex) {
    if (row % 2 == 0) {
      // horizontal line row
      return checkForNewlyFilledBoxesFromHorizontalLine(row, column, lineIndex);
    } else {
      // vertical line row
      return checkForNewlyFilledBoxesFromVerticalLine(row, column, lineIndex);
    }
  }

  private List<Integer> checkForNewlyFilledBoxesFromHorizontalLine(int row, int column,
      int lineIndex) {
    final int boxRow = row / 2;
    int bottomBox = -1;

    // check box below line
    if (boxRow < rowCount) {
      if (lineMarked(lineIndex) &&
          lineMarked(lineIndex + columnSpan) &&
          lineMarked(lineIndex + columnSpan + 1) &&
          lineMarked(lineIndex + 2 * columnSpan)
      ) {
        final int boxIndex = getBoxIndex(boxRow, column);
        if (!boxMarked(boxIndex)) {
          bottomBox = boxIndex;
        }
      }
    }

    // check box above line
    if (row > 0) {
      if (lineMarked(lineIndex - 2 * (columnSpan)) &&
          lineMarked(lineIndex - columnSpan) &&
          lineMarked(lineIndex - (columnSpan - 1)) &&
          lineMarked(lineIndex)
      ) {
        final int boxIndex = getBoxIndex(boxRow - 1, column);
        if (!boxMarked(boxIndex)) {
          if (bottomBox == -1) {
            return List.of(boxIndex);
          } else {
            return List.of(boxIndex, bottomBox);
          }
        }
      }
    }

    return bottomBox == -1 ? Collections.emptyList() : List.of(bottomBox);
  }

  private List<Integer> checkForNewlyFilledBoxesFromVerticalLine(int row, int column,
      int lineIndex) {
    final int boxRow = (row - 1) / 2;
    final int columnSpan = columnCount + 1;
    int leftBox = -1;

    // check box left of line
    if (column > 0) {
      if (lineMarked(lineIndex - (columnSpan + 1)) &&
          lineMarked(lineIndex - 1) &&
          lineMarked(lineIndex) &&
          lineMarked(lineIndex + (columnSpan - 1))
      ) {
        final int boxIndex = getBoxIndex(boxRow, column - 1);
        if (!boxMarked(boxIndex)) {
          leftBox = boxIndex;
        }
      }
    }

    // check box right of line
    if (column < columnCount) {
      if (lineMarked(lineIndex - columnSpan) &&
          lineMarked(lineIndex) &&
          lineMarked(lineIndex + 1) &&
          lineMarked(lineIndex + columnSpan)
      ) {
        final int boxIndex = getBoxIndex(boxRow, column);
        if (!boxMarked(boxIndex)) {
          if (leftBox == -1) {
            return List.of(boxIndex);
          } else {
            return List.of(leftBox, boxIndex);
          }
        }
      }
    }

    return leftBox == -1 ? Collections.emptyList() : List.of(leftBox);
  }

}
