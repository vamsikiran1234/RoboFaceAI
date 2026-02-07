package com.example.robofaceai.ai

import android.content.Context
import android.util.Log
import com.example.robofaceai.domain.RoboEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AI Manager that coordinates TFLite inference with sensor data.
 * Collects sensor readings over time and runs periodic predictions.
 */
class AIManager(context: Context) {

    private val tfliteEngine = TFLiteEngine(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Sensor data buffer (rolling window)
    private val sensorBuffer = mutableListOf<Float>()
    private val maxBufferSize = 30 // Store last 30 readings

    // AI inference results
    private val _aiEvent = MutableStateFlow<RoboEvent.AIResult?>(null)
    val aiEvent: StateFlow<RoboEvent.AIResult?> = _aiEvent.asStateFlow()

    // Prediction flow for ViewModel
    private val _predictions = MutableStateFlow("")
    val predictions: StateFlow<String> = _predictions.asStateFlow()

    // Inference stats for display
    private val _inferenceStats = MutableStateFlow(InferenceStats())
    val inferenceStats: StateFlow<InferenceStats> = _inferenceStats.asStateFlow()

    data class InferenceStats(
        val latencyMs: Long = 0,
        val prediction: String = "N/A",
        val confidence: Float = 0f,
        val isModelLoaded: Boolean = false,
        val inferenceCount: Int = 0
    )

    private var inferenceJob: Job? = null
    private var isRunning = false

    /**
     * Initialize the AI engine
     */
    fun initialize() {
        val isModelAvailable = tfliteEngine.isModelAvailable()

        if (isModelAvailable) {
            val success = tfliteEngine.initialize()
            _inferenceStats.value = _inferenceStats.value.copy(isModelLoaded = success)
            Log.d(TAG, "AI Manager initialized with TFLite model")
        } else {
            _inferenceStats.value = _inferenceStats.value.copy(isModelLoaded = false)
            Log.w(TAG, "TFLite model file not found, using dummy predictions")
        }
    }

    /**
     * Start periodic AI inference
     */
    fun start() {
        if (isRunning) return

        isRunning = true
        inferenceJob = scope.launch {
            while (isActive && isRunning) {
                runInference()
                delay(1000) // Run inference every 1 second
            }
        }

        Log.d(TAG, "AI Manager started")
    }

    /**
     * Stop AI inference
     */
    fun stop() {
        isRunning = false
        inferenceJob?.cancel()
        Log.d(TAG, "AI Manager stopped")
    }

    /**
     * Add sensor reading to buffer (alias for compatibility)
     */
    fun feedSensorData(x: Float, y: Float, z: Float) {
        addSensorData(x, y, z)
    }

    /**
     * Add sensor reading to buffer
     */
    fun addSensorData(x: Float, y: Float, z: Float) {
        synchronized(sensorBuffer) {
            sensorBuffer.add(x)
            sensorBuffer.add(y)
            sensorBuffer.add(z)

            // Keep buffer size limited
            while (sensorBuffer.size > maxBufferSize) {
                sensorBuffer.removeAt(0)
            }
        }
    }

    /**
     * Get current stats
     */
    fun getStats(): InferenceStats {
        return _inferenceStats.value
    }

    /**
     * Run AI inference on collected sensor data
     */
    private suspend fun runInference() {
        val data = synchronized(sensorBuffer) {
            if (sensorBuffer.size < 6) return // Need at least 2 readings
            sensorBuffer.toFloatArray()
        }

        // Run intelligent rule-based inference
        val result = tfliteEngine.predict(data)

        // Update stats
        val currentStats = _inferenceStats.value
        _inferenceStats.value = InferenceStats(
            latencyMs = result.latencyMs,
            prediction = result.prediction,
            confidence = result.confidence,
            isModelLoaded = currentStats.isModelLoaded,
            inferenceCount = currentStats.inferenceCount + 1
        )

        // Emit prediction to flow
        _predictions.value = result.prediction

        // Emit AI event if confidence is high enough
        // IMPORTANT: Don't emit "sleep" predictions to prevent unwanted state changes
        if (result.confidence > 0.7f && result.prediction != "sleep") {
            _aiEvent.value = RoboEvent.AIResult(result.prediction, result.confidence)
        }
    }

    /**
     * Manually trigger inference
     */
    suspend fun triggerInference() {
        runInference()
    }

    /**
     * Release resources
     */
    fun close() {
        stop()
        tfliteEngine.close()
        scope.cancel()
        Log.d(TAG, "AI Manager closed")
    }

    companion object {
        private const val TAG = "AIManager"
    }
}


