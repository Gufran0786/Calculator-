package com.example

import com.example.engine.AngleMode
import com.example.engine.MathEvaluator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun degree_trigonometry_isAccurate() {
        // sin(30) = 0.5
        val sin30 = MathEvaluator.evaluate("sin(30)", AngleMode.DEG).getOrNull()
        assertEquals(0.5, sin30 ?: 0.0, 1e-9)

        // cos(60) = 0.5
        val cos60 = MathEvaluator.evaluate("cos(60)", AngleMode.DEG).getOrNull()
        assertEquals(0.5, cos60 ?: 0.0, 1e-9)

        // tan(45) = 1.0
        val tan45 = MathEvaluator.evaluate("tan(45)", AngleMode.DEG).getOrNull()
        assertEquals(1.0, tan45 ?: 0.0, 1e-9)

        // sin(90) = 1.0, cos(90) = 0.0
        val sin90 = MathEvaluator.evaluate("sin(90)", AngleMode.DEG).getOrNull()
        assertEquals(1.0, sin90 ?: 0.0, 1e-9)

        val cos90 = MathEvaluator.evaluate("cos(90)", AngleMode.DEG).getOrNull()
        assertEquals(0.0, cos90 ?: 1.0, 1e-9)

        // Inverse trig in DEG
        val asin05 = MathEvaluator.evaluate("asin(0.5)", AngleMode.DEG).getOrNull()
        assertEquals(30.0, asin05 ?: 0.0, 1e-9)

        val acos05 = MathEvaluator.evaluate("acos(0.5)", AngleMode.DEG).getOrNull()
        assertEquals(60.0, acos05 ?: 0.0, 1e-9)

        val atan1 = MathEvaluator.evaluate("atan(1)", AngleMode.DEG).getOrNull()
        assertEquals(45.0, atan1 ?: 0.0, 1e-9)
    }

    @Test
    fun radian_trigonometry_isAccurate() {
        // sin(pi / 2) = 1.0
        val sinPiOver2 = MathEvaluator.evaluate("sin(pi / 2)", AngleMode.RAD).getOrNull()
        assertEquals(1.0, sinPiOver2 ?: 0.0, 1e-9)

        // cos(pi) = -1.0
        val cosPi = MathEvaluator.evaluate("cos(pi)", AngleMode.RAD).getOrNull()
        assertEquals(-1.0, cosPi ?: 0.0, 1e-9)

        // tan(pi / 4) = 1.0
        val tanPiOver4 = MathEvaluator.evaluate("tan(pi / 4)", AngleMode.RAD).getOrNull()
        assertEquals(1.0, tanPiOver4 ?: 0.0, 1e-9)

        // Inverse trig in RAD: asin(1) = pi / 2
        val asin1 = MathEvaluator.evaluate("asin(1)", AngleMode.RAD).getOrNull()
        assertEquals(Math.PI / 2, asin1 ?: 0.0, 1e-9)
    }

    @Test
    fun solve_linear_equations() {
        // 2x + 5 = 15 => x = 5
        val sol1 = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("2x + 5 = 15", "x", null)
        val val1 = sol1.variableValues.firstOrNull { it.variableName == "x" }?.value
        assertEquals("5", val1)

        // 5x - 8 = 2x + 16 => 3x = 24 => x = 8
        val sol2 = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("5x - 8 = 2x + 16", "x", null)
        val val2 = sol2.variableValues.firstOrNull { it.variableName == "x" }?.value
        assertEquals("8", val2)

        // 3(2x - 4) = 18 => 6x - 12 = 18 => 6x = 30 => x = 5
        val sol3 = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("3(2x - 4) = 18", "x", null)
        val val3 = sol3.variableValues.firstOrNull { it.variableName == "x" }?.value
        assertEquals("5", val3)
    }

    @Test
    fun solve_quadratic_equations() {
        // x^2 - 5x + 6 = 0 => x1 = 3, x2 = 2
        val sol1 = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("x^2 - 5x + 6 = 0", "x", null)
        val values = sol1.variableValues.map { it.value }.toSet()
        assertTrue(values.contains("3") && values.contains("2"))

        // x² - 4 = 0 => x1 = 2, x2 = -2
        val sol2 = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("x² - 4 = 0", "x", null)
        val values2 = sol2.variableValues.map { it.value }.toSet()
        assertTrue(values2.contains("2") && values2.contains("-2"))
    }

    @Test
    fun solve_system_of_equations() {
        // 2x + y = 7, x - y = 2 => x = 3, y = 1
        val sol = com.example.engine.GeminiMathSolver.solveOfflineAlgebra("2x + 1y = 7, 1x - 1y = 2", "x", null)
        val xVal = sol.variableValues.firstOrNull { it.variableName == "x" }?.value
        val yVal = sol.variableValues.firstOrNull { it.variableName == "y" }?.value
        assertEquals("3", xVal)
        assertEquals("1", yVal)
    }
}
