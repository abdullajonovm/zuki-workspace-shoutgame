package tv.shout.util;

import java.util.Collection;

public class HashSet<E> extends java.util.HashSet<E> {

    private static final long serialVersionUID = 1L;

    public HashSet() {
        super();
    }

    public HashSet(Collection<? extends E> c) {
        super(c);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public HashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public String join(String delimiter, E...skip) {
        return CollectionJoin.join(this, delimiter, skip);
    }
}
