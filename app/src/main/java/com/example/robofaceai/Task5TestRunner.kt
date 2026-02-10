package com.example.robofaceai

import com.example.robofaceai.domain.*

/**
 * Simple standalone test runner for Task 5 FSM
 *
 * Run this from Android Studio or as a JVM application to test the FSM
 */
fun main() {
    println("╔════════════════════════════════════════════════════════╗")
    println("║     TASK 5 FSM - MANUAL TEST RUNNER                   ║")
    println("╚════════════════════════════════════════════════════════╝")
    println()

    var testsPassed = 0
    var testsFailed = 0

    fun test(name: String, test: () -> Boolean) {
        print("Testing: $name ... ")
        if (test()) {
            println("✅ PASS")
            testsPassed++
        } else {
            println("❌ FAIL")
            testsFailed++
        }
    }

    // Test 1: Face Detection Flow
    test("FaceDetected: Idle → Curious") {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        result is RoboState.Curious
    }

    test("FaceDetected: Curious → Happy") {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.FaceDetected)
        result is RoboState.Happy
    }

    test("FaceDetected: Sleep → Curious (wake)") {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.FaceDetected)
        result is RoboState.Curious
    }

    // Test 2: Face Lost
    test("FaceLost: Happy → Idle") {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.FaceLost)
        result is RoboState.Idle
    }

    test("FaceLost: Curious → Idle") {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.FaceLost)
        result is RoboState.Idle
    }

    test("FaceLost: Sleep → Sleep (no change)") {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.FaceLost)
        result is RoboState.Sleep
    }

    // Test 3: Loud Sound
    test("LoudSound: Idle → Angry") {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.LoudSoundDetected)
        result is RoboState.Angry
    }

    test("LoudSound: Curious → Angry") {
        val result = RoboReducer.reduce(RoboState.Curious, RoboEvent.LoudSoundDetected)
        result is RoboState.Angry
    }

    test("LoudSound: Sleep → Sleep (ignored)") {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.LoudSoundDetected)
        result is RoboState.Sleep
    }

    // Test 4: Inactivity
    test("Inactivity: Idle → Sleep") {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ProlongedInactivity)
        result is RoboState.Sleep
    }

    test("Inactivity: Happy → Sleep") {
        val result = RoboReducer.reduce(RoboState.Happy, RoboEvent.ProlongedInactivity)
        result is RoboState.Sleep
    }

    // Test 5: Shake Detection
    test("Gentle Shake: Idle → Curious") {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ShakeDetected(0.3f))
        result is RoboState.Curious
    }

    test("Strong Shake: Idle → Angry") {
        val result = RoboReducer.reduce(RoboState.Idle, RoboEvent.ShakeDetected(0.8f))
        result is RoboState.Angry
    }

    test("Shake during Sleep: Sleep → Sleep (ignored)") {
        val result = RoboReducer.reduce(RoboState.Sleep, RoboEvent.ShakeDetected(0.9f))
        result is RoboState.Sleep
    }

    // Test 6: Complex Scenario
    test("Scenario: Complete interaction flow") {
        var state: RoboState = RoboState.Idle

        // Face appears
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        if (state !is RoboState.Curious) return@test false

        // Face stays
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        if (state !is RoboState.Happy) return@test false

        // Face leaves
        state = RoboReducer.reduce(state, RoboEvent.FaceLost)
        if (state !is RoboState.Idle) return@test false

        // Inactivity
        state = RoboReducer.reduce(state, RoboEvent.ProlongedInactivity)
        if (state !is RoboState.Sleep) return@test false

        // Wake up
        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        if (state !is RoboState.Curious) return@test false

        true
    }

    // Test 7: Interruption Scenario
    test("Scenario: Interrupted interaction") {
        var state: RoboState = RoboState.Idle

        state = RoboReducer.reduce(state, RoboEvent.FaceDetected)
        if (state !is RoboState.Curious) return@test false

        // Loud sound interrupts
        state = RoboReducer.reduce(state, RoboEvent.LoudSoundDetected)
        if (state !is RoboState.Angry) return@test false

        true
    }

    // Test 8: Utility Methods
    test("Utility: getStateName") {
        RoboReducer.getStateName(RoboState.Idle) == "Idle" &&
        RoboReducer.getStateName(RoboState.Curious) == "Curious" &&
        RoboReducer.getStateName(RoboState.Happy) == "Happy" &&
        RoboReducer.getStateName(RoboState.Angry) == "Angry" &&
        RoboReducer.getStateName(RoboState.Sleep) == "Sleep"
    }

    test("Utility: isActive") {
        RoboReducer.isActive(RoboState.Idle) &&
        RoboReducer.isActive(RoboState.Curious) &&
        !RoboReducer.isActive(RoboState.Sleep)
    }

    test("Utility: isEngaged") {
        !RoboReducer.isEngaged(RoboState.Idle) &&
        RoboReducer.isEngaged(RoboState.Curious) &&
        RoboReducer.isEngaged(RoboState.Happy) &&
        !RoboReducer.isEngaged(RoboState.Sleep)
    }

    // Test 9: Determinism
    test("Determinism: Same input → same output") {
        val result1 = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        val result2 = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        val result3 = RoboReducer.reduce(RoboState.Idle, RoboEvent.FaceDetected)
        result1 == result2 && result2 == result3 && result1 is RoboState.Curious
    }

    // Print Results
    println()
    println("╔════════════════════════════════════════════════════════╗")
    println("║                  TEST RESULTS                          ║")
    println("╠════════════════════════════════════════════════════════╣")
    println("║  Tests Passed: ${testsPassed.toString().padEnd(40)} ║")
    println("║  Tests Failed: ${testsFailed.toString().padEnd(40)} ║")
    println("║  Total Tests:  ${(testsPassed + testsFailed).toString().padEnd(40)} ║")
    println("╠════════════════════════════════════════════════════════╣")

    if (testsFailed == 0) {
        println("║  Status: ✅ ALL TESTS PASSED!                         ║")
    } else {
        println("║  Status: ❌ SOME TESTS FAILED                         ║")
    }

    println("╚════════════════════════════════════════════════════════╝")
    println()

    // Interactive Demo
    println("═══════════════════════════════════════════════════════")
    println("         INTERACTIVE FSM DEMONSTRATION")
    println("═══════════════════════════════════════════════════════")
    println()

    var currentState: RoboState = RoboState.Idle

    fun applyEvent(event: RoboEvent, description: String) {
        val oldState = RoboReducer.getStateName(currentState)
        currentState = RoboReducer.reduce(currentState, event)
        val newState = RoboReducer.getStateName(currentState)
        println("$description")
        println("  $oldState → $newState")
        println()
    }

    applyEvent(RoboEvent.FaceDetected, "1. 👁️ Face appears")
    applyEvent(RoboEvent.FaceDetected, "2. 👁️ Face still present")
    applyEvent(RoboEvent.LoudSoundDetected, "3. 🔊 Loud noise!")
    applyEvent(RoboEvent.IdleTimeout, "4. ⏱️ Timeout (calming down)")
    applyEvent(RoboEvent.ProlongedInactivity, "5. 😴 No activity...")
    applyEvent(RoboEvent.FaceDetected, "6. 👁️ Face detected (wake up!)")

    println("═══════════════════════════════════════════════════════")
    println("          TASK 5 FSM TESTING COMPLETE! ✅")
    println("═══════════════════════════════════════════════════════")
}

