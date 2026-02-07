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
 * TASK 2: Native Vector-Based Robo Face (AIMER Society Internship Challenge)
 *
 * DESIGN PHILOSOPHY:
 * This is a REFERENCE-INSPIRED implementation, NOT a pixel-perfect copy.
 * Focus is on engineering quality, state-driven architecture, and animation systems.
 *
 * IMPLEMENTATION APPROACH:
 * - 100% programmatic rendering using Jetpack Compose Canvas
 * - NO image assets, NO SVG imports, NO bitmap usage
 * - All visuals generated via shapes, paths, and gradients
 * - State-driven behavior (RoboState → visual properties)
 * - Smooth 60 FPS animations with performance awareness
 *
 * COMPONENTS (Reference-Inspired Structure):
 * - Eyes: Concentric circular layers with radial glow effects
 *   └─ Circuit-like radial lines for futuristic tech aesthetic
 * - Nose: Minimal geometric triangle (inverted V) with glowing sensor dot
 * - Mouth: Digital equalizer-style animated bars (9 bars)
 *
 * STATE-DRIVEN RENDERING:
 * All visual properties (colors, pulse speed, glow intensity, animation patterns)
 * are determined by RoboState, NOT hardcoded in drawing functions.
 * This ensures extensibility and clean separation of concerns.
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
 *
 * DESIGN DECISIONS (Reference-Inspired, NOT Pixel-Perfect):
 * - Structure: Concentric circular layers mimicking camera lens/robotic optics
 * - Glow: Radial gradients create atmospheric, futuristic aesthetic
 * - Circuit Lines: Radial patterns add technological detail (inspired by reference)
 * - Color: State-driven via RoboAnimations.getPrimaryColor()
 * - Animation: Pulse scale, glow intensity, rotation all parameterized by state
 *
 * LAYERS (Outside → Inside):
 * 1. Outermost glow aura (diffuse radial gradient)
 * 2. Outer neon ring (main border, thick stroke)
 * 3-5. Secondary rings (depth and separation)
 * 6. Dark background (contrast)
 * 7. Middle processing layer (blue gradient)
 * 8. Circuit lines and tech details (HUD-like elements)
 * 9. Inner identity ring (cyan outline)
 * 10. Core energy center (bright white/blue glow)
 *
 * STATE-DRIVEN:
 * All visual properties (radius, color, glow) come from RoboState via RoboAnimations.
 * This is NOT hardcoded - it's engineered to be extensible and maintainable.
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
        // Outermost glow aura (most diffuse)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f * glowIntensity),
                    primaryColor.copy(alpha = 0.15f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = center
        )

        // Outer cyan neon ring - main border (thickest)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 1.0f * glowIntensity),
                    primaryColor.copy(alpha = 0.8f * glowIntensity)
                ),
                center = center,
                radius = radius * 1.08f
            ),
            radius = radius * 1.08f,
            center = center,
            style = Stroke(width = radius * 0.12f)
        )

        // Secondary bright ring (inner edge of outer ring)
        drawCircle(
            color = primaryColor.copy(alpha = 0.6f * glowIntensity),
            radius = radius * 0.96f,
            center = center,
            style = Stroke(width = radius * 0.02f)
        )

        // Tertiary dark ring
        drawCircle(
            color = Color(0xFF1A3A4A).copy(alpha = 0.8f),
            radius = radius * 0.90f,
            center = center,
            style = Stroke(width = radius * 0.06f)
        )

        // Dark background circle for contrast and depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF08111C),
                    Color(0xFF0A0F1E),
                    Color(0xFF050A14)
                ),
                center = center,
                radius = radius * 0.87f
            ),
            radius = radius * 0.87f,
            center = center
        )

        // Middle processing layer - blue ring with gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2E5C8F).copy(alpha = 0.5f * glowIntensity),
                    Color(0xFF1A3D5F).copy(alpha = 0.7f * glowIntensity),
                    Color(0xFF0F2236).copy(alpha = 0.9f)
                ),
                center = center,
                radius = radius * 0.78f
            ),
            radius = radius * 0.78f,
            center = center
        )

        // Blue ring border (bright)
        drawCircle(
            color = Color(0xFF4A90E2).copy(alpha = 0.9f * glowIntensity),
            radius = radius * 0.78f,
            center = center,
            style = Stroke(width = radius * 0.05f)
        )

        // Inner thin ring separator
        drawCircle(
            color = Color(0xFF2A5A8A).copy(alpha = 0.5f * glowIntensity),
            radius = radius * 0.68f,
            center = center,
            style = Stroke(width = radius * 0.02f)
        )

        // Inner identity layer background (darkest)
        drawCircle(
            color = Color(0xFF080E18),
            radius = radius * 0.60f,
            center = center
        )

        // Circuit-like radial lines and dots (BEFORE inner ring so they appear behind)
        drawCircuitLines(center, radius, primaryColor, glowIntensity)

        // Additional tech details (arcs, dots, patterns)
        drawTechDetails(center, radius, primaryColor, secondaryColor, glowIntensity)

        // Inner cyan ring outline (identity layer)
        drawCircle(
            color = primaryColor.copy(alpha = 0.85f * glowIntensity),
            radius = radius * 0.56f,
            center = center,
            style = Stroke(width = radius * 0.08f)
        )

        // Inner ring glow
        drawCircle(
            color = primaryColor.copy(alpha = 0.3f * glowIntensity),
            radius = radius * 0.50f,
            center = center,
            style = Stroke(width = radius * 0.04f)
        )

        // Core energy background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.6f * glowIntensity),
                    Color(0xFF1A2B4D).copy(alpha = 0.8f),
                    Color(0xFF0D1622).copy(alpha = 1.0f)
                ),
                center = center,
                radius = radius * 0.35f
            ),
            radius = radius * 0.35f,
            center = center
        )

        // Core (bright white/blue energy source)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = glowIntensity),
                    Color(0xFFE0F4FF).copy(alpha = 0.95f * glowIntensity),
                    coreColor.copy(alpha = 0.85f * glowIntensity),
                    secondaryColor.copy(alpha = 0.6f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.30f
            ),
            radius = radius * 0.30f,
            center = center
        )

        // Core inner bright ring
        drawCircle(
            color = Color(0xFFB0D8FF).copy(alpha = 0.9f * glowIntensity),
            radius = radius * 0.22f,
            center = center,
            style = Stroke(width = radius * 0.03f)
        )

        // Core highlight (brightest central point)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 1.0f * glowIntensity),
                    Color(0xFFFFFDFA).copy(alpha = 0.85f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.14f
            ),
            radius = radius * 0.14f,
            center = center
        )
    }
}

