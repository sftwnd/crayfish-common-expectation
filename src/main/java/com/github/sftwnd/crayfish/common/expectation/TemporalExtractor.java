package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

/**
 * A function that gives the tick of an element, not null: the tick of
 * {@link ExpectedPackage#extract(Object, TemporalExtractor)}.
 *
 * @param <E> the type of the element
 * @param <T> the type of the tick
 */
@FunctionalInterface
public interface TemporalExtractor<E,T extends TemporalAccessor> extends Function<E,T> {

    /**
     * Returns the tick of an element.
     *
     * @param element the element, not null
     * @return the tick, not null
     */
    @Override @NonNull T apply(@NonNull E element);

}

