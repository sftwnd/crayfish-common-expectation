package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * Packing an object into a package, where a temporary marker is attached to the element
 * @param <M> type of marked object
 * @param <T> time marker type
 */
public interface ExpectedPackage<M,T extends TemporalAccessor> extends Expected<T> {

    /**
     * Packed item
     * @return item
     */
    @NonNull M getElement();

    /**
     * Packing an element into a package with a specified time
     * @param element item
     * @param tick time marker
     * @param <M> type of packed object
     * @param <T> time marker type
     * @return package
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@NonNull M element, @NonNull T tick) {
        return supply(element, () -> tick);
    }

    /**
     * Packing an element into a package with the construction of a time marker at the time of packaging
     * @param element item
     * @param temporalSupplier time marker construction supplier
     * @param <M> type of packed object
     * @param <T> time marker type
     * @return package
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@NonNull M element, @NonNull TemporalSupplier<T> temporalSupplier) {
        Objects.requireNonNull(element, "ExpectedPackage::extract - element is null");
        Objects.requireNonNull(temporalSupplier, "ExpectedPackage::extract - temporalSupplier is null");
        return new ExpectedPackage<M,T>() {
            @Override @NonNull public M getElement() { return element; }
            @Override @NonNull public T getTick() { return Objects.requireNonNull(temporalSupplier.get(), "ExpectedPackage::getTick - result is null"); }
            @Override public String toString() { return ExpectedPackage.toString(this); }
        };
    }

    /**
     * Packing an element into a package with the construction of a time marker from the item at the time of packaging
     * @param element item
     * @param extractor function of constructing a time marker from an element
     * @param <M> type of packed object
     * @param <T> time marker type
     * @return package
     */
    @NonNull static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@NonNull M element, @NonNull TemporalExtractor<M,T> extractor) {
        Objects.requireNonNull(extractor, "ExpectedPackage::extract - extractor is null");
        return supply(element, () -> extractor.apply(element));
    }

    /**
     * Returns a String object representing the specified integer. The argument is converted to signed
     * @param expectedPackage - an expected package to be converted
     * @return string representation of the argument
     */
    static String toString(ExpectedPackage<?,?> expectedPackage) {
        return String.format("ExpectedPackage(%s,%s)", Instant.from(expectedPackage.getTick()), expectedPackage.getElement());
    }

}