/**
 * Draw radial circuit lines inside the eye - Enhanced with dots and arcs
 */
private fun DrawScope.drawCircuitLines(
    center: Offset,
    radius: Float,
    color: Color,
    intensity: Float
) {
    val lineCount = 16 // Increased for more detail
    for (i in 0 until lineCount) {
        val angle = (i * 360f / lineCount) * Math.PI / 180.0
        val startRadius = radius * 0.38f
        val midRadius = radius * 0.56f
        val endRadius = radius * 0.72f

        // Main radial lines (thin)
        drawLine(
            color = color.copy(alpha = 0.35f * intensity),
            start = Offset(
                center.x + (startRadius * cos(angle)).toFloat(),
                center.y + (startRadius * sin(angle)).toFloat()
            ),
            end = Offset(
                center.x + (endRadius * cos(angle)).toFloat(),
                center.y + (endRadius * sin(angle)).toFloat()
            ),
            strokeWidth = 1.5f
        )

        // Every other line gets thicker emphasis
        if (i % 2 == 0) {
            drawLine(
                color = color.copy(alpha = 0.5f * intensity),
                start = Offset(
                    center.x + (startRadius * cos(angle)).toFloat(),
                    center.y + (startRadius * sin(angle)).toFloat()
                ),
                end = Offset(
                    center.x + (midRadius * cos(angle)).toFloat(),
                    center.y + (midRadius * sin(angle)).toFloat()
                ),
                strokeWidth = 2.5f
            )
        }

        // End point dots (bright)
        drawCircle(
            color = color.copy(alpha = 0.8f * intensity),
            radius = if (i % 4 == 0) 4f else 2.5f,
            center = Offset(
                center.x + (endRadius * cos(angle)).toFloat(),
                center.y + (endRadius * sin(angle)).toFloat()
            )
        )

        // Mid-way dots (dimmer, smaller)
        if (i % 3 == 0) {
            drawCircle(
                color = color.copy(alpha = 0.5f * intensity),
                radius = 2f,
                center = Offset(
                    center.x + (midRadius * cos(angle)).toFloat(),
                    center.y + (midRadius * sin(angle)).toFloat()
                )
            )
        }

        // Inner connection dots
        if (i % 4 == 0) {
            drawCircle(
                color = Color(0xFF4A90E2).copy(alpha = 0.6f * intensity),
                radius = 2.5f,
                center = Offset(
                    center.x + (startRadius * cos(angle)).toFloat(),
                    center.y + (startRadius * sin(angle)).toFloat()
                )
            )
        }
    }

    // Additional curved connector lines (like circuit traces)
    for (i in 0 until 8) {
        val angle = (i * 45f) * Math.PI / 180.0
        val arcCenter = Offset(
            center.x + (radius * 0.60f * cos(angle)).toFloat(),
            center.y + (radius * 0.60f * sin(angle)).toFloat()
        )

        drawCircle(
            color = color.copy(alpha = 0.25f * intensity),
            radius = radius * 0.08f,
            center = arcCenter,
            style = Stroke(width = 1f)
        )
    }
}

