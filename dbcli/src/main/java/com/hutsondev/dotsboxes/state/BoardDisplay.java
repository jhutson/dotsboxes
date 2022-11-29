package com.hutsondev.dotsboxes.state;

import com.hutsondev.dotsboxes.core.BoardView;
import com.hutsondev.dotsboxes.core.Player;
import com.hutsondev.dotsboxes.core.SortedMergedIterator;
import java.io.PrintWriter;
import java.util.Iterator;
import lombok.NonNull;

public final class BoardDisplay {

  private BoardDisplay() {
  }

  public static void display(@NonNull PrintWriter writer, @NonNull BoardView board) {
    Iterator<Integer> markedLines = new SortedMergedIterator<>(
        board.getMarkedLines(Player.ONE).iterator(),
        board.getMarkedLines(Player.TWO).iterator()
    );

    Iterator<Integer> filledBoxesOne = board.getFilledBoxes(Player.ONE).iterator();
    Iterator<Integer> filledBoxesTwo = board.getFilledBoxes(Player.TWO).iterator();

    int columnCount = board.getColumnCount();
    int columnSpan = columnCount + 1;
    int markedLine = markedLines.hasNext() ? markedLines.next() : -1;

    int[] filledBox = new int[2];
    filledBox[0] = filledBoxesOne.hasNext() ? filledBoxesOne.next() : -1;
    filledBox[1] = filledBoxesTwo.hasNext() ? filledBoxesTwo.next() : -1;

    final String markedHorizontal = "+---";
    final String emptyHorizontal = "+   ";

    for (int row = 0; row < board.getRowCount() * 2; row++) {
      if (row % 2 == 0) { // horizontal
        for (int column = 0; column < columnSpan; column++) {
          int line = row * columnSpan + column;

          if (line == markedLine) {
            markedLine = markedLines.hasNext() ? markedLines.next() : -1;
            writer.print(markedHorizontal);
          } else {
            writer.print(emptyHorizontal);
          }
        }

      } else { // vertical
        int boxRow = row / 2;

        for (int column = 0; column < columnSpan; column++) {
          int line = row * columnSpan + column;

          if (line == markedLine) {
            markedLine = markedLines.hasNext() ? markedLines.next() : -1;
            writer.print("| ");
          } else {
            writer.print("  ");
          }

          if (column < columnCount) {
            int box = boxRow * columnCount + column;
            if (box == filledBox[0]) {
              filledBox[0] = filledBoxesOne.hasNext() ? filledBoxesOne.next() : -1;
              writer.print('X');
            } else if (box == filledBox[1]) {
              filledBox[1] = filledBoxesTwo.hasNext() ? filledBoxesTwo.next() : -1;
              writer.print('O');
            } else {
              writer.print(' ');
            }
            writer.print(' ');
          } else {
            writer.print("  ");
          }

        }
      }
      writer.println();
    }

    // last horizontal row
    for (int column = 0; column < columnSpan; column++) {
      int line = (board.getRowCount() * 2) * columnSpan + column;

      if (line == markedLine) {
        markedLine = markedLines.hasNext() ? markedLines.next() : -1;
        writer.print(markedHorizontal);
      } else {
        writer.print(emptyHorizontal);
      }
    }
    writer.println();
  }
}
