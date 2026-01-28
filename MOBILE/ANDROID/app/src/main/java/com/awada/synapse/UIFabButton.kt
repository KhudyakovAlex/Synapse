package com.awada.synapse

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors

enum class FabState {
    Default,
    ActiveStroke1,
    ActiveStroke12,
    Disabled
}

@Composable
fun UIFabButton(
    state: FabState,
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    @DrawableRes iconDisabled: Int,
    modifier: Modifier = Modifier
) {
    // Background color
    val backgroundColor = when (state) {
        FabState.Default -> PixsoColors.Color_Bg_bg_surface
        FabState.ActiveStroke1, FabState.ActiveStroke12 -> PixsoColors.Color_Bg_bg_primary_light
        FabState.Disabled -> PixsoColors.Color_State_disabled
    }

    // Border
    val borderModifier = when (state) {
        FabState.ActiveStroke1 -> Modifier.border(
            width = 1.dp,
            color = PixsoColors.Color_Border_border_focus,
            shape = CircleShape
        )
        FabState.ActiveStroke12 -> Modifier.border(
            width = 12.dp,
            color = PixsoColors.Color_Border_border_focus,
            shape = CircleShape
        )
        else -> Modifier
    }

    // Icon color
    val iconColor = when (state) {
        FabState.Disabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_State_primary
    }

    val enabled = state != FabState.Disabled

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(enabled = enabled) { onClick() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = if (state == FabState.Disabled) iconDisabled else icon),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
