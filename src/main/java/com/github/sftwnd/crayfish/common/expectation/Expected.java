package com.github.sftwnd.crayfish.common.expectation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * This interface describes "scheduled" objects.
 * An Object has a specific point in time, which describes when to perform / performed any action with it.
 */
public interface Expected<T extends TemporalAccessor> {

    /**
     * Get the point in time associated with an object.
     * @return time moment
     */
    @Nonnull T getTick();

    /**
     * Identification that a point in time has passed to a specified point in time.
     * If not specified, then currently
     *
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
     * Creating a comparator based on comparing 2 elements, where if the dates match, the passed comparator is used
     * @param objectComparator item comparator
     * @param <M> Expected extended type
     * @return result of comparison
     */
    @SuppressWarnings({
            "unchecked",
            /*  Just because you can do something, doesn’t mean you should, and that’s the case with nested ternary
                operations. Nesting ternary operators results in the kind of code that may seem clear as day when you
                write it, but six months later will leave maintainers (or worse - future you) scratching their heads
                and cursing.
             */
            "squid:S3358"
    })
    static <M extends Expected<? extends TemporalAccessor>> Comparator<M> comparator(@Nullable Comparator<? super M> objectComparator) {
        return (first, second) -> first == second ? 0 // 0 for the same element
                : first == null ? -1 // -1 for null first
                : second == null ? 1 // 1 for null second
                : Optional.of(compareTemporalAccessor(first.getTick(), second.getTick()))
                        // if times are not equals - result of time comparison
                        .filter(result -> result != 0)
                        // if dates are different - compare objects
                        .orElseGet(() -> Optional.ofNullable(objectComparator)
                                .map(comparator -> comparator.compare(first, second))
                                .orElseGet(() -> Optional.of(first)
                                        .filter(f -> f.getClass().equals(second.getClass()))
                                        .filter(Comparable.class::isInstance)
                                        .map(Comparable.class::cast)
                                        .map(f -> f.compareTo(second))
                                        .orElseGet(() -> Integer.compare(first.hashCode(), second.hashCode())))
                        );
    }

    /**
     * Comparison of 2 points in time
     * @param first first compared moment
     * @param second second compared moment
     * @return comparison result
     */
    static int compareTemporalAccessor(@Nonnull TemporalAccessor first, @Nonnull TemporalAccessor second) {
        return Instant.from(first).compareTo(Instant.from(second));
    }

}