package com.example.mobidesk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FluentDarkColorScheme = darkColorScheme(
    primary = FluentBlueDark,
    onPrimary = TextPrimaryLight,
    primaryContainer = FluentSurfaceVariantDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = FluentBlueDarkHover,
    onSecondary = TextPrimaryDark,
    background = FluentBgDark,
    onBackground = TextPrimaryDark,
    surface = FluentSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = FluentSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = FluentBorderDark,
    error = StatusError
)

private val FluentLightColorScheme = lightColorScheme(
    primary = FluentBlueLight,
    onPrimary = TextPrimaryDark,
    primaryContainer = FluentSurfaceVariantLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = FluentBlueHover,
    onSecondary = TextPrimaryDark,
    background = FluentBgLight,
    onBackground = TextPrimaryLight,
    surface = FluentSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = FluentSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = FluentBorderLight,
    error = StatusError
)

@Composable
fun MobideskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Prefer Microsoft Fluent theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FluentDarkColorScheme
        else -> FluentLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
