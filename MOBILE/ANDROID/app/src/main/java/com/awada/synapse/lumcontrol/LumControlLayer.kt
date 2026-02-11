package com.awada.synapse.lumcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// AI layer constants (must match AI.kt)
private val AI_MAIN_PANEL_HEIGHT = 100.dp
private val AI_DRAG_HANDLE_HEIGHT = 48.dp
private val AI_MIN_HEIGHT = AI_MAIN_PANEL_HEIGHT + AI_DRAG_HANDLE_HEIGHT + 20.dp

/**
 * Lighting control layer - positioned between pages and AI layer
 * Can be shown/hidden with animation
 * Positioned with bottom padding to avoid overlapping AI component
 */
@Composable
fun LumControlLayer(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    aiBottomPadding: PaddingValues = PaddingValues(bottom = AI_MIN_HEIGHT)
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(aiBottomPadding),
            contentAlignment = Alignment.Center
        ) {
            // Lighting control components will be added here based on Pixso design
        }
    }
}
