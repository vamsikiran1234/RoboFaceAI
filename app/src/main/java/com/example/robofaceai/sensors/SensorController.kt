package com.example.robofaceai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.view.Surface
import android.view.WindowManager
import com.example.robofaceai.domain.RoboEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * SENSOR FUSION CONTROLLER
 *
 * Features:
 * - Multi-sensor fusion (Accelerometer + Gyroscope)
 * - Advanced signal processing (Complementary + Kalman filters)
 * - Spring physics for natural eye movement
 * - Real-time gesture recognition (shake, tilt, rotation)
 * - Battery-optimized sampling
 */
class SensorController(private val context: Context) : SensorEventListener {

    // ========== SENSOR SERVICES ==========
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // ========== SENSORS ==========
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // ========== STATE FLOWS ==========
    private val _roboEvent = MutableStateFlow<RoboEvent?>(null)
    val roboEvent: StateFlow<RoboEvent?> = _roboEvent.asStateFlow()

    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    private val _tiltY = MutableStateFlow(0f)
    val tiltY: StateFlow<Float> = _tiltY.asStateFlow()

    private val _headRotation = MutableStateFlow(0f)
    val headRotation: StateFlow<Float> = _headRotation.asStateFlow()

    // ========== FILTERING PARAMETERS ==========
    private val lowPassAlpha = 0.2f
    private var lowPassAccel = FloatArray(3)
    private var lowPassGyro = FloatArray(3)

    private val complementaryAlpha = 0.96f
    private var fusedTilt = FloatArray(2)

    private var kalmanTilt = FloatArray(2)
    private var kalmanUncertainty = FloatArray(2) { 1f }
    private val processNoise = 0.003f
    private val measurementNoise = 0.05f

    private var springTilt = FloatArray(2)
    private var springVelocity = FloatArray(2)
    private val springStiffness = 18f
    private val springDamping = 0.6f
    private var lastUpdateTime = 0L

    // ========== SHAKE DETECTION ==========
    private var shakeHistory = mutableListOf<Float>()
    private var shakeTimestamps = mutableListOf<Long>()
    private var lastShakeTime = 0L
    private val shakeThresholdMild = 12f
    private val shakeThresholdStrong = 18f
    private val shakeCooldown = 800L
    private val shakeWindowSize = 10

    private var consecutiveShakes = 0
    private var shakeCount = 0
    private var lastShakeCheckTime = 0L
    private val multiShakeWindow = 1500L

    // ========== GYROSCOPE INTEGRATION ==========
    private var gyroAngleX = 0f
    private var gyroAngleY = 0f
    private var gyroAngleZ = 0f
    private var gyroTimestamp = 0L


    // ========== DEVICE ORIENTATION ==========
    private var deviceRotation = Surface.ROTATION_0

    // ========== CALIBRATION ==========
    private var baselineAccel = floatArrayOf(0f, 0f, 9.81f)
    private var calibrationSamples = 0
    private val calibrationRequired = 30
    private var isCalibrated = false

    // ========== PERFORMANCE ==========
    private var sensorUpdateCount = 0
    private var lastPerformanceLog = 0L

    // AI callback
    var onSensorDataCallback: ((Float, Float, Float) -> Unit)? = null

    // ========== LIFECYCLE ==========

    fun start() {
        android.util.Log.d("SensorController", "🚀 Starting sensor fusion system...")

        try {
            deviceRotation = windowManager.defaultDisplay.rotation
            isCalibrated = false
            calibrationSamples = 0
            lastUpdateTime = System.currentTimeMillis()

            // Start accelerometer
            accelerometer?.let {
                val registered = sensorManager.registerListener(
                    this, it, SensorManager.SENSOR_DELAY_UI
                )
                android.util.Log.d("SensorController",
                    "✓ Accelerometer: ${if (registered) "ACTIVE @ UI RATE (~60-100Hz)" else "FAILED"}")
            }

            // Start gyroscope
            gyroscope?.let {
                val registered = sensorManager.registerListener(
                    this, it, SensorManager.SENSOR_DELAY_UI
                )
                android.util.Log.d("SensorController",
                    "✓ Gyroscope: ${if (registered) "ACTIVE @ UI RATE" else "FAILED"}")
            }


            android.util.Log.d("SensorController", "🎯 Sensor fusion system ready")
        } catch (e: Exception) {
            android.util.Log.e("SensorController", "❌ Error starting sensors: ${e.message}")
        }
    }

    fun stop() {
        try {
            sensorManager.unregisterListener(this)
            android.util.Log.d("SensorController", "🛑 Sensors stopped")
        } catch (e: Exception) {
            android.util.Log.e("SensorController", "Error stopping sensors: ${e.message}")
        }
    }

