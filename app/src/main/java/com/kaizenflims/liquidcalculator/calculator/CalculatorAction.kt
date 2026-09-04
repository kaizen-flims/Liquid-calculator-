package com.kaizenflims.liquidcalculator.calculator

sealed interface CalculatorAction {
    data class Digit(val value: Int) : CalculatorAction
    data class Operator(val value: BinaryOperator) : CalculatorAction
    data class Unary(val value: UnaryOperation) : CalculatorAction
    data class Constant(val value: CalculatorConstant) : CalculatorAction
    data class Recall(val value: String) : CalculatorAction

    data object Decimal : CalculatorAction
    data object Equals : CalculatorAction
    data object Clear : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percent : CalculatorAction
}

enum class BinaryOperator(val symbol: String) {
    Add("+"),
    Subtract("−"),
    Multiply("×"),
    Divide("÷"),
}

enum class UnaryOperation(val label: String) {
    Sine("sin"),
    Cosine("cos"),
    Tangent("tan"),
    NaturalLog("ln"),
    CommonLog("log"),
    SquareRoot("√"),
    Square("x²"),
    Reciprocal("1/x"),
    Factorial("x!"),
}

enum class CalculatorConstant {
    Pi,
    Euler,
}

