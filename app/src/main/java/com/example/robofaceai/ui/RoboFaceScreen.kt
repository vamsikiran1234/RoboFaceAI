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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.robofaceai.ai.AIManager
import com.example.robofaceai.domain.RoboState
import com.example.robofaceai.viewmodel.RoboViewModel

/**
 * Main screen displaying the Robo Face with controls.
 * This is the entry point for the UI.
 */
@Composable
fun RoboFaceScreen(
    viewModel: RoboViewModel = viewModel(),
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    aiStats: AIManager.InferenceStats = AIManager.InferenceStats(),
    modifier: Modifier = Modifier
) {
    val currentState by viewModel.state.collectAsState()
    val stateName by viewModel.stateName.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Robo Face Canvas
        RoboCanvas(
            state = currentState,
            tiltX = tiltX,
            tiltY = tiltY,
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
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

/**
 * Display current state name
 */
@Composable
private fun StateIndicator(
    stateName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "State: $stateName",
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Display AI inference statistics
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
            text = "AI STATS",
            color = Color.Cyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Latency: ${stats.latencyMs}ms",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Prediction: ${stats.prediction}",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Confidence: ${(stats.confidence * 100).toInt()}%",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Inferences: ${stats.inferenceCount}",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "Engine: Rule-Based AI",
            color = if (stats.isModelLoaded) Color.Green else Color.Cyan,
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateButton("Idle", RoboState.Idle, viewModel)
            StateButton("Curious", RoboState.Curious, viewModel)
            StateButton("Happy", RoboState.Happy, viewModel)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateButton("Angry", RoboState.Angry, viewModel)
            StateButton("Sleep", RoboState.Sleep, viewModel)
        }
        Button(
            onClick = { viewModel.cycleStates() }
        ) {
            Text("Cycle All States")
        }
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



