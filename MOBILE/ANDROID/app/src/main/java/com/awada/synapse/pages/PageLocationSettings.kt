package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PageLocationSettings(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Настройки\nлокации",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize()
    ) {
        // Пока пусто
    }
}

