package com.github.sftwnd.crayfish.common.expectation;

import javax.annotation.Nonnull;
import java.time.temporal.TemporalAccessor;

/**
 *
 * Functional interface. Allows you to extract the time marker associated with it from the object
 *
 * @param <M> element type
 * @param <T> time marker type
 */
@FunctionalInterface
public interface Expectation<M, T extends TemporalAccessor> extends TimeExtractor<M,T> {

    /**
     * Applies this function to the given argument.
     *
     * @param element the function argument
     * @return the function result
     */
    @Nonnull T apply(@Nonnull M element);

}
