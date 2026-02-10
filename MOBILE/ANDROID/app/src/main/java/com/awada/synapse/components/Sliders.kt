package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import com.awada.synapse.ui.theme.LabelLarge

/**
 * Track rendering mode for sliders
 */
sealed class TrackMode {
    data class Gradient(val brush: Brush, val colors: List<Color>) : TrackMode()
    data class DualColor(val leftColor: Color, val rightColor: Color) : TrackMode()
    data class DynamicGradient(val staticColor: Color, val dynamicColor: Color) : TrackMode()
}

/**
 * Interpolate color in gradient based on normalized position (0f to 1f)
 */
fun interpolateColor(colors: List<Color>, position: Float): Color {
    if (colors.isEmpty()) return Color.Black
    if (colors.size == 1) return colors[0]
    
    val clampedPos = position.coerceIn(0f, 1f)
    val scaledPos = clampedPos * (colors.size - 1)
    val index = scaledPos.toInt().coerceIn(0, colors.size - 2)
    val fraction = scaledPos - index
    
    val color1 = colors[index]
    val color2 = colors[index + 1]
    
    return Color(
        red = color1.red + (color2.red - color1.red) * fraction,
        green = color1.green + (color2.green - color1.green) * fraction,
        blue = color1.blue + (color2.blue - color1.blue) * fraction,
        alpha = color1.alpha + (color2.alpha - color1.alpha) * fraction
    )
}

/**
 * Base Slider component with Canvas for custom gradient rendering
 */
@Composable
fun BaseSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    thumbColor: Color,
    activeTrackColor: Color,
    trackMode: TrackMode,
    modifier: Modifier = Modifier,
    label: String = "",
    @DrawableRes icon: Int? = null,
    minValue: Float = 0f,
    maxValue: Float = 100f,
    showValue: Boolean = true,
    enabled: Boolean = true
) {
    val valueText = if (showValue) "${value.toInt()} %" else ""
    val trackHeight = 16.dp
    val thumbRadius = 16.dp
    val sliderHeight = 44.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header with label and value
        if (label.isNotEmpty() || showValue) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label with icon
                if (label.isNotEmpty()) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = Color(0xFFF2FCFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = LabelLarge.copy(
                                color = Color(0xFFF2FCFF)
                            )
                        )
                    }
                }

                // Value (right-aligned)
                if (showValue) {
                    Text(
                        text = valueText,
                        style = LabelLarge.copy(
                            color = Color(0xFFF2FCFF)
                        )
                    )
                }
            }
        }

        // Canvas slider
        var canvasWidth = 0f
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(sliderHeight)
                .pointerInput(Unit) {
                    if (enabled) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val thumbRadiusPx = thumbRadius.toPx()
                            val padding = thumbRadiusPx / 2
                            val availableWidth = canvasWidth - 2 * padding
                            
                            val xPosition = (down.position.x - padding).coerceIn(0f, availableWidth)
                            val newValue = minValue + (xPosition / availableWidth) * (maxValue - minValue)
                            onValueChange(newValue.coerceIn(minValue, maxValue))
                            
                            horizontalDrag(down.id) { change ->
                                val newX = (change.position.x - padding).coerceIn(0f, availableWidth)
                                val dragValue = minValue + (newX / availableWidth) * (maxValue - minValue)
                                onValueChange(dragValue.coerceIn(minValue, maxValue))
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            canvasWidth = size.width
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val padding = thumbRadiusPx / 2
            val availableWidth = canvasWidth - 2 * padding
            val trackCenterY = size.height / 2

            // Calculate thumb position with padding
            val range = maxValue - minValue
            val normalizedValue = if (range != 0f) (value - minValue) / range else 0f
            val thumbX = padding + normalizedValue * availableWidth
            val thumbY = trackCenterY

            // Draw background track based on mode
            when (trackMode) {
                is TrackMode.Gradient -> {
                    // Draw full gradient track
                    drawRoundRect(
                        brush = trackMode.brush,
                        topLeft = Offset(0f, trackCenterY - trackHeightPx / 2),
                        size = Size(canvasWidth, trackHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2)
                    )
                }
                is TrackMode.DualColor -> {
                    // Draw left part (left color)
                    drawRoundRect(
                        color = trackMode.leftColor,
                        topLeft = Offset(0f, trackCenterY - trackHeightPx / 2),
                        size = Size(thumbX, trackHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2)
                    )
                    // Draw right part (right color)
                    drawRoundRect(
                        color = trackMode.rightColor,
                        topLeft = Offset(thumbX, trackCenterY - trackHeightPx / 2),
                        size = Size(canvasWidth - thumbX, trackHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2)
                    )
                }
                is TrackMode.DynamicGradient -> {
                    // Draw gradient from static to dynamic color
                    val dynamicGradientBrush = Brush.linearGradient(
                        colors = listOf(trackMode.staticColor, trackMode.dynamicColor),
                        start = Offset(0f, trackCenterY),
                        end = Offset(canvasWidth, trackCenterY)
                    )
                    drawRoundRect(
                        brush = dynamicGradientBrush,
                        topLeft = Offset(0f, trackCenterY - trackHeightPx / 2),
                        size = Size(canvasWidth, trackHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2)
                    )
                }
            }

            // Calculate thumb color based on track mode
            val thumbActualColor = when {
                !enabled -> Color(0xFFACAAAA)
                trackMode is TrackMode.Gradient -> interpolateColor(trackMode.colors, normalizedValue)
                trackMode is TrackMode.DualColor -> trackMode.leftColor
                trackMode is TrackMode.DynamicGradient -> interpolateColor(
                    listOf(trackMode.staticColor, trackMode.dynamicColor),
                    normalizedValue
                )
                else -> thumbColor
            }

            // Draw thumb
            drawCircle(
                color = thumbActualColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, thumbY)
            )

            // Draw thumb stroke (black border)
            drawCircle(
                color = Color.Black,
                radius = thumbRadiusPx,
                center = Offset(thumbX, thumbY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
        }
    }
}
