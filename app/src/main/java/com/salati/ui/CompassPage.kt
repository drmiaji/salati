package com.salati.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.salati.R
import com.salati.ui.components.ActionButton
import com.salati.ui.components.RotationTarget
import com.salati.ui.components.TextBody
import com.salati.ui.components.TextHeading
import com.salati.ui.components.TextHeadingXLarge
import com.salati.ui.components.TextTitle
import com.salati.theme.White

@Composable
fun CompassPage(
    isFacingQilba: Boolean,
    qilbaRotation: RotationTarget,
    compassRotation: RotationTarget,
    locationAddress: String,
    goToBack: () -> Unit,
    refreshLocation: () -> Unit
) {
    Scaffold {
        ConstraintLayout(
            modifier = Modifier.Companion
                .padding(it)
                .fillMaxSize()
        ) {
            val (bg, back, refresh, title, degree, description, location, windDir, compass) = createRefs()
            val realDegree = 360 - qilbaRotation.to

            Image(
                painterResource(id = R.drawable.ic_bg_schedule),
                contentDescription = "",
                contentScale = ContentScale.Companion.FillHeight,
                modifier = Modifier.Companion.constrainAs(bg) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.Companion.fillToConstraints
                    height = Dimension.Companion.percent(0.25f)
                })

            ActionButton(modifier = Modifier.Companion.constrainAs(back) {
                top.linkTo(parent.top, margin = 32.dp)
                start.linkTo(parent.start, margin = 32.dp)
            }, icon = R.drawable.ic_arrow_back, type = 1, goToBack)

            ActionButton(modifier = Modifier.Companion.constrainAs(refresh) {
                top.linkTo(parent.top, margin = 32.dp)
                end.linkTo(parent.end, margin = 32.dp)
            }, icon = R.drawable.ic_repeat, type = 1, refreshLocation)

            TextTitle(modifier = Modifier.Companion.constrainAs(title) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(back.top)
                bottom.linkTo(back.bottom)
                bottom.linkTo(degree.top, margin = 8.dp) // Add margin to avoid overlap
            }, text = "Find Qibla Direction", textColor = White)

            TextHeadingXLarge(
                modifier = Modifier.Companion.constrainAs(degree) {
                    bottom.linkTo(description.top, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(title.bottom, margin = 8.dp) // Add margin to avoid overlap
                }, text = "${realDegree.toInt()}°", textColor = White
            )

            Row(modifier = Modifier.Companion.constrainAs(description) {
                bottom.linkTo(location.top, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }) {
                if (realDegree in 1f..180f && !isFacingQilba) Image(
                    modifier = Modifier.Companion.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "",
                    colorFilter = ColorFilter.Companion.tint(White)
                )
                if (isFacingQilba) TextHeading(
                    text = "You are facing the Qibla", textColor = White
                )
                if (realDegree in 181f..360f && !isFacingQilba) Image(
                    modifier = Modifier.Companion
                        .size(24.dp)
                        .rotate(180f),
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "",
                    colorFilter = ColorFilter.Companion.tint(White)
                )
            }

            Row(
                modifier = Modifier.Companion.constrainAs(location) {
                    bottom.linkTo(bg.bottom, 32.dp)
                    start.linkTo(back.end)
                    end.linkTo(refresh.start)
                    width = Dimension.Companion.fillToConstraints
                }, horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "",
                    modifier = Modifier.Companion.size(22.dp)
                )
                TextBody(
                    modifier = Modifier.Companion.padding(top = 2.dp, start = 4.dp),
                    text = locationAddress,
                    textColor = White
                )
            }

            Image(
                modifier = Modifier.Companion
                    .rotate(compassRotation.to)
                    .constrainAs(compass) {
                        top.linkTo(bg.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                painter = painterResource(
                    if (isSystemInDarkTheme()) R.drawable.ic_wind_direction_night
                    else R.drawable.ic_wind_direction
                ),
                contentDescription = "",
            )

            Image(
                modifier = Modifier.Companion
                    .rotate(qilbaRotation.to)
                    .constrainAs(windDir) {
                        top.linkTo(compass.top)
                        bottom.linkTo(compass.bottom)
                        start.linkTo(compass.start)
                        end.linkTo(compass.end)
                    },
                painter = painterResource(id = R.drawable.ic_compass_direction),
                contentDescription = ""
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCompassPage() {
    CompassPage(false,
        RotationTarget(0f, 294f),
        RotationTarget(0f, 0f),
        "Malmö, Sweden",
        {},
        {})
}