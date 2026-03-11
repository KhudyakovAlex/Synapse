package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.Lum
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.PresSensor
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
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
    onGroupClick: (groupId: Int) -> Unit,
    onLumClick: (luminaireId: Long) -> Unit,
    onSensorPressSettingsClick: (sensorId: Long) -> Unit,
    onSensorBrightSettingsClick: (sensorId: Long) -> Unit,
    onButtonPanelClick: (buttonPanelId: Long) -> Unit,
    appearingRoomId: Int? = null,
    onAppearingRoomConsumed: ((Int) -> Unit)? = null,
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
    var pendingDeleteRoomId by remember { mutableIntStateOf(-1) }
    var pendingDeleteRoomTitle by remember { mutableStateOf("") }
    val orderedKeysState = remember { mutableStateOf<List<DeviceKey>>(emptyList()) }
    val orderedRoomsState = remember { mutableStateOf<List<RoomEntity>>(emptyList()) }
    var draggingRoomId by remember { mutableIntStateOf(-1) }
    var pressedRoomId by remember { mutableIntStateOf(-1) }
    val roomBoundsById = remember { mutableStateMapOf<Int, Rect>() }
    val deviceCircleBoundsByKey = remember { mutableStateMapOf<DeviceKey, Rect>() }

    fun groupIdForKey(key: DeviceKey): Int? =
        when (key.type) {
            DeviceType.Luminaire -> luminairesOrNull?.firstOrNull { it.id == key.id }?.groupId
            DeviceType.BrightSensor -> brightSensorsOrNull?.firstOrNull { it.id == key.id }?.groupId
            DeviceType.ButtonPanel, DeviceType.PresSensor -> null
        }

    suspend fun controllerGroupKeys(groupId: Int): List<DeviceKey> {
        val cid = controllerId ?: return emptyList()
        return buildList {
            db.luminaireDao()
                .getAllForGroup(cid, groupId)
                .forEach { add(DeviceKey(DeviceType.Luminaire, it.id)) }
            db.brightSensorDao()
                .getAllForGroup(cid, groupId)
                .forEach { add(DeviceKey(DeviceType.BrightSensor, it.id)) }
        }.distinct()
    }

    fun visibleGroupKeys(groupId: Int): List<DeviceKey> =
        buildList {
            luminairesOrNull
                .orEmpty()
                .filter { it.roomId == null && it.groupId == groupId }
                .forEach { add(DeviceKey(DeviceType.Luminaire, it.id)) }
            brightSensorsOrNull
                .orEmpty()
                .filter { it.roomId == null && it.groupId == groupId }
                .forEach { add(DeviceKey(DeviceType.BrightSensor, it.id)) }
        }.distinct()

    LaunchedEffect(roomsOrNull, draggingRoomId) {
        if (draggingRoomId == -1) {
            val rooms = roomsOrNull ?: emptyList()
            orderedRoomsState.value = rooms
            val validIds = rooms.asSequence().map { it.id }.toSet()
            roomBoundsById.keys
                .filter { it !in validIds }
                .forEach(roomBoundsById::remove)
        }
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
                        val dimmedCircleAlpha = 0.55f
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
                        ReorderableRoomsGrid(
                            rooms = orderedRoomsState.value,
                            draggingId = draggingRoomId,
                            pressedId = pressedRoomId,
                            modalVisible = pendingDeleteRoomId != -1 || pendingDeleteKey != null,
                            appearingRoomId = appearingRoomId,
                            onAppearingRoomConsumed = onAppearingRoomConsumed,
                            onDraggingIdChange = { draggingRoomId = it },
                            onPressedIdChange = { pressedRoomId = it },
                            onRoomsChange = { orderedRoomsState.value = it },
                            onCommitOrder = { finalOrder ->
                                val cid = controllerId ?: return@ReorderableRoomsGrid
                                scope.launch {
                                    val roomDao = db.roomDao()
                                    finalOrder.forEachIndexed { index, room ->
                                        roomDao.setGridPos(cid, room.id, index)
                                    }
                                }
                            },
                            onRequestDelete = { roomId, title ->
                                pendingDeleteRoomId = roomId
                                pendingDeleteRoomTitle = title
                            },
                            onRoomClick = { roomId, title, iconId ->
                                onRoomClick(roomId, title, iconId)
                            },
                            onRoomBoundsChange = { roomId, bounds ->
                                roomBoundsById[roomId] = bounds
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (ready) {
                        data class DeviceInfo(
                            val key: DeviceKey,
                            val gridPos: Int,
                            val titleForDelete: String,
                            val groupId: Int?,
                            val content: @Composable (Boolean, Boolean, Float, Modifier) -> Unit
                        )

                        val infoByKey: Map<DeviceKey, DeviceInfo> = buildMap {
                            luminaires!!.forEach { e ->
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
                                        groupId = e.groupId,
                                        content = { isPressed, suppressClick, circleAlpha, m ->
                                            Lum(
                                                title = e.name.ifBlank { "Светильник" },
                                                iconSize = iconSize,
                                                brightnessPercent = e.bright,
                                                typeId = e.typeId,
                                                hue = e.hue,
                                                saturation = e.saturation,
                                                temperature = e.temperature,
                                                iconResId = icon,
                                                forcePressed = isPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                circleAlpha = circleAlpha,
                                                onClick = if (suppressClick) {
                                                    null
                                                } else {
                                                    { e.groupId?.let(onGroupClick) ?: onLumClick(e.id) }
                                                },
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
                                    groupId = null,
                                        content = { isPressed, suppressClick, circleAlpha, m ->
                                            ButtonPanel(
                                                title = e.name.ifBlank { "Панель\nкнопок" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                circleAlpha = circleAlpha,
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
                                    groupId = null,
                                        content = { isPressed, suppressClick, circleAlpha, m ->
                                            PresSensor(
                                                title = e.name.ifBlank { "Сенсор\nнажатия" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                circleAlpha = circleAlpha,
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
                                    groupId = e.groupId,
                                        content = { isPressed, suppressClick, circleAlpha, m ->
                                            BrightSensor(
                                                title = e.name.ifBlank { "Сенсор\nяркости" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                circleAlpha = circleAlpha,
                                                onClick = if (suppressClick) {
                                                    null
                                                } else {
                                                    { e.groupId?.let(onGroupClick) ?: onSensorBrightSettingsClick(e.id) }
                                                },
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
                            val groupedKeysToMove =
                                infoByKey[key]
                                    ?.groupId
                                    ?.let { groupId ->
                                        buildList {
                                            luminaires
                                                ?.filter { it.groupId == groupId }
                                                ?.forEach { add(DeviceKey(DeviceType.Luminaire, it.id)) }
                                            bright
                                                ?.filter { it.groupId == groupId }
                                                ?.forEach { add(DeviceKey(DeviceType.BrightSensor, it.id)) }
                                        }
                                    }
                                    .orEmpty()
                            val keysToMove =
                                (if (groupedKeysToMove.isEmpty()) listOf(key) else groupedKeysToMove)
                                    .distinct()
                            val remainingKeys = orderedKeysState.value.filter { it !in keysToMove }
                            orderedKeysState.value = remainingKeys
                            draggingKey = null
                            pressedKey = null
                            scope.launch {
                                val groupId = infoByKey[key]?.groupId
                                if (groupId != null && controllerId != null) {
                                    db.luminaireDao()
                                        .getAllForGroup(controllerId, groupId)
                                        .forEach { db.luminaireDao().moveToRoom(it.id, roomId) }
                                    db.brightSensorDao()
                                        .getAllForGroup(controllerId, groupId)
                                        .forEach { db.brightSensorDao().moveToRoom(it.id, roomId) }
                                } else {
                                    when (key.type) {
                                        DeviceType.Luminaire -> db.luminaireDao().moveToRoom(key.id, roomId)
                                        DeviceType.ButtonPanel -> db.buttonPanelDao().moveToRoom(key.id, roomId)
                                        DeviceType.PresSensor -> db.presSensorDao().moveToRoom(key.id, roomId)
                                        DeviceType.BrightSensor -> db.brightSensorDao().moveToRoom(key.id, roomId)
                                    }
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
                        val groupIdByKey: Map<DeviceKey, Int?> = infoByKey.values.associate { it.key to it.groupId }
                        val dimmedKeys =
                            if (draggingKey == null) {
                                groupLinkCoveredKeysInRoot(
                                    circleBoundsInRootByKey = deviceCircleBoundsByKey,
                                    groupIdByKey = groupIdByKey
                                )
                            } else {
                                emptySet()
                            }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            GroupLinksOverlay(
                                circleBoundsInRootByKey = deviceCircleBoundsByKey,
                                groupIdByKey = groupIdByKey,
                                visible = draggingKey == null,
                                modifier = Modifier.matchParentSize()
                            )
                            ReorderableKeyGrid(
                                keys = orderedKeys,
                                columns = 4,
                                rowSpacing = 0.dp,
                                draggingKey = draggingKey,
                                pressedKey = pressedKey,
                                modalVisible = pendingDeleteKey != null || pendingDeleteRoomId != -1,
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
                                    infoByKey[k]?.content?.invoke(
                                        isPressed,
                                        suppressClick,
                                        if (k in dimmedKeys) dimmedCircleAlpha else 1f,
                                        m
                                    )
                                }
                            )
                        }

                    }
                }
            }
        }
        }

        if (pendingDeleteKey != null) {
            val keyToDelete = pendingDeleteKey!!
            val groupId = groupIdForKey(keyToDelete)
            Tooltip(
                text = null,
                primaryButtonText = "Удалить устройство",
                tertiaryButtonText = if (groupId != null) "Убрать устройство из группы" else null,
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            val keysToDelete = groupId?.let(::visibleGroupKeys).orEmpty()
                            val remaining =
                                orderedKeysState.value.filter {
                                    it !in if (keysToDelete.isEmpty()) listOf(keyToDelete) else keysToDelete
                                }
                            orderedKeysState.value = remaining
                            pendingDeleteKey = null
                            pendingDeleteTitle = ""
                            pressedKey = null
                            scope.launch {
                                val keysForController =
                                    groupId?.let { controllerGroupKeys(it) }.orEmpty()
                                (if (keysForController.isEmpty()) listOf(keyToDelete) else keysForController)
                                    .forEach { key ->
                                        when (key.type) {
                                            DeviceType.Luminaire -> db.luminaireDao().deleteById(key.id)
                                            DeviceType.ButtonPanel -> db.buttonPanelDao().deleteById(key.id)
                                            DeviceType.PresSensor -> db.presSensorDao().deleteById(key.id)
                                            DeviceType.BrightSensor -> db.brightSensorDao().deleteById(key.id)
                                        }
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
                        TooltipResult.Tertiary -> {
                            pendingDeleteKey = null
                            pendingDeleteTitle = ""
                            pressedKey = null
                            scope.launch {
                                groupId?.let { gid ->
                                    controllerGroupKeys(gid).forEach { key ->
                                        when (key.type) {
                                            DeviceType.Luminaire -> db.luminaireDao().moveToGroup(key.id, null)
                                            DeviceType.BrightSensor -> db.brightSensorDao().moveToGroup(key.id, null)
                                            DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                        }
                                    }
                                }
                            }
                        }
                        TooltipResult.Quaternary, TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pendingDeleteKey = null
                            pendingDeleteTitle = ""
                            pressedKey = null
                        }
                    }
                }
            )
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
                            val roomId = pendingDeleteRoomId
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                            pressedRoomId = -1
                            draggingRoomId = -1
                            if (cid != null && roomId != -1) {
                                scope.launch {
                                    val roomDao = db.roomDao()
                                    roomDao.deleteById(cid, roomId)
                                    val remaining = roomDao.getAllOrdered(cid)
                                    remaining.forEachIndexed { index, room ->
                                        roomDao.setGridPos(cid, room.id, index)
                                    }
                                }
                            }
                        }
                        else -> {
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                            pressedRoomId = -1
                            draggingRoomId = -1
                        }
                    }
                }
            )
        }
    }
}

