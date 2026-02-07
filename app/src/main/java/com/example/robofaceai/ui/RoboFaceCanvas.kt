package com.example.robofaceai.ui

import androidx.compose.animation.core.*
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
import com.example.robofaceai.domain.RoboState
import kotlin.math.*

/**
 * TASK 2 & 3: NATIVE VECTOR ROBO FACE WITH SENSOR INTERACTION
 * Professional implementation with real-time sensor fusion
 * Pure Jetpack Compose Canvas - NO IMAGES
 *
 * Visual Components:
 * - Eyes: Concentric rings with circuit dots and bright core
 * - Nose: Inverted V chevron with glowing dot
 * - Mouth: Equalizer-style rectangular bars
 * - All state-driven with smooth animations
 *
 * Sensor Integration (Task 3):
 * - Eyes follow device tilt in real-time
 * - Physics-based spring damping for natural movement
 * - Head rotation effect from gyroscope
 */

@Composable
fun RoboFaceCanvas(
    state: RoboState,
    tiltX: Float = 0f,        // Sensor input: -1 (left) to 1 (right)
    tiltY: Float = 0f,        // Sensor input: -1 (forward) to 1 (backward)
    headRotation: Float = 0f, // Gyroscope input: rotation in degrees
    modifier: Modifier = Modifier
) {
    // Animation configuration based on current state
    val animationConfig = rememberAnimationConfig(state)

    // Infinite pulse animation for eyes
    val infiniteTransition = rememberInfiniteTransition(label = "robo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = animationConfig.pulseTarget,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationConfig.pulseDuration,
                easing = animationConfig.pulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotation animation for curious state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (state == RoboState.Curious) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Mouth bars animation
    val mouthAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationConfig.mouthSpeed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "mouth_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val eyeY = size.height * 0.35f
        val eyeSpacing = size.width * 0.25f
        val eyeRadius = size.width * 0.12f

        // === TASK 3: SENSOR-DRIVEN EYE MOVEMENT (ENHANCED FOR DRAMATIC EFFECT) ===
        // Calculate eye offset from tilt (with natural limits)
        val maxEyeOffset = eyeRadius * 0.55f // OPTIMIZED: Eyes can move 55% of radius (was 35%)
        val eyeOffsetX = tiltX * maxEyeOffset
        val eyeOffsetY = -tiltY * maxEyeOffset // Invert Y for natural feel

        // Add subtle micro-movements (breathing effect when idle)
        val breathePhase = sin(mouthAnimPhase * 0.5f)
        val microOffsetX = if (state == RoboState.Idle || state == RoboState.Sleep) {
            breathePhase * 2f
        } else 0f
        val microOffsetY = if (state == RoboState.Idle || state == RoboState.Sleep) {
            cos(mouthAnimPhase * 0.5f) * 1.5f
        } else 0f

        // === ENHANCED BACKGROUND GRADIENT (emotion-driven) ===
        val bgGradient = when (state) {
            RoboState.Angry -> Brush.radialGradient(
                0f to Color(0xFF1a0a0a), // Dark red center
                0.5f to Color(0xFF0a0505),
                1f to Color.Black,
                center = Offset(centerX, size.height * 0.5f),
                radius = size.width * 0.8f
            )
            RoboState.Happy -> Brush.radialGradient(
                0f to Color(0xFF0a1a15), // Dark green-cyan
                0.5f to Color(0xFF050a08),
                1f to Color.Black,
                center = Offset(centerX, size.height * 0.5f),
                radius = size.width * 0.8f
            )
            else -> Brush.radialGradient(
                0f to Color(0xFF0a1520), // Dark blue
                0.5f to Color(0xFF050a10),
                1f to Color.Black,
                center = Offset(centerX, size.height * 0.5f),
                radius = size.width * 0.8f
            )
        }

        drawRect(brush = bgGradient)

        // Background energy field for enhanced visuals
        drawEnergyField(
            center = Offset(centerX, size.height * 0.5f),
            radius = size.width * 0.45f,
            time = mouthAnimPhase,
            animConfig = animationConfig
        )

        // Background hexagon frame for futuristic look
        drawHexagonFrame(
            center = Offset(centerX, size.height * 0.5f),
            radius = size.width * 0.4f,
            animConfig = animationConfig
        )

        // Connection lines between components (head wiring)
        drawConnectionLines(
            centerX = centerX,
            eyeY = eyeY,
            eyeSpacing = eyeSpacing,
            noseY = size.height * 0.55f,
            mouthY = size.height * 0.68f,
            animConfig = animationConfig
        )

        // Draw left eye (with sensor-driven offset)
        drawRoboEye(
            center = Offset(
                centerX - eyeSpacing + eyeOffsetX + microOffsetX,
                eyeY + eyeOffsetY + microOffsetY
            ),
            radius = eyeRadius,
            pulseScale = pulseScale,
            rotation = rotation + headRotation * 0.6f, // OPTIMIZED: More visible head tilt (was 0.3)
            animConfig = animationConfig
        )

        // Draw right eye (with sensor-driven offset)
        drawRoboEye(
            center = Offset(
                centerX + eyeSpacing + eyeOffsetX + microOffsetX,
                eyeY + eyeOffsetY + microOffsetY
            ),
            radius = eyeRadius,
            pulseScale = pulseScale,
            rotation = rotation + headRotation * 0.6f, // OPTIMIZED: More visible head tilt (was 0.3)
            animConfig = animationConfig
        )

        // Draw nose
        drawRoboNose(
            centerX = centerX,
            topY = size.height * 0.55f,
            size = size.width * 0.08f,
            animConfig = animationConfig
        )

        // Draw mouth
        drawRoboMouth(
            centerX = centerX,
            topY = size.height * 0.68f,
            width = size.width * 0.5f,
            phase = mouthAnimPhase,
            animConfig = animationConfig
        )

        // Floating particles for extra effect
        drawParticleEffects(
            center = Offset(centerX, size.height * 0.5f),
            time = mouthAnimPhase,
            animConfig = animationConfig
        )

        // === SCAN LINE EFFECT (moving horizontal line) ===
        val scanLineY = (mouthAnimPhase * size.height / (2f * PI.toFloat())) % size.height
        drawLine(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                0.3f to animationConfig.primaryColor.copy(alpha = 0.15f * animationConfig.brightness),
                0.5f to animationConfig.primaryColor.copy(alpha = 0.3f * animationConfig.brightness),
                0.7f to animationConfig.primaryColor.copy(alpha = 0.15f * animationConfig.brightness),
                1f to Color.Transparent,
                startX = 0f,
                endX = size.width
            ),
            start = Offset(0f, scanLineY),
            end = Offset(size.width, scanLineY),
            strokeWidth = 2f
        )

        // === SCREEN OVERLAY EFFECT (subtle noise) ===
        drawRect(
            color = animationConfig.primaryColor.copy(alpha = 0.02f * animationConfig.brightness),
            topLeft = Offset.Zero,
            size = size
        )
    }
}

