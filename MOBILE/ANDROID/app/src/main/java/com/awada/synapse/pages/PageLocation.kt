package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Initial page for Locations.
 * Always placed below UIAI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Локации",
        onSettingsClick = onSettingsClick,
        isScrollable = true,
        modifier = modifier
    ) {
        // Future location content goes here
    }
}
