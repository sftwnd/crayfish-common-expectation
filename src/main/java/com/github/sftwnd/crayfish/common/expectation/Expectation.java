package com.github.sftwnd.crayfish.common.expectation;

import com.github.sftwnd.crayfish.common.required.RequiredFunction;

import java.time.temporal.TemporalAccessor;

/**
 *
 * Functional interface. Allows you to extract the time marker associated with it from the object
 *
 * @param <M> item type
 * @param <T> time marker type
 */
public interface Expectation<M, T extends TemporalAccessor> extends RequiredFunction<M,T> {}