/**
 * Draw robo eye MATCHING REFERENCE IMAGE EXACTLY
 * Professional implementation with precise concentric rings, circuit details, and glowing effects
 */
private fun DrawScope.drawRoboEye(
    center: Offset,
    radius: Float,
    pulseScale: Float,
    rotation: Float,
    animConfig: AnimationConfig
) {
    val effectiveRadius = radius * pulseScale
    val darkBg = Color(0xFF0A1628) // Dark navy from reference
    val deepBlue = Color(0xFF1a3a5c) // Deep blue for inner areas

    // === LAYER 1: Extended outer glow (subtle cyan aura) ===
    drawCircle(
        brush = Brush.radialGradient(
            0.75f to animConfig.primaryColor.copy(alpha = 0.25f * animConfig.brightness),
            0.9f to animConfig.primaryColor.copy(alpha = 0.1f * animConfig.brightness),
            1f to Color.Transparent,
            center = center,
            radius = effectiveRadius * 1.15f
        ),
        radius = effectiveRadius * 1.15f,
        center = center
    )

    // === LAYER 2: OUTERMOST THICK CYAN RING (identity layer) ===
    // This is the most prominent feature in the reference image
    val outerRingThickness = effectiveRadius * 0.12f

    // Fill base dark circle
    drawCircle(
        color = darkBg,
        radius = effectiveRadius,
        center = center
    )

    // Main cyan ring with glow
    drawCircle(
        color = animConfig.primaryColor.copy(alpha = animConfig.brightness * 0.95f),
        radius = effectiveRadius,
        center = center,
        style = Stroke(width = outerRingThickness)
    )

    // Inner glow of outer ring
    drawCircle(
        brush = Brush.radialGradient(
            0.86f to Color.Transparent,
            0.91f to animConfig.primaryColor.copy(alpha = 0.6f * animConfig.brightness),
            0.95f to animConfig.primaryColor.copy(alpha = 0.3f * animConfig.brightness),
            1f to Color.Transparent,
            center = center,
            radius = effectiveRadius
        ),
        radius = effectiveRadius,
        center = center
    )

    // === LAYER 3: Dark separator ring (space between rings) ===
    drawCircle(
        color = darkBg.copy(alpha = 0.95f),
        radius = effectiveRadius * 0.82f,
        center = center
    )

    // === LAYER 4: SECOND DARK RING (structure layer) ===
    val secondRingRadius = effectiveRadius * 0.72f
    drawCircle(
        color = deepBlue.copy(alpha = 0.7f),
        radius = secondRingRadius,
        center = center
    )
    // Subtle edge highlight
    drawCircle(
        color = animConfig.primaryColor.copy(alpha = 0.25f * animConfig.brightness),
        radius = secondRingRadius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // === LAYER 5: THIRD PROCESSING RING (thinner dark blue) ===
    val thirdRingRadius = effectiveRadius * 0.58f
    drawCircle(
        color = Color(0xFF0D2844).copy(alpha = 0.8f),
        radius = thirdRingRadius,
        center = center
    )
    drawCircle(
        color = Color(0xFF2a5a8a).copy(alpha = 0.4f * animConfig.brightness),
        radius = thirdRingRadius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // === LAYER 6: BLUE PROCESSING RING (glowing blue) ===
    val blueRingRadius = effectiveRadius * 0.45f
    val blueRingThickness = effectiveRadius * 0.08f

    // Dark fill inside
    drawCircle(
        color = darkBg,
        radius = blueRingRadius,
        center = center
    )

    // Glowing blue ring
    drawCircle(
        brush = Brush.radialGradient(
            0f to animConfig.secondaryColor.copy(alpha = 0.3f),
            0.85f to animConfig.secondaryColor.copy(alpha = animConfig.brightness * 0.9f),
            1f to animConfig.secondaryColor.copy(alpha = animConfig.brightness * 0.6f),
            center = center,
            radius = blueRingRadius
        ),
        radius = blueRingRadius,
        center = center,
        style = Stroke(width = blueRingThickness)
    )

    // === LAYER 7: Inner dark circle ===
    drawCircle(
        color = darkBg,
        radius = effectiveRadius * 0.30f,
        center = center
    )

    // === LAYER 7: BRIGHT WHITE CORE (energy source) ===
    val coreRadius = effectiveRadius * 0.22f
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = animConfig.brightness),
            0.4f to Color.White.copy(alpha = animConfig.brightness * 0.9f),
            0.7f to animConfig.coreColor.copy(alpha = 0.7f),
            1f to animConfig.coreColor.copy(alpha = 0.3f),
            center = center,
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )

    // === CIRCUIT DETAILS: Dots, dashes, and radial lines (matching reference image EXACTLY) ===
    rotate(rotation * 0.3f, center) {

        // === Outer ring scattered dots (like in reference) ===
        val outerDotPositions = listOf(8f, 22f, 45f, 68f, 82f, 105f, 128f, 145f, 172f, 195f, 212f, 235f, 262f, 285f, 305f, 325f, 345f, 352f)
        outerDotPositions.forEach { angle ->
            val dotRadius = effectiveRadius * 0.82f
            val dotPos = Offset(
                center.x + cos(angle.toRadians()) * dotRadius,
                center.y + sin(angle.toRadians()) * dotRadius
            )
            drawCircle(
                color = animConfig.primaryColor.copy(alpha = 0.7f * animConfig.brightness),
                radius = 2.2f,
                center = dotPos
            )
        }

        // === Middle ring technical markings (dashes and dots) ===
        for (i in 0 until 16) {
            val angle = (i * 22.5f).toRadians()
            val markerRadius = effectiveRadius * 0.62f

            if (i % 4 == 0) {
                // Longer dash marks at cardinal positions
                val dashStart = markerRadius - 10f
                val dashEnd = markerRadius + 10f
                drawLine(
                    color = animConfig.primaryColor.copy(alpha = 0.5f * animConfig.brightness),
                    start = Offset(
                        center.x + cos(angle) * dashStart,
                        center.y + sin(angle) * dashStart
                    ),
                    end = Offset(
                        center.x + cos(angle) * dashEnd,
                        center.y + sin(angle) * dashEnd
                    ),
                    strokeWidth = 2f
                )
            } else if (i % 2 == 0) {
                // Medium dashes
                val dashStart = markerRadius - 6f
                val dashEnd = markerRadius + 6f
                drawLine(
                    color = animConfig.primaryColor.copy(alpha = 0.4f * animConfig.brightness),
                    start = Offset(
                        center.x + cos(angle) * dashStart,
                        center.y + sin(angle) * dashStart
                    ),
                    end = Offset(
                        center.x + cos(angle) * dashEnd,
                        center.y + sin(angle) * dashEnd
                    ),
                    strokeWidth = 1.5f
                )
            } else {
                // Small dots
                val dotPos = Offset(
                    center.x + cos(angle) * markerRadius,
                    center.y + sin(angle) * markerRadius
                )
                drawCircle(
                    color = animConfig.primaryColor.copy(alpha = 0.6f * animConfig.brightness),
                    radius = 2f,
                    center = dotPos
                )
            }
        }

        // === Inner ring circuit dots (blue accents) ===
        val innerDotCount = 12
        for (i in 0 until innerDotCount) {
            val angle = (i * 30f).toRadians()
            val dotRadius = blueRingRadius * 0.88f
            val dotPos = Offset(
                center.x + cos(angle) * dotRadius,
                center.y + sin(angle) * dotRadius
            )
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.75f * animConfig.brightness),
                radius = if (i % 3 == 0) 2.5f else 1.8f,
                center = dotPos
            )
        }

        // === Radial lines from core (technical detail) ===
        for (i in 0 until 8) {
            val angle = (i * 45f).toRadians()
            val lineStart = coreRadius * 1.2f
            val lineEnd = blueRingRadius * 0.7f

            drawLine(
                color = animConfig.secondaryColor.copy(alpha = 0.25f * animConfig.brightness),
                start = Offset(
                    center.x + cos(angle) * lineStart,
                    center.y + sin(angle) * lineStart
                ),
                end = Offset(
                    center.x + cos(angle) * lineEnd,
                    center.y + sin(angle) * lineEnd
                ),
                strokeWidth = 1f
            )
        }

        // === Floating particles in outer zone (animated) ===
        val particleAngles = listOf(30f, 95f, 160f, 210f, 280f, 340f)
        particleAngles.forEachIndexed { index, baseAngle ->
            val particleAngle = (baseAngle + rotation * (0.2f + index * 0.1f)).toRadians()
            val particleDistance = effectiveRadius * 0.68f
            val particlePos = Offset(
                center.x + cos(particleAngle) * particleDistance,
                center.y + sin(particleAngle) * particleDistance
            )
            drawCircle(
                color = animConfig.primaryColor.copy(alpha = 0.5f * animConfig.brightness),
                radius = 1.5f,
                center = particlePos
            )
        }
    }
}

