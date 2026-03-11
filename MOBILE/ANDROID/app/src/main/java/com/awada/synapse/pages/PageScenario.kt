package com.awada.synapse.pages

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.ScenarioPoint
import com.awada.synapse.components.ScenarioPointField
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.vibrateStrongClick
import com.awada.synapse.db.ActionDao
import com.awada.synapse.db.ActionEntity
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.GroupEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val OBJECT_TYPE_LOCATION = 1
private const val OBJECT_TYPE_ROOM = 2
private const val OBJECT_TYPE_GROUP = 3
private const val OBJECT_TYPE_LUMINAIRE = 4

private const val CHANGE_TYPE_SCENE = 1
private const val CHANGE_TYPE_AUTO = 2

@Composable
fun PageScenario(
    scenarioId: Long?,
    buttonPanelId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val buttonPanel by remember(db, buttonPanelId) {
        if (buttonPanelId == null) {
            flowOf(null)
        } else {
            db.buttonPanelDao().observeById(buttonPanelId)
        }
    }.collectAsState(initial = null)
    val controllerId = buttonPanel?.controllerId
    val actions by remember(db, scenarioId) {
        if (scenarioId == null) {
            flowOf(emptyList())
        } else {
            db.actionDao().observeAllForScenario(scenarioId)
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
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }
    val actionHeights = remember { mutableStateMapOf<Long, Int>() }
    var draggingActionId by remember { mutableStateOf<Long?>(null) }
    var pressedActionId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteActionId by remember { mutableStateOf<Long?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }

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
            DropdownItem(CHANGE_TYPE_SCENE.toLong(), "Включение световой сцены"),
            DropdownItem(CHANGE_TYPE_AUTO.toLong(), "Управление АВТО"),
        )
    }
    val sceneValueItems = remember {
        listOf(
            DropdownItem(0, "Сцена Выкл"),
            DropdownItem(1, "Сцена 1"),
            DropdownItem(2, "Сцена 2"),
            DropdownItem(3, "Сцена 3"),
            DropdownItem(4, "Сцена Вкл"),
        )
    }
    val autoValueItems = remember {
        listOf(
            DropdownItem(0, "Выключить"),
            DropdownItem(1, "Включить"),
        )
    }

    LaunchedEffect(draggingActionId) {
        if (draggingActionId == null) {
            dragDelta = Offset.Zero
        }
    }

    PageContainer(
        title = "Сценарий",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))

            ReorderableScenarioActionsList(
                actions = actions,
                draggingActionId = draggingActionId,
                pendingDeleteVisible = pendingDeleteActionId != null,
                actionHeights = actionHeights,
                expandedStates = expandedStates,
                dragDelta = dragDelta,
                rooms = rooms,
                groups = groups,
                luminaires = luminaires,
                objectTypeItems = objectTypeItems,
                changeTypeItems = changeTypeItems,
                sceneValueItems = sceneValueItems,
                autoValueItems = autoValueItems,
                onDragStart = { actionId ->
                    draggingActionId = actionId
                    pressedActionId = actionId
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vibrateStrongClick(context)
                },
                onDragDeltaChange = { dragDelta = it },
                onDragEnd = { draggingActionId = null },
                onRequestDelete = { actionId ->
                    pendingDeleteActionId = actionId
                    pressedActionId = actionId
                },
                onClearPressed = { pressedActionId = null },
                onMove = { reordered ->
                    scope.launch {
                        persistActionOrder(db.actionDao(), reordered)
                    }
                },
                onObjectTypeChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(
                            action.copy(
                                objectTypeId = value?.toInt(),
                                objectId = null,
                            )
                        )
                    }
                },
                onObjectChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(action.copy(objectId = value))
                    }
                },
                onChangeTypeChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(
                            action.copy(
                                changeTypeId = value?.toInt(),
                                changeValueId = null,
                            )
                        )
                    }
                },
                onChangeValueChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(action.copy(changeValueId = value?.toInt()))
                    }
                },
                isPressed = { actionId -> actionId == pressedActionId },
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = {
                    val resolvedScenarioId = scenarioId ?: return@PrimaryIconButtonLarge
                    scope.launch {
                        val nextPosition = db.actionDao().getNextPositionForScenario(resolvedScenarioId)
                        val newActionId = db.actionDao().insert(
                            ActionEntity(
                                scenarioId = resolvedScenarioId,
                                position = nextPosition,
                            )
                        )
                        expandedStates[newActionId] = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (pendingDeleteActionId != null) {
        val actionId = pendingDeleteActionId!!
        Tooltip(
            text = "Удалить действие?",
            primaryButtonText = "Удалить",
            secondaryButtonText = "Отмена",
            onResult = { result ->
                when (result) {
                    TooltipResult.Primary -> {
                        val remaining = actions.filter { it.id != actionId }
                        pendingDeleteActionId = null
                        pressedActionId = null
                        scope.launch {
                            db.actionDao().deleteById(actionId)
                            persistActionOrder(db.actionDao(), remaining)
                        }
                    }
                    TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                        pendingDeleteActionId = null
                        pressedActionId = null
                    }
                }
            },
        )
    }
}

