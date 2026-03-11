package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.SchedulePoint
import com.awada.synapse.components.ScheduleScenario
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.EventEntity
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun PageSchedule(
    controllerId: Int?,
    onBackClick: () -> Unit,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showSchedulePoint by remember { mutableStateOf(false) }
    var editingEventId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteEventId by remember { mutableStateOf<Long?>(null) }

    if (showSchedulePoint) {
        PageSchedulePoint(
            controllerId = controllerId,
            eventId = editingEventId,
            onBackClick = {
                editingEventId = null
                showSchedulePoint = false
            },
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val events by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf(emptyList())
        } else {
            db.eventDao().observeAllForController(controllerId)
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

    val openNewPoint: () -> Unit = {
        editingEventId = null
        showSchedulePoint = true
    }

    PageContainer(
        title = "Расписание",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            if (events.isEmpty()) {
                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_48))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Пунктов расписания нет",
                        style = TitleMedium,
                        color = PixsoColors.Color_Text_text_3_level,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            events.forEach { event ->
                EventSchedulePoint(
                    event = event,
                    rooms = rooms,
                    groups = groups,
                    luminaires = luminaires,
                    onOpen = {
                        editingEventId = event.id
                        showSchedulePoint = true
                    },
                    onRequestDelete = { pendingDeleteEventId = event.id },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = {
                    onAddClick?.invoke()
                    openNewPoint()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (pendingDeleteEventId != null) {
        Tooltip(
            text = "Удалить пункт?",
            primaryButtonText = "Удалить",
            secondaryButtonText = "Отмена",
            onResult = { result ->
                when (result) {
                    TooltipResult.Primary -> {
                        val eventIdToDelete = pendingDeleteEventId ?: return@Tooltip
                        pendingDeleteEventId = null
                        scope.launch {
                            db.eventDao().deleteById(eventIdToDelete)
                        }
                    }
                    TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                        pendingDeleteEventId = null
                    }
                }
            },
        )
    }
}

@Composable
private fun EventSchedulePoint(
    event: EventEntity,
    rooms: List<com.awada.synapse.db.RoomEntity>,
    groups: List<com.awada.synapse.db.GroupEntity>,
    luminaires: List<com.awada.synapse.db.LuminaireEntity>,
    onOpen: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val actions by remember(db, event.scenarioId) {
        if (event.scenarioId == EventEntity.NO_SCENARIO_ID) {
            flowOf(emptyList())
        } else {
            db.actionDao().observeAllForScenario(event.scenarioId)
        }
    }.collectAsState(initial = emptyList())
    val scenarioTitles = remember(actions, rooms, groups, luminaires) {
        actions.map { action ->
            ScheduleEventFormatter.buildScenarioActionTitle(
                action = action,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
            )
        }
    }
    val scenarioRows = if (scenarioTitles.isEmpty()) {
        listOf(ScheduleScenario(text = "Сценарий не выбран", onClick = onOpen, enabled = false))
    } else {
        scenarioTitles.map { title ->
            ScheduleScenario(text = title, onClick = onOpen)
        }
    }

    SchedulePoint(
        timeText = ScheduleEventFormatter.formatTime(event.time),
        days = ScheduleEventFormatter.buildDayLabels(event.days),
        scenarios = scenarioRows,
        modifier = modifier,
        onClick = onOpen,
        onLongClick = onRequestDelete,
    )
}