/**
 * Draw minimalistic geometric nose with sensor node - ENHANCED
 */
private fun DrawScope.drawRoboNose(
    centerX: Float,
    topY: Float,
    size: Float,
    animConfig: AnimationConfig
) {
    val noseColor = animConfig.primaryColor.copy(alpha = 0.7f * animConfig.brightness)

    // === Main inverted V shape ===
    val path = Path().apply {
        moveTo(centerX - size * 0.6f, topY)
        lineTo(centerX, topY + size * 0.8f)
        lineTo(centerX + size * 0.6f, topY)
    }

    // Draw with glow
    drawPath(
        path = path,
        color = noseColor.copy(alpha = 0.3f),
        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = path,
        color = noseColor,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // === Sensor node dot below (glowing) ===
    val sensorPos = Offset(centerX, topY + size * 1.3f)

    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            0f to animConfig.primaryColor.copy(alpha = 0.6f * animConfig.brightness),
            0.5f to animConfig.primaryColor.copy(alpha = 0.3f * animConfig.brightness),
            1f to Color.Transparent,
            center = sensorPos,
            radius = 8f
        ),
        radius = 8f,
        center = sensorPos
    )

    // Core sensor dot
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = animConfig.brightness),
            0.5f to animConfig.primaryColor.copy(alpha = animConfig.brightness),
            1f to animConfig.primaryColor.copy(alpha = 0.6f * animConfig.brightness),
            center = sensorPos,
            radius = 4f
        ),
        radius = 4f,
        center = sensorPos
    )
}