@Composable
private fun ReorderableScenarioActionsList(
    actions: List<ActionEntity>,
    draggingActionId: Long?,
    pendingDeleteVisible: Boolean,
    actionHeights: MutableMap<Long, Int>,
    expandedStates: MutableMap<Long, Boolean>,
    dragDelta: Offset,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    objectTypeItems: List<DropdownItem>,
    changeTypeItems: List<DropdownItem>,
    sceneValueItems: List<DropdownItem>,
    autoValueItems: List<DropdownItem>,
    onDragStart: (Long) -> Unit,
    onDragDeltaChange: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRequestDelete: (Long) -> Unit,
    onClearPressed: () -> Unit,
    onMove: (List<ActionEntity>) -> Unit,
    onObjectTypeChange: (ActionEntity, Long?) -> Unit,
    onObjectChange: (ActionEntity, Long?) -> Unit,
    onChangeTypeChange: (ActionEntity, Long?) -> Unit,
    onChangeValueChange: (ActionEntity, Long?) -> Unit,
    isPressed: (Long) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val actionsState = rememberUpdatedState(actions)
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val spacingPx = with(density) { PixsoDimens.Numeric_12.toPx() }
    val fallbackHeightPx = with(density) { 248.dp.toPx() }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        var currentTop = 0f
        val slotTops = actions.map { action ->
            val top = currentTop
            val height = actionHeights[action.id]?.toFloat() ?: fallbackHeightPx
            currentTop += height + spacingPx
            top
        }
        val slotHeights = actions.map { action -> actionHeights[action.id]?.toFloat() ?: fallbackHeightPx }
        val contentHeightPx = if (actions.isEmpty()) 0f else currentTop - spacingPx
        val contentHeightDp = with(density) { contentHeightPx.toDp() }
        val slotRects = actions.indices.map { index ->
            Rect(
                offset = Offset(0f, slotTops[index]),
                size = androidx.compose.ui.geometry.Size(widthPx, slotHeights[index]),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .pointerInput(actions, pendingDeleteVisible) {
                    if (pendingDeleteVisible || actions.isEmpty()) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        down.consume()

                        val startAction = actionsState.value.getOrNull(startIndex) ?: return@awaitEachGesture
                        onDragStart(startAction.id)

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

                        val finalOrder = actionsState.value
                        onDragEnd()

                        if (!moved) {
                            onRequestDelete(startAction.id)
                            return@awaitEachGesture
                        }

                        val from = finalOrder.indexOfFirst { it.id == startAction.id }
                        if (from == -1) return@awaitEachGesture

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val to = if (dropOver != -1) dropOver else hoverIndex
                        if (to == from) {
                            onRequestDelete(startAction.id)
                            return@awaitEachGesture
                        }

                        onClearPressed()
                        val reordered = finalOrder.toMutableList()
                        val item = reordered.removeAt(from)
                        reordered.add(to.coerceIn(0, reordered.size), item)
                        onMove(reordered)
                    }
                },
        ) {
            actions.forEachIndexed { index, action ->
                key(action.id) {
                    val top = slotTops[index]
                    val isDragging = action.id == draggingActionId
                    val animatedOffset by animateIntOffsetAsState(
                        targetValue = IntOffset(0, top.roundToInt()),
                        animationSpec = tween(durationMillis = 220),
                        label = "scenarioActionOffset",
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
                                if (isDragging) {
                                    alpha = 0f
                                }
                            },
                    ) {
                        ScenarioActionCard(
                            action = action,
                            expanded = expandedStates[action.id] ?: false,
                            rooms = rooms,
                            groups = groups,
                            luminaires = luminaires,
                            objectTypeItems = objectTypeItems,
                            changeTypeItems = changeTypeItems,
                            sceneValueItems = sceneValueItems,
                            autoValueItems = autoValueItems,
                            onExpandedChange = { expandedStates[action.id] = it },
                            onObjectTypeChange = { onObjectTypeChange(action, it) },
                            onObjectChange = { onObjectChange(action, it) },
                            onChangeTypeChange = { onChangeTypeChange(action, it) },
                            onChangeValueChange = { onChangeValueChange(action, it) },
                            modifier = Modifier.fillMaxWidth(),
                            onMeasured = { actionHeights[action.id] = it },
                            isPressed = isPressed(action.id),
                        )
                    }
                }
            }

            val draggedIndex = actions.indexOfFirst { it.id == draggingActionId }
            val draggedAction = actions.getOrNull(draggedIndex)
            if (draggedAction != null && draggedIndex != -1) {
                ScenarioActionCard(
                    action = draggedAction,
                    expanded = expandedStates[draggedAction.id] ?: false,
                    rooms = rooms,
                    groups = groups,
                    luminaires = luminaires,
                    objectTypeItems = objectTypeItems,
                    changeTypeItems = changeTypeItems,
                    sceneValueItems = sceneValueItems,
                    autoValueItems = autoValueItems,
                    onExpandedChange = { expandedStates[draggedAction.id] = it },
                    onObjectTypeChange = { onObjectTypeChange(draggedAction, it) },
                    onObjectChange = { onObjectChange(draggedAction, it) },
                    onChangeTypeChange = { onChangeTypeChange(draggedAction, it) },
                    onChangeValueChange = { onChangeValueChange(draggedAction, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, (slotTops[draggedIndex] + dragDelta.y).roundToInt()) }
                        .zIndex(20f),
                    onMeasured = { actionHeights[draggedAction.id] = it },
                    isPressed = true,
                )
            }
        }
    }
}

