package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.LumIndicatorsBlock

/**
 * Page for individual luminaire control.
 * For now it's an empty placeholder screen.
 */
@Composable
fun PageLum(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    brightnessPercent: Int,
    typeId: Int,
    hue: Int,
    saturation: Int,
    temperature: Int,
    indicatorsOffsetY: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Светильник",
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = false,
        bottomSpacerHeightOverride = 0.dp,
        modifier = modifier
    ) {
        val bottomInset = LocalBottomOverlayInset.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Shrink available height so center is between AppBar and panel top
                .padding(bottom = bottomInset),
            contentAlignment = Alignment.Center
        ) {
            // Single unified block to position as a whole.
            LumIndicatorsBlock(
                brightnessPercent = brightnessPercent,
                typeId = typeId,
                hue = hue,
                saturation = saturation,
                temperature = temperature,
                modifier = Modifier.offset(y = indicatorsOffsetY)
            )
        }
    }
}

