package com.example.robofaceai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.robofaceai.ai.AIManager
import com.example.robofaceai.domain.RoboState
import com.example.robofaceai.viewmodel.RoboViewModel

/**
 * Main screen displaying the Robo Face with sensor-driven interaction.
 *
 * TASK 2 & 3 Integration:
 * - Displays native vector Robo Face (Task 2)
 * - Real-time sensor fusion for eye tracking (Task 3)
 * - State machine integration
 * - AI stats overlay
 */
@Composable
fun RoboFaceScreen(
    viewModel: RoboViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Get ViewModel (either passed in or create new one)
    val vm = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<RoboViewModel>()

    // Collect state from ViewModel
    val currentState by vm.state.collectAsState()
    val stateName by vm.stateName.collectAsState()

    // === TASK 3: Collect sensor values from ViewModel ===
    val tiltX by vm.tiltX.collectAsState()
    val tiltY by vm.tiltY.collectAsState()
    val headRotation by vm.headRotation.collectAsState()

    // Collect AI stats
    val aiStats by vm.aiStats.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Robo Face Canvas (TASK 2 + 3: Vector graphics with sensor interaction)
        RoboFaceCanvas(
            state = currentState,
            tiltX = tiltX,           // Sensor-driven eye movement
            tiltY = tiltY,           // Sensor-driven eye movement
            headRotation = headRotation, // Gyroscope head tilt
            modifier = Modifier.fillMaxSize()
        )

        // State indicator (top)
        StateIndicator(
            stateName = stateName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )

        // AI Stats (top right)
        AIStatsDisplay(
            stats = aiStats,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
        )

        // Test controls (bottom) - for development/demo
        TestControls(
            viewModel = vm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

/**
 * Display current state name (text only, no emoji)
 */
@Composable
private fun StateIndicator(
    stateName: String,
    modifier: Modifier = Modifier
) {
    val color = when (stateName) {
        "Sleep" -> Color(0xFF6495ED) // Blue
        "Angry" -> Color(0xFFFF4444) // Red
        "Happy" -> Color(0xFF4CAF50) // Green
        "Curious" -> Color(0xFFFFEB3B) // Yellow
        "Idle" -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "State: $stateName",
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Display AI inference statistics (Task 6 requirement)
 */
@Composable
private fun AIStatsDisplay(
    stats: AIManager.InferenceStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "🧠 AI ENGINE",
            color = Color.Cyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Accelerator mode
        Text(
            text = "Mode: ${stats.acceleratorMode}",
            color = when (stats.acceleratorMode) {
                "GPU" -> Color.Green
                "NNAPI" -> Color.Yellow
                else -> Color.White
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // Latency
        Text(
            text = "Latency: ${stats.latencyMs}ms",
            color = when {
                stats.latencyMs < 10 -> Color.Green
                stats.latencyMs < 50 -> Color.Yellow
                else -> Color.Red
            },
            fontSize = 12.sp
        )

        // FPS
        Text(
            text = "FPS: ${"%.1f".format(stats.fps)}",
            color = when {
                stats.fps > 30 -> Color.Green
                stats.fps > 10 -> Color.Yellow
                else -> Color.Red
            },
            fontSize = 12.sp
        )

        Text(
            text = "Prediction: ${stats.prediction}",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Confidence: ${(stats.confidence * 100).toInt()}%",
            color = if (stats.confidence > 0.7f) Color.Green else Color.Yellow,
            fontSize = 12.sp
        )
        Text(
            text = "Inferences: ${stats.inferenceCount}",
            color = Color.White,
            fontSize = 11.sp
        )
        Text(
            text = if (stats.isModelLoaded) "✓ TFLite Ready" else "Rule-Based AI",
            color = if (stats.isModelLoaded) Color.Green else Color.Cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Display real-time sensor values for debugging
 */
@Composable
private fun SensorDebugDisplay(
    tiltX: Float,
    tiltY: Float,
    headRotation: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "SENSORS 📡",
            color = Color.Green,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tilt X: ${"%.2f".format(tiltX)}",
            color = if (kotlin.math.abs(tiltX) > 0.1f) Color.Yellow else Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Tilt Y: ${"%.2f".format(tiltY)}",
            color = if (kotlin.math.abs(tiltY) > 0.1f) Color.Yellow else Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Rotation: ${"%.1f".format(headRotation)}°",
            color = if (kotlin.math.abs(headRotation) > 5f) Color.Yellow else Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "🎯 ${if (kotlin.math.abs(tiltX) > 0.1f || kotlin.math.abs(tiltY) > 0.1f) "TRACKING" else "STABLE"}",
            color = if (kotlin.math.abs(tiltX) > 0.1f || kotlin.math.abs(tiltY) > 0.1f) Color.Green else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Test controls for manually triggering state changes
 * (These can be hidden in final demo)
 */
@Composable
private fun TestControls(
    viewModel: RoboViewModel,
    modifier: Modifier = Modifier
) {
    val isAIEnabled by viewModel.isAIEnabled.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Dynamic AI status display
        Text(
            text = if (isAIEnabled) "🤖 AI: ACTIVE" else "🔘 MANUAL MODE",
            color = if (isAIEnabled) Color(0xFF4CAF50) else Color(0xFFFFEB3B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        // State buttons - INCLUDING IDLE
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateButton("Idle", RoboState.Idle, viewModel)
            StateButton("Curious", RoboState.Curious, viewModel)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateButton("Happy", RoboState.Happy, viewModel)
            StateButton("Angry", RoboState.Angry, viewModel)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateButton("Sleep", RoboState.Sleep, viewModel)
        }

        // Cycle button
        Button(
            onClick = { viewModel.cycleStates() }
        ) {
            Text("Cycle All States")
        }

        // Instructions
        Text(
            text = "💡 Cover top of phone for Sleep | Shake for Angry/Curious\n" +
                   "🔘 Manual buttons disable AI until you move the phone",
            color = Color(0xFFFFEB3B),
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Button to set a specific state
 */
@Composable
private fun StateButton(
    label: String,
    targetState: RoboState,
    viewModel: RoboViewModel
) {
    Button(
        onClick = { viewModel.setStateManually(targetState) }
    ) {
        Text(label)
    }
}



