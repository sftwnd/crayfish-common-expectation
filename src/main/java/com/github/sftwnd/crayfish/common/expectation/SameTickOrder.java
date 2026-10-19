package com.github.sftwnd.crayfish.common.expectation;

import java.util.Arrays;

/**
 * The order of {@link Expected#comparator(java.util.Comparator)} for the objects of the same tick, when no comparator
 * of the objects is given: a total order, in which two distinct objects are equal only by the natural order of their
 * class.
 */
final class SameTickOrder {

    // the order of the classes, and of the objects of a class that is not compared by its natural order
    private static final IdentityOrder IDENTITY = new IdentityOrder();

    // whether a class is comparable with itself: it has a public compareTo that accepts the class, and not only that
    // of a Comparable of another type, whose bridge method would throw a ClassCastException
    private static final ClassValue<Boolean> COMPARABLE = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return Comparable.class.isAssignableFrom(type) && Arrays.stream(type.getMethods())
                    .anyMatch(method -> "compareTo".equals(method.getName()) && !method.isBridge()
                            && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(type));
        }
    };

    private SameTickOrder() {
    }

    /**
     * Compares two objects of the same tick: objects of the same class that is comparable with itself by their natural
     * order, other objects of the same class by their identity, objects of different classes by the identity of their
     * classes.
     *
     * @param first the first object
     * @param second the second object
     * @return the result of the comparison
     */
    static int compare(Object first, Object second) {
        Class<?> type = first.getClass();
        if (type != second.getClass()) {
            return IDENTITY.compare(type, second.getClass());
        }
        return Boolean.TRUE.equals(COMPARABLE.get(type)) ? natural(first, second) : IDENTITY.compare(first, second);
    }

    @SuppressWarnings("unchecked")
    private static int natural(Object first, Object second) {
        return ((Comparable<Object>) first).compareTo(second);
    }

}
