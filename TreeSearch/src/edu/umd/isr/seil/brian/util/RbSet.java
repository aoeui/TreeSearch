package edu.umd.isr.seil.brian.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RbSet<K extends Comparable<? super K>> implements Iterable<K> {
  final RbTree<K, Void> tree;

  private RbSet(RbTree<K,Void> tree) {
    this.tree = tree;
  }

  public static <K extends Comparable<? super K>> RbSet<K> getEmpty() {
    return new RbSet<K>(RbTree.<K,Void>getEmpty());
  }
  public static <K extends Comparable<? super K>> RbSet<K> empty() {
    return new RbSet<K>(RbTree.<K,Void>getEmpty());
  }

  public int size() { return tree.size(); }

  public boolean isEmpty() { return tree.isEmpty(); }

  public RbSet<K> add(K elt) {
    return new RbSet<K>(tree.put(elt, null));
  }

  public RbSet<K> remove(K elt) {
    RbTree<K,Void> wrapped = tree.remove(elt);
    if (wrapped == tree) return this;

    return new RbSet<K>(wrapped);
  }

  public boolean contains(K elt) { return tree.containsKey(elt); }

  // Standard iterators implemented on top of RbTreeIterators
  public Iterator<K> iterator() {
    return new IteratorAdapter<K>(tree.iterator());
  }

  public RbSet<K> union(RbSet<K> set) {
    return addAll(set);
  }
  public RbSet<K> addAll(RbSet<K> set) {
    RbTree<K,Void> newTree = tree;

    for (K val : set) {
      newTree = newTree.put(val, null);
    }
    return new RbSet<K>(newTree);
  }

  public RbSet<K> intersect(RbSet<? extends K> set) {
    RbSet<K> rv = RbSet.<K>getEmpty();

    for (K val : set) {
      if (contains(val)) {
        rv = rv.add(val);
      }
    }
    return rv;
  }
  
  public RbTreeIterator<K,Void> rbTreeIterator() {
    return tree.iterator();
  }

  static class IteratorAdapter<K extends Comparable<? super K>> implements Iterator<K> {
    RbTreeIterator<K,Void> iterator;

    IteratorAdapter(RbTreeIterator<K,Void> iterator) {
      this.iterator = iterator;
    }

    public boolean hasNext() { return !iterator.isEnd(); }

    public K next() {
      if (iterator.isEnd()) {
        throw new NoSuchElementException();
      } else {
        K rv = iterator.key();
        iterator = iterator.next();
        return rv;
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
