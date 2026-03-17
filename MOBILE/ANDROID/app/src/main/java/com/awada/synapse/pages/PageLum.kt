package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.LumIndicatorsBlock

/** Экран управления отдельным светильником. */
internal val PageLumLlmDescriptor = LLMPageDescriptor(
    fileName = "PageLum",
    screenName = "Lum",
    titleRu = "Светильник",
    description = "Позволяет управлять выбранным светильником, его яркостью, температурой и сценами."
)

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
        var stableBottomInset by remember { mutableStateOf(0.dp) }

        // Keep the largest observed inset for this screen instance so the indicator
        // does not jump down while the panel is closing during navigation away.
        LaunchedEffect(bottomInset) {
            if (bottomInset > stableBottomInset) {
                stableBottomInset = bottomInset
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Shrink available height so center is between AppBar and panel top
                .padding(bottom = stableBottomInset),
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

