package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awada.synapse.ui.theme.IBMPlexSansFamily
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PanelButtonVariant {
    Def,
    Active,
}

@Composable
fun PanelButton(
    text: String,
    variant: PanelButtonVariant,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    tapToActiveHoldMs: Long? = null,
    onClick: (() -> Unit)? = null,
) {
    val scale = size.value / 72f
    val innerSize = 64.dp * scale
    val dotSize = 4.5.dp * scale
    val dotLeft = 16.875.dp * scale
    val dotTop = 33.75.dp * scale

    // Requirement: number is horizontally centered over the dot below it.
    // Dot stays at Def position for all variants; only its color changes.
    val textTop = 10.25.dp * scale

    val scope = rememberCoroutineScope()
    var forceActive by remember { mutableStateOf(false) }
    var releaseJob by remember { mutableStateOf<Job?>(null) }
    val effectiveVariant = if (tapToActiveHoldMs != null && forceActive) {
        PanelButtonVariant.Active
    } else {
        variant
    }

    val backgroundColor = when (effectiveVariant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_State_tertiary
    }
    val textColor = when (effectiveVariant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_on_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_Text_text_inverse
    }
    val dotColor = when (effectiveVariant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_on_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_Bg_bg_elevated
    }

    val interactionSource = remember { MutableInteractionSource() }
    val enabled = onClick != null
    val pressToActiveModifier = if (enabled && tapToActiveHoldMs != null) {
        Modifier.pointerInput(tapToActiveHoldMs) {
            detectTapGestures(
                onPress = {
                    releaseJob?.cancel()
                    forceActive = true
                    val released = tryAwaitRelease()
                    if (released) {
                        releaseJob = scope.launch {
                            delay(tapToActiveHoldMs)
                            forceActive = false
                        }
                    } else {
                        forceActive = false
                    }
                },
                onTap = { onClick?.invoke() },
            )
        }
    } else if (enabled) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onClick?.invoke() },
        )
    } else {
        Modifier
    }

    val density = LocalDensity.current
    var textWidthPx by remember(text) { mutableIntStateOf(0) }
    val textOffsetXpx = with(density) {
        val dotCenterXpx = (dotLeft + dotSize / 2f).toPx()
        (dotCenterXpx - textWidthPx / 2f).roundToInt()
    }
    val textOffsetYpx = with(density) { textTop.toPx().roundToInt() }

    Box(
        modifier = modifier
            .size(size)
            .then(pressToActiveModifier),
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .align(Alignment.Center)
                .background(backgroundColor, RoundedCornerShape(2.dp)),
        ) {
        }

        Text(
            text = text,
            style = TextStyle(
                fontFamily = IBMPlexSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp * scale,
                lineHeight = 20.sp * scale,
                letterSpacing = 0.5.sp * scale,
                textAlign = TextAlign.Center,
            ),
            color = textColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(textOffsetXpx, textOffsetYpx) },
            onTextLayout = { textWidthPx = it.size.width },
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(x = dotLeft, y = dotTop)
                .background(dotColor, CircleShape)
                .align(Alignment.TopStart),
        )
    }
}

