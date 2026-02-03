package com.awada.synapse

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.*

/**
 * Keyboard button styles matching Pixso design
 */
enum class KeyboardButtonStyle {
    Default,  // Regular text button (e.g. "1")
    Icon,     // Icon button
    Help      // Help text button (e.g. "Не могу войти")
}

/**
 * Keyboard button component.
 * Size: 88×68dp, Corner radius: 16dp
 *
 * Tokens:
 * - Radius: Radius_S (16dp)
 * - Style=Default: Headline L text style
 * - Style=Help: Label L text style
 * - State=Default background: bg_surface (white) with border_shade_8
 * - State=Pressed background: secondary_pressed (light gray)
 * - Text/Icon color: text_1_level
 */
@Composable
fun UIKeyboardButton(
    style: KeyboardButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    @DrawableRes icon: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        style == KeyboardButtonStyle.Default && !isPressed -> PixsoColors.Color_Bg_bg_surface
        isPressed -> PixsoColors.Color_State_secondary_pressed
        else -> Color.Transparent
    }

    val borderStroke = if (style == KeyboardButtonStyle.Default && !isPressed) {
        BorderStroke(PixsoDimens.Stroke_S, PixsoColors.Color_Border_border_shade_8)
    } else {
        null
    }

    Box(
        modifier = modifier
            .size(width = 88.dp, height = 68.dp)
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_S))
            .background(backgroundColor)
            .then(
                if (borderStroke != null) {
                    Modifier.border(
                        borderStroke.width,
                        borderStroke.brush,
                        RoundedCornerShape(PixsoDimens.Radius_Radius_S)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            KeyboardButtonStyle.Default -> {
                Text(
                    text = text,
                    style = HeadlineLarge,
                    color = PixsoColors.Color_Text_text_1_level,
                    textAlign = TextAlign.Center
                )
            }
            KeyboardButtonStyle.Icon -> {
                icon?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = PixsoColors.Color_Text_text_1_level,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            KeyboardButtonStyle.Help -> {
                Text(
                    text = text,
                    style = LabelLarge,
                    color = PixsoColors.Color_Text_text_1_level,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
