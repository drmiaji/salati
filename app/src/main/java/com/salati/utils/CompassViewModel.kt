package com.salati.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salati.ui.components.RotationTarget
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.apply
import kotlin.collections.first
import kotlin.let

class CompassViewModel : ViewModel() {

    var isFacingQibla by mutableStateOf(false)
    var qilbaRotation by mutableStateOf(RotationTarget(0f, 0f))
    var compassRotation by mutableStateOf(RotationTarget(0f, 0f))

    var locationAddress by mutableStateOf("-")

    fun updateCompass(qilba: RotationTarget, compass: RotationTarget, isFacing: Boolean) {
        isFacingQibla = isFacing
        qilbaRotation = qilba
        compassRotation = compass
    }

    fun getLocationAddress(context: Context, location: Location) {
        viewModelScope.launch {
            Geocoder(context, Locale.getDefault()).apply {
                getFromLocation(location.latitude, location.longitude, 1)?.first()
                    ?.let { address ->
                        locationAddress = buildString {
                            append(address.locality).append(", ")
                            append(address.subAdminArea)
                        }
                    }
            }
        }
    }
}