package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.vibrateStrongClick
import com.awada.synapse.db.ActionEntity
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GroupEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.ScenarioEntity
import com.awada.synapse.db.ScenarioSetEntity
import com.awada.synapse.ui.theme.ButtonSmall
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val BUTTON_SETTINGS_OBJECT_TYPE_LOCATION = 1
private const val BUTTON_SETTINGS_OBJECT_TYPE_ROOM = 2
private const val BUTTON_SETTINGS_OBJECT_TYPE_GROUP = 3
private const val BUTTON_SETTINGS_OBJECT_TYPE_LUMINAIRE = 4

@Composable
fun PageButtonSettings(
    buttonPanelId: Long?,
    buttonNumber: Int?,
    onBackClick: () -> Unit,
    onScenarioClick: (scenarioId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val resolvedButtonNumber = buttonNumber ?: 1
    val scenarioBlockHeights = remember { mutableStateMapOf<Long, Int>() }
    var draggingScenarioSetId by remember { mutableStateOf<Long?>(null) }
    var shortPressDragDelta by remember { mutableStateOf(Offset.Zero) }
    var pendingDeleteScenarioSetId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteLongPress by remember { mutableStateOf(false) }
    var suppressClickScenarioSetId by remember { mutableStateOf<Long?>(null) }
    var suppressClickScenarioSetToken by remember { mutableStateOf(0) }
    var suppressClickLongPress by remember { mutableStateOf(false) }
    var suppressClickLongPressToken by remember { mutableStateOf(0) }

    val button by remember(db, buttonPanelId, resolvedButtonNumber) {
        if (buttonPanelId == null) {
            flowOf(null)
        } else {
            db.buttonDao().observeByPanelAndNumber(buttonPanelId, resolvedButtonNumber)
        }
    }.collectAsState(initial = null)

    val shortPressScenarios by remember(db, button?.id) {
        val buttonId = button?.id
        if (buttonId == null) {
            flowOf(emptyList())
        } else {
            db.scenarioSetDao().observeAllForButton(buttonId)
        }
    }.collectAsState(initial = emptyList())
    val buttonPanel by remember(db, buttonPanelId) {
        if (buttonPanelId == null) {
            flowOf(null)
        } else {
            db.buttonPanelDao().observeById(buttonPanelId)
        }
    }.collectAsState(initial = null)
    val controllerId = buttonPanel?.controllerId
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

    LaunchedEffect(draggingScenarioSetId) {
        if (draggingScenarioSetId == null) {
            shortPressDragDelta = Offset.Zero
        }
    }

    BackHandler(onBack = onBackClick)

    PageContainer(
        title = "Кнопка $resolvedButtonNumber",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScenarioSection(
                title = "Короткое нажатие\n(сценарии ниже будут перебираться по очереди)",
                scenarioSets = shortPressScenarios,
                draggingScenarioSetId = draggingScenarioSetId,
                dragDelta = shortPressDragDelta,
                suppressClickScenarioSetId = suppressClickScenarioSetId,
                scenarioBlockHeights = scenarioBlockHeights,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
                onScenarioClick = onScenarioClick,
                onDragStart = { scenarioSetId ->
                    draggingScenarioSetId = scenarioSetId
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vibrateStrongClick(context)
                    suppressClickScenarioSetToken += 1
                    val token = suppressClickScenarioSetToken
                    suppressClickScenarioSetId = scenarioSetId
                    scope.launch {
                        delay(250)
                        if (suppressClickScenarioSetToken == token) {
                            suppressClickScenarioSetId = null
                        }
                    }
                },
                onDragDeltaChange = { shortPressDragDelta = it },
                onDragEnd = { draggingScenarioSetId = null },
                onMove = { reordered ->
                    scope.launch {
                        persistScenarioSetOrder(db, reordered)
                    }
                },
                onRequestDelete = { scenarioSetId ->
                    pendingDeleteScenarioSetId = scenarioSetId
                },
                onAdd = {
                    val buttonId = button?.id ?: return@ScenarioSection
                    scope.launch {
                        val scenarioId = db.scenarioDao().insert(ScenarioEntity())
                        val position = db.scenarioSetDao().getNextPositionForButton(buttonId)
                        db.scenarioSetDao().insert(
                            ScenarioSetEntity(
                                buttonId = buttonId,
                                position = position,
                                scenarioId = scenarioId,
                            )
                        )
                        onScenarioClick(scenarioId)
                    }
                },
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_8))

            ScenarioSectionSingle(
                title = "Длинное нажатие\n(плавное изменение к сценарию)",
                scenarioId = button?.longPressScenarioId,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
                onScenarioClick = onScenarioClick,
                suppressClick = suppressClickLongPress,
                onRequestDelete = {
                    suppressClickLongPressToken += 1
                    val token = suppressClickLongPressToken
                    suppressClickLongPress = true
                    scope.launch {
                        delay(250)
                        if (suppressClickLongPressToken == token) {
                            suppressClickLongPress = false
                        }
                    }
                    pendingDeleteLongPress = true
                },
                onAdd = {
                    val buttonId = button?.id ?: return@ScenarioSectionSingle
                    scope.launch {
                        val existingScenarioId = button?.longPressScenarioId
                        val scenarioId = if (existingScenarioId != null) {
                            existingScenarioId
                        } else {
                            db.scenarioDao().insert(ScenarioEntity()).also {
                                db.buttonDao().setLongPressScenarioId(buttonId, it)
                            }
                        }
                        onScenarioClick(scenarioId)
                    }
                },
            )
        }
    }

    if (pendingDeleteScenarioSetId != null) {
        Tooltip(
            text = "Удалить сценарий?",
            primaryButtonText = "Удалить",
            secondaryButtonText = "Отмена",
            onResult = { result ->
                when (result) {
                    TooltipResult.Primary -> {
                        val scenarioSetId = pendingDeleteScenarioSetId ?: return@Tooltip
                        val remaining = shortPressScenarios.filter { it.id != scenarioSetId }
                        pendingDeleteScenarioSetId = null
                        scope.launch {
                            db.scenarioSetDao().deleteById(scenarioSetId)
                            persistScenarioSetOrder(db, remaining)
                        }
                    }
                    TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                        pendingDeleteScenarioSetId = null
                    }
                }
            },
        )
    }

    if (pendingDeleteLongPress) {
        Tooltip(
            text = "Удалить сценарий?",
            primaryButtonText = "Удалить",
            secondaryButtonText = "Отмена",
            onResult = { result ->
                when (result) {
                    TooltipResult.Primary -> {
                        val buttonId = button?.id
                        pendingDeleteLongPress = false
                        if (buttonId != null) {
                            scope.launch {
                                db.buttonDao().setLongPressScenarioId(buttonId, null)
                            }
                        }
                    }
                    TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                        pendingDeleteLongPress = false
                    }
                }
            },
        )
    }
}

