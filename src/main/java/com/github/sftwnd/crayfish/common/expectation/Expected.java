package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;

/**
 * An object that has a tick: the point in time when something is due to happen with the object, or happened.
 * <p>
 * A tick may be of any {@link TemporalAccessor} type that can be converted to an {@link Instant}: {@code Instant},
 * {@link java.time.ZonedDateTime}, {@link java.time.OffsetDateTime}, ... The ticks are compared as instants, so that an
 * {@code Instant} and a {@code ZonedDateTime} of the same moment are equal. A tick without an instant, such as a
 * {@link java.time.LocalDateTime} or a {@link java.time.LocalDate}, makes {@link #happened()} and the comparisons throw
 * a {@link java.time.DateTimeException}; the packages of {@link ExpectedPackage} reject such a tick at the packaging.
 *
 * @param <T> the type of the tick
 */
@FunctionalInterface
public interface Expected<T extends TemporalAccessor> {

    /**
     * Returns the tick of the object.
     *
     * @return the tick, not null
     */
    @NonNull T getTick();

    /**
     * Tells whether the tick has come by a moment: whether the tick is not after the moment.
     *
     * @param moment the moment, or {@code null} for the current time
     * @return {@code true} if the tick is not after the moment
     * @throws java.time.DateTimeException if the tick or the moment cannot be converted to an {@link Instant}
     */
    default boolean happened(@Nullable TemporalAccessor moment) {
        return compareTemporalAccessor(getTick(), moment != null ? moment : Instant.now()) <= 0;
    }

    /**
     * Tells whether the tick has come by now: whether the tick is not after the current time.
     *
     * @return {@code true} if the tick is not after the current time
     * @throws java.time.DateTimeException if the tick cannot be converted to an {@link Instant}
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
     *     <li>objects of the same class that is comparable with itself (it implements {@code Comparable} of the class
     *     or of a supertype of it, itself or by a superclass or an interface): by their natural order;</li>
     *     <li>other objects of the same class: by an arbitrary order of the objects;</li>
     *     <li>objects of different classes: by the names of their classes, and classes of the same name, of different
     *     class loaders, by an arbitrary order of the classes.</li>
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
     * Compares two moments as instants.
     *
     * @param first the first moment
     * @param second the second moment
     * @return a negative number, zero or a positive number as the first moment is before, at or after the second
     * @throws NullPointerException if a moment is null
     * @throws java.time.DateTimeException if a moment cannot be converted to an {@link Instant}
     */
    static int compareTemporalAccessor(@NonNull TemporalAccessor first, @NonNull TemporalAccessor second) {
        return Instant.from(first).compareTo(Instant.from(second));
    }

}