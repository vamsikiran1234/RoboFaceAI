package com.example.robofaceai.domain

/**
 * Pure state reducer function implementing the Finite State Machine (FSM) logic.
 * This is a pure function: (CurrentState, Event) -> NewState
 *
 * No side effects, no UI logic, just pure state transitions.
 * This makes the behavior deterministic and easy to test.
 */
object RoboReducer {

    /**
     * Reduces the current state and event to produce a new state.
     *
     * @param currentState The current state of the Robo
     * @param event The event that occurred
     * @return The new state after processing the event
     */
    fun reduce(currentState: RoboState, event: RoboEvent): RoboState {
        // Log all events for debugging
        android.util.Log.d("RoboReducer", "📥 Event received: ${event.javaClass.simpleName} | Current state: ${getStateName(currentState)}")

        return when (event) {

            // ========== PROXIMITY EVENTS (Highest Priority) ==========
            is RoboEvent.ProximityChanged -> {
                if (event.isNear) {
                    // Hand close -> Sleep
                    android.util.Log.d("RoboReducer", "😴 Proximity NEAR detected → Transitioning to SLEEP")
                    RoboState.Sleep
                } else {
                    // Hand away -> Wake up to Curious
                    if (currentState is RoboState.Sleep) {
                        android.util.Log.d("RoboReducer", "👁️ Proximity FAR detected from SLEEP → Transitioning to CURIOUS (wake up)")
                        RoboState.Curious
                    } else {
                        android.util.Log.d("RoboReducer", "👁️ Proximity FAR detected (not sleeping) → Staying in ${getStateName(currentState)}")
                        currentState // Stay in current state if not sleeping
                    }
                }
            }

            // ========== SHAKE EVENTS (High Priority) ==========
            is RoboEvent.ShakeDetected -> {
                if (currentState !is RoboState.Sleep) {
                    // High intensity shake -> Angry
                    if (event.intensity > 0.5f) {
                        android.util.Log.d("RoboReducer", "😠 STRONG shake detected (intensity=${event.intensity}) → Transitioning to ANGRY")
                        RoboState.Angry
                    } else {
                        // Gentle shake -> Curious
                        android.util.Log.d("RoboReducer", "🤔 Mild shake detected (intensity=${event.intensity}) → Transitioning to CURIOUS")
                        RoboState.Curious
                    }
                } else {
                    android.util.Log.d("RoboReducer", "😴 Shake detected but robot is SLEEPING → Ignoring")
                    currentState // Don't wake from sleep on shake
                }
            }

            // ========== TILT EVENTS ==========
            is RoboEvent.TiltDetected -> {
                // Only transition to Curious from Idle (not from Happy or Angry)
                if (currentState is RoboState.Idle) {
                    RoboState.Curious
                } else {
                    currentState // Keep current state
                }
            }

            // ========== ROTATION EVENTS ==========
            is RoboEvent.RotationDetected -> {
                // Only transition to Curious from Idle (not from Happy or Angry)
                if (currentState is RoboState.Idle) {
                    RoboState.Curious
                } else {
                    currentState // Keep current state
                }
            }

            // ========== AI EVENTS ==========
            is RoboEvent.AIResult -> {
                if (currentState !is RoboState.Sleep) {
                    when (event.prediction.lowercase()) {
                        "happy", "joy", "smile" -> RoboState.Happy
                        "angry", "mad", "upset" -> RoboState.Angry
                        "neutral", "calm" -> RoboState.Idle
                        "curious", "alert", "interested" -> RoboState.Curious
                        "sleep", "tired", "inactive" -> RoboState.Sleep
                        else -> currentState // Unknown prediction, keep current state
                    }
                } else {
                    currentState // Don't change state if sleeping
                }
            }

            // ========== SYSTEM EVENTS ==========
            is RoboEvent.IdleTimeout -> {
                if (currentState !is RoboState.Sleep) {
                    // After timeout, return to Idle or Sleep
                    if (currentState is RoboState.Angry) {
                        RoboState.Idle // Calm down from angry
                    } else {
                        RoboState.Sleep // Go to sleep from other states
                    }
                } else {
                    currentState
                }
            }

            is RoboEvent.WakeUp -> {
                if (currentState is RoboState.Sleep) {
                    RoboState.Curious // Wake up curious
                } else {
                    currentState
                }
            }

            is RoboEvent.ManualStateChange -> {
                // Allow manual state changes for testing
                event.targetState
            }
        }
    }

    /**
     * Get a human-readable name for the current state
     */
    fun getStateName(state: RoboState): String {
        return when (state) {
            is RoboState.Idle -> "Idle"
            is RoboState.Curious -> "Curious"
            is RoboState.Happy -> "Happy"
            is RoboState.Angry -> "Angry"
            is RoboState.Sleep -> "Sleep"
        }
    }
}

