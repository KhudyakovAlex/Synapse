package com.awada.synapse.lumcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.delay

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
    colorValue: Float? = null,
    onColorValueChange: ((Float) -> Unit)? = null,
    saturationValue: Float? = null,
    onSaturationValueChange: ((Float) -> Unit)? = null,
    temperatureValue: Float? = null,
    onTemperatureValueChange: ((Float) -> Unit)? = null,
    brightnessValue: Float? = null,
    onBrightnessValueChange: ((Float) -> Unit)? = null,
    brightnessEnabled: Boolean = true,
    onSceneSelected: ((Int) -> Unit)? = null,
    onSceneLongSelected: ((Int) -> Unit)? = null,
    bottomPadding: Int = 178,
    autoExpandOnShow: Boolean = false,
    stateKey: Any? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var localColorValue by remember { mutableFloatStateOf(0f) }
    var localSaturationValue by remember { mutableFloatStateOf(50f) }
    var localTemperatureValue by remember { mutableFloatStateOf(4000f) }
    var localBrightnessValue by remember { mutableFloatStateOf(50f) }
    var controlledColorValue by remember(stateKey) { mutableFloatStateOf(colorValue ?: 0f) }
    var controlledSaturationValue by remember(stateKey) { mutableFloatStateOf(saturationValue ?: 50f) }
    var controlledTemperatureValue by remember(stateKey) { mutableFloatStateOf(temperatureValue ?: 4000f) }
    var controlledBrightnessValue by remember(stateKey) { mutableFloatStateOf(brightnessValue ?: 0f) }

    LaunchedEffect(colorValue, stateKey) {
        if (colorValue != null) {
            controlledColorValue = colorValue.coerceIn(0f, 100f)
        }
    }
    LaunchedEffect(saturationValue, stateKey) {
        if (saturationValue != null) {
            controlledSaturationValue = saturationValue.coerceIn(0f, 100f)
        }
    }
    LaunchedEffect(temperatureValue, stateKey) {
        if (temperatureValue != null) {
            controlledTemperatureValue = temperatureValue.coerceIn(3000f, 5000f)
        }
    }
    LaunchedEffect(brightnessValue, stateKey) {
        if (brightnessValue != null) {
            controlledBrightnessValue = brightnessValue.coerceIn(0f, 100f)
        }
    }

    val resolvedColorValue = if (colorValue != null) {
        controlledColorValue
    } else {
        localColorValue
    }
    val resolvedSaturationValue = if (saturationValue != null) {
        controlledSaturationValue
    } else {
        localSaturationValue
    }
    val resolvedTemperatureValue = if (temperatureValue != null) {
        controlledTemperatureValue
    } else {
        localTemperatureValue
    }
    val resolvedBrightnessValue = if (brightnessValue != null) {
        controlledBrightnessValue
    } else {
        localBrightnessValue
    }

    val sliderCount = sliders.size
    val sliderSectionPx = with(density) {
        if (sliderCount > 0) {
            (SLIDER_ITEM_HEIGHT * sliderCount + SLIDER_SPACING * (sliderCount - 1) + SLIDER_TO_BUTTONS_GAP).toPx()
        } else 0f
    }

    val anchoredDraggableState = remember(sliderCount, stateKey, isVisible, autoExpandOnShow) {
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

    val shouldEntryAnimateOpen = autoExpandOnShow && isVisible && sliderCount > 0
    var entryAnimDone by remember(stateKey, shouldEntryAnimateOpen) { mutableStateOf(false) }
    val entryRevealPx by animateFloatAsState(
        targetValue = if (shouldEntryAnimateOpen && !entryAnimDone) sliderSectionPx else 0f,
        animationSpec = tween(
            durationMillis = 550,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        ),
        label = "LumControlEntryReveal"
    )
    androidx.compose.runtime.LaunchedEffect(shouldEntryAnimateOpen, entryAnimDone, stateKey) {
        if (shouldEntryAnimateOpen && !entryAnimDone) {
            delay(560L)
            entryAnimDone = true
        }
    }

    val revealForUi = if (shouldEntryAnimateOpen && !entryAnimDone) entryRevealPx else revealPx

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
                        revealPx = revealForUi,
                        colorValue = resolvedColorValue,
                        onColorValueChange = { value ->
                            val clampedValue = value.coerceIn(0f, 100f)
                            if (colorValue != null && onColorValueChange != null) {
                                controlledColorValue = clampedValue
                                onColorValueChange(clampedValue)
                            } else {
                                localColorValue = clampedValue
                            }
                        },
                        saturationValue = resolvedSaturationValue,
                        onSaturationValueChange = { value ->
                            val clampedValue = value.coerceIn(0f, 100f)
                            if (saturationValue != null && onSaturationValueChange != null) {
                                controlledSaturationValue = clampedValue
                                onSaturationValueChange(clampedValue)
                            } else {
                                localSaturationValue = clampedValue
                            }
                        },
                        temperatureValue = resolvedTemperatureValue,
                        onTemperatureValueChange = { value ->
                            val clampedValue = value.coerceIn(3000f, 5000f)
                            if (temperatureValue != null && onTemperatureValueChange != null) {
                                controlledTemperatureValue = clampedValue
                                onTemperatureValueChange(clampedValue)
                            } else {
                                localTemperatureValue = clampedValue
                            }
                        },
                        brightnessValue = resolvedBrightnessValue,
                        brightnessEnabled = brightnessEnabled,
                        onBrightnessValueChange = { value ->
                            val clampedValue = value.coerceIn(0f, 100f)
                            if (brightnessValue != null && onBrightnessValueChange != null) {
                                controlledBrightnessValue = clampedValue
                                onBrightnessValueChange(clampedValue)
                            } else {
                                localBrightnessValue = clampedValue
                            }
                        },
                        onSceneSelected = onSceneSelected,
                        onSceneLongSelected = onSceneLongSelected
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
                            if (!shouldEntryAnimateOpen || entryAnimDone) {
                                Modifier.anchoredDraggable(
                                    state = anchoredDraggableState,
                                    orientation = Orientation.Vertical
                                )
                            } else {
                                Modifier
                            }
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
    brightnessEnabled: Boolean,
    onBrightnessValueChange: (Float) -> Unit,
    onSceneSelected: ((Int) -> Unit)?,
    onSceneLongSelected: ((Int) -> Unit)?
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
                                onValueChange = onBrightnessValueChange,
                                enabled = brightnessEnabled
                            )
                        }
                    }
                }
            }.first().measure(loose)
        } else null

        val buttonsPlaceable = subcompose("buttons") {
            QuickButtonsRow(
                buttons = defaultQuickButtons,
                onButtonSelected = { button -> onSceneSelected?.invoke(button.id) },
                onButtonLongSelected = { button -> onSceneLongSelected?.invoke(button.id) }
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
