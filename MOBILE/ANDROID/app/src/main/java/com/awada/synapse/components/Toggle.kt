package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors

@Composable
fun Toggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @DrawableRes iconOn: Int,
    @DrawableRes iconOff: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Track colors
    val trackColor = if (isChecked && enabled) {
        PixsoColors.Color_Bg_bg_shade_primary
    } else {
        PixsoColors.Color_Bg_bg_shade_disabled
    }

    // Thumb colors
    val thumbColor = when {
        !enabled -> PixsoColors.Color_State_disabled
        isChecked -> PixsoColors.Color_State_secondary
        else -> PixsoColors.Color_State_tertiary
    }

    // Icon colors
    val iconColor = when {
        !enabled -> PixsoColors.Color_State_on_disabled
        isChecked -> PixsoColors.Color_State_on_secondary
        else -> PixsoColors.Color_State_on_tertiary
    }

    // Thumb offset animation
    val thumbOffset by animateDpAsState(
        targetValue = if (isChecked) 28.dp else 0.dp,
        label = "thumb_offset"
    )

    // Track: 76x48
    Box(
        modifier = modifier
            .size(width = 76.dp, height = 48.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(
                width = 1.dp,
                color = PixsoColors.Color_State_pressed_shade_4,
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(4.dp)
    ) {
        // Thumb: 40x40
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(40.dp)
                .clip(CircleShape)
                .background(thumbColor)
                .then(
                    if (isChecked) {
                        Modifier.border(
                            width = 1.dp,
                            color = PixsoColors.Color_State_pressed_shade_4,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isChecked) iconOn else iconOff),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
