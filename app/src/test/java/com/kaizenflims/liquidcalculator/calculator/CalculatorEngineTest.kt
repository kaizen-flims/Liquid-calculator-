package com.kaizenflims.liquidcalculator.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {
    private val engine = CalculatorEngine()

    @Test
    fun decimalArithmeticDoesNotLeakBinaryFloatingPointArtifacts() {
        val result = run(
            CalculatorAction.Digit(0),
            CalculatorAction.Decimal,
            CalculatorAction.Digit(1),
            CalculatorAction.Operator(BinaryOperator.Add),
            CalculatorAction.Digit(0),
            CalculatorAction.Decimal,
            CalculatorAction.Digit(2),
            CalculatorAction.Equals,
        )

        assertEquals("0.3", result.display)
    }

    @Test
    fun chainedOperationsResolveInCalculatorOrder() {
        val result = run(
            CalculatorAction.Digit(2),
            CalculatorAction.Operator(BinaryOperator.Add),
            CalculatorAction.Digit(3),
            CalculatorAction.Operator(BinaryOperator.Multiply),
            CalculatorAction.Digit(4),
            CalculatorAction.Equals,
        )

        assertEquals("20", result.display)
    }

    @Test
    fun repeatedEqualsReusesLastOperatorAndOperand() {
        var state = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Operator(BinaryOperator.Add),
            CalculatorAction.Digit(2),
            CalculatorAction.Equals,
        )
        state = engine.reduce(state, CalculatorAction.Equals).state
        state = engine.reduce(state, CalculatorAction.Equals).state

        assertEquals("11", state.display)
    }

    @Test
    fun additivePercentageUsesAccumulatorAsItsBase() {
        val result = run(
            CalculatorAction.Digit(2),
            CalculatorAction.Digit(0),
            CalculatorAction.Digit(0),
            CalculatorAction.Operator(BinaryOperator.Add),
            CalculatorAction.Digit(1),
            CalculatorAction.Digit(0),
            CalculatorAction.Percent,
            CalculatorAction.Equals,
        )

        assertEquals("220", result.display)
    }

    @Test
    fun multiplicativePercentageBecomesFraction() {
        val result = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Digit(0),
            CalculatorAction.Operator(BinaryOperator.Multiply),
            CalculatorAction.Digit(1),
            CalculatorAction.Digit(0),
            CalculatorAction.Percent,
            CalculatorAction.Equals,
        )

        assertEquals("5", result.display)
    }

    @Test
    fun signToggleCanBeginANegativeOperand() {
        val result = run(
            CalculatorAction.Digit(8),
            CalculatorAction.Operator(BinaryOperator.Add),
            CalculatorAction.ToggleSign,
            CalculatorAction.Digit(3),
            CalculatorAction.Equals,
        )

        assertEquals("5", result.display)
    }

    @Test
    fun divisionByZeroProducesRecoverableError() {
        var state = run(
            CalculatorAction.Digit(9),
            CalculatorAction.Operator(BinaryOperator.Divide),
            CalculatorAction.Digit(0),
            CalculatorAction.Equals,
        )

        assertTrue(state.isError)
        assertEquals("Undefined", state.display)

        state = engine.reduce(state, CalculatorAction.Digit(4)).state
        assertFalse(state.isError)
        assertEquals("4", state.display)
    }

    @Test
    fun scientificUnaryOperationsAreFunctional() {
        val result = run(
            CalculatorAction.Digit(9),
            CalculatorAction.Unary(UnaryOperation.SquareRoot),
        )
        assertEquals("3", result.display)
    }

    @Test
    fun trigonometryIsRoundedForHumanReadableOutput() {
        val result = run(
            CalculatorAction.Digit(3),
            CalculatorAction.Digit(0),
            CalculatorAction.Unary(UnaryOperation.Sine),
        )

        assertEquals("0.5", result.display)
    }

    @Test
    fun calculationCompletionIsEmittedForHistory() {
        var state = CalculatorState()
        state = engine.reduce(state, CalculatorAction.Digit(7)).state
        state = engine.reduce(state, CalculatorAction.Operator(BinaryOperator.Subtract)).state
        state = engine.reduce(state, CalculatorAction.Digit(2)).state
        val update = engine.reduce(state, CalculatorAction.Equals)

        assertEquals("7 − 2", update.completedCalculation?.expression)
        assertEquals("5", update.completedCalculation?.result)
    }

    private fun run(vararg actions: CalculatorAction): CalculatorState {
        var state = CalculatorState()
        actions.forEach { state = engine.reduce(state, it).state }
        return state
    }
}
