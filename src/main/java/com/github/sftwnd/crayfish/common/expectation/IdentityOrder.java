package com.github.sftwnd.crayfish.common.expectation;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An arbitrary total order of objects by their identity: it does not change while the objects live, and an object is
 * equal only to itself.
 * <p>
 * The objects are ordered by their identity hash codes. Objects of the same identity hash code are ordered by the
 * numbers they get at their first comparison with one another: the numbers are held with weak references, so that they
 * do not keep the objects alive, and are dropped after the objects are collected.
 */
final class IdentityOrder {

    // the numbered objects, by their identity hash code
    private final Map<Integer, List<Numbered>> numbered = new HashMap<>();
    // the references of the numbered objects that are collected
    private final ReferenceQueue<Object> collected = new ReferenceQueue<>();
    private long lastNumber;

    /**
     * Compares two objects by their identity.
     *
     * @param first the first object
     * @param second the second object
     * @return 0 for the same object, otherwise a negative or a positive number, the same at every call
     */
    int compare(Object first, Object second) {
        if (first == second) {
            return 0;
        }
        int result = Integer.compare(System.identityHashCode(first), System.identityHashCode(second));
        return result != 0 ? result : Long.compare(number(first), number(second));
    }

    /**
     * Returns the count of the numbered objects that are not collected yet.
     *
     * @return the count
     */
    synchronized int size() {
        expunge();
        return numbered.values().stream().mapToInt(List::size).sum();
    }

    private synchronized long number(Object object) {
        expunge();
        List<Numbered> sameHash = numbered.computeIfAbsent(System.identityHashCode(object), hash -> new ArrayList<>(2));
        for (Numbered reference : sameHash) {
            if (reference.get() == object) {
                return reference.number;
            }
        }
        Numbered reference = new Numbered(object, collected, ++lastNumber);
        sameHash.add(reference);
        return reference.number;
    }

    // drops the numbers of the collected objects
    private synchronized void expunge() {
        for (Numbered reference = (Numbered) collected.poll(); reference != null; reference = (Numbered) collected.poll()) {
            List<Numbered> sameHash = numbered.get(reference.hash);
            sameHash.remove(reference);
            if (sameHash.isEmpty()) {
                numbered.remove(reference.hash);
            }
        }
    }

    // the number of an object, with a weak reference to it
    private static final class Numbered extends WeakReference<Object> {

        private final int hash;
        private final long number;

        Numbered(Object object, ReferenceQueue<Object> queue, long number) {
            super(object, queue);
            this.hash = System.identityHashCode(object);
            this.number = number;
        }

    }

}
