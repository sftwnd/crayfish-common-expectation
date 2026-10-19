package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

/**
 * The package of {@link ExpectedPackage#pack}, {@link ExpectedPackage#supply} and {@link ExpectedPackage#extract}: an
 * immutable element and tick, equal to another such package of an equal element and an equal tick.
 *
 * @param <M> the type of the element
 * @param <T> the type of the tick
 */
final class SimpleExpectedPackage<M, T extends TemporalAccessor> implements ExpectedPackage<M, T> {

    private final M element;
    private final T tick;
    // the tick as an instant: converted at the packaging, so that a tick without an instant is rejected there
    private final Instant instant;

    /**
     * Creates a package of an element and a tick.
     *
     * @param element the element, not null
     * @param tick the tick, not null
     * @throws DateTimeException if the tick cannot be converted to an {@link Instant}
     */
    SimpleExpectedPackage(M element, T tick) {
        this.element = element;
        this.tick = tick;
        this.instant = Instant.from(tick);
    }

    @Override
    @NonNull
    public M getElement() {
        return element;
    }

    @Override
    @NonNull
    public T getTick() {
        return tick;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SimpleExpectedPackage)) {
            return false;
        }
        SimpleExpectedPackage<?, ?> other = (SimpleExpectedPackage<?, ?>) object;
        return element.equals(other.element) && tick.equals(other.tick);
    }

    @Override
    public int hashCode() {
        return 31 * element.hashCode() + tick.hashCode();
    }

    @Override
    public String toString() {
        return format(instant, element);
    }

    /**
     * Returns the text of a package: {@code ExpectedPackage(<tick>,<element>)}.
     *
     * @param tick the tick, or its instant
     * @param element the element
     * @return the text
     */
    static String format(@Nullable Object tick, @Nullable Object element) {
        return "ExpectedPackage(" + tick + "," + element + ")";
    }

    /**
     * Returns a tick as an instant, if it can be converted to one, otherwise the tick itself.
     *
     * @param tick the tick
     * @return the instant of the tick, the tick, or null for a null tick
     */
    @Nullable
    static Object moment(@Nullable TemporalAccessor tick) {
        if (tick == null) {
            return null;
        }
        try {
            return Instant.from(tick);
        } catch (DateTimeException exception) {
            return tick;
        }
    }

}
