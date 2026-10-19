package com.github.sftwnd.crayfish.common.expectation.test;

import com.github.sftwnd.crayfish.common.expectation.Expected;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static com.github.sftwnd.crayfish.common.expectation.test.IdentityHashCodes.sameIdentityHashCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedTest {

    @Test
    void happenedTest() {
        Assertions.assertTrue(expected(Instant.now().minusMillis(10)).happened(), "A past Expected has to have happened");
        Assertions.assertFalse(expected(Instant.now().plusSeconds(10)).happened(), "A future Expected must not have happened");
    }

    @Test
    void happenedOnNullTickTest() {
        Assertions.assertTrue(expected(Instant.now().minusMillis(10)).happened(null), "A past Expected has to have happened by now, if the tick is null");
        Assertions.assertFalse(expected(Instant.now().plusSeconds(10)).happened(null), "A future Expected must not have happened by now, if the tick is null");
    }

    @Test
    void happenedOnTimeTest() {
        Assertions.assertTrue(expected(instant).happened(instant), "An Expected has to have happened at its own tick");
        Assertions.assertTrue(expected(instant.minusMillis(1)).happened(instant), "An Expected has to have happened at a later tick");
        Assertions.assertFalse(expected(instant.plusMillis(1)).happened(instant), "An Expected must not have happened at an earlier tick");
    }

    @Test
    void happenedOnOtherTemporalTypeTest() {
        Expected<ZonedDateTime> zoned = () -> ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Tokyo"));
        Assertions.assertTrue(zoned.happened(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)), "An Expected has to have happened at the same moment of another type and zone");
        Assertions.assertFalse(zoned.happened(instant.minusMillis(1)), "An Expected must not have happened at an earlier moment of another type");
    }

    @Test
    void compareTemporalAccessorTest() {
        assertTrue(Expected.compareTemporalAccessor(Instant.now().minusMillis(10), ZonedDateTime.now()) < 0, "The comparison of a moment before now with now has to be less than zero");
        assertTrue(Expected.compareTemporalAccessor(ZonedDateTime.now().plus(10, ChronoUnit.SECONDS), Instant.now()) > 0, "The comparison of a moment after now with now has to be greater than zero");
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        assertEquals(0, Expected.compareTemporalAccessor(zdt, instant), "The comparison of the same moment of different types has to be zero");
    }

    @Test
    void compareTemporalAccessorWithoutInstantTest() {
        LocalDateTime local = LocalDateTime.now();
        assertThrows(DateTimeException.class, () -> Expected.compareTemporalAccessor(local, instant), "A moment without a zone cannot be converted to an Instant");
        assertThrows(DateTimeException.class, () -> Expected.compareTemporalAccessor(instant, local), "A moment without a zone cannot be converted to an Instant");
    }

    @Test
    void comparatorTest() {
        Comparator<StringPack> stringPackComparator = Comparator.comparing(sp -> sp.string);
        Comparator<ExpectedStringPack> comparator = Expected.comparator(stringPackComparator);
        ExpectedStringPack expectedPackage = new ExpectedStringPack("A", instant);
        assertEquals(0, comparator.compare(expectedPackage, expectedPackage), "comparator(comparator).compare has to return zero for the same object");
        assertEquals(0, comparator.compare(expectedPackage, new ExpectedStringPack("A", instant)), "comparator(comparator).compare has to return zero for equal objects");
        assertEquals(0, comparator.compare(null, null), "comparator(comparator).compare has to return zero for two nulls");
        assertEquals(-1, comparator.compare(null, expectedPackage), "comparator(comparator).compare has to return -1 if the first object is null");
        assertEquals(1, comparator.compare(expectedPackage, null), "comparator(comparator).compare has to return 1 if the second object is null");
        assertTrue(comparator.compare(new ExpectedStringPack("A", instant.minusMillis(1)), expectedPackage) < 0, "comparator(comparator).compare(earlier tick (A), tick (A)) has to be less than zero");
        assertTrue(comparator.compare(expectedPackage, new ExpectedStringPack("A", instant.minusMillis(1))) > 0, "comparator(comparator).compare(tick (A), earlier tick (A)) has to be greater than zero");
        assertTrue(comparator.compare(expectedPackage, new ExpectedStringPack("B", instant)) < 0, "comparator(comparator).compare(tick (A), tick (B)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("B", instant), expectedPackage) > 0, "comparator(comparator).compare(tick (B), tick (A)) has to be greater than zero");
    }

    @Test
    void comparatorTickFirstTest() {
        Comparator<ExpectedStringPack> comparator = Expected.comparator(Comparator.comparing(sp -> sp.string));
        assertTrue(comparator.compare(new ExpectedStringPack("B", instant.minusMillis(1)), new ExpectedStringPack("A", instant)) < 0, "comparator(comparator).compare has to compare the ticks before the objects");
    }

    @Test
    void comparatorComparableTest() {
        Comparator<ExpectedStringPack> comparator = Expected.comparator(null);
        assertEquals(0, comparator.compare(new ExpectedStringPack("A", instant), new ExpectedStringPack("A", instant)), "comparator(null).compare has to return zero for equal objects");
        assertTrue(comparator.compare(new ExpectedStringPack("A", instant.minusMillis(1)), new ExpectedStringPack("A", instant)) < 0, "comparator(null).compare(earlier tick (A), tick (A)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A", instant), new ExpectedStringPack("A", instant.minusMillis(1))) > 0, "comparator(null).compare(tick (A), earlier tick (A)) has to be greater than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A", instant), new ExpectedStringPack("B", instant)) < 0, "comparator(null).compare(tick (A), tick (B)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("B", instant), new ExpectedStringPack("A", instant)) > 0, "comparator(null).compare(tick (B), tick (A)) has to be greater than zero");
    }

    @Test
    void comparatorNotComparableTest() {
        Comparator<HashedExpected> comparator = Expected.comparator(null);
        HashedExpected first = new HashedExpected(1, instant);
        HashedExpected second = new HashedExpected(1, instant);
        assertEquals(0, comparator.compare(first, first), "comparator(null).compare has to return zero for the same object that is not Comparable");
        int result = comparator.compare(first, second);
        assertNotEquals(0, result, "comparator(null).compare must not make distinct objects that are not Comparable equal, even with equal hash codes");
        assertEquals(-Integer.signum(result), Integer.signum(comparator.compare(second, first)), "comparator(null).compare has to be antisymmetric for objects that are not Comparable");
        assertEquals(result, comparator.compare(first, second), "comparator(null).compare has to give the same order at every call");
    }

    @Test
    void comparatorSortedSetTest() {
        Set<HashedExpected> set = new TreeSet<>(Expected.comparator(null));
        for (int i = 0; i < 1000; i++) {
            set.add(new HashedExpected(i % 3, instant));
        }
        assertEquals(1000, set.size(), "A sorted set of comparator(null) has to keep every distinct object of a tick that is not Comparable");
    }

    @Test
    void comparatorSameIdentityHashCodeTest() {
        List<Expected<Instant>> objects = sameIdentityHashCode(() -> expected(instant));
        Comparator<Expected<Instant>> comparator = Expected.comparator(null);
        int result = comparator.compare(objects.get(0), objects.get(1));
        assertNotEquals(0, result, "comparator(null).compare must not make distinct objects of the same identity hash code equal");
        assertEquals(-result, comparator.compare(objects.get(1), objects.get(0)), "comparator(null).compare has to be antisymmetric for objects of the same identity hash code");
        Set<Expected<Instant>> set = new TreeSet<>(comparator);
        set.addAll(objects);
        assertEquals(2, set.size(), "A sorted set of comparator(null) has to keep distinct objects of the same identity hash code");
    }

    @Test
    void comparatorDifferentClassesTest() {
        Comparator<ExpectedStringPack> comparator = Expected.comparator(null);
        ExpectedStringPack first = new ExpectedStringPack("A", instant);
        ExpectedStringPack second = new OtherExpectedStringPack("A", instant);
        int result = comparator.compare(first, second);
        assertNotEquals(0, result, "comparator(null).compare must not compare objects of different classes by the natural order of one of them");
        assertEquals(-Integer.signum(result), Integer.signum(comparator.compare(second, first)), "comparator(null).compare has to be antisymmetric for objects of different classes");
        assertEquals(0, comparator.compare(second, new OtherExpectedStringPack("A", instant)), "comparator(null).compare has to compare objects of the same Comparable subclass by their natural order");
    }

    @Test
    void comparatorComparableWithAnotherTypeTest() {
        Comparator<ComparableToString> comparator = Expected.comparator(null);
        ComparableToString first = new ComparableToString(instant);
        ComparableToString second = new ComparableToString(instant);
        assertEquals(0, comparator.compare(first, first), "comparator(null).compare has to return zero for the same object");
        int result = comparator.compare(first, second);
        assertNotEquals(0, result, "comparator(null).compare must not make distinct objects of a class that is not comparable with itself equal");
        assertEquals(-Integer.signum(result), Integer.signum(comparator.compare(second, first)), "comparator(null).compare has to be antisymmetric for a class that is not comparable with itself");
    }

    @Test
    void comparatorTransitivityTest() {
        Comparator<Expected<Instant>> comparator = Expected.comparator(null);
        // a < b by their natural order, b < c and c < a by their hash codes in the order of 1.3.x
        List<Expected<Instant>> cycle = Arrays.asList(new HashedExpectedStringPack("A", instant, 5), new HashedExpectedStringPack("B", instant, 1), new HashedExpected(3, instant));
        assertTotalOrder(comparator, cycle);
        List<Expected<Instant>> objects = new ArrayList<>();
        for (Instant tick : Arrays.asList(instant, instant.plusSeconds(1))) {
            for (int hash = 1; hash <= 5; hash += 2) {
                objects.add(new HashedExpectedStringPack(String.valueOf((char) ('A' + hash % 3)), tick, hash));
                objects.add(new HashedExpected(hash % 2, tick));
            }
            objects.add(new OtherExpectedStringPack("A", tick));
            objects.add(new OtherExpectedStringPack("B", tick));
            objects.add(new ComparableToString(tick));
            objects.add(new ComparableToString(tick));
            objects.add(expected(tick));
            objects.add(expected(tick));
        }
        assertTotalOrder(comparator, objects);
    }

    @Test
    void comparatorSortTest() {
        Random random = new Random(1);
        List<Expected<Instant>> objects = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            Instant tick = instant.plusSeconds(random.nextInt(3));
            switch (random.nextInt(4)) {
                case 0:
                    objects.add(new HashedExpectedStringPack(String.valueOf(random.nextInt(10)), tick, random.nextInt()));
                    break;
                case 1:
                    objects.add(new HashedExpected(random.nextInt(), tick));
                    break;
                case 2:
                    objects.add(new OtherExpectedStringPack(String.valueOf(random.nextInt(10)), tick));
                    break;
                default:
                    objects.add(expected(tick));
            }
        }
        Comparator<Expected<Instant>> comparator = Expected.comparator(null);
        objects.sort(comparator);
        for (int i = 1; i < objects.size(); i++) {
            assertTrue(comparator.compare(objects.get(i - 1), objects.get(i)) <= 0, "A list sorted by comparator(null) has to be in its order");
        }
    }

    private static <T> void assertTotalOrder(Comparator<? super T> comparator, List<T> objects) {
        for (T first : objects) {
            for (T second : objects) {
                int result = Integer.signum(comparator.compare(first, second));
                assertEquals(-result, Integer.signum(comparator.compare(second, first)), "The comparator has to be antisymmetric");
                for (T third : objects) {
                    if (result <= 0 && comparator.compare(second, third) <= 0) {
                        assertTrue(comparator.compare(first, third) <= 0, "The comparator has to be transitive");
                    }
                }
            }
        }
    }

    static class StringPack implements Comparable<StringPack> {
        final String string;
        StringPack(String string) {
            this.string = string;
        }

        @Override
        public int compareTo(@NonNull StringPack o) {
            return string.compareTo(o.string);
        }
    }

    static class ExpectedStringPack extends StringPack implements Expected<Instant> {
        final Instant instant;
        ExpectedStringPack(String string, Instant instant) {
            super(string);
            this.instant = instant;
        }
        @NonNull
        @Override
        public Instant getTick() {
            return this.instant;
        }
    }

    static class OtherExpectedStringPack extends ExpectedStringPack {
        OtherExpectedStringPack(String string, Instant instant) {
            super(string, instant);
        }
    }

    static class HashedExpectedStringPack extends ExpectedStringPack {
        final int hash;
        HashedExpectedStringPack(String string, Instant instant, int hash) {
            super(string, instant);
            this.hash = hash;
        }
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            HashedExpectedStringPack that = (HashedExpectedStringPack) o;
            return hash == that.hash && string.equals(that.string) && instant.equals(that.instant);
        }
        @Override
        public int hashCode() {
            return hash;
        }
    }

    static class HashedExpected implements Expected<Instant> {
        final int hash;
        final Instant instant;
        HashedExpected(int hash, Instant instant) {
            this.hash = hash;
            this.instant = instant;
        }
        @NonNull
        @Override
        public Instant getTick() {
            return this.instant;
        }
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            HashedExpected that = (HashedExpected) o;
            return hash == that.hash && instant.equals(that.instant);
        }
        @Override
        public int hashCode() {
            return hash;
        }
    }

    // Comparable with a String, not with itself; its compareTo of two objects is an overload, not that of Comparable
    static class ComparableToString implements Expected<Instant>, Comparable<String> {
        final Instant instant;
        ComparableToString(Instant instant) {
            this.instant = instant;
        }
        @NonNull
        @Override
        public Instant getTick() {
            return this.instant;
        }
        @Override
        public int compareTo(@NonNull String string) {
            throw new UnsupportedOperationException("ComparableToString is not compared with a String in the tests");
        }
        public int compareTo(ComparableToString first, ComparableToString second) {
            return first.instant.compareTo(second.instant);
        }
    }

    Expected<Instant> expected(Instant instant) {
        return () -> instant;
    }

    Instant instant;

    @BeforeEach
    void startUp() {
        instant = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    }

    @AfterEach
    void tearDown() {
        instant = null;
    }

}