@Composable
private fun ScenarioSection(
    title: String,
    scenarioSets: List<ScenarioSetEntity>,
    draggingScenarioSetId: Long?,
    dragDelta: Offset,
    suppressClickScenarioSetId: Long?,
    scenarioBlockHeights: MutableMap<Long, Int>,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    onScenarioClick: (Long) -> Unit,
    onDragStart: (Long) -> Unit,
    onDragDeltaChange: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onMove: (List<ScenarioSetEntity>) -> Unit,
    onRequestDelete: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SupportingText(text = title)

        ReorderableScenarioSetList(
            scenarioSets = scenarioSets,
            draggingScenarioSetId = draggingScenarioSetId,
            dragDelta = dragDelta,
            suppressClickScenarioSetId = suppressClickScenarioSetId,
            scenarioBlockHeights = scenarioBlockHeights,
            rooms = rooms,
            groups = groups,
            luminaires = luminaires,
            onScenarioClick = onScenarioClick,
            onDragStart = onDragStart,
            onDragDeltaChange = onDragDeltaChange,
            onDragEnd = onDragEnd,
            onMove = onMove,
            onRequestDelete = onRequestDelete,
            modifier = Modifier.fillMaxWidth(),
        )

        SecondaryButton(
            text = "Добавить",
            onClick = onAdd,
        )
    }
}

