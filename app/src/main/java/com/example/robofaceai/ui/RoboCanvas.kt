package com.example.robofaceai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.robofaceai.domain.RoboState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native vector-based Robo Face drawn entirely with Canvas.
 * NO image assets - 100% code-based drawing.
 *
 * Components:
 * - Eyes: Concentric circles with glow effects
 * - Mouth: Equalizer-style bars
 * - Nose: Geometric shape with glowing dot
 */
@Composable
fun RoboCanvas(
    state: RoboState,
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    modifier: Modifier = Modifier
) {
    // Animation time ticker
    var animationTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            animationTime = System.currentTimeMillis()
            delay(16) // ~60 FPS
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // Calculate dynamic values based on state
        val pulseScale = RoboAnimations.getEyePulseScale(state, animationTime)
        val glowIntensity = RoboAnimations.getEyeGlowIntensity(state, animationTime)
        val eyeRotation = RoboAnimations.getEyeRotation(state, animationTime)

        // Eye positions (adjusted by tilt)
        val eyeSpacing = canvasWidth * 0.25f
        val leftEyeX = centerX - eyeSpacing + (tiltX * 30f)
        val rightEyeX = centerX + eyeSpacing + (tiltX * 30f)
        val eyeY = centerY - canvasHeight * 0.15f + (tiltY * 30f)
        val eyeRadius = canvasWidth * 0.12f

        // Draw left eye
        drawRoboEye(
            center = Offset(leftEyeX, eyeY),
            baseRadius = eyeRadius,
            state = state,
            pulseScale = pulseScale,
            glowIntensity = glowIntensity,
            rotation = eyeRotation
        )

        // Draw right eye
        drawRoboEye(
            center = Offset(rightEyeX, eyeY),
            baseRadius = eyeRadius,
            state = state,
            pulseScale = pulseScale,
            glowIntensity = glowIntensity,
            rotation = eyeRotation
        )

        // Draw nose
        drawRoboNose(
            center = Offset(centerX, centerY + canvasHeight * 0.05f),
            size = canvasWidth * 0.08f,
            state = state,
            glowIntensity = RoboAnimations.getNoseGlowIntensity(state, animationTime)
        )

        // Draw mouth
        drawRoboMouth(
            center = Offset(centerX, centerY + canvasHeight * 0.25f),
            width = canvasWidth * 0.5f,
            state = state,
            animationTime = animationTime
        )
    }
}

/**
 * Draw a single robo eye with concentric rings and glow effect
 */
private fun DrawScope.drawRoboEye(
    center: Offset,
    baseRadius: Float,
    state: RoboState,
    pulseScale: Float,
    glowIntensity: Float,
    rotation: Float
) {
    val radius = baseRadius * pulseScale

    val primaryColor = RoboAnimations.getPrimaryColor(state)
    val secondaryColor = RoboAnimations.getSecondaryColor(state)
    val coreColor = RoboAnimations.getCoreColor(state)

    rotate(rotation, center) {
        // Outer glow (largest)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.3f
            ),
            radius = radius * 1.3f,
            center = center
        )

        // Outer cyan neon ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.8f * glowIntensity),
            radius = radius,
            center = center,
            style = Stroke(width = radius * 0.08f)
        )

        // Middle ring (blue processing layer)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.6f * glowIntensity),
                    secondaryColor.copy(alpha = 0.3f * glowIntensity)
                ),
                center = center,
                radius = radius * 0.75f
            ),
            radius = radius * 0.75f,
            center = center
        )

        // Inner ring outline
        drawCircle(
            color = primaryColor.copy(alpha = 0.6f * glowIntensity),
            radius = radius * 0.5f,
            center = center,
            style = Stroke(width = radius * 0.05f)
        )

        // Core (bright white/colored center)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coreColor.copy(alpha = glowIntensity),
                    secondaryColor.copy(alpha = 0.8f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.35f
            ),
            radius = radius * 0.35f,
            center = center
        )

        // Circuit-like radial lines
        drawCircuitLines(center, radius, primaryColor, glowIntensity)
    }
}

