package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.Lum
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.PresSensor
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Page for a single location (singular).
 */
@Composable
fun PageLocation(
    location: LocationItem,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRoomClick: (roomId: Int, roomTitle: String, roomIconId: Int) -> Unit,
    onLumClick: (luminaireId: Long) -> Unit,
    onSensorPressSettingsClick: (sensorId: Long) -> Unit,
    onSensorBrightSettingsClick: (sensorId: Long) -> Unit,
    onButtonPanelClick: (buttonPanelId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controllerId = location.controllerId
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val roomsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<com.awada.synapse.db.RoomEntity>?>(emptyList())
        } else {
            db.roomDao()
                .observeAll(controllerId)
                .map<List<com.awada.synapse.db.RoomEntity>, List<com.awada.synapse.db.RoomEntity>?> { it }
        }
    }.collectAsState(initial = null)

    val luminairesOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<com.awada.synapse.db.LuminaireEntity>?>(emptyList())
        } else {
            db.luminaireDao()
                .observeAll(controllerId, roomId = null)
                .map<List<com.awada.synapse.db.LuminaireEntity>, List<com.awada.synapse.db.LuminaireEntity>?> { it }
        }
    }.collectAsState(initial = null)

    val buttonPanelsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<com.awada.synapse.db.ButtonPanelEntity>?>(emptyList())
        } else {
            db.buttonPanelDao()
                .observeAll(controllerId, roomId = null)
                .map<List<com.awada.synapse.db.ButtonPanelEntity>, List<com.awada.synapse.db.ButtonPanelEntity>?> { it }
        }
    }.collectAsState(initial = null)

    val presSensorsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<com.awada.synapse.db.PresSensorEntity>?>(emptyList())
        } else {
            db.presSensorDao()
                .observeAll(controllerId, roomId = null)
                .map<List<com.awada.synapse.db.PresSensorEntity>, List<com.awada.synapse.db.PresSensorEntity>?> { it }
        }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<com.awada.synapse.db.BrightSensorEntity>?>(emptyList())
        } else {
            db.brightSensorDao()
                .observeAll(controllerId, roomId = null)
                .map<List<com.awada.synapse.db.BrightSensorEntity>, List<com.awada.synapse.db.BrightSensorEntity>?> { it }
        }
    }.collectAsState(initial = null)

    var draggingKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pressedKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingDeleteKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingDeleteTitle by remember { mutableStateOf("") }
    val orderedKeysState = remember { mutableStateOf<List<DeviceKey>>(emptyList()) }
    val roomBoundsById = remember { mutableStateMapOf<Int, Rect>() }

    val dotColors = remember {
        listOf(
            PixsoColors.Color_Bg_bg_surface,
            PixsoColors.Color_Border_border_error,
            PixsoColors.Color_Border_border_focus,
            PixsoColors.Color_State_on_disabled
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = location.title,
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val iconSize = 82.dp
                    val rooms = roomsOrNull
                    val showRooms = rooms != null && rooms.isNotEmpty()
                    val luminaires = luminairesOrNull
                    val panels = buttonPanelsOrNull
                    val pres = presSensorsOrNull
                    val bright = brightSensorsOrNull

                    // Prevent a brief "empty" flicker before first DB emission.
                    val ready =
                        rooms != null && luminaires != null && panels != null && pres != null && bright != null

                    if (ready && showRooms) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            rooms!!
                                .chunked(2)
                                .forEach { rowRooms ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowRooms.forEach { r ->
                                            val title = r.name.ifBlank { "Помещение ${r.id + 1}" }
                                            val icon = iconResId(
                                                context = context,
                                                iconId = r.icoNum,
                                                fallback = com.awada.synapse.R.drawable.location_208_kuhnya
                                            )
                                            RoomIcon(
                                                text = title,
                                                iconResId = icon,
                                                onClick = { onRoomClick(r.id, title, r.icoNum) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .onGloballyPositioned { coordinates ->
                                                        roomBoundsById[r.id] = coordinates.boundsInRoot()
                                                    }
                                            )
                                        }
                                        if (rowRooms.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                        }
                    }

                    if (ready && showRooms) {
                        Spacer(modifier = Modifier.height(15.dp))
                    }

                    if (ready) {
                        data class DeviceInfo(
                            val key: DeviceKey,
                            val gridPos: Int,
                            val titleForDelete: String,
                            val content: @Composable (Boolean, Boolean, Modifier) -> Unit
                        )

                        val infoByKey: Map<DeviceKey, DeviceInfo> = buildMap {
                            luminaires!!.forEachIndexed { idx, e ->
                                val icon = iconResId(
                                    context = context,
                                    iconId = e.icoNum,
                                    fallback = com.awada.synapse.R.drawable.luminaire_300_default
                                )
                                val key = DeviceKey(DeviceType.Luminaire, e.id)
                                put(
                                    key,
                                    DeviceInfo(
                                        key = key,
                                        gridPos = e.gridPos,
                                        titleForDelete = e.name.ifBlank { "Светильник" },
                                        content = { isPressed, suppressClick, m ->
                                            Lum(
                                                title = e.name.ifBlank { "Светильник" },
                                                iconSize = iconSize,
                                                brightnessPercent = 35,
                                                iconResId = icon,
                                                statusDotColor = dotColors[idx % dotColors.size],
                                                forcePressed = isPressed,
                                                onClick = if (suppressClick) null else { { onLumClick(e.id) } },
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                            panels!!.forEach { e ->
                                val key = DeviceKey(DeviceType.ButtonPanel, e.id)
                                put(
                                    key,
                                    DeviceInfo(
                                        key = key,
                                        gridPos = e.gridPos,
                                        titleForDelete = e.name.ifBlank { "Панель кнопок" },
                                        content = { isPressed, suppressClick, m ->
                                            ButtonPanel(
                                                title = e.name.ifBlank { "Панель\nкнопок" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onClick = if (suppressClick) null else { { onButtonPanelClick(e.id) } },
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                            pres!!.forEach { e ->
                                val key = DeviceKey(DeviceType.PresSensor, e.id)
                                put(
                                    key,
                                    DeviceInfo(
                                        key = key,
                                        gridPos = e.gridPos,
                                        titleForDelete = e.name.ifBlank { "Сенсор нажатия" },
                                        content = { isPressed, suppressClick, m ->
                                            PresSensor(
                                                title = e.name.ifBlank { "Сенсор\nнажатия" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onClick = if (suppressClick) null else { { onSensorPressSettingsClick(e.id) } },
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                            bright!!.forEach { e ->
                                val key = DeviceKey(DeviceType.BrightSensor, e.id)
                                put(
                                    key,
                                    DeviceInfo(
                                        key = key,
                                        gridPos = e.gridPos,
                                        titleForDelete = e.name.ifBlank { "Сенсор яркости" },
                                        content = { isPressed, suppressClick, m ->
                                            BrightSensor(
                                                title = e.name.ifBlank { "Сенсор\nяркости" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onClick = if (suppressClick) null else { { onSensorBrightSettingsClick(e.id) } },
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                        }

                        val initialOrder: List<DeviceKey> =
                            infoByKey.values
                                .sortedWith(
                                    compareBy<DeviceInfo> { it.gridPos }
                                        .thenBy { it.key.type.ordinal }
                                        .thenBy { it.key.id }
                                )
                                .map { it.key }

                        LaunchedEffect(initialOrder, draggingKey) {
                            if (draggingKey == null) {
                                orderedKeysState.value = initialOrder
                            }
                        }

                        fun commitOrder(finalKeys: List<DeviceKey>) {
                            scope.launch {
                                finalKeys.forEachIndexed { index, k ->
                                    when (k.type) {
                                        DeviceType.Luminaire -> db.luminaireDao().setGridPos(k.id, index)
                                        DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(k.id, index)
                                        DeviceType.PresSensor -> db.presSensorDao().setGridPos(k.id, index)
                                        DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(k.id, index)
                                    }
                                }
                            }
                        }

                        fun moveDeviceToRoom(key: DeviceKey, roomId: Int) {
                            val remainingKeys = orderedKeysState.value.filter { it != key }
                            orderedKeysState.value = remainingKeys
                            draggingKey = null
                            pressedKey = null
                            scope.launch {
                                when (key.type) {
                                    DeviceType.Luminaire -> db.luminaireDao().moveToRoom(key.id, roomId)
                                    DeviceType.ButtonPanel -> db.buttonPanelDao().moveToRoom(key.id, roomId)
                                    DeviceType.PresSensor -> db.presSensorDao().moveToRoom(key.id, roomId)
                                    DeviceType.BrightSensor -> db.brightSensorDao().moveToRoom(key.id, roomId)
                                }
                                remainingKeys.forEachIndexed { index, deviceKey ->
                                    when (deviceKey.type) {
                                        DeviceType.Luminaire -> db.luminaireDao().setGridPos(deviceKey.id, index)
                                        DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(deviceKey.id, index)
                                        DeviceType.PresSensor -> db.presSensorDao().setGridPos(deviceKey.id, index)
                                        DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(deviceKey.id, index)
                                    }
                                }
                            }
                        }

                        val orderedKeys = orderedKeysState.value.filter { it in infoByKey }
                        ReorderableKeyGrid(
                            keys = orderedKeys,
                            columns = 4,
                            draggingKey = draggingKey,
                            pressedKey = pressedKey,
                            modalVisible = pendingDeleteKey != null,
                            onDraggingKeyChange = { draggingKey = it },
                            onPressedKeyChange = { pressedKey = it },
                            onKeysChange = { orderedKeysState.value = it },
                            onCommitOrder = { commitOrder(it) },
                            onDropOutsideGrid = { key, itemCenterInRoot ->
                                val targetRoomId =
                                    rooms
                                        ?.firstOrNull { room ->
                                            roomBoundsById[room.id]?.contains(itemCenterInRoot) == true
                                        }
                                        ?.id
                                if (targetRoomId != null) {
                                    moveDeviceToRoom(key, targetRoomId)
                                    true
                                } else {
                                    false
                                }
                            },
                            onRequestDelete = { k ->
                                pendingDeleteKey = k
                                pendingDeleteTitle = infoByKey[k]?.titleForDelete.orEmpty()
                            },
                            itemHeight = 128.dp,
                            itemContent = { k, isPressed, suppressClick, m ->
                                infoByKey[k]?.content?.invoke(isPressed, suppressClick, m)
                            }
                        )

                    }
                }
            }
        }
        }

        if (pendingDeleteKey != null) {
            val keyToDelete = pendingDeleteKey!!
            val text = if (pendingDeleteTitle.isNotBlank()) {
                "Удалить устройство «$pendingDeleteTitle»?"
            } else {
                "Удалить устройство?"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Удалить",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            val remaining = orderedKeysState.value.filter { it != keyToDelete }
                            orderedKeysState.value = remaining
                            pendingDeleteKey = null
                            pendingDeleteTitle = ""
                            pressedKey = null
                            scope.launch {
                                when (keyToDelete.type) {
                                    DeviceType.Luminaire -> db.luminaireDao().deleteById(keyToDelete.id)
                                    DeviceType.ButtonPanel -> db.buttonPanelDao().deleteById(keyToDelete.id)
                                    DeviceType.PresSensor -> db.presSensorDao().deleteById(keyToDelete.id)
                                    DeviceType.BrightSensor -> db.brightSensorDao().deleteById(keyToDelete.id)
                                }
                                remaining.forEachIndexed { index, k ->
                                    when (k.type) {
                                        DeviceType.Luminaire -> db.luminaireDao().setGridPos(k.id, index)
                                        DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(k.id, index)
                                        DeviceType.PresSensor -> db.presSensorDao().setGridPos(k.id, index)
                                        DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(k.id, index)
                                    }
                                }
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pendingDeleteKey = null
                            pendingDeleteTitle = ""
                            pressedKey = null
                        }
                    }
                }
            )
        }
    }
}

