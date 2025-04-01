package com.salati.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.salati.R

val alifFontFamily = FontFamily(
    Font(resId = R.font.roboto_light, weight = FontWeight.Light),
    Font(resId = R.font.roboto_regular, weight = FontWeight.Normal),
    Font(resId = R.font.roboto_medium, weight = FontWeight.SemiBold),
    Font(resId = R.font.roboto_bold, weight = FontWeight.Bold),
)

val AlifTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = alifFontFamily,
        fontSize = 26.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = TextStyle(
        fontFamily = alifFontFamily,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = TextStyle(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = TextStyle( // title
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Light
    )
)