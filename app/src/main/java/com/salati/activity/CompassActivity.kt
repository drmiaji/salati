package com.salati.activity

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.salati.ui.CompassPage
import com.salati.ui.components.RotationTarget
import com.salati.utils.CompassViewModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationAddress: String = "Unknown Location"
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private var compassRotation: Float = 0f
    private var qiblaDirection: Float = 0f

    private var currentLocation: Location? = null
    private var rotationSensor: Sensor? = null
    private var currentDegree = 0f
    private var currentDegreeNeedle = 0f

    private val model: CompassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission { getLocation() }

        setContent {
            CompassPage(
                isFacingQilba = isFacingQibla(),
                qilbaRotation = RotationTarget(0f, qiblaDirection),
                compassRotation = RotationTarget(0f, compassRotation),
                locationAddress = locationAddress,
                goToBack = { finish() },
                refreshLocation = { getLocation() }
            )
        }
    }

    private fun checkLocationPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            callback()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission { requestLocationPermission() }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                model.getLocationAddress(this, currentLocation!!)

                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
                rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                rotationSensor?.let { sensor ->
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometerSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometerSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                lastAccelerometerSet = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                lastMagnetometerSet = true
            }
        }

        if (lastAccelerometerSet && lastMagnetometerSet && currentLocation != null) {
            val rotationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val degree = (azimuth + 360) % 360

                // Qibla logic
                val destinationLoc = Location("service Provider").apply {
                    latitude = 21.422487
                    longitude = 39.826206
                }

                var bearTo = currentLocation!!.bearingTo(destinationLoc)
                if (bearTo < 0) bearTo += 360

                var direction = bearTo - degree
                if (direction < 0) direction += 360

                val isFacingQibla = direction in 359.0..360.0 || direction in 0.0..1.0

                // ✅ SMOOTHING: Only update if degree changed significantly
                if (abs(currentDegree - (-degree)) > 0.5f || abs(currentDegreeNeedle - direction) > 0.5f) {
                    // Optional low-pass filter (smoothing)
                    val smoothDegree = (0.85f * currentDegree + 0.15f * -degree)
                    val smoothNeedle = (0.85f * currentDegreeNeedle + 0.15f * direction)

                    val qiblaRotation = RotationTarget(currentDegreeNeedle, smoothNeedle)
                    val compassRotation = RotationTarget(currentDegree, smoothDegree)

                    currentDegree = smoothDegree
                    currentDegreeNeedle = smoothNeedle

                    model.updateCompass(qiblaRotation, compassRotation, isFacingQibla)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun isFacingQibla(): Boolean {
        val threshold = 15 // degrees
        val relativeDirection = (compassRotation - qiblaDirection + 360) % 360
        return relativeDirection < threshold || relativeDirection > 360 - threshold
    }
}