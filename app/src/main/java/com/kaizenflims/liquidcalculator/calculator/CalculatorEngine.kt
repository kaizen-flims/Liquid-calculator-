package com.kaizenflims.liquidcalculator.calculator

import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculatorEngine {
    private val mc = CalculatorFormatter.mathContext

    fun reduce(state: CalculatorState, action: CalculatorAction): EngineUpdate {
        if (action is CalculatorAction.Clear) return EngineUpdate(CalculatorState())
        val safeState = if (state.isError) CalculatorState() else state

        return when (action) {
            is CalculatorAction.Digit -> enterDigit(safeState, action.value)
            is CalculatorAction.Operator -> chooseOperator(safeState, action.value)
            is CalculatorAction.Unary -> applyUnary(safeState, action.value)
            is CalculatorAction.Constant -> enterConstant(safeState, action.value)
            is CalculatorAction.Recall -> recall(action.value)
            CalculatorAction.Decimal -> enterDecimal(safeState)
            CalculatorAction.Equals -> equals(safeState)
            CalculatorAction.ToggleSign -> toggleSign(safeState)
            CalculatorAction.Percent -> percent(safeState)
            CalculatorAction.Clear -> EngineUpdate(CalculatorState())
        }
    }

    private fun enterDigit(state: CalculatorState, digit: Int): EngineUpdate {
        require(digit in 0..9)
        val prepared = if (state.justEvaluated && state.pendingOperator == null) {
            CalculatorState()
        } else {
            state
        }
        val currentDigits = prepared.display.count(Char::isDigit)
        if (!prepared.replaceInput && currentDigits >= 32) return EngineUpdate(prepared)

        val next = when {
            prepared.replaceInput -> digit.toString()
            prepared.display == "0" -> digit.toString()
            prepared.display == "-0" -> "-$digit"
            else -> prepared.display + digit
        }
        return EngineUpdate(
            prepared.copy(
                display = next,
                replaceInput = false,
                justEvaluated = false,
                lastOperator = null,
                lastOperand = null,
            ),
        )
    }

    private fun enterDecimal(state: CalculatorState): EngineUpdate {
        val prepared = if (state.justEvaluated && state.pendingOperator == null) {
            CalculatorState()
        } else {
            state
        }
        if (!prepared.replaceInput && prepared.display.contains('.')) return EngineUpdate(prepared)
        val next = if (prepared.replaceInput) "0." else prepared.display + "."
        return EngineUpdate(
            prepared.copy(
                display = next,
                replaceInput = false,
                justEvaluated = false,
                lastOperator = null,
                lastOperand = null,
            ),
        )
    }

    private fun chooseOperator(
        state: CalculatorState,
        operator: BinaryOperator,
    ): EngineUpdate {
        val current = CalculatorFormatter.parse(state.display)
        if (state.pendingOperator != null && state.replaceInput) {
            return EngineUpdate(
                state.copy(
                    pendingOperator = operator,
                    expression = "${CalculatorFormatter.format(state.accumulator ?: current)} ${operator.symbol}",
                    justEvaluated = false,
                ),
            )
        }

        val accumulated = when {
            state.pendingOperator != null && state.accumulator != null ->
                calculate(state.accumulator, current, state.pendingOperator)
            else -> Result.success(current)
        }
        return accumulated.fold(
            onSuccess = { value ->
                val formatted = CalculatorFormatter.format(value)
                EngineUpdate(
                    state.copy(
                        display = formatted,
                        expression = "$formatted ${operator.symbol}",
                        accumulator = value,
                        pendingOperator = operator,
                        replaceInput = true,
                        justEvaluated = false,
                        lastOperator = null,
                        lastOperand = null,
                    ),
                )
            },
            onFailure = { EngineUpdate(errorState(state)) },
        )
    }

    private fun equals(state: CalculatorState): EngineUpdate {
        val current = CalculatorFormatter.parse(state.display)
        val operator: BinaryOperator
        val left: BigDecimal
        val right: BigDecimal

        if (state.pendingOperator != null) {
            operator = state.pendingOperator
            left = state.accumulator ?: current
            right = if (state.replaceInput) left else current
        } else if (state.lastOperator != null && state.lastOperand != null) {
            operator = state.lastOperator
            left = current
            right = state.lastOperand
        } else {
            return EngineUpdate(state.copy(replaceInput = true, justEvaluated = true))
        }

        return calculate(left, right, operator).fold(
            onSuccess = { result ->
                val formatted = CalculatorFormatter.format(result)
                val expression = "${CalculatorFormatter.format(left)} ${operator.symbol} ${CalculatorFormatter.format(right)}"
                EngineUpdate(
                    state = CalculatorState(
                        display = formatted,
                        expression = expression,
                        lastOperator = operator,
                        lastOperand = right,
                        replaceInput = true,
                        justEvaluated = true,
                    ),
                    completedCalculation = CompletedCalculation(expression, formatted),
                )
            },
            onFailure = { EngineUpdate(errorState(state)) },
        )
    }

    private fun toggleSign(state: CalculatorState): EngineUpdate {
        if (state.replaceInput && state.pendingOperator != null) {
            return EngineUpdate(state.copy(display = "-0", replaceInput = false))
        }
        val current = CalculatorFormatter.parse(state.display)
        val toggled = if (current.compareTo(BigDecimal.ZERO) == 0) {
            if (state.display.startsWith("-")) "0" else "-0"
        } else {
            CalculatorFormatter.format(current.negate(mc))
        }
        return EngineUpdate(
            state.copy(
                display = toggled,
                replaceInput = false,
                justEvaluated = false,
                lastOperator = null,
                lastOperand = null,
            ),
        )
    }

    private fun percent(state: CalculatorState): EngineUpdate {
        val current = CalculatorFormatter.parse(state.display)
        val percentValue = when (state.pendingOperator) {
            BinaryOperator.Add, BinaryOperator.Subtract ->
                (state.accumulator ?: BigDecimal.ZERO)
                    .multiply(current, mc)
                    .divide(BigDecimal("100"), mc)
            else -> current.divide(BigDecimal("100"), mc)
        }
        return EngineUpdate(
            state.copy(
                display = CalculatorFormatter.format(percentValue),
                replaceInput = false,
                justEvaluated = false,
                lastOperator = null,
                lastOperand = null,
            ),
        )
    }

    private fun enterConstant(
        state: CalculatorState,
        constant: CalculatorConstant,
    ): EngineUpdate {
        val value = when (constant) {
            CalculatorConstant.Pi -> BigDecimal("3.141592653589793238462643383279503")
            CalculatorConstant.Euler -> BigDecimal("2.718281828459045235360287471352662")
        }
        return EngineUpdate(
            state.copy(
                display = CalculatorFormatter.format(value),
                replaceInput = false,
                justEvaluated = false,
                lastOperator = null,
                lastOperand = null,
            ),
        )
    }

    private fun applyUnary(
        state: CalculatorState,
        operation: UnaryOperation,
    ): EngineUpdate {
        val input = CalculatorFormatter.parse(state.display)
        val result = when (operation) {
            UnaryOperation.Square -> Result.success(input.multiply(input, mc))
            UnaryOperation.Reciprocal -> {
                if (input.compareTo(BigDecimal.ZERO) == 0) Result.failure(ArithmeticException())
                else Result.success(BigDecimal.ONE.divide(input, mc))
            }
            UnaryOperation.Factorial -> factorial(input)
            UnaryOperation.SquareRoot -> doubleResult(input) { if (it < 0) Double.NaN else sqrt(it) }
            UnaryOperation.NaturalLog -> doubleResult(input) { if (it <= 0) Double.NaN else ln(it) }
            UnaryOperation.CommonLog -> doubleResult(input) { if (it <= 0) Double.NaN else log10(it) }
            UnaryOperation.Sine -> doubleResult(input) { sin(Math.toRadians(it)) }
            UnaryOperation.Cosine -> doubleResult(input) { cos(Math.toRadians(it)) }
            UnaryOperation.Tangent -> doubleResult(input) {
                val radians = Math.toRadians(it)
                if (kotlin.math.abs(cos(radians)) < 1e-14) Double.NaN else tan(radians)
            }
        }

        return result.fold(
            onSuccess = { value ->
                val formatted = CalculatorFormatter.format(value)
                val unaryExpression = when (operation) {
                    UnaryOperation.SquareRoot -> "√(${CalculatorFormatter.format(input)})"
                    UnaryOperation.Square -> "(${CalculatorFormatter.format(input)})²"
                    UnaryOperation.Factorial -> "(${CalculatorFormatter.format(input)})!"
                    else -> "${operation.label}(${CalculatorFormatter.format(input)})"
                }
                val fullExpression = if (state.pendingOperator != null && state.accumulator != null) {
                    "${CalculatorFormatter.format(state.accumulator)} ${state.pendingOperator.symbol} $unaryExpression"
                } else {
                    unaryExpression
                }
                EngineUpdate(
                    state.copy(
                        display = formatted,
                        expression = fullExpression,
                        replaceInput = state.pendingOperator == null,
                        justEvaluated = state.pendingOperator == null,
                        lastOperator = null,
                        lastOperand = null,
                    ),
                )
            },
            onFailure = { EngineUpdate(errorState(state)) },
        )
    }

    private fun recall(value: String): EngineUpdate {
        val parsed = value.toBigDecimalOrNull() ?: return EngineUpdate(CalculatorState())
        return EngineUpdate(
            CalculatorState(
                display = CalculatorFormatter.format(parsed),
                replaceInput = true,
                justEvaluated = true,
            ),
        )
    }

    private fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        operator: BinaryOperator,
    ): Result<BigDecimal> = runCatching {
        when (operator) {
            BinaryOperator.Add -> left.add(right, mc)
            BinaryOperator.Subtract -> left.subtract(right, mc)
            BinaryOperator.Multiply -> left.multiply(right, mc)
            BinaryOperator.Divide -> {
                if (right.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                left.divide(right, mc)
            }
        }
    }

    private fun factorial(input: BigDecimal): Result<BigDecimal> = runCatching {
        val normalized = input.stripTrailingZeros()
        if (normalized.scale() > 0) throw ArithmeticException("Factorial requires an integer")
        val number = normalized.intValueExact()
        if (number !in 0..170) throw ArithmeticException("Factorial range")
        var value = BigDecimal.ONE
        for (factor in 2..number) value = value.multiply(BigDecimal(factor), mc)
        value
    }

    private inline fun doubleResult(
        input: BigDecimal,
        transform: (Double) -> Double,
    ): Result<BigDecimal> = runCatching {
        CalculatorFormatter.fromDouble(transform(input.toDouble()))
            ?: throw ArithmeticException("Undefined result")
    }

    private fun errorState(previous: CalculatorState): CalculatorState =
        previous.copy(
            display = "Undefined",
            expression = "",
            accumulator = null,
            pendingOperator = null,
            lastOperator = null,
            lastOperand = null,
            replaceInput = true,
            justEvaluated = false,
            isError = true,
        )
}
