package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleMode
import com.example.engine.MathEvaluator
import com.example.ui.viewmodel.GraphPoint
import com.example.ui.viewmodel.GraphViewModel
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "2D Graph Plotter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                actions = {
                    FilterChip(
                        selected = uiState.angleMode == AngleMode.RAD,
                        onClick = { viewModel.toggleAngleMode() },
                        label = {
                            Text(
                                if (uiState.angleMode == AngleMode.RAD) "RAD" else "DEG",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Formula Input & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.inputFormula,
                    onValueChange = { viewModel.setInputFormula(it) },
                    label = { Text("f(x) =") },
                    placeholder = { Text("e.g. sin(x), x^2 - 4") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (uiState.inputFormula.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setInputFormula("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear formula")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("graph_formula_input")
                )

                Button(
                    onClick = { viewModel.applyFormula() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("plot_graph_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Plot")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Plot", fontWeight = FontWeight.Bold)
                }
            }

            // Error Message (if any)
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // Presets Horizontal Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.PRESET_FUNCTIONS) { preset ->
                    val isSelected = uiState.formula == preset.formula
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectPreset(preset) },
                        label = { Text(preset.title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Graph Canvas Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF070B12))
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = Color(0xFF1E293B)
                val axisColor = Color(0xFF64748B)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaXFraction = dragAmount.x / size.width
                                val deltaYFraction = dragAmount.y / size.height
                                viewModel.onPan(deltaXFraction, deltaYFraction)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val xSpan = uiState.xMax - uiState.xMin
                                val clickedX = uiState.xMin + (offset.x / size.width) * xSpan
                                viewModel.onInspectPoint(clickedX)
                            }
                        }
                        .testTag("graph_canvas")
                ) {
                    val w = size.width
                    val h = size.height
                    val xMin = uiState.xMin
                    val xMax = uiState.xMax
                    val yMin = uiState.yMin
                    val yMax = uiState.yMax

                    val xSpan = xMax - xMin
                    val ySpan = yMax - yMin

                    // Helper coordinate transforms
                    fun toScreenX(mathX: Double): Float = (((mathX - xMin) / xSpan) * w).toFloat()
                    fun toScreenY(mathY: Double): Float = (h - ((mathY - yMin) / ySpan) * h).toFloat()
                    fun toMathX(screenX: Float): Double = xMin + (screenX / w) * xSpan

                    // Draw Grid Lines
                    val gridStepX = calculateNiceStep(xSpan)
                    val gridStepY = calculateNiceStep(ySpan)

                    val startGridX = floor(xMin / gridStepX) * gridStepX
                    val endGridX = ceil(xMax / gridStepX) * gridStepX
                    var currX = startGridX
                    while (currX <= endGridX) {
                        val sx = toScreenX(currX)
                        drawLine(
                            color = gridColor,
                            start = Offset(sx, 0f),
                            end = Offset(sx, h),
                            strokeWidth = 1f
                        )
                        currX += gridStepX
                    }

                    val startGridY = floor(yMin / gridStepY) * gridStepY
                    val endGridY = ceil(yMax / gridStepY) * gridStepY
                    var currY = startGridY
                    while (currY <= endGridY) {
                        val sy = toScreenY(currY)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, sy),
                            end = Offset(w, sy),
                            strokeWidth = 1f
                        )
                        currY += gridStepY
                    }

                    // Draw X Axis (y = 0)
                    if (0.0 in yMin..yMax) {
                        val yZeroScreen = toScreenY(0.0)
                        drawLine(
                            color = axisColor,
                            start = Offset(0f, yZeroScreen),
                            end = Offset(w, yZeroScreen),
                            strokeWidth = 2.5f
                        )
                    }

                    // Draw Y Axis (x = 0)
                    if (0.0 in xMin..xMax) {
                        val xZeroScreen = toScreenX(0.0)
                        drawLine(
                            color = axisColor,
                            start = Offset(xZeroScreen, 0f),
                            end = Offset(xZeroScreen, h),
                            strokeWidth = 2.5f
                        )
                    }

                    // Plot Function Curve
                    val steps = (w.toInt()).coerceAtLeast(300)
                    val path = Path()
                    var isDrawing = false
                    var prevScreenY = 0f

                    for (i in 0..steps) {
                        val screenX = (i.toFloat() / steps) * w
                        val mathX = toMathX(screenX)
                        val mathY = MathEvaluator.evaluateForX(uiState.formula, mathX, uiState.angleMode)

                        if (mathY.isNaN() || mathY.isInfinite()) {
                            isDrawing = false
                            continue
                        }

                        val screenY = toScreenY(mathY)

                        // Avoid asymptote huge jumps
                        if (isDrawing && abs(screenY - prevScreenY) > h * 1.5f) {
                            isDrawing = false
                        }

                        if (!isDrawing) {
                            path.moveTo(screenX, screenY)
                            isDrawing = true
                        } else {
                            path.lineTo(screenX, screenY)
                        }
                        prevScreenY = screenY
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.5f)
                    )

                    // Draw Selected Point / Crosshair
                    uiState.selectedPoint?.let { pt ->
                        val ptScreenX = toScreenX(pt.x)
                        val ptScreenY = toScreenY(pt.y)

                        // Crosshair dashed guide lines
                        drawLine(
                            color = Color(0x6600E5FF),
                            start = Offset(ptScreenX, 0f),
                            end = Offset(ptScreenX, h),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = Color(0x6600E5FF),
                            start = Offset(0f, ptScreenY),
                            end = Offset(w, ptScreenY),
                            strokeWidth = 1.5f
                        )

                        // Outer ring & center dot
                        drawCircle(
                            color = Color(0x5500E5FF),
                            radius = 14f,
                            center = Offset(ptScreenX, ptScreenY)
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 6f,
                            center = Offset(ptScreenX, ptScreenY)
                        )
                    }
                }

                // Floating HUD for Selected Coordinate Point
                uiState.selectedPoint?.let { pt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xEE0B132B),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        val df = DecimalFormat("0.####")
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "x = ${df.format(pt.x)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "•",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "y = ${df.format(pt.y)}",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Floating Zoom & Reset Controls on bottom-right of Canvas
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.zoomIn() },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                    }

                    FloatingActionButton(
                        onClick = { viewModel.zoomOut() },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                    }

                    FloatingActionButton(
                        onClick = { viewModel.resetView() },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset View", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Bottom Axis Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val df = DecimalFormat("0.#")
                Text(
                    text = "X: [${df.format(uiState.xMin)}, ${df.format(uiState.xMax)}]",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Drag to pan • Tap to inspect coordinates",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Text(
                    text = "Y: [${df.format(uiState.yMin)}, ${df.format(uiState.yMax)}]",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun calculateNiceStep(span: Double): Double {
    val rawStep = span / 8.0
    val magnitude = Math.pow(10.0, floor(Math.log10(rawStep)))
    val normalized = rawStep / magnitude
    val niceNormalized = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    return niceNormalized * magnitude
}
