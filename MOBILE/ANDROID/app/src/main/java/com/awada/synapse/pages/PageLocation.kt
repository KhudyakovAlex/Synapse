package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BaseSlider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.awada.synapse.R
import com.awada.synapse.components.TrackMode
import com.awada.synapse.components.interpolateColor
import com.awada.synapse.components.ColorSlider
import com.awada.synapse.components.SaturationSlider
import com.awada.synapse.components.TemperatureSlider
import com.awada.synapse.components.BrightnessSlider
import com.awada.synapse.components.QuickButtonsRow

/**
 * Predefined gradients for slider tracks
 */
object SliderGradients {
    val temperatureGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFC47B),  // warm
            Color(0xFFFFFFFF),  // white
            Color(0xFF92E4FF)   // cool
        )
    )
    
    val spectrumGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF0000),  // красный
            Color(0xFFFF7F00),  // оранжевый
            Color(0xFFFFFF00),  // жёлтый
            Color(0xFF00FF00),  // зелёный
            Color(0xFF00FFFF),  // голубой
            Color(0xFF0000FF),  // синий
            Color(0xFF8B00FF),  // фиолетовый
            Color(0xFFFF0000)   // красный
        )
    )
}

/**
 * Initial page for Locations.
 * Always placed below AI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onSettingsLumClick: () -> Unit,
    onSettingsSensorPressClick: () -> Unit,
    onSettingsSensorBrightClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    PageContainer(
        title = "Локации",
        onSettingsClick = onSettingsClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // All sliders on one card
            val (colorValue, setColorValue) = remember { mutableStateOf(50f) }
            val (saturationValue, setSaturationValue) = remember { mutableStateOf(50f) }
            val (temperatureValue, setTemperatureValue) = remember { mutableStateOf(4000f) }
            val (brightnessValue, setBrightnessValue) = remember { mutableStateOf(50f) }
            
            val colorSpectrumColors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF7F00),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00FFFF),
                Color(0xFF0000FF),
                Color(0xFF8B00FF),
                Color(0xFFFF0000)
            )
            val currentColor = interpolateColor(colorSpectrumColors, colorValue / 100f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorSlider(
                        value = colorValue,
                        onValueChange = setColorValue
                    )
                    
                    SaturationSlider(
                        value = saturationValue,
                        onValueChange = setSaturationValue,
                        dynamicColor = currentColor
                    )
                    
                    TemperatureSlider(
                        value = temperatureValue,
                        onValueChange = setTemperatureValue
                    )
                    
                    BrightnessSlider(
                        value = brightnessValue,
                        onValueChange = setBrightnessValue
                    )
                    
                    QuickButtonsRow(
                        onButtonSelected = { button ->
                            // Handle button selection
                        }
                    )
                }
            }
            Button(
                onClick = onSettingsLumClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Настройки светильника")
            }

            Button(
                onClick = onSettingsSensorPressClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Настройки датчика нажатия")
            }

            Button(
                onClick = onSettingsSensorBrightClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Настройки датчика яркости")
            }
        }
    }
}
