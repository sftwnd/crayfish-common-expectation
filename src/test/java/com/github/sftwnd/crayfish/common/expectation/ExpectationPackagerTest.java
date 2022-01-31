package com.github.sftwnd.crayfish.common.expectation;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpectationPackagerTest {

    private static class Check {
        Instant instant;
        Check(Instant instant) {
            this.instant = instant;
        }
    }

    @Test
    void applyTest() {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Check check = new Check(instant);
        ExpectationPackager<Check, Instant> packager = new ExpectationPackager<>(c -> c.instant);
        assertDoesNotThrow(() -> packager.apply(check), "ExpectationPackager::apply hasn't got to throws Exception");
        assertEquals(instant, packager.apply(check).getTick(), "ExpectationPackager::apply.getTick() has to return right instant");
    }
}