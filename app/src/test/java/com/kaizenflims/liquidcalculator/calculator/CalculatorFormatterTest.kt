package com.kaizenflims.liquidcalculator.calculator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CalculatorFormatterTest {
    @Test
    fun removesUnhelpfulTrailingZeros() {
        assertEquals("12.34", CalculatorFormatter.format(BigDecimal("12.340000")))
    }

    @Test
    fun switchesToScientificNotationForVeryLargeValues() {
        assertEquals(
            "1.23456789E+19",
            CalculatorFormatter.format(BigDecimal("12345678900000000000")),
        )
    }

    @Test
    fun normalizesNegativeZero() {
        assertEquals("0", CalculatorFormatter.format(BigDecimal("-0.000")))
    }
}

