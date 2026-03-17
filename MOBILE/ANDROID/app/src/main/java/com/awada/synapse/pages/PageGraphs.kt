package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.awada.synapse.components.GraphPoint
import com.awada.synapse.components.GraphPreview
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GraphEntity
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val OBJECT_TYPE_LOCATION = 1
private const val OBJECT_TYPE_ROOM = 2
private const val OBJECT_TYPE_GROUP = 3
private const val OBJECT_TYPE_LUMINAIRE = 4

private const val CHANGE_TYPE_BRIGHTNESS = 1
private const val CHANGE_TYPE_TEMPERATURE = 2

/** Вложенная страница списка графиков и перехода к редактированию отдельного графика. */
internal val PageGraphsLlmDescriptor = LLMPageDescriptor(
    fileName = "PageGraphs",
    titleRu = "Графики",
    description = "Показывает список графиков выбранного контроллера и позволяет открыть или создать график."
)

@Composable
fun PageGraphs(
    controllerId: Int? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var showGraph by remember { mutableStateOf(false) }
    var editingGraphId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteGraphId by remember { mutableStateOf<Long?>(null) }
    val graphs by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.graphDao().observeAllForController(controllerId)
        }
    }.collectAsState(initial = emptyList())
    val graphPointsByGraphId by remember(db, graphs) {
        if (graphs.isEmpty()) {
            flowOf<Map<Long, List<GraphPoint>>>(emptyMap())
        } else {
            db.graphPointDao()
                .observeAllForGraphs(graphs.map(GraphEntity::id))
                .map { points ->
                    points
                        .groupBy { it.graphId }
                        .mapValues { (_, graphPoints) ->
                            graphPoints.map { point ->
                                GraphPoint(
                                    id = point.id,
                                    time = point.time,
                                    value = point.value,
                                )
                            }
                        }
                }
        }
    }.collectAsState(initial = emptyMap())
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

    if (showGraph) {
        PageGraph(
            controllerId = controllerId,
            graphId = editingGraphId,
            onBackClick = { showGraph = false },
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    PageContainer(
        title = "Графики\nяркости и температуры",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            if (graphs.isEmpty()) {
                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_48))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Графиков нет",
                        style = TitleMedium,
                        color = PixsoColors.Color_Text_text_3_level,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                graphs.forEach { graph ->
                    GraphListItem(
                        title = buildGraphTitle(
                            graph = graph,
                            rooms = rooms,
                            groups = groups,
                            luminaires = luminaires,
                        ),
                        previewPoints = graphPointsByGraphId[graph.id].orEmpty(),
                        previewValueRange = valueRangeForGraphChangeType(graph.changeTypeId),
                        onClick = {
                            editingGraphId = graph.id
                            showGraph = true
                        },
                        onLongClick = { pendingDeleteGraphId = graph.id },
                        isPressed = pendingDeleteGraphId == graph.id,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = {
                    editingGraphId = null
                    showGraph = true
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (pendingDeleteGraphId != null) {
        Tooltip(
            text = "Удалить график?",
            primaryButtonText = "Удалить",
            secondaryButtonText = "Отмена",
            onResult = { result ->
                when (result) {
                    TooltipResult.Primary -> {
                        val graphIdToDelete = pendingDeleteGraphId ?: return@Tooltip
                        pendingDeleteGraphId = null
                        scope.launch {
                            db.graphDao().deleteById(graphIdToDelete)
                        }
                    }
                    TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                        pendingDeleteGraphId = null
                    }
                }
            },
        )
    }
}

@Composable
private fun GraphListItem(
    title: String,
    previewPoints: List<GraphPoint>,
    previewValueRange: IntRange,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)

    Box(
        modifier = modifier
            .graphicsLayer {
                if (isPressed) {
                    scaleX = 0.995f
                    scaleY = 0.995f
                }
            }
            .clip(shape)
            .background(
                if (isPressed) {
                    PixsoColors.Color_State_primary_pressed
                } else {
                    PixsoColors.Color_Bg_bg_surface
                }
            )
            .border(
                width = PixsoDimens.Stroke_S,
                color = if (isPressed) {
                    PixsoColors.Color_State_primary_pressed
                } else {
                    PixsoColors.Color_Border_border_primary
                },
                shape = shape,
            )
            .pointerInput(onClick, onLongClick) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() },
                )
            }
            .padding(
                horizontal = PixsoDimens.Numeric_20,
                vertical = PixsoDimens.Numeric_16,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            Text(
                text = title,
                style = BodyLarge,
                color = if (isPressed) {
                    PixsoColors.Color_State_on_primary
                } else {
                    PixsoColors.Color_State_on_secondary
                },
                modifier = Modifier.fillMaxWidth(),
            )
            GraphPreview(
                points = previewPoints,
                valueRange = previewValueRange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun buildGraphTitle(
    graph: GraphEntity,
    rooms: List<com.awada.synapse.db.RoomEntity>,
    groups: List<com.awada.synapse.db.GroupEntity>,
    luminaires: List<com.awada.synapse.db.LuminaireEntity>,
): String {
    val roomNames = rooms.associate { it.id to it.name.ifBlank { "Помещение ${it.id + 1}" } }
    val objectTitle = when (graph.objectTypeId) {
        OBJECT_TYPE_LOCATION -> "Вся локация"
        OBJECT_TYPE_ROOM -> {
            val roomId = graph.objectId?.toInt()
            rooms.firstOrNull { it.id == roomId }?.name?.ifBlank { "Помещение ${(roomId ?: 0) + 1}" }
        }
        OBJECT_TYPE_GROUP -> {
            val groupId = graph.objectId?.toInt()
            groups.firstOrNull { it.id == groupId }?.name?.ifBlank { "Группа ${(groupId ?: 0) + 1}" }
        }
        OBJECT_TYPE_LUMINAIRE -> {
            luminaires.firstOrNull { it.id == graph.objectId }?.let { luminaire ->
                val roomTitle = roomNames[luminaire.roomId] ?: "Без помещения"
                val luminaireTitle = luminaire.name.ifBlank { "Светильник ${luminaire.id}" }
                "$roomTitle / $luminaireTitle"
            }
        }
        else -> null
    }
    val changeTitle = when (graph.changeTypeId) {
        CHANGE_TYPE_BRIGHTNESS -> "Яркость"
        CHANGE_TYPE_TEMPERATURE -> "Температура"
        else -> null
    }

    return if (!objectTitle.isNullOrBlank() && !changeTitle.isNullOrBlank()) {
        "$objectTitle - $changeTitle"
    } else {
        "График"
    }
}

private fun valueRangeForGraphChangeType(changeTypeId: Int?): IntRange {
    return when (changeTypeId) {
        CHANGE_TYPE_TEMPERATURE -> 3000..5000
        else -> 0..100
    }
}
