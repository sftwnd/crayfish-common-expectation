package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * An element with a tick: an {@link Expected} that carries an object.
 * <p>
 * {@link #pack}, {@link #supply} and {@link #extract} make immutable packages of an element and a tick: the tick is
 * taken once, at the packaging, and checked there to be not null and to have an instant; two such packages of equal
 * elements and equal ticks are equal. Another implementation of the interface has an equality of its own.
 *
 * @param <M> the type of the element
 * @param <T> the type of the tick
 */
public interface ExpectedPackage<M,T extends TemporalAccessor> extends Expected<T> {

    /**
     * Returns the element of the package.
     *
     * @return the element, not null
     */
    @NonNull M getElement();

    /**
     * Packs an element with a tick.
     *
     * @param element the element
     * @param tick the tick
     * @param <M> the type of the element
     * @param <T> the type of the tick
     * @return an immutable package of the element and the tick, equal to another package of {@code pack},
     * {@code supply} or {@code extract} of an equal element and an equal tick
     * @throws NullPointerException if the element or the tick is null
     * @throws java.time.DateTimeException if the tick cannot be converted to an {@link java.time.Instant}
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@NonNull M element, @NonNull T tick) {
        Objects.requireNonNull(element, "ExpectedPackage::pack - element is null");
        Objects.requireNonNull(tick, "ExpectedPackage::pack - tick is null");
        return new SimpleExpectedPackage<>(element, tick);
    }

    /**
     * Packs an element with the tick of a supplier, which is called once, at the packaging.
     *
     * @param element the element
     * @param temporalSupplier the supplier of the tick
     * @param <M> the type of the element
     * @param <T> the type of the tick
     * @return an immutable package of the element and the tick, as that of {@link #pack(Object, TemporalAccessor)}
     * @throws NullPointerException if the element or the supplier is null, or the supplier gives a null tick
     * @throws java.time.DateTimeException if the tick cannot be converted to an {@link java.time.Instant}
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@NonNull M element, @NonNull TemporalSupplier<T> temporalSupplier) {
        Objects.requireNonNull(element, "ExpectedPackage::supply - element is null");
        Objects.requireNonNull(temporalSupplier, "ExpectedPackage::supply - temporalSupplier is null");
        return new SimpleExpectedPackage<>(element, Objects.requireNonNull(temporalSupplier.get(), "ExpectedPackage::supply - temporalSupplier gave a null tick"));
    }

    /**
     * Packs an element with the tick that an extractor gives for the element, once, at the packaging.
     *
     * @param element the element
     * @param extractor the extractor of the tick from the element
     * @param <M> the type of the element
     * @param <T> the type of the tick
     * @return an immutable package of the element and the tick, as that of {@link #pack(Object, TemporalAccessor)}
     * @throws NullPointerException if the element or the extractor is null, or the extractor gives a null tick
     * @throws java.time.DateTimeException if the tick cannot be converted to an {@link java.time.Instant}
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@NonNull M element, @NonNull TemporalExtractor<M,T> extractor) {
        Objects.requireNonNull(element, "ExpectedPackage::extract - element is null");
        Objects.requireNonNull(extractor, "ExpectedPackage::extract - extractor is null");
        return new SimpleExpectedPackage<>(element, Objects.requireNonNull(extractor.apply(element), "ExpectedPackage::extract - extractor gave a null tick"));
    }

    /**
     * Returns the text of a package, {@code ExpectedPackage(<tick>,<element>)}: the tick as an {@link java.time.Instant} if it
     * can be converted to one, and as it is otherwise. It is the text of the packages of {@code pack}, {@code supply}
     * and {@code extract}; it calls only {@code getTick()} and {@code getElement()} of another package.
     *
     * @param expectedPackage the package
     * @return the text of the package, or {@code "null"} for a null package
     */
    static String toString(@Nullable ExpectedPackage<?,?> expectedPackage) {
        return expectedPackage == null ? "null"
                : SimpleExpectedPackage.format(SimpleExpectedPackage.moment(expectedPackage.getTick()), expectedPackage.getElement());
    }

}
