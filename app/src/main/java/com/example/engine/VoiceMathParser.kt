package com.example.engine

import java.util.Locale

object VoiceMathParser {

    private val WORD_TO_NUMBER = mapOf(
        "zero" to 0.0, "shunya" to 0.0,
        "one" to 1.0, "ek" to 1.0,
        "two" to 2.0, "do" to 2.0,
        "three" to 3.0, "teen" to 3.0,
        "four" to 4.0, "chaar" to 4.0,
        "five" to 5.0, "paanch" to 5.0,
        "six" to 6.0, "chhah" to 6.0, "che" to 6.0,
        "seven" to 7.0, "saat" to 7.0,
        "eight" to 8.0, "aath" to 8.0,
        "nine" to 9.0, "nau" to 9.0,
        "ten" to 10.0, "das" to 10.0,
        "eleven" to 11.0, "gyarah" to 11.0,
        "twelve" to 12.0, "barah" to 12.0,
        "thirteen" to 13.0, "terah" to 13.0,
        "fourteen" to 14.0, "chaudah" to 14.0,
        "fifteen" to 15.0, "pandrah" to 15.0,
        "sixteen" to 16.0, "solah" to 16.0,
        "seventeen" to 17.0, "satrah" to 17.0,
        "eighteen" to 18.0, "atharah" to 18.0,
        "nineteen" to 19.0, "unnis" to 19.0,
        "twenty" to 20.0, "bees" to 20.0,
        "thirty" to 30.0, "tees" to 30.0,
        "forty" to 40.0, "chalis" to 40.0,
        "fifty" to 50.0, "pachaas" to 50.0,
        "sixty" to 60.0, "saath" to 60.0,
        "seventy" to 70.0, "sattar" to 70.0,
        "eighty" to 80.0, "assi" to 80.0,
        "ninety" to 90.0, "nabbe" to 90.0,
        "hundred" to 100.0, "sau" to 100.0,
        "thousand" to 1000.0, "hazaar" to 1000.0,
        "million" to 1000000.0,
        "billion" to 1000000000.0,
        "lakh" to 100000.0, "crore" to 10000000.0
    )

    fun parseSpokenTextToMath(spoken: String): String {
        var text = spoken.lowercase(Locale.ROOT).trim()

        // Replace common spoken operators & phrasing
        text = text
            // Powers & Roots
            .replace("to the power of", "^")
            .replace("to the power", "^")
            .replace("raised to the power of", "^")
            .replace("raised to the power", "^")
            .replace("raised to", "^")
            .replace("power of", "^")
            .replace("power", "^")
            .replace("ghat", "^")
            .replace("square of", "sqr_placeholder(")
            .replace("cube of", "cube_placeholder(")
            .replace("square root of", "sqrt(")
            .replace("square root", "sqrt(")
            .replace("root of", "sqrt(")
            .replace("cube root of", "cbrt(")
            .replace("cube root", "cbrt(")

            // Trigonometry & Log
            .replace("sine of", "sin(")
            .replace("sin of", "sin(")
            .replace("cosine of", "cos(")
            .replace("cos of", "cos(")
            .replace("tangent of", "tan(")
            .replace("tan of", "tan(")
            .replace("arc sine of", "asin(")
            .replace("arc cos of", "acos(")
            .replace("arc tan of", "atan(")
            .replace("logarithm of", "log(")
            .replace("natural log of", "ln(")
            .replace("natural logarithm of", "ln(")
            .replace("log of", "log(")
            .replace("ln of", "ln(")

            // Multiplication
            .replace("multiplied by", "*")
            .replace("multiply by", "*")
            .replace("multiply", "*")
            .replace("multiplied with", "*")
            .replace("times", "*")
            .replace("into", "*")
            .replace("guna", "*")
            .replace("gunank", "*")
            .replace(" x ", " * ")

            // Division
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("divide", "/")
            .replace("divided", "/")
            .replace("over", "/")
            .replace("bhaag", "/")
            .replace("batte", "/")
            .replace("by", "/")

            // Addition
            .replace("plus", "+")
            .replace("add", "+")
            .replace("addition", "+")
            .replace("jod", "+")
            .replace("jama", "+")

            // Subtraction
            .replace("minus", "-")
            .replace("subtract", "-")
            .replace("subtracted by", "-")
            .replace("kam", "-")
            .replace("ghata", "-")

            // Percentage / Modulo
            .replace("percent of", "% *")
            .replace("percent", "%")
            .replace("percentage", "%")
            .replace("pratishat", "%")
            .replace("modulo", "%")
            .replace("mod", "%")

            // Constants
            .replace("pi constant", "pi")
            .replace("pie", "pi")
            .replace("pai", "pi")
            .replace("euler number", "e")
            .replace("euler's number", "e")

            // Decimals & Point
            .replace("point", ".")
            .replace("dot", ".")
            .replace("dashamlav", ".")

            // Parentheses
            .replace("open bracket", "(")
            .replace("open parenthesis", "(")
            .replace("close bracket", ")")
            .replace("close parenthesis", ")")
            .replace("bracket open", "(")
            .replace("bracket close", ")")

        // Parse words like "twenty five" or "one hundred fifty" to numbers
        val words = text.split(Regex("\\s+"))
        val resultTokens = mutableListOf<String>()
        var currentNumberSum = 0.0
        var currentChunk = 0.0
        var isParsingNumber = false

        for (w in words) {
            val num = WORD_TO_NUMBER[w]
            if (num != null) {
                isParsingNumber = true
                if (num >= 1000.0) {
                    currentChunk = if (currentChunk == 0.0) 1.0 else currentChunk
                    currentNumberSum += currentChunk * num
                    currentChunk = 0.0
                } else if (num == 100.0) {
                    currentChunk = if (currentChunk == 0.0) 1.0 else currentChunk
                    currentChunk *= 100.0
                } else {
                    currentChunk += num
                }
            } else {
                if (isParsingNumber) {
                    val finalNum = currentNumberSum + currentChunk
                    resultTokens.add(if (finalNum % 1.0 == 0.0) finalNum.toLong().toString() else finalNum.toString())
                    currentNumberSum = 0.0
                    currentChunk = 0.0
                    isParsingNumber = false
                }
                resultTokens.add(w)
            }
        }

        if (isParsingNumber) {
            val finalNum = currentNumberSum + currentChunk
            resultTokens.add(if (finalNum % 1.0 == 0.0) finalNum.toLong().toString() else finalNum.toString())
        }

        var parsed = resultTokens.joinToString(" ")
            .replace("sqr_placeholder( ", "")
            .replace("sqr_placeholder(", "")
            .replace("cube_placeholder( ", "")
            .replace("cube_placeholder(", "")

        // Balance unclosed parenthesis from voice triggers (e.g. sqrt(144 -> sqrt(144))
        val openCount = parsed.count { it == '(' }
        val closeCount = parsed.count { it == ')' }
        if (openCount > closeCount) {
            parsed += ")".repeat(openCount - closeCount)
        }

        return parsed
            .replace(Regex("\\s+"), " ")
            .replace("( ", "(")
            .replace(" )", ")")
            .replace(" + ", "+")
            .replace(" - ", "-")
            .replace(" * ", "×")
            .replace(" / ", "÷")
            .trim()
    }
}
