package com.hutsondev.dotsboxes.core;

import java.util.BitSet;
import lombok.Getter;

@Getter
public class BoardBuilder {

  private int rowCount;
  private int columnCount;
  private BitSet playerOneBoxes;
  private BitSet playerTwoBoxes;
  private BitSet playerOneLines;
  private BitSet playerTwoLines;

  public BoardBuilder setRowCount(int rowCount) {
    this.rowCount = rowCount;
    return this;
  }

  public BoardBuilder setColumnCount(int columnCount) {
    this.columnCount = columnCount;
    return this;
  }

  public BoardBuilder setPlayerOneBoxes(BitSet playerOneBoxes) {
    this.playerOneBoxes = playerOneBoxes;
    return this;
  }

  public BoardBuilder setPlayerTwoBoxes(BitSet playerTwoBoxes) {
    this.playerTwoBoxes = playerTwoBoxes;
    return this;
  }

  public BoardBuilder setPlayerOneLines(BitSet playerOneLines) {
    this.playerOneLines = playerOneLines;
    return this;
  }

  public BoardBuilder setPlayerTwoLines(BitSet playerTwoLines) {
    this.playerTwoLines = playerTwoLines;
    return this;
  }

  public Board build() {
    return new Board(rowCount, columnCount, this);
  }
}
