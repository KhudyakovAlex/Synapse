package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.awada.synapse.R
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.PanelButtonSettingBlock
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Button panel settings page.
 * Placeholder: duplicated structure from sensor settings.
 */
@Composable
fun PageButtonPanelSettings(
    onBackClick: () -> Unit,
    onScenarioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var roomId by remember { mutableIntStateOf(-1) }
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

    // Mock data for dropdown
    val roomItems = listOf(
        DropdownItem(1, "Гостиная"),
        DropdownItem(2, "Спальня"),
        DropdownItem(3, "Кухня"),
        DropdownItem(4, "Ванная")
    )

    PageContainer(
        title = "Настройки\nкнопочной панели",
        onBackClick = onBackClick,
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

            // 2. Помещение
            TextFieldForList(
                value = roomId.takeIf { it >= 0 },
                onValueChange = { roomId = it },
                icon = R.drawable.ic_chevron_down,
                label = "Помещение",
                placeholder = "Не выбрано",
                enabled = true,
                dropdownItems = roomItems
            )

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

