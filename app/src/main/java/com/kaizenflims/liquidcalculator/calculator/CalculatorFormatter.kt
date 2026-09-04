package com.kaizenflims.liquidcalculator.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CalculatorFormatter {
    val mathContext: MathContext = MathContext(34, RoundingMode.HALF_EVEN)
    private const val SCIENTIFIC_HIGH = 15
    private const val SCIENTIFIC_LOW = -9

    fun parse(value: String): BigDecimal =
        value.replace("−", "-").toBigDecimalOrNull() ?: BigDecimal.ZERO

    fun format(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"

        val rounded = value.round(mathContext).stripTrailingZeros()
        val exponent = rounded.precision() - rounded.scale() - 1
        if (exponent >= SCIENTIFIC_HIGH || exponent <= SCIENTIFIC_LOW) {
            val mantissa = rounded.movePointLeft(exponent).stripTrailingZeros().toPlainString()
            return "${mantissa}E${if (exponent >= 0) "+" else ""}$exponent"
        }
        return rounded.toPlainString()
    }

    fun fromDouble(value: Double): BigDecimal? {
        if (!value.isFinite()) return null
        val cleaned = if (kotlin.math.abs(value) < 1e-15) 0.0 else value
        return BigDecimal.valueOf(cleaned).round(MathContext(15, RoundingMode.HALF_EVEN))
    }
}
