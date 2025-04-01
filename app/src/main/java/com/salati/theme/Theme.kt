package com.salati.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Primary,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    secondary = Primary,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun AlifTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlifTypography, // Define it using Material 3 names!
        shapes = Shapes,
        content = content
    )
}

object AlifThemes {

    private val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = AlifTypography

    val Colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    object TextStyles {
        val headingXLarge: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.displayLarge

        val headingLarge: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.displayMedium

        val heading: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.displaySmall

        val title: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.headlineLarge

        val titleSmall: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.headlineSmall

        val subtitle: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.titleMedium

        val body: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.bodyLarge

        val bodySmall: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = typography.bodySmall
    }
}