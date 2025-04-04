package com.salati.activity

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.salati.ui.CompassPage
import com.salati.ui.components.RotationTarget
import java.util.Locale
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    updateLocationAddress(it)
                    calculateQiblaDirection(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun updateLocationAddress(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            locationAddress = addresses[0].getAddressLine(0)
            updateUI()
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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == accelerometerSensor) {
            lowPass(event.values, lastAccelerometer)  // Smoothing accelerometer data
            lastAccelerometerSet = true
        } else if (event.sensor == magnetometerSensor) {
            lowPass(event.values, lastMagnetometer)  // Smoothing magnetometer data
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val rotationMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // Get the azimuth in radians
                val azimuthInRadians = orientation[0]

                // Convert azimuth from radians to degrees
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                // Normalize azimuth to ensure it stays within 0-360 degrees
                compassRotation = (azimuthInDegrees + 360) % 360

                updateUI()
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.4f  // Increased smoothing factor
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    private fun calculateQiblaDirection(latitude: Double, longitude: Double) {
        val mKaabaLatitude = 21.422487
        val mKaabaLongitude = 39.826206

        val latRad = Math.toRadians(latitude)
        val longRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(mKaabaLatitude)
        val kaabaLongRad = Math.toRadians(mKaabaLongitude)

        val y = sin(kaabaLongRad - longRad) * cos(kaabaLatRad)
        val x = cos(latRad) * sin(kaabaLatRad) - sin(latRad) * cos(kaabaLatRad) * cos(kaabaLongRad - longRad)

        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360

        qiblaDirection = bearing.toFloat()
    }

    private fun isFacingQibla(): Boolean {
        val threshold = 15 // degrees
        val relativeDirection = (compassRotation - qiblaDirection + 360) % 360
        return relativeDirection < threshold || relativeDirection > 360 - threshold
    }

    private var lastCompassRotation = -1f

    private fun updateUI() {
        // Update UI only if the compass rotation has changed significantly
        if (abs(compassRotation - lastCompassRotation) > 5f) {
            lastCompassRotation = compassRotation
            val relativeQiblaDirection = (qiblaDirection - compassRotation + 360) % 360
            setContent {
                CompassPage(
                    isFacingQilba = isFacingQibla(),
                    qilbaRotation = RotationTarget(0f, relativeQiblaDirection),
                    compassRotation = RotationTarget(0f, compassRotation),
                    locationAddress = locationAddress,
                    goToBack = { finish() },
                    refreshLocation = { getLocation() }
                )
            }
        }
    }
}