package com.awada.synapse.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun PageSchedule(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Расписание",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize()
    ) {
    }
}

