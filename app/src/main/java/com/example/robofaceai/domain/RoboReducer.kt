package com.example.robofaceai.domain

/**
 * TASK 5: Pure Finite State Machine (FSM) Reducer
 *
 * Pure function implementing state transition logic: (CurrentState, Event) → NewState
 *
 * Key Principles:
 * - NO side effects (no logging, no I/O, no Android framework dependencies)
 * - Deterministic: same input always produces same output
 * - Testable: can be unit tested without Android runtime
 * - Easy to extend: add new state/event/transition
 *
 * Architecture:
 * - Input: Current state + Event
 * - Logic: Pattern matching on (state, event) pairs
 * - Output: New state
 */
object RoboReducer {

    /**
     * Core reducer function: applies an event to current state to produce new state.
     *
     * @param currentState The current state of the Robo
     * @param event The event that occurred
     * @return The new state after processing the event (may be same as current)
     */
    fun reduce(currentState: RoboState, event: RoboEvent): RoboState {
        return when (event) {

            // ========== TASK 5: BEHAVIOR ENGINE EVENTS (Highest Priority) ==========

            is RoboEvent.FaceDetected -> handleFaceDetected(currentState)
            is RoboEvent.FaceLost -> handleFaceLost(currentState)
            is RoboEvent.LoudSoundDetected -> handleLoudSound(currentState)
            is RoboEvent.ProlongedInactivity -> handleInactivity(currentState)

            // ========== SENSOR EVENTS ==========

            is RoboEvent.ShakeDetected -> handleShake(currentState, event.intensity)
            is RoboEvent.TiltDetected -> handleTilt(currentState)
            is RoboEvent.RotationDetected -> handleRotation(currentState)

            // ========== AI EVENTS ==========

            is RoboEvent.AIResult -> handleAIPrediction(currentState, event.prediction)

            // ========== SYSTEM EVENTS ==========

            is RoboEvent.IdleTimeout -> handleIdleTimeout(currentState)
            is RoboEvent.WakeUp -> handleWakeUp(currentState)
            is RoboEvent.ManualStateChange -> event.targetState
        }
    }

    // ========== TASK 5: BEHAVIOR TRANSITION HANDLERS ==========

    /**
     * Handle face detection event.
     *
     * Transition rules:
     * - Idle → Curious (initial interest)
     * - Sleep → Curious (wake up)
     * - Curious → Happy (sustained interaction)
     * - Happy/Angry → no change (already engaged)
     */
    private fun handleFaceDetected(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Idle -> RoboState.Curious
        is RoboState.Sleep -> RoboState.Curious
        is RoboState.Curious -> RoboState.Happy
        else -> currentState // Happy or Angry: maintain current emotional state
    }

    /**
     * Handle face lost event.
     *
     * Transition rules:
     * - Any active state → Idle (return to neutral when interaction ends)
     * - Sleep → no change (already inactive)
     */
    private fun handleFaceLost(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Sleep -> currentState
        else -> RoboState.Idle
    }

    /**
     * Handle loud sound detection.
     *
     * Transition rules:
     * - Any active state → Angry (startled/annoyed reaction)
     * - Sleep → no change (sleeping robo doesn't react)
     */
    private fun handleLoudSound(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Sleep -> currentState
        else -> RoboState.Angry
    }

    /**
     * Handle prolonged inactivity.
     *
     * Transition rules:
     * - Any state → Sleep (conservation mode after inactivity)
     */
    private fun handleInactivity(currentState: RoboState): RoboState = RoboState.Sleep

    // ========== SENSOR EVENT HANDLERS ==========

    /**
     * Handle shake detection with intensity-based branching.
     * FIX: Shake now wakes from sleep state
     */
    private fun handleShake(currentState: RoboState, intensity: Float): RoboState {
        return if (currentState is RoboState.Sleep) {
            // Wake up from sleep on shake
            RoboState.Curious
        } else {
            if (intensity > 0.5f) RoboState.Angry else RoboState.Curious
        }
    }

    /**
     * Handle tilt detection (subtle interaction).
     */
    private fun handleTilt(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Idle -> RoboState.Curious
        else -> currentState
    }

    /**
     * Handle rotation detection (subtle interaction).
     */
    private fun handleRotation(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Idle -> RoboState.Curious
        else -> currentState
    }

    // ========== AI EVENT HANDLERS ==========

    /**
     * Handle AI prediction events.
     */
    private fun handleAIPrediction(currentState: RoboState, prediction: String): RoboState {
        return if (currentState is RoboState.Sleep) {
            currentState // Don't change when sleeping
        } else {
            when (prediction.lowercase()) {
                "happy", "joy", "smile" -> RoboState.Happy
                "angry", "mad", "upset" -> RoboState.Angry
                "neutral", "calm" -> RoboState.Idle
                "curious", "alert", "interested" -> RoboState.Curious
                "sleep", "tired", "inactive" -> RoboState.Sleep
                else -> currentState
            }
        }
    }

    // ========== SYSTEM EVENT HANDLERS ==========

    /**
     * Handle idle timeout (energy conservation).
     */
    private fun handleIdleTimeout(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Sleep -> currentState
        is RoboState.Angry -> RoboState.Idle // Calm down
        else -> RoboState.Sleep // Enter sleep mode
    }

    /**
     * Handle wake up signal.
     */
    private fun handleWakeUp(currentState: RoboState): RoboState = when (currentState) {
        is RoboState.Sleep -> RoboState.Curious
        else -> currentState
    }

    // ========== UTILITY METHODS ==========

    /**
     * Get human-readable name for a state (useful for debugging/logging in outer layers).
     */
    fun getStateName(state: RoboState): String = when (state) {
        is RoboState.Idle -> "Idle"
        is RoboState.Curious -> "Curious"
        is RoboState.Happy -> "Happy"
        is RoboState.Angry -> "Angry"
        is RoboState.Sleep -> "Sleep"
    }

    /**
     * Check if a state is "active" (not sleeping).
     */
    fun isActive(state: RoboState): Boolean = state !is RoboState.Sleep

    /**
     * Check if a state is "engaged" (actively interacting).
     */
    fun isEngaged(state: RoboState): Boolean = when (state) {
        is RoboState.Curious, is RoboState.Happy, is RoboState.Angry -> true
        else -> false
    }
}



