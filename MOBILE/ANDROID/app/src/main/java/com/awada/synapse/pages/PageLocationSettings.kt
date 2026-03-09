package com.awada.synapse.pages

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.Switch
import com.awada.synapse.components.TextField
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PageLocationSettings(
    controllerId: Int?,
    onBackClick: () -> Unit,
    onSaved: ((name: String, iconId: Int) -> Unit)? = null,
    onRoomClick: ((roomId: Int, roomTitle: String, roomIconId: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showIconSelect by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var draftIconId by remember { mutableIntStateOf(100) }
    var loadedForId by remember { mutableStateOf<Int?>(null) }
    var pendingDeleteRoomId by remember { mutableIntStateOf(-1) }
    var pendingDeleteRoomTitle by remember { mutableStateOf("") }
    val orderedRooms = remember { mutableStateOf<List<RoomEntity>>(emptyList()) }
    var draggingRoomId by remember { mutableIntStateOf(-1) }
    var pressedRoomId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(controllerId) {
        if (controllerId == null) return@LaunchedEffect
        if (loadedForId == controllerId) return@LaunchedEffect
        val c = db.controllerDao().getById(controllerId)
        if (c != null) {
            draftName = c.name
            draftIconId = c.icoNum
            loadedForId = controllerId
        }
    }

    if (showIconSelect) {
        PageIconSelect(
            category = "controller",
            currentIconId = draftIconId,
            onIconSelected = { newId ->
                draftIconId = newId
                showIconSelect = false
            },
            onBackClick = { showIconSelect = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (showChangePassword) {
        PageChangePassword(
            onBackClick = { showChangePassword = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (showSchedule) {
        PageSchedule(
            onBackClick = { showSchedule = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val iconRes = iconResId(context, draftIconId)
    val roomsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<RoomEntity>?>(emptyList())
        } else {
            db.roomDao()
                .observeAll(controllerId)
                .map<List<RoomEntity>, List<RoomEntity>?> { it }
        }
    }.collectAsState(initial = null)
    val rooms = roomsOrNull

    LaunchedEffect(rooms, draggingRoomId) {
        if (draggingRoomId == -1) {
            orderedRooms.value = rooms ?: emptyList()
        }
    }

    val handleBackClick: () -> Unit = {
        val id = controllerId
        if (id == null) {
            onBackClick()
        } else {
            scope.launch {
                db.controllerDao().updateNameAndIcon(id, draftName, draftIconId)
                onSaved?.invoke(draftName, draftIconId)
                onBackClick()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Настройки\nлокации",
            onBackClick = handleBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_16)
            ) {
                TextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = "Название",
                    placeholder = "",
                    enabled = true
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

                Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8)) {
                    androidx.compose.material3.Text(
                        text = "Иконка",
                        style = LabelLarge,
                        color = PixsoColors.Color_Text_text_3_level,
                        modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12)
                    )
                    IconSelectButton(
                        icon = iconRes,
                        onClick = { showIconSelect = true }
                    )
                }

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                ScheduleCard(
                    modifier = Modifier.fillMaxWidth(),
                    onConfigureClick = { showSchedule = true }
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16)) {
                    ReorderableRoomsLayout(
                        rooms = orderedRooms.value,
                        draggingId = draggingRoomId,
                        pressedId = pressedRoomId,
                        modalVisible = pendingDeleteRoomId != -1,
                        onDraggingIdChange = { draggingRoomId = it },
                        onPressedIdChange = { pressedRoomId = it },
                        onRoomsChange = { orderedRooms.value = it },
                        onCommitOrder = { finalOrder ->
                            val cid = controllerId ?: return@ReorderableRoomsLayout
                            scope.launch {
                                val roomDao = db.roomDao()
                                finalOrder.forEachIndexed { index, r ->
                                    roomDao.setGridPos(cid, r.id, index)
                                }
                            }
                        },
                        onRequestDelete = { rid, title ->
                            pendingDeleteRoomId = rid
                            pendingDeleteRoomTitle = title
                        },
                        onRoomClick = onRoomClick?.let { cb ->
                            { rid, title, iconId -> cb(rid, title, iconId) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val canAddRoom = controllerId != null && rooms != null && rooms.size < 16
                    SecondaryButton(
                        text = "Добавить помещение",
                        enabled = canAddRoom,
                        onClick = {
                            val cid = controllerId ?: return@SecondaryButton
                            val current = rooms ?: return@SecondaryButton
                            val usedIds = current.asSequence().map { it.id }.toHashSet()
                            val newId = (0..15).firstOrNull { it !in usedIds } ?: return@SecondaryButton
                            val nextPos = (current.maxOfOrNull { it.gridPos } ?: -1) + 1
                            scope.launch {
                                db.roomDao().insert(
                                    RoomEntity(
                                        controllerId = cid,
                                        id = newId,
                                        gridPos = nextPos
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                SecondaryButton(
                    text = "Сменить пароль контроллера",
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (pendingDeleteRoomId != -1) {
            val text = if (pendingDeleteRoomTitle.isNotBlank()) {
                "Удалить помещение «$pendingDeleteRoomTitle»?"
            } else {
                "Удалить помещение?"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Удалить",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            val cid = controllerId
                            val rid = pendingDeleteRoomId
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                            pressedRoomId = -1
                            if (cid != null && rid != -1) {
                                scope.launch {
                                    val roomDao = db.roomDao()
                                    roomDao.deleteById(cid, rid)
                                    val remaining = roomDao.getAllOrdered(cid)
                                    remaining.forEachIndexed { index, r ->
                                        roomDao.setGridPos(cid, r.id, index)
                                    }
                                }
                            }
                        }

                        TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                            pressedRoomId = -1
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ReorderableRoomsLayout(
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
            androidx.compose.ui.geometry.Rect(
                offset = topLeft,
                size = androidx.compose.ui.geometry.Size(cardWpx, cardHpx)
            )
        }

        val anyDragging = draggingId != -1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                // Drag should win over scroll: pointerInput first, then PageContainer scroll.
                .pointerInput(n, maxWidth, modalVisible) {
                    if (modalVisible || n == 0) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startIndex = slotRects.indexOfFirst { it.contains(down.position) }
                        if (startIndex == -1) return@awaitEachGesture

                        awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        // From this point on, we must consume events so RoomIcon doesn't receive a click on release.
                        down.consume()

                        val list0 = roomsState.value
                        val room0 = list0.getOrNull(startIndex) ?: return@awaitEachGesture
                        val id = room0.id

                        // A long-press would otherwise trigger RoomIcon click on release.
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

                        // Clear click suppression shortly AFTER release (not from long-press start),
                        // otherwise holding the press longer would allow the click to slip through.
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
                        val toRaw = if (dropOver != -1) dropOver else hoverIndex
                        val to = toRaw
                        if (to == from) {
                            // User dragged but ended up in the same slot: no-op (do NOT delete).
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
            rooms.forEachIndexed { index, r ->
                key(r.id) {
                    val topLeft = slotPositions.getOrNull(index) ?: Offset.Zero
                    val target = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    val isDragging = r.id == draggingId
                    val animOffset by animateIntOffsetAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 600),
                        label = "roomOffset"
                    )
                    val isPressed = r.id == pressedId || isDragging

                    val title = r.name.ifBlank { "Помещение ${r.id + 1}" }
                    val icon = iconResId(
                        context = context,
                        iconId = r.icoNum,
                        fallback = R.drawable.location_208_kuhnya
                    )

                    RoomIcon(
                        text = title,
                        iconResId = icon,
                        onClick = onRoomClick?.let { cb ->
                            {
                                if (suppressClickRoomId == r.id) {
                                    suppressClickRoomId = -1
                                } else {
                                    cb(r.id, title, r.icoNum)
                                }
                            }
                        },
                        // Long-press is reserved for reorder/delete intent (like controllers).
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
                    )
                }
            }
        }

        // Draw dragged item above the grid to keep shadow visible.
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

@Composable
private fun ScheduleCard(
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_M))
            .background(PixsoColors.Color_Bg_bg_surface)
            .padding(PixsoDimens.Numeric_20),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_24)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = "Расписание",
                style = HeadlineExtraSmall,
                color = PixsoColors.Color_Text_text_1_level
            )

            Switch(
                isChecked = isEnabled,
                onCheckedChange = { isEnabled = it },
                enabled = true
            )
        }

        SecondaryButton(
            text = "Настроить",
            onClick = onConfigureClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
