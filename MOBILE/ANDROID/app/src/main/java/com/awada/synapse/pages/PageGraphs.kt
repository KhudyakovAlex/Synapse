package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GraphEntity
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import com.awada.synapse.ui.theme.BodyLarge
import kotlinx.coroutines.flow.flowOf

private const val OBJECT_TYPE_LOCATION = 1
private const val OBJECT_TYPE_ROOM = 2
private const val OBJECT_TYPE_GROUP = 3
private const val OBJECT_TYPE_LUMINAIRE = 4

private const val CHANGE_TYPE_BRIGHTNESS = 1
private const val CHANGE_TYPE_TEMPERATURE = 2

@Composable
fun PageGraphs(
    controllerId: Int? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    var showGraph by remember { mutableStateOf(false) }
    var editingGraphId by remember { mutableStateOf<Long?>(null) }
    val graphs by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.graphDao().observeAllForController(controllerId)
        }
    }.collectAsState(initial = emptyList())
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
                        onClick = {
                            editingGraphId = graph.id
                            showGraph = true
                        },
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
}

@Composable
private fun GraphListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)

    Box(
        modifier = modifier
            .clip(shape)
            .background(PixsoColors.Color_Bg_bg_surface)
            .border(
                width = PixsoDimens.Stroke_S,
                color = PixsoColors.Color_Border_border_primary,
                shape = shape,
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(
                horizontal = PixsoDimens.Numeric_20,
                vertical = PixsoDimens.Numeric_16,
            ),
    ) {
        Text(
            text = title,
            style = BodyLarge,
            color = PixsoColors.Color_State_on_secondary,
            modifier = Modifier.fillMaxWidth(),
        )
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
