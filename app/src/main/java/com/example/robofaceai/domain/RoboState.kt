package com.example.robofaceai.domain

/**
 * Sealed class representing all possible states of the Robo character.
 * Used in the Finite State Machine (FSM) pattern.
 */
sealed class RoboState {
    /**
     * Idle state - Robo is calm and waiting
     * Visual: Slow pulse, gentle breathing animation
     */
    object Idle : RoboState()

    /**
     * Curious state - Robo is interested or alert
     * Visual: Faster pulse, eyes rotate slightly
     */
    object Curious : RoboState()

    /**
     * Happy state - Robo is pleased or excited
     * Visual: Bright glow, smooth mouth bounce, green tint
     */
    object Happy : RoboState()

    /**
     * Angry state - Robo is upset or alert
     * Visual: Fast sharp pulse, red tint, aggressive movements
     */
    object Angry : RoboState()

    /**
     * Sleep state - Robo is inactive or proximity detected
     * Visual: Dim glow, minimal animation, eyes fade
     */
    object Sleep : RoboState()
}

