package com.hutsondev.dotsboxes.core;

import java.util.List;
import java.util.Optional;

public record TurnResult(
    Player currentPlayer,
    Player lastPlayer,
    Optional<List<Integer>> filledBoxes) {

}
