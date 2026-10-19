package com.github.sftwnd.crayfish.common.expectation.test;

import com.github.sftwnd.crayfish.common.expectation.ExpectedPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertEquals("ExpectedPackage::supply - element is null", exception.getMessage(), "ExpectedPackage::pack has to name the null element");
    }

    @Test
    void packOnNullTickTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.pack(object, null);
        NullPointerException exception = assertThrows(NullPointerException.class, pack::getTick, "ExpectedPackage::pack::getTick has to throw an NPE if the tick is null");
        assertEquals("ExpectedPackage::getTick - result is null", exception.getMessage(), "ExpectedPackage::pack::getTick has to name the null tick");
    }

    @Test
    void supplyTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the element given to ExpectedPackage::supply");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the tick of the supplier given to ExpectedPackage::supply");
    }

    @Test
    void supplyOnEveryGetTickTest() {
        AtomicInteger calls = new AtomicInteger();
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> instant.plusSeconds(calls.incrementAndGet()));
        assertEquals(0, calls.get(), "ExpectedPackage::supply must not call the supplier before ExpectedPackage::getTick");
        assertEquals(instant.plusSeconds(1), pack.getTick(), "ExpectedPackage::getTick has to return the tick of the first call of the supplier");
        assertEquals(instant.plusSeconds(2), pack.getTick(), "ExpectedPackage::getTick has to call the supplier again");
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
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> null);
        NullPointerException exception = assertThrows(NullPointerException.class, pack::getTick, "ExpectedPackage::supply::getTick has to throw an NPE if the supplied tick is null");
        assertEquals("ExpectedPackage::getTick - result is null", exception.getMessage(), "ExpectedPackage::supply::getTick has to name the null tick");
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
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract(object, obj -> null);
        NullPointerException exception = assertThrows(NullPointerException.class, pack::getTick, "ExpectedPackage::extract::getTick has to throw an NPE if the extracted tick is null");
        assertEquals("ExpectedPackage::getTick - result is null", exception.getMessage(), "ExpectedPackage::extract::getTick has to name the null tick");
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
