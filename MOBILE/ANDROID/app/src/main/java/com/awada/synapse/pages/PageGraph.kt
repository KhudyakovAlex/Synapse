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
import com.awada.synapse.components.Graph
import com.awada.synapse.components.GraphPoint
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GraphEntity
import com.awada.synapse.db.GraphPointEntity
import com.awada.synapse.ui.theme.BodyMedium
import com.awada.synapse.ui.theme.PixsoColors
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
    var graphPoints by remember(graphId) { mutableStateOf(defaultGraphPoints()) }
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
    val graphValueRange = valueRangeForChangeType(changeTypeId)
    val saveAndBack: () -> Unit = saveAndBack@{
        val resolvedControllerId = controllerId
        if (resolvedControllerId == null) {
            onBackClick()
            return@saveAndBack
        }
        val resolvedObjectId = if (objectTypeId == OBJECT_TYPE_LOCATION) null else objectId
        val normalizedPoints = normalizeGraphPoints(
            points = graphPoints,
            valueRange = graphValueRange,
        )
        val hasCustomPoints = normalizedPoints.size > 1 ||
            normalizedPoints.firstOrNull()?.value != graphValueRange.first
        val shouldPersist = graphId != null ||
            objectTypeId != null ||
            changeTypeId != null ||
            hasCustomPoints

        scope.launch {
            if (shouldPersist) {
                val entity = GraphEntity(
                    id = graphId ?: 0,
                    controllerId = resolvedControllerId,
                    objectTypeId = objectTypeId,
                    objectId = resolvedObjectId,
                    changeTypeId = changeTypeId,
                )
                val persistedGraphId = if (graphId == null) {
                    db.graphDao().insert(entity)
                } else {
                    db.graphDao().update(entity)
                    graphId
                }
                db.graphPointDao().deleteAllForGraph(persistedGraphId)
                normalizedPoints.forEach { point ->
                    db.graphPointDao().insert(
                        GraphPointEntity(
                            graphId = persistedGraphId,
                            time = point.time,
                            value = point.value,
                        )
                    )
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
            graphPoints = defaultGraphPoints()
            return@LaunchedEffect
        }

        val graph = db.graphDao().getById(graphId) ?: return@LaunchedEffect
        objectTypeId = graph.objectTypeId
        objectId = graph.objectId
        changeTypeId = graph.changeTypeId
        val valueRange = valueRangeForChangeType(graph.changeTypeId)
        graphPoints = db.graphPointDao()
            .getAllForGraph(graphId)
            .map { point ->
                GraphPoint(
                    id = point.id,
                    time = point.time,
                    value = point.value,
                )
            }
            .let { loadedPoints ->
                normalizeGraphPoints(
                    points = loadedPoints,
                    valueRange = valueRange,
                )
            }
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
                onValueChange = { value ->
                    changeTypeId = value.toInt()
                    graphPoints = normalizeGraphPoints(
                        points = graphPoints,
                        valueRange = valueRangeForChangeType(changeTypeId),
                    )
                },
                icon = R.drawable.ic_chevron_down,
                label = "Что меняем",
                placeholder = "Не выбрано",
                dropdownItems = changeTypeItems,
            )

            if (changeTypeId != null) {
                Graph(
                    points = graphPoints,
                    valueRange = graphValueRange,
                    valueFormatter = { value ->
                        when (changeTypeId) {
                            CHANGE_TYPE_TEMPERATURE -> "${value}K"
                            else -> "$value%"
                        }
                    },
                    onPointsChange = { updatedPoints ->
                        graphPoints = normalizeGraphPoints(
                            points = updatedPoints,
                            valueRange = graphValueRange,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Чтобы настроить график, сначала выберите, что именно меняем.",
                    style = BodyMedium,
                    color = PixsoColors.Color_Text_text_3_level,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
        }
    }
}

private fun valueRangeForChangeType(changeTypeId: Int?): IntRange {
    return when (changeTypeId) {
        CHANGE_TYPE_TEMPERATURE -> 3000..5000
        else -> 0..100
    }
}

private fun defaultGraphPoints(): List<GraphPoint> {
    return listOf(
        GraphPoint(
            id = -1L,
            time = "0000",
            value = 0,
        )
    )
}

private fun normalizeGraphPoints(
    points: List<GraphPoint>,
    valueRange: IntRange,
): List<GraphPoint> {
    val uniqueByMinute = linkedMapOf<Int, GraphPoint>()

    points.forEach { point ->
        val minute = timeToMinuteOfDay(point.time)
        uniqueByMinute[minute] = GraphPoint(
            id = point.id,
            time = minuteOfDayToTime(minute),
            value = point.value.coerceIn(valueRange.first, valueRange.last),
        )
    }

    if (0 !in uniqueByMinute) {
        val temporaryId = (points.minOfOrNull(GraphPoint::id) ?: 0L).coerceAtMost(0L) - 1L
        uniqueByMinute[0] = GraphPoint(
            id = temporaryId,
            time = "0000",
            value = valueRange.first,
        )
    }

    return uniqueByMinute
        .toSortedMap()
        .values
        .toList()
}

private fun timeToMinuteOfDay(raw: String): Int {
    val digits = raw.filter(Char::isDigit).padEnd(4, '0').take(4)
    val hours = digits.substring(0, 2).toIntOrNull()?.coerceIn(0, 23) ?: 0
    val minutes = digits.substring(2, 4).toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hours * 60 + minutes
}

private fun minuteOfDayToTime(minuteOfDay: Int): String {
    val clamped = minuteOfDay.coerceIn(0, 23 * 60 + 59)
    val hours = clamped / 60
    val minutes = clamped % 60
    return "%02d%02d".format(hours, minutes)
}
