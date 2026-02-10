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
                        rightColor = Color(0xFF9F9F9F)
                    ),
                    icon = R.drawable.ic_settings,
                    label = "Двухцветный",
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
