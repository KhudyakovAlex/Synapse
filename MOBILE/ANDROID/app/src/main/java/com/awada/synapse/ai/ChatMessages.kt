package com.awada.synapse.ai

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.BodySmall
import com.awada.synapse.ui.theme.ButtonMedium
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

private val BUBBLE_MAX_WIDTH = 336.dp
private val BUBBLE_PADDING_HORIZONTAL = 16.dp
private val BUBBLE_PADDING_VERTICAL = 8.dp
private val LOADING_BUBBLE_MIN_HEIGHT = 48.dp
private val MESSAGE_PADDING_TOP = 16.dp
private val MESSAGE_PADDING_BOTTOM = 8.dp
private val MESSAGE_PADDING_SIDE = 40.dp
private val TIME_PADDING_TOP = 4.dp

/**
 * AI message bubble - aligned to start (left)
 * Corner radii: topStart=S, topEnd=S, bottomStart=None, bottomEnd=S
 * Time is inside the bubble, aligned to end
 */
@Composable
fun UIMessageAI(
    modifier: Modifier = Modifier,
    text: String,
    time: String = ""
) {
    val bubbleShape = aiBubbleShape()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = MESSAGE_PADDING_TOP,
                bottom = MESSAGE_PADDING_BOTTOM,
                start = 0.dp,
                end = MESSAGE_PADDING_SIDE
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = BUBBLE_MAX_WIDTH)
                .background(
                    color = PixsoColors.Color_Bg_bg_surface,
                    shape = bubbleShape
                )
                .border(
                    width = 1.dp,
                    color = PixsoColors.Color_Border_border_shade_8,
                    shape = bubbleShape
                )
                .padding(
                    horizontal = BUBBLE_PADDING_HORIZONTAL,
                    vertical = BUBBLE_PADDING_VERTICAL
                )
        ) {
            Text(
                text = text,
                style = BodyLarge,
                color = PixsoColors.Color_Text_text_1_level
            )
            
            if (time.isNotEmpty()) {
                Text(
                    text = time,
                    style = BodySmall,
                    color = PixsoColors.Color_Text_text_4_level,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TIME_PADDING_TOP),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun UIMessageAILoading(
    modifier: Modifier = Modifier
) {
    val bubbleShape = aiBubbleShape()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = MESSAGE_PADDING_TOP,
                bottom = MESSAGE_PADDING_BOTTOM,
                start = 0.dp,
                end = MESSAGE_PADDING_SIDE
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = BUBBLE_MAX_WIDTH)
                .defaultMinSize(minHeight = LOADING_BUBBLE_MIN_HEIGHT)
                .background(
                    color = PixsoColors.Color_Bg_bg_surface,
                    shape = bubbleShape
                )
                .border(
                    width = 1.dp,
                    color = PixsoColors.Color_Border_border_shade_8,
                    shape = bubbleShape
                )
                .padding(
                    horizontal = BUBBLE_PADDING_HORIZONTAL,
                    vertical = BUBBLE_PADDING_VERTICAL
                ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                LoadingDot(index = index)
            }
        }
    }
}

/**
 * User message bubble - aligned to end (right)
 * Corner radii: topStart=S, topEnd=S, bottomStart=S, bottomEnd=None
 * Time is inside the bubble, aligned to end
 */
@Composable
fun UIMessageUser(
    modifier: Modifier = Modifier,
    text: String,
    time: String = ""
) {
    val bubbleShape = RoundedCornerShape(
        topStart = PixsoDimens.Radius_Radius_S,
        topEnd = PixsoDimens.Radius_Radius_S,
        bottomStart = PixsoDimens.Radius_Radius_S,
        bottomEnd = PixsoDimens.Radius_Radius_None
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = MESSAGE_PADDING_TOP,
                bottom = MESSAGE_PADDING_BOTTOM,
                start = MESSAGE_PADDING_SIDE,
                end = 0.dp
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = BUBBLE_MAX_WIDTH)
                .background(
                    color = PixsoColors.Color_Bg_bg_primary_light,
                    shape = bubbleShape
                )
                .padding(
                    horizontal = BUBBLE_PADDING_HORIZONTAL,
                    vertical = BUBBLE_PADDING_VERTICAL
                )
        ) {
            Text(
                text = text,
                style = BodyLarge,
                color = PixsoColors.Color_Text_text_1_level
            )
            
            if (time.isNotEmpty()) {
                Text(
                    text = time,
                    style = BodySmall,
                    color = PixsoColors.Color_Text_text_4_level,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TIME_PADDING_TOP),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun LoadingDot(index: Int) {
    val transition = rememberInfiniteTransition(label = "aiLoadingDots")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = index * 160
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aiLoadingDotAlpha"
    )

    Box(
        modifier = Modifier
            .size(4.dp)
            .alpha(alpha)
            .background(
                color = PixsoColors.Color_State_primary,
                shape = CircleShape
            )
    )
}

private fun aiBubbleShape() = RoundedCornerShape(
    topStart = PixsoDimens.Radius_Radius_S,
    topEnd = PixsoDimens.Radius_Radius_S,
    bottomStart = PixsoDimens.Radius_Radius_None,
    bottomEnd = PixsoDimens.Radius_Radius_S
)

private val QUICK_REPLY_MIN_WIDTH = 80.dp
private val QUICK_REPLY_MIN_HEIGHT = 44.dp
private val QUICK_REPLY_PADDING_HORIZONTAL = 16.dp
private val QUICK_REPLY_PADDING_VERTICAL = 12.dp
private val QUICK_REPLY_SPACING = 4.dp

/**
 * Quick reply chip - aligned to end (right)
 * Used for suggested responses
 */
@Composable
fun UIQuickReply(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {}
) {
    val chipShape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor = if (isPressed) {
        PixsoColors.Color_State_secondary_pressed
    } else {
        PixsoColors.Color_State_secondary
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = QUICK_REPLY_SPACING,
                bottom = QUICK_REPLY_SPACING,
                start = MESSAGE_PADDING_SIDE,
                end = 0.dp
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = QUICK_REPLY_MIN_WIDTH,
                    minHeight = QUICK_REPLY_MIN_HEIGHT
                )
                .clip(chipShape)
                .background(color = backgroundColor)
                .border(
                    width = 1.dp,
                    color = PixsoColors.Color_Border_border_primary,
                    shape = chipShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
                .padding(
                    horizontal = QUICK_REPLY_PADDING_HORIZONTAL,
                    vertical = QUICK_REPLY_PADDING_VERTICAL
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = ButtonMedium,
                color = PixsoColors.Color_State_on_secondary
            )
        }
    }
}
