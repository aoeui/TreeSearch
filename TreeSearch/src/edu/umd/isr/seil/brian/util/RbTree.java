package edu.umd.isr.seil.brian.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class RbTree<K extends Comparable<? super K>, V>
    implements java.io.Serializable {
  // private final static RbTree LEAF = RbTreeLeaf.INSTANCE;
  // private final static RbTree EMPTY = LEAF;

  private static final long serialVersionUID = 8336576521057482327L;
  public final static boolean RED = true;
  public final static boolean BLACK = false;

  public final static boolean LEFT = false;
  public final static boolean RIGHT = true;

  private final boolean _color;    // black is true, red is false
  private final K _key;
  private final V _val;
  private final RbTree<K,V> _left, _right;

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<? super K>,V> RbTree<K, V> getLeaf() {
    return (RbTree<K, V>)RbTreeLeaf.INSTANCE;
  }

  @SuppressWarnings("unchecked")
  public static <K extends Comparable<? super K>,V> RbTree<K, V> getEmpty() {
    return (RbTree<K, V>)RbTreeLeaf.INSTANCE;
  }
  @SuppressWarnings("unchecked")
  public static <K extends Comparable<? super K>,V> RbTree<K, V> empty() {
    return (RbTree<K, V>)RbTreeLeaf.INSTANCE;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder("{");
    boolean isFirst = true;
    for (RbTreeIterator<K,V> it = iterator(); !it.isEnd(); it=it.next()) {
      if (isFirst) isFirst = false;
      else builder.append(", ");
      builder.append("[").append(it.key()).append("->").append(it.value()).append("]");
    }
    builder.append("}");
    return builder.toString();
  }
  
  public int size() {
    return 1+_left.size()+_right.size();
  }

    // Client classes should build trees starting from LEAF and not call
    // this constructor.
    RbTree(boolean color, K key, V val, RbTree<K,V> left, RbTree<K,V> right) {
        _color = color;
        _key = key;
        _val = val;
        _left = left;
        _right = right;
    }

    public RbTree<K,V> merge(RbTree<K,V> t) {
        RbTree<K,V> merged = this;
        RbTreeIterator<K,V> it;
        for (it = t.iterator();  !it.isEnd();  it = it.next()) {
            merged = merged.put(it.key(), it.value());
        }
        return merged;
    }

    public boolean isEmpty() { return isLeaf(); }
    public boolean isLeaf() { return false; }

    public K key() { return _key; }
    public boolean color() { return _color; }
    public RbTree<K,V> left() { return _left; }
    public RbTree<K,V> right() { return _right; }
    public V val() { return _val; }

    // If entry with given key exists, it is replaced, otherwise a new
    // node is added to the tree.
    public RbTree<K,V> put(K key, V val) {
        RbStack<K,V> stack = find(key);
        if (stack.resolve().isLeaf()) {
            return stack.stitchRed(new RbTree<K,V>(RbTree.RED, key, val,
                        RbTree.<K,V>getLeaf(), RbTree.<K,V>getLeaf()));
        } else {
            return stack.replace(stack.resolve().setVal(val));
        }
    }

    private RbTree<K,V> setVal(V val) {
        return new RbTree<K,V>(color(), key(), val, left(), right());
    }
    private RbTree<K,V> setEntry(K key, V val) {
        return new RbTree<K,V>(color(), key, val, left(), right());
    }
    RbTree<K,V> setColor(boolean color) {
        return new RbTree<K,V>(color, key(), val(), left(), right());
    }
    RbTree<K,V> setColorAndChildren(boolean color, RbTree<K,V> left,
            RbTree<K,V> right, boolean flip) {
        return flip ? new RbTree<K,V>(color, key(), val(), right, left)
                : new RbTree<K,V>(color, key(), val(), left, right);
    }
    RbTree<K,V> setColorAndChild(boolean color, RbTree<K,V> child,
            boolean dir) {
        return dir == LEFT
                ? new RbTree<K,V>(color, key(), val(), child, right())
                : new RbTree<K,V>(color, key(), val(), left(), child);
    }
    RbTree<K,V> setChildren(RbTree<K,V> left, RbTree<K,V> right) {
        return new RbTree<K,V>(color(), key(), val(), left, right);
    }
    RbTree<K,V> setChildren(RbTree<K,V> left, RbTree<K,V> right,
            boolean flip) {
        return flip ? setChildren(right, left) : setChildren(left, right);
    }
    RbTree<K,V> setChild(RbTree<K,V> newChild, boolean dir) {
        return dir == LEFT ? setChildren(newChild, right())
                : setChildren(left(), newChild);
    }

    // Removes the entry for the given key if it is present.  Throws an exception
    // if the key is not found.
    public RbTree<K,V> remove(K key) {
        RbStack<K,V> stack = find(key);
        RbTree<K,V> victim = stack.resolve();
        if (victim.isLeaf()) { throw new NoSuchElementException(); }
        if (!(victim.left().isLeaf() || victim.right().isLeaf())) {
            RbStack<K,V> toSucc = victim.right().findLeast(
                    RbStack.<K,V>newInstance(victim.right()));
            RbTree<K,V> succ = toSucc.resolve();
            stack = stack.push(victim.setEntry(succ.key(), succ.val()), RIGHT)
                    .pushAll(toSucc);
            victim = succ;
        }
        RbTree<K,V> stitched = victim.right().isLeaf()
            ? victim.left() : victim.right();
        return victim.color() == RED
                ? stack.replace(stitched) : stack.stitchBlacken(stitched);
    }

    // Returns null if object not found, but also returns null if "null"
    // is keyed with this key.
    public V get(K key) {
        if (key.equals(_key)) return _val;
        return getChild(key.compareTo(_key) >= 1).get(key);
    }

    public boolean containsKey(K key) {
        if (key.equals(_key)) return true;
        return getChild(key.compareTo(_key) >= 1).containsKey(key);
    }

    protected RbStack<K,V> findLeast(RbStack<K,V> stack) {
        return left().findLeast(stack.push(this, LEFT));
    }
    protected RbStack<K,V> findGreatest(RbStack<K,V> stack) {
        return right().findGreatest(stack.push(this, RIGHT));
    }

    // Returns a path to the node containing the specified key.
    // Returns pointer to a leaf position if the tree does not contain
    // the specified key.
    public RbStack<K,V> find(K key) {
        return findImpl(RbStack.<K,V>newInstance(this), key);
    }

    protected RbStack<K,V> findImpl(RbStack<K,V> stack, K key) {
        if (key.equals(_key)) return stack;
        boolean nextDir = key.compareTo(_key) >= 1;
        return getChild(nextDir).findImpl(stack.push(this, nextDir), key);
    }

    RbTree<K,V> getChild(boolean dir) {
        return dir == LEFT ? left() : right();
    }

    public Collection<K> keys() {
        LinkedList<K> list = new LinkedList<K>();
        addKeys(list);
        return list;
    }

    void addKeys(LinkedList<K> list) {
        left().addKeys(list);
        list.add(key());
        right().addKeys(list);
    }

    public RbTreeIterator<K,V> iterator() {
        return new RbTreeIterator<K,V>(findLeast(RbStack.<K,V>newInstance(this)));
    }

    // test code
    void verifyProperties() {
        blackHeight();
        verifyRb();
    }

    void verifyRb() {
        if (color() == RED) {
            if (left().color() == RED || right().color() == RED) {
                throw new RuntimeException("Red-Red!");
            }
        }
        left().verifyRb();
        right().verifyRb();
    }

    int blackHeight() {
        int lbh = left().blackHeight();
        int rbh = right().blackHeight();
        if (lbh != rbh) {
            throw new RuntimeException("Unmatched Black heights!");
        }
        return lbh + (color() == BLACK ? 1 : 0);
    }

    @SuppressWarnings("rawtypes")
    static class RbTreeLeaf<K extends Comparable<? super K>,V> extends RbTree<K,V> {
      private static final long serialVersionUID = -7427714198747737037L;
      
      public int size() { return 0; }
        final static RbTreeLeaf INSTANCE = new RbTreeLeaf();

        private RbTreeLeaf() {
            super(BLACK, null, null, null, null);
        }

        @Override
        public boolean isLeaf() { return true; }

        // Hopefully, this overrides?
        @Override
        public RbTree<K,V> put(K key, V val) {
            return new RbTree<K,V>(BLACK, key, val, this, this);
        }
        @Override
        public V get(K key) { return null; }
        @Override
        public boolean containsKey(K key) { return false; }

        protected RbStack<K,V> findImpl(RbStack<K,V> stack, K key) {
            return stack;
        }
        protected RbStack<K,V> findLeast(RbStack<K,V> stack) { return stack.tail(); }
        protected RbStack<K,V> findGreatest(RbStack<K,V> stack) { return stack.tail(); }

        public RbTreeIterator<K,V> iterator() { return RbTreeIterator.getEnd(); }

        public RbTree<K,V> left() { return childError(); }
        public RbTree<K,V> right() { return childError(); }

        public RbTree<K,V> setColor(boolean color) {
            if (color == BLACK) return this;
            throw new IllegalArgumentException("Leaf nodes cannot be RED");
        }

        public int blackHeight() { return 1; }
        public void verifyRb() { }

        @Override
        void addKeys(LinkedList list) { }

        private RbTree<K,V> childError() {
            throw new java.util.NoSuchElementException(
                    "Leaf nodes have no children");
        }
    }
}
