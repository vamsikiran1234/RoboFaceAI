package com.example.robofaceai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.robofaceai.domain.RoboEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Manages all device sensors for Task 3.
 *
 * Sensors integrated:
 * - Accelerometer (tilt detection, shake detection)
 * - Gyroscope (rotation detection)
 * - Proximity (sleep/wake detection)
 *
 * Implements low-pass filtering for smooth sensor data.
 */
class SensorController(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Sensors
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // Event flows
    private val _roboEvent = MutableStateFlow<RoboEvent?>(null)
    val roboEvent: StateFlow<RoboEvent?> = _roboEvent.asStateFlow()

    // Tilt values (normalized -1 to 1)
    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    private val _tiltY = MutableStateFlow(0f)
    val tiltY: StateFlow<Float> = _tiltY.asStateFlow()

    // Filtered sensor values
    private var filteredAccel = FloatArray(3)
    private var filteredGyro = FloatArray(3)

    // Shake detection
    private var lastShakeTime = 0L
    private val shakeThreshold = 15f // m/s²
    private val shakeCooldown = 1000L // ms

    // Low-pass filter coefficient
    private val alpha = 0.8f

    // Proximity state
    private var isProximityNear = false

    // Callback for feeding data to AI
    var onSensorDataCallback: ((Float, Float, Float) -> Unit)? = null

    /**
     * Start listening to all sensors
     */
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        proximitySensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * Stop listening to all sensors
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_GYROSCOPE -> handleGyroscope(event)
            Sensor.TYPE_PROXIMITY -> handleProximity(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    /**
     * Handle accelerometer data (tilt and shake)
     */
    private fun handleAccelerometer(event: SensorEvent) {
        // Apply low-pass filter for smoothness
        filteredAccel[0] = alpha * filteredAccel[0] + (1 - alpha) * event.values[0]
        filteredAccel[1] = alpha * filteredAccel[1] + (1 - alpha) * event.values[1]
        filteredAccel[2] = alpha * filteredAccel[2] + (1 - alpha) * event.values[2]

        // Feed data to AI Manager
        onSensorDataCallback?.invoke(filteredAccel[0], filteredAccel[1], filteredAccel[2])

        // Update tilt values (normalized)
        // X: left/right tilt
        // Y: forward/backward tilt
        _tiltX.value = (filteredAccel[0] / 10f).coerceIn(-1f, 1f)
        _tiltY.value = (filteredAccel[1] / 10f).coerceIn(-1f, 1f)

        // Emit tilt event
        _roboEvent.value = RoboEvent.TiltDetected(_tiltX.value, _tiltY.value)

        // Shake detection (using raw values for sensitivity)
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
            event.values[1] * event.values[1] +
            event.values[2] * event.values[2]
        )

        val currentTime = System.currentTimeMillis()
        if (magnitude > shakeThreshold && currentTime - lastShakeTime > shakeCooldown) {
            lastShakeTime = currentTime
            val intensity = ((magnitude - shakeThreshold) / 10f).coerceIn(0f, 1f)
            _roboEvent.value = RoboEvent.ShakeDetected(intensity)
        }
    }

    /**
     * Handle gyroscope data (rotation)
     */
    private fun handleGyroscope(event: SensorEvent) {
        // Apply low-pass filter
        filteredGyro[0] = alpha * filteredGyro[0] + (1 - alpha) * event.values[0]
        filteredGyro[1] = alpha * filteredGyro[1] + (1 - alpha) * event.values[1]
        filteredGyro[2] = alpha * filteredGyro[2] + (1 - alpha) * event.values[2]

        // Emit rotation event
        _roboEvent.value = RoboEvent.RotationDetected(
            yaw = filteredGyro[2],
            pitch = filteredGyro[1],
            roll = filteredGyro[0]
        )
    }

    /**
     * Handle proximity sensor (sleep/wake)
     */
    private fun handleProximity(event: SensorEvent) {
        val distance = event.values[0]
        val maxRange = event.sensor.maximumRange

        // Near if distance is very close (typically < 5cm)
        val wasNear = isProximityNear
        isProximityNear = distance < maxRange * 0.1f

        // Only emit event on state change
        if (wasNear != isProximityNear) {
            _roboEvent.value = RoboEvent.ProximityChanged(isProximityNear)
        }
    }

    /**
     * Check if all required sensors are available
     */
    fun areSensorsAvailable(): Triple<Boolean, Boolean, Boolean> {
        return Triple(
            accelerometer != null,
            gyroscope != null,
            proximitySensor != null
        )
    }
}



