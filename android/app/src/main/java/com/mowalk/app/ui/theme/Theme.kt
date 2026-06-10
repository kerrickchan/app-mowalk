package com.mowalk.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = Color(0xFF002114),
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = Color(0xFF334D41),
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = Color(0xFFBCECF0),
    onTertiaryContainer = Color(0xFF1F5152),
    error = errorLight,
    onError = onErrorLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFE2E7DD),
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = Color(0xFF00533A),
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = Color(0xFFB2CBDC),
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = Color(0xFF1F5152),
    onTertiaryContainer = Color(0xFF96D0D1),
    error = errorDark,
    onError = onErrorDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    surfaceContainerHighest = Color(0xFF434841),
)

@Immutable
data class ColorPalettes(
    val scheme: androidx.compose.material3.ColorScheme,
)

@Composable
fun AppColorPalettes(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
): ColorPalettes {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    return ColorPalettes(
        scheme = scheme,
    )
}

@Composable
fun MoWalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorPalettes = AppColorPalettes(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
    )

    MaterialTheme(
        colorScheme = colorPalettes.scheme,
        typography = Typography,
        content = content,
    )
}
