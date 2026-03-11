package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.TextField
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ButtonEntity
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Button panel settings page.
 * Placeholder: duplicated structure from sensor settings.
 */
@Composable
fun PageButtonPanelSettings(
    buttonPanelId: Long?,
    onBackClick: () -> Unit,
    onButtonClick: (buttonNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    val buttons by remember(db, buttonPanelId) {
        if (buttonPanelId == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            db.buttonDao().observeAllForPanel(buttonPanelId)
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(buttonPanelId) {
        val id = buttonPanelId ?: return@LaunchedEffect
        val e = db.buttonPanelDao().getById(id) ?: return@LaunchedEffect
        name = e.name
    }

    fun saveAndBack() {
        scope.launch {
            val id = buttonPanelId
            if (id != null) {
                db.buttonPanelDao().setName(id = id, name = name)
            }
            onBackClick()
        }
    }

    BackHandler { saveAndBack() }

    PageContainer(
        title = "Настройки\nкнопочной панели",
        onBackClick = ::saveAndBack,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            // 1. Название
            TextField(
                value = name,
                onValueChange = { name = it },
                label = "Название",
                placeholder = "",
                enabled = true
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

            Text(
                text = "Настройка нажатий\nи расположения кнопок",
                style = LabelLarge.copy(color = PixsoColors.Color_Text_text_3_level),
                modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12),
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_24))

            ButtonMatrixEditor(
                buttons = buttons,
                onButtonClick = onButtonClick,
                onMoveButton = { draggedButton, row, col ->
                    scope.launch {
                        val targetButton = buttons.firstOrNull {
                            it.id != draggedButton.id && it.matrixRow == row && it.matrixCol == col
                        }
                        if (targetButton != null) {
                            val emptyIndex = findNextEmptyMatrixIndex(
                                buttons = buttons,
                                draggedButton = draggedButton,
                                targetRow = row,
                                targetCol = col,
                            )
                            if (emptyIndex == null) return@launch

                            db.buttonDao().moveToOccupiedMatrixPosition(
                                draggedId = draggedButton.id,
                                targetId = targetButton.id,
                                targetRow = row,
                                targetCol = col,
                                emptyRow = emptyIndex / ButtonEntity.MATRIX_SIZE,
                                emptyCol = emptyIndex % ButtonEntity.MATRIX_SIZE,
                            )
                        } else {
                            db.buttonDao().setMatrixPosition(
                                id = draggedButton.id,
                                matrixRow = row,
                                matrixCol = col,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ButtonMatrixEditor(
    buttons: List<ButtonEntity>,
    onButtonClick: (buttonNumber: Int) -> Unit,
    onMoveButton: (button: ButtonEntity, row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val matrixButtons = buttons
        .filter { it.matrixRow in 0 until ButtonEntity.MATRIX_SIZE && it.matrixCol in 0 until ButtonEntity.MATRIX_SIZE }
    val buttonsState = rememberUpdatedState(matrixButtons)
    val scope = rememberCoroutineScope()
    val viewConfig = LocalViewConfiguration.current
    var draggingButtonId by remember { mutableStateOf<Int?>(null) }
    var pressedButtonId by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var suppressClickButtonId by remember { mutableStateOf<Int?>(null) }
    var suppressClickToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(draggingButtonId) {
        if (draggingButtonId == null) {
            dragDelta = Offset.Zero
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val cellSpacing = 4.dp
        val matrixSize = ButtonEntity.MATRIX_SIZE
        val cellSize = (maxWidth - cellSpacing * (matrixSize - 1)) / matrixSize.toFloat()
        val cellSizePx = with(density) { cellSize.toPx() }
        val cellSpacingPx = with(density) { cellSpacing.toPx() }
        val contentHeight = cellSize * matrixSize + cellSpacing * (matrixSize - 1)
        val slotPositions = List(matrixSize * matrixSize) { index ->
            val row = index / matrixSize
            val col = index % matrixSize
            Offset(
                x = col * (cellSizePx + cellSpacingPx),
                y = row * (cellSizePx + cellSpacingPx),
            )
        }
        val slotRects = slotPositions.map { topLeft ->
            Rect(topLeft, androidx.compose.ui.geometry.Size(cellSizePx, cellSizePx))
        }
        val buttonSize = if (cellSize > 108.dp) 108.dp else cellSize - 4.dp
        val slotDotSize = buttonSize / 10f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeight)
                .pointerInput(matrixButtons, maxWidth) {
                    if (buttonsState.value.isEmpty()) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        val startButton = buttonsState.value.firstOrNull {
                            it.matrixRow * matrixSize + it.matrixCol == startIndex
                        } ?: return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        down.consume()

                        suppressClickToken += 1
                        val token = suppressClickToken
                        suppressClickButtonId = startButton.id
                        draggingButtonId = startButton.id
                        pressedButtonId = startButton.id

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
                                    slotPositions[startIndex] + dragDelta + Offset(cellSizePx / 2f, cellSizePx / 2f)
                                var best = hoverIndex
                                var bestDist = Float.MAX_VALUE
                                for (i in slotPositions.indices) {
                                    val c = slotPositions[i] + Offset(cellSizePx / 2f, cellSizePx / 2f)
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
                                suppressClickButtonId = null
                            }
                        }

                        draggingButtonId = null
                        pressedButtonId = null
                        dragDelta = Offset.Zero

                        if (!moved) {
                            onButtonClick(startButton.num)
                            return@awaitEachGesture
                        }

                        val dropOver = slotRects.indexOfFirst { it.contains(lastPos) }
                        val targetIndex = if (dropOver != -1) dropOver else hoverIndex
                        if (targetIndex == startIndex) return@awaitEachGesture

                        onMoveButton(
                            startButton,
                            targetIndex / matrixSize,
                            targetIndex % matrixSize,
                        )
                    }
                },
        ) {
            slotPositions.forEach { topLeft ->
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .offset { IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(slotDotSize)
                            .background(
                                color = PixsoColors.Color_State_disabled,
                                shape = CircleShape,
                            ),
                    )
                }
            }

            matrixButtons.forEach { button ->
                key(button.id) {
                    val slotIndex = button.matrixRow * matrixSize + button.matrixCol
                    val topLeft = slotPositions.getOrNull(slotIndex) ?: Offset.Zero
                    val isDragging = button.id == draggingButtonId
                    val isPressed = button.id == pressedButtonId || isDragging
                    val animatedOffset by animateIntOffsetAsState(
                        targetValue = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
                        animationSpec = tween(durationMillis = 220),
                        label = "buttonMatrixOffset",
                    )

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .offset {
                                if (isDragging) {
                                    IntOffset(
                                        (topLeft.x + dragDelta.x).roundToInt(),
                                        (topLeft.y + dragDelta.y).roundToInt(),
                                    )
                                } else {
                                    animatedOffset
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        PanelButtonWithSuppressedClick(
                            button = button,
                            buttonSize = buttonSize,
                            isPressed = isPressed,
                            suppressClick = suppressClickButtonId == button.id,
                            onButtonClick = onButtonClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelButtonWithSuppressedClick(
    button: ButtonEntity,
    buttonSize: androidx.compose.ui.unit.Dp,
    isPressed: Boolean,
    suppressClick: Boolean,
    onButtonClick: (buttonNumber: Int) -> Unit,
) {
    com.awada.synapse.components.PanelButton(
        text = button.num.toString(),
        variant = if (isPressed) com.awada.synapse.components.PanelButtonVariant.Active else com.awada.synapse.components.PanelButtonVariant.Def,
        size = buttonSize,
        onClick = if (suppressClick) {
            null
        } else {
            { onButtonClick(button.num) }
        },
    )
}

private fun findNextEmptyMatrixIndex(
    buttons: List<ButtonEntity>,
    draggedButton: ButtonEntity,
    targetRow: Int,
    targetCol: Int,
): Int? {
    val matrixSize = ButtonEntity.MATRIX_SIZE
    val targetIndex = targetRow * matrixSize + targetCol
    val occupiedIndices = buttons
        .filter { it.id != draggedButton.id }
        .map { it.matrixRow * matrixSize + it.matrixCol }
        .toSet()

    for (offset in 1 until matrixSize * matrixSize) {
        val candidateIndex = (targetIndex + offset) % (matrixSize * matrixSize)
        if (candidateIndex !in occupiedIndices) {
            return candidateIndex
        }
    }

    return null
}

