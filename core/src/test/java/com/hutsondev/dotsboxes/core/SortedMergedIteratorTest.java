package com.hutsondev.dotsboxes.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SortedMergedIteratorTest {

  @Test
  void mergeSingleElementIterators() {
    Iterator<Integer> iterator = new SortedMergedIterator<>(
        List.of(1).iterator(),
        List.of(2).iterator());

    assertThat(iterator).toIterable().containsExactly(1, 2);
  }
}
