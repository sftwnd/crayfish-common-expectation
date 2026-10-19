package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;

import static java.util.Optional.ofNullable;

/**
 * This interface describes "scheduled" objects.
 * An Object has a specific point in time, which describes when to perform / performed any action with it.
 * @param <T> time marker type
 */
@FunctionalInterface
public interface Expected<T extends TemporalAccessor> {

    /**
     * Get the point in time associated with an object.
     * @return time moment
     */
    @NonNull T getTick();

    /**
     * Identification that a point in time has passed to a specified point in time.
     * If not specified, then currently
     *
     * @param tick point in time at which the check is made
     * @return confirmation of the time moment
     */
    default boolean happened(TemporalAccessor tick) {
        return compareTemporalAccessor(getTick(), ofNullable(tick).orElseGet(Instant::now)) <= 0;
    }

    /**
     * Identification that a point in time has arrived at the current point in time.
     *
     * @return confirmation of the time moment
     */
    default boolean happened() {
        return happened(null);
    }

    /**
     * Returns a comparator of objects by their ticks: the earlier tick first, the ticks compared as instants, and
     * {@code null} before every object.
     * <p>
     * The objects of the same tick are compared by {@code objectComparator}, and its result is final. Without it the
     * objects of the same tick are ordered:
     * <ul>
     *     <li>objects of the same class that is comparable with itself (it has a public {@code compareTo} that accepts
     *     the class): by their natural order;</li>
     *     <li>other objects of the same class: by an arbitrary order of the objects;</li>
     *     <li>objects of different classes: by an arbitrary order of their classes.</li>
     * </ul>
     * An arbitrary order does not change while the objects live, and in it an object is equal only to itself: the
     * comparator is a total order, and a sorted set keeps every distinct object of a tick, unless the natural order of
     * its class makes two of them equal.
     * <p>
     * The comparator throws a {@link java.time.DateTimeException} if a tick cannot be converted to an {@link Instant}.
     *
     * @param objectComparator the order of the objects of the same tick, or {@code null} for the order above
     * @param <M> the type of the objects
     * @return the comparator
     */
    static <M extends Expected<? extends TemporalAccessor>> Comparator<M> comparator(@Nullable Comparator<? super M> objectComparator) {
        return (first, second) -> {
            if (first == second) {
                return 0;
            }
            if (first == null || second == null) {
                return first == null ? -1 : 1;
            }
            int result = compareTemporalAccessor(first.getTick(), second.getTick());
            if (result != 0) {
                return result;
            }
            return objectComparator != null ? objectComparator.compare(first, second) : SameTickOrder.compare(first, second);
        };
    }

    /**
     * Comparison of 2 points in time
     * @param first first compared moment
     * @param second second compared moment
     * @return comparison result
     */
    static int compareTemporalAccessor(@NonNull TemporalAccessor first, @NonNull TemporalAccessor second) {
        return Instant.from(first).compareTo(Instant.from(second));
    }

}