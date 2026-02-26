package com.awada.synapse.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun PageSchedule(
    onBackClick: () -> Unit,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Расписание",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = { onAddClick?.invoke() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