@Composable
private fun ScenarioSectionSingle(
    title: String,
    scenarioId: Long?,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    onScenarioClick: (Long) -> Unit,
    suppressClick: Boolean,
    onRequestDelete: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SupportingText(text = title)

        if (scenarioId != null) {
            LongPressDeleteScenarioSummary(
                scenarioId = scenarioId,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
                onScenarioClick = onScenarioClick,
                suppressClick = suppressClick,
                onRequestDelete = onRequestDelete,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (scenarioId == null) {
            SecondaryButton(
                text = "Добавить",
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun SupportingText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = BodyLarge,
        color = PixsoColors.Color_Text_text_1_level,
        modifier = modifier,
    )
}

@Composable
private fun ScenarioSummaryBlock(
    scenarioId: Long,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    onScenarioClick: (Long) -> Unit,
    clickEnabled: Boolean = true,
    highlighted: Boolean = false,
    onMeasured: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val actions by remember(db, scenarioId) {
        db.actionDao().observeAllForScenario(scenarioId)
    }.collectAsState(initial = emptyList())
    val actionTitles = remember(actions, rooms, groups, luminaires) {
        actions.map { action ->
            buildButtonScenarioActionTitle(
                action = action,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
            )
        }
    }
    val summaryRows = if (actionTitles.isEmpty()) listOf("Действие") else actionTitles
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)

    Column(
        modifier = modifier
            .then(
                if (onMeasured != null) {
                    Modifier.onGloballyPositioned { onMeasured(it.size.height) }
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(
                if (highlighted) PixsoColors.Color_State_primary_pressed
                else PixsoColors.Color_Bg_bg_surface
            )
            .border(
                width = PixsoDimens.Stroke_S,
                color = if (highlighted) {
                    PixsoColors.Color_State_primary_pressed
                } else {
                    PixsoColors.Color_Border_border_primary
                },
                shape = shape,
            )
            .then(
                if (clickEnabled) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource()
                        },
                        onClick = { onScenarioClick(scenarioId) },
                    )
                } else {
                    Modifier
                }
            )
            .padding(PixsoDimens.Numeric_8),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8),
    ) {
        summaryRows.forEach { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(
                        if (highlighted) PixsoColors.Color_State_primary_pressed
                        else PixsoColors.Color_Bg_bg_surface
                    )
                    .border(
                        width = PixsoDimens.Stroke_S,
                        color = if (highlighted) {
                            PixsoColors.Color_State_primary_pressed
                        } else {
                            PixsoColors.Color_Border_border_primary
                        },
                        shape = shape,
                    )
                    .padding(
                        horizontal = PixsoDimens.Numeric_12,
                        vertical = PixsoDimens.Numeric_8,
                    ),
            ) {
                Text(
                    text = title,
                    style = ButtonSmall,
                    color = if (highlighted) {
                        PixsoColors.Color_State_on_primary
                    } else {
                        PixsoColors.Color_State_on_secondary
                    },
                )
            }
        }
    }
}

