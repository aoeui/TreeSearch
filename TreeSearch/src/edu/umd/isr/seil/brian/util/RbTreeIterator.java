package edu.umd.isr.seil.brian.util;

public class RbTreeIterator<K extends Comparable<? super K>, V> {
    // public final static RbTreeIterator END = RbTreeIteratorEnd.INSTANCE;

    final RbStack<K,V> _state;    // must resolve to a non-leaf element

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>,V> RbTreeIterator<K,V>
        getEnd() {
      return (RbTreeIterator<K,V>)RbTreeIteratorEnd.INSTANCE;
    }

    // not for client classes
    RbTreeIterator(RbStack<K,V> stack) {
        _state = stack;
    }
    
    public String toString() {
      return "[" + key() + ", " + value() + "]";
    }

    public RbTreeIterator<K,V> next() {
        RbTree<K,V> current = _state.resolve();
        RbTree<K,V> right = current.right();
        if (!right.isLeaf()) {
            return new RbTreeIterator<K,V>(searchLeft(_state.push(current,
                    RbTree.RIGHT)));
        } else {
            if (_state.isEmpty()) return RbTreeIterator.<K,V>getEnd();

            if (_state.dir() == RbTree.LEFT) {
                return new RbTreeIterator<K,V>(_state.tail());
            } else {
                RbStack<K,V> search = _state.tail();
                boolean found = false;
                while (!found && !search.isEmpty()) {
                    found = search.dir() == RbTree.LEFT;
                    search = search.tail();
                }
                return found ? new RbTreeIterator<K,V>(search)
                        : RbTreeIterator.<K,V>getEnd();
            }
        }
    }

    RbStack<K,V> searchLeft(RbStack<K,V> stack) {
        RbTree<K,V> current = stack.resolve();
        RbTree<K,V> next = current.left();
        while (!next.isLeaf()) {
            stack = stack.push(current, RbTree.LEFT);
            current = next;
            next = current.left();
        }
        return stack;
    }

    public boolean isEnd() {
        return false;
    }
    public V value() { return _state.resolve().val(); }
    public K key() { return _state.resolve().key(); }

    @SuppressWarnings("rawtypes")
    static class RbTreeIteratorEnd extends RbTreeIterator {
        public final static RbTreeIteratorEnd INSTANCE
                = new RbTreeIteratorEnd();

        @SuppressWarnings("unchecked")
        private RbTreeIteratorEnd() { super(null); }
        public RbTreeIterator<?,?> next() {
            throw new java.util.NoSuchElementException();
        }
        public boolean isEnd() { return true; }
        public Object value() {
            throw new java.util.NoSuchElementException();
        }
        public Comparable<?> key() {
            throw new java.util.NoSuchElementException();
        }
    }
}
