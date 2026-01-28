package com.awada.synapse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Color scheme using Pixso design tokens
 */
private val DarkColorScheme = darkColorScheme(
    primary = PixsoColors.Color_Primary_Primary_70,
    onPrimary = PixsoColors.Color_Neutral_Neutral_0,
    primaryContainer = PixsoColors.Color_Primary_Primary_30,
    onPrimaryContainer = PixsoColors.Color_Primary_Primary_90,
    secondary = PixsoColors.Color_Secondary_Secondary_70,
    onSecondary = PixsoColors.Color_Neutral_Neutral_0,
    secondaryContainer = PixsoColors.Color_Secondary_Secondary_30,
    onSecondaryContainer = PixsoColors.Color_Secondary_Secondary_90,
    tertiary = PixsoColors.Color_Secondary_Secondary_70,
    onTertiary = PixsoColors.Color_Neutral_Neutral_0,
    tertiaryContainer = PixsoColors.Color_Secondary_Secondary_30,
    onTertiaryContainer = PixsoColors.Color_Secondary_Secondary_90,
    error = PixsoColors.Color_Error_Error_70,
    onError = PixsoColors.Color_Neutral_Neutral_0,
    errorContainer = PixsoColors.Color_Error_Error_30,
    onErrorContainer = PixsoColors.Color_Error_Error_90,
    background = PixsoColors.Color_Neutral_Neutral_10,
    onBackground = PixsoColors.Color_Neutral_Neutral_90,
    surface = PixsoColors.Color_Neutral_Neutral_10,
    onSurface = PixsoColors.Color_Neutral_Neutral_90,
    surfaceVariant = PixsoColors.Color_Neutral_Neutral_25,
    onSurfaceVariant = PixsoColors.Color_Neutral_Neutral_80,
    outline = PixsoColors.Color_Neutral_Neutral_50,
    outlineVariant = PixsoColors.Color_Neutral_Neutral_30
)

private val LightColorScheme = lightColorScheme(
    primary = PixsoColors.Color_Primary_Primary_40,
    onPrimary = PixsoColors.Color_Neutral_Neutral_100,
    primaryContainer = PixsoColors.Color_Primary_Primary_90,
    onPrimaryContainer = PixsoColors.Color_Primary_Primary_10,
    secondary = PixsoColors.Color_Secondary_Secondary_40,
    onSecondary = PixsoColors.Color_Neutral_Neutral_100,
    secondaryContainer = PixsoColors.Color_Secondary_Secondary_90,
    onSecondaryContainer = PixsoColors.Color_Secondary_Secondary_10,
    tertiary = PixsoColors.Color_Secondary_Secondary_40,
    onTertiary = PixsoColors.Color_Neutral_Neutral_100,
    tertiaryContainer = PixsoColors.Color_Secondary_Secondary_90,
    onTertiaryContainer = PixsoColors.Color_Secondary_Secondary_10,
    error = PixsoColors.Color_Error_Error_40,
    onError = PixsoColors.Color_Neutral_Neutral_100,
    errorContainer = PixsoColors.Color_Error_Error_90,
    onErrorContainer = PixsoColors.Color_Error_Error_10,
    background = PixsoColors.Color_Bg_bg_canvas,
    onBackground = PixsoColors.Color_Neutral_Neutral_10,
    surface = PixsoColors.Color_Bg_bg_surface,
    onSurface = PixsoColors.Color_Neutral_Neutral_10,
    surfaceVariant = PixsoColors.Color_Neutral_Neutral_90,
    onSurfaceVariant = PixsoColors.Color_Neutral_Neutral_30,
    outline = PixsoColors.Color_Neutral_Neutral_50,
    outlineVariant = PixsoColors.Color_Neutral_Neutral_80
)

@Composable
fun SynapseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use Pixso colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PixsoTypography,
        content = content
    )
}
