package com.example.robofaceai.behavior

import com.example.robofaceai.domain.RoboEvent
import com.example.robofaceai.domain.RoboState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🔹 TASK 5: ROBO PERSONALITY & BEHAVIOR ENGINE
 *
 * A simple internal "mind" for the robo using a state machine that controls
 * emotions and reactions based on inputs over time.
 *
 * Instead of reacting randomly, the robo behaves consistently based on inputs.
 * This focuses on logic, architecture, and behavior modeling.
 *
 * Required States:
 * - Idle: Calm and waiting
 * - Curious: Interested or alert
 * - Happy: Pleased or excited
 * - Angry: Upset or alert
 * - Sleep: Inactive
 *
 * State Transitions (Examples):
 * - No face detected for 10 seconds → Idle → Sleep
 * - Face detected again → Curious → Happy
 * - Loud sound → Angry
 * - Prolonged inactivity → Sleep
 *
 * Technical Requirements:
 * - Finite State Machine with sealed classes ✓
 * - Clear separation between: Input, State logic, Rendering ✓
 * - Easy to extend with new states ✓
 */
class Task5BehaviorEngine(
    private val scope: CoroutineScope
) {
    // ========== STATE TRACKING ==========

    private var lastFaceDetectionTime = 0L
    private var lastActivityTime = 0L
    private var faceDetected = false

    // Timers
    private var noFaceTimer: Job? = null
    private var inactivityTimer: Job? = null

    // ========== CONFIGURATION ==========

    // Time without face before going from Curious/Happy → Idle (shortened for 10s total)
    private val noFaceToIdleDelay = 3000L // 3 seconds

    // Time without any activity before going Idle → Sleep (7s so total = 10s)
    private val inactivityToSleepDelay = 7000L // 7 seconds

    // ========== EVENT OUTPUT ==========

    private val _behaviorEvent = MutableStateFlow<RoboEvent?>(null)
    val behaviorEvent: StateFlow<RoboEvent?> = _behaviorEvent.asStateFlow()

    // ========== PUBLIC API ==========

    /**
     * Notify that a face has been detected
     */
    fun onFaceDetected() {
        val now = System.currentTimeMillis()
        lastFaceDetectionTime = now
        lastActivityTime = now

        if (!faceDetected) {
            faceDetected = true
            android.util.Log.d("BehaviorEngine", "👁️ Face detected!")
            _behaviorEvent.value = RoboEvent.FaceDetected

            // Cancel inactivity timer
            inactivityTimer?.cancel()
        }

        // Cancel "no face" timer since face is visible
        noFaceTimer?.cancel()
    }

    /**
     * Notify that the face was lost
     */
    fun onFaceLost() {
        if (faceDetected) {
            faceDetected = false
            android.util.Log.d("BehaviorEngine", "👋 Face lost")

            // Start timer: if no face for X seconds → Idle
            noFaceTimer?.cancel()
            noFaceTimer = scope.launch {
                delay(noFaceToIdleDelay)
                android.util.Log.d("BehaviorEngine", "⏱️ No face for ${noFaceToIdleDelay}ms → FaceLost event")
                _behaviorEvent.value = RoboEvent.FaceLost
                startInactivityTimer()
            }
        }
    }

    /**
     * Notify about sensor activity (shake, tilt, rotation)
     */
    fun onSensorActivity() {
        lastActivityTime = System.currentTimeMillis()

        // Reset inactivity timer
        inactivityTimer?.cancel()
        startInactivityTimer()
    }

    /**
     * Notify about loud sound detection
     */
    fun onLoudSound() {
        lastActivityTime = System.currentTimeMillis()
        android.util.Log.d("BehaviorEngine", "🔊 Loud sound detected!")
        _behaviorEvent.value = RoboEvent.LoudSoundDetected

        // Reset inactivity timer
        inactivityTimer?.cancel()
        startInactivityTimer()
    }

    /**
     * Notify the engine about current state (for context-aware decisions)
     */
    fun onStateChanged(newState: RoboState) {
        when (newState) {
            is RoboState.Sleep -> {
                // Cancel all timers when sleeping
                noFaceTimer?.cancel()
                inactivityTimer?.cancel()
            }
            is RoboState.Idle -> {
                // Start inactivity timer when entering Idle
                startInactivityTimer()
            }
            else -> {
                // For other states, keep monitoring inactivity
                if (inactivityTimer?.isActive != true) {
                    startInactivityTimer()
                }
            }
        }
    }

    // ========== PRIVATE METHODS ==========

    private fun startInactivityTimer() {
        inactivityTimer?.cancel()
        inactivityTimer = scope.launch {
            delay(inactivityToSleepDelay)

            // Check if still inactive
            val now = System.currentTimeMillis()
            if (now - lastActivityTime >= inactivityToSleepDelay) {
                android.util.Log.d("BehaviorEngine", "😴 Prolonged inactivity detected → Sleep")
                _behaviorEvent.value = RoboEvent.ProlongedInactivity
            }
        }
    }

    /**
     * Start monitoring behavior
     */
    fun start() {
        android.util.Log.d("BehaviorEngine", "🧠 Task 5 Behavior Engine started")
        lastActivityTime = System.currentTimeMillis()
        startInactivityTimer()
    }

    /**
     * Stop monitoring behavior
     */
    fun stop() {
        android.util.Log.d("BehaviorEngine", "🛑 Task 5 Behavior Engine stopped")
        noFaceTimer?.cancel()
        inactivityTimer?.cancel()
    }

    /**
     * Get diagnostics string
     */
    fun getDiagnostics(): String {
        val now = System.currentTimeMillis()
        return buildString {
            appendLine("🧠 Behavior Engine Status:")
            appendLine("├─ Face Detected: ${if (faceDetected) "YES ✓" else "NO"}")
            appendLine("├─ Last Face: ${if (lastFaceDetectionTime > 0) "${(now - lastFaceDetectionTime) / 1000}s ago" else "Never"}")
            appendLine("├─ Last Activity: ${(now - lastActivityTime) / 1000}s ago")
            appendLine("├─ No-Face Timer: ${if (noFaceTimer?.isActive == true) "ACTIVE" else "Inactive"}")
            appendLine("└─ Inactivity Timer: ${if (inactivityTimer?.isActive == true) "ACTIVE" else "Inactive"}")
        }
    }
}

