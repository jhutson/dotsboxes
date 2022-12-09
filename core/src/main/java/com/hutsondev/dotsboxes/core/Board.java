package com.hutsondev.dotsboxes.core;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.Getter;

public class Board implements BoardView {

  /**
   * Maximum size of row or column count.
   */
  public static final int MAX_DIMENSION = 20;

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
    this(rowCount, columnCount, null);
  }

  public Board(int rowCount, int columnCount, BoardBuilder builder) {
    if (rowCount < 1) {
      throw argumentException(ErrorMessages.ROW_COUNT_GREATER_THAN_ZERO);
    }

    if (columnCount < 1) {
      throw argumentException(ErrorMessages.COLUMN_COUNT_GREATER_THAN_ZERO);
    }

    if (rowCount > MAX_DIMENSION || columnCount > MAX_DIMENSION) {
      throw argumentException(ErrorMessages.DIMENSION_TOO_LARGE);
    }

    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.columnSpan = columnCount + 1;

    this.boxCount = rowCount * columnCount;
    boxStates = new BitSet[2];

    if (builder == null ||
        (builder.getPlayerOneBoxes() == null && builder.getPlayerTwoBoxes() == null)) {
      boxStates[0] = new BitSet(boxCount);
      boxStates[1] = new BitSet(boxCount);
    } else {
      boxStates[0] = getBitset(boxCount, builder.getPlayerOneBoxes());
      boxStates[1] = getBitset(boxCount, builder.getPlayerTwoBoxes());

      this.markedBoxCount = filledBoxCount(Player.ONE) + filledBoxCount(Player.TWO);
    }

    final int lineCount = 2 * rowCount * (columnCount + 1) + columnCount;
    lineStates = new BitSet[2];

    if (builder == null ||
        (builder.getPlayerOneLines() == null && builder.getPlayerTwoLines() == null)) {
      lineStates[0] = new BitSet(lineCount);
      lineStates[1] = new BitSet(lineCount);
    } else {
      lineStates[0] = getBitset(lineCount, builder.getPlayerOneLines());
      lineStates[1] = getBitset(lineCount, builder.getPlayerTwoLines());
    }
  }

  private static BitSet getBitset(int bitCount, BitSet existing) {
    if (existing == null) {
      return new BitSet(bitCount);

    } else {
      int existingSize = existing.size();
      if (existingSize == bitCount) {
        return existing;

      } else if (existingSize < bitCount) {
        BitSet bitset = new BitSet(bitCount);
        bitset.or(existing);
        return bitset;

      } else {
        throw argumentException(ErrorMessages.BITSET_SIZE_TOO_LARGE);
      }
    }
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

  @Override
  public byte[] getLineState(Player player) {
    return lineStates[player.getIndex()].toByteArray();
  }

  @Override
  public byte[] getBoxState(Player player) {
    return boxStates[player.getIndex()].toByteArray();
  }

  @Override
  public long[] getLineStateLongs(Player player) {
    return lineStates[player.getIndex()].toLongArray();
  }

  private int getLineIndex(int row, int column) {
    if (row < 0) {
      throw argumentException(ErrorMessages.ROW_GREATER_THAN_ZERO);
    }

    if (column < 0) {
      throw argumentException(ErrorMessages.COLUMN_GREATER_THAN_ZERO);
    }

    if (column >= columnCount + row % 2) {
      throw argumentException(ErrorMessages.COLUMN_EXCEEDS_MAXIMUM_FOR_ROW);
    }

    if (row > 2 * rowCount) {
      throw argumentException(ErrorMessages.ROW_EXCEEDS_MAXIMUM);
    }

    return row * columnSpan + column;
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

  private boolean boxOpen(int index) {
    return !boxStates[0].get(index) && !boxStates[1].get(index);
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
        if (boxOpen(boxIndex)) {
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
        if (boxOpen(boxIndex)) {
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
    int leftBox = -1;

    // check box left of line
    if (column > 0) {
      if (lineMarked(lineIndex - (columnSpan + 1)) &&
          lineMarked(lineIndex - 1) &&
          lineMarked(lineIndex) &&
          lineMarked(lineIndex + (columnSpan - 1))
      ) {
        final int boxIndex = getBoxIndex(boxRow, column - 1);
        if (boxOpen(boxIndex)) {
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
        if (boxOpen(boxIndex)) {
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

  private static IllegalArgumentException argumentException(ErrorMessages errorMessage) {
    return new IllegalArgumentException(errorMessage.getMessage());
  }
}
