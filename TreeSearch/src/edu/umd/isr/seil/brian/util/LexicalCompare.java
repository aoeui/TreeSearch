package edu.umd.isr.seil.brian.util;

import java.util.Comparator;
import java.util.Iterator;

public class LexicalCompare {
  @SuppressWarnings("unchecked")
  public static <S extends Comparable<? super S>> LexicalComparator<S> comparatorInstance() {
    return LexicalComparator.INSTANCE;
  }
  
  public static class LexicalComparator<S extends Comparable<? super S>> implements Comparator<Iterable<S>> {
    @SuppressWarnings("rawtypes")
    public final static LexicalComparator INSTANCE = new LexicalComparator();
    private LexicalComparator() {}
    @Override
    public int compare(Iterable<S> o1, Iterable<S> o2) {
      return LexicalCompare.compare(o1, o2);
    }
  }
  
  public static boolean areEqual(Iterator<?> aIt, Iterator<?> bIt) {
    boolean foundDiff = false;
    while (!foundDiff && aIt.hasNext() && bIt.hasNext()) {
      Object a = aIt.next();
      Object b = bIt.next();
      if (a == null || b == null) {
        if (a != b) foundDiff = true;
      } else {
        foundDiff = !a.equals(b);
      }
    }
    if (aIt.hasNext() || bIt.hasNext()) foundDiff = true;
    return !foundDiff;
  }
  
  public static<T extends Comparable<? super T>> int compare(Iterator<T> aIt, Iterator<T> bIt) {
    int rv = 0;
    while (rv == 0 && aIt.hasNext() && bIt.hasNext()) {
      T a = aIt.next();
      T b = bIt.next();
      if (a == null || b == null) {
        if (a != b) {
          rv = a == null ? -1 : 1;
        }
      } else {
        rv = a.compareTo(b);
      }
    }
    if (rv != 0) return rv;

    if (aIt.hasNext()) {
      rv = 1;
    } else if (bIt.hasNext()) {
      rv = -1;
    }
    return rv;
  }
  
  public static<T extends Comparable<? super T>> int compare(Iterable<T> a, Iterable<T> b) {
    return LexicalCompare.<T>compare(a.iterator(), b.iterator());
  }
}