@Composable
private fun ReorderableScenarioSetList(
    scenarioSets: List<ScenarioSetEntity>,
    draggingScenarioSetId: Long?,
    dragDelta: Offset,
    suppressClickScenarioSetId: Long?,
    scenarioBlockHeights: MutableMap<Long, Int>,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    onScenarioClick: (Long) -> Unit,
    onDragStart: (Long) -> Unit,
    onDragDeltaChange: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onMove: (List<ScenarioSetEntity>) -> Unit,
    onRequestDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scenarioSetsState = rememberUpdatedState(scenarioSets)
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val spacingPx = with(density) { PixsoDimens.Numeric_8.toPx() }
    val fallbackHeightPx = with(density) { 104.dp.toPx() }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        var currentTop = 0f
        val slotTops = scenarioSets.map { item ->
            val top = currentTop
            val height = scenarioBlockHeights[item.id]?.toFloat() ?: fallbackHeightPx
            currentTop += height + spacingPx
            top
        }
        val slotHeights = scenarioSets.map { item -> scenarioBlockHeights[item.id]?.toFloat() ?: fallbackHeightPx }
        val contentHeightPx = if (scenarioSets.isEmpty()) 0f else currentTop - spacingPx
        val contentHeightDp = with(density) { contentHeightPx.toDp() }
        val slotRects = scenarioSets.indices.map { index ->
            Rect(
                offset = Offset(0f, slotTops[index]),
                size = androidx.compose.ui.geometry.Size(widthPx, slotHeights[index]),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .pointerInput(scenarioSets) {
                    if (scenarioSets.isEmpty()) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        down.consume()

                        val startItem = scenarioSetsState.value.getOrNull(startIndex) ?: return@awaitEachGesture
                        onDragStart(startItem.id)

                        var moved = false
                        var hoverIndex = startIndex
                        var lastPos = down.position
                        var currentDragDelta = Offset.Zero
                        onDragDeltaChange(Offset.Zero)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                            if (!change.pressed) {
                                change.consume()
                                break
                            }

                            val delta = change.position - change.previousPosition
                            lastPos = change.position
                            if (delta != Offset.Zero) {
                                val newDelta = currentDragDelta + delta
                                currentDragDelta = newDelta
                                onDragDeltaChange(newDelta)
                                if (!moved && newDelta.getDistance() > viewConfig.touchSlop) moved = true

                                val centerY = slotTops[startIndex] + newDelta.y + slotHeights[startIndex] / 2f
                                var best = hoverIndex
                                var bestDistance = Float.MAX_VALUE
                                slotTops.indices.forEach { index ->
                                    val slotCenter = slotTops[index] + slotHeights[index] / 2f
                                    val distance = abs(centerY - slotCenter)
                                    if (distance < bestDistance) {
                                        bestDistance = distance
                                        best = index
                                    }
                                }
                                hoverIndex = best
                            }

                            change.consume()
                        }

                        val finalOrder = scenarioSetsState.value
                        onDragEnd()

                        if (!moved) {
                            onRequestDelete(startItem.id)
                            return@awaitEachGesture
                        }

                        val from = finalOrder.indexOfFirst { it.id == startItem.id }
                        if (from == -1) return@awaitEachGesture

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val to = if (dropOver != -1) dropOver else hoverIndex
                        if (to == from) {
                            return@awaitEachGesture
                        }

                        val reordered = finalOrder.toMutableList()
                        val item = reordered.removeAt(from)
                        reordered.add(to.coerceIn(0, reordered.size), item)
                        onMove(reordered)
                    }
                },
        ) {
            scenarioSets.forEachIndexed { index, scenarioSet ->
                key(scenarioSet.id) {
                    val top = slotTops[index]
                    val isDragging = scenarioSet.id == draggingScenarioSetId
                    val animatedOffset by animateIntOffsetAsState(
                        targetValue = IntOffset(0, top.roundToInt()),
                        animationSpec = tween(durationMillis = 220),
                        label = "buttonScenarioSetOffset",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                if (isDragging) {
                                    IntOffset(0, (top + dragDelta.y).roundToInt())
                                } else {
                                    animatedOffset
                                }
                            }
                            .zIndex(if (isDragging) 10f else 0f)
                            .graphicsLayer {
                                if (isDragging) alpha = 0f
                            },
                    ) {
                        ScenarioSummaryBlock(
                            scenarioId = scenarioSet.scenarioId,
                            rooms = rooms,
                            groups = groups,
                            luminaires = luminaires,
                            onScenarioClick = onScenarioClick,
                            clickEnabled = suppressClickScenarioSetId != scenarioSet.id,
                            highlighted = isDragging,
                            onMeasured = { scenarioBlockHeights[scenarioSet.id] = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(unbounded = true),
                        )
                    }
                }
            }

            val draggedIndex = scenarioSets.indexOfFirst { it.id == draggingScenarioSetId }
            val draggedItem = scenarioSets.getOrNull(draggedIndex)
            if (draggedIndex != -1 && draggedItem != null) {
                ScenarioSummaryBlock(
                    scenarioId = draggedItem.scenarioId,
                    rooms = rooms,
                    groups = groups,
                    luminaires = luminaires,
                    onScenarioClick = onScenarioClick,
                    clickEnabled = false,
                    highlighted = true,
                    onMeasured = { scenarioBlockHeights[draggedItem.id] = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(unbounded = true)
                        .offset { IntOffset(0, (slotTops[draggedIndex] + dragDelta.y).roundToInt()) }
                        .zIndex(20f),
                )
            }
        }
    }
}

