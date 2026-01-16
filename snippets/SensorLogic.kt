class AccelerometerRepository @Inject constructor(
    private val sensorManager: SensorManager
) {
    // Threshold: -8.0 allows for slight tilt (not perfectly flat), but definitely "down"
    private val faceDownThreshold = -8.0f

    /**
     * Converts the legacy SensorManager callback system into a cold Flow.
     * This ensures listeners are only active when the UI is actually observing the data,
     * saving significant battery life.
     */
    val orientationFlow: Flow<OrientationState> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val zAxis = it.values[2] // Index 2 is the Z-axis

                    // Map raw physics to Domain State
                    val newState = if (zAxis < faceDownThreshold) {
                        OrientationState.FACE_DOWN
                    } else {
                        OrientationState.FACE_UP
                    }

                    trySend(newState)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op for this use case
            }
        }

        // Register the listener (Sampling rate: UI is sufficient for now)
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        // Cleanup when the Flow collection stops (Crucial for battery!)
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
        // Optimization: Only emit if the state actually changes
        .distinctUntilChanged()
        // Optimization: Run on IO thread
        .flowOn(Dispatchers.IO)
}