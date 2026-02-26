package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awada.synapse.ui.theme.IBMPlexSansFamily
import com.awada.synapse.ui.theme.PixsoColors

enum class PanelButtonVariant {
    Def,
    Active,
}

@Composable
fun PanelButton(
    text: String,
    variant: PanelButtonVariant,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val backgroundColor = when (variant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_State_tertiary
    }
    val textColor = when (variant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_on_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_Text_text_inverse
    }
    val dotColor = when (variant) {
        PanelButtonVariant.Def -> PixsoColors.Color_State_on_disabled
        PanelButtonVariant.Active -> PixsoColors.Color_Bg_bg_elevated
    }

    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = IBMPlexSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                ),
                color = textColor,
            )

            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .offset(x = 16.875.dp, y = 1.75.dp)
                    .background(dotColor, CircleShape)
                    .align(Alignment.CenterStart),
            )
        }
    }
}

