package com.awada.synapse.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun PageSchedulePoint(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(
        title = "Пункт расписания",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
        ) {
            // empty mock content for now
        }
    }
}

