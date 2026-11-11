package com.quant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.quant.risk.RiskChecker;

import org.junit.jupiter.api.Test;

class RiskCheckerTest {

    @Test
    void limits() {
        var r = new RiskChecker();
        assertEquals("PASS", r.check("BUY", 100, 100));
        assertNotEquals("PASS", r.check("BUY", 100, 101));
        assertNotEquals("PASS", r.check("HOLD", 100, 1));
        assertNotEquals("PASS", r.check("BUY", Double.NaN, 1));
    }
}
