package com.awada.synapse.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors
import androidx.compose.runtime.remember

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
fun BraightSensor(
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
fun BrightSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    BraightSensor(
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
    val isPressed by interactionSource.collectIsPressedAsState()
    // keep tracking for future pressed/active visuals
    @Suppress("UNUSED_VARIABLE")
    val _ignore = isPressed

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
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(PixsoColors.Color_Bg_bg_surface),
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

