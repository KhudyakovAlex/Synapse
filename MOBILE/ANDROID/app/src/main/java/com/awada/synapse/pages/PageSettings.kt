package com.awada.synapse.pages

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.LocationIcon
import com.awada.synapse.components.LocationsContainer
import com.awada.synapse.components.PrimaryIconLButton
import com.awada.synapse.components.controllerIconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ControllerEntity
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import kotlinx.coroutines.launch

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
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    val rects = remember { mutableStateMapOf<Int, Rect>() }
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
            isScrollable = false,
            modifier = Modifier.fillMaxSize()
        ) {
            if (ordered.value.isEmpty()) {
                LocationsContainer(
                    locations = emptyList(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onEmptyButtonClick = onFindControllerClick
                )
                return@PageContainer
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = PixsoDimens.Numeric_16)
            ) {
                Text(
                    text = "Чтобы добавить локацию, подключитесь к ее контроллеру",
                    style = TitleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryIconLButton(
                    text = "Найти контроллер",
                    onClick = { onFindControllerClick?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onFindControllerClick != null
                )
                Spacer(modifier = Modifier.height(20.dp))

                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onGloballyPositioned { coords ->
                            rootOrigin = coords.positionInRoot()
                        }
                        .verticalScroll(scrollState)
                        .pointerInput(ordered.value, rootOrigin) {
                            var currentPos = Offset.Zero
                            var changedOrder = false
                            var moved = false

                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // Fires exactly after long-press timeout, even without movement
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    val startRoot = rootOrigin + offset
                                    val startId = rects.entries.firstOrNull { it.value.contains(startRoot) }?.key

                                    currentPos = offset
                                    changedOrder = false
                                    moved = false
                                    draggingId = startId ?: -1
                                },
                                onDrag = { change, dragAmount ->
                                    if (draggingId == -1) return@detectDragGesturesAfterLongPress
                                    if (!moved && dragAmount.getDistance() > 0f) moved = true

                                    currentPos += dragAmount
                                    val posRoot = rootOrigin + currentPos
                                    val overId = rects.entries
                                        .firstOrNull { it.key != draggingId && it.value.contains(posRoot) }
                                        ?.key

                                    if (overId != null) {
                                        val list = ordered.value.toMutableList()
                                        val from = list.indexOfFirst { it.id == draggingId }
                                        val to = list.indexOfFirst { it.id == overId }
                                        if (from != -1 && to != -1 && from != to) {
                                            val item = list.removeAt(from)
                                            val adjTo = if (to > from) to - 1 else to
                                            list.add(adjTo, item)
                                            ordered.value = list
                                            changedOrder = true
                                        }
                                    }

                                    change.consume()
                                },
                                onDragEnd = {
                                    val endId = draggingId
                                    val finalOrder = ordered.value
                                    val didChange = changedOrder
                                    val didMove = moved

                                    draggingId = -1
                                    changedOrder = false
                                    moved = false

                                    if (didChange) {
                                        scope.launch {
                                            val dao = db.controllerDao()
                                            finalOrder.forEachIndexed { index, c ->
                                                dao.setGridPos(c.id, index)
                                            }
                                        }
                                    } else if (!didMove && endId != -1) {
                                        pendingDeleteId = endId
                                    }
                                },
                                onDragCancel = {
                                    draggingId = -1
                                    changedOrder = false
                                    moved = false
                                }
                            )
                        }
                ) {
                    ControllersIconLayout(
                        controllers = ordered.value,
                        draggingId = draggingId,
                        onItemPositioned = { id, rect -> rects[id] = rect }
                    )
                }
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
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ControllersIconLayout(
    controllers: List<ControllerEntity>,
    draggingId: Int,
    onItemPositioned: (id: Int, rectInRoot: Rect) -> Unit
) {
    val items = controllers.map { c ->
        c.id to (c.name.ifBlank { "Контроллер ${c.id}" } to controllerIconResId(c.icoNum))
    }

    fun Modifier.track(id: Int): Modifier =
        this
            .alpha(if (id == draggingId) 0.6f else 1f)
            .onGloballyPositioned { coords -> onItemPositioned(id, coords.boundsInRoot()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        when (items.size) {
            1 -> {
                val (id, data) = items.first()
                val (title, icon) = data
                LocationIcon(
                    title = title,
                    iconResId = icon,
                    cardSize = 156.dp * 1.5f,
                    iconSize = 56.dp * 1.5f,
                    contentOffsetY = 8.dp * 1.5f,
                    showTitle = true,
                    enabled = true,
                    onClick = null,
                    modifier = Modifier
                        .track(id)
                        .padding(vertical = 24.dp)
                )
            }
            2 -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items.forEach { (id, data) ->
                        val (title, icon) = data
                        LocationIcon(
                            title = title,
                            iconResId = icon,
                            showTitle = true,
                            enabled = true,
                            onClick = null,
                            modifier = Modifier
                                .track(id)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = items.chunked(2)
                    rows.forEachIndexed { _, row ->
                        if (row.size == 2) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { (id, data) ->
                                    val (title, icon) = data
                                    LocationIcon(
                                        title = title,
                                        iconResId = icon,
                                        showTitle = true,
                                        enabled = true,
                                        onClick = null,
                                        modifier = Modifier.track(id),
                                    )
                                }
                            }
                        } else {
                            val (id, data) = row.first()
                            val (title, icon) = data
                            LocationIcon(
                                title = title,
                                iconResId = icon,
                                showTitle = true,
                                enabled = true,
                                onClick = null,
                                modifier = Modifier.track(id)
                            )
                        }
                    }
                }
            }
        }
    }
}
