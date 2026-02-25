package com.awada.synapse.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors

@Composable
fun Switch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isChecked) 24.dp else 0.dp, // 56 - 2*4 - 24
        label = "switch_thumb_offset"
    )

    val trackColor = if (isChecked) {
        PixsoColors.Color_State_primary
    } else {
        PixsoColors.Color_State_on_disabled
    }

    val thumbColor = PixsoColors.Color_State_secondary

    Box(
        modifier = modifier
            .size(width = 56.dp, height = 32.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(thumbColor)
                .border(
                    width = 1.dp,
                    color = PixsoColors.Color_State_pressed_shade_4,
                    shape = CircleShape
                )
        )
    }
}

