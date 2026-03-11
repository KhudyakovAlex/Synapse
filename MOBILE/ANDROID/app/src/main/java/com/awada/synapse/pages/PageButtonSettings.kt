package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.ScenarioBlock
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ScenarioEntity
import com.awada.synapse.db.ScenarioSetEntity
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun PageButtonSettings(
    buttonPanelId: Long?,
    buttonNumber: Int?,
    onBackClick: () -> Unit,
    onScenarioClick: (scenarioId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val resolvedButtonNumber = buttonNumber ?: 1

    val button by remember(db, buttonPanelId, resolvedButtonNumber) {
        if (buttonPanelId == null) {
            flowOf(null)
        } else {
            db.buttonDao().observeByPanelAndNumber(buttonPanelId, resolvedButtonNumber)
        }
    }.collectAsState(initial = null)

    val shortPressScenarios by remember(db, button?.id) {
        val buttonId = button?.id
        if (buttonId == null) {
            flowOf(emptyList())
        } else {
            db.scenarioSetDao().observeAllForButton(buttonId)
        }
    }.collectAsState(initial = emptyList())

    val shortPressScenarioBlocks = shortPressScenarios.map { scenarioSet ->
        listOf(
            ScheduleScenario(
                text = "Сценарий ${scenarioSet.scenarioId}",
                onClick = { onScenarioClick(scenarioSet.scenarioId) },
            )
        )
    }
    val longPressScenarioBlock = button?.longPressScenarioId?.let { scenarioId ->
        listOf(
            ScheduleScenario(
                text = "Сценарий $scenarioId",
                onClick = { onScenarioClick(scenarioId) },
            )
        )
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
                onAdd = {
                    val buttonId = button?.id ?: return@ScenarioSection
                    scope.launch {
                        val scenarioId = db.scenarioDao().insert(ScenarioEntity())
                        val position = db.scenarioSetDao().getNextPositionForButton(buttonId)
                        db.scenarioSetDao().insert(
                            ScenarioSetEntity(
                                buttonId = buttonId,
                                position = position,
                                scenarioId = scenarioId,
                            )
                        )
                        onScenarioClick(scenarioId)
                    }
                },
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_8))

            ScenarioSectionSingle(
                title = "Длинное нажатие\n(плавное изменение к сценарию)",
                scenarioBlock = longPressScenarioBlock,
                onAdd = {
                    val buttonId = button?.id ?: return@ScenarioSectionSingle
                    scope.launch {
                        val existingScenarioId = button?.longPressScenarioId
                        val scenarioId = if (existingScenarioId != null) {
                            existingScenarioId
                        } else {
                            db.scenarioDao().insert(ScenarioEntity()).also {
                                db.buttonDao().setLongPressScenarioId(buttonId, it)
                            }
                        }
                        onScenarioClick(scenarioId)
                    }
                },
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
