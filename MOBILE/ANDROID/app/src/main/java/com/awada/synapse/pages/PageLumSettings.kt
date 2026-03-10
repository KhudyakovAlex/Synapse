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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GroupEntity
import com.awada.synapse.db.LuminaireTypeEntity
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.displayName
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.launch

/**
 * Luminaire settings page.
 * Configure luminaire parameters.
 */
@Composable
fun PageLumSettings(
    luminaireId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var showIconSelect by remember { mutableStateOf(false) }
    var iconId by remember { mutableIntStateOf(300) }
    var name by remember { mutableStateOf("") }
    var typeId by remember { mutableIntStateOf(LuminaireTypeEntity.TYPE_DIMMABLE) }
    var roomId by remember { mutableStateOf<Int?>(null) }
    var groupId by remember { mutableStateOf<Int?>(null) }
    var controllerId by remember { mutableStateOf<Int?>(null) }
    var rooms by remember { mutableStateOf<List<RoomEntity>>(emptyList()) }
    var groups by remember { mutableStateOf<List<GroupEntity>>(emptyList()) }
    var luminaireTypes by remember { mutableStateOf<List<LuminaireTypeEntity>>(emptyList()) }

    LaunchedEffect(luminaireId) {
        val id = luminaireId ?: return@LaunchedEffect
        val e = db.luminaireDao().getById(id) ?: return@LaunchedEffect
        name = e.name
        iconId = e.icoNum
        typeId = e.typeId
        roomId = e.roomId
        groupId = e.groupId
        controllerId = e.controllerId
        rooms = db.roomDao().getAllOrdered(e.controllerId)
        groups = db.groupDao().getAllOrdered()
        luminaireTypes = db.luminaireTypeDao().getAllOrdered()
    }

    fun saveAndBack() {
        scope.launch {
            val id = luminaireId
            if (id != null) {
                db.luminaireDao().setNameIconAndType(id = id, name = name, icoNum = iconId, typeId = typeId)
                db.luminaireDao().moveToRoom(id = id, roomId = roomId)
                db.luminaireDao().moveToGroup(id = id, groupId = groupId)
            }
            onBackClick()
        }
    }
    
    BackHandler {
        if (showIconSelect) {
            showIconSelect = false
        } else {
            saveAndBack()
        }
    }

    if (showIconSelect) {
        PageIconSelect(
            category = "luminaire",
            currentIconId = iconId,
            onIconSelected = { newId ->
                iconId = newId
                showIconSelect = false
            },
            onBackClick = { showIconSelect = false },
            modifier = modifier
        )
        return
    }

    PageContainer(
        title = "Настройки\nсветильника",
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

            // 3. Группа
            val groupDropdownItems = listOf(DropdownItem(id = -1, text = "Вне групп")) +
                groups.map { DropdownItem(id = it.id, text = it.displayName()) }

            TextFieldForList(
                value = groupId ?: -1,
                onValueChange = { selectedId ->
                    groupId = if (selectedId == -1) null else selectedId
                },
                icon = R.drawable.ic_chevron_down,
                label = "Группа",
                placeholder = "Выберите группу",
                enabled = true,
                dropdownItems = groupDropdownItems
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 4. Тип
            val typeDropdownItems = luminaireTypes.map { DropdownItem(id = it.id, text = it.name) }

            TextFieldForList(
                value = typeId,
                onValueChange = { selectedId -> typeId = selectedId },
                icon = R.drawable.ic_chevron_down,
                label = "Тип",
                placeholder = "Выберите тип",
                enabled = typeDropdownItems.isNotEmpty(),
                dropdownItems = typeDropdownItems
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

            // 5. Иконка
            Column(
                verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8)
            ) {
                Text(
                    text = "Иконка",
                    style = LabelLarge,
                    color = PixsoColors.Color_Text_text_3_level,
                    modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12)
                )
                IconSelectButton(
                    icon = iconResId(context, iconId, fallback = R.drawable.luminaire_300_default),
                    onClick = { showIconSelect = true }
                )
            }
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
        }
    }
}