@Composable
private fun LongPressDeleteScenarioSummary(
    scenarioId: Long,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    onScenarioClick: (Long) -> Unit,
    suppressClick: Boolean,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.pointerInput(scenarioId) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                down.consume()

                var moved = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                    if (!change.pressed) {
                        change.consume()
                        break
                    }
                    if (change.position != change.previousPosition) {
                        moved = true
                    }
                    change.consume()
                }
                if (!moved) {
                    onRequestDelete()
                }
            }
        }
    ) {
        ScenarioSummaryBlock(
            scenarioId = scenarioId,
            rooms = rooms,
            groups = groups,
            luminaires = luminaires,
            onScenarioClick = onScenarioClick,
            clickEnabled = !suppressClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun buildButtonScenarioActionTitle(
    action: ActionEntity,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
): String {
    val roomNames = rooms.associate { it.id to it.name.ifBlank { "Помещение ${it.id + 1}" } }
    val objectTitle = when (action.objectTypeId) {
        BUTTON_SETTINGS_OBJECT_TYPE_LOCATION -> "Вся локация"
        BUTTON_SETTINGS_OBJECT_TYPE_ROOM -> {
            val roomId = action.objectId?.toInt()
            rooms.firstOrNull { it.id == roomId }?.name?.ifBlank { "Помещение ${(roomId ?: 0) + 1}" }
        }
        BUTTON_SETTINGS_OBJECT_TYPE_GROUP -> {
            val groupId = action.objectId?.toInt()
            groups.firstOrNull { it.id == groupId }?.name?.ifBlank { "Группа ${(groupId ?: 0) + 1}" }
        }
        BUTTON_SETTINGS_OBJECT_TYPE_LUMINAIRE -> {
            luminaires.firstOrNull { it.id == action.objectId }?.let { luminaire ->
                val roomTitle = roomNames[luminaire.roomId] ?: "Без помещения"
                val luminaireTitle = luminaire.name.ifBlank { "Светильник ${luminaire.id}" }
                "$roomTitle / $luminaireTitle"
            }
        }
        else -> null
    }
    val changeValueTitle = when (action.changeTypeId) {
        1 -> when (action.changeValueId) {
            0 -> "Сцена Выкл"
            1 -> "Сцена 1"
            2 -> "Сцена 2"
            3 -> "Сцена 3"
            4 -> "Сцена Вкл"
            else -> null
        }
        2 -> when (action.changeValueId) {
            0 -> "Выключить АВТО"
            1 -> "Включить АВТО"
            else -> null
        }
        else -> null
    }

    return if (!objectTitle.isNullOrBlank() && !changeValueTitle.isNullOrBlank()) {
        "$objectTitle - $changeValueTitle"
    } else {
        "Действие"
    }
}

private suspend fun persistScenarioSetOrder(
    db: AppDatabase,
    orderedItems: List<ScenarioSetEntity>,
) {
    val dao = db.scenarioSetDao()
    orderedItems.forEachIndexed { index, item ->
        dao.update(item.copy(position = 1_000_000 + index))
    }
    orderedItems.forEachIndexed { index, item ->
        dao.update(item.copy(position = index))
    }
}
