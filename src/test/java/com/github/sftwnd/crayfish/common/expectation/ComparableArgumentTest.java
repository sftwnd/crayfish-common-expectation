package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComparableArgumentTest {

    @Test
    void directTest() {
        assertSame(Direct.class, ComparableArgument.of(Direct.class), "The argument of Comparable<Direct> has to be Direct");
        assertSame(Direct.class, ComparableArgument.of(SubDirect.class), "The argument of a subclass of Comparable<Direct> has to be Direct");
    }

    @Test
    void anotherTypeTest() {
        assertSame(String.class, ComparableArgument.of(ToString.class), "The argument of Comparable<String> has to be String, whatever overloads of compareTo the class has");
    }

    @Test
    void rawTest() {
        assertSame(Object.class, ComparableArgument.of(Raw.class), "The argument of a raw Comparable has to be Object");
    }

    @Test
    void enumTest() {
        assertSame(Phase.class, ComparableArgument.of(Phase.class), "The argument of an enum has to be the enum, by the type variable of Enum");
    }

    @Test
    void typeVariablesTest() {
        assertSame(Rank.class, ComparableArgument.of(Rank.class), "The argument of Ranked<Rank> has to be Rank");
        assertSame(Ranked.class, ComparableArgument.of(RawRank.class), "The argument of a raw Ranked has to be the bound of its type variable");
        assertSame(Final.class, ComparableArgument.of(Final.class), "The argument has to be substituted along two generic superclasses");
    }

    @Test
    void interfacesTest() {
        assertSame(String.class, ComparableArgument.of(StringKeyed.class), "The argument of Keyed<String> has to be String");
        assertSame(Ordered.class, ComparableArgument.of(OrderedImpl.class), "The argument of an interface that extends Comparable<Ordered> has to be Ordered");
    }

    @Test
    void genericArrayTest() {
        assertSame(Object[].class, ComparableArgument.of(ArrayOf.class), "The argument of Comparable<T[]> has to be the array of the erasure of T");
        assertSame(Number[].class, ComparableArgument.of(NumbersOf.class), "The argument of Comparable<T[]> of T extends Number has to be Number[]");
    }

    @Test
    void notComparableTest() {
        assertThrows(IllegalArgumentException.class, () -> ComparableArgument.of(Object.class), "A class that does not implement Comparable has no argument of it");
    }

    static class Direct implements Comparable<Direct> {
        @Override
        public int compareTo(Direct other) {
            return 0;
        }
    }

    static class SubDirect extends Direct {
    }

    static class ToString implements Comparable<String> {
        @Override
        public int compareTo(String other) {
            return 0;
        }
        public int compareTo(ToString other) {
            return this == other ? 0 : 1;
        }
    }

    @SuppressWarnings("rawtypes")
    static class Raw implements Comparable {
        @Override
        public int compareTo(Object other) {
            return 0;
        }
    }

    enum Phase {
        FIRST
    }

    abstract static class Ranked<T extends Ranked<T>> implements Comparable<T> {
        @Override
        public int compareTo(T other) {
            return 0;
        }
    }

    static class Rank extends Ranked<Rank> {
    }

    @SuppressWarnings("rawtypes")
    static class RawRank extends Ranked {
    }

    abstract static class Holder<T> implements Comparable<T> {
        @Override
        public int compareTo(T other) {
            return 0;
        }
    }

    static class Pass<V> extends Holder<V> {
    }

    static class Final extends Pass<Final> {
    }

    interface Keyed<K> extends Comparable<K> {
    }

    static class StringKeyed implements Keyed<String> {
        @Override
        public int compareTo(String other) {
            return 0;
        }
    }

    interface Ordered extends Comparable<Ordered> {
        @Override
        default int compareTo(Ordered other) {
            return 0;
        }
    }

    static class OrderedImpl implements Ordered {
    }

    static class ArrayOf<T> implements Comparable<T[]> {
        @Override
        public int compareTo(T[] other) {
            return 0;
        }
    }

    static class NumbersOf<T extends Number> implements Comparable<T[]> {
        @Override
        public int compareTo(T[] other) {
            return 0;
        }
    }

}
