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
 * Press sensor settings page.
 * Configure press sensor parameters.
 */
@Composable
fun PageSensorPressSettings(
    sensorId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var delaySec by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf<Int?>(null) }
    var controllerId by remember { mutableStateOf<Int?>(null) }
    var rooms by remember { mutableStateOf<List<RoomEntity>>(emptyList()) }

    LaunchedEffect(sensorId) {
        val id = sensorId ?: return@LaunchedEffect
        val e = db.presSensorDao().getById(id) ?: return@LaunchedEffect
        name = e.name
        roomId = e.roomId
        controllerId = e.controllerId
        rooms = db.roomDao().getAllOrdered(e.controllerId)
    }

    fun saveAndBack() {
        scope.launch {
            val id = sensorId
            if (id != null) {
                db.presSensorDao().setName(id = id, name = name)
                db.presSensorDao().moveToRoom(id = id, roomId = roomId)
            }
            onBackClick()
        }
    }
    
    BackHandler { saveAndBack() }

    PageContainer(
        title = "Настройки\nдатчика присутствия",
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
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // Остальные параметры пока не сохраняем (вне текущей задачи).
            TextField(
                value = delaySec,
                onValueChange = { delaySec = it },
                label = "Задержка, сек",
                placeholder = "",
                enabled = true
            )
        }
    }
}
