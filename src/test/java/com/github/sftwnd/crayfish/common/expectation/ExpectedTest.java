package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class ExpectedTest {

    @Test
    void happenedTest() {
        assertTrue(expected(Instant.now().minusMillis(10)).happened(), "Past Expected has to be happend");
        assertFalse(expected(Instant.now().plusMillis(10)).happened(), "Past Expected hasn't got to be happend");
    }

    @Test
    void happenedOnTimeTest() {
        assertTrue(expected(instant).happened(instant), "Current Expected on the moment has to be happend");
        assertTrue(expected(instant.minusMillis(1)).happened(instant), "Past Expected on the moment has to be happend");
        assertFalse(expected(instant.plusMillis(1)).happened(instant), "Past Expected on the moment hasn't got to be happend");
    }

    @Test
    void compareTemporalAccessorTest() {
        assertTrue(Expected.compareTemporalAccessor(Instant.now().minusMillis(10), ZonedDateTime.now()) < 0, "TemporalAccessor result for less to now has to be less than zero" );
        assertTrue(Expected.compareTemporalAccessor(ZonedDateTime.now().plus(10, ChronoUnit.SECONDS), Instant.now()) > 0, "TemporalAccessor result for future to now has to be more than zero" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        assertEquals(0, Expected.compareTemporalAccessor(zdt, instant), "TemporalAccessor result for equals moment of time has to be equals zero");
    }

    @Test
    void comparatorTest() {
        Comparator<StringPack> stringPackComparator = Comparator.comparing(sp -> sp.string);
        Comparator<ExpectedStringPack> comparator = Expected.comparator(stringPackComparator);
        assertEquals(0, comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("A",instant)), "comparator(comparator).compare has to return zero for equals values");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant.minusMillis(1)),  new ExpectedStringPack("A",instant)) < 0, "comparator(comparator).compare(prev instant(a),instant(a)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("A",instant.minusMillis(1))) > 0, "comparator(comparator).compare(instant(a),prev instant(a)) has to be more than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("B",instant)) < 0, "comparator(comparator).compare(instant(a),instant(b)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("B",instant),  new ExpectedStringPack("A",instant)) > 0, "comparator(comparator).compare(instant(b),instant(a)) has to be more than zero");
    }

    @Test
    void comparatorComparableTest() {
        Comparator<ExpectedStringPack> comparator = Expected.comparator(null);
        assertEquals(0, comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("A",instant)), "comparator(null).compare has to return zero for equals values");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant.minusMillis(1)),  new ExpectedStringPack("A",instant)) < 0, "comparator(null).compare(prev instant(a),instant(a)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("A",instant.minusMillis(1))) > 0, "comparator(null).compare(instant(a),prev instant(a)) has to be more than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("A",instant),  new ExpectedStringPack("B",instant)) < 0, "comparator(null).compare(instant(a),instant(b)) has to be less than zero");
        assertTrue(comparator.compare(new ExpectedStringPack("B",instant),  new ExpectedStringPack("A",instant)) > 0, "comparator(null).compare(instant(b),instant(a)) has to be more than zero");
    }

    static class StringPack implements Comparable<StringPack> {
        String string;
        StringPack(String string) {
            this.string = string;
        }

        @Override
        public int compareTo(@Nonnull StringPack o) {
            return string.compareTo(o.string);
        }
    }

    static class ExpectedStringPack extends StringPack implements Expected<Instant> {
        Instant instant;
        ExpectedStringPack(String string, Instant instant) {
            super(string);
            this.instant = instant;
        }
        @Nonnull
        @Override
        public Instant getTick() {
            return this.instant;
        }
    }

    Expected<Instant> expected(Instant instant) {
        return new Expected<Instant>() {
            @Nonnull
            @Override
            public Instant getTick() {
                return instant;
            }
        };
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