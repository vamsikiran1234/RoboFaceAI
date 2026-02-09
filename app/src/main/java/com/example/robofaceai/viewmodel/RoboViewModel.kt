package com.example.robofaceai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.robofaceai.ai.AIManager
import com.example.robofaceai.domain.RoboEvent
import com.example.robofaceai.domain.RoboReducer
import com.example.robofaceai.domain.RoboState
import com.example.robofaceai.sensors.SensorController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * TASK 2, 3, 6: Complete MVVM + FSM Architecture with Sensor Fusion & AI
 *
 * This ViewModel is the orchestration layer that integrates:
 * - Task 2: State machine for visual behavior
 * - Task 3: Sensor fusion (tilt, shake, proximity)
 * - Task 6: AI predictions
 *
 * Architecture:
 * - MVVM pattern (reactive StateFlows)
 * - FSM pattern (pure state transitions via RoboReducer)
 * - Sensor integration (SensorController)
 * - AI integration (AIManager)
 *
 * All state changes flow through handleEvent() → RoboReducer → New State
 */
class RoboViewModel(application: Application) : AndroidViewModel(application) {

    // ========== CORE STATE ==========

    private val _state = MutableStateFlow<RoboState>(RoboState.Idle)
    val state: StateFlow<RoboState> = _state.asStateFlow()

    private val _stateName = MutableStateFlow(RoboReducer.getStateName(RoboState.Idle))
    val stateName: StateFlow<String> = _stateName.asStateFlow()

    // ========== TASK 3: SENSOR INTEGRATION ==========

    private val sensorController = SensorController(application.applicationContext)

    // Expose sensor values for UI (Task 3: Real-time interaction)
    val tiltX: StateFlow<Float> = sensorController.tiltX
    val tiltY: StateFlow<Float> = sensorController.tiltY
    val headRotation: StateFlow<Float> = sensorController.headRotation

    // ========== TASK 6: AI INTEGRATION ==========

    private val aiManager = AIManager(application.applicationContext)

    private val _aiStats = MutableStateFlow(AIManager.InferenceStats())
    val aiStats: StateFlow<AIManager.InferenceStats> = _aiStats.asStateFlow()

    // AI is controllable - disabled when user manually sets state
    private val _isAIEnabled = MutableStateFlow(true)
    val isAIEnabled: StateFlow<Boolean> = _isAIEnabled.asStateFlow()

    // ========== LIFECYCLE & INITIALIZATION ==========

    private var lastManualChangeTime = 0L
    private var userHasManuallySetState = false // Track if user has taken manual control
    private var angerTimeoutJob: kotlinx.coroutines.Job? = null

    init {
        android.util.Log.d("RoboViewModel", "🎯 Initializing RoboViewModel with full sensor fusion...")

        // Initialize and start AI Manager
        aiManager.initialize()
        aiManager.start()

        // Start sensors
        sensorController.start()

        // Connect sensor data callback to AI
        sensorController.onSensorDataCallback = { x, y, z ->
            aiManager.feedSensorData(x, y, z)
        }

        // Listen to sensor events and feed to state machine
        viewModelScope.launch {
            sensorController.roboEvent.collect { event ->
                event?.let { handleEvent(it) }
            }
        }

        // Listen to AI predictions
        viewModelScope.launch {
            aiManager.predictions.collect { prediction ->
                _aiStats.value = aiManager.getStats()

                // Feed AI predictions as events
                if (prediction.isNotEmpty() && prediction != "unknown") {
                    handleEvent(RoboEvent.AIResult(
                        prediction = prediction,
                        confidence = aiManager.getStats().confidence
                    ))
                }
            }
        }

        // Log sensor availability
        val (hasAccel, hasGyro, hasProx) = sensorController.areSensorsAvailable()
        android.util.Log.d("RoboViewModel", """
            📱 Sensor Availability:
            ├─ Accelerometer: ${if (hasAccel) "✓" else "✗"}
            ├─ Gyroscope: ${if (hasGyro) "✓" else "✗"}
            └─ Proximity: ${if (hasProx) "✓" else "✗"}
        """.trimIndent())

        android.util.Log.d("RoboViewModel", "✓ RoboViewModel initialized successfully")
    }

    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("RoboViewModel", "🛑 Cleaning up RoboViewModel...")
        sensorController.stop()
        aiManager.stop()
    }

    // ========== STATE MANAGEMENT ==========

    /**
     * Handle an event and update the state accordingly.
     * Uses the pure reducer function from RoboReducer.
     *
     * @param event The event to process
     */
    fun handleEvent(event: RoboEvent) {
        // CRITICAL FIX: Completely ignore AI when user has manually set state
        // AI only re-enables when user physically interacts with phone (shake, tilt, proximity)
        if (event is RoboEvent.AIResult && userHasManuallySetState) {
            android.util.Log.v("RoboViewModel", "🚫 Ignoring AI event - user has manual control")
            return
        }

        // Re-enable AI when user physically interacts with phone
        if (event is RoboEvent.ShakeDetected ||
            event is RoboEvent.TiltDetected ||
            event is RoboEvent.RotationDetected ||
            event is RoboEvent.ProximityChanged) {
            if (userHasManuallySetState) {
                userHasManuallySetState = false
                _isAIEnabled.value = true
                android.util.Log.i("RoboViewModel", "🎯 User interacted with phone - AI re-enabled")
            }
        }

        android.util.Log.d("RoboViewModel", "🔄 Processing event: ${event.javaClass.simpleName}")

        val newState = RoboReducer.reduce(_state.value, event)

        // Only update if state actually changed
        if (newState != _state.value) {
            val oldStateName = RoboReducer.getStateName(_state.value)
            val newStateName = RoboReducer.getStateName(newState)

            android.util.Log.d("RoboViewModel", "✅ STATE TRANSITION: $oldStateName → $newStateName")

            _state.value = newState
            _stateName.value = newStateName

            // Start timeout timer when entering certain states
            when (newState) {
                is RoboState.Angry -> startAngerTimeout()
                // REMOVED: Automatic sleep timeouts that were causing issues
                // is RoboState.Curious -> startIdleTimeout(15000)
                // is RoboState.Happy -> startIdleTimeout(10000)
                else -> {}
            }
        } else {
            android.util.Log.d("RoboViewModel", "⏸️ No state change (already in ${RoboReducer.getStateName(_state.value)})")
        }
    }

    /**
     * Get the current state value
     */
    fun getCurrentState(): RoboState = _state.value

    // ========== TIMEOUT MANAGEMENT ==========

    /**
     * After being angry for a while, calm down to idle
     * Increased to 30 seconds so users can actually see the angry state
     */
    private fun startAngerTimeout() {
        // Cancel any previous anger timeout
        angerTimeoutJob?.cancel()

        angerTimeoutJob = viewModelScope.launch {
            delay(30000) // 30 seconds of anger (was 5 seconds - too fast)
            if (_state.value is RoboState.Angry) {
                android.util.Log.d("RoboViewModel", "⏱️ Anger timeout reached → Returning to Idle")
                handleEvent(RoboEvent.IdleTimeout)
            }
        }
    }

    /**
     * After inactivity, transition to sleep or idle
     */
    private fun startIdleTimeout(delayMs: Long) {
        viewModelScope.launch {
            delay(delayMs)
            if (_state.value !is RoboState.Sleep && _state.value !is RoboState.Idle) {
                handleEvent(RoboEvent.IdleTimeout)
            }
        }
    }

    // ========== TESTING HELPERS ==========

    /**
     * Manually set state (for testing/demo purposes)
     * DISABLES AI until user physically interacts with phone
     */
    fun setStateManually(targetState: RoboState) {
        lastManualChangeTime = System.currentTimeMillis()
        userHasManuallySetState = true
        _isAIEnabled.value = false

        val stateName = RoboReducer.getStateName(targetState)
        android.util.Log.d("RoboViewModel",
            "🔘 Manual button pressed: $stateName | AI DISABLED until physical interaction")
        handleEvent(RoboEvent.ManualStateChange(targetState))
    }

    /**
     * Cycle through all states (for demo/testing)
     */
    fun cycleStates() {
        viewModelScope.launch {
            val states = listOf(
                RoboState.Idle,
                RoboState.Curious,
                RoboState.Happy,
                RoboState.Angry,
                RoboState.Sleep
            )

            states.forEach { state ->
                setStateManually(state)
                delay(2000) // 2 seconds per state
            }

            // Return to Idle
            setStateManually(RoboState.Idle)
        }
    }
}

