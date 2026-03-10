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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import com.awada.synapse.R
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun PresSensor(
    title: String = "Сенсор\nнажатия",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_901_sensorp,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = false,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
fun PresSensor(
    title: String = "Сенсор\nнажатия",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    forcePressed: Boolean,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_901_sensorp,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = forcePressed,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
fun BrightSensor(
    title: String = "Сенсор\nяркости",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    forceSecondaryPressed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_902_sensorb,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = false,
        forceSecondaryPressed = forceSecondaryPressed,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
fun BrightSensor(
    title: String = "Сенсор\nяркости",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    forcePressed: Boolean,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    forceSecondaryPressed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_902_sensorb,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = forcePressed,
        forceSecondaryPressed = forceSecondaryPressed,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
fun ButtonPanel(
    title: String = "Панель\nкнопок",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_903_buttons,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = false,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
fun ButtonPanel(
    title: String = "Панель\nкнопок",
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    forcePressed: Boolean,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
    onClick: (() -> Unit)? = null
) {
    SystemIconTileInternal(
        title = title,
        iconResId = R.drawable.system_903_buttons,
        modifier = modifier,
        iconSize = iconSize,
        enabled = enabled,
        forcePressed = forcePressed,
        onCircleBoundsInRoot = onCircleBoundsInRoot,
        circleAlpha = circleAlpha,
        onClick = onClick
    )
}

@Composable
private fun SystemIconTileInternal(
    title: String,
    iconResId: Int,
    modifier: Modifier,
    iconSize: Dp,
    enabled: Boolean,
    forcePressed: Boolean,
    forceSecondaryPressed: Boolean = false,
    onCircleBoundsInRoot: ((Rect) -> Unit)? = null,
    circleAlpha: Float = 1f,
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
    val rawCircleBg = if (onClick != null && enabled && showPressed) {
        PixsoColors.Color_State_secondary_pressed
    } else if (forceSecondaryPressed) {
        PixsoColors.Color_State_secondary_pressed
    } else if (forcePressed) {
        PixsoColors.Color_State_primary_pressed
    } else {
        PixsoColors.Color_Bg_bg_surface
    }
    val circleBg = rawCircleBg.copy(alpha = rawCircleBg.alpha * circleAlpha.coerceIn(0f, 1f))

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
                .onGloballyPositioned { coords ->
                    onCircleBoundsInRoot?.invoke(coords.boundsInRoot())
                }
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(iconSize * 0.53f),
                colorFilter = if (forcePressed) {
                    ColorFilter.tint(PixsoColors.Color_State_on_primary)
                } else {
                    null
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatTileTitle(title),
            style = LabelMedium.copy(lineHeight = LabelMedium.lineHeight * 0.8f),
            color = PixsoColors.Color_Text_text_1_level,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatTileTitle(text: String): String {
    val maxLength = 24
    var result = text.take(maxLength)
    
    val lines = result.split("\n")
    if (lines.size >= 2) {
        return result
    }
    
    val words = result.split(" ")
    if (words.isNotEmpty() && words[0].length > 12) {
        return result.split("").take(13).joinToString("").trim() + "\n" + 
               result.drop(12).take(12)
    }
    
    var currentLine = ""
    val formattedLines = mutableListOf<String>()
    
    for (word in words) {
        if ((currentLine + word).length <= 12) {
            currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        } else {
            if (currentLine.isNotEmpty()) formattedLines.add(currentLine)
            currentLine = word
        }
    }
    if (currentLine.isNotEmpty()) formattedLines.add(currentLine)
    
    return formattedLines.take(2).joinToString("\n")
}

