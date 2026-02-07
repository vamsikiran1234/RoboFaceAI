package com.example.robofaceai

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.robofaceai.ai.AIManager
import com.example.robofaceai.sensors.SensorController
import com.example.robofaceai.ui.RoboFaceScreen
import com.example.robofaceai.ui.theme.RoboFaceAITheme
import com.example.robofaceai.viewmodel.RoboViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sensorController: SensorController
    private lateinit var aiManager: AIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on for demo
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize controllers
        sensorController = SensorController(this)
        aiManager = AIManager(this)
        aiManager.initialize()

        // Connect sensor data to AI Manager
        sensorController.onSensorDataCallback = { x, y, z ->
            aiManager.addSensorData(x, y, z)
        }

        enableEdgeToEdge()
        setContent {
            RoboFaceAITheme {
                val viewModel: RoboViewModel = viewModel()

                // Collect sensor tilt values
                val tiltX by sensorController.tiltX.collectAsState()
                val tiltY by sensorController.tiltY.collectAsState()

                // Collect AI inference stats
                val aiStats by aiManager.inferenceStats.collectAsState()

                // Connect sensor events to ViewModel
                LaunchedEffect(Unit) {
                    sensorController.roboEvent.collect { event ->
                        event?.let { viewModel.handleEvent(it) }
                    }
                }

                // Connect AI events to ViewModel
                LaunchedEffect(Unit) {
                    aiManager.aiEvent.collect { event ->
                        event?.let { viewModel.handleEvent(it) }
                    }
                }

                RoboFaceScreen(
                    viewModel = viewModel,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    aiStats = aiStats,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorController.start()
        aiManager.start()
    }

    override fun onPause() {
        super.onPause()
        sensorController.stop()
        aiManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        aiManager.close()
    }
}




