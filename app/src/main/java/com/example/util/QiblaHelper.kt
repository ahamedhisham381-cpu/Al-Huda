package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class QiblaHelper(context: Context) : SensorEventListener {

    // Kaaba coordinates in Makkah
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _hasSensor = MutableStateFlow(rotationSensor != null)
    val hasSensor: StateFlow<Boolean> = _hasSensor.asStateFlow()

    fun startListening() {
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            _azimuth.value = (azimuthDeg + 360f) % 360f
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            _azimuth.value = (event.values[0] + 360f) % 360f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setManualAzimuth(degrees: Float) {
        _azimuth.value = (degrees + 360f) % 360f
    }

    /**
     * Calculates the Qibla bearing from user coordinates (lat, lng) to Kaaba.
     * Returns azimuth in degrees [0, 360).
     */
    fun calculateQiblaBearing(lat: Double, lng: Double): Float {
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(kaabaLat)
        val deltaLng = Math.toRadians(kaabaLng - lng)

        val y = sin(deltaLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLng)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla.toFloat()
    }

    /**
     * Calculates the great-circle distance to Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(kaabaLat - lat)
        val dLng = Math.toRadians(kaabaLng - lng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(kaabaLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