/**
 * Draw hexagonal frame around the robo face for futuristic look - ENHANCED
 */
private fun DrawScope.drawHexagonFrame(
    center: Offset,
    radius: Float,
    animConfig: AnimationConfig
) {
    val hexPoints = mutableListOf<Offset>()

    // Build hexagon path and collect points
    val path = Path()
    for (i in 0 until 6) {
        val angle = (i * 60f - 30f).toRadians()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)

        hexPoints.add(Offset(x, y))

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    // Outer glow
    drawPath(
        path = path,
        color = animConfig.primaryColor.copy(alpha = 0.25f * animConfig.brightness),
        style = Stroke(width = 3f)
    )

    // Main frame
    drawPath(
        path = path,
        color = animConfig.primaryColor.copy(alpha = 0.4f * animConfig.brightness),
        style = Stroke(width = 1.5f)
    )

    // === Corner nodes at hexagon vertices ===
    hexPoints.forEach { point ->
        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                0f to animConfig.primaryColor.copy(alpha = 0.5f * animConfig.brightness),
                1f to Color.Transparent,
                center = point,
                radius = 8f
            ),
            radius = 8f,
            center = point
        )

        // Core node
        drawCircle(
            color = animConfig.primaryColor.copy(alpha = 0.7f * animConfig.brightness),
            radius = 3f,
            center = point
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f * animConfig.brightness),
            radius = 1.5f,
            center = point
        )
    }

    // === Inner hexagon (smaller) ===
    val innerPath = Path()
    for (i in 0 until 6) {
        val angle = (i * 60f - 30f).toRadians()
        val x = center.x + radius * 0.85f * cos(angle)
        val y = center.y + radius * 0.85f * sin(angle)

        if (i == 0) {
            innerPath.moveTo(x, y)
        } else {
            innerPath.lineTo(x, y)
        }
    }
    innerPath.close()

    drawPath(
        path = innerPath,
        color = animConfig.primaryColor.copy(alpha = 0.15f * animConfig.brightness),
        style = Stroke(width = 1f)
    )
}

