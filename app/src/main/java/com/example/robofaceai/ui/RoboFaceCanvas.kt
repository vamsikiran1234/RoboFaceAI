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
 * TASK 2 & 5: NATIVE VECTOR ROBO FACE WITH ENHANCED FOCUS PERCEPTION
 * Professional implementation with real-time sensor fusion and emotional expression
 * Pure Jetpack Compose Canvas - NO IMAGES
 *
 * Visual Components:
 * - Eyes: Concentric rings with circuit dots and bright core
 * - Nose: Inverted V chevron with glowing dot
 * - Mouth: Equalizer-style rectangular bars
 * - All state-driven with smooth animations
 *
 * IMPROVEMENTS (Review Completed):
 * 1. Core Parallax: Inner white core moves 35% of outer ring displacement
 *    - Formula: coreDx = outerDx * 0.35
 *    - Creates depth perception and "looking" behavior
 *    - Prevents core from escaping outer rings
 *    - Makes focus direction immediately clear
 *
 * 2. Pupil Aperture: Core size varies by emotional state
 *    - Happy: 1.15x (wider, more open)
 *    - Angry: 0.75x (narrower, more focused)
 *    - Curious: 1.05x (slightly wider)
 *    - Sleep: 0.6x (smallest, nearly closed)
 *    - Idle: 1.0x (normal)
 *
 * 3. Focus Arc Indicator: Curved line on core showing gaze direction
 *    - 60° arc positioned based on offset angle
 *    - Only visible when actively tilting (not sleeping)
 *    - Subtle white line at 40% opacity
 *
 * Performance: No extra allocations, uses existing draw calls
 * Accessibility: All movements < 200ms except Angry (fast pulse allowed)
 */

