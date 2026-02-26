package com.awada.synapse.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun PageScenario(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(
        title = "Сенарий",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize(),
    ) {
    }
}

