package com.hutsondev.dotsboxes.state;

import java.util.Optional;

public record InterfaceOption(String prompt, OptionType type) {

  public static Optional<Integer> parseNumber(String input) {
    try {
      return Optional.of(Integer.parseUnsignedInt(input));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public static Optional<Boolean> parseYesNo(String input) {
    if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
      return Optional.of(Boolean.TRUE);
    } else if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
      return Optional.of(Boolean.FALSE);
    } else {
      return Optional.empty();
    }
  }
}
