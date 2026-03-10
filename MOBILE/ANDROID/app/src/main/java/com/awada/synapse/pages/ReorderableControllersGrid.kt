package com.awada.synapse.pages

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.components.LocationIcon
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.ControllerEntity
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
internal fun ReorderableControllersGrid(
    controllers: List<ControllerEntity>,
    draggingId: Int,
    pressedId: Int,
    modalVisible: Boolean,
    onDraggingIdChange: (Int) -> Unit,
    onPressedIdChange: (Int) -> Unit,
    onControllersChange: (List<ControllerEntity>) -> Unit,
    onCommitOrder: (List<ControllerEntity>) -> Unit,
    onRequestDelete: (Int) -> Unit,
    onLongPressActivated: () -> Unit = {},
    onControllerClick: ((ControllerEntity) -> Unit)? = null,
    modifier: Modifier = Modifier,
    appearingControllerId: Int? = null,
    onAppearingControllerConsumed: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val controllersState = rememberUpdatedState(controllers)
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var suppressClickId by remember { mutableIntStateOf(-1) }
    val viewConfig = LocalViewConfiguration.current

    LaunchedEffect(draggingId) {
        if (draggingId == -1) {
            dragDelta = Offset.Zero
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
                .pointerInput(n, maxWidth, modalVisible) {
                    if (modalVisible || n == 0) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

                        val list0 = controllersState.value
                        val controller = list0.getOrNull(startIndex) ?: return@awaitEachGesture
                        val id = controller.id

                        onLongPressActivated()
                        suppressClickId = id
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
                        dragDelta = Offset.Zero

                        if (!moved) {
                            onRequestDelete(id)
                            return@awaitEachGesture
                        }

                        val from = finalOrder.indexOfFirst { it.id == id }
                        if (from == -1) return@awaitEachGesture

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val to = if (dropOver != -1) dropOver else hoverIndex
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
            controllers.forEachIndexed { index, controller ->
                key(controller.id) {
                    val topLeft = slotPositions[index]
                    val target = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    val isDragging = controller.id == draggingId
                    var isVisible by remember(controller.id, appearingControllerId) {
                        mutableStateOf(controller.id != appearingControllerId)
                    }
                    val animOffset by animateIntOffsetAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 600),
                        label = "locationOffset"
                    )
                    val appearAlpha by animateFloatAsState(
                        targetValue = if (isVisible || isDragging) 1f else 0f,
                        animationSpec = tween(durationMillis = 350),
                        label = "locationAppearAlpha"
                    )
                    val appearScale by animateFloatAsState(
                        targetValue = if (isVisible || isDragging) 1f else 0.92f,
                        animationSpec = tween(durationMillis = 350),
                        label = "locationAppearScale"
                    )
                    val isPressed = controller.id == pressedId || isDragging

                    LaunchedEffect(controller.id, appearingControllerId) {
                        if (controller.id == appearingControllerId) {
                            isVisible = false
                            kotlinx.coroutines.delay(40)
                            isVisible = true
                            kotlinx.coroutines.delay(350)
                            onAppearingControllerConsumed?.invoke(controller.id)
                        } else {
                            isVisible = true
                        }
                    }

                    val title = controller.name.ifBlank { "Контроллер ${controller.id}" }
                    val icon = iconResId(context, controller.icoNum)

                    LocationIcon(
                        title = title,
                        iconResId = icon,
                        cardSize = cardSize,
                        iconSize = iconSize,
                        contentOffsetY = contentOffsetY,
                        showTitle = true,
                        enabled = true,
                        onClick = {
                            if (suppressClickId == controller.id) {
                                suppressClickId = -1
                            } else {
                                onControllerClick?.invoke(controller)
                            }
                        },
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
                            .graphicsLayer {
                                if (isDragging) {
                                    alpha = 0f
                                } else {
                                    alpha = appearAlpha
                                    scaleX = appearScale
                                    scaleY = appearScale
                                }
                            }
                    )
                }
            }
        }

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
