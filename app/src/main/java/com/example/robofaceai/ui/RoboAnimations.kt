package com.example.robofaceai.ui

import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import com.example.robofaceai.domain.RoboState
import kotlin.math.abs
import kotlin.math.sin

/**
 * TASK 2: State-Driven Animation Logic (AIMER Society Internship Challenge)
 *
 * ARCHITECTURE PATTERN:
 * This file implements the "Animation Engine" for the Robo Face.
 * All visual behaviors are PURE FUNCTIONS that map:
 *   (RoboState, Time) → Visual Property
 *
 * BENEFITS OF THIS APPROACH:
 * 1. Testability: Pure functions with no side effects
 * 2. Centralization: All animation logic in one place
 * 3. Extensibility: Add new states without touching rendering code
 * 4. Reusability: Functions can be used across multiple components
 *
 * STATE-DRIVEN BEHAVIORS:
 * - Idle    → Slow pulse, gentle breathing, cyan
 * - Curious → Medium pulse with rotation, purple
 * - Happy   → Fast bounce, bright glow, green
 * - Angry   → Sharp flicker, aggressive motion, red
 * - Sleep   → Dimmed, minimal animation, gray
 *
 * NO HARDCODED VISUALS:
 * Drawing functions in RoboCanvas.kt call these functions to get
 * animation values. This ensures visual behavior is driven by state,
 * not hardcoded in rendering code.
 */
object RoboAnimations {

    // ========== EYE ANIMATIONS ==========

    /**
     * Get pulse scale factor based on state
     * @param state Current robot state
     * @param time Current animation time in milliseconds
     * @return Scale factor (0.8 to 1.2)
     */
    fun getEyePulseScale(state: RoboState, time: Long): Float {
        return when (state) {
            is RoboState.Idle -> {
                // Slow, gentle pulse
                val cycle = (time % 2000) / 2000f
                0.95f + 0.05f * sin(cycle * Math.PI * 2).toFloat()
            }
            is RoboState.Curious -> {
                // Medium speed pulse
                val cycle = (time % 1500) / 1500f
                0.92f + 0.08f * sin(cycle * Math.PI * 2).toFloat()
            }
            is RoboState.Happy -> {
                // Bouncy, larger pulse
                val cycle = (time % 1000) / 1000f
                0.9f + 0.1f * sin(cycle * Math.PI * 2).toFloat()
            }
            is RoboState.Angry -> {
                // Fast, sharp pulse
                val cycle = (time % 500) / 500f
                0.85f + 0.15f * abs(sin(cycle * Math.PI * 4)).toFloat()
            }
            is RoboState.Sleep -> {
                // Minimal pulse, fading
                0.7f
            }
        }
    }

    /**
     * Get eye glow intensity based on state
     */
    fun getEyeGlowIntensity(state: RoboState, time: Long): Float {
        return when (state) {
            is RoboState.Idle -> 0.6f
            is RoboState.Curious -> 0.75f
            is RoboState.Happy -> 0.95f
            is RoboState.Angry -> {
                // Flickering glow
                val cycle = (time % 300) / 300f
                0.7f + 0.3f * abs(sin(cycle * Math.PI * 6)).toFloat()
            }
            is RoboState.Sleep -> 0.2f
        }
    }

    /**
     * Get eye rotation angle for curious state
     */
    fun getEyeRotation(state: RoboState, time: Long): Float {
        return when (state) {
            is RoboState.Curious -> {
                val cycle = (time % 3000) / 3000f
                5f * sin(cycle * Math.PI * 2).toFloat()
            }
            else -> 0f
        }
    }

    // ========== MOUTH ANIMATIONS ==========

