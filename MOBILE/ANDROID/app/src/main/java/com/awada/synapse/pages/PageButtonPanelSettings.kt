package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.awada.synapse.components.PanelButtonSettingBlock
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.TextField
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.launch

/**
 * Button panel settings page.
 * Placeholder: duplicated structure from sensor settings.
 */
@Composable
fun PageButtonPanelSettings(
    buttonPanelId: Long?,
    onBackClick: () -> Unit,
    onScenarioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    val shortPressScenarioBlocks: List<androidx.compose.runtime.MutableState<List<List<String>>>> = remember {
        listOf(
            mutableStateOf(
                listOf(
                    listOf(
                        "Кухня – Вкл",
                        "Моя любимая спаленка - темп. света 4500K",
                    ),
                    listOf("Гостиная – Выкл"),
                )
            ),
            mutableStateOf<List<List<String>>>(emptyList()),
            mutableStateOf<List<List<String>>>(emptyList()),
            mutableStateOf<List<List<String>>>(emptyList()),
        )
    }
    val longPressScenario: List<androidx.compose.runtime.MutableState<String?>> = remember {
        listOf(
            mutableStateOf<String?>("Спальня – Сцена 2"),
            mutableStateOf<String?>(null),
            mutableStateOf<String?>(null),
            mutableStateOf<String?>(null),
        )
    }

    LaunchedEffect(buttonPanelId) {
        val id = buttonPanelId ?: return@LaunchedEffect
        val e = db.buttonPanelDao().getById(id) ?: return@LaunchedEffect
        name = e.name
    }

    fun saveAndBack() {
        scope.launch {
            val id = buttonPanelId
            if (id != null) {
                db.buttonPanelDao().setName(id = id, name = name)
            }
            onBackClick()
        }
    }

    BackHandler { saveAndBack() }

    PageContainer(
        title = "Настройки\nкнопочной панели",
        onBackClick = ::saveAndBack,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            // 1. Название
            TextField(
                value = name,
                onValueChange = { name = it },
                label = "Название",
                placeholder = "",
                enabled = true
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

            Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_24)) {
                repeat(4) { idx ->
                    val shortBlocks = shortPressScenarioBlocks[idx].value
                    val shortScheduleBlocks = shortBlocks.map { block ->
                        block.map { text -> ScheduleScenario(text = text, onClick = onScenarioClick) }
                    }
                    val longScheduleBlock = longPressScenario[idx].value?.let { text ->
                        listOf(ScheduleScenario(text = text, onClick = onScenarioClick))
                    }
                    PanelButtonSettingBlock(
                        buttonNumber = idx + 1,
                        shortPressScenarioBlocks = shortScheduleBlocks,
                        longPressScenarioBlock = longScheduleBlock,
                        onAddShortPressScenario = onScenarioClick,
                        onAddLongPressScenario = onScenarioClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