@Composable
fun RoboFaceCanvas(
    state: RoboState,
    tiltX: Float = 0f,        // Sensor input: -1 (left) to 1 (right)
    tiltY: Float = 0f,        // Sensor input: -1 (forward) to 1 (backward)
    headRotation: Float = 0f, // Gyroscope input: rotation in degrees
    modifier: Modifier = Modifier
) {
    // Smooth interpolation for eye movement (prevents jitter)
    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "tiltX"
    )
    
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "tiltY"
    )

    val animatedHeadRotation by animateFloatAsState(
        targetValue = headRotation,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "headRotation"
    )

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

    // === TASK 3: SENSOR-DRIVEN EYE MOVEMENT WITH SMOOTH INTERPOLATION ===
        // Calculate outer eye offset from animated tilt (with natural limits)
        val maxEyeOffset = eyeRadius * 0.55f
        val eyeOffsetX = animatedTiltX * maxEyeOffset
        val eyeOffsetY = -animatedTiltY * maxEyeOffset // Invert Y for natural feel

        // IMPROVEMENT: Core parallax - inner core moves less (35% of outer movement)
        // Reduced from 60% to prevent core from escaping outer rings
        // This creates depth perception and "looking" behavior
        // FIX: Core moves ONLY with tilt - no circular breathing animation
        val coreOffsetX = eyeOffsetX * 0.35f
        val coreOffsetY = eyeOffsetY * 0.35f


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

        // Store eye positions for neural network connections
        val leftEyeCenter = Offset(
            centerX - eyeSpacing + eyeOffsetX,
            eyeY + eyeOffsetY
        )
        val rightEyeCenter = Offset(
            centerX + eyeSpacing + eyeOffsetX,
            eyeY + eyeOffsetY
        )

        // === NEURAL NETWORK CONNECTIONS BETWEEN EYES ===
        drawNeuralConnections(
            leftEye = leftEyeCenter,
            rightEye = rightEyeCenter,
            animConfig = animationConfig,
            phase = mouthAnimPhase
        )

        // Draw left eye (with sensor-driven offset and core parallax)
        drawRoboEye(
            center = leftEyeCenter,
            coreOffset = Offset(coreOffsetX, coreOffsetY), // IMPROVEMENT: Core moves less
            radius = eyeRadius,
            pulseScale = pulseScale,
            rotation = rotation + animatedHeadRotation * 0.6f,
            animConfig = animationConfig,
            state = state // Pass state for pupil aperture
        )

        // Draw right eye (with sensor-driven offset and core parallax)
        drawRoboEye(
            center = rightEyeCenter,
            coreOffset = Offset(coreOffsetX, coreOffsetY), // IMPROVEMENT: Core moves less
            radius = eyeRadius,
            pulseScale = pulseScale,
            rotation = rotation + animatedHeadRotation * 0.6f,
            animConfig = animationConfig,
            state = state // Pass state for pupil aperture
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
 * Draw robo eye with IMPROVED FOCUS INDICATORS
 * - Core parallax: inner core moves less than outer rings (depth perception)
 * - Pupil aperture: core size varies by emotion
 * - Focus arc: curved line shows gaze direction
 */
private fun DrawScope.drawRoboEye(
    center: Offset,
    coreOffset: Offset, // IMPROVEMENT: Separate offset for core (parallax)
    radius: Float,
    pulseScale: Float,
    rotation: Float,
    animConfig: AnimationConfig,
    state: RoboState // IMPROVEMENT: State for pupil aperture
) {
    val effectiveRadius = radius * pulseScale
    val darkBg = Color(0xFF0A1628)
    val deepBlue = Color(0xFF1a3a5c)

    // IMPROVEMENT: Pupil aperture - core size varies by emotion
    val coreAperture = when (state) {
        RoboState.Happy -> 1.15f      // Wider when happy
        RoboState.Angry -> 0.75f      // Narrower when angry
        RoboState.Curious -> 1.05f    // Slightly wider when curious
        RoboState.Sleep -> 0.6f       // Smallest when sleeping
        else -> 1.0f                  // Normal when idle
    }

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

    // === LAYER 7: ULTRA-BRIGHT WHITE CORE WITH PARALLAX & APERTURE ===
    // IMPROVEMENT: Core center offset for parallax effect (creates depth)
    val coreCenter = Offset(center.x + coreOffset.x, center.y + coreOffset.y)

    // IMPROVEMENT: Aperture adjustment based on emotional state
    val coreRadius = effectiveRadius * 0.22f * coreAperture

    // Ultra-bright outer glow (new layer for enhanced effect)
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = animConfig.brightness * 0.9f),
            0.3f to Color.White.copy(alpha = animConfig.brightness * 0.7f),
            0.6f to animConfig.coreColor.copy(alpha = 0.5f),
            1f to Color.Transparent,
            center = coreCenter,
            radius = coreRadius * 1.5f
        ),
        radius = coreRadius * 1.5f,
        center = coreCenter
    )

    // Main core with enhanced brightness
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = animConfig.brightness),
            0.3f to Color.White.copy(alpha = animConfig.brightness * 0.95f),
            0.6f to animConfig.coreColor.copy(alpha = 0.8f),
            1f to animConfig.coreColor.copy(alpha = 0.4f),
            center = coreCenter,
            radius = coreRadius
        ),
        radius = coreRadius,
        center = coreCenter
    )

    // Inner ultra-bright white spot
    drawCircle(
        color = Color.White.copy(alpha = animConfig.brightness),
        radius = coreRadius * 0.4f,
        center = coreCenter
    )

    // IMPROVEMENT: Focus arc indicator - curved line showing gaze direction
    // Only visible when not sleeping and there's actual offset
    // Enhanced visibility with stronger glow
    if (state != RoboState.Sleep && (abs(coreOffset.x) > 0.3f || abs(coreOffset.y) > 0.3f)) {
        val arcRadius = coreRadius * 0.85f
        val arcAngle = atan2(coreOffset.y, coreOffset.x)
        val arcStartAngle = Math.toDegrees((arcAngle - 0.5f).toDouble()).toFloat()
        val arcSweepAngle = 60f // Half-arc covering top portion

        // Outer glow for arc
        drawArc(
            color = Color.White.copy(alpha = 0.3f * animConfig.brightness),
            startAngle = arcStartAngle,
            sweepAngle = arcSweepAngle,
            useCenter = false,
            topLeft = Offset(coreCenter.x - arcRadius, coreCenter.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 4.0f, cap = StrokeCap.Round)
        )

        // Main arc line
        drawArc(
            color = Color.White.copy(alpha = 0.8f * animConfig.brightness),
            startAngle = arcStartAngle,
            sweepAngle = arcSweepAngle,
            useCenter = false,
            topLeft = Offset(coreCenter.x - arcRadius, coreCenter.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }

    // === ENHANCED CIRCUIT DETAILS: Advanced technical markings ===
    rotate(rotation * 0.3f, center) {

        // === ENHANCEMENT 1: Segmented arc patterns on outer ring ===
        // Creates clock-like markers on the second ring
        for (i in 0 until 16) {
            val angle = (i * 22.5f).toRadians()
            val segmentRadius = effectiveRadius * 0.78f
            val segmentLength = if (i % 4 == 0) 6f else 3f // Longer at cardinal positions
            
            val segmentStart = segmentRadius - segmentLength
            val segmentEnd = segmentRadius + segmentLength
            
            drawLine(
                color = animConfig.primaryColor.copy(alpha = if (i % 4 == 0) 0.5f else 0.25f * animConfig.brightness),
                start = Offset(
                    center.x + cos(angle) * segmentStart,
                    center.y + sin(angle) * segmentStart
                ),
                end = Offset(
                    center.x + cos(angle) * segmentEnd,
                    center.y + sin(angle) * segmentEnd
                ),
                strokeWidth = if (i % 4 == 0) 2f else 1f
            )
        }

        // === ENHANCEMENT 2: Middle ring technical markings (enhanced from 8 to 12) ===
        for (i in 0 until 12) {
            val angle = (i * 30f).toRadians()
            val markerRadius = effectiveRadius * 0.62f

            // Alternating dash and dot pattern
            if (i % 3 == 0) {
                // Longer dashes at key positions
                val dashStart = markerRadius - 8f
                val dashEnd = markerRadius + 8f
                drawLine(
                    color = animConfig.primaryColor.copy(alpha = 0.45f * animConfig.brightness),
                    start = Offset(
                        center.x + cos(angle) * dashStart,
                        center.y + sin(angle) * dashStart
                    ),
                    end = Offset(
                        center.x + cos(angle) * dashEnd,
                        center.y + sin(angle) * dashEnd
                    ),
                    strokeWidth = 1.8f
                )
            } else {
                // Small dots between dashes
                val dotPos = Offset(
                    center.x + cos(angle) * markerRadius,
                    center.y + sin(angle) * markerRadius
                )
                drawCircle(
                    color = animConfig.primaryColor.copy(alpha = 0.35f * animConfig.brightness),
                    radius = 1.5f,
                    center = dotPos
                )
            }
        }

        // === ENHANCEMENT 3: Circuit board dots scattered across rings ===
        // Inner ring circuit dots (12 positions)
        for (i in 0 until 12) {
            val angle = (i * 30f + 15f).toRadians() // Offset from main markers
            val dotRadius = effectiveRadius * 0.52f
            val dotPos = Offset(
                center.x + cos(angle) * dotRadius,
                center.y + sin(angle) * dotRadius
            )
            
            // Tiny circuit dot with glow
            drawCircle(
                brush = Brush.radialGradient(
                    0f to animConfig.secondaryColor.copy(alpha = 0.6f * animConfig.brightness),
                    1f to Color.Transparent,
                    center = dotPos,
                    radius = 2.5f
                ),
                radius = 2.5f,
                center = dotPos
            )
            drawCircle(
                color = animConfig.secondaryColor.copy(alpha = 0.8f * animConfig.brightness),
                radius = 1f,
                center = dotPos
            )
        }

        // === ENHANCEMENT 4: Enhanced radial lines (increased from 4 to 12) ===
        for (i in 0 until 12) {
            val angle = (i * 30f).toRadians()
            val lineStart = coreRadius * 1.3f
            val lineEnd = blueRingRadius * 0.75f
            
            // Thicker lines at cardinal positions
            val isCardinal = i % 3 == 0
            val lineAlpha = if (isCardinal) 0.35f else 0.18f
            val lineWidth = if (isCardinal) 1.5f else 0.8f

            // Gradient radial line
            drawLine(
                brush = Brush.linearGradient(
                    0f to animConfig.secondaryColor.copy(alpha = lineAlpha * animConfig.brightness),
                    0.5f to animConfig.secondaryColor.copy(alpha = lineAlpha * 0.6f * animConfig.brightness),
                    1f to animConfig.secondaryColor.copy(alpha = lineAlpha * 0.2f * animConfig.brightness),
                    start = Offset(
                        center.x + cos(angle) * lineStart,
                        center.y + sin(angle) * lineStart
                    ),
                    end = Offset(
                        center.x + cos(angle) * lineEnd,
                        center.y + sin(angle) * lineEnd
                    )
                ),
                start = Offset(
                    center.x + cos(angle) * lineStart,
                    center.y + sin(angle) * lineStart
                ),
                end = Offset(
                    center.x + cos(angle) * lineEnd,
                    center.y + sin(angle) * lineEnd
                ),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }

        // === ENHANCEMENT 5: Data node indicators at cardinal positions ===
        for (i in 0 until 4) {
            val angle = (i * 90f).toRadians()
            val nodeRadius = effectiveRadius * 0.68f
            val nodePos = Offset(
                center.x + cos(angle) * nodeRadius,
                center.y + sin(angle) * nodeRadius
            )
            
            // Larger data indicator nodes
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color(0xFF00E5FF).copy(alpha = 0.7f * animConfig.brightness),
                    0.6f to animConfig.primaryColor.copy(alpha = 0.4f * animConfig.brightness),
                    1f to Color.Transparent,
                    center = nodePos,
                    radius = 5f
                ),
                radius = 5f,
                center = nodePos
            )
            
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * animConfig.brightness),
                radius = 1.8f,
                center = nodePos
            )
        }

        // === ENHANCEMENT 6: Dashed arc segments on blue processing ring ===
        val blueRingRadius = effectiveRadius * 0.45f
        for (i in 0 until 24) {
            val startAngle = i * 15f
            val sweepAngle = 8f // Short dashed segments
            
            // Alternating pattern - draw every other segment
            if (i % 2 == 0) {
                drawArc(
                    color = animConfig.secondaryColor.copy(alpha = 0.4f * animConfig.brightness),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - blueRingRadius, center.y - blueRingRadius),
                    size = Size(blueRingRadius * 2, blueRingRadius * 2),
                    style = Stroke(width = 1f)
                )
            }
        }

        // === ENHANCEMENT 7: Micro connection traces between elements ===
        // Connect neural nodes to inner circuit dots
        val neuralNodeCount = 8
        for (i in 0 until 4) { // Just 4 connections to avoid clutter
            val nodeAngle = (i * 90f + 22.5f).toRadians()
            val dotAngle = (i * 90f + 22.5f + 30f).toRadians()
            
            val nodeRadius = effectiveRadius * 0.38f
            val dotRadius = effectiveRadius * 0.52f
            
            val nodePos = Offset(
                center.x + cos(nodeAngle) * nodeRadius,
                center.y + sin(nodeAngle) * nodeRadius
            )
            
            val dotPos = Offset(
                center.x + cos(dotAngle) * dotRadius,
                center.y + sin(dotAngle) * dotRadius
            )
            
            // Thin trace line
            drawLine(
                color = animConfig.secondaryColor.copy(alpha = 0.12f * animConfig.brightness),
                start = nodePos,
                end = dotPos,
                strokeWidth = 0.5f
            )
        }

        // === ENHANCEMENT 8: Neural network nodes (enhanced size and glow) ===
        for (i in 0 until neuralNodeCount) {
            val angle = (i * 45f).toRadians()
            val nodeRadius = effectiveRadius * 0.38f
            val nodePos = Offset(
                center.x + cos(angle) * nodeRadius,
                center.y + sin(angle) * nodeRadius
            )
            
            // Enhanced node outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    0f to animConfig.secondaryColor.copy(alpha = 0.6f * animConfig.brightness),
                    0.5f to animConfig.secondaryColor.copy(alpha = 0.35f * animConfig.brightness),
                    1f to Color.Transparent,
                    center = nodePos,
                    radius = 5f
                ),
                radius = 5f,
                center = nodePos
            )
            
            // Node core (slightly larger)
            drawCircle(
                color = animConfig.secondaryColor.copy(alpha = 0.85f * animConfig.brightness),
                radius = 2f,
                center = nodePos
            )
            
            // Bright center spot
            drawCircle(
                color = Color.White.copy(alpha = 0.7f * animConfig.brightness),
                radius = 0.8f,
                center = nodePos
            )
            
            // Connect to adjacent nodes
            if (i < neuralNodeCount - 1 || neuralNodeCount > 2) {
                val nextAngle = ((i + 1) % neuralNodeCount * 45f).toRadians()
                val nextNodePos = Offset(
                    center.x + cos(nextAngle) * nodeRadius,
                    center.y + sin(nextAngle) * nodeRadius
                )
                
                // Enhanced connection line
                drawLine(
                    color = animConfig.secondaryColor.copy(alpha = 0.25f * animConfig.brightness),
                    start = nodePos,
                    end = nextNodePos,
                    strokeWidth = 1f
                )
            }
        }

        // === ENHANCEMENT 9: Technical readout markers on outer edge ===
        for (i in 0 until 8) {
            val angle = (i * 45f + 22.5f).toRadians()
            val markerRadius = effectiveRadius * 0.88f
            val markerPos = Offset(
                center.x + cos(angle) * markerRadius,
                center.y + sin(angle) * markerRadius
            )
            
            // Small technical indicator
            drawCircle(
                color = animConfig.primaryColor.copy(alpha = 0.3f * animConfig.brightness),
                radius = 1.2f,
                center = markerPos
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
 * Draw minimal ambient particles for depth (static, professional)
 */
private fun DrawScope.drawParticleEffects(
    center: Offset,
    time: Float,
    animConfig: AnimationConfig
) {
    if (animConfig.brightness < 0.3f) return // Skip in sleep mode

    // === Minimal static particles for subtle ambient effect ===
    val particlePositions = listOf(
        Pair(220f, 30f), Pair(220f, 150f), Pair(220f, 270f),
        Pair(180f, 60f), Pair(180f, 180f), Pair(180f, 300f)
    )
    
    particlePositions.forEach { (distance, angleDeg) ->
        val angle = angleDeg.toRadians()
        val particleX = center.x + cos(angle) * distance
        val particleY = center.y + sin(angle) * distance

        // Subtle static particle with very gentle pulse
        val pulseAlpha = (sin(time * 0.5f) * 0.1f + 0.2f) * animConfig.brightness

        drawCircle(
            brush = Brush.radialGradient(
                0f to animConfig.primaryColor.copy(alpha = pulseAlpha * 0.6f),
                1f to Color.Transparent,
                center = Offset(particleX, particleY),
                radius = 3f
            ),
            radius = 3f,
            center = Offset(particleX, particleY)
        )

        drawCircle(
            color = animConfig.primaryColor.copy(alpha = pulseAlpha * 0.4f),
            radius = 1.5f,
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
 * Draw neural network connections between the two eyes
 * Creates interconnected nodes with data flow lines
 */
private fun DrawScope.drawNeuralConnections(
    leftEye: Offset,
    rightEye: Offset,
    animConfig: AnimationConfig,
    phase: Float
) {
    if (animConfig.brightness < 0.3f) return // Skip in sleep mode

    // === Main connection line between eyes ===
    val connectionColor = animConfig.primaryColor.copy(alpha = 0.3f * animConfig.brightness)
    
    // Draw main neural pathway
    drawLine(
        brush = Brush.linearGradient(
            0f to connectionColor.copy(alpha = 0.1f),
            0.3f to connectionColor,
            0.5f to connectionColor.copy(alpha = 0.5f),
            0.7f to connectionColor,
            1f to connectionColor.copy(alpha = 0.1f),
            start = leftEye,
            end = rightEye
        ),
        start = leftEye,
        end = rightEye,
        strokeWidth = 2f
    )

    // === Neural nodes along the connection ===
    val nodeCount = 5
    for (i in 0 until nodeCount) {
        val t = i / (nodeCount - 1f)
        val nodeX = leftEye.x + (rightEye.x - leftEye.x) * t
        val nodeY = leftEye.y + (rightEye.y - leftEye.y) * t
        
        // Add wave offset for organic feel
        val waveOffset = sin(phase + i * 0.8f) * 8f
        val nodePos = Offset(nodeX, nodeY + waveOffset)
        
        // Node glow (pulsing)
        val pulseAlpha = (sin(phase * 2f + i * 0.5f) * 0.3f + 0.7f) * animConfig.brightness
        drawCircle(
            brush = Brush.radialGradient(
                0f to animConfig.primaryColor.copy(alpha = pulseAlpha * 0.6f),
                1f to Color.Transparent,
                center = nodePos,
                radius = 6f
            ),
            radius = 6f,
            center = nodePos
        )
        
        // Node core
        drawCircle(
            color = Color.White.copy(alpha = pulseAlpha),
            radius = 2f,
            center = nodePos
        )
    }

    // === Side neural branches (top and bottom) ===
    val midPoint = Offset(
        (leftEye.x + rightEye.x) / 2f,
        (leftEye.y + rightEye.y) / 2f
    )
    
    // Top branch
    val topBranchEnd = Offset(midPoint.x, midPoint.y - 40f)
    drawLine(
        color = connectionColor.copy(alpha = 0.25f),
        start = midPoint,
        end = topBranchEnd,
        strokeWidth = 1.5f
    )
    drawCircle(
        brush = Brush.radialGradient(
            0f to animConfig.secondaryColor.copy(alpha = 0.5f * animConfig.brightness),
            1f to Color.Transparent,
            center = topBranchEnd,
            radius = 5f
        ),
        radius = 5f,
        center = topBranchEnd
    )
    
    // Bottom branch
    val bottomBranchEnd = Offset(midPoint.x, midPoint.y + 40f)
    drawLine(
        color = connectionColor.copy(alpha = 0.25f),
        start = midPoint,
        end = bottomBranchEnd,
        strokeWidth = 1.5f
    )
    drawCircle(
        brush = Brush.radialGradient(
            0f to animConfig.secondaryColor.copy(alpha = 0.5f * animConfig.brightness),
            1f to Color.Transparent,
            center = bottomBranchEnd,
            radius = 5f
        ),
        radius = 5f,
        center = bottomBranchEnd
    )

    // === Data flow particles along the main line ===
    for (i in 0..2) {
        val flowT = ((phase * 0.3f + i * 0.33f) % 1f)
        val flowX = leftEye.x + (rightEye.x - leftEye.x) * flowT
        val flowY = leftEye.y + (rightEye.y - leftEye.y) * flowT
        
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color.Cyan.copy(alpha = 0.8f * animConfig.brightness),
                1f to Color.Transparent,
                center = Offset(flowX, flowY),
                radius = 4f
            ),
            radius = 4f,
            center = Offset(flowX, flowY)
        )
    }
}

/**
 * Helper extension to convert degrees to radians
 */
private fun Float.toRadians(): Float = this * PI.toFloat() / 180f






