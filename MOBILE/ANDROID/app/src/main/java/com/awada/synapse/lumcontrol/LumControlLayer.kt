package com.awada.synapse.lumcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.roundToInt

private val DRAG_HANDLE_HEIGHT = 48.dp
private val SLIDER_ITEM_HEIGHT = 60.dp
private val SLIDER_SPACING = 4.dp
private val SLIDER_TO_BUTTONS_GAP = 12.dp

enum class LumControlState { Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LumControlLayer(
    isVisible: Boolean = true,
    sliders: List<String> = emptyList(),
    bottomPadding: Int = 178,
    autoExpandOnShow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var colorValue by remember { mutableFloatStateOf(0f) }
    var saturationValue by remember { mutableFloatStateOf(50f) }
    var temperatureValue by remember { mutableFloatStateOf(4000f) }
    var brightnessValue by remember { mutableFloatStateOf(50f) }

    val sliderCount = sliders.size
    val sliderSectionPx = with(density) {
        if (sliderCount > 0) {
            (SLIDER_ITEM_HEIGHT * sliderCount + SLIDER_SPACING * (sliderCount - 1) + SLIDER_TO_BUTTONS_GAP).toPx()
        } else 0f
    }

    val anchoredDraggableState = remember(sliderCount, isVisible, autoExpandOnShow) {
        AnchoredDraggableState(
            initialValue = if (autoExpandOnShow && isVisible && sliderCount > 0) {
                LumControlState.Expanded
            } else {
                LumControlState.Collapsed
            },
            anchors = DraggableAnchors {
                LumControlState.Collapsed at sliderSectionPx
                LumControlState.Expanded at 0f
            },
            positionalThreshold = { distance: Float -> distance * 0.3f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = tween(
                durationMillis = 550,
                easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
            ),
            decayAnimationSpec = exponentialDecay(frictionMultiplier = 2f)
        )
    }

    val currentOffset = try {
        anchoredDraggableState.requireOffset()
    } catch (_: IllegalStateException) {
        sliderSectionPx
    }

    val revealPx = (sliderSectionPx - currentOffset).coerceAtLeast(0f)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp)
                        .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_L))
                        .background(color = PixsoColors.Color_State_tertiary_variant)
                        .padding(18.dp)
                ) {
                    RevealSlidersAboveButtons(
                        sliders = sliders,
                        revealPx = revealPx,
                        colorValue = colorValue,
                        onColorValueChange = { colorValue = it },
                        saturationValue = saturationValue,
                        onSaturationValueChange = { saturationValue = it },
                        temperatureValue = temperatureValue,
                        onTemperatureValueChange = { temperatureValue = it },
                        brightnessValue = brightnessValue,
                        onBrightnessValueChange = { brightnessValue = it }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DRAG_HANDLE_HEIGHT)
                    .align(Alignment.TopCenter)
                    .then(
                        if (sliders.isNotEmpty()) {
                            Modifier.anchoredDraggable(
                                state = anchoredDraggableState,
                                orientation = Orientation.Vertical
                            )
                        } else Modifier
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_Full))
                        .background(PixsoColors.Color_State_tertiary_variant)
                )
            }
        }
    }
}

@Composable
private fun RevealSlidersAboveButtons(
    sliders: List<String>,
    revealPx: Float,
    colorValue: Float,
    onColorValueChange: (Float) -> Unit,
    saturationValue: Float,
    onSaturationValueChange: (Float) -> Unit,
    temperatureValue: Float,
    onTemperatureValueChange: (Float) -> Unit,
    brightnessValue: Float,
    onBrightnessValueChange: (Float) -> Unit
) {
    SubcomposeLayout(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                clipRect(
                    left = -10_000f,
                    top = 0f,
                    right = size.width + 10_000f,
                    bottom = size.height
                ) {
                    this@drawWithContent.drawContent()
                }
            }
    ) { constraints ->
        val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val gapPx = SLIDER_TO_BUTTONS_GAP.roundToPx()

        val dynamicSaturationColor = run {
            val maxIdx = (colorSpectrumColors.size - 1).coerceAtLeast(0)
            val idx = ((colorValue.coerceIn(0f, 100f) / 100f) * maxIdx).roundToInt().coerceIn(0, maxIdx)
            colorSpectrumColors[idx]
        }

        val slidersPlaceable = if (sliders.isNotEmpty()) {
            subcompose("sliders") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SLIDER_SPACING)
                ) {
                    sliders.forEach { sliderType ->
                        when (sliderType) {
                            "Color" -> ColorSlider(
                                value = colorValue,
                                onValueChange = onColorValueChange
                            )
                            "Saturation" -> SaturationSlider(
                                value = saturationValue,
                                onValueChange = onSaturationValueChange,
                                dynamicColor = dynamicSaturationColor
                            )
                            "Temperature" -> TemperatureSlider(
                                value = temperatureValue,
                                onValueChange = onTemperatureValueChange
                            )
                            "Brightness" -> BrightnessSlider(
                                value = brightnessValue,
                                onValueChange = onBrightnessValueChange
                            )
                        }
                    }
                }
            }.first().measure(loose)
        } else null

        val buttonsPlaceable = subcompose("buttons") {
            QuickButtonsRow(
                buttons = defaultQuickButtons,
                onButtonSelected = { _ -> }
            )
        }.first().measure(loose)

        val slidersHeight = slidersPlaceable?.height ?: 0
        val maxReveal = if (slidersPlaceable != null) slidersHeight + gapPx else 0
        val clampedReveal = revealPx.coerceIn(0f, maxReveal.toFloat())
        val layoutHeight = (buttonsPlaceable.height + clampedReveal).toInt()

        layout(constraints.maxWidth, layoutHeight) {
            val buttonsY = layoutHeight - buttonsPlaceable.height
            buttonsPlaceable.placeRelative(0, buttonsY)

            if (slidersPlaceable != null) {
                val slidersY = buttonsY - gapPx - slidersPlaceable.height
                slidersPlaceable.placeRelative(0, slidersY)
            }
        }
    }
}
