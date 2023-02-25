package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.temporal.TemporalAccessor;
import java.util.function.Supplier;

/**
 * Extraction of nonnull time
 * @param <T> time-specific type
 */
@FunctionalInterface
public interface TemporalSupplier<T extends TemporalAccessor> extends Supplier<T> {

    /**
     * Gets a time-specific result.
     * @return time-specific value
     */
    @Override @NonNull T get();

}