    /**
     * Get mouth bar height for a specific bar index
     * @param state Current robot state
     * @param barIndex Index of the bar (0 to barCount-1)
     * @param barCount Total number of bars
     * @param time Current animation time in milliseconds
     * @return Height multiplier (0 to 1)
     */
    fun getMouthBarHeight(
        state: RoboState,
        barIndex: Int,
        barCount: Int,
        time: Long
    ): Float {
        return when (state) {
            is RoboState.Idle -> {
                // Gentle wave pattern
                val phase = (barIndex.toFloat() / barCount) * Math.PI
                val cycle = (time % 2000) / 2000f
                0.3f + 0.2f * sin(cycle * Math.PI * 2 + phase).toFloat()
            }
            is RoboState.Curious -> {
                // Medium wave
                val phase = (barIndex.toFloat() / barCount) * Math.PI * 2
                val cycle = (time % 1500) / 1500f
                0.4f + 0.3f * sin(cycle * Math.PI * 2 + phase).toFloat()
            }
            is RoboState.Happy -> {
                // Smooth bounce, higher bars
                val phase = (barIndex.toFloat() / barCount) * Math.PI
                val cycle = (time % 1000) / 1000f
                0.6f + 0.3f * sin(cycle * Math.PI * 2 + phase).toFloat()
            }
            is RoboState.Angry -> {
                // Sharp, jagged movement
                val phase = (barIndex.toFloat() / barCount) * Math.PI * 3
                val cycle = (time % 400) / 400f
                0.5f + 0.4f * abs(sin(cycle * Math.PI * 8 + phase)).toFloat()
            }
            is RoboState.Sleep -> {
                // Minimal height
                0.1f
            }
        }
    }

    /**
     * Get mouth bar brightness
     */
    fun getMouthBarBrightness(state: RoboState, barHeight: Float): Float {
        val baseBrightness = when (state) {
            is RoboState.Sleep -> 0.3f
            is RoboState.Idle -> 0.6f
            is RoboState.Curious -> 0.75f
            is RoboState.Happy -> 0.9f
            is RoboState.Angry -> 0.8f
        }
        // Brightness correlates with height
        return baseBrightness * (0.5f + 0.5f * barHeight)
    }

    // ========== COLOR ANIMATIONS ==========

    /**
     * Get primary color based on state
     */
    fun getPrimaryColor(state: RoboState): Color {
        return when (state) {
            is RoboState.Idle -> Color(0xFF00D9FF) // Cyan
            is RoboState.Curious -> Color(0xFF6B5FFF) // Purple
            is RoboState.Happy -> Color(0xFF00FF88) // Green
            is RoboState.Angry -> Color(0xFFFF3355) // Red
            is RoboState.Sleep -> Color(0xFF444455) // Dark gray
        }
    }

    /**
     * Get secondary color (inner glow) based on state
     */
    fun getSecondaryColor(state: RoboState): Color {
        return when (state) {
            is RoboState.Idle -> Color(0xFF0099FF) // Blue
            is RoboState.Curious -> Color(0xFF4433FF) // Deep purple
            is RoboState.Happy -> Color(0xFF00DD66) // Bright green
            is RoboState.Angry -> Color(0xFFDD0033) // Dark red
            is RoboState.Sleep -> Color(0xFF222233) // Very dark
        }
    }

    /**
     * Get core/center color (brightest part)
     */
    fun getCoreColor(state: RoboState): Color {
        return when (state) {
            is RoboState.Sleep -> Color(0xFF666677)
            else -> Color.White
        }
    }

    // ========== NOSE ANIMATIONS ==========

    /**
     * Get nose glow intensity
     */
    fun getNoseGlowIntensity(state: RoboState, time: Long): Float {
        return when (state) {
            is RoboState.Idle -> 0.4f
            is RoboState.Curious -> {
                val cycle = (time % 1000) / 1000f
                0.5f + 0.2f * sin(cycle * Math.PI * 2).toFloat()
            }
            is RoboState.Happy -> 0.7f
            is RoboState.Angry -> 0.6f
            is RoboState.Sleep -> 0.1f
        }
    }
}

