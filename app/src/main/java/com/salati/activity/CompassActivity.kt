package com.salati.activity

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.Address
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.salati.theme.AlifTheme
import com.salati.ui.components.RotationTarget
import com.salati.utils.LocationUtils
import com.salati.utils.LocationUtils.checkLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.salati.utils.CompassViewModel
import com.salati.ui.CompassPage
import java.io.IOException
import java.util.Locale
import kotlin.math.abs

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private var currentDegree = 0f
    private var currentDegreeNeedle = 0f

    private val model: CompassViewModel by viewModels()

    private var locationAddress: String = "Unknown Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlifTheme {
                CompassPage(
                    model.isFacingQibla,
                    model.qilbaRotation,
                    model.compassRotation,
                    model.locationAddress,
                    goToBack = { finish() },
                    refreshLocation = {
                        if (this::sensorManager.isInitialized) {
                            sensorManager.unregisterListener(this)
                        }
                        getLocation()
                    }
                )
            }
        }
        getLocation()
    }

    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission { requestLocationPermission() }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                updateLocationAddress(it)  // Call the function here
                model.getLocationAddress(this, currentLocation)

                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
                rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                rotationSensor?.let { sensor ->
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    private fun updateLocationAddress(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                locationAddress = buildString {
                    append(address.getAddressLine(0)).append(", ")
                   // append(address.locality).append(", ")
                  //  append(address.adminArea).append(", ")
                  //  append(address.postalCode).append(", ")
                   // append(address.countryName)
                }
                updateUI()
            } else {
                locationAddress = "Address not found"
                updateUI()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            locationAddress = "Unable to get address"
            updateUI()
        }
    }

    private fun updateUI() {
        setContent {
            AlifTheme {
                CompassPage(
                    model.isFacingQibla,
                    model.qilbaRotation,
                    model.compassRotation,
                    locationAddress,
                    goToBack = { finish() },
                    refreshLocation = {
                        if (this::sensorManager.isInitialized) {
                            sensorManager.unregisterListener(this)
                        }
                        getLocation()
                    }
                )
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val degree = (azimuth + 360) % 360

        // Qibla logic
        val destinationLoc = Location("service Provider").apply {
            latitude = 21.422487
            longitude = 39.826206
        }

        var bearTo = currentLocation.bearingTo(destinationLoc)
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        if (this::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    @SuppressLint("NewApi")
    fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            LocationUtils.handlePermission(permissions)
        }
        LocationUtils.launchPermission(locationPermissionRequest)
    }
}

@Preview(showBackground = true)
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AlifTheme {
        Greeting("Android")
    }
}