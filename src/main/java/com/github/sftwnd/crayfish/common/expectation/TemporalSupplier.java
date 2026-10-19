package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.temporal.TemporalAccessor;
import java.util.function.Supplier;

/**
 * A supplier of a tick, not null: the tick of {@link ExpectedPackage#supply(Object, TemporalSupplier)}.
 *
 * @param <T> the type of the tick
 */
@FunctionalInterface
public interface TemporalSupplier<T extends TemporalAccessor> extends Supplier<T> {

    /**
     * Returns a tick.
     *
     * @return the tick, not null
     */
    @Override @NonNull T get();

}
