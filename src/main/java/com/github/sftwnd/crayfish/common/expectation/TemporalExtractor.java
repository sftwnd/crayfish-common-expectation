package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

/**
 * Extract Nonnull time-specific value from nonnull element
 * @param <E> element type
 * @param <T> time-specific type
 */
@FunctionalInterface
public interface TemporalExtractor<E,T extends TemporalAccessor> extends Function<E,T> {

    /**
     * Applies this function to the given argument for time extraction
     *
     * @param element source element
     * @return the time-specific result from the element
     */
    @Override @NonNull T apply(@NonNull E element);

}

