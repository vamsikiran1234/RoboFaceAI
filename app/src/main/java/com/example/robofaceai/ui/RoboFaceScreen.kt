package com.example.robofaceai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robofaceai.ai.AIManager
import com.example.robofaceai.domain.RoboEvent
import com.example.robofaceai.viewmodel.RoboViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main screen displaying the Robo Face with clean, professional UI.
 *
 * TASK 2, 5, 6 Integration:
 * - Displays native vector Robo Face (Task 2)
 * - FSM-driven behavior (Task 5)
 * - Sensor-driven interaction
 * - AI integration (Task 6)
 */
@Composable
fun RoboFaceScreen(
    viewModel: RoboViewModel? = null,
    modifier: Modifier = Modifier
) {
    val vm = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<RoboViewModel>()

    // Collect state from ViewModel
    val currentState by vm.state.collectAsState()
    val stateName by vm.stateName.collectAsState()

    // Collect sensor values
    val tiltX by vm.tiltX.collectAsState()
    val tiltY by vm.tiltY.collectAsState()
    val headRotation by vm.headRotation.collectAsState()

    // Collect AI stats for Task 6 display
    val aiStats by vm.aiStats.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Robo Face Canvas
        RoboFaceCanvas(
            state = currentState,
            tiltX = tiltX,
            tiltY = tiltY,
            headRotation = headRotation,
            modifier = Modifier.fillMaxSize()
        )

        // Clean state indicator at top
        StateIndicator(
            stateName = stateName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        // Task 6 AI Stats (top-right corner - away from eyes)
        AIStatsOverlay(
            stats = aiStats,
            currentStateName = stateName,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 12.dp)
        )

        // Task 5 Test Controls (bottom - smaller buttons)
        Task5TestControls(
            viewModel = vm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

/**
 * Clean state indicator with enhanced glow
 */
@Composable
private fun StateIndicator(
    stateName: String,
    modifier: Modifier = Modifier
) {
    val color = when (stateName) {
        "Sleep" -> Color(0xFF6495ED)
        "Angry" -> Color(0xFFFF4444)
        "Happy" -> Color(0xFF4CAF50)
        "Curious" -> Color(0xFFFFEB3B)
        "Idle" -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    0f to Color.Black.copy(alpha = 0.9f),
                    1f to Color.Black.copy(alpha = 0.6f)
                )
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Single clean text (no double rendering)
        Text(
            text = stateName,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = color.copy(alpha = 0.6f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 12f
                )
            )
        )
    }
}

/**
 * Task 5 Test Controls - Icon-Based Futuristic Design
 */
