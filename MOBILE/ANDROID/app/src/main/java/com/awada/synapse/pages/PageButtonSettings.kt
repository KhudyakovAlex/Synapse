package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.ScenarioBlock
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import androidx.compose.material3.Text

@Composable
fun PageButtonSettings(
    buttonNumber: Int?,
    onBackClick: () -> Unit,
    onScenarioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedButtonNumber = buttonNumber ?: 1
    val shortPressScenarioBlocks = if (resolvedButtonNumber == 1) {
        listOf(
            listOf(
                ScheduleScenario(text = "Кухня – Вкл", onClick = onScenarioClick),
                ScheduleScenario(
                    text = "Моя любимая спаленка - темп. света 4500K",
                    onClick = onScenarioClick,
                ),
            ),
            listOf(ScheduleScenario(text = "Гостиная – Выкл", onClick = onScenarioClick)),
        )
    } else {
        emptyList()
    }
    val longPressScenarioBlock = if (resolvedButtonNumber == 1) {
        listOf(ScheduleScenario(text = "Спальня – Сцена 2", onClick = onScenarioClick))
    } else {
        null
    }

    BackHandler(onBack = onBackClick)

    PageContainer(
        title = "Кнопка $resolvedButtonNumber",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScenarioSection(
                title = "Короткое нажатие\n(сценарии ниже будут перебираться по очереди)",
                scenarioBlocks = shortPressScenarioBlocks,
                onAdd = onScenarioClick,
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_8))

            ScenarioSectionSingle(
                title = "Длинное нажатие\n(плавное изменение к сценарию)",
                scenarioBlock = longPressScenarioBlock,
                onAdd = onScenarioClick,
            )
        }
    }
}

@Composable
private fun ScenarioSection(
    title: String,
    scenarioBlocks: List<List<ScheduleScenario>>,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SupportingText(text = title)

        scenarioBlocks.forEach { block ->
            ScenarioBlock(
                scenarios = block,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SecondaryButton(
            text = "Добавить",
            onClick = onAdd,
        )
    }
}

@Composable
private fun ScenarioSectionSingle(
    title: String,
    scenarioBlock: List<ScheduleScenario>?,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SupportingText(text = title)

        if (scenarioBlock != null) {
            ScenarioBlock(
                scenarios = scenarioBlock,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (scenarioBlock == null) {
            SecondaryButton(
                text = "Добавить",
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun SupportingText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = BodyLarge,
        color = PixsoColors.Color_Text_text_1_level,
        modifier = modifier,
    )
}
