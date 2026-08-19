package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AngleMode
import com.example.engine.MathEvaluator
import com.example.engine.UnitConverter
import com.example.engine.VoiceMathParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Scientific Calculator", appName)
    }

    @Test
    fun `test math evaluator basic operations`() {
        val result = MathEvaluator.evaluate("25 + 75 * 2")
        assertTrue(result.isSuccess)
        assertEquals(175.0, result.getOrNull()!!, 0.0001)
    }

    @Test
    fun `test math evaluator scientific functions`() {
        val sin90 = MathEvaluator.evaluate("sin(90)", AngleMode.DEG)
        assertTrue(sin90.isSuccess)
        assertEquals(1.0, sin90.getOrNull()!!, 0.0001)

        val sqrt144 = MathEvaluator.evaluate("sqrt(144)")
        assertTrue(sqrt144.isSuccess)
        assertEquals(12.0, sqrt144.getOrNull()!!, 0.0001)

        val fact5 = MathEvaluator.evaluate("5!")
        assertTrue(fact5.isSuccess)
        assertEquals(120.0, fact5.getOrNull()!!, 0.0001)
    }

    @Test
    fun `test unit converter length conversion`() {
        val lengthCat = UnitConverter.CATEGORIES.first { it.id == "length" }
        val meter = lengthCat.units.first { it.id == "m" }
        val km = lengthCat.units.first { it.id == "km" }

        val converted = UnitConverter.convert(1500.0, meter, km)
        assertEquals(1.5, converted, 0.0001)
    }

    @Test
    fun `test voice math parser`() {
        val parsed = VoiceMathParser.parseSpokenTextToMath("twenty five plus seventy five")
        assertEquals("25+75", parsed)

        val parsedSqrt = VoiceMathParser.parseSpokenTextToMath("square root of 144")
        assertEquals("sqrt(144)", parsedSqrt)
    }

    @Test
    fun `test floating ai view model state and bubble control`() {
        val vm = com.example.ui.viewmodel.FloatingAiViewModel()
        assertTrue(vm.uiState.value.isBubbleVisible)
        assertFalse(vm.uiState.value.isWindowOpen)

        vm.openFloatingWindow()
        assertTrue(vm.uiState.value.isWindowOpen)

        vm.setBubbleOffset(120f, 340f)
        assertEquals(120f, vm.uiState.value.bubbleOffsetX, 0.001f)
        assertEquals(340f, vm.uiState.value.bubbleOffsetY, 0.001f)

        vm.resetBubblePosition()
        assertEquals(0f, vm.uiState.value.bubbleOffsetX, 0.001f)
        assertEquals(0f, vm.uiState.value.bubbleOffsetY, 0.001f)
    }
}