/**
 * Draw radial circuit lines inside the eye
 */
private fun DrawScope.drawCircuitLines(
    center: Offset,
    radius: Float,
    color: Color,
    intensity: Float
) {
    val lineCount = 8
    for (i in 0 until lineCount) {
        val angle = (i * 360f / lineCount) * Math.PI / 180.0
        val startRadius = radius * 0.4f
        val endRadius = radius * 0.7f

        drawLine(
            color = color.copy(alpha = 0.3f * intensity),
            start = Offset(
                center.x + (startRadius * cos(angle)).toFloat(),
                center.y + (startRadius * sin(angle)).toFloat()
            ),
            end = Offset(
                center.x + (endRadius * cos(angle)).toFloat(),
                center.y + (endRadius * sin(angle)).toFloat()
            ),
            strokeWidth = 2f
        )

        // Small dots at end
        drawCircle(
            color = color.copy(alpha = 0.6f * intensity),
            radius = 3f,
            center = Offset(
                center.x + (endRadius * cos(angle)).toFloat(),
                center.y + (endRadius * sin(angle)).toFloat()
            )
        )
    }
}

/**
 * Draw geometric nose with glowing sensor dot
 */
private fun DrawScope.drawRoboNose(
    center: Offset,
    size: Float,
    state: RoboState,
    glowIntensity: Float
) {
    val primaryColor = RoboAnimations.getPrimaryColor(state)

    // Inverted V shape
    val path = Path().apply {
        moveTo(center.x - size * 0.5f, center.y + size * 0.3f)
        lineTo(center.x, center.y - size * 0.5f)
        lineTo(center.x + size * 0.5f, center.y + size * 0.3f)
    }

    drawPath(
        path = path,
        color = primaryColor.copy(alpha = 0.6f * glowIntensity),
        style = Stroke(width = 4f)
    )

    // Glowing sensor dot below nose
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = glowIntensity),
                primaryColor.copy(alpha = 0.3f * glowIntensity),
                Color.Transparent
            ),
            center = Offset(center.x, center.y + size * 0.6f),
            radius = size * 0.3f
        ),
        radius = size * 0.3f,
        center = Offset(center.x, center.y + size * 0.6f)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.8f * glowIntensity),
        radius = size * 0.15f,
        center = Offset(center.x, center.y + size * 0.6f)
    )
}

/**
 * Draw equalizer-style mouth bars
 */
private fun DrawScope.drawRoboMouth(
    center: Offset,
    width: Float,
    state: RoboState,
    animationTime: Long
) {
    val barCount = 9
    val barWidth = width / (barCount * 1.5f)
    val spacing = width / barCount
    val maxBarHeight = width * 0.4f

    val primaryColor = RoboAnimations.getPrimaryColor(state)

    for (i in 0 until barCount) {
        val barX = center.x - width / 2f + i * spacing

        // Get animated height for this bar
        val heightMultiplier = RoboAnimations.getMouthBarHeight(
            state = state,
            barIndex = i,
            barCount = barCount,
            time = animationTime
        )

        val barHeight = maxBarHeight * heightMultiplier
        val brightness = RoboAnimations.getMouthBarBrightness(state, heightMultiplier)

        // Draw bar with gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = brightness),
                    primaryColor.copy(alpha = brightness * 0.5f)
                ),
                startY = center.y - barHeight / 2f,
                endY = center.y + barHeight / 2f
            ),
            topLeft = Offset(barX, center.y - barHeight / 2f),
            size = Size(barWidth, barHeight)
        )

        // Glow effect on top
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = brightness * 0.6f),
                    Color.Transparent
                ),
                startY = center.y - barHeight / 2f,
                endY = center.y - barHeight / 2f + barHeight * 0.3f
            ),
            topLeft = Offset(barX, center.y - barHeight / 2f),
            size = Size(barWidth, barHeight * 0.3f)
        )
    }
}

