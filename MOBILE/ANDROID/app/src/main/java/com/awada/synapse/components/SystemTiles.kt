package com.awada.synapse.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun PresSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp
) {
    SystemIconTile(
        title = "Сенсор\nнажатия",
        iconResId = R.drawable.system_901_sensorp,
        modifier = modifier,
        iconSize = iconSize
    )
}

@Composable
fun BraightSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp
) {
    SystemIconTile(
        title = "Сенсор\nяркости",
        iconResId = R.drawable.system_902_sensorb,
        modifier = modifier,
        iconSize = iconSize
    )
}

@Composable
fun BrightSensor(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp
) {
    BraightSensor(
        modifier = modifier,
        iconSize = iconSize
    )
}

@Composable
fun ButtonPanel(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp
) {
    SystemIconTile(
        title = "Панель\nкнопок",
        iconResId = R.drawable.system_903_buttons,
        modifier = modifier,
        iconSize = iconSize
    )
}

@Composable
private fun SystemIconTile(
    title: String,
    iconResId: Int,
    modifier: Modifier,
    iconSize: Dp
) {
    Column(
        modifier = modifier.widthIn(min = iconSize),
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

