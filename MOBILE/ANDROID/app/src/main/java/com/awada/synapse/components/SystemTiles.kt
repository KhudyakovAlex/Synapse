package com.awada.synapse.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun PresSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    SystemIconTile(
        title = "Сенсор\nнажатия",
        iconResId = R.drawable.system_901_sensorp,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun BrightSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    SystemIconTile(
        title = "Сенсор\nяркости",
        iconResId = R.drawable.system_902_sensorb,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ButtonPanel(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    SystemIconTile(
        title = "Панель\nкнопок",
        iconResId = R.drawable.system_903_buttons,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun SystemIconTile(
    title: String,
    iconResId: Int,
    modifier: Modifier,
    iconSize: Dp,
    enabled: Boolean,
    onClick: (() -> Unit)?
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showPressed by remember { mutableStateOf(false) }

    val instantPressModifier = if (onClick != null && enabled) {
        Modifier.pointerInput(onClick, enabled) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                showPressed = true

                val start = down.position
                var cancelledByMove = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                    if (!change.pressed) break

                    if (!cancelledByMove) {
                        val dist = (change.position - start).getDistance()
                        if (dist > viewConfiguration.touchSlop) {
                            cancelledByMove = true
                            showPressed = false
                        }
                    }
                }

                showPressed = false
            }
        }
    } else {
        Modifier
    }
    val shadowColor = Color.Black.copy(alpha = 1f / 3f)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .widthIn(min = iconSize)
            .then(instantPressModifier)
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .background(
                    if (onClick != null && enabled && showPressed) {
                        PixsoColors.Color_State_secondary_pressed
                    } else {
                        PixsoColors.Color_Bg_bg_surface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(iconSize * 0.53f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = LabelMedium.copy(lineHeight = LabelMedium.lineHeight * 0.8f),
            color = PixsoColors.Color_Text_text_1_level,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

