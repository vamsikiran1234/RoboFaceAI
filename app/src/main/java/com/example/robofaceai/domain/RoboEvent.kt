package com.example.robofaceai.domain

/**
 * Sealed class representing all events that can trigger state transitions.
 * Events come from sensors, AI predictions, or user interactions.
 */
sealed class RoboEvent {

    // ========== SENSOR EVENTS (Task 3) ==========

    /**
     * Tilt detected from accelerometer
     * @param x Tilt angle in X direction (-1 to 1)
     * @param y Tilt angle in Y direction (-1 to 1)
     */
    data class TiltDetected(val x: Float, val y: Float) : RoboEvent()

    /**
     * Shake detected from accelerometer magnitude
     * @param intensity Shake intensity (0 to 1)
     */
    data class ShakeDetected(val intensity: Float) : RoboEvent()

    /**
     * Proximity sensor state changed
     * @param isNear True if object is near (< 5cm), false otherwise
     */
    data class ProximityChanged(val isNear: Boolean) : RoboEvent()

    /**
     * Rotation detected from gyroscope
     * @param yaw Rotation around vertical axis
     * @param pitch Rotation around horizontal axis (forward/back)
     * @param roll Rotation around horizontal axis (left/right)
     */
    data class RotationDetected(val yaw: Float, val pitch: Float, val roll: Float) : RoboEvent()

    // ========== AI EVENTS (Task 6) ==========

    /**
     * AI model inference result
     * @param prediction The predicted class/emotion (e.g., "happy", "angry")
     * @param confidence Confidence score (0 to 1)
     */
    data class AIResult(val prediction: String, val confidence: Float) : RoboEvent()

    // ========== SYSTEM EVENTS ==========

    /**
     * No activity detected for a period of time
     */
    object IdleTimeout : RoboEvent()

    /**
     * Wake up trigger (e.g., proximity sensor far, or user interaction)
     */
    object WakeUp : RoboEvent()

    /**
     * Manual state change for testing
     */
    data class ManualStateChange(val targetState: RoboState) : RoboEvent()
}

