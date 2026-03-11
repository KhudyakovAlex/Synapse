package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    Tertiary,   // Middle optional button clicked
    Quaternary, // Additional optional button clicked
    Secondary,  // Secondary button clicked
    Dismissed   // Dismissed by tapping outside
}

object TooltipOverlayState {
    var isVisible by mutableStateOf(false)
}

/**
 * Modal tooltip dialog with optional text and buttons.
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
 * @param text Optional text content
 * @param onResult Callback with result (Primary/Secondary/Dismissed)
 * @param primaryButtonText Primary button text (right button, required)
 * @param secondaryButtonText Optional secondary button text (left button)
 * @param tertiaryButtonText Optional middle button text
 */
@Composable
fun Tooltip(
    text: String? = null,
    primaryButtonText: String,
    onResult: (TooltipResult) -> Unit,
    modifier: Modifier = Modifier,
    secondaryButtonText: String? = null,
    tertiaryButtonText: String? = null,
    quaternaryButtonText: String? = null
) {
    DisposableEffect(Unit) {
        TooltipOverlayState.isVisible = true
        onDispose {
            TooltipOverlayState.isVisible = false
        }
    }

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
            if (!text.isNullOrBlank()) {
                Text(
                    text = text,
                    style = BodyLarge,
                    color = PixsoColors.Color_Text_text_1_level,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (tertiaryButtonText != null || quaternaryButtonText != null || text.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (text.isNullOrBlank()) 0.dp else 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    PrimaryButton(
                        text = primaryButtonText,
                        onClick = { onResult(TooltipResult.Primary) }
                    )

                    tertiaryButtonText?.let {
                        PrimaryButton(
                            text = it,
                            onClick = { onResult(TooltipResult.Tertiary) }
                        )
                    }

                    quaternaryButtonText?.let {
                        PrimaryButton(
                            text = it,
                            onClick = { onResult(TooltipResult.Quaternary) }
                        )
                    }

                    secondaryButtonText?.let {
                        SecondaryButton(
                            text = it,
                            onClick = { onResult(TooltipResult.Secondary) }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (text.isNullOrBlank()) 0.dp else 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    PrimaryButton(
                        text = primaryButtonText,
                        onClick = { onResult(TooltipResult.Primary) }
                    )
                    secondaryButtonText?.let {
                        SecondaryButton(
                            text = it,
                            onClick = { onResult(TooltipResult.Secondary) }
                        )
                    }
                }
            }
        }
    }
}