/**
 * Draw additional tech details - small arcs, dots, and circuit patterns
 * Enhanced with HUD-like elements and data visualization patterns
 */
private fun DrawScope.drawTechDetails(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    secondaryColor: Color,
    intensity: Float
) {
    // Outer arc segments (circuit traces) - more visible
    val arcCount = 8
    for (i in 0 until arcCount) {
        val startAngle = i * 45f + 12f
        val sweepAngle = 28f
        val arcRadius = radius * 0.82f

        drawArc(
            color = primaryColor.copy(alpha = 0.45f * intensity),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 2f)
        )
    }

    // Middle arc segments (processing indicators)
    for (i in 0 until 6) {
        val startAngle = i * 60f + 20f
        val sweepAngle = 35f
        val arcRadius = radius * 0.65f

        drawArc(
            color = Color(0xFF3A7ABF).copy(alpha = 0.4f * intensity),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 1.5f)
        )
    }

    // Small glowing tech dots around middle ring (enhanced distribution)
    val dotCount = 20
    val dotRingRadius = radius * 0.70f
    for (i in 0 until dotCount) {
        val angle = (i * 360f / dotCount) * Math.PI / 180.0
        val dotSize = when {
            i % 5 == 0 -> 3.5f // Larger marker dots
            i % 2 == 0 -> 2.5f
            else -> 1.8f
        }

        val dotColor = if (i % 5 == 0) primaryColor else secondaryColor

        drawCircle(
            color = dotColor.copy(alpha = 0.7f * intensity),
            radius = dotSize,
            center = Offset(
                center.x + (dotRingRadius * cos(angle)).toFloat(),
                center.y + (dotRingRadius * sin(angle)).toFloat()
            )
        )

        // Add glow to larger dots
        if (i % 5 == 0) {
            drawCircle(
                color = dotColor.copy(alpha = 0.3f * intensity),
                radius = dotSize * 2f,
                center = Offset(
                    center.x + (dotRingRadius * cos(angle)).toFloat(),
                    center.y + (dotRingRadius * sin(angle)).toFloat()
                )
            )
        }
    }

    // Inner ring data points (HUD-style indicators)
    val dataPointCount = 12
    val dataRingRadius = radius * 0.48f
    for (i in 0 until dataPointCount) {
        val angle = (i * 360f / dataPointCount + 15f) * Math.PI / 180.0

        // Small rectangular data indicators
        val rectWidth = radius * 0.025f
        val rectHeight = radius * 0.06f

        drawRect(
            color = if (i % 3 == 0)
                primaryColor.copy(alpha = 0.6f * intensity)
            else
                Color(0xFF4A90E2).copy(alpha = 0.4f * intensity),
            topLeft = Offset(
                center.x + (dataRingRadius * cos(angle)).toFloat() - rectWidth / 2f,
                center.y + (dataRingRadius * sin(angle)).toFloat() - rectHeight / 2f
            ),
            size = Size(rectWidth, rectHeight)
        )
    }

    // Subtle scanning arc (rotates based on state)
    val scanAngle = ((System.currentTimeMillis() / 30) % 360).toFloat()
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                primaryColor.copy(alpha = 0.2f * intensity),
                primaryColor.copy(alpha = 0.5f * intensity),
                Color.Transparent
            ),
            center = center
        ),
        startAngle = scanAngle,
        sweepAngle = 60f,
        useCenter = true,
        topLeft = Offset(center.x - radius * 0.58f, center.y - radius * 0.58f),
        size = Size(radius * 1.16f, radius * 1.16f),
        alpha = 0.3f
    )
}

/**
 * Draw geometric nose with glowing sensor dot
 *
 * DESIGN DECISIONS:
 * - Shape: Inverted V (upward triangle) - minimal, geometric, robotic
 * - Implementation: Path-based vector drawing (3 points)
 * - Glow Layers: Outer glow → Main outline → Inner accent
 * - Sensor Dot: Positioned below nose, represents proximity/environment sensor
 *
 * STATE-DRIVEN:
 * - Glow intensity varies by state (bright in Curious, dim in Sleep)
 * - Color from RoboAnimations.getPrimaryColor(state)
 * - Pulsing animation in Curious state via getNoseGlowIntensity()
 *
 * REFERENCE-INSPIRED:
 * Structure inspired by reference image, but engineered for state-driven behavior.
 * NOT a pixel-perfect copy - focuses on geometric simplicity and animation.
 */
