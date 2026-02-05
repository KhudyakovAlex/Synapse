package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Icon selection button component.
 * Size: 72×72dp, Corner radius: 16dp, Icon size: 40×40dp
 *
 * Tokens:
 * - Radius: Radius_S (16dp)
 * - State=Default: bg_surface + border_shade_4 (1dp) + tertiary icon
 * - State=Pressed: secondary_pressed + border_shade_4 (1dp) + tertiary icon
 * - State=Active: bg_surface + border_focus (4dp) + tertiary icon
 * - State=Disabled: disabled bg + on_disabled icon (no border)
 */
@Composable
fun IconSelectButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        !enabled -> PixsoColors.Color_State_disabled
        isPressed -> PixsoColors.Color_State_secondary_pressed
        else -> PixsoColors.Color_Bg_bg_surface
    }

    val borderColor = if (isActive) {
        PixsoColors.Color_Border_border_focus
    } else {
        PixsoColors.Color_Border_border_shade_4
    }

    val borderWidth = if (isActive) {
        PixsoDimens.Border_width_border_width_4
    } else {
        PixsoDimens.Stroke_S
    }

    val showBorder = enabled

    val iconColor = if (enabled) {
        PixsoColors.Color_State_tertiary
    } else {
        PixsoColors.Color_State_on_disabled
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_S))
            .background(backgroundColor)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(PixsoDimens.Radius_Radius_S)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        key(icon) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(PixsoDimens.Numeric_40)
            )
        }
    }
}
