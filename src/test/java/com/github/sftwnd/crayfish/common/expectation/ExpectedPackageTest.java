package com.github.sftwnd.crayfish.common.expectation;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedPackageTest {

    @Test
    void packTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.pack(object, instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the element given to ExpectedPackage::pack");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the tick given to ExpectedPackage::pack");
    }

    @Test
    void packOnNullElementTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.pack(null, instant), "ExpectedPackage::pack has to throw an NPE if the element is null");
        assertEquals("ExpectedPackage::pack - element is null", exception.getMessage(), "ExpectedPackage::pack has to name the null element");
    }

    @Test
    void packOnNullTickTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.pack(object, null), "ExpectedPackage::pack has to throw an NPE at once if the tick is null");
        assertEquals("ExpectedPackage::pack - tick is null", exception.getMessage(), "ExpectedPackage::pack has to name the null tick");
    }

    @Test
    void supplyTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the element given to ExpectedPackage::supply");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the tick of the supplier given to ExpectedPackage::supply");
    }

    @Test
    void supplyOnceTest() {
        AtomicInteger calls = new AtomicInteger();
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> instant.plusSeconds(calls.incrementAndGet()));
        assertEquals(1, calls.get(), "ExpectedPackage::supply has to call the supplier at the packaging");
        assertEquals(instant.plusSeconds(1), pack.getTick(), "ExpectedPackage::getTick has to return the tick of the packaging");
        assertEquals(instant.plusSeconds(1), pack.getTick(), "ExpectedPackage::getTick has to return the same tick again");
        assertFalse(pack.toString().isEmpty(), "ExpectedPackage::toString has to return a text");
        assertEquals(0, Expected.comparator(null).compare(pack, pack), "The comparator has to compare a package with itself");
        assertTrue(pack.happened(instant.plusSeconds(1)), "The package has to have happened at its tick");
        assertEquals(1, calls.get(), "The supplier must not be called after the packaging");
    }

    @Test
    void supplyOnNullElementTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.supply(null, () -> instant), "ExpectedPackage::supply has to throw an NPE if the element is null");
        assertEquals("ExpectedPackage::supply - element is null", exception.getMessage(), "ExpectedPackage::supply has to name the null element");
    }

    @Test
    void supplyOnNullSupplierTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.supply(object, null), "ExpectedPackage::supply has to throw an NPE if the supplier is null");
        assertEquals("ExpectedPackage::supply - temporalSupplier is null", exception.getMessage(), "ExpectedPackage::supply has to name the null supplier");
    }

    @Test
    void supplyOnSupplyNullValueTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.supply(object, () -> null), "ExpectedPackage::supply has to throw an NPE at once if the supplied tick is null");
        assertEquals("ExpectedPackage::supply - temporalSupplier gave a null tick", exception.getMessage(), "ExpectedPackage::supply has to name the null tick");
    }

    @Test
    void extractTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract(object, obj -> instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the element given to ExpectedPackage::extract");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the tick of the extractor given to ExpectedPackage::extract");
    }

    @Test
    void extractFromElementTest() {
        ExpectedPackage<Instant, Instant> pack = ExpectedPackage.extract(instant, tick -> tick.plusSeconds(1));
        assertEquals(instant.plusSeconds(1), pack.getTick(), "ExpectedPackage::extract has to apply the extractor to the element");
    }

    @Test
    void extractOnceTest() {
        AtomicInteger calls = new AtomicInteger();
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract(object, obj -> instant.plusSeconds(calls.incrementAndGet()));
        assertEquals(1, calls.get(), "ExpectedPackage::extract has to call the extractor at the packaging");
        assertEquals(pack.getTick(), pack.getTick(), "ExpectedPackage::getTick has to return the same tick again");
        assertTrue(pack.happened(instant.plusSeconds(1)), "The package has to have happened at its tick");
        assertEquals(1, calls.get(), "The extractor must not be called after the packaging");
    }

    @Test
    void extractOnNullElementTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.extract(null, obj -> instant), "ExpectedPackage::extract has to throw an NPE if the element is null");
        assertEquals("ExpectedPackage::extract - element is null", exception.getMessage(), "ExpectedPackage::extract has to name the null element");
    }

    @Test
    void extractOnNullExtractorTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.extract(object, null), "ExpectedPackage::extract has to throw an NPE if the extractor is null");
        assertEquals("ExpectedPackage::extract - extractor is null", exception.getMessage(), "ExpectedPackage::extract has to name the null extractor");
    }

    @Test
    void extractOnExtractNullValueTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ExpectedPackage.extract(object, obj -> null), "ExpectedPackage::extract has to throw an NPE at once if the extracted tick is null");
        assertEquals("ExpectedPackage::extract - extractor gave a null tick", exception.getMessage(), "ExpectedPackage::extract has to name the null tick");
    }

    @Test
    void tickWithoutInstantTest() {
        LocalDateTime local = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        assertThrows(DateTimeException.class, () -> ExpectedPackage.pack(object, local), "ExpectedPackage::pack has to reject a tick without an instant at once");
        assertThrows(DateTimeException.class, () -> ExpectedPackage.supply(object, () -> local), "ExpectedPackage::supply has to reject a tick without an instant at once");
        assertThrows(DateTimeException.class, () -> ExpectedPackage.extract(object, obj -> local.toLocalDate()), "ExpectedPackage::extract has to reject a tick without an instant at once");
    }

    @Test
    void equalsTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.pack("element", instant);
        assertEquals(pack, pack, "A package has to be equal to itself");
        assertEquals(ExpectedPackage.pack("element", instant), pack, "Packages of equal elements and equal ticks have to be equal");
        assertEquals(ExpectedPackage.pack("element", instant).hashCode(), pack.hashCode(), "Equal packages have to have equal hash codes");
        assertEquals(ExpectedPackage.supply("element", () -> instant), pack, "A package of ExpectedPackage::supply has to be equal to that of ExpectedPackage::pack of the same element and tick");
        assertEquals(ExpectedPackage.extract("element", obj -> instant), pack, "A package of ExpectedPackage::extract has to be equal to that of ExpectedPackage::pack of the same element and tick");
        assertNotEquals(ExpectedPackage.pack("other", instant), pack, "Packages of different elements must not be equal");
        assertNotEquals(ExpectedPackage.pack("element", instant.plusMillis(1)), pack, "Packages of different ticks must not be equal");
        assertNotEquals(ExpectedPackage.pack("element", instant.atZone(ZoneOffset.UTC)), pack, "Packages of ticks of different types must not be equal, even of the same instant");
        assertNotEquals(pack, new OwnPackage("element", instant), "A package must not be equal to another implementation of ExpectedPackage");
        assertNotEquals(null, pack, "A package must not be equal to null");
        assertNotEquals("element", pack, "A package must not be equal to an object of another type");
    }

    @Test
    void happenedTest() {
        assertTrue(ExpectedPackage.pack(object, Instant.now().minusMillis(10)).happened(), "A package with a past tick has to have happened");
        assertFalse(ExpectedPackage.pack(object, Instant.now().plusSeconds(10)).happened(), "A package with a future tick must not have happened");
    }

    @Test
    void toStringTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract("toStringTest", obj -> instant);
        assertEquals("ExpectedPackage(" + instant + ",toStringTest)", pack.toString(), "ExpectedPackage::toString has to return the tick and the element");
    }

    @Test
    void toStringOfZonedTickTest() {
        ExpectedPackage<Object, ZonedDateTime> pack = ExpectedPackage.pack("zoned", ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Tokyo")));
        assertEquals("ExpectedPackage(" + instant + ",zoned)", pack.toString(), "ExpectedPackage::toString has to return the tick as an Instant");
        assertEquals(pack.toString(), ExpectedPackage.toString(pack), "ExpectedPackage::toString of a package has to be ExpectedPackage.toString of it");
    }

    @Test
    void toStringOfOtherPackagesTest() {
        LocalDate date = LocalDate.of(2026, 10, 19);
        assertEquals("null", ExpectedPackage.toString(null), "ExpectedPackage.toString has to return \"null\" for a null package");
        assertEquals("ExpectedPackage(2026-10-19,own)", ExpectedPackage.toString(new OwnPackage("own", date)), "ExpectedPackage.toString has to show a tick without an instant as it is");
        assertEquals("ExpectedPackage(null,own)", ExpectedPackage.toString(new OwnPackage("own", null)), "ExpectedPackage.toString has to show a null tick as null");
        assertEquals("ExpectedPackage(" + instant + ",own)", ExpectedPackage.toString(new OwnPackage("own", instant)), "ExpectedPackage.toString has to show a tick as an Instant");
    }

    // another implementation of ExpectedPackage, whose tick may have no instant and may be null
    static class OwnPackage implements ExpectedPackage<String, TemporalAccessor> {
        final String element;
        final TemporalAccessor tick;
        OwnPackage(String element, TemporalAccessor tick) {
            this.element = element;
            this.tick = tick;
        }
        @NonNull
        @Override
        public String getElement() {
            return element;
        }
        @NonNull
        @Override
        public TemporalAccessor getTick() {
            return tick;
        }
    }

    private Object object;
    private Instant instant;

    @BeforeEach
    void startUp() {
        object = new Object();
        instant = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    }

    @AfterEach
    void tearDown() {
        object = null;
        instant = null;
    }

}
