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
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(comparator.compare(new HashedExpected(1, instant), new HashedExpected(2, instant)) < 0, "comparator(null).compare has to compare objects that are not Comparable by their hash codes");
        assertTrue(comparator.compare(new HashedExpected(2, instant), new HashedExpected(1, instant)) > 0, "comparator(null).compare has to compare objects that are not Comparable by their hash codes");
        assertEquals(0, comparator.compare(new HashedExpected(1, instant), new HashedExpected(1, instant)), "comparator(null).compare has to return zero for objects that are not Comparable with the same hash code");
    }

    @Test
    void comparatorComparableOfDifferentClassesTest() {
        Comparator<ExpectedStringPack> comparator = Expected.comparator(null);
        ExpectedStringPack first = new HashedExpectedStringPack("B", instant, 1);
        ExpectedStringPack second = new HashedExpectedStringPack("A", instant, 2);
        ExpectedStringPack third = new OtherHashedExpectedStringPack("A", instant, 3);
        assertTrue(comparator.compare(first, second) > 0, "comparator(null).compare has to compare Comparable objects of the same class by their natural order");
        assertTrue(comparator.compare(second, third) < 0, "comparator(null).compare has to compare Comparable objects of different classes by their hash codes");
        assertTrue(comparator.compare(third, second) > 0, "comparator(null).compare has to compare Comparable objects of different classes by their hash codes");
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

    static class OtherHashedExpectedStringPack extends HashedExpectedStringPack {
        OtherHashedExpectedStringPack(String string, Instant instant, int hash) {
            super(string, instant, hash);
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
