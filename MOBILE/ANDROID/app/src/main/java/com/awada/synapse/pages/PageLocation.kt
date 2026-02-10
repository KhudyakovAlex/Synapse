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
            // Test BaseSlider with Gradient
            val (sliderValue, setSliderValue) = remember { mutableStateOf(50f) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                BaseSlider(
                    value = sliderValue,
                    onValueChange = setSliderValue,
                    thumbColor = Color(0xFF6BBECF),
                    activeTrackColor = Color(0xFFF2FCFF),
                    trackMode = TrackMode.Gradient(
                        brush = SliderGradients.temperatureGradient,
                        colors = listOf(
                            Color(0xFFFFC47B),
                            Color(0xFFFFFFFF),
                            Color(0xFF92E4FF)
                        )
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Тест слайдер",
                    showValue = true
                )
            }
            
            // Test BaseSlider with DualColor
            val (sliderValue2, setSliderValue2) = remember { mutableStateOf(30f) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                BaseSlider(
                    value = sliderValue2,
                    onValueChange = setSliderValue2,
                    thumbColor = Color(0xFF6BBECF),
                    activeTrackColor = Color(0xFFF2FCFF),
                    trackMode = TrackMode.DualColor(
                        leftColor = Color(0xFFF2FDFF),  // Color_State_on_tertiary
                        rightColor = Color(0xFF414C4F)
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Двухцветный",
                    showValue = true
                )
            }
            
            // Test BaseSlider with Spectrum Gradient
            val (sliderValue3, setSliderValue3) = remember { mutableStateOf(70f) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                BaseSlider(
                    value = sliderValue3,
                    onValueChange = setSliderValue3,
                    thumbColor = Color(0xFF6BBECF),
                    activeTrackColor = Color(0xFFF2FCFF),
                    trackMode = TrackMode.Gradient(
                        brush = SliderGradients.spectrumGradient,
                        colors = listOf(
                            Color(0xFFFF0000),
                            Color(0xFFFF7F00),
                            Color(0xFFFFFF00),
                            Color(0xFF00FF00),
                            Color(0xFF00FFFF),
                            Color(0xFF0000FF),
                            Color(0xFF8B00FF),
                            Color(0xFFFF0000)
                        )
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Спектр",
                    showValue = true
                )
            }
            
            // Dynamic Gradient Sliders - linked
            val (hueValue, setHueValue) = remember { mutableStateOf(50f) }
            val spectrumColors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF7F00),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00FFFF),
                Color(0xFF0000FF),
                Color(0xFF8B00FF),
                Color(0xFFFF0000)
            )
            val currentHueColor = interpolateColor(spectrumColors, hueValue / 100f)
            
            val (saturationValue, setSaturationValue) = remember { mutableStateOf(50f) }
            
            // Hue slider (spectrum)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                BaseSlider(
                    value = hueValue,
                    onValueChange = setHueValue,
                    thumbColor = Color(0xFF6BBECF),
                    activeTrackColor = Color(0xFFF2FCFF),
                    trackMode = TrackMode.Gradient(
                        brush = SliderGradients.spectrumGradient,
                        colors = spectrumColors
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Оттенок",
                    showValue = true
                )
            }
            
            // Saturation slider (dynamic gradient based on hue)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1B1C1C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                BaseSlider(
                    value = saturationValue,
                    onValueChange = setSaturationValue,
                    thumbColor = Color(0xFF6BBECF),
                    activeTrackColor = Color(0xFFF2FCFF),
                    trackMode = TrackMode.DynamicGradient(
                        staticColor = Color(0xFF9F9F9F),
                        dynamicColor = currentHueColor
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Насыщенность",
                    showValue = true
                )
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
