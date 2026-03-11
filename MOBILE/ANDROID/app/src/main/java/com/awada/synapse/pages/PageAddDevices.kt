package com.awada.synapse.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PageAddDevices(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Добавление устройств",
        onBackClick = onBackClick,
        isScrollable = false,
        bottomSpacerHeightOverride = 0.dp,
        modifier = modifier
    ) {
    }
}
