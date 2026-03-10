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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.awada.synapse.R
import com.awada.synapse.components.PanelButtonSettingBlock
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.displayName
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
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
    var roomId by remember { mutableStateOf<Int?>(null) }
    var controllerId by remember { mutableStateOf<Int?>(null) }
    var rooms by remember { mutableStateOf<List<RoomEntity>>(emptyList()) }
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
        roomId = e.roomId
        controllerId = e.controllerId
        rooms = db.roomDao().getAllOrdered(e.controllerId)
    }

    fun saveAndBack() {
        scope.launch {
            val id = buttonPanelId
            if (id != null) {
                db.buttonPanelDao().setName(id = id, name = name)
                db.buttonPanelDao().moveToRoom(id = id, roomId = roomId)
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

            // 2. Помещение
            val roomDropdownItems = listOf(DropdownItem(id = -1, text = "Вне помещений")) +
                rooms.map { DropdownItem(id = it.id, text = it.displayName()) }
            
            TextFieldForList(
                value = roomId ?: -1,
                onValueChange = { selectedId ->
                    roomId = if (selectedId == -1) null else selectedId
                },
                icon = R.drawable.ic_chevron_down,
                label = "Помещение",
                placeholder = "Выберите помещение",
                enabled = true,
                dropdownItems = roomDropdownItems
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

