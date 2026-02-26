package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.awada.synapse.ui.theme.ButtonSmall
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun ScenarioButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerShape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)

    val backgroundColor = when {
        !enabled -> PixsoColors.Color_Bg_bg_shade_disabled
        isPressed -> PixsoColors.Color_State_secondary_pressed
        else -> PixsoColors.Color_Bg_bg_surface
    }

    val borderColor = when {
        !enabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_Border_border_primary
    }

    val textColor = when {
        !enabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_State_on_secondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .defaultMinSize(minWidth = PixsoDimens.Numeric_80, minHeight = PixsoDimens.Numeric_44)
            .clip(containerShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = PixsoDimens.Numeric_8, vertical = PixsoDimens.Numeric_4),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = PixsoDimens.Numeric_32)
                .clip(containerShape)
                .background(backgroundColor)
                .border(width = PixsoDimens.Stroke_S, color = borderColor, shape = containerShape)
                .padding(horizontal = PixsoDimens.Numeric_12, vertical = PixsoDimens.Numeric_8),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = ButtonSmall,
                color = textColor,
            )
        }
    }
}

