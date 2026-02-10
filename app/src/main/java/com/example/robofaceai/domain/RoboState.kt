package com.example.robofaceai.domain

/**
 * TASK 5: Sealed class representing all possible emotional/behavioral states.
 *
 * This is the OUTPUT of the FSM - the robo's current emotional state.
 * States are immutable objects that represent distinct behavioral modes.
 *
 * Design principles:
 * - Each state has a clear personality
 * - States guide rendering (but contain no UI code)
 * - Easy to add new states by extending the sealed class
 *
 * Emotional model:
 * - Sleep: Inactive, conserving energy
 * - Idle: Neutral, waiting for interaction
 * - Curious: Interested, alert, exploring
 * - Happy: Positive engagement, satisfaction
 * - Angry: Negative reaction, frustration
 */
sealed class RoboState {

    /**
     * Idle state - Robo is calm and waiting.
     *
     * Personality:
     * - Neutral emotional state
     * - Ready to react to input
     * - Low energy consumption
     *
     * Visual guidelines:
     * - Slow pulse (breathing effect)
     * - Gentle animations
     * - Neutral colors (blue/white)
     *
     * Entry conditions:
     * - Default starting state
     * - After face lost
     * - After calming down from Angry
     *
     * Exit conditions:
     * - Face detected → Curious
     * - Tilt/rotation → Curious
     * - Prolonged inactivity → Sleep
     */
    object Idle : RoboState()

    /**
     * Curious state - Robo is interested or alert.
     *
     * Personality:
     * - Active attention
     * - Exploratory behavior
     * - Increased responsiveness
     *
     * Visual guidelines:
     * - Faster pulse
     * - Eyes tracking/rotating
     * - Brighter glow
     *
     * Entry conditions:
     * - Face detected from Idle/Sleep
     * - Gentle shake
     * - Tilt/rotation from Idle
     *
     * Exit conditions:
     * - Face persists → Happy
     * - Face lost → Idle
     * - Strong shake → Angry
     * - Loud sound → Angry
     */
    object Curious : RoboState()

    /**
     * Happy state - Robo is pleased or excited.
     *
     * Personality:
     * - Positive engagement
     * - Satisfaction with interaction
     * - Welcoming demeanor
     *
     * Visual guidelines:
     * - Bright glow
     * - Smooth bouncing animations
     * - Green tint
     * - Mouth smile/bounce effect
     *
     * Entry conditions:
     * - Face detected while Curious (sustained interaction)
     * - AI predicts "happy" emotion
     *
     * Exit conditions:
     * - Face lost → Idle
     * - Loud sound → Angry
     */
    object Happy : RoboState()

    /**
     * Angry state - Robo is upset or alert.
     *
     * Personality:
     * - Negative reaction
     * - Defensive/frustrated
     * - High arousal
     *
     * Visual guidelines:
     * - Fast sharp pulse
     * - Red tint
     * - Aggressive eye movements
     * - Jagged animations
     *
     * Entry conditions:
     * - Strong shake (intensity > 0.5)
     * - Loud sound
     * - AI predicts "angry" emotion
     *
     * Exit conditions:
     * - Timeout (30s) → Idle (calms down)
     */
    object Angry : RoboState()

    /**
     * Sleep state - Robo is inactive.
     *
     * Personality:
     * - Minimal activity
     * - Conservation mode
     * - Requires explicit wake-up
     *
     * Visual guidelines:
     * - Dim glow
     * - Minimal/slow animations
     * - Eyes closed or faded
     * - Dark colors
     *
     * Entry conditions:
     * - Prolonged inactivity (10+ seconds)
     * - IdleTimeout from non-Angry states
     * - AI predicts "sleep"
     *
     * Exit conditions:
     * - Face detected → Curious (wake up)
     * - WakeUp event → Curious
     *
     * Special behavior:
     * - Ignores most events (shake, sound) while sleeping
     */
    object Sleep : RoboState()
}

