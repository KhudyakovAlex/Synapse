package com.awada.synapse.pages

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.components.LocationIcon
import com.awada.synapse.components.LocationsContainer
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ControllerEntity
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.vibrateStrongClick
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Settings page.
 * Allows user to configure app settings.
 */
@Composable
fun PageSettings(
    onBackClick: () -> Unit,
    onFindControllerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val controllers by db.controllerDao().observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val ordered = remember { mutableStateOf<List<ControllerEntity>>(emptyList()) }
    var draggingId by remember { mutableIntStateOf(-1) }
    var pressedId by remember { mutableIntStateOf(-1) }
    var pendingDeleteId by remember { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(controllers, draggingId) {
        if (draggingId == -1) {
            ordered.value = controllers
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Настройки",
            onBackClick = onBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            if (ordered.value.isEmpty()) {
                LocationsContainer(
                    locations = emptyList(),
                    modifier = Modifier
                        .fillMaxWidth(),
                    fillAvailableHeight = false,
                    onEmptyButtonClick = onFindControllerClick
                )
                return@PageContainer
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_16)
            ) {
                Text(
                    text = "Чтобы добавить локацию, подключитесь к ее контроллеру",
                    style = TitleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryIconButtonLarge(
                    text = "Найти контроллер",
                    onClick = { onFindControllerClick?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onFindControllerClick != null
                )
                Spacer(modifier = Modifier.height(40.dp))
                ReorderableControllersLayout(
                    controllers = ordered.value,
                    draggingId = draggingId,
                    pressedId = pressedId,
                    modalVisible = pendingDeleteId != -1,
                    onDraggingIdChange = { draggingId = it },
                    onPressedIdChange = { pressedId = it },
                    onControllersChange = { ordered.value = it },
                    onCommitOrder = { finalOrder ->
                        scope.launch {
                            val dao = db.controllerDao()
                            finalOrder.forEachIndexed { index, c ->
                                dao.setGridPos(c.id, index)
                            }
                        }
                    },
                    onRequestDelete = { pendingDeleteId = it },
                    onLongPressActivated = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vibrateStrongClick(context)
                    }
                )
            }
        }

        // Modal is outside PageContainer to match PagePassword behavior (covers AppBar too)
        if (pendingDeleteId != -1) {
            Tooltip(
                text = "Удалить локацию?",
                primaryButtonText = "Удалить",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            val id = pendingDeleteId
                            val remaining = ordered.value.filter { it.id != id }
                            ordered.value = remaining
                            pendingDeleteId = -1
                            pressedId = -1
                            scope.launch {
                                val dao = db.controllerDao()
                                dao.deleteById(id)
                                remaining.forEachIndexed { index, c ->
                                    dao.setGridPos(c.id, index)
                                }
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pendingDeleteId = -1
                            pressedId = -1
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ReorderableControllersLayout(
    controllers: List<ControllerEntity>,
    draggingId: Int,
    pressedId: Int,
    modalVisible: Boolean,
    onDraggingIdChange: (Int) -> Unit,
    onPressedIdChange: (Int) -> Unit,
    onControllersChange: (List<ControllerEntity>) -> Unit,
    onCommitOrder: (List<ControllerEntity>) -> Unit,
    onRequestDelete: (Int) -> Unit,
    onLongPressActivated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controllersState = rememberUpdatedState(controllers)
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var draggedId by remember { mutableIntStateOf(-1) }
    val viewConfig = LocalViewConfiguration.current

    LaunchedEffect(draggingId) {
        if (draggingId == -1) {
            dragDelta = Offset.Zero
            draggedId = -1
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val n = controllers.size

        val scale = if (n == 1) 1.5f else 1f
        val cardSize: Dp = 156.dp * scale
        val iconSize: Dp = 56.dp * scale * 1.1f
        val contentOffsetY: Dp = 8.dp * scale
        val spacing: Dp = 24.dp
        val topPadding: Dp = if (n == 1) 24.dp else 0.dp
        val shadowBottomPadding: Dp = if (n == 0) 0.dp else 12.dp

        val cardPx = with(density) { cardSize.toPx() }
        val spacingPx = with(density) { spacing.toPx() }
        val topPaddingPx = with(density) { topPadding.toPx() }
        val shadowBottomPaddingPx = with(density) { shadowBottomPadding.toPx() }
        val widthPx = with(density) { maxWidth.toPx() }

        fun slotTopLeft(index: Int): Offset {
            if (n == 1) {
                val x = (widthPx - cardPx) / 2f
                return Offset(x, topPaddingPx)
            }
            if (n == 2) {
                val x = (widthPx - cardPx) / 2f
                val y = index * (cardPx + spacingPx)
                return Offset(x, y)
            }
            val rows = ceil(n / 2f).toInt()
            var idx = 0
            var y = 0f
            for (r in 0 until rows) {
                val remaining = n - idx
                val rowCount = if (remaining >= 2) 2 else 1
                if (rowCount == 2) {
                    val rowWidth = cardPx * 2f + spacingPx
                    val startX = (widthPx - rowWidth) / 2f
                    if (idx == index) return Offset(startX, y)
                    if (idx + 1 == index) return Offset(startX + cardPx + spacingPx, y)
                    idx += 2
                } else {
                    val startX = (widthPx - cardPx) / 2f
                    if (idx == index) return Offset(startX, y)
                    idx += 1
                }
                y += cardPx + spacingPx
            }
            return Offset(0f, 0f)
        }

        val slotPositions: List<Offset> = List(n) { slotTopLeft(it) }
        val totalRows = when {
            n <= 1 -> 1
            n == 2 -> 2
            else -> ceil(n / 2f).toInt()
        }
        val gridHeightPx =
            topPaddingPx +
                totalRows * cardPx +
                (totalRows - 1).coerceAtLeast(0) * spacingPx +
                shadowBottomPaddingPx
        val contentHeightDp = with(density) { gridHeightPx.toDp() }

        val slotRects = slotPositions.map { topLeft ->
            androidx.compose.ui.geometry.Rect(
                offset = topLeft,
                size = androidx.compose.ui.geometry.Size(cardPx, cardPx)
            )
        }

        val anyDragging = draggingId != -1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                // Drag should win over scroll: pointerInput first, then verticalScroll
                .pointerInput(n, maxWidth, modalVisible) {
                    if (modalVisible || n == 0) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

                        val list0 = controllersState.value
                        val id = list0.getOrNull(startIndex)?.id ?: return@awaitEachGesture

                        onLongPressActivated()
                        draggedId = id
                        onDraggingIdChange(id)
                        onPressedIdChange(id)

                        var moved = false
                        var hoverIndex = startIndex
                        var lastPos = down.position
                        dragDelta = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                            if (!change.pressed) break

                            val delta = change.position - change.previousPosition
                            lastPos = change.position
                            if (delta != Offset.Zero) {
                                dragDelta += delta
                                if (!moved && dragDelta.getDistance() > viewConfig.touchSlop) moved = true

                                val center = slotPositions[startIndex] + dragDelta + Offset(cardPx / 2f, cardPx / 2f)
                                var best = hoverIndex
                                var bestDist = Float.MAX_VALUE
                                for (i in slotPositions.indices) {
                                    val c = slotPositions[i] + Offset(cardPx / 2f, cardPx / 2f)
                                    val d = abs(center.x - c.x) + abs(center.y - c.y)
                                    if (d < bestDist) {
                                        bestDist = d
                                        best = i
                                    }
                                }
                                hoverIndex = best
                            }

                            change.consume()
                        }

                        val finalOrder = controllersState.value
                        onDraggingIdChange(-1)
                        draggedId = -1
                        dragDelta = Offset.Zero

                        if (!moved) {
                            onRequestDelete(id)
                            return@awaitEachGesture
                        }

                        // Reorder only on drop (no live rearrangement while dragging).
                        val from = finalOrder.indexOfFirst { it.id == id }
                        if (from == -1) return@awaitEachGesture

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val toRaw = if (dropOver != -1) dropOver else hoverIndex
                        val to = if (toRaw > from) toRaw - 1 else toRaw
                        // If user dragged but ended up without a reorder, interpret as delete intent.
                        if (to == from) {
                            onRequestDelete(id)
                            return@awaitEachGesture
                        }

                        onPressedIdChange(-1)
                        val newList = finalOrder.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to.coerceIn(0, newList.size), item)
                        onControllersChange(newList)
                        onCommitOrder(newList)
                    }
                }
        ) {
            controllers.forEachIndexed { index, c ->
                key(c.id) {
                    val topLeft = slotPositions[index]
                    val target = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    val isDragging = c.id == draggingId
                    val animOffset by animateIntOffsetAsState(
                        targetValue = target,
                        // 2x slower reorder animation.
                        animationSpec = tween(durationMillis = 600),
                        label = "iconOffset"
                    )
                    val isPressed = c.id == pressedId || isDragging

                    val title = c.name.ifBlank { "Контроллер ${c.id}" }
                    val icon = iconResId(context, c.icoNum)

                    LocationIcon(
                        title = title,
                        iconResId = icon,
                        cardSize = cardSize,
                        iconSize = iconSize,
                        contentOffsetY = contentOffsetY,
                        showTitle = true,
                        enabled = true,
                        onClick = null,
                        backgroundColor = if (isPressed) PixsoColors.Color_State_primary_pressed else PixsoColors.Color_Bg_bg_surface,
                        titleColor = if (isPressed) PixsoColors.Color_State_on_primary else PixsoColors.Color_State_tertiary,
                        iconTint = if (isPressed) PixsoColors.Color_State_on_primary else null,
                        modifier = Modifier
                            .offset {
                                if (isDragging) {
                                    IntOffset(
                                        (topLeft.x + dragDelta.x).roundToInt(),
                                        (topLeft.y + dragDelta.y).roundToInt()
                                    )
                                } else {
                                    animOffset
                                }
                            }
                            .zIndex(if (isDragging) 10f else 0f)
                            .alpha(if (isDragging) 0f else 1f)
                    )
                }
            }
        }

        // Draw dragged item outside scroll container to avoid shadow clipping
        if (anyDragging) {
            val draggedIndex = controllers.indexOfFirst { it.id == draggingId }
            val draggedController = controllers.getOrNull(draggedIndex)
            if (draggedIndex != -1 && draggedController != null) {
                val topLeft = slotPositions[draggedIndex]
                val title = draggedController.name.ifBlank { "Контроллер ${draggedController.id}" }
                val icon = iconResId(context, draggedController.icoNum)
                LocationIcon(
                    title = title,
                    iconResId = icon,
                    cardSize = cardSize,
                    iconSize = iconSize,
                    contentOffsetY = contentOffsetY,
                    showTitle = true,
                    enabled = true,
                    onClick = null,
                    backgroundColor = PixsoColors.Color_State_primary_pressed,
                    titleColor = PixsoColors.Color_State_on_primary,
                    iconTint = PixsoColors.Color_State_on_primary,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (topLeft.x + dragDelta.x).roundToInt(),
                                (topLeft.y + dragDelta.y).roundToInt()
                            )
                        }
                        .zIndex(20f)
                )
            }
        }
    }
}
