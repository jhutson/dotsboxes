package com.hutsondev.dotsboxes.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.NonNull;

/**
 * Iterator that merges two sorted lists into a single sequence.
 * @param <T> Type of elements.
 */
public class SortedMergedIterator<T extends Comparable<T>> implements Iterator<T> {

  private final Iterator<T> first;
  private final Iterator<T> second;
  private final List<T> headFirst = new ArrayList<>(1);
  private final List<T> headSecond = new ArrayList<>(1);

  public SortedMergedIterator(@NonNull Iterator<T> first, @NonNull Iterator<T> second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean hasNext() {
    return !(headFirst.isEmpty() && headSecond.isEmpty()) || first.hasNext() || second.hasNext();
  }

  /**
   * Ensure head is populated from iterator.
   *
   * @param iterator Supplies elements.
   * @param head Stores most recent element from iterator.
   * @return True if head is populated; otherwise false.
   */
  private boolean ensureElement(Iterator<T> iterator, List<T> head) {
    if (head.isEmpty()) {
      if (iterator.hasNext()) {
        head.add(iterator.next());
        return true;
      }
    } else {
      return true;
    }

    return false;
  }

  @Override
  public T next() {
    if (hasNext()) {
      final boolean haveFirst = ensureElement(first, headFirst);
      final boolean haveSecond = ensureElement(second, headSecond);

      if (haveFirst && haveSecond) {
        if (headFirst.get(0).compareTo(headSecond.get(0)) <= 0) {
          return headFirst.remove(0);
        } else {
          return headSecond.remove(0);
        }
      } else if (haveFirst) {
        return headFirst.remove(0);
      } else {
        return headSecond.remove(0);
      }
    }

    throw new NoSuchElementException("Iterator has no more elements.");
  }
}
