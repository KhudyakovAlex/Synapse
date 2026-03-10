package com.awada.synapse.pages

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.R
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
internal fun ReorderableRoomsGrid(
    rooms: List<RoomEntity>,
    draggingId: Int,
    pressedId: Int,
    modalVisible: Boolean,
    onDraggingIdChange: (Int) -> Unit,
    onPressedIdChange: (Int) -> Unit,
    onRoomsChange: (List<RoomEntity>) -> Unit,
    onCommitOrder: (List<RoomEntity>) -> Unit,
    onRequestDelete: (roomId: Int, title: String) -> Unit,
    onRoomClick: ((roomId: Int, roomTitle: String, roomIconId: Int) -> Unit)?,
    onRoomBoundsChange: ((roomId: Int, bounds: Rect) -> Unit)? = null,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 96.dp * 0.9f,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val roomsState = rememberUpdatedState(rooms)
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var suppressClickRoomId by remember { mutableIntStateOf(-1) }
    var suppressClickToken by remember { mutableIntStateOf(0) }
    val viewConfig = LocalViewConfiguration.current

    LaunchedEffect(draggingId) {
        if (draggingId == -1) {
            dragDelta = Offset.Zero
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val n = rooms.size

        val spacing = PixsoDimens.Numeric_16
        val shadowBottomPadding = 12.dp
        val cardHeightDp = itemHeight
        val cardWidthDp = (maxWidth - spacing) / 2f

        val cardWpx = with(density) { cardWidthDp.toPx() }
        val cardHpx = with(density) { cardHeightDp.toPx() }
        val spacingPx = with(density) { spacing.toPx() }
        val shadowBottomPaddingPx = with(density) { shadowBottomPadding.toPx() }

        fun slotTopLeft(index: Int): Offset {
            val col = index % 2
            val row = index / 2
            val x = col * (cardWpx + spacingPx)
            val y = row * (cardHpx + spacingPx)
            return Offset(x, y)
        }

        val slotPositions: List<Offset> = List(n) { slotTopLeft(it) }
        val totalRows = if (n == 0) 0 else ceil(n / 2f).toInt()
        val gridHeightPx =
            totalRows * cardHpx +
                (totalRows - 1).coerceAtLeast(0) * spacingPx +
                shadowBottomPaddingPx
        val contentHeightDp = with(density) { gridHeightPx.toDp() }

        val slotRects = slotPositions.map { topLeft ->
            Rect(
                offset = topLeft,
                size = androidx.compose.ui.geometry.Size(cardWpx, cardHpx)
            )
        }

        val anyDragging = draggingId != -1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .pointerInput(n, maxWidth, modalVisible) {
                    if (modalVisible || n == 0) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        down.consume()

                        val list0 = roomsState.value
                        val room0 = list0.getOrNull(startIndex) ?: return@awaitEachGesture
                        val id = room0.id

                        suppressClickToken += 1
                        val token = suppressClickToken
                        suppressClickRoomId = id
                        onDraggingIdChange(id)
                        onPressedIdChange(id)

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
                                suppressClickRoomId = -1
                            }
                        }

                        val finalOrder = roomsState.value
                        onDraggingIdChange(-1)
                        dragDelta = Offset.Zero

                        val title0 = room0.name.ifBlank { "Помещение ${room0.id + 1}" }
                        if (!moved) {
                            onRequestDelete(id, title0)
                            return@awaitEachGesture
                        }

                        val from = finalOrder.indexOfFirst { it.id == id }
                        if (from == -1) return@awaitEachGesture

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val to = if (dropOver != -1) dropOver else hoverIndex
                        if (to == from) {
                            onPressedIdChange(-1)
                            return@awaitEachGesture
                        }

                        onPressedIdChange(-1)
                        val newList = finalOrder.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to.coerceIn(0, newList.size), item)
                        onRoomsChange(newList)
                        onCommitOrder(newList)
                    }
                }
        ) {
            rooms.forEachIndexed { index, room ->
                key(room.id) {
                    val topLeft = slotPositions.getOrNull(index) ?: Offset.Zero
                    val target = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    val isDragging = room.id == draggingId
                    val animOffset by animateIntOffsetAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 600),
                        label = "roomOffset"
                    )
                    val isPressed = room.id == pressedId || isDragging

                    val title = room.name.ifBlank { "Помещение ${room.id + 1}" }
                    val icon = iconResId(
                        context = context,
                        iconId = room.icoNum,
                        fallback = R.drawable.location_208_kuhnya
                    )

                    RoomIcon(
                        text = title,
                        iconResId = icon,
                        onClick = onRoomClick?.let { cb ->
                            {
                                if (suppressClickRoomId == room.id) {
                                    suppressClickRoomId = -1
                                } else {
                                    cb(room.id, title, room.icoNum)
                                }
                            }
                        },
                        onLongClick = null,
                        height = itemHeight,
                        backgroundColor = if (isPressed) PixsoColors.Color_State_primary_pressed else PixsoColors.Color_Bg_bg_surface,
                        contentColor = if (isPressed) PixsoColors.Color_State_on_primary else PixsoColors.Color_State_tertiary,
                        modifier = Modifier
                            .width(cardWidthDp)
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
                            .onGloballyPositioned { coordinates ->
                                onRoomBoundsChange?.invoke(room.id, coordinates.boundsInRoot())
                            }
                    )
                }
            }
        }

        if (anyDragging) {
            val draggedIndex = rooms.indexOfFirst { it.id == draggingId }
            val draggedRoom = rooms.getOrNull(draggedIndex)
            if (draggedIndex != -1 && draggedRoom != null) {
                val topLeft = slotPositions[draggedIndex]
                val title = draggedRoom.name.ifBlank { "Помещение ${draggedRoom.id + 1}" }
                val icon = iconResId(
                    context = context,
                    iconId = draggedRoom.icoNum,
                    fallback = R.drawable.location_208_kuhnya
                )
                RoomIcon(
                    text = title,
                    iconResId = icon,
                    onClick = null,
                    onLongClick = null,
                    height = itemHeight,
                    backgroundColor = PixsoColors.Color_State_primary_pressed,
                    contentColor = PixsoColors.Color_State_on_primary,
                    modifier = Modifier
                        .width(cardWidthDp)
                        .offset {
                            IntOffset(
                                (topLeft.x + dragDelta.x).roundToInt(),
                                (topLeft.y + dragDelta.y).roundToInt()
                            )
                        }
                        .zIndex(10f)
                )
            }
        }
    }
}