/**
 * Draw floating particle effects around the face - ENHANCED
 */
private fun DrawScope.drawParticleEffects(
    center: Offset,
    time: Float,
    animConfig: AnimationConfig
) {
    if (animConfig.brightness < 0.3f) return // Skip in sleep mode

    // === Orbiting particles (larger circle) ===
    for (i in 0 until 16) {
        val angle = (time * 0.4f + i * 22.5f).toRadians()
        val distance = 220f + sin(time * 1.5f + i * 0.5f) * 30f
        val particleX = center.x + cos(angle) * distance
        val particleY = center.y + sin(angle) * distance

        val particleAlpha = (sin(time * 2f + i) * 0.5f + 0.5f) * 0.35f * animConfig.brightness
        val particleSize = if (i % 4 == 0) 3f else 2f

        // Particle glow
        drawCircle(
            brush = Brush.radialGradient(
                0f to animConfig.primaryColor.copy(alpha = particleAlpha),
                1f to Color.Transparent,
                center = Offset(particleX, particleY),
                radius = particleSize * 2f
            ),
            radius = particleSize * 2f,
            center = Offset(particleX, particleY)
        )

        // Particle core
        drawCircle(
            color = Color.White.copy(alpha = particleAlpha * 0.8f),
            radius = particleSize * 0.6f,
            center = Offset(particleX, particleY)
        )
    }

    // === Floating particles in close orbit ===
    for (i in 0 until 8) {
        val angle = (time * -0.6f + i * 45f).toRadians()
        val distance = 160f + cos(time + i * 0.8f) * 20f
        val particleX = center.x + cos(angle) * distance
        val particleY = center.y + sin(angle) * distance

        val particleAlpha = (cos(time * 1.5f + i * 0.7f) * 0.5f + 0.5f) * 0.4f * animConfig.brightness

        drawCircle(
            color = animConfig.secondaryColor.copy(alpha = particleAlpha),
            radius = 2.5f,
            center = Offset(particleX, particleY)
        )
    }
}

