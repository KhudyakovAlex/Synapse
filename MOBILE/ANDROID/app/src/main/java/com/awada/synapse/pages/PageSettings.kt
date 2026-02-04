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
 * Settings page.
 * Allows user to configure app settings.
 */
@Composable
fun PageSettings(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier
    ) {
        // Future settings content goes here
    }
}
