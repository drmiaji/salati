package com.salati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.salati.compass.CompassPage
import com.salati.compass.RotationTarget

class CompassActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompassPage(
                isFacingQilba = false,
                qilbaRotation = RotationTarget(0f, 294f),
                compassRotation = RotationTarget(0f, 0f),
                locationAddress = "Malmö, Sweden",
                goToBack = { finish() },
                refreshLocation = { /* Implement location refresh logic */ }
            )
        }
    }
}