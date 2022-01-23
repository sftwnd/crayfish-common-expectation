package com.github.sftwnd.crayfish.common.expectation;

import com.github.sftwnd.crayfish.common.required.RequiredFunction;

import javax.annotation.Nonnull;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public class ExpectationPackager<M,T extends TemporalAccessor> implements RequiredFunction<M, ExpectedPackage<M,T>> {

    private final Expectation<M,T> fireTimeExtractor;

    public ExpectationPackager(@Nonnull Expectation<M,T> fireTimeExtractor) {
        this.fireTimeExtractor = Objects.requireNonNull(fireTimeExtractor, "ExpectationPackager::new - fireTimeExtractor is null");
    }

    @Override
    public @Nonnull ExpectedPackage<M, T> apply(@Nonnull M element) {
        return ExpectedPackage.extract(element, fireTimeExtractor);
    }

}