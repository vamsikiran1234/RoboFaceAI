package com.example.robofaceai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.robofaceai.domain.RoboEvent
import com.example.robofaceai.domain.RoboReducer
import com.example.robofaceai.domain.RoboState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the Robo's state using MVVM + FSM architecture.
 *
 * This is the bridge between:
 * - Domain logic (RoboReducer)
 * - UI (Compose screens)
 * - External inputs (Sensors, AI)
 *
 * All state changes go through handleEvent() which uses the pure reducer function.
 */
class RoboViewModel : ViewModel() {

    // ========== STATE ==========

    /**
     * Current state of the Robo (private mutable version)
     */
    private val _state = MutableStateFlow<RoboState>(RoboState.Idle)

    /**
     * Public read-only state flow for UI observation
     */
    val state: StateFlow<RoboState> = _state.asStateFlow()

    /**
     * Current state name for debugging/display
     */
    val stateName: StateFlow<String> = MutableStateFlow(RoboReducer.getStateName(RoboState.Idle))

    // ========== STATE MANAGEMENT ==========

    /**
     * Handle an event and update the state accordingly.
     * Uses the pure reducer function from RoboReducer.
     *
     * @param event The event to process
     */
    fun handleEvent(event: RoboEvent) {
        val newState = RoboReducer.reduce(_state.value, event)

        // Only update if state actually changed
        if (newState != _state.value) {
            _state.value = newState
            (stateName as MutableStateFlow).value = RoboReducer.getStateName(newState)

            // Start timeout timer when entering certain states
            when (newState) {
                is RoboState.Angry -> startAngerTimeout()
                is RoboState.Curious -> startIdleTimeout(15000) // 15 seconds
                is RoboState.Happy -> startIdleTimeout(10000) // 10 seconds
                else -> {}
            }
        }
    }

    /**
     * Get the current state value
     */
    fun getCurrentState(): RoboState = _state.value

    // ========== TIMEOUT MANAGEMENT ==========

    /**
     * After being angry for a while, calm down to idle
     */
    private fun startAngerTimeout() {
        viewModelScope.launch {
            delay(5000) // 5 seconds of anger
            if (_state.value is RoboState.Angry) {
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
     */
    fun setStateManually(targetState: RoboState) {
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

