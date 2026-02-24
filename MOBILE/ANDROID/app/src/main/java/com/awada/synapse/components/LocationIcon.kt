package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors

@Composable
fun LocationIcon(
    title: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    cardSize: Dp = 156.dp,
    iconSize: Dp = 56.dp,
    contentOffsetY: Dp = 0.dp,
    showTitle: Boolean = true,
    enabled: Boolean = true,
    backgroundColor: Color = PixsoColors.Color_Bg_bg_surface,
    titleColor: Color = PixsoColors.Color_State_tertiary,
    iconTint: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shadowElevation = if (enabled) 8.dp else 0.dp
    val shadowColor = Color.Black.copy(alpha = 1f / 3f)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .size(cardSize)
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(cardSize)
                .shadow(
                    elevation = shadowElevation,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = contentOffsetY)
            ) {
                Image(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    colorFilter = iconTint?.let { ColorFilter.tint(it) }
                )

                if (showTitle) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = LabelMedium.copy(lineHeight = LabelMedium.lineHeight * 0.8f),
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .widthIn(max = (cardSize - 12.dp))
                    )
                }
            }
        }
    }
}
