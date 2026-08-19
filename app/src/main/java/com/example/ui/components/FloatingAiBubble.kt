package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FloatingAiBubble(
    offsetX: Float,
    offsetY: Float,
    onOffsetChange: (Float, Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val bubbleSize = 64.dp
    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    // Breathing pulse animation for AI aura
    val infiniteTransition = rememberInfiniteTransition(label = "ai_bubble_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("floating_ai_bubble_container")
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        val marginPx = with(density) { 16.dp.toPx() }
        val topMarginPx = with(density) { 100.dp.toPx() }
        val bottomMarginPx = with(density) { 120.dp.toPx() }

        val minX = marginPx
        val maxX = (screenWidthPx - bubbleSizePx - marginPx).coerceAtLeast(minX)
        val minY = topMarginPx
        val maxY = (screenHeightPx - bubbleSizePx - bottomMarginPx).coerceAtLeast(minY)

        // Internal remembered positions initialized to right side
        var currentX by remember(screenWidthPx, minX, maxX) {
            mutableFloatStateOf(
                if (offsetX > 0f) offsetX.coerceIn(minX, maxX) else maxX
            )
        }
        var currentY by remember(screenHeightPx, minY, maxY) {
            mutableFloatStateOf(
                if (offsetY > 0f) offsetY.coerceIn(minY, maxY) else (minY + maxY) * 0.45f
            )
        }

        // Update when external props change
        LaunchedEffect(offsetX, offsetY) {
            if (offsetX > 0f) currentX = offsetX.coerceIn(minX, maxX)
            if (offsetY > 0f) currentY = offsetY.coerceIn(minY, maxY)
        }

        // Drag displacement accumulator to differentiate click from drag
        var accumulatedDrag by remember { mutableFloatStateOf(0f) }

        // Floating Draggable AI Bubble
        Box(
            modifier = Modifier
                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                .size(bubbleSize)
                .scale(pulseScale)
                // Outer glowing aura
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF8B5CF6).copy(alpha = auraAlpha),
                    spotColor = Color(0xFF3B82F6).copy(alpha = auraAlpha)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7C3AED), // Vivid Purple
                            Color(0xFF2563EB), // Deep Blue
                            Color(0xFF0284C7)  // Cyan
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD700), // Gold
                            Color(0xFFE879F9), // Fuchsia
                            Color(0xFF38BDF8), // Light Blue
                            Color(0xFFFFD700)  // Gold
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(minX, maxX, minY, maxY) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )
                }
                .pointerInput(minX, maxX, minY, maxY) {
                    detectDragGestures(
                        onDragStart = {
                            accumulatedDrag = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += abs(dragAmount.x) + abs(dragAmount.y)
                            val nextX = (currentX + dragAmount.x).coerceIn(minX, maxX)
                            val nextY = (currentY + dragAmount.y).coerceIn(minY, maxY)
                            currentX = nextX
                            currentY = nextY
                            onOffsetChange(nextX, nextY)
                        },
                        onDragEnd = {
                            // If barely dragged, trigger click
                            if (accumulatedDrag < 10f) {
                                onClick()
                            }
                        },
                        onDragCancel = {
                            if (accumulatedDrag < 10f) {
                                onClick()
                            }
                        }
                    )
                }
                .testTag("floating_ai_bubble_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Floating Solver",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