/**
 * Draw animated mouth bars (equalizer style) - ENHANCED VERSION
 * Individual bars animate based on state and phase with professional glow effects
 */
private fun DrawScope.drawRoboMouth(
    centerX: Float,
    topY: Float,
    width: Float,
    phase: Float,
    animConfig: AnimationConfig
) {
    val barCount = 9 // Matches reference image (5 visible bars with spacing)
    val barWidth = width / (barCount * 2f) // Wider bars
    val barSpacing = width / (barCount - 1)
    val maxBarHeight = 70f * animConfig.brightness
    val minBarHeight = 8f * animConfig.brightness

    for (i in 0 until barCount) {
        val xPos = centerX - width / 2f + i * barSpacing

        // Create sophisticated wave pattern based on phase and bar index
        val waveOffset = when (animConfig.mouthIntensity) {
            in 0.7f..1.0f -> {
                // Happy/Angry: bouncy wave or sharp peaks
                abs(sin(phase * 1.5f + i * 0.7f)) * animConfig.mouthIntensity
            }
            in 0.3f..0.5f -> {
                // Curious: smooth wave
                abs(sin(phase + i * 0.6f)) * 0.6f + 0.3f
            }
            else -> {
                // Idle/Sleeping: gentle low waves or minimal
                (sin(phase * 0.8f + i * 0.5f) * animConfig.mouthIntensity + animConfig.mouthIntensity).coerceIn(0f, 1f)
            }
        }

        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * waveOffset

        // === Main bar with vertical gradient ===
        val barGradient = Brush.verticalGradient(
            0f to animConfig.primaryColor.copy(alpha = 0.2f),
            0.3f to animConfig.primaryColor.copy(alpha = animConfig.brightness * 0.7f),
            0.7f to animConfig.primaryColor.copy(alpha = animConfig.brightness * 0.95f),
            1f to animConfig.primaryColor.copy(alpha = animConfig.brightness),
            startY = topY + maxBarHeight - barHeight,
            endY = topY + maxBarHeight
        )

        // Draw main bar with gradient
        drawRoundRect(
            brush = barGradient,
            topLeft = Offset(xPos, topY + maxBarHeight - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // === Glow effect on active bars ===
        if (waveOffset > 0.5f) {
            // Top glow
            drawCircle(
                brush = Brush.radialGradient(
                    0f to animConfig.primaryColor.copy(alpha = (waveOffset - 0.5f) * animConfig.brightness * 0.8f),
                    1f to Color.Transparent,
                    center = Offset(xPos + barWidth / 2f, topY + maxBarHeight - barHeight),
                    radius = barWidth * 1.5f
                ),
                radius = barWidth * 1.5f,
                center = Offset(xPos + barWidth / 2f, topY + maxBarHeight - barHeight)
            )
        }

        // === Bar outline for definition ===
        drawRoundRect(
            color = animConfig.primaryColor.copy(alpha = 0.4f * animConfig.brightness),
            topLeft = Offset(xPos, topY + maxBarHeight - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
            style = Stroke(width = 0.5f)
        )
    }

    // === Base line under mouth ===
    drawLine(
        color = animConfig.primaryColor.copy(alpha = 0.2f * animConfig.brightness),
        start = Offset(centerX - width / 2f - 10f, topY + maxBarHeight + 5f),
        end = Offset(centerX + width / 2f + 10f, topY + maxBarHeight + 5f),
        strokeWidth = 1f
    )
}

/**
 * Animation configuration for each state
 * This drives all visual behaviors parametrically
 */
@Composable
private fun rememberAnimationConfig(state: RoboState): AnimationConfig {
    return remember(state) {
        when (state) {
            RoboState.Idle -> AnimationConfig(
                primaryColor = Color.Cyan,
                secondaryColor = Color.Blue,
                coreColor = Color(0xFF66CCFF),
                brightness = 0.7f,
                pulseTarget = 1.08f,
                pulseDuration = 2000,
                pulseEasing = FastOutSlowInEasing,
                mouthIntensity = 0.3f,
                mouthSpeed = 2000
            )

            RoboState.Curious -> AnimationConfig(
                primaryColor = Color(0xFF00FFFF), // Bright cyan
                secondaryColor = Color(0xFF0099FF),
                coreColor = Color.White,
                brightness = 0.85f,
                pulseTarget = 1.12f,
                pulseDuration = 1500,
                pulseEasing = LinearEasing,
                mouthIntensity = 0.5f,
                mouthSpeed = 1500
            )

            RoboState.Happy -> AnimationConfig(
                primaryColor = Color(0xFF00FF88), // Green-cyan
                secondaryColor = Color(0xFF00CC66),
                coreColor = Color.White,
                brightness = 1f,
                pulseTarget = 1.15f,
                pulseDuration = 1000,
                pulseEasing = FastOutSlowInEasing,
                mouthIntensity = 0.8f,
                mouthSpeed = 800
            )

            RoboState.Angry -> AnimationConfig(
                primaryColor = Color(0xFFFF3333), // Red
                secondaryColor = Color(0xFFCC0000),
                coreColor = Color(0xFFFFAA00),
                brightness = 0.95f,
                pulseTarget = 1.2f,
                pulseDuration = 400,
                pulseEasing = LinearEasing,
                mouthIntensity = 0.9f,
                mouthSpeed = 300
            )

            RoboState.Sleep -> AnimationConfig(
                primaryColor = Color(0xFF334466), // Dim blue
                secondaryColor = Color(0xFF223344),
                coreColor = Color(0xFF445566),
                brightness = 0.2f,
                pulseTarget = 1.02f,
                pulseDuration = 4000,
                pulseEasing = FastOutSlowInEasing,
                mouthIntensity = 0.05f,
                mouthSpeed = 5000
            )
        }
    }
}

/**
 * Data class holding all animation parameters for a state
 * This enables state-driven rendering without hardcoded visuals
 */
private data class AnimationConfig(
    val primaryColor: Color,
    val secondaryColor: Color,
    val coreColor: Color,
    val brightness: Float,
    val pulseTarget: Float,
    val pulseDuration: Int,
    val pulseEasing: Easing,
    val mouthIntensity: Float,
    val mouthSpeed: Int
)

/**
 * Draw energy field background with pulsing effect
 */
private fun DrawScope.drawEnergyField(
    center: Offset,
    radius: Float,
    time: Float,
    animConfig: AnimationConfig
) {
    // Draw multiple concentric circles with varying alpha
    for (i in 1..3) {
        val ringRadius = radius * (0.6f + i * 0.15f)
        val alphaWave = (sin(time - i * 0.5f) * 0.5f + 0.5f) * 0.08f * animConfig.brightness

        drawCircle(
            brush = Brush.radialGradient(
                0.8f to Color.Transparent,
                0.95f to animConfig.primaryColor.copy(alpha = alphaWave),
                1f to Color.Transparent,
                center = center,
                radius = ringRadius
            ),
            radius = ringRadius,
            center = center
        )
    }
}

/**
 * Draw connection lines between facial components
 * Creates a circuit board aesthetic
 */
private fun DrawScope.drawConnectionLines(
    centerX: Float,
    eyeY: Float,
    eyeSpacing: Float,
    noseY: Float,
    mouthY: Float,
    animConfig: AnimationConfig
) {
    val lineColor = animConfig.primaryColor.copy(alpha = 0.2f * animConfig.brightness)
    val lineWidth = 1.5f

    // Connect eyes to nose
    drawLine(
        color = lineColor,
        start = Offset(centerX - eyeSpacing, eyeY + 20f),
        end = Offset(centerX - 15f, noseY),
        strokeWidth = lineWidth
    )

    drawLine(
        color = lineColor,
        start = Offset(centerX + eyeSpacing, eyeY + 20f),
        end = Offset(centerX + 15f, noseY),
        strokeWidth = lineWidth
    )

    // Connect nose to mouth
    drawLine(
        color = lineColor,
        start = Offset(centerX, noseY + 25f),
        end = Offset(centerX, mouthY - 10f),
        strokeWidth = lineWidth
    )

    // Add small connection nodes
    val nodePositions = listOf(
        Offset(centerX - 15f, noseY),
        Offset(centerX + 15f, noseY),
        Offset(centerX, mouthY - 10f)
    )

    nodePositions.forEach { pos ->
        drawCircle(
            color = animConfig.primaryColor.copy(alpha = 0.4f * animConfig.brightness),
            radius = 3f,
            center = pos
        )
    }
}

/**
 * Helper extension to convert degrees to radians
 */
private fun Float.toRadians(): Float = this * PI.toFloat() / 180f





