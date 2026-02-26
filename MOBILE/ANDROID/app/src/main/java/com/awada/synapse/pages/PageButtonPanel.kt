package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PageButtonPanel(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(
        title = "Панель 7",
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize(),
    ) {
        // empty content by design
    }
}

