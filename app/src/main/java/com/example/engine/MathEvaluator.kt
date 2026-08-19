package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
    DEG, RAD
}

object MathEvaluator {

    val CONST_PI = Math.PI
    val CONST_E = Math.E
    val CONST_PHI = 1.618033988749895
    val CONST_TAU = 2 * Math.PI

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): Result<Double> {
        return try {
            val sanitized = sanitize(expression)
            if (sanitized.isBlank()) {
                return Result.failure(IllegalArgumentException("Expression is empty"))
            }
            val tokens = tokenize(sanitized)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)
            if (result.isNaN()) {
                Result.failure(ArithmeticException("Undefined or NaN"))
            } else if (result.isInfinite()) {
                Result.failure(ArithmeticException(if (result > 0) "Infinity" else "-Infinity"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun evaluateForX(expression: String, x: Double, angleMode: AngleMode = AngleMode.RAD): Double {
        return try {
            val formattedX = if (x < 0) "($x)" else "$x"
            // Replace variable x/X with the value, avoiding function names like exp
            val replaced = expression.replace(Regex("(?<![a-zA-Z])([xX])(?![a-zA-Z])"), formattedX)
            val eval = evaluate(replaced, angleMode)
            eval.getOrNull() ?: Double.NaN
        } catch (e: Exception) {
            Double.NaN
        }
    }

    fun formatResult(value: Double, maxDecimals: Int = 8): String {
        if (value.isNaN()) return "NaN"
        if (value == Double.POSITIVE_INFINITY) return "∞"
        if (value == Double.NEGATIVE_INFINITY) return "-∞"

        val absVal = abs(value)
        if (absVal < 1e-13) return "0"

        if (absVal >= 1e12 || absVal < 1e-6) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value).replace("E", " × 10^")
        }

        val pattern = buildString {
            append("0.")
            repeat(maxDecimals) { append("#") }
        }
        val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val formatted = df.format(value)
        return if (formatted == "-0" || formatted == "-0.0") "0" else formatted
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("φ", "phi")
            .replace("τ", "tau")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("e^", "exp")
            .replace("mod", "%")
            .trim()
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            // Numbers (including decimal point and scientific notation e.g., 1.5e-3)
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                var hasDot = false
                while (i < len && (expr[i].isDigit() || (!hasDot && expr[i] == '.'))) {
                    if (expr[i] == '.') hasDot = true
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            // Identifiers / Functions / Constants
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < len && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString().lowercase())
                continue
            }

            // Operators & Parentheses
            if (c in "+-*/%^()!,") {
                tokens.add(c.toString())
                i++
                continue
            }

            i++
        }

        // Handle implicit multiplication: e.g. 2pi -> 2 * pi, (2)(3) -> (2) * (3), 3(4) -> 3 * (4), 2sin(x) -> 2 * sin(x)
        val expanded = mutableListOf<String>()
        for (idx in tokens.indices) {
            val curr = tokens[idx]
            expanded.add(curr)
            if (idx + 1 < tokens.size) {
                val next = tokens[idx + 1]
                val currIsNumber = curr.toDoubleOrNull() != null || isConstant(curr) || curr == ")" || curr == "!"
                val nextIsFunctionOrConstOrNumber = next.toDoubleOrNull() != null || isConstant(next) || isFunction(next) || next == "("
                if (currIsNumber && nextIsFunctionOrConstOrNumber) {
                    expanded.add("*")
                }
            }
        }

        // Handle unary minus: replace unary '-' with 'u-'
        val normalized = mutableListOf<String>()
        for (idx in expanded.indices) {
            val curr = expanded[idx]
            if (curr == "-") {
                val isUnary = idx == 0 || expanded[idx - 1] in listOf("(", "+", "-", "*", "/", "%", "^", ",") || isFunction(expanded[idx - 1])
                if (isUnary) {
                    normalized.add("u-")
                } else {
                    normalized.add(curr)
                }
            } else if (curr == "+") {
                val isUnary = idx == 0 || expanded[idx - 1] in listOf("(", "+", "-", "*", "/", "%", "^", ",") || isFunction(expanded[idx - 1])
                if (!isUnary) {
                    normalized.add(curr)
                }
            } else {
                normalized.add(curr)
            }
        }

        return normalized
    }

    private fun isFunction(token: String): Boolean {
        return token in listOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "ln", "log", "log10", "log2",
            "sqrt", "cbrt", "abs", "exp", "floor", "ceil", "round"
        )
    }

    private fun isConstant(token: String): Boolean {
        return token in listOf("pi", "e", "phi", "tau")
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/", "%" -> 2
            "u-" -> 3
            "^" -> 4
            "!" -> 5
            else -> 0
        }
    }

    private fun isRightAssociative(op: String): Boolean {
        return op == "^" || op == "u-"
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null || isConstant(token) -> {
                    output.add(token)
                }
                isFunction(token) -> {
                    stack.addLast(token)
                }
                token == "(" -> {
                    stack.addLast(token)
                }
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() == "(") {
                        stack.removeLast()
                    }
                    if (stack.isNotEmpty() && isFunction(stack.last())) {
                        output.add(stack.removeLast())
                    }
                }
                token == "!" -> {
                    output.add(token)
                }
                else -> { // Operator
                    while (stack.isNotEmpty() && stack.last() != "(" &&
                        (precedence(stack.last()) > precedence(token) ||
                                (precedence(stack.last()) == precedence(token) && !isRightAssociative(token)))
                    ) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, angleMode: AngleMode): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            when {
                token == "pi" -> stack.addLast(CONST_PI)
                token == "e" -> stack.addLast(CONST_E)
                token == "phi" -> stack.addLast(CONST_PHI)
                token == "tau" -> stack.addLast(CONST_TAU)
                token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
                token == "u-" -> {
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand for negation")
                    stack.addLast(-a)
                }
                token == "!" -> {
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand for factorial")
                    stack.addLast(factorial(a))
                }
                isFunction(token) -> {
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand for function $token")
                    val res = applyFunction(token, a, angleMode)
                    stack.addLast(res)
                }
                token in listOf("+", "-", "*", "/", "%", "^") -> {
                    val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                    val res = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (b == 0.0) throw ArithmeticException("Division by zero")
                            a / b
                        }
                        "%" -> a % b
                        "^" -> a.pow(b)
                        else -> 0.0
                    }
                    stack.addLast(res)
                }
            }
        }

        return stack.lastOrNull() ?: throw IllegalArgumentException("Invalid expression")
    }

    private fun applyFunction(fn: String, x: Double, angleMode: AngleMode): Double {
        return when (fn) {
            "sin" -> {
                if (angleMode == AngleMode.DEG) {
                    val norm = ((x % 360.0) + 360.0) % 360.0
                    when {
                        norm == 0.0 || norm == 180.0 || norm == 360.0 -> 0.0
                        norm == 30.0 || norm == 150.0 -> 0.5
                        norm == 210.0 || norm == 330.0 -> -0.5
                        norm == 90.0 -> 1.0
                        norm == 270.0 -> -1.0
                        norm == 45.0 || norm == 135.0 -> sqrt(2.0) / 2.0
                        norm == 225.0 || norm == 315.0 -> -sqrt(2.0) / 2.0
                        norm == 60.0 || norm == 120.0 -> sqrt(3.0) / 2.0
                        norm == 240.0 || norm == 300.0 -> -sqrt(3.0) / 2.0
                        else -> {
                            val res = sin(Math.toRadians(x))
                            if (abs(res) < 1e-15) 0.0 else res
                        }
                    }
                } else {
                    val res = sin(x)
                    if (abs(res) < 1e-15) 0.0 else res
                }
            }
            "cos" -> {
                if (angleMode == AngleMode.DEG) {
                    val norm = ((x % 360.0) + 360.0) % 360.0
                    when {
                        norm == 90.0 || norm == 270.0 -> 0.0
                        norm == 0.0 || norm == 360.0 -> 1.0
                        norm == 180.0 -> -1.0
                        norm == 60.0 || norm == 300.0 -> 0.5
                        norm == 120.0 || norm == 240.0 -> -0.5
                        norm == 45.0 || norm == 315.0 -> sqrt(2.0) / 2.0
                        norm == 135.0 || norm == 225.0 -> -sqrt(2.0) / 2.0
                        norm == 30.0 || norm == 330.0 -> sqrt(3.0) / 2.0
                        norm == 150.0 || norm == 210.0 -> -sqrt(3.0) / 2.0
                        else -> {
                            val res = cos(Math.toRadians(x))
                            if (abs(res) < 1e-15) 0.0 else res
                        }
                    }
                } else {
                    val res = cos(x)
                    if (abs(res) < 1e-15) 0.0 else res
                }
            }
            "tan" -> {
                if (angleMode == AngleMode.DEG) {
                    val norm = ((x % 360.0) + 360.0) % 360.0
                    when {
                        norm == 90.0 || norm == 270.0 -> throw ArithmeticException("Tangent undefined at ${x.toInt()}°")
                        norm == 0.0 || norm == 180.0 || norm == 360.0 -> 0.0
                        norm == 45.0 || norm == 225.0 -> 1.0
                        norm == 135.0 || norm == 315.0 -> -1.0
                        else -> {
                            val res = tan(Math.toRadians(x))
                            if (abs(res) < 1e-15) 0.0 else res
                        }
                    }
                } else {
                    val cosVal = cos(x)
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Tangent undefined at this angle")
                    val res = tan(x)
                    if (abs(res) < 1e-15) 0.0 else res
                }
            }
            "asin" -> {
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: sin⁻¹(x) defined for -1 ≤ x ≤ 1")
                val radRes = asin(x)
                if (angleMode == AngleMode.DEG) {
                    val degRes = Math.toDegrees(radRes)
                    if (abs(degRes - round(degRes)) < 1e-12) round(degRes) else degRes
                } else {
                    radRes
                }
            }
            "acos" -> {
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: cos⁻¹(x) defined for -1 ≤ x ≤ 1")
                val radRes = acos(x)
                if (angleMode == AngleMode.DEG) {
                    val degRes = Math.toDegrees(radRes)
                    if (abs(degRes - round(degRes)) < 1e-12) round(degRes) else degRes
                } else {
                    radRes
                }
            }
            "atan" -> {
                val radRes = atan(x)
                if (angleMode == AngleMode.DEG) {
                    val degRes = Math.toDegrees(radRes)
                    if (abs(degRes - round(degRes)) < 1e-12) round(degRes) else degRes
                } else {
                    radRes
                }
            }
            "sinh" -> sinh(x)
            "cosh" -> cosh(x)
            "tanh" -> tanh(x)
            "ln" -> {
                if (x <= 0) throw ArithmeticException("ln of non-positive number")
                ln(x)
            }
            "log", "log10" -> {
                if (x <= 0) throw ArithmeticException("log of non-positive number")
                log10(x)
            }
            "log2" -> {
                if (x <= 0) throw ArithmeticException("log2 of non-positive number")
                log2(x)
            }
            "sqrt" -> {
                if (x < 0) throw ArithmeticException("Square root of negative number")
                sqrt(x)
            }
            "cbrt" -> cbrt(x)
            "abs" -> abs(x)
            "exp" -> exp(x)
            "floor" -> floor(x)
            "ceil" -> ceil(x)
            "round" -> round(x)
            else -> throw IllegalArgumentException("Unknown function $fn")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw ArithmeticException("Factorial is only defined for non-negative integers")
        if (n > 170) return Double.POSITIVE_INFINITY
        var res = 1.0
        val intN = n.toInt()
        for (i in 2..intN) {
            res *= i
        }
        return res
    }
}
