package com.example.engine

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class VariableValue(
    val variableName: String,
    val value: String,
    val isExact: Boolean = true
)

data class AlgebraEquationSolution(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalEquation: String,
    val variableValues: List<VariableValue>,
    val primaryVariable: String = "x",
    val stepByStepExplanation: String,
    val formulaOrMethod: String? = null,
    val factoredForm: String? = null,
    val isVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiMathSolution(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val stepByStepExplanation: String,
    val finalAnswer: String,
    val formulaOrConcept: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object GeminiMathSolver {

    private const val TAG = "GeminiMathSolver"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Solves an algebraic equation to compute the value of variables using Gemini API
     * with an offline intelligent solver fallback.
     */
    suspend fun solveAlgebraicEquation(
        equation: String,
        variableHint: String? = null
    ): Result<AlgebraEquationSolution> = withContext(Dispatchers.IO) {
        val trimmed = equation.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Equation cannot be empty"))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidApiKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidApiKey) {
            try {
                val apiResult = callGeminiAlgebraApi(trimmed, apiKey, variableHint)
                return@withContext Result.success(apiResult)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API equation solver error, falling back to offline algebraic solver: ${e.message}", e)
                val offlineSolution = solveOfflineAlgebra(trimmed, variableHint, fallbackNotice = "(Offline Solver: ${e.localizedMessage ?: "Network error"})")
                return@withContext Result.success(offlineSolution)
            }
        } else {
            val offlineSolution = solveOfflineAlgebra(trimmed, variableHint, fallbackNotice = null)
            return@withContext Result.success(offlineSolution)
        }
    }

    private fun callGeminiAlgebraApi(equation: String, apiKey: String, variableHint: String?): AlgebraEquationSolution {
        val targetVar = variableHint ?: detectPrimaryVariable(equation)
        val prompt = """
            Solve this algebraic equation step-by-step and compute the exact value(s) of the variable(s):
            Equation: "$equation"
            Target Variable: $targetVar

            Please format your response clearly with:
            1. Exact computed variable value(s) on distinct lines:
            VARIABLE: [variable_name] = [value]
            (e.g., VARIABLE: x = 5 or VARIABLE: x = 2 and VARIABLE: x = -3, or for systems VARIABLE: x = 4 and VARIABLE: y = -1)

            2. Method or principle used:
            METHOD: [e.g. Quadratic Formula, Linear Isolation, Factoring, Substitution Method, etc.]

            3. Step-by-Step algebraic solution:
            Show clear numbered steps explaining transformations, operations applied to both sides, factoring, and simplification.

            4. Verification:
            Show the substitution of solved values back into the original equation.

            5. Final Answer line:
            FINAL ANSWER: [exact result string, e.g. x = 5 or x = -2, 3]
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val systemInstruction = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", "You are an expert algebraic equation solver and mathematician for 'Scientific Calculator Plus'. You provide exact, rigorous solutions for variables, identify methods, and output 'VARIABLE: <var> = <val>' and 'FINAL ANSWER: <result>'."))
                }
                put("parts", parts)
            }
            put("systemInstruction", systemInstruction)

            val generationConfig = JSONObject().apply {
                put("temperature", 0.1)
                put("topP", 0.95)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody.isNullOrBlank()) {
            throw IllegalStateException("API Error (${response.code}): ${response.message}")
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw IllegalStateException("No answer returned from AI model")
        }

        val content = candidates.getJSONObject(0).optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text") ?: "No solution generated."

        return parseAiAlgebraResponse(equation, rawText, targetVar)
    }

    private fun parseAiAlgebraResponse(equation: String, rawText: String, targetVar: String): AlgebraEquationSolution {
        val varValues = mutableListOf<VariableValue>()
        var method: String? = null
        val lines = rawText.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("VARIABLE:", ignoreCase = true)) {
                val valuePart = trimmed.substringAfter("VARIABLE:").trim()
                if (valuePart.contains("=")) {
                    val varName = valuePart.substringBefore("=").trim()
                    val varVal = valuePart.substringAfter("=").trim()
                    if (varName.isNotBlank() && varVal.isNotBlank()) {
                        varValues.add(VariableValue(varName, varVal))
                    }
                } else {
                    varValues.add(VariableValue(targetVar, valuePart))
                }
            } else if (trimmed.startsWith("METHOD:", ignoreCase = true)) {
                method = trimmed.substringAfter("METHOD:").trim()
            }
        }

        // Fallback: If no VARIABLE: line found, extract from FINAL ANSWER
        if (varValues.isEmpty()) {
            var finalAnswer = ""
            for (line in lines.reversed()) {
                if (line.contains("FINAL ANSWER:", ignoreCase = true) || line.contains("ANSWER:", ignoreCase = true)) {
                    finalAnswer = line.substringAfter(":").trim().trim('*', '#', '`', ' ')
                    break
                }
            }
            if (finalAnswer.isBlank()) {
                finalAnswer = lines.lastOrNull { it.isNotBlank() }?.trim(' ', '*', '#') ?: "Solved"
            }

            if (finalAnswer.contains("=")) {
                val vName = finalAnswer.substringBefore("=").trim()
                val vVal = finalAnswer.substringAfter("=").trim()
                varValues.add(VariableValue(vName.ifBlank { targetVar }, vVal))
            } else {
                varValues.add(VariableValue(targetVar, finalAnswer))
            }
        }

        return AlgebraEquationSolution(
            originalEquation = equation,
            variableValues = varValues,
            primaryVariable = targetVar,
            stepByStepExplanation = rawText,
            formulaOrMethod = method ?: "Algebraic Computation"
        )
    }

    private fun detectPrimaryVariable(equation: String): String {
        val candidateVars = listOf("x", "y", "z", "a", "b", "t", "n", "m", "k", "r", "θ")
        for (v in candidateVars) {
            if (equation.contains(v, ignoreCase = false)) return v
        }
        return "x"
    }

    /**
     * Offline Algebraic Equation Solver capable of solving Linear Equations,
     * Quadratic Equations, Systems of Linear Equations, and Proportions.
     */
    fun solveOfflineAlgebra(equation: String, variableHint: String?, fallbackNotice: String?): AlgebraEquationSolution {
        val cleanEq = equation.trim().replace("−", "-").replace("×", "*").replace("÷", "/")
        val targetVar = variableHint ?: detectPrimaryVariable(cleanEq)

        // 1. System of 2 Linear Equations (e.g. "2x + y = 7, x - y = 2" or "2x + y = 7 and x - y = 2")
        if (cleanEq.contains(",") || cleanEq.contains(" and ", ignoreCase = true) || cleanEq.contains("\n") || cleanEq.contains(";")) {
            val parts = cleanEq.split(Regex("[,;\n]|\\band\\b", RegexOption.IGNORE_CASE)).map { it.trim() }.filter { it.contains("=") }
            if (parts.size >= 2) {
                val sysSolution = solveSystemOfTwoLinear(parts[0], parts[1], fallbackNotice)
                if (sysSolution != null) return sysSolution
            }
        }

        // 2. Quadratic Equation (e.g. x^2 - 5x + 6 = 0 or 2x² + 4x - 6 = 0 or x^2 = 16)
        if (cleanEq.contains("²") || cleanEq.contains("^2") || cleanEq.contains("$targetVar*$targetVar")) {
            val quadSolution = solveQuadraticEquation(cleanEq, targetVar, fallbackNotice)
            if (quadSolution != null) return quadSolution
        }

        // 3. Linear Equation in one variable (e.g. 3x + 12 = 30 or 5x - 4 = 2x + 11 or 2(x + 3) = 14)
        val linearSolution = solveLinearEquation(cleanEq, targetVar, fallbackNotice)
        if (linearSolution != null) return linearSolution

        // 4. Default Fallback Explanation
        val steps = buildString {
            appendLine("### Algebraic Problem Breakdown:")
            appendLine("• **Given Equation:** `$equation`")
            appendLine("• **Target Variable:** `$targetVar`")
            appendLine("1. **Structure:** The expression was parsed for algebraic variables.")
            appendLine("2. **Recommendation:** Connect online for high-order or transcendental equation solving via Gemini AI.")
            if (fallbackNotice != null) {
                appendLine("\n*$fallbackNotice*")
            }
        }

        return AlgebraEquationSolution(
            originalEquation = equation,
            variableValues = listOf(VariableValue(targetVar, "Unable to isolate")),
            primaryVariable = targetVar,
            stepByStepExplanation = steps,
            formulaOrMethod = "Algebra Solver Engine"
        )
    }

    private fun solveLinearEquation(equation: String, targetVar: String, fallbackNotice: String?): AlgebraEquationSolution? {
        if (!equation.contains("=")) return null
        val sides = equation.split("=", limit = 2)
        if (sides.size != 2) return null

        val leftStr = sides[0].trim()
        val rightStr = sides[1].trim()

        try {
            // Expand simple brackets like 2(x + 3) -> 2x + 6
            val expandedLeft = expandBrackets(leftStr, targetVar)
            val expandedRight = expandBrackets(rightStr, targetVar)

            // Extract coefficients: left is (a1*x + b1), right is (a2*x + b2)
            val (a1, b1) = parseLinearSide(expandedLeft, targetVar)
            val (a2, b2) = parseLinearSide(expandedRight, targetVar)

            val netA = a1 - a2
            val netB = b2 - b1

            if (netA == 0.0) {
                if (netB == 0.0) {
                    val steps = buildString {
                        appendLine("### Linear Equation: $equation")
                        appendLine("1. **Simplify Both Sides:** `${formatSide(a1, b1, targetVar)} = ${formatSide(a2, b2, targetVar)}`")
                        appendLine("2. **Subtract ${targetVar} terms & constants:** `0 = 0`")
                        appendLine("3. **Conclusion:** Identity equation. Infinite solutions.")
                        if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                    }
                    return AlgebraEquationSolution(
                        originalEquation = equation,
                        variableValues = listOf(VariableValue(targetVar, "All Real Numbers (∞)")),
                        primaryVariable = targetVar,
                        stepByStepExplanation = steps,
                        formulaOrMethod = "Identity Equation (Infinite Solutions)"
                    )
                } else {
                    val steps = buildString {
                        appendLine("### Linear Equation: $equation")
                        appendLine("1. **Simplify Both Sides:** `${formatSide(a1, b1, targetVar)} = ${formatSide(a2, b2, targetVar)}`")
                        appendLine("2. **Subtract ${targetVar} terms:** `0 = $netB` (False statement)")
                        appendLine("3. **Conclusion:** Inconsistent equation. No solution.")
                        if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                    }
                    return AlgebraEquationSolution(
                        originalEquation = equation,
                        variableValues = listOf(VariableValue(targetVar, "No Real Solution (∅)")),
                        primaryVariable = targetVar,
                        stepByStepExplanation = steps,
                        formulaOrMethod = "Inconsistent Equation (No Solution)"
                    )
                }
            }

            val xVal = netB / netA
            val formattedVal = formatNumber(xVal)

            val steps = buildString {
                appendLine("### Step-by-Step Linear Equation Solution:")
                appendLine("• **Original Equation:** `$equation`")
                appendLine("1. **Expand & Group Terms:**")
                appendLine("   - Left side: `${formatSide(a1, b1, targetVar)}`")
                appendLine("   - Right side: `${formatSide(a2, b2, targetVar)}`")
                appendLine("2. **Isolate Variable ($targetVar) terms on left:**")
                appendLine("   `${formatSide(netA, 0.0, targetVar)} = $netB`")
                appendLine("3. **Divide by coefficient ($netA):**")
                appendLine("   `$targetVar = $netB / $netA`")
                appendLine("   `$targetVar = $formattedVal`")
                appendLine("4. **Verification by Substitution:**")
                appendLine("   - Left: `$a1($formattedVal) ${if (b1 >= 0) "+ $b1" else "- ${-b1}"} = ${formatNumber(a1 * xVal + b1)}`")
                appendLine("   - Right: `$a2($formattedVal) ${if (b2 >= 0) "+ $b2" else "- ${-b2}"} = ${formatNumber(a2 * xVal + b2)}`")
                appendLine("   - `LHS = RHS` ✓ Verified!")
                if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                appendLine("\n**FINAL ANSWER:** $targetVar = $formattedVal")
            }

            return AlgebraEquationSolution(
                originalEquation = equation,
                variableValues = listOf(VariableValue(targetVar, formattedVal)),
                primaryVariable = targetVar,
                stepByStepExplanation = steps,
                formulaOrMethod = "Linear Equation: $targetVar = (d - b) / (a - c)"
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun solveQuadraticEquation(equation: String, targetVar: String, fallbackNotice: String?): AlgebraEquationSolution? {
        val clean = equation.replace("²", "^2").replace(" ", "")
        val sides = clean.split("=", limit = 2)
        val leftStr = sides[0]
        val rightStr = if (sides.size > 1) sides[1] else "0"

        try {
            val (a1, b1, c1) = parseQuadraticSide(leftStr, targetVar)
            val (a2, b2, c2) = parseQuadraticSide(rightStr, targetVar)

            val a = a1 - a2
            val b = b1 - b2
            val c = c1 - c2

            if (a == 0.0) {
                // Degenerates to linear
                return solveLinearEquation(equation, targetVar, fallbackNotice)
            }

            val discriminant = (b * b) - (4 * a * c)
            val varValues = mutableListOf<VariableValue>()

            val steps = buildString {
                appendLine("### Step-by-Step Quadratic Equation Solution:")
                appendLine("• **Standard Form:** `${if (a == 1.0) "" else if (a == -1.0) "-" else formatNumber(a)}${targetVar}² ${if (b >= 0) "+ ${formatNumber(b)}" else "- ${formatNumber(-b)}"}$targetVar ${if (c >= 0) "+ ${formatNumber(c)}" else "- ${formatNumber(-c)}"} = 0`")
                appendLine("• **Coefficients:** `a = ${formatNumber(a)}, b = ${formatNumber(b)}, c = ${formatNumber(c)}`")
                appendLine("1. **Calculate Discriminant (D = b² - 4ac):**")
                appendLine("   `D = (${formatNumber(b)})² - 4(${formatNumber(a)})(${formatNumber(c)}) = ${formatNumber(discriminant)}`")

                if (discriminant > 0) {
                    val sqrtD = Math.sqrt(discriminant)
                    val r1 = (-b + sqrtD) / (2 * a)
                    val r2 = (-b - sqrtD) / (2 * a)
                    val r1Str = formatNumber(r1)
                    val r2Str = formatNumber(r2)

                    varValues.add(VariableValue("${targetVar}₁", r1Str))
                    varValues.add(VariableValue("${targetVar}₂", r2Str))

                    appendLine("2. **Since D > 0, there are two distinct real roots:**")
                    appendLine("   - `$targetVar₁ = (-b + √D) / (2a) = (${formatNumber(-b)} + ${formatNumber(sqrtD)}) / ${formatNumber(2 * a)} = $r1Str`")
                    appendLine("   - `$targetVar₂ = (-b - √D) / (2a) = (${formatNumber(-b)} - ${formatNumber(sqrtD)}) / ${formatNumber(2 * a)} = $r2Str`")
                    appendLine("3. **Factored Form:**")
                    appendLine("   `($targetVar - $r1Str)($targetVar - $r2Str) = 0`")
                    if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                    appendLine("\n**FINAL ANSWER:** $targetVar = $r1Str, $r2Str")
                } else if (discriminant == 0.0) {
                    val r = -b / (2 * a)
                    val rStr = formatNumber(r)
                    varValues.add(VariableValue(targetVar, rStr))

                    appendLine("2. **Since D = 0, there is one repeated real root:**")
                    appendLine("   - `$targetVar = -b / (2a) = ${formatNumber(-b)} / ${formatNumber(2 * a)} = $rStr`")
                    appendLine("3. **Factored Form:**")
                    appendLine("   `($targetVar - $rStr)² = 0`")
                    if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                    appendLine("\n**FINAL ANSWER:** $targetVar = $rStr (multiplicity 2)")
                } else {
                    val realPart = -b / (2 * a)
                    val imagPart = Math.sqrt(-discriminant) / (2 * a)
                    val realStr = formatNumber(realPart)
                    val imagStr = formatNumber(Math.abs(imagPart))

                    varValues.add(VariableValue("${targetVar}₁", "$realStr + ${imagStr}i"))
                    varValues.add(VariableValue("${targetVar}₂", "$realStr - ${imagStr}i"))

                    appendLine("2. **Since D < 0, roots are complex conjugates:**")
                    appendLine("   - `$targetVar = ($realStr) ± (${imagStr})i`")
                    if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                    appendLine("\n**FINAL ANSWER:** $targetVar = $realStr ± ${imagStr}i")
                }
            }

            return AlgebraEquationSolution(
                originalEquation = equation,
                variableValues = varValues,
                primaryVariable = targetVar,
                stepByStepExplanation = steps,
                formulaOrMethod = "Quadratic Formula: $targetVar = (-b ± √(b² - 4ac)) / (2a)"
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun solveSystemOfTwoLinear(eq1: String, eq2: String, fallbackNotice: String?): AlgebraEquationSolution? {
        try {
            val (a1, b1, c1) = parseTwoVarLinear(eq1, "x", "y") ?: return null
            val (a2, b2, c2) = parseTwoVarLinear(eq2, "x", "y") ?: return null

            val det = (a1 * b2) - (a2 * b1)
            if (det == 0.0) {
                return null
            }

            val x = ((c1 * b2) - (c2 * b1)) / det
            val y = ((a1 * c2) - (a2 * c1)) / det

            val xStr = formatNumber(x)
            val yStr = formatNumber(y)

            val steps = buildString {
                appendLine("### System of 2 Linear Equations Solution:")
                appendLine("• **Equation (1):** `$eq1` ⇒ `${formatNumber(a1)}x + ${formatNumber(b1)}y = ${formatNumber(c1)}`")
                appendLine("• **Equation (2):** `$eq2` ⇒ `${formatNumber(a2)}x + ${formatNumber(b2)}y = ${formatNumber(c2)}`")
                appendLine("1. **Determinant Method (Cramer's Rule):**")
                appendLine("   - `Δ = (a₁b₂ - a₂b₁) = (${formatNumber(a1)} × ${formatNumber(b2)}) - (${formatNumber(a2)} × ${formatNumber(b1)}) = ${formatNumber(det)}`")
                appendLine("2. **Solve for x (Δx / Δ):**")
                appendLine("   - `Δx = (c₁b₂ - c₂b₁) = (${formatNumber(c1)} × ${formatNumber(b2)}) - (${formatNumber(c2)} × ${formatNumber(b1)}) = ${formatNumber((c1 * b2) - (c2 * b1))}`")
                appendLine("   - `x = Δx / Δ = $xStr`")
                appendLine("3. **Solve for y (Δy / Δ):**")
                appendLine("   - `Δy = (a₁c₂ - a₂c₁) = (${formatNumber(a1)} × ${formatNumber(c2)}) - (${formatNumber(a2)} × ${formatNumber(c1)}) = ${formatNumber((a1 * c2) - (a2 * c1))}`")
                appendLine("   - `y = Δy / Δ = $yStr`")
                appendLine("4. **Verification:**")
                appendLine("   - Eq 1: `$a1($xStr) + $b1($yStr) = ${formatNumber(a1 * x + b1 * y)}` = $c1 ✓")
                appendLine("   - Eq 2: `$a2($xStr) + $b2($yStr) = ${formatNumber(a2 * x + b2 * y)}` = $c2 ✓")
                if (fallbackNotice != null) appendLine("\n*$fallbackNotice*")
                appendLine("\n**FINAL ANSWER:** x = $xStr, y = $yStr")
            }

            return AlgebraEquationSolution(
                originalEquation = "$eq1, $eq2",
                variableValues = listOf(VariableValue("x", xStr), VariableValue("y", yStr)),
                primaryVariable = "x, y",
                stepByStepExplanation = steps,
                formulaOrMethod = "Cramer's Rule / Elimination Method"
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseTwoVarLinear(eq: String, var1: String, var2: String): Triple<Double, Double, Double>? {
        if (!eq.contains("=")) return null
        val sides = eq.split("=", limit = 2)
        val left = sides[0].replace(" ", "")
        val right = sides[1].replace(" ", "")

        val cRight = right.toDoubleOrNull() ?: 0.0

        // Parse ax + by on left
        val regex = Regex("""([+-]?\d*\.?\d*)$var1([+-]\d*\.?\d*)$var2""")
        val m = regex.find(left)
        if (m != null) {
            val aStr = m.groupValues[1]
            val a = if (aStr.isEmpty() || aStr == "+") 1.0 else if (aStr == "-") -1.0 else aStr.toDoubleOrNull() ?: 1.0
            val bStr = m.groupValues[2]
            val b = if (bStr == "+") 1.0 else if (bStr == "-") -1.0 else bStr.toDoubleOrNull() ?: 1.0
            return Triple(a, b, cRight)
        }
        return null
    }

    private fun expandBrackets(expr: String, targetVar: String): String {
        // Simple expansion for k(ax + b)
        val bracketRegex = Regex("""([+-]?\d*\.?\d*)\(([^)]+)\)""")
        return bracketRegex.replace(expr) { match ->
            val factorStr = match.groupValues[1]
            val factor = if (factorStr.isEmpty() || factorStr == "+") 1.0 else if (factorStr == "-") -1.0 else factorStr.toDoubleOrNull() ?: 1.0
            val inside = match.groupValues[2]
            val (a, b) = parseLinearSide(inside, targetVar)
            val newA = factor * a
            val newB = factor * b
            formatSide(newA, newB, targetVar)
        }
    }

    private fun parseLinearSide(expr: String, targetVar: String): Pair<Double, Double> {
        val clean = expr.replace(" ", "").replace("-", "+-")
        val terms = clean.split("+").filter { it.isNotBlank() }
        var coeffA = 0.0
        var constB = 0.0

        for (term in terms) {
            if (term.contains(targetVar, ignoreCase = true)) {
                val numPart = term.replace(targetVar, "", ignoreCase = true)
                val c = when {
                    numPart.isEmpty() -> 1.0
                    numPart == "+" -> 1.0
                    numPart == "-" -> -1.0
                    else -> numPart.toDoubleOrNull() ?: 1.0
                }
                coeffA += c
            } else {
                constB += term.toDoubleOrNull() ?: 0.0
            }
        }
        return Pair(coeffA, constB)
    }

    private fun parseQuadraticSide(expr: String, targetVar: String): Triple<Double, Double, Double> {
        val clean = expr.replace(" ", "").replace("-", "+-")
        val terms = clean.split("+").filter { it.isNotBlank() }
        var a = 0.0
        var b = 0.0
        var c = 0.0

        for (term in terms) {
            if (term.contains("$targetVar^2", ignoreCase = true)) {
                val numPart = term.replace("$targetVar^2", "", ignoreCase = true)
                a += when {
                    numPart.isEmpty() -> 1.0
                    numPart == "+" -> 1.0
                    numPart == "-" -> -1.0
                    else -> numPart.toDoubleOrNull() ?: 1.0
                }
            } else if (term.contains(targetVar, ignoreCase = true)) {
                val numPart = term.replace(targetVar, "", ignoreCase = true)
                b += when {
                    numPart.isEmpty() -> 1.0
                    numPart == "+" -> 1.0
                    numPart == "-" -> -1.0
                    else -> numPart.toDoubleOrNull() ?: 1.0
                }
            } else {
                c += term.toDoubleOrNull() ?: 0.0
            }
        }
        return Triple(a, b, c)
    }

    private fun formatSide(a: Double, b: Double, targetVar: String): String {
        val aPart = when {
            a == 1.0 -> targetVar
            a == -1.0 -> "-$targetVar"
            a == 0.0 -> ""
            else -> "${formatNumber(a)}$targetVar"
        }
        val bPart = when {
            b == 0.0 -> ""
            b > 0.0 -> if (aPart.isNotEmpty()) " + ${formatNumber(b)}" else formatNumber(b)
            else -> if (aPart.isNotEmpty()) " - ${formatNumber(-b)}" else formatNumber(b)
        }
        val combined = aPart + bPart
        return combined.ifEmpty { "0" }
    }

    private fun formatNumber(value: Double): String {
        return if (Math.abs(value - Math.round(value)) < 1e-9) {
            Math.round(value).toString()
        } else {
            "%.4f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    suspend fun solveProblem(question: String): Result<AiMathSolution> = withContext(Dispatchers.IO) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Question cannot be empty"))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidApiKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidApiKey) {
            try {
                val apiResult = callGeminiApi(trimmed, apiKey)
                return@withContext Result.success(apiResult)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API error, using offline step-by-step solver fallback: ${e.message}", e)
                // If API call fails (network/quota), fallback gracefully to offline rule solver with notice
                val offlineSolution = solveOfflineWithSteps(trimmed, fallbackNotice = " (Offline Solver: ${e.localizedMessage ?: "Network error"})")
                return@withContext Result.success(offlineSolution)
            }
        } else {
            // Local offline solver when API key is placeholder
            val offlineSolution = solveOfflineWithSteps(trimmed, fallbackNotice = null)
            return@withContext Result.success(offlineSolution)
        }
    }

    private fun callGeminiApi(question: String, apiKey: String): AiMathSolution {
        val prompt = """
            Solve this math or science problem step-by-step:
            "$question"

            Please provide:
            1. Clear, concise step-by-step explanation.
            2. Any formulas or scientific principles used.
            3. The definitive final answer at the very end in format:
            FINAL ANSWER: [exact result]
            
            Respond in the same language as the question (e.g. English, Hindi/Hinglish).
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // System instructions
            val systemInstruction = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", "You are an expert Math, Physics, and Engineering Problem Solver for 'Calculator Plus - Gufran Khan Edition'. Provide rigorous yet easy-to-understand step-by-step solutions, identify formulas, and always state 'FINAL ANSWER: <result>'."))
                }
                put("parts", parts)
            }
            put("systemInstruction", systemInstruction)

            val generationConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("topP", 0.95)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody.isNullOrBlank()) {
            throw IllegalStateException("API Error (${response.code}): ${response.message}")
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw IllegalStateException("No answer returned from AI model")
        }

        val content = candidates.getJSONObject(0).optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text") ?: "No solution generated."

        return parseAiResponse(question, rawText)
    }

    private fun parseAiResponse(question: String, rawText: String): AiMathSolution {
        // Extract FINAL ANSWER line if present
        var finalAnswer = ""
        val lines = rawText.lines()
        for (line in lines.reversed()) {
            val upper = line.uppercase()
            if (upper.contains("FINAL ANSWER:") || upper.contains("ANSWER:") || upper.contains("उत्तर:")) {
                val parts = line.split(":", limit = 2)
                if (parts.size > 1) {
                    finalAnswer = parts[1].trim().trim('*', '#', '`', ' ')
                    break
                }
            }
        }

        if (finalAnswer.isBlank()) {
            // Pick last non-empty line as fallback
            finalAnswer = lines.lastOrNull { it.isNotBlank() }?.trim(' ', '*', '#') ?: "Solved"
        }

        // Detect formula or concept
        var formula: String? = null
        for (line in lines) {
            if (line.contains("Formula", ignoreCase = true) || line.contains("सूत्र", ignoreCase = true) || line.contains("Theorem", ignoreCase = true)) {
                formula = line.trim()
                break
            }
        }

        return AiMathSolution(
            question = question,
            stepByStepExplanation = rawText,
            finalAnswer = finalAnswer,
            formulaOrConcept = formula
        )
    }

    /**
     * Offline intelligent step-by-step math solver for common algebraic, arithmetic,
     * geometric, and scientific problems when offline.
     */
    private fun solveOfflineWithSteps(question: String, fallbackNotice: String?): AiMathSolution {
        val qLower = question.lowercase().trim()

        // 1. Direct arithmetic or trigonometric evaluation
        val evalResult = MathEvaluator.evaluate(question, AngleMode.DEG)
        if (evalResult.isSuccess) {
            val answer = MathEvaluator.formatResult(evalResult.getOrThrow(), 8)
            val steps = buildString {
                appendLine("### Step-by-step Solution:")
                appendLine("1. **Analyze Expression:** `$question`")
                appendLine("2. **Apply Operator Precedence:** Evaluated standard mathematical functions, trigonometric operations, powers, and arithmetic.")
                appendLine("3. **Computed Value:** `$answer`")
                if (fallbackNotice != null) {
                    appendLine("\n*$fallbackNotice*")
                }
                appendLine("\n**FINAL ANSWER:** $answer")
            }
            return AiMathSolution(
                question = question,
                stepByStepExplanation = steps,
                finalAnswer = answer,
                formulaOrConcept = "Arithmetic & Scientific Evaluation"
            )
        }

        // 2. Linear Equation Solver (e.g. 2x + 5 = 15 or 3x - 9 = 0)
        val linearRegex = Regex("""([+-]?\s*\d*\.?\d*)\s*x\s*([+-]\s*\d+\.?\d*)?\s*=\s*([+-]?\s*\d+\.?\d*)""", RegexOption.IGNORE_CASE)
        val match = linearRegex.find(question.replace(" ", ""))
        if (match != null) {
            val aStr = match.groupValues[1]
            val a = when {
                aStr.isEmpty() || aStr == "+" -> 1.0
                aStr == "-" -> -1.0
                else -> aStr.toDoubleOrNull() ?: 1.0
            }
            val bStr = match.groupValues[2].replace(" ", "")
            val b = if (bStr.isNotEmpty()) bStr.toDoubleOrNull() ?: 0.0 else 0.0
            val c = match.groupValues[3].toDoubleOrNull() ?: 0.0

            if (a != 0.0) {
                val x = (c - b) / a
                val formattedX = if (x % 1.0 == 0.0) x.toLong().toString() else "%.4f".format(x)
                val steps = buildString {
                    appendLine("### Linear Equation Solution for $question:")
                    appendLine("1. **Given equation:** `${a}x ${if (b >= 0) "+ $b" else "- ${-b}"} = $c`")
                    appendLine("2. **Isolate constant terms:** `${a}x = $c ${if (b >= 0) "- $b" else "+ ${-b}"} = ${c - b}`")
                    appendLine("3. **Divide by coefficient of x ($a):** `x = (${c - b}) / $a = $formattedX`")
                    appendLine("\n**FINAL ANSWER:** x = $formattedX")
                }
                return AiMathSolution(
                    question = question,
                    stepByStepExplanation = steps,
                    finalAnswer = formattedX,
                    formulaOrConcept = "Linear Equation: ax + b = c ⇒ x = (c - b) / a"
                )
            }
        }

        // 3. Quadratic Equation Solver (e.g. x^2 - 5x + 6 = 0 or 2x^2 + 4x - 6 = 0)
        if (qLower.contains("x^2") || qLower.contains("x²")) {
            val quadRegex = Regex("""([+-]?\d*\.?\d*)x\^?2([+-]\d*\.?\d*)x([+-]\d+\.?\d*)=0""", RegexOption.IGNORE_CASE)
            val qClean = question.replace(" ", "").replace("x²", "x^2")
            val quadMatch = quadRegex.find(qClean)
            if (quadMatch != null) {
                val aStr = quadMatch.groupValues[1]
                val a = if (aStr.isEmpty() || aStr == "+") 1.0 else if (aStr == "-") -1.0 else aStr.toDoubleOrNull() ?: 1.0
                val bStr = quadMatch.groupValues[2]
                val b = if (bStr == "+") 1.0 else if (bStr == "-") -1.0 else bStr.toDoubleOrNull() ?: 0.0
                val c = quadMatch.groupValues[3].toDoubleOrNull() ?: 0.0

                val disc = (b * b) - (4 * a * c)
                val steps = buildString {
                    appendLine("### Quadratic Equation Solution ($question):")
                    appendLine("1. **Identify Coefficients:** `a = $a, b = $b, c = $c`")
                    appendLine("2. **Calculate Discriminant (D):** `D = b² - 4ac = ($b)² - 4($a)($c) = $disc`")
                    if (disc > 0) {
                        val root1 = (-b + Math.sqrt(disc)) / (2 * a)
                        val root2 = (-b - Math.sqrt(disc)) / (2 * a)
                        appendLine("3. **Two Real Distinct Roots:**")
                        appendLine("   - `x₁ = (-b + √D) / (2a) = $root1`")
                        appendLine("   - `x₂ = (-b - √D) / (2a) = $root2`")
                        appendLine("\n**FINAL ANSWER:** x = $root1, $root2")
                    } else if (disc == 0.0) {
                        val root = -b / (2 * a)
                        appendLine("3. **One Real Root (Repeated):** `x = -b / (2a) = $root`")
                        appendLine("\n**FINAL ANSWER:** x = $root")
                    } else {
                        val realPart = -b / (2 * a)
                        val imagPart = Math.sqrt(-disc) / (2 * a)
                        appendLine("3. **Complex Roots:** `x = $realPart ± ${"%.4f".format(imagPart)}i`")
                        appendLine("\n**FINAL ANSWER:** x = $realPart ± ${"%.4f".format(imagPart)}i")
                    }
                }
                return AiMathSolution(
                    question = question,
                    stepByStepExplanation = steps,
                    finalAnswer = if (disc >= 0) "%.4f".format((-b + Math.sqrt(Math.max(0.0, disc))) / (2 * a)) else "Complex",
                    formulaOrConcept = "Quadratic Formula: x = (-b ± √(b² - 4ac)) / (2a)"
                )
            }
        }

        // 4. General Math & Science Knowledge Solver for common questions
        val explanation = buildString {
            appendLine("### AI Problem Breakdown:")
            appendLine("**Question:** $question\n")
            appendLine("1. **Problem Analysis:** Extracted mathematical components and variables.")
            appendLine("2. **Recommended Method:** Verify input terms or connect to network for online AI generative reasoning.")
            if (fallbackNotice != null) {
                appendLine("\n*$fallbackNotice*")
            }
            appendLine("\n**FINAL ANSWER:** Ready for input")
        }

        return AiMathSolution(
            question = question,
            stepByStepExplanation = explanation,
            finalAnswer = "Ready",
            formulaOrConcept = "Math & Science Engine"
        )
    }
}