private fun DrawScope.drawRoboNose(
    center: Offset,
    size: Float,
    state: RoboState,
    glowIntensity: Float
) {
    val primaryColor = RoboAnimations.getPrimaryColor(state)

    // Inverted V shape (upward pointing)
    val path = Path().apply {
        moveTo(center.x - size * 0.6f, center.y + size * 0.4f)
        lineTo(center.x, center.y - size * 0.6f)
        lineTo(center.x + size * 0.6f, center.y + size * 0.4f)
    }

    // Outer glow for the nose outline
    drawPath(
        path = path,
        color = primaryColor.copy(alpha = 0.3f * glowIntensity),
        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Main nose outline (bright)
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.9f * glowIntensity),
        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner accent line
    drawPath(
        path = path,
        color = primaryColor.copy(alpha = 0.7f * glowIntensity),
        style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Glowing sensor dot below nose (larger glow)
    val dotCenter = Offset(center.x, center.y + size * 0.7f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = glowIntensity * 0.8f),
                primaryColor.copy(alpha = 0.4f * glowIntensity),
                primaryColor.copy(alpha = 0.2f * glowIntensity),
                Color.Transparent
            ),
            center = dotCenter,
            radius = size * 0.5f
        ),
        radius = size * 0.5f,
        center = dotCenter
    )

    // Mid glow layer
    drawCircle(
        color = primaryColor.copy(alpha = 0.9f * glowIntensity),
        radius = size * 0.25f,
        center = dotCenter
    )

    // Core bright dot
    drawCircle(
        color = Color.White.copy(alpha = 0.95f * glowIntensity),
        radius = size * 0.18f,
        center = dotCenter
    )

    // Highlight spot
    drawCircle(
        color = Color.White.copy(alpha = glowIntensity),
        radius = size * 0.08f,
        center = dotCenter
    )
}

/**
 * Draw equalizer-style mouth bars
 *
 * DESIGN DECISIONS:
 * - Style: Digital equalizer (9 animated bars)
 * - Justification: Represents digital/AI nature, highly expressive for emotions
 * - Animation: Wave pattern with phase offset (each bar animates independently)
 * - Visual Layers: Glow → Main gradient → Top highlight (reflection)
 *
 * STATE-DRIVEN ANIMATION PATTERNS:
 * - Idle:    Gentle wave (2s cycle), low amplitude
 * - Curious: Medium wave (1.5s), moderate amplitude
 * - Happy:   Smooth bounce (1s), high amplitude, bright
 * - Angry:   Jagged, sharp movement (0.4s), fast and aggressive
 * - Sleep:   Minimal height (0.1x), barely visible
 *
 * IMPLEMENTATION:
 * - Animation height from RoboAnimations.getMouthBarHeight()
 * - Brightness correlates with bar height (taller = brighter)
 * - Rounded corners for smoother, modern look
 *
 * REFERENCE-INSPIRED:
 * Equalizer bars match the digital aesthetic of the reference image.
 * Engineered for smooth animation and state-driven behavior, NOT hardcoded visuals.
 */
private fun DrawScope.drawRoboMouth(
    center: Offset,
    width: Float,
    state: RoboState,
    animationTime: Long
) {
    val barCount = 9
    val barWidth = width / (barCount * 1.6f)
    val spacing = width / barCount
    val maxBarHeight = width * 0.45f

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

        // Glow behind bar
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = brightness * 0.3f),
                    primaryColor.copy(alpha = brightness * 0.1f),
                    Color.Transparent
                ),
                startY = center.y - barHeight / 2f - 5f,
                endY = center.y + barHeight / 2f + 5f
            ),
            topLeft = Offset(barX - 2f, center.y - barHeight / 2f - 5f),
            size = Size(barWidth + 4f, barHeight + 10f)
        )

        // Main bar with gradient
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = brightness),
                    primaryColor.copy(alpha = brightness * 0.7f),
                    primaryColor.copy(alpha = brightness * 0.5f)
                ),
                startY = center.y - barHeight / 2f,
                endY = center.y + barHeight / 2f
            ),
            topLeft = Offset(barX, center.y - barHeight / 2f),
            size = Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.15f, barWidth * 0.15f)
        )

        // Top highlight reflection
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = brightness * 0.7f),
                    Color.White.copy(alpha = brightness * 0.3f),
                    Color.Transparent
                ),
                startY = center.y - barHeight / 2f,
                endY = center.y - barHeight / 2f + barHeight * 0.4f
            ),
            topLeft = Offset(barX, center.y - barHeight / 2f),
            size = Size(barWidth, barHeight * 0.4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.15f, barWidth * 0.15f)
        )
    }
}

