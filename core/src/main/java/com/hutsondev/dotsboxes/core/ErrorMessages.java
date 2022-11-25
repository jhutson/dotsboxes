package com.hutsondev.dotsboxes.core;

import java.util.ResourceBundle;

public enum ErrorMessages {
  ROW_COUNT_GREATER_THAN_ZERO("row_count_greater_than_zero"),
  COLUMN_COUNT_GREATER_THAN_ZERO("column_count_greater_than_zero"),
  ROW_GREATER_THAN_ZERO("row_greater_than_zero"),
  COLUMN_GREATER_THAN_ZERO("column_greater_than_zero"),
  COLUMN_EXCEEDS_MAXIMUM_FOR_ROW("column_exceeds_maximum_for_row"),
  ROW_EXCEEDS_MAXIMUM("row_exceeds_maximum");

  private final String key;

  ErrorMessages(String key) {
    this.key = key;
  }

  public String getMessage() {
    return ResourceBundle.getBundle("ErrorMessages").getString(key);
  }
}
