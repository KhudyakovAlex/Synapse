package com.awada.synapse.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.awada.synapse.db.LuminaireTypeEntity
import com.awada.synapse.lumcontrol.colorSpectrumColors
import com.awada.synapse.lumcontrol.interpolateColor

data class LumVisualState(
    val brightnessPercent: Int,
    val statusDotColor: Color?,
)

fun resolveLumVisualState(
    typeId: Int,
    brightnessPercent: Int,
    hue: Int,
    saturation: Int,
    temperature: Int,
): LumVisualState {
    val resolvedBrightness = when (typeId) {
        LuminaireTypeEntity.TYPE_ON_OFF -> {
            if (brightnessPercent.coerceIn(0, 100) > 0) 100 else 0
        }
        else -> brightnessPercent.coerceIn(0, 100)
    }

    val dotColor = when (typeId) {
        LuminaireTypeEntity.TYPE_RGB -> {
            val huePosition = (hue.coerceIn(0, 100) / 100f)
            val hueColor = interpolateColor(colorSpectrumColors, huePosition)
            val saturationFraction = saturation.coerceIn(0, 100) / 100f
            lerp(Color(0xFFB9B9B9), hueColor, saturationFraction)
        }
        LuminaireTypeEntity.TYPE_TW -> {
            val temperatureColors = listOf(
                Color(0xFFFFC47B),
                Color(0xFFFFFFFF),
                Color(0xFF92E4FF)
            )
            val normalizedTemperature = (((if (temperature > 0) temperature else 3500) - 3000f) / 2000f)
                .coerceIn(0f, 1f)
            interpolateColor(temperatureColors, normalizedTemperature)
        }
        else -> null
    }

    return LumVisualState(
        brightnessPercent = resolvedBrightness,
        statusDotColor = dotColor
    )
}