@Composable
private fun ScenarioActionCard(
    action: ActionEntity,
    expanded: Boolean,
    rooms: List<RoomEntity>,
    groups: List<GroupEntity>,
    luminaires: List<LuminaireEntity>,
    objectTypeItems: List<DropdownItem>,
    changeTypeItems: List<DropdownItem>,
    sceneValueItems: List<DropdownItem>,
    autoValueItems: List<DropdownItem>,
    onExpandedChange: (Boolean) -> Unit,
    onObjectTypeChange: (Long) -> Unit,
    onObjectChange: (Long) -> Unit,
    onChangeTypeChange: (Long) -> Unit,
    onChangeValueChange: (Long) -> Unit,
    onMeasured: (Int) -> Unit,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
) {
    val roomNames = remember(rooms) {
        rooms.associate { it.id to it.name.ifBlank { "Помещение ${it.id + 1}" } }
    }
    val objectItems = remember(action.objectTypeId, rooms, groups, luminaires) {
        when (action.objectTypeId) {
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
    val valueItems = when (action.changeTypeId) {
        CHANGE_TYPE_SCENE -> sceneValueItems
        CHANGE_TYPE_AUTO -> autoValueItems
        else -> emptyList()
    }
    val showObjectField = action.objectTypeId != OBJECT_TYPE_LOCATION
    val objectPlaceholder = when {
        action.objectTypeId == null -> "Сначала выберите тип объекта"
        objectItems.isEmpty() && action.objectTypeId == OBJECT_TYPE_ROOM -> "Нет помещений"
        objectItems.isEmpty() && action.objectTypeId == OBJECT_TYPE_GROUP -> "Нет групп"
        objectItems.isEmpty() && action.objectTypeId == OBJECT_TYPE_LUMINAIRE -> "Нет светильников"
        else -> "Не выбрано"
    }

    Box(
        modifier = modifier.then(
            Modifier.graphicsLayer {
                if (isPressed) {
                    scaleX = 0.995f
                    scaleY = 0.995f
                }
            }
        )
    ) {
        ScenarioPoint(
            title = "Действие",
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            objectTypeField = ScenarioPointField(
                value = action.objectTypeId?.toLong(),
                onValueChange = onObjectTypeChange,
                placeholder = "Не выбрано",
                dropdownItems = objectTypeItems,
            ),
            objectField = if (showObjectField) {
                ScenarioPointField(
                    value = action.objectId,
                    onValueChange = onObjectChange,
                    placeholder = objectPlaceholder,
                    enabled = action.objectTypeId != null && objectItems.isNotEmpty(),
                    dropdownItems = objectItems,
                )
            } else {
                null
            },
            changeTypeField = ScenarioPointField(
                value = action.changeTypeId?.toLong(),
                onValueChange = onChangeTypeChange,
                placeholder = "Не выбрано",
                dropdownItems = changeTypeItems,
            ),
            changeValueField = ScenarioPointField(
                value = action.changeValueId?.toLong(),
                onValueChange = onChangeValueChange,
                placeholder = when {
                    action.changeTypeId == null -> "Сначала выберите изменение"
                    valueItems.isEmpty() -> "Нет значений"
                    else -> "Не выбрано"
                },
                enabled = action.changeTypeId != null && valueItems.isNotEmpty(),
                dropdownItems = valueItems,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { onMeasured(it.size.height) },
            backgroundColor = if (isPressed) {
                PixsoColors.Color_State_primary_pressed
            } else {
                PixsoColors.Color_Bg_bg_surface
            },
            borderColor = if (isPressed) {
                PixsoColors.Color_State_primary_pressed
            } else {
                PixsoColors.Color_Border_border_primary
            },
            headerColor = if (isPressed) {
                PixsoColors.Color_State_on_primary
            } else {
                PixsoColors.Color_State_on_secondary
            },
        )
    }
}

private suspend fun persistActionOrder(
    actionDao: ActionDao,
    orderedActions: List<ActionEntity>,
) {
    orderedActions.forEachIndexed { index, action ->
        actionDao.update(action.copy(position = 1_000_000 + index))
    }
    orderedActions.forEachIndexed { index, action ->
        actionDao.update(action.copy(position = index))
    }
}

