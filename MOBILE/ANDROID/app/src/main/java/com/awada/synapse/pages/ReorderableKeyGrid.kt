package com.awada.synapse.pages

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
internal fun <K : Any> ReorderableKeyGrid(
    keys: List<K>,
    columns: Int,
    draggingKey: K?,
    pressedKey: K?,
    modalVisible: Boolean,
    onDraggingKeyChange: (K?) -> Unit,
    onPressedKeyChange: (K?) -> Unit,
    onKeysChange: (List<K>) -> Unit,
    onCommitOrder: (List<K>) -> Unit,
    onDropOutsideGrid: (key: K, itemCenterInRoot: Offset) -> Boolean = { _, _ -> false },
    onDropOverKey: (draggedKey: K, targetKey: K) -> Boolean = { _, _ -> false },
    onRequestDelete: (K) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    rowSpacing: Dp = spacing,
    columnSpacing: Dp = spacing,
    itemHeight: Dp = 128.dp,
    itemContent: @Composable (key: K, isPressed: Boolean, suppressClick: Boolean, modifier: Modifier) -> Unit,
) {
    val keysState = rememberUpdatedState(keys)
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    val viewConfig = LocalViewConfiguration.current
    val scope = rememberCoroutineScope()
    var suppressClickKey by remember { mutableStateOf<K?>(null) }
    var suppressClickToken by remember { mutableStateOf(0) }
    var gridOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(draggingKey) {
        if (draggingKey == null) {
            dragDelta = Offset.Zero
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val n = keys.size
        val cols = columns.coerceAtLeast(1)

        val cardWidthDp = (maxWidth - columnSpacing * (cols - 1)) / cols.toFloat()
        val shadowBottomPadding = 12.dp

        val cardWpx = with(density) { cardWidthDp.toPx() }
        val cardHpx = with(density) { itemHeight.toPx() }
        val rowSpacingPx = with(density) { rowSpacing.toPx() }
        val columnSpacingPx = with(density) { columnSpacing.toPx() }
        val shadowBottomPaddingPx = with(density) { shadowBottomPadding.toPx() }

        fun slotTopLeft(index: Int): Offset {
            val col = index % cols
            val row = index / cols
            val x = col * (cardWpx + columnSpacingPx)
            val y = row * (cardHpx + rowSpacingPx)
            return Offset(x, y)
        }

        val slotPositions: List<Offset> = List(n) { slotTopLeft(it) }
        val totalRows = if (n == 0) 0 else ceil(n / cols.toFloat()).toInt()
        val gridHeightPx =
            totalRows * cardHpx +
                (totalRows - 1).coerceAtLeast(0) * rowSpacingPx +
                shadowBottomPaddingPx
        val contentHeightDp = with(density) { gridHeightPx.toDp() }

        val slotRects = slotPositions.map { topLeft ->
            androidx.compose.ui.geometry.Rect(
                offset = topLeft,
                size = androidx.compose.ui.geometry.Size(cardWpx, cardHpx)
            )
        }

        val anyDragging = draggingKey != null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    gridOriginInRoot = Offset(position.x, position.y)
                }
                // Drag should win over scroll: pointerInput first, then PageContainer scroll.
                .pointerInput(n, maxWidth, modalVisible, cols, rowSpacing, columnSpacing) {
                    if (modalVisible || n == 0) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        down.consume()

                        val list0 = keysState.value
                        val key0 = list0.getOrNull(startIndex) ?: return@awaitEachGesture
                        suppressClickToken += 1
                        val token = suppressClickToken
                        suppressClickKey = key0
                        onDraggingKeyChange(key0)
                        onPressedKeyChange(key0)

                        var moved = false
                        var hoverIndex = startIndex
                        var lastPos = down.position
                        dragDelta = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()
                            val change =
                                event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                            if (!change.pressed) {
                                change.consume()
                                break
                            }

                            val delta = change.position - change.previousPosition
                            lastPos = change.position
                            if (delta != Offset.Zero) {
                                dragDelta += delta
                                if (!moved && dragDelta.getDistance() > viewConfig.touchSlop) moved = true

                                val center =
                                    slotPositions[startIndex] + dragDelta + Offset(cardWpx / 2f, cardHpx / 2f)
                                var best = hoverIndex
                                var bestDist = Float.MAX_VALUE
                                for (i in slotPositions.indices) {
                                    val c = slotPositions[i] + Offset(cardWpx / 2f, cardHpx / 2f)
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

                        scope.launch {
                            delay(250)
                            if (suppressClickToken == token) {
                                suppressClickKey = null
                            }
                        }

                        val finalOrder = keysState.value
                        val itemCenterInRoot =
                            gridOriginInRoot +
                                slotPositions[startIndex] +
                                dragDelta +
                                Offset(cardWpx / 2f, cardHpx / 2f)
                        onDraggingKeyChange(null)
                        dragDelta = Offset.Zero

                        if (!moved) {
                            onRequestDelete(key0)
                            return@awaitEachGesture
                        }

                        val from = finalOrder.indexOf(key0)
                        if (from == -1) return@awaitEachGesture

                        if (onDropOutsideGrid(key0, itemCenterInRoot)) {
                            onPressedKeyChange(null)
                            return@awaitEachGesture
                        }

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val targetKey =
                            dropOver
                                .takeIf { it != -1 }
                                ?.let { finalOrder.getOrNull(it) }
                        if (targetKey != null && targetKey != key0 && onDropOverKey(key0, targetKey)) {
                            onPressedKeyChange(null)
                            return@awaitEachGesture
                        }
                        val toRaw = if (dropOver != -1) dropOver else hoverIndex
                        val to = toRaw
                        if (to == from) {
                            onPressedKeyChange(null)
                            return@awaitEachGesture
                        }

                        onPressedKeyChange(null)
                        val newList = finalOrder.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to.coerceIn(0, newList.size), item)
                        onKeysChange(newList)
                        onCommitOrder(newList)
                    }
                }
        ) {
            keys.forEachIndexed { index, k ->
                key(k) {
                    val topLeft = slotPositions.getOrNull(index) ?: Offset.Zero
                    val target = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    val isDragging = k == draggingKey
                    val animOffset by animateIntOffsetAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 600),
                        label = "gridOffset"
                    )
                    val isPressed = k == pressedKey || isDragging

                    Box(
                        modifier = Modifier
                            .width(cardWidthDp)
                            .height(itemHeight)
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
                            .alpha(if (isDragging) 0f else 1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val suppressClick = suppressClickKey == k
                        itemContent(k, isPressed, suppressClick, Modifier)
                    }
                }
            }
        }

        if (anyDragging) {
            val draggedIndex = keys.indexOfFirst { it == draggingKey }
            val draggedKey = keys.getOrNull(draggedIndex)
            if (draggedIndex != -1 && draggedKey != null) {
                val topLeft = slotPositions[draggedIndex]
                Box(
                    modifier = Modifier
                        .width(cardWidthDp)
                        .height(itemHeight)
                        .offset {
                            IntOffset(
                                (topLeft.x + dragDelta.x).roundToInt(),
                                (topLeft.y + dragDelta.y).roundToInt()
                            )
                        }
                        .zIndex(20f),
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(draggedKey, true, true, Modifier)
                }
            }
        }
    }
}

