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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.R
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GraphEntity
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val OBJECT_TYPE_LOCATION = 1
private const val OBJECT_TYPE_ROOM = 2
private const val OBJECT_TYPE_GROUP = 3
private const val OBJECT_TYPE_LUMINAIRE = 4

private const val CHANGE_TYPE_BRIGHTNESS = 1
private const val CHANGE_TYPE_TEMPERATURE = 2

@Composable
fun PageGraph(
    controllerId: Int?,
    graphId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var objectTypeId by remember(graphId) { mutableStateOf<Int?>(null) }
    var objectId by remember(graphId) { mutableStateOf<Long?>(null) }
    var changeTypeId by remember(graphId) { mutableStateOf<Int?>(null) }
    val rooms by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.roomDao().observeAll(controllerId)
        }
    }.collectAsState(initial = emptyList())
    val groups by remember(db) {
        db.groupDao().observeAll()
    }.collectAsState(initial = emptyList())
    val luminaires by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.luminaireDao().observeAllForController(controllerId)
        }
    }.collectAsState(initial = emptyList())
    val objectTypeItems = remember {
        listOf(
            DropdownItem(OBJECT_TYPE_LOCATION.toLong(), "Вся локация"),
            DropdownItem(OBJECT_TYPE_ROOM.toLong(), "Помещение"),
            DropdownItem(OBJECT_TYPE_GROUP.toLong(), "Группа"),
            DropdownItem(OBJECT_TYPE_LUMINAIRE.toLong(), "Светильник"),
        )
    }
    val changeTypeItems = remember {
        listOf(
            DropdownItem(CHANGE_TYPE_BRIGHTNESS.toLong(), "Яркость"),
            DropdownItem(CHANGE_TYPE_TEMPERATURE.toLong(), "Температура"),
        )
    }
    val roomNames = remember(rooms) {
        rooms.associate { it.id to it.name.ifBlank { "Помещение ${it.id + 1}" } }
    }
    val objectItems = remember(objectTypeId, rooms, groups, luminaires) {
        when (objectTypeId) {
            OBJECT_TYPE_ROOM -> {
                rooms.map { DropdownItem(it.id.toLong(), it.name.ifBlank { "Помещение ${it.id + 1}" }) }
            }
            OBJECT_TYPE_GROUP -> {
                groups.map { DropdownItem(it.id.toLong(), it.name.ifBlank { "Группа ${it.id + 1}" }) }
            }
            OBJECT_TYPE_LUMINAIRE -> {
                luminaires.map { luminaire ->
                    val roomTitle = roomNames[luminaire.roomId] ?: "Без помещения"
                    val luminaireTitle = luminaire.name.ifBlank { "Светильник ${luminaire.id}" }
                    DropdownItem(luminaire.id, "$roomTitle / $luminaireTitle")
                }
            }
            else -> emptyList()
        }
    }
    val objectPlaceholder = when {
        objectTypeId == null -> "Сначала выберите тип объекта"
        objectTypeId == OBJECT_TYPE_LOCATION -> "Для всей локации не требуется"
        objectItems.isEmpty() && objectTypeId == OBJECT_TYPE_ROOM -> "Нет помещений"
        objectItems.isEmpty() && objectTypeId == OBJECT_TYPE_GROUP -> "Нет групп"
        objectItems.isEmpty() && objectTypeId == OBJECT_TYPE_LUMINAIRE -> "Нет светильников"
        else -> "Не выбрано"
    }
    val saveAndBack: () -> Unit = saveAndBack@{
        val resolvedControllerId = controllerId
        if (resolvedControllerId == null) {
            onBackClick()
            return@saveAndBack
        }
        val resolvedObjectId = if (objectTypeId == OBJECT_TYPE_LOCATION) null else objectId
        val shouldPersist = graphId != null || objectTypeId != null || changeTypeId != null

        scope.launch {
            if (shouldPersist) {
                val entity = GraphEntity(
                    id = graphId ?: 0,
                    controllerId = resolvedControllerId,
                    objectTypeId = objectTypeId,
                    objectId = resolvedObjectId,
                    changeTypeId = changeTypeId,
                )
                if (graphId == null) {
                    db.graphDao().insert(entity)
                } else {
                    db.graphDao().update(entity)
                }
            }
            onBackClick()
        }
    }

    LaunchedEffect(graphId) {
        if (graphId == null) {
            objectTypeId = null
            objectId = null
            changeTypeId = null
            return@LaunchedEffect
        }

        val graph = db.graphDao().getById(graphId) ?: return@LaunchedEffect
        objectTypeId = graph.objectTypeId
        objectId = graph.objectId
        changeTypeId = graph.changeTypeId
    }

    BackHandler { saveAndBack() }

    PageContainer(
        title = "График",
        onBackClick = saveAndBack,
        isScrollable = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16),
        ) {
            TextFieldForList(
                value = objectTypeId?.toLong(),
                onValueChange = { value ->
                    objectTypeId = value.toInt()
                    objectId = null
                },
                icon = R.drawable.ic_chevron_down,
                label = "Тип объекта",
                placeholder = "Не выбрано",
                dropdownItems = objectTypeItems,
            )

            TextFieldForList(
                value = objectId,
                onValueChange = { objectId = it },
                icon = R.drawable.ic_chevron_down,
                label = "Объект",
                placeholder = objectPlaceholder,
                enabled = objectTypeId != null &&
                    objectTypeId != OBJECT_TYPE_LOCATION &&
                    objectItems.isNotEmpty(),
                dropdownItems = objectItems,
            )

            TextFieldForList(
                value = changeTypeId?.toLong(),
                onValueChange = { value -> changeTypeId = value.toInt() },
                icon = R.drawable.ic_chevron_down,
                label = "Что меняем",
                placeholder = "Не выбрано",
                dropdownItems = changeTypeItems,
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
        }
    }
}
