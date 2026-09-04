package com.kaizenflims.liquidcalculator.calculator

import java.math.BigDecimal

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    internal val accumulator: BigDecimal? = null,
    internal val pendingOperator: BinaryOperator? = null,
    internal val lastOperator: BinaryOperator? = null,
    internal val lastOperand: BigDecimal? = null,
    internal val replaceInput: Boolean = true,
    internal val justEvaluated: Boolean = false,
    val isError: Boolean = false,
)

data class CompletedCalculation(
    val expression: String,
    val result: String,
)

data class EngineUpdate(
    val state: CalculatorState,
    val completedCalculation: CompletedCalculation? = null,
)

