package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
    indicatorsOffsetY: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Светильник",
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Single unified block to position as a whole.
            LumIndicatorsBlock(
                brightnessPercent = 35, // TODO: bind to real device state
                modifier = Modifier.offset(y = indicatorsOffsetY)
            )
        }
    }
}

