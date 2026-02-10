package com.example.robofaceai.domain

import org.junit.Test
import org.junit.Assert.*

/**
 * TASK 5: Unit tests for the pure FSM reducer.
 *
 * These tests demonstrate:
 * - Pure function behavior (no Android dependencies)
 * - Deterministic state transitions
 * - All behavior rules from Task 5 requirements
 *
 * Can run without Android runtime - just JUnit.
 */
class RoboReducerTest {

    // ========== TASK 5: FACE DETECTION BEHAVIOR ==========

    @Test
    fun `FaceDetected from Idle transitions to Curious`() {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        assertEquals(RoboState.Curious, result)
    }

    @Test
    fun `FaceDetected from Sleep wakes to Curious`() {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.FaceDetected)
        assertEquals(RoboState.Curious, result)
    }

    @Test
    fun `FaceDetected from Curious transitions to Happy`() {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.FaceDetected)
        assertEquals(RoboState.Happy, result)
    }

    @Test
    fun `FaceDetected from Happy stays Happy`() {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.FaceDetected)
        assertEquals(RoboState.Happy, result)
    }

    @Test
    fun `FaceDetected from Angry stays Angry`() {
        val result = RoboReducer.reduce(RoboState.Angry, RoboEvent.FaceDetected)
        assertEquals(RoboState.Angry, result)
    }

    // ========== FACE LOST BEHAVIOR ==========

    @Test
    fun `FaceLost from Happy returns to Idle`() {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.FaceLost)
        assertEquals(RoboState.Idle, result)
    }

    @Test
    fun `FaceLost from Curious returns to Idle`() {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.FaceLost)
        assertEquals(RoboState.Idle, result)
    }

    @Test
    fun `FaceLost from Sleep stays Sleep`() {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.FaceLost)
        assertEquals(RoboState.Sleep, result)
    }

    // ========== LOUD SOUND BEHAVIOR ==========

    @Test
    fun `LoudSound from Idle triggers Angry`() {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.LoudSoundDetected)
        assertEquals(RoboState.Angry, result)
    }

    @Test
    fun `LoudSound from Curious triggers Angry`() {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.LoudSoundDetected)
        assertEquals(RoboState.Angry, result)
    }

    @Test
    fun `LoudSound from Happy triggers Angry`() {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.LoudSoundDetected)
        assertEquals(RoboState.Angry, result)
    }

    @Test
    fun `LoudSound from Sleep ignored`() {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.LoudSoundDetected)
        assertEquals(RoboState.Sleep, result)
    }

    // ========== INACTIVITY BEHAVIOR ==========

    @Test
    fun `ProlongedInactivity from Idle triggers Sleep`() {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ProlongedInactivity)
        assertEquals(RoboState.Sleep, result)
    }

    @Test
    fun `ProlongedInactivity from Happy triggers Sleep`() {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.ProlongedInactivity)
        assertEquals(RoboState.Sleep, result)
    }

    @Test
    fun `ProlongedInactivity from Angry triggers Sleep`() {
        val result = RoboReducer.reduce(RoboState.Angry, RoboEvent.ProlongedInactivity)
        assertEquals(RoboState.Sleep, result)
    }

    // ========== SHAKE BEHAVIOR ==========

    @Test
    fun `Gentle shake from Idle triggers Curious`() {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ShakeDetected(0.3f))
        assertEquals(RoboState.Curious, result)
    }

    @Test
    fun `Strong shake from Idle triggers Angry`() {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ShakeDetected(0.8f))
        assertEquals(RoboState.Angry, result)
    }

    @Test
    fun `Shake during Sleep ignored`() {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.ShakeDetected(0.8f))
        assertEquals(RoboState.Sleep, result)
    }

    // ========== COMPLEX SCENARIOS ==========

    @Test
    fun `Scenario - Face interaction flow Happy path`() {
        // Start idle
        var state: RoboState = RoboState.Idle

        // Face detected → Curious
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        assertEquals(RoboState.Curious, state)

        // Face still present → Happy
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        assertEquals(RoboState.Happy, state)

        // Face lost → Idle
        state = RoboReducer.reduce(state, RoboEvent.FaceLost)
        assertEquals(RoboState.Idle, state)

        // Inactivity → Sleep
        state = RoboReducer.reduce(state, RoboEvent.ProlongedInactivity)
        assertEquals(RoboState.Sleep, state)
    }

    @Test
    fun `Scenario - Interrupted interaction`() {
        var state: RoboState = RoboState.Idle

        // Face detected → Curious
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        assertEquals(RoboState.Curious, state)

        // Loud sound interrupts → Angry
        state = RoboReducer.reduce(state, RoboEvent.LoudSoundDetected)
        assertEquals(RoboState.Angry, state)

        // IdleTimeout → calm to Idle
        state = RoboReducer.reduce(state, RoboEvent.IdleTimeout)
        assertEquals(RoboState.Idle, state)
    }

    @Test
    fun `Scenario - Wake from sleep`() {
        var state: RoboState = RoboState.Sleep

        // Face detected wakes → Curious
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        assertEquals(RoboState.Curious, state)

        // Face persists → Happy
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        assertEquals(RoboState.Happy, state)
    }

    // ========== UTILITY METHODS ==========

    @Test
    fun `getStateName returns correct names`() {
        assertEquals("Idle", RoboReducer.getStateName(RoboState.Idle))
        assertEquals("Curious", RoboReducer.getStateName(RoboState.Curious))
        assertEquals("Happy", RoboReducer.getStateName(RoboState.Happy))
        assertEquals("Angry", RoboReducer.getStateName(RoboState.Angry))
        assertEquals("Sleep", RoboReducer.getStateName(RoboState.Sleep))
    }

    @Test
    fun `isActive identifies active states`() {
        assertTrue(RoboReducer.isActive(RoboState.Idle))
        assertTrue(RoboReducer.isActive(RoboState.Curious))
        assertTrue(RoboReducer.isActive(RoboState.Happy))
        assertTrue(RoboReducer.isActive(RoboState.Angry))
        assertFalse(RoboReducer.isActive(RoboState.Sleep))
    }

    @Test
    fun `isEngaged identifies engaged states`() {
        assertFalse(RoboReducer.isEngaged(RoboState.Idle))
        assertTrue(RoboReducer.isEngaged(RoboState.Curious))
        assertTrue(RoboReducer.isEngaged(RoboState.Happy))
        assertTrue(RoboReducer.isEngaged(RoboState.Angry))
        assertFalse(RoboReducer.isEngaged(RoboState.Sleep))
    }

    // ========== DETERMINISM TESTS ==========

    @Test
    fun `FSM is deterministic - same input produces same output`() {
        // Call same transition multiple times
        repeat(10) {
            val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
            assertEquals(RoboState.Curious, result)
        }
    }

    @Test
    fun `FSM is stateless - reducer doesn't maintain state`() {
        // Multiple transitions don't affect each other
        val result1 = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        val result2 = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        assertEquals(result1, result2)
        assertEquals(RoboState.Curious, result1)
    }
}