    // ========== SENSOR EVENT PROCESSING ==========

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerAdvanced(event)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeAdvanced(event)
        }

        // Performance monitoring
        sensorUpdateCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPerformanceLog > 5000) {
            val fps = sensorUpdateCount / 5f
            android.util.Log.d("SensorController", "📊 Sensor FPS: ${"%.1f".format(fps)} Hz")
            sensorUpdateCount = 0
            lastPerformanceLog = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE ->
                android.util.Log.w("SensorController", "⚠ ${sensor?.name} accuracy: UNRELIABLE")
            SensorManager.SENSOR_STATUS_ACCURACY_LOW ->
                android.util.Log.w("SensorController", "⚠ ${sensor?.name} accuracy: LOW")
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ->
                android.util.Log.d("SensorController", "✓ ${sensor?.name} accuracy: MEDIUM")
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH ->
                android.util.Log.d("SensorController", "✓ ${sensor?.name} accuracy: HIGH")
        }
    }

    // ========== ACCELEROMETER PROCESSING ==========

    private fun handleAccelerometerAdvanced(event: SensorEvent) {
        val rawX = event.values[0]
        val rawY = event.values[1]
        val rawZ = event.values[2]

        val (orientedX, orientedY, orientedZ) = compensateDeviceRotation(rawX, rawY, rawZ)

        lowPassAccel[0] = lowPassAlpha * lowPassAccel[0] + (1 - lowPassAlpha) * orientedX
        lowPassAccel[1] = lowPassAlpha * lowPassAccel[1] + (1 - lowPassAlpha) * orientedY
        lowPassAccel[2] = lowPassAlpha * lowPassAccel[2] + (1 - lowPassAlpha) * orientedZ

        if (!isCalibrated) {
            baselineAccel[0] += lowPassAccel[0]
            baselineAccel[1] += lowPassAccel[1]
            baselineAccel[2] += lowPassAccel[2]
            calibrationSamples++

            if (calibrationSamples >= calibrationRequired) {
                baselineAccel[0] /= calibrationSamples
                baselineAccel[1] /= calibrationSamples
                baselineAccel[2] /= calibrationSamples
                isCalibrated = true
                android.util.Log.d("SensorController", "✓ Calibrated gravity: [${baselineAccel.joinToString()}]")
            }
            return
        }

        val tiltAccelX = lowPassAccel[0] - baselineAccel[0]
        val tiltAccelY = lowPassAccel[1] - baselineAccel[1]

        fusedTilt[0] = complementaryAlpha * (fusedTilt[0] + gyroAngleX) + (1 - complementaryAlpha) * tiltAccelX
        fusedTilt[1] = complementaryAlpha * (fusedTilt[1] + gyroAngleY) + (1 - complementaryAlpha) * tiltAccelY

        for (i in 0..1) {
            val predicted = kalmanTilt[i]
            val predictedUncertainty = kalmanUncertainty[i] + processNoise
            val kalmanGain = predictedUncertainty / (predictedUncertainty + measurementNoise)
            kalmanTilt[i] = predicted + kalmanGain * (fusedTilt[i] - predicted)
            kalmanUncertainty[i] = (1 - kalmanGain) * predictedUncertainty
        }

        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        lastUpdateTime = currentTime

        if (deltaTime > 0 && deltaTime < 0.1f) {
            for (i in 0..1) {
                val target = kalmanTilt[i] / 10f
                val displacement = springTilt[i] - target
                val springForce = -springStiffness * displacement
                val dampingForce = -springDamping * springVelocity[i]
                val acceleration = springForce + dampingForce
                springVelocity[i] += acceleration * deltaTime
                springTilt[i] += springVelocity[i] * deltaTime
                springTilt[i] = springTilt[i].coerceIn(-1f, 1f)
            }
        }

        _tiltX.value = springTilt[0]
        _tiltY.value = springTilt[1]

        if (abs(springTilt[0]) > 0.02f || abs(springTilt[1]) > 0.02f) {
            _roboEvent.value = RoboEvent.TiltDetected(springTilt[0], springTilt[1])
        }

        detectShakePattern(rawX, rawY, rawZ, currentTime)
        onSensorDataCallback?.invoke(lowPassAccel[0], lowPassAccel[1], lowPassAccel[2])
    }

    private fun compensateDeviceRotation(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        return when (deviceRotation) {
            Surface.ROTATION_0 -> Triple(x, y, z)
            Surface.ROTATION_90 -> Triple(-y, x, z)
            Surface.ROTATION_180 -> Triple(-x, -y, z)
            Surface.ROTATION_270 -> Triple(y, -x, z)
            else -> Triple(x, y, z)
        }
    }

    private fun detectShakePattern(rawX: Float, rawY: Float, rawZ: Float, currentTime: Long) {
        val magnitude = sqrt(rawX * rawX + rawY * rawY + rawZ * rawZ)

        shakeHistory.add(magnitude)
        shakeTimestamps.add(currentTime)

        if (shakeHistory.size > shakeWindowSize) {
            shakeHistory.removeAt(0)
            shakeTimestamps.removeAt(0)
        }

        if (currentTime - lastShakeTime < shakeCooldown) {
            return
        }

        val peakMagnitude = shakeHistory.maxOrNull() ?: 0f

        if (peakMagnitude > shakeThresholdMild) {
            if (currentTime - lastShakeCheckTime > 100) {
                shakeCount++
                lastShakeCheckTime = currentTime
                android.util.Log.d("SensorController", "🔔 Shake peak detected: ${peakMagnitude.format(1)} m/s² (count=$shakeCount)")
            }
        }

        if (currentTime - lastShakeCheckTime > multiShakeWindow) {
            if (shakeCount > 0) {
                android.util.Log.d("SensorController", "🔄 Shake window expired (count was $shakeCount)")
            }
            shakeCount = 0
        }

        when {
            peakMagnitude > shakeThresholdStrong -> {
                lastShakeTime = currentTime
                consecutiveShakes++
                val intensity = ((peakMagnitude - shakeThresholdStrong) / 10f).coerceIn(0.7f, 1f)
                _roboEvent.value = RoboEvent.ShakeDetected(intensity)
                android.util.Log.d("SensorController", "💢 STRONG SHAKE! Magnitude: ${peakMagnitude.format(1)} m/s²")
                checkDoubleShakePattern(currentTime)
            }
            peakMagnitude > shakeThresholdMild && shakeCount >= 1 -> {
                lastShakeTime = currentTime
                consecutiveShakes++
                val intensity = ((peakMagnitude - shakeThresholdMild) / 8f).coerceIn(0.3f, 0.6f)
                _roboEvent.value = RoboEvent.ShakeDetected(intensity)
                android.util.Log.d("SensorController", "🔍 SHAKE! Magnitude: ${peakMagnitude.format(1)} m/s²")
                shakeCount = 0
            }
        }
    }

    private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)

    private fun checkDoubleShakePattern(currentTime: Long) {
        if (consecutiveShakes >= 2 && currentTime - lastShakeTime < multiShakeWindow) {
            android.util.Log.d("SensorController", "⚡ DOUBLE SHAKE PATTERN detected!")
            consecutiveShakes = 0
        } else if (currentTime - lastShakeTime > multiShakeWindow) {
            consecutiveShakes = 0
        }
    }

    // ========== GYROSCOPE PROCESSING ==========

    private fun handleGyroscopeAdvanced(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        val deltaTime = if (gyroTimestamp != 0L) {
            (currentTime - gyroTimestamp) / 1000f
        } else {
            0f
        }
        gyroTimestamp = currentTime

        if (deltaTime > 0 && deltaTime < 0.1f) {
            val rawGyroX = event.values[0]
            val rawGyroY = event.values[1]
            val rawGyroZ = event.values[2]

            lowPassGyro[0] = lowPassAlpha * lowPassGyro[0] + (1 - lowPassAlpha) * rawGyroX
            lowPassGyro[1] = lowPassAlpha * lowPassGyro[1] + (1 - lowPassAlpha) * rawGyroY
            lowPassGyro[2] = lowPassAlpha * lowPassGyro[2] + (1 - lowPassAlpha) * rawGyroZ

            gyroAngleX = lowPassGyro[0] * deltaTime
            gyroAngleY = lowPassGyro[1] * deltaTime
            gyroAngleZ = lowPassGyro[2] * deltaTime

            val headTiltChange = lowPassGyro[2] * deltaTime * (180f / PI.toFloat())
            val decay = 0.92f
            val newHeadRotation = (_headRotation.value + headTiltChange) * decay
            _headRotation.value = newHeadRotation.coerceIn(-25f, 25f)

            if (abs(lowPassGyro[0]) > 0.3f || abs(lowPassGyro[1]) > 0.3f || abs(lowPassGyro[2]) > 0.3f) {
                _roboEvent.value = RoboEvent.RotationDetected(
                    yaw = lowPassGyro[2],
                    pitch = lowPassGyro[1],
                    roll = lowPassGyro[0]
                )
            }
        }
    }

    // ========== UTILITY METHODS ==========

    fun areSensorsAvailable(): Pair<Boolean, Boolean> {
        return Pair(
            accelerometer != null,
            gyroscope != null
        )
    }

    fun isCalibrated(): Boolean = isCalibrated

    fun recalibrate() {
        isCalibrated = false
        calibrationSamples = 0
        baselineAccel = floatArrayOf(0f, 0f, 9.81f)
        android.util.Log.d("SensorController", "🔄 Recalibration requested")
    }

    fun getSensorStats(): String {
        return buildString {
            appendLine("Sensor Fusion Stats:")
            appendLine("├─ Tilt: [X: ${"%.2f".format(_tiltX.value)}, Y: ${"%.2f".format(_tiltY.value)}]")
            appendLine("├─ Head Rotation: ${"%.1f".format(_headRotation.value)}°")
            appendLine("├─ Calibrated: $isCalibrated")
            appendLine("└─ Spring Velocity: [X: ${"%.3f".format(springVelocity[0])}, Y: ${"%.3f".format(springVelocity[1])}]")
        }
    }
}

