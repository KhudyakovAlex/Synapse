package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.LabelMedium
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun IconRoom(
    text: String,
    secondaryText: String? = null,
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
    height: Dp = 96.dp,
    elevation: Dp = 8.dp,
    backgroundColor: Color = PixsoColors.Color_Bg_bg_surface,
    contentColor: Color = PixsoColors.Color_State_tertiary,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_Full)
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .height(height)
            // Shadow pattern matches LocationIcon: shadow outside, then clip + background.
            .shadow(elevation = elevation, shape = shape, clip = false)
            .clip(shape)
            .background(backgroundColor)
            .then(clickableModifier)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(contentColor)
            )
        }

        // No spacing in Pixso: text starts right after 80dp icon slot.
        val displayText = if (!secondaryText.isNullOrBlank()) "$text\n$secondaryText" else text
        Text(
            text = displayText,
            style = LabelMedium,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(120.dp)
        )
    }
}
