package com.awada.synapse.pages

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom inset reserved for overlays (e.g. LumControlLayer).
 * Pages can use it to avoid being covered by the bottom panel.
 */
val LocalBottomOverlayInset: androidx.compose.runtime.ProvidableCompositionLocal<Dp> =
    compositionLocalOf { 0.dp }

