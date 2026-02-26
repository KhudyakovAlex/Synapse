package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.SchedulePoint
import com.awada.synapse.components.ScheduleScenario
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
        isScrollable = true,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            SchedulePoint(
                timeText = "10:00",
                days = listOf("Пн", "Вт", "Ср", "Чт", "Пт"),
                scenarios = listOf(
                    ScheduleScenario(text = "Кухня – Вкл", onClick = {}),
                    ScheduleScenario(text = "Моя любимая спаленка - темп. света 4500K", onClick = {}),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            SchedulePoint(
                timeText = "19:00",
                days = listOf("Ежедневно"),
                scenarios = listOf(
                    ScheduleScenario(text = "Спальня – Выкл", onClick = {}),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            SchedulePoint(
                timeText = "12:00",
                days = listOf("Пн", "Вт", "Ср", "Чт", "Пт"),
                scenarios = listOf(
                    ScheduleScenario(text = "Кухня – Вкл", onClick = {}),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = { onAddClick?.invoke() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

