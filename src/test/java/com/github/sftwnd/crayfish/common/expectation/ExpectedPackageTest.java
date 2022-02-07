package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedPackageTest {

    @Test
    void packETest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.pack(object, instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the same object as in .pack method attribute");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the same object as in .pack method attribute");
    }

    @Test
    void supplyTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.supply(object, () -> instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the same object as in .supply method attribute");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the same object as in .supply method attribute");
    }

    @Test
    void extractTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract(object, obj -> instant);
        assertSame(object, pack.getElement(), "ExpectedPackage::getElement has to return the same object as in .extract method attribute");
        assertSame(instant, pack.getTick(), "ExpectedPackage::getTick has to return the same object as in .extract method attribute");
    }

    @Test
    void extractOnNullElementTest() {
        assertThrows(NullPointerException.class, () -> ExpectedPackage.extract(null, obj -> instant), "ExpectedPackage::extract has to throws NPE if object is null");
    }

    @Test
    void extractOnNullExtractorTest() {
        assertThrows(NullPointerException.class, () -> ExpectedPackage.extract(object, null), "ExpectedPackage::extract has to throws NPE if extractor is null");
    }

    @Test
    void extractOnExtractNullValueTest() {
        ExpectedPackage<Object,Instant> pack = ExpectedPackage.extract(object, obj -> null);
        assertThrows(NullPointerException.class, pack::getTick, "ExpectedPackage::extract::getTick has to throws NPE if generated tick is null");
    }

    @Test
    void toStringTest() {
        ExpectedPackage<Object, Instant> pack = ExpectedPackage.extract("toStringTest", obj -> instant);
        Pattern pattern = Pattern.compile("ExpectedPackage\\(.*,"+pack.getElement()+"\\)");
        assertTrue(pattern.matcher(pack.toString()).matches(), "ExpectedPackage::toString return wrong result: "+pack);
    }

    Object object;
    Instant instant;

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