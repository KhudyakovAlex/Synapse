package com.awada.synapse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors

@Composable
fun PanelButtonSettingBlock(
    buttonNumber: Int,
    shortPressScenarioBlocks: List<List<ScheduleScenario>>,
    longPressScenarioBlock: List<ScheduleScenario>?,
    onAddShortPressScenario: () -> Unit,
    onAddLongPressScenario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        PanelButton(
            text = buttonNumber.toString(),
            variant = PanelButtonVariant.Def,
            size = 72.dp,
            modifier = Modifier,
            onClick = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            ScenarioSection(
                title = "Короткое нажатие",
                scenarioBlocks = shortPressScenarioBlocks,
                onAdd = onAddShortPressScenario,
            )

            ScenarioSectionSingle(
                title = "Длинное нажатие",
                scenarioBlock = longPressScenarioBlock,
                onAdd = onAddLongPressScenario,
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

