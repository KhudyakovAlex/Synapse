package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Result of tooltip interaction
 */
enum class TooltipResult {
    Primary,    // Primary button clicked
    Secondary,  // Secondary button clicked
    Dismissed   // Dismissed by tapping outside
}

/**
 * Modal tooltip dialog with text and optional buttons.
 * 
 * Features:
 * - Centered on screen
 * - Dimmed background (scrim)
 * - AI layer stays on top and remains interactive (not blocked)
 * 
 * Tokens:
 * - Background: bg_surface (white)
 * - Scrim: bg_scrim (dimmed)
 * - Radius: Radius_M (24dp)
 * - Text: text_1_level + Body L style
 * - Padding: 20/24dp
 * - Buttons spacing: 12dp (vertical), 16dp (horizontal)
 * 
 * @param text Main text content
 * @param onResult Callback with result (Primary/Secondary/Dismissed)
 * @param primaryButtonText Primary button text (right button, required)
 * @param secondaryButtonText Optional secondary button text (left button)
 */
@Composable
fun Tooltip(
    text: String,
    primaryButtonText: String,
    onResult: (TooltipResult) -> Unit,
    modifier: Modifier = Modifier,
    secondaryButtonText: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixsoColors.Color_Bg_scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onResult(TooltipResult.Dismissed) }
            )
            .zIndex(999f), // Below AI layer (AI is 1000+)
        contentAlignment = Alignment.Center
    ) {
        // Tooltip content box
        Column(
            modifier = Modifier
                .width(328.dp)
                .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_M))
                .background(PixsoColors.Color_Bg_bg_surface)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Stop click propagation */ }
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text content
            Text(
                text = text,
                style = BodyLarge,
                color = PixsoColors.Color_Text_text_1_level,
                modifier = Modifier.fillMaxWidth()
            )

            // Buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), // Additional 4dp for total 16dp spacing
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                // Secondary button (left) - optional
                secondaryButtonText?.let {
                    SecondaryButton(
                        text = it,
                        onClick = { onResult(TooltipResult.Secondary) }
                    )
                }

                // Primary button (right) - required
                PrimaryButton(
                    text = primaryButtonText,
                    onClick = { onResult(TooltipResult.Primary) }
                )
            }
        }
    }
}
