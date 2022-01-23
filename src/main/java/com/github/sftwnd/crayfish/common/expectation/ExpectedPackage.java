package com.github.sftwnd.crayfish.common.expectation;

import com.github.sftwnd.crayfish.common.required.RequiredFunction;
import com.github.sftwnd.crayfish.common.required.RequiredSupplier;

import javax.annotation.Nonnull;
import java.time.temporal.TemporalAccessor;

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
    @Nonnull M getElement();

    /**
     * Packing an element into a package with a specified time
     * @param element item
     * @param tick time marker
     * @param <M> type of packed object
     * @param <T> time marker type
     * @return package
     */
    @Nonnull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@Nonnull M element, @Nonnull T tick) {
        return new ExpectedPackage<M,T>() {
            @Override @Nonnull public M getElement() { return element; }
            @Override @Nonnull public T getTick() { return tick; }
        };
    }

    /**
     * Packing an element into a package with the construction of a time marker at the time of packaging
     * @param element item
     * @param tick time marker construction supplier
     * @param <M> type of packed object
     * @param <T> time marker type
     * @return package
     */
    @Nonnull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@Nonnull M element, @Nonnull RequiredSupplier<T> tick) {
        return new ExpectedPackage<M,T>() {
            @Override @Nonnull public M getElement() { return element; }
            @Override @Nonnull public T getTick() { return tick.get(); }
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
    @Nonnull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@Nonnull M element, @Nonnull RequiredFunction<M,T> extractor) {
        return new ExpectedPackage<M,T>() {
            @Override @Nonnull public M getElement() { return element; }
            @Override @Nonnull public T getTick() { return extractor.apply(element); }
        };
    }

}