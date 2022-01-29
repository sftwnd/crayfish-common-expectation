package com.github.sftwnd.crayfish.common.expectation;

import javax.annotation.Nonnull;
import java.time.temporal.TemporalAccessor;
import java.util.function.Supplier;

/**
 * Extraction of nonnull time
 * @param <T> time-specific type
 */
interface TimeSupplier<T extends TemporalAccessor> extends Supplier<T> {
    /**
     * Gets a time-specific result.
     * @return time-specific value
     */
    @Override @Nonnull T get();
}