@Composable
private fun Task5TestControls(
    viewModel: RoboViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Face events
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconEventButton(
                type = ButtonType.FACE_DETECT,
                color = Color(0xFF00BCD4),
                label = "SCAN"
            ) {
                // Trigger curious state (scanning mode)
                viewModel.handleEvent(RoboEvent.ManualStateChange(com.example.robofaceai.domain.RoboState.Curious))
            }
            IconEventButton(
                type = ButtonType.FACE_DETECTED,
                color = Color(0xFF4CAF50),
                label = "FOUND"
            ) {
                viewModel.handleEvent(RoboEvent.FaceDetected)
            }
            IconEventButton(
                type = ButtonType.FACE_LOST,
                color = Color(0xFF9E9E9E),
                label = "LOST"
            ) {
                viewModel.handleEvent(RoboEvent.FaceLost)
            }
        }

        // Row 2: Sound & Inactivity
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconEventButton(
                type = ButtonType.SOUND,
                color = Color(0xFFFF4444),
                label = "SOUND"
            ) {
                viewModel.handleEvent(RoboEvent.LoudSoundDetected)
            }
            IconEventButton(
                type = ButtonType.SLEEP,
                color = Color(0xFF6495ED),
                label = "SLEEP"
            ) {
                viewModel.handleEvent(RoboEvent.ProlongedInactivity)
            }
        }

        // Hint text
        Text(
            text = "Shake phone for real-time testing",
            color = Color(0xFF9E9E9E),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Button types for icon rendering
 */
private enum class ButtonType {
    FACE_DETECT, FACE_DETECTED, FACE_LOST, SOUND, SLEEP
}

/**
 * Futuristic icon-based button with canvas-drawn symbols
 */
@Composable
private fun IconEventButton(
    type: ButtonType,
    color: Color,
    label: String = "",
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // Radar animation for SCAN button
    val infiniteTransition = rememberInfiniteTransition(label = "radar_scan")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .width(70.dp)
                .height(50.dp)
                .clickable {
                    isPressed = true
                    onClick()
                    // Reset press state after animation
                    GlobalScope.launch {
                        delay(150)
                        isPressed = false
                    }
                }
        ) {
            val cornerRadius = 12.dp.toPx()
            val strokeWidth = 3.dp.toPx()
            val scale = if (isPressed) 0.95f else 1f

            val drawSize = Size(size.width * scale, size.height * scale)
            val offset = Offset(
                (size.width - drawSize.width) / 2,
                (size.height - drawSize.height) / 2
            )

            // Background with gradient
            drawRoundRect(
                brush = Brush.linearGradient(
                    0f to color.copy(alpha = 0.3f),
                    1f to color.copy(alpha = 0.15f)
                ),
                topLeft = offset,
                size = drawSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )

            // Border with glow
            drawRoundRect(
                color = color.copy(alpha = 0.8f),
                topLeft = offset,
                size = drawSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(width = strokeWidth)
            )

            // Draw icon centered
            val iconCenter = Offset(size.width / 2, size.height / 2)
            when (type) {
                ButtonType.FACE_DETECT -> drawFaceDetectIcon(iconCenter, color, radarAngle)
                ButtonType.FACE_DETECTED -> drawFaceDetectedIcon(iconCenter, color)
                ButtonType.FACE_LOST -> drawFaceLostIcon(iconCenter, color)
                ButtonType.SOUND -> drawSoundIcon(iconCenter, color)
                ButtonType.SLEEP -> drawSleepIcon(iconCenter, color)
            }
        }

        // Label text
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = color.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Draw face detected with checkmark icon
 */
private fun DrawScope.drawFaceDetectedIcon(center: Offset, color: Color) {
    val faceRadius = 15.dp.toPx()

    // Face circle
    drawCircle(
        color = color,
        radius = faceRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Happy eyes (crescents)
    val eyeY = center.y - 3.dp.toPx()
    val eyeLeftX = center.x - 5.dp.toPx()
    val eyeRightX = center.x + 5.dp.toPx()
    
    // Left eye crescent
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(eyeLeftX - 2.5.dp.toPx(), eyeY - 2.5.dp.toPx()),
        size = Size(5.dp.toPx(), 5.dp.toPx()),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // Right eye crescent
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(eyeRightX - 2.5.dp.toPx(), eyeY - 2.5.dp.toPx()),
        size = Size(5.dp.toPx(), 5.dp.toPx()),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Smile
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - 6.dp.toPx(), center.y + 2.dp.toPx()),
        size = Size(12.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Checkmark badge (top-right corner)
    val checkCenter = Offset(center.x + 12.dp.toPx(), center.y - 12.dp.toPx())
    
    // Badge circle background
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = 6.dp.toPx(),
        center = checkCenter
    )
    
    // Badge circle border
    drawCircle(
        color = color,
        radius = 6.dp.toPx(),
        center = checkCenter,
        style = Stroke(width = 1.5.dp.toPx())
    )
    
    // Checkmark
    val checkPath = Path().apply {
        moveTo(checkCenter.x - 3.dp.toPx(), checkCenter.y)
        lineTo(checkCenter.x - 1.dp.toPx(), checkCenter.y + 2.dp.toPx())
        lineTo(checkCenter.x + 3.dp.toPx(), checkCenter.y - 2.dp.toPx())
    }
    
    drawPath(
        path = checkPath,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * Draw face detection scanning frame icon with radar animation
 */
private fun DrawScope.drawFaceDetectIcon(center: Offset, color: Color, radarAngle: Float = 0f) {
    val faceRadius = 15.dp.toPx()
    val cornerSize = 8.dp.toPx()
    val cornerOffset = 20.dp.toPx()

    // Face circle
    drawCircle(
        color = color,
        radius = faceRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Eyes
    drawCircle(
        color = color,
        radius = 2.dp.toPx(),
        center = Offset(center.x - 5.dp.toPx(), center.y - 3.dp.toPx())
    )
    drawCircle(
        color = color,
        radius = 2.dp.toPx(),
        center = Offset(center.x + 5.dp.toPx(), center.y - 3.dp.toPx())
    )

    // === ANIMATED RADAR SWEEP ===
    // Rotating radar line (sweeps around 360 degrees)
    val radarRadius = faceRadius * 1.8f
    val radarRad = Math.toRadians(radarAngle.toDouble()).toFloat()
    val radarEndX = center.x + kotlin.math.cos(radarRad) * radarRadius
    val radarEndY = center.y + kotlin.math.sin(radarRad) * radarRadius
    
    // Radar sweep line with glow
    drawLine(
        brush = Brush.linearGradient(
            0f to color.copy(alpha = 0.8f),
            0.7f to color.copy(alpha = 0.5f),
            1f to color.copy(alpha = 0.1f),
            start = center,
            end = Offset(radarEndX, radarEndY)
        ),
        start = center,
        end = Offset(radarEndX, radarEndY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    
    // Radar arc trail (fading sweep trail)
    drawArc(
        brush = Brush.sweepGradient(
            0f to Color.Transparent,
            0.3f to color.copy(alpha = 0.15f),
            0.6f to color.copy(alpha = 0.05f),
            1f to Color.Transparent,
            center = center
        ),
        startAngle = radarAngle - 90f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(center.x - radarRadius, center.y - radarRadius),
        size = Size(radarRadius * 2, radarRadius * 2),
        alpha = 0.4f
    )
    
    // Pulsing radar rings
    val pulseOffset = (radarAngle % 120f) / 120f
    for (i in 0..2) {
        val ringProgress = (pulseOffset + i * 0.33f) % 1f
        val ringRadius = faceRadius * (0.8f + ringProgress * 1.2f)
        val ringAlpha = (1f - ringProgress) * 0.3f
        
        drawCircle(
            color = color.copy(alpha = ringAlpha),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // Scanning corners
    val corners = listOf(
        // Top-left
        Pair(center.x - cornerOffset, center.y - cornerOffset),
        // Top-right
        Pair(center.x + cornerOffset, center.y - cornerOffset),
        // Bottom-left
        Pair(center.x - cornerOffset, center.y + cornerOffset),
        // Bottom-right
        Pair(center.x + cornerOffset, center.y + cornerOffset)
    )

    corners.forEachIndexed { index, (x, y) ->
        val horizontal = if (index % 2 == 0) cornerSize else -cornerSize
        val vertical = if (index < 2) cornerSize else -cornerSize

        // Horizontal line
        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(x, y),
            end = Offset(x + horizontal, y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Vertical line
        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(x, y),
            end = Offset(x, y + vertical),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draw face lost (face with X mark - no face detected) icon
 */
private fun DrawScope.drawFaceLostIcon(center: Offset, color: Color) {
    val faceRadius = 15.dp.toPx()

    // Face circle (dashed/broken)
    drawCircle(
        color = color.copy(alpha = 0.5f),
        radius = faceRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Sad eyes (X marks)
    val eyeY = center.y - 3.dp.toPx()
    val eyeLeftX = center.x - 5.dp.toPx()
    val eyeRightX = center.x + 5.dp.toPx()
    val eyeSize = 3.dp.toPx()

    // Left eye X
    drawLine(
        color = color,
        start = Offset(eyeLeftX - eyeSize, eyeY - eyeSize),
        end = Offset(eyeLeftX + eyeSize, eyeY + eyeSize),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(eyeLeftX + eyeSize, eyeY - eyeSize),
        end = Offset(eyeLeftX - eyeSize, eyeY + eyeSize),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Right eye X
    drawLine(
        color = color,
        start = Offset(eyeRightX - eyeSize, eyeY - eyeSize),
        end = Offset(eyeRightX + eyeSize, eyeY + eyeSize),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(eyeRightX + eyeSize, eyeY - eyeSize),
        end = Offset(eyeRightX - eyeSize, eyeY + eyeSize),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Sad mouth (frown)
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(center.x - 6.dp.toPx(), center.y + 5.dp.toPx()),
        size = Size(12.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Big X mark overlay (face not detected)
    val xSize = 22.dp.toPx()
    drawLine(
        color = color,
        start = Offset(center.x - xSize, center.y - xSize),
        end = Offset(center.x + xSize, center.y + xSize),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + xSize, center.y - xSize),
        end = Offset(center.x - xSize, center.y + xSize),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/**
 * Draw sound wave icon
 */
private fun DrawScope.drawSoundIcon(center: Offset, color: Color) {
    // Speaker cone
    val speakerPath = Path().apply {
        moveTo(center.x - 12.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x - 4.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x + 4.dp.toPx(), center.y - 15.dp.toPx())
        lineTo(center.x + 4.dp.toPx(), center.y + 15.dp.toPx())
        lineTo(center.x - 4.dp.toPx(), center.y + 8.dp.toPx())
        lineTo(center.x - 12.dp.toPx(), center.y + 8.dp.toPx())
        close()
    }

    drawPath(
        path = speakerPath,
        color = color,
        style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
    )

    // Sound waves
    for (i in 1..3) {
        val waveOffset = i * 6.dp.toPx()
        drawArc(
            color = color.copy(alpha = 1f - (i * 0.2f)),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(
                center.x + 4.dp.toPx() - waveOffset,
                center.y - waveOffset
            ),
            size = Size(waveOffset * 2, waveOffset * 2),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Draw sleep mode (moon with Z) icon
 */
private fun DrawScope.drawSleepIcon(center: Offset, color: Color) {
    // Moon crescent
    drawCircle(
        color = color,
        radius = 12.dp.toPx(),
        center = Offset(center.x - 2.dp.toPx(), center.y),
        style = Stroke(width = 2.dp.toPx())
    )

    // Cover part to create crescent
    drawCircle(
        color = Color.Black,
        radius = 10.dp.toPx(),
        center = Offset(center.x + 3.dp.toPx(), center.y - 2.dp.toPx())
    )

    // Z symbols
    val zPath = Path().apply {
        // First Z
        moveTo(center.x + 10.dp.toPx(), center.y - 12.dp.toPx())
        lineTo(center.x + 18.dp.toPx(), center.y - 12.dp.toPx())
        lineTo(center.x + 10.dp.toPx(), center.y - 6.dp.toPx())
        lineTo(center.x + 18.dp.toPx(), center.y - 6.dp.toPx())

        // Second Z (smaller, offset)
        moveTo(center.x + 14.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x + 20.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x + 14.dp.toPx(), center.y + 3.dp.toPx())
        lineTo(center.x + 20.dp.toPx(), center.y + 3.dp.toPx())
    }

    drawPath(
        path = zPath,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Task 6 AI Stats Overlay - Enhanced with FPS and Inference Count
 * Positioned on top-right to avoid overlapping eyes
 */
@Composable
private fun AIStatsOverlay(
    stats: AIManager.InferenceStats,
    currentStateName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(10.dp)
            .width(130.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = "AI Stats",
            color = Color(0xFF00BCD4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Accelerator mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mode:",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp
            )
            Text(
                text = stats.acceleratorMode,
                color = when (stats.acceleratorMode) {
                    "NNAPI" -> Color(0xFF4CAF50)
                    "GPU" -> Color(0xFFFFEB3B)
                    else -> Color(0xFF00BCD4)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Latency (real-time value)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Latency:",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp
            )
            Text(
                text = "${stats.latencyMs}ms",
                color = when {
                    stats.latencyMs < 20 -> Color(0xFF4CAF50)
                    stats.latencyMs < 50 -> Color(0xFFFFEB3B)
                    else -> Color(0xFFFF4444)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // FPS (frames per second calculation)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "FPS:",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp
            )
            Text(
                text = String.format("%.1f", stats.fps),
                color = when {
                    stats.fps > 30 -> Color(0xFF4CAF50)
                    stats.fps > 15 -> Color(0xFFFFEB3B)
                    else -> Color(0xFFFF4444)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Inference count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Inference:",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp
            )
            Text(
                text = "#${stats.inferenceCount}",
                color = Color(0xFF00BCD4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Current State (real-time from sensor/button input)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "State:",
                color = Color(0xFF9E9E9E),
                fontSize = 10.sp
            )
            Text(
                text = currentStateName.uppercase(),
                color = when (currentStateName) {
                    "Happy" -> Color(0xFF4CAF50)
                    "Angry" -> Color(0xFFFF4444)
                    "Curious" -> Color(0xFFFFEB3B)
                    "Sleep" -> Color(0xFF6495ED)
                    else -> Color.White
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Confidence with visual bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confidence:",
                    color = Color(0xFF9E9E9E),
                    fontSize = 10.sp
                )
                Text(
                    text = "${(stats.confidence * 100).toInt()}%",
                    color = if (stats.confidence > 0.7f) Color(0xFF4CAF50) else Color(0xFFFFEB3B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Confidence bar
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF333333))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(stats.confidence.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(
                            if (stats.confidence > 0.7f) Color(0xFF4CAF50)
                            else Color(0xFFFFEB3B)
                        )
                )
            }
        }
    }
}









