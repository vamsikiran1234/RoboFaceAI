package com.example.robofaceai.domain

/**
 * TASK 5: Sealed class representing all events that can trigger state transitions.
 *
 * Events are the INPUT layer of the FSM architecture.
 * They come from various sources but are abstracted as pure data classes/objects.
 *
 * Architecture principle: Events are LOGICAL abstractions, not implementation details.
 * - FaceDetected doesn't mean "camera detected a face" - it means "the system should behave as if a face was detected"
 * - This allows testing without cameras, and flexibility in how events are triggered
 */
sealed class RoboEvent {

    // ========== TASK 5: BEHAVIOR ENGINE EVENTS (Logical Events) ==========

    /**
     * Face detected (LOGICAL EVENT).
     *
     * Interpretation: "The robo should behave as if someone is looking at it"
     *
     * Possible sources:
     * - Camera + ML face detection (future)
     * - Manual trigger for testing
     * - Timer-based simulation
     * - Proximity sensor interpretation
     *
     * Transition effects:
     * - Idle → Curious
     * - Sleep → Curious (wake up)
     * - Curious → Happy (sustained)
     */
    object FaceDetected : RoboEvent()

    /**
     * Face lost (LOGICAL EVENT).
     *
     * Interpretation: "The robo should behave as if the person looked away"
     *
     * Possible sources:
     * - Camera detects no face (future)
     * - Timeout after FaceDetected
     * - Manual trigger
     *
     * Transition effects:
     * - Active states → Idle (return to neutral)
     */
    object FaceLost : RoboEvent()

    /**
     * Loud sound detected (LOGICAL EVENT).
     *
     * Interpretation: "The robo should react as if startled by noise"
     *
     * Possible sources:
     * - Microphone amplitude detection (future)
     * - Strong shake (substitution for demo)
     * - Manual trigger
     *
     * Transition effects:
     * - Active states → Angry (startled/annoyed)
     */
    object LoudSoundDetected : RoboEvent()

    /**
     * Prolonged inactivity detected (LOGICAL EVENT).
     *
     * Interpretation: "No interaction for a long time - conserve energy"
     *
     * Source:
     * - BehaviorEngine timer (10+ seconds of no activity)
     *
     * Transition effects:
     * - Any state → Sleep (power saving mode)
     */
    object ProlongedInactivity : RoboEvent()

    // ========== SENSOR EVENTS (Physical Inputs) ==========

    /**
     * Tilt detected from accelerometer.
     * @param x Tilt angle in X direction (-1 to 1)
     * @param y Tilt angle in Y direction (-1 to 1)
     */
    data class TiltDetected(val x: Float, val y: Float) : RoboEvent()

    /**
     * Shake detected from accelerometer magnitude.
     * @param intensity Shake intensity (0 to 1)
     */
    data class ShakeDetected(val intensity: Float) : RoboEvent()

    /**
     * Rotation detected from gyroscope.
     * @param yaw Rotation around vertical axis
     * @param pitch Rotation around horizontal axis (forward/back)
     * @param roll Rotation around horizontal axis (left/right)
     */
    data class RotationDetected(val yaw: Float, val pitch: Float, val roll: Float) : RoboEvent()

    // ========== AI EVENTS (ML Predictions) ==========

    /**
     * AI model inference result.
     * @param prediction The predicted class/emotion (e.g., "happy", "angry")
     * @param confidence Confidence score (0 to 1)
     */
    data class AIResult(val prediction: String, val confidence: Float) : RoboEvent()

    // ========== SYSTEM EVENTS (Lifecycle & Testing) ==========

    /**
     * No activity detected for a period of time.
     *
     * Different from ProlongedInactivity:
     * - IdleTimeout: shorter duration, for specific state transitions
     * - ProlongedInactivity: longer duration, leads to sleep
     */
    object IdleTimeout : RoboEvent()

    /**
     * Wake up trigger (e.g., user interaction after sleep).
     */
    object WakeUp : RoboEvent()

    /**
     * Manual state change for testing/demo purposes.
     * @param targetState The state to transition to
     */
    data class ManualStateChange(val targetState: RoboState) : RoboEvent()
}



