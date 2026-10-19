package com.github.sftwnd.crayfish.common.expectation.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Objects of the same identity hash code, for the tests of the order of the objects of the same tick.
 */
public final class IdentityHashCodes {

    // an identity hash code has 31 bits at most: two of 100 000 objects share one with a probability of 90%
    private static final int LIMIT = 100_000_000;

    private IdentityHashCodes() {
    }

    /**
     * Creates objects until two of them have the same identity hash code.
     *
     * @param factory the factory of the objects
     * @param <T> the type of the objects
     * @return the two objects of the same identity hash code
     */
    public static <T> List<T> sameIdentityHashCode(Supplier<T> factory) {
        Map<Integer, T> objects = new HashMap<>();
        for (int count = 0; count < LIMIT; count++) {
            T object = factory.get();
            T previous = objects.putIfAbsent(System.identityHashCode(object), object);
            if (previous != null) {
                return Arrays.asList(previous, object);
            }
        }
        throw new AssertionError("No two of " + LIMIT + " objects have the same identity hash code");
    }

}
