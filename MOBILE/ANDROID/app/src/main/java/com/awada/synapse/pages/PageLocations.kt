package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.LocationsContainer
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.components.vibrateStrongClick
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ControllerEntity
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Стартовый экран со списком локаций, переходом в настройки и поиском контроллера. */
internal val PageLocationsLlmDescriptor = LLMPageDescriptor(
    fileName = "PageLocations",
    screenName = "Locations",
    titleRu = "Локации",
    description = "Показывает список локаций и позволяет открыть детали локации, настройки приложения или поиск контроллера."
)

@Composable
fun PageLocations(
    onSettingsClick: () -> Unit,
    onLocationClick: (LocationItem) -> Unit,
    onFindControllerClick: (() -> Unit)? = null,
    appearingLocationId: Int? = null,
    onAppearingLocationConsumed: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val controllersOrNull by remember(db) {
        db.controllerDao()
            .observeAll()
            .map<List<com.awada.synapse.db.ControllerEntity>, List<com.awada.synapse.db.ControllerEntity>?> { it }
    }.collectAsState(initial = null)
    val ordered = remember { mutableStateOf<List<ControllerEntity>>(emptyList()) }
    var draggingId by remember { mutableIntStateOf(-1) }
    var pressedId by remember { mutableIntStateOf(-1) }
    var pendingDeleteId by remember { mutableIntStateOf(-1) }

    // Prevent flicker: don't show "empty" CTA until first DB emission arrives.
    if (controllersOrNull == null) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }
        return
    }

    val controllers = controllersOrNull ?: emptyList()
    LaunchedEffect(controllers, draggingId) {
        if (draggingId == -1) {
            ordered.value = controllers
        }
    }
    val visibleControllers = if (draggingId == -1) controllers else ordered.value

    fun openLocation(controller: ControllerEntity) {
        onLocationClick(
            LocationItem(
                controllerId = controller.id,
                title = controller.name.ifBlank { "Контроллер ${controller.id}" },
                iconResId = iconResId(context, controller.icoNum)
            )
        )
    }

    if (visibleControllers.isEmpty()) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = modifier
        ) {
            LocationsContainer(
                locations = emptyList(),
                modifier = Modifier.fillMaxWidth(),
                iconSize = 56.dp * 1.1f,
                fillAvailableHeight = false,
                onEmptyButtonClick = { onFindControllerClick?.invoke() }
            )
        }
    } else if (visibleControllers.size > 5) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PixsoDimens.Numeric_16)
            ) {
                ReorderableControllersGrid(
                    controllers = visibleControllers,
                    draggingId = draggingId,
                    pressedId = pressedId,
                    modalVisible = pendingDeleteId != -1,
                    onDraggingIdChange = { draggingId = it },
                    onPressedIdChange = { pressedId = it },
                    onControllersChange = { ordered.value = it },
                    onCommitOrder = { finalOrder ->
                        scope.launch {
                            val dao = db.controllerDao()
                            dao.replaceOrder(finalOrder.map { it.id })
                        }
                    },
                    onRequestDelete = { pendingDeleteId = it },
                    onLongPressActivated = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vibrateStrongClick(context)
                    },
                    onControllerClick = { controller ->
                        openLocation(controller)
                    },
                    appearingControllerId = appearingLocationId,
                    onAppearingControllerConsumed = onAppearingLocationConsumed
                )
            }
        }
    } else {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = false,
            bottomSpacerHeightOverride = 0.dp,
            modifier = modifier
        ) {
            val bottomInset = LocalBottomOverlayInset.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = bottomInset),
                contentAlignment = Alignment.Center
            ) {
                ReorderableControllersGrid(
                    controllers = visibleControllers,
                    draggingId = draggingId,
                    pressedId = pressedId,
                    modalVisible = pendingDeleteId != -1,
                    onDraggingIdChange = { draggingId = it },
                    onPressedIdChange = { pressedId = it },
                    onControllersChange = { ordered.value = it },
                    onCommitOrder = { finalOrder ->
                        scope.launch {
                            val dao = db.controllerDao()
                            dao.replaceOrder(finalOrder.map { it.id })
                        }
                    },
                    onRequestDelete = { pendingDeleteId = it },
                    onLongPressActivated = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vibrateStrongClick(context)
                    },
                    onControllerClick = { controller ->
                        openLocation(controller)
                    },
                    appearingControllerId = appearingLocationId,
                    onAppearingControllerConsumed = onAppearingLocationConsumed
                )
            }
        }
    }

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
                            db.buttonDao().deleteAllForController(id)
                            dao.deleteById(id)
                        }
                    }
                    else -> {
                        pendingDeleteId = -1
                        pressedId = -1
                    }
                }
            }
        )
    }
}
