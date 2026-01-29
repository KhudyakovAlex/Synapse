package com.awada.synapse

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

enum class FabState {
    Default,
    ActiveStroke1,
    ActiveStroke12,
    Disabled
}

private val BORDER_WIDTH_MAX = PixsoDimens.Border_width_border_width_12
private val BORDER_WIDTH_MIN = PixsoDimens.Border_width_border_width_12 / 3

@Composable
fun UIFabButton(
    state: FabState,
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    @DrawableRes iconDisabled: Int,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // Pulsating animation for border width when pressed
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val animatedBorderWidthFloat by infiniteTransition.animateFloat(
        initialValue = BORDER_WIDTH_MAX.value,
        targetValue = BORDER_WIDTH_MIN.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderWidth"
    )
    val animatedBorderWidth = animatedBorderWidthFloat.dp
    
    // Background color
    val backgroundColor = when {
        state == FabState.Disabled -> PixsoColors.Color_State_disabled
        isPressed -> PixsoColors.Color_Bg_bg_primary_light
        state == FabState.ActiveStroke1 || state == FabState.ActiveStroke12 -> PixsoColors.Color_Bg_bg_primary_light
        else -> PixsoColors.Color_Bg_bg_surface
    }

    // Border width for outer stroke
    val borderWidth = when {
        isPressed -> animatedBorderWidth
        state == FabState.ActiveStroke1 -> 1.dp
        state == FabState.ActiveStroke12 -> BORDER_WIDTH_MAX
        else -> 0.dp
    }
    
    val borderColor = PixsoColors.Color_State_primary

    // Icon color
    val iconColor = when (state) {
        FabState.Disabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_State_primary
    }

    val enabled = state != FabState.Disabled
    val buttonSize = 72.dp

    Box(
        modifier = modifier
            .size(buttonSize + borderWidth * 2) // Expand size for outer border
            .drawBehind {
                if (borderWidth > 0.dp) {
                    val strokeWidthPx = borderWidth.toPx()
                    // Draw circle outside the button (radius = button radius + half stroke)
                    drawCircle(
                        color = borderColor,
                        radius = (buttonSize.toPx() / 2) + (strokeWidthPx / 2),
                        style = Stroke(width = strokeWidthPx)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(backgroundColor)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                                onClick()
                            }
                        )
                    }
                }
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
}
