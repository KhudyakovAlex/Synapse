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
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun PageScenario(
    scenarioId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val actions by remember(db, scenarioId) {
        if (scenarioId == null) {
            flowOf(emptyList())
        } else {
            db.actionDao().observeAllForScenario(scenarioId)
        }
    }.collectAsState(initial = emptyList())
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }
    val actionHeights = remember { mutableStateMapOf<Long, Int>() }
    var draggingActionId by remember { mutableStateOf<Long?>(null) }
    var pressedActionId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteActionId by remember { mutableStateOf<Long?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }

    val whereItems = remember {
        listOf(
            DropdownItem(1, "Гостиная"),
            DropdownItem(2, "Кухня"),
            DropdownItem(3, "Спальня"),
        )
    }
    val whatItems = remember {
        listOf(
            DropdownItem(1, "Выключить"),
            DropdownItem(2, "Включить"),
            DropdownItem(3, "Яркость"),
            DropdownItem(4, "Сцена"),
        )
    }
    val valueItems = remember {
        listOf(
            DropdownItem(0, "0"),
            DropdownItem(1, "1"),
            DropdownItem(25, "25"),
            DropdownItem(50, "50"),
            DropdownItem(75, "75"),
            DropdownItem(100, "100"),
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
                whereItems = whereItems,
                whatItems = whatItems,
                valueItems = valueItems,
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
                onWhereChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(action.copy(whereId = value))
                    }
                },
                onWhatChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(action.copy(whatId = value))
                    }
                },
                onValueChange = { action, value ->
                    scope.launch {
                        db.actionDao().update(action.copy(valueId = value))
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
    whereItems: List<DropdownItem>,
    whatItems: List<DropdownItem>,
    valueItems: List<DropdownItem>,
    onDragStart: (Long) -> Unit,
    onDragDeltaChange: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRequestDelete: (Long) -> Unit,
    onClearPressed: () -> Unit,
    onMove: (List<ActionEntity>) -> Unit,
    onWhereChange: (ActionEntity, Int) -> Unit,
    onWhatChange: (ActionEntity, Int) -> Unit,
    onValueChange: (ActionEntity, Int) -> Unit,
    isPressed: (Long) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val actionsState = rememberUpdatedState(actions)
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val spacingPx = with(density) { PixsoDimens.Numeric_12.toPx() }
    val fallbackHeightPx = with(density) { 188.dp.toPx() }

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
                            whereItems = whereItems,
                            whatItems = whatItems,
                            valueItems = valueItems,
                            onExpandedChange = { expandedStates[action.id] = it },
                            onWhereChange = { onWhereChange(action, it) },
                            onWhatChange = { onWhatChange(action, it) },
                            onValueChange = { onValueChange(action, it) },
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
                    whereItems = whereItems,
                    whatItems = whatItems,
                    valueItems = valueItems,
                    onExpandedChange = { expandedStates[draggedAction.id] = it },
                    onWhereChange = { onWhereChange(draggedAction, it) },
                    onWhatChange = { onWhatChange(draggedAction, it) },
                    onValueChange = { onValueChange(draggedAction, it) },
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
    whereItems: List<DropdownItem>,
    whatItems: List<DropdownItem>,
    valueItems: List<DropdownItem>,
    onExpandedChange: (Boolean) -> Unit,
    onWhereChange: (Int) -> Unit,
    onWhatChange: (Int) -> Unit,
    onValueChange: (Int) -> Unit,
    onMeasured: (Int) -> Unit,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
) {
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
            whereField = ScenarioPointField(
                value = action.whereId,
                onValueChange = onWhereChange,
                placeholder = "Не выбрано",
                dropdownItems = whereItems,
            ),
            whatField = ScenarioPointField(
                value = action.whatId,
                onValueChange = onWhatChange,
                placeholder = "Не выбрано",
                dropdownItems = whatItems,
            ),
            valueField = ScenarioPointField(
                value = action.valueId,
                onValueChange = onValueChange,
                placeholder = "Не выбрано",
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

