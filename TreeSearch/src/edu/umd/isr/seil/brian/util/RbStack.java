package edu.umd.isr.seil.brian.util;

public class RbStack<K extends Comparable<? super K>, V> {
    final RbTree<K,V> _tree;
    private final boolean _dir;

    private final RbStack<K,V> _tail;

    public static <K extends Comparable<? super K>,V> RbStack<K,V> newInstance(RbTree<K,V> root) {
        return new EmptyRbStack<K,V>(root);
    }

    protected RbStack(RbTree<K,V> tree, boolean dir, RbStack<K,V> tail) {
        _tree = tree;
        _dir = dir;
        _tail = tail;
    }

    public RbTree<K,V> parent() { return _tree; }
    public boolean dir() { return _dir; }

    public RbStack<K,V> tail() { return _tail; }

    public RbStack<K,V> push(RbTree<K,V> tree, boolean dir) {
        return new RbStack<K,V>(tree, dir, this);
    }

    public boolean isEmpty() { return false; }

    // not tail recursive
    public RbStack<K,V> pushAll(RbStack<K,V> stack) {
        return stack.isEmpty() ? this
                : pushAll(stack.tail()).push(stack.parent(), stack.dir());
    }

    public RbTree<K,V> replace(RbTree<K,V> replacement) {
        return tail().replace(parent().setChild(replacement, dir()));
    }

    public RbTree<K,V> resolve() { return parent().getChild(dir()); }
    public RbTree<K,V> sibling() { return parent().getChild(!dir()); }
    public RbTree<K,V> gParent() { return tail().parent(); }
    public RbTree<K,V> uncle() { return tail().sibling(); }
    public RbTree<K,V> nearNephew() { return sibling().getChild(dir()); }
    public RbTree<K,V> farNephew() { return sibling().getChild(!dir()); }
    // there are 4 great nephews, in order of closeness 1 2 3 4
    public RbTree<K,V> gNephew1() { return nearNephew().getChild(dir()); }
    public RbTree<K,V> gNephew2() { return nearNephew().getChild(!dir()); }
    // there are 8 great great nephews in order of closeness 1-8
    public RbTree<K,V> ggNephew1() { return gNephew1().getChild(dir()); }
    public RbTree<K,V> ggNephew2() { return gNephew1().getChild(!dir()); }

    RbTree<K,V> stitchRed(RbTree<K,V> red) {
        if (parent().color() == RbTree.BLACK) return replace(red);

        RbTree<K,V> newTree = null;
        if (dir() == tail().dir()) {
            RbTree<K,V> newSib = gParent().setChild(sibling(), dir());
            newTree = parent().setChildren(red.setColor(RbTree.BLACK),
                    newSib, dir());
        } else {
            RbTree<K,V> newL = parent().setColorAndChild(RbTree.BLACK,
                    red.getChild(!dir()), dir());
            RbTree<K,V> newR = gParent().setChild(red.getChild(dir()), !dir());
            newTree = red.setChildren(newL, newR, !dir());
        }
        return tail().tail().stitchRed(newTree);
    }

    RbTree<K,V> stitchBlacken(RbTree<K,V> black) {
        if (black.color() == RbTree.RED) {
            return replace(black.setColor(RbTree.BLACK));
        }
        RbTree<K,V> newTree = null;
        if (sibling().color() == RbTree.RED) {
            if (gNephew1().color() == RbTree.RED) {
                RbTree<K,V> newP = parent().setChildren(black, ggNephew1(), dir());
                RbTree<K,V> newU = nearNephew().setChild(ggNephew2(), dir());
                RbTree<K,V> newGp = gNephew1().setChildren(newP, newU, dir());
                newTree = sibling().setColorAndChild(RbTree.BLACK,
                        newGp, dir());
            } else {
                RbTree<K,V> newP = parent().setColorAndChildren(RbTree.RED,
                        black, gNephew1(), dir());
                RbTree<K,V> newGp = nearNephew().setChild(newP, dir());
                newTree = sibling().setColorAndChild(RbTree.BLACK,
                        newGp, dir());
            }
        } else {
            if (nearNephew().color() == RbTree.RED) {
                RbTree<K,V> newP = parent().setColorAndChildren(RbTree.BLACK,
                        black, gNephew1(), dir());
                RbTree<K,V> newU = sibling().setChild(gNephew2(), dir());
                newTree = nearNephew().setColorAndChildren(
                        parent().color(), newP, newU, dir());
            } else if (farNephew().color() == RbTree.RED) {
                RbTree<K,V> newP = parent().setColorAndChildren(RbTree.BLACK,
                        black, nearNephew(), dir());
                newTree = sibling().setColorAndChildren(parent().color(),
                        newP, farNephew().setColor(RbTree.BLACK), dir());
            } else {
                return tail().stitchBlacken(parent().setChildren(
                        black, sibling().setColor(RbTree.RED), dir()));
            }
        }
        return tail().replace(newTree);
    }

    static class EmptyRbStack<K extends Comparable<? super K>,V>
            extends RbStack<K,V> {
        EmptyRbStack(RbTree<K,V> root) { super(root, true, null); }

        public RbTree<K,V> replace(RbTree<K,V> replacement) {
            return replacement;
        }

        public boolean isEmpty() { return true; }

        RbTree<K,V> stitchRed(RbTree<K,V> red) { return red.setColor(RbTree.BLACK); }
        RbTree<K,V> stitchBlacken(RbTree<K,V> black) {
            return black.setColor(RbTree.BLACK);
        }

        public boolean dir() {
            throw new java.util.NoSuchElementException(
                    "EmptyRbStack does not have associated direction");
        }

        public RbStack<K,V> tail() {
            throw new java.util.NoSuchElementException(
                    "EmptyRbStack has no tail");
        }

        // slight naming abuse
        public RbTree<K,V> resolve() { return _tree; }
        public RbTree<K,V> sibling() { throw new RuntimeException(); }
        public RbTree<K,V> gParent() { throw new RuntimeException(); }
        public RbTree<K,V> uncle() { throw new RuntimeException(); }
        public RbTree<K,V> nearNephew() { throw new RuntimeException(); }
        public RbTree<K,V> farNephew() { throw new RuntimeException(); }
        public RbTree<K,V> gNephew1() { throw new RuntimeException(); }
    }
}
