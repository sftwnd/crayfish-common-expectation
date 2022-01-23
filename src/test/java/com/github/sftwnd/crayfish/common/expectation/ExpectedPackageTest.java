package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertSame;

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