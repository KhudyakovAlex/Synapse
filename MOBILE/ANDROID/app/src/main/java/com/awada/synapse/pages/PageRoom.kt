package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.Lum
import com.awada.synapse.components.PresSensor
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.ButtonPanelEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.PresSensorEntity
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Page for a single room (within a location).
 * Displays devices (luminaires, sensors, button panels) from the database for this room.
 */
/** Экран помещения с устройствами и дочерними переходами внутри выбранного контроллера. */
internal val PageRoomLlmDescriptor = LLMPageDescriptor(
    fileName = "PageRoom",
    screenName = "Room",
    titleRu = "Помещение",
    description = "Показывает выбранное помещение и позволяет открыть его группы, светильники, датчики и кнопочные панели."
)

@Composable
fun PageRoom(
    roomTitle: String,
    controllerId: Int,
    roomId: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGroupClick: (groupId: Int) -> Unit,
    onLumClick: (luminaireId: Long) -> Unit,
    onSensorPressSettingsClick: (sensorId: Long) -> Unit,
    onSensorBrightSettingsClick: (sensorId: Long) -> Unit,
    onButtonPanelSettingsClick: (buttonPanelId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    val luminairesOrNull by remember(db, controllerId, roomId) {
        db.luminaireDao()
            .observeAll(controllerId, roomId)
            .map<List<LuminaireEntity>, List<LuminaireEntity>?> { it }
    }.collectAsState(initial = null)

    val buttonPanelsOrNull by remember(db, controllerId, roomId) {
        db.buttonPanelDao()
            .observeAll(controllerId, roomId)
            .map<List<ButtonPanelEntity>, List<ButtonPanelEntity>?> { it }
    }.collectAsState(initial = null)

    val presSensorsOrNull by remember(db, controllerId, roomId) {
        db.presSensorDao()
            .observeAll(controllerId, roomId)
            .map<List<PresSensorEntity>, List<PresSensorEntity>?> { it }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId, roomId) {
        db.brightSensorDao()
            .observeAll(controllerId, roomId)
            .map<List<BrightSensorEntity>, List<BrightSensorEntity>?> { it }
    }.collectAsState(initial = null)

    var draggingKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pressedKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingDeleteKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingDeleteTitle by remember { mutableStateOf("") }
    var pendingDropSourceKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingDropTargetKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingNoFreeGroupTooltip by remember { mutableStateOf(false) }
    var locallyHiddenKeys by remember { mutableStateOf<Set<DeviceKey>>(emptySet()) }
    val orderedKeysState = remember { mutableStateOf<List<DeviceKey>>(emptyList()) }
    val deviceCircleBoundsByKey = remember { mutableStateMapOf<DeviceKey, Rect>() }

    fun groupIdForKey(key: DeviceKey): Int? =
        when (key.type) {
            DeviceType.Luminaire -> luminairesOrNull?.firstOrNull { it.id == key.id }?.groupId
            DeviceType.BrightSensor -> brightSensorsOrNull?.firstOrNull { it.id == key.id }?.groupId
            DeviceType.ButtonPanel, DeviceType.PresSensor -> null
        }

    fun groupedKeysFor(groupId: Int): List<DeviceKey> =
        buildList {
            luminairesOrNull
                .orEmpty()
                .filter { it.groupId == groupId }
                .forEach { add(DeviceKey(DeviceType.Luminaire, it.id)) }
            brightSensorsOrNull
                .orEmpty()
                .filter { it.groupId == groupId }
                .forEach { add(DeviceKey(DeviceType.BrightSensor, it.id)) }
        }.distinct()

    fun groupedKeysInCurrentOrder(groupId: Int): List<DeviceKey> =
        orderedKeysState.value.filter { groupIdForKey(it) == groupId }

    suspend fun controllerGroupKeys(groupId: Int): List<DeviceKey> =
        buildList {
            db.luminaireDao()
                .getAllForGroup(controllerId, groupId)
                .forEach { add(DeviceKey(DeviceType.Luminaire, it.id)) }
            db.brightSensorDao()
                .getAllForGroup(controllerId, groupId)
                .forEach { add(DeviceKey(DeviceType.BrightSensor, it.id)) }
        }.distinct()

    fun supportsGrouping(key: DeviceKey): Boolean =
        when (key.type) {
            DeviceType.Luminaire, DeviceType.BrightSensor -> true
            DeviceType.ButtonPanel, DeviceType.PresSensor -> false
        }

    suspend fun findEmptyGroupId(): Int? {
        val usedGroupIds =
            buildSet {
                db.luminaireDao()
                    .getAllForController(controllerId)
                    .mapNotNullTo(this) { it.groupId }
                db.brightSensorDao()
                    .getAllForController(controllerId)
                    .mapNotNullTo(this) { it.groupId }
            }
        return (0..15).firstOrNull { it !in usedGroupIds }
    }

    fun moveKeyWithinGrid(sourceKey: DeviceKey, targetKey: DeviceKey) {
        val currentKeys = orderedKeysState.value
        val from = currentKeys.indexOf(sourceKey)
        val to = currentKeys.indexOf(targetKey)
        if (from == -1 || to == -1 || from == to) return
        val newList = currentKeys.toMutableList()
        val movedKey = newList.removeAt(from)
        newList.add(to.coerceIn(0, newList.size), movedKey)
        orderedKeysState.value = newList
        scope.launch {
            newList.forEachIndexed { index, k ->
                when (k.type) {
                    DeviceType.Luminaire -> db.luminaireDao().setGridPos(k.id, index)
                    DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(k.id, index)
                    DeviceType.PresSensor -> db.presSensorDao().setGridPos(k.id, index)
                    DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(k.id, index)
                }
            }
        }
    }

    fun insertKeyAfterTarget(sourceKey: DeviceKey, targetKey: DeviceKey) {
        val newList = orderedKeysState.value.toMutableList()
        newList.remove(sourceKey)
        val targetIndex = newList.indexOf(targetKey)
        val insertIndex =
            if (targetIndex == -1) {
                newList.size
            } else {
                (targetIndex + 1).coerceAtMost(newList.size)
            }
        newList.add(insertIndex, sourceKey)
        orderedKeysState.value = newList
        scope.launch {
            newList.forEachIndexed { index, k ->
                when (k.type) {
                    DeviceType.Luminaire -> db.luminaireDao().setGridPos(k.id, index)
                    DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(k.id, index)
                    DeviceType.PresSensor -> db.presSensorDao().setGridPos(k.id, index)
                    DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(k.id, index)
                }
            }
        }
    }

    fun insertKeyAfterLastVisibleGroupMember(
        sourceKey: DeviceKey,
        groupId: Int,
        fallbackTargetKey: DeviceKey
    ) {
        val newList = orderedKeysState.value.toMutableList()
        newList.remove(sourceKey)
        val lastGroupIndex = newList.indexOfLast { groupIdForKey(it) == groupId }
        val fallbackIndex = newList.indexOf(fallbackTargetKey)
        val insertAfterIndex =
            if (lastGroupIndex != -1) lastGroupIndex else fallbackIndex
        val insertIndex =
            if (insertAfterIndex == -1) {
                newList.size
            } else {
                (insertAfterIndex + 1).coerceAtMost(newList.size)
            }
        newList.add(insertIndex, sourceKey)
        orderedKeysState.value = newList
        scope.launch {
            newList.forEachIndexed { index, k ->
                when (k.type) {
                    DeviceType.Luminaire -> db.luminaireDao().setGridPos(k.id, index)
                    DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(k.id, index)
                    DeviceType.PresSensor -> db.presSensorDao().setGridPos(k.id, index)
                    DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(k.id, index)
                }
            }
        }
    }

    fun moveKeysOutOfRoom(keysToMove: List<DeviceKey>) {
        locallyHiddenKeys = locallyHiddenKeys + keysToMove
        val remaining = orderedKeysState.value.filter { it !in keysToMove }
        orderedKeysState.value = remaining
        pendingDeleteKey = null
        pendingDeleteTitle = ""
        pressedKey = null
        scope.launch {
            keysToMove.forEach { key ->
                when (key.type) {
                    DeviceType.Luminaire -> db.luminaireDao().moveToRoom(key.id, null)
                    DeviceType.ButtonPanel -> db.buttonPanelDao().moveToRoom(key.id, null)
                    DeviceType.PresSensor -> db.presSensorDao().moveToRoom(key.id, null)
                    DeviceType.BrightSensor -> db.brightSensorDao().moveToRoom(key.id, null)
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

    suspend fun appendKeysToLocationEnd(keysToAppend: List<DeviceKey>) {
        val rootKeys =
            buildList {
                db.luminaireDao()
                    .getAllOrdered(controllerId, roomId = null)
                    .forEach { add(DeviceKey(DeviceType.Luminaire, it.id) to it.gridPos) }
                db.buttonPanelDao()
                    .getAllOrdered(controllerId, roomId = null)
                    .forEach { add(DeviceKey(DeviceType.ButtonPanel, it.id) to it.gridPos) }
                db.presSensorDao()
                    .getAllOrdered(controllerId, roomId = null)
                    .forEach { add(DeviceKey(DeviceType.PresSensor, it.id) to it.gridPos) }
                db.brightSensorDao()
                    .getAllOrdered(controllerId, roomId = null)
                    .forEach { add(DeviceKey(DeviceType.BrightSensor, it.id) to it.gridPos) }
            }
                .sortedWith(
                    compareBy<Pair<DeviceKey, Int>> { it.second }
                        .thenBy { it.first.type.ordinal }
                        .thenBy { it.first.id }
                )
                .map { it.first }

        val finalKeys =
            rootKeys.filter { it !in keysToAppend } +
                keysToAppend.distinct()

        finalKeys.forEachIndexed { index, key ->
            when (key.type) {
                DeviceType.Luminaire -> db.luminaireDao().setGridPos(key.id, index)
                DeviceType.ButtonPanel -> db.buttonPanelDao().setGridPos(key.id, index)
                DeviceType.PresSensor -> db.presSensorDao().setGridPos(key.id, index)
                DeviceType.BrightSensor -> db.brightSensorDao().setGridPos(key.id, index)
            }
        }
    }

    fun deleteKeys(keysToDelete: List<DeviceKey>) {
        locallyHiddenKeys = locallyHiddenKeys + keysToDelete
        val remaining = orderedKeysState.value.filter { it !in keysToDelete }
        orderedKeysState.value = remaining
        pendingDeleteKey = null
        pendingDeleteTitle = ""
        pressedKey = null
        scope.launch {
            keysToDelete.forEach { key ->
                when (key.type) {
                    DeviceType.Luminaire -> db.luminaireDao().deleteById(key.id)
                    DeviceType.ButtonPanel -> {
                        db.buttonDao().deleteAllForPanel(key.id)
                        db.buttonPanelDao().deleteById(key.id)
                    }
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

    fun removeKeysFromGroup(keysToUpdate: List<DeviceKey>) {
        pendingDeleteKey = null
        pendingDeleteTitle = ""
        pressedKey = null
        scope.launch {
            keysToUpdate.forEach { key ->
                when (key.type) {
                    DeviceType.Luminaire -> db.luminaireDao().moveToGroup(key.id, null)
                    DeviceType.BrightSensor -> db.brightSensorDao().moveToGroup(key.id, null)
                    DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = roomTitle,
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            val luminaires = luminairesOrNull
            val panels = buttonPanelsOrNull
            val pres = presSensorsOrNull
            val bright = brightSensorsOrNull
            val ready = luminaires != null && panels != null && pres != null && bright != null

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (ready) {
                    data class DeviceInfo(
                        val key: DeviceKey,
                        val gridPos: Int,
                        val titleForDelete: String,
                        val groupId: Int?,
                        val content: @Composable (Boolean, Boolean, Float, Modifier) -> Unit
                    )

                    val iconSize = 82.dp
                    val dimmedCircleAlpha = 0.55f

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
                                            onClick = if (suppressClick) null else { { onButtonPanelSettingsClick(e.id) } },
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
                    val visibleInfoByKey = infoByKey.filterKeys { it !in locallyHiddenKeys }

                    LaunchedEffect(infoByKey.keys) {
                        locallyHiddenKeys = locallyHiddenKeys.intersect(infoByKey.keys)
                    }

                    val initialOrder: List<DeviceKey> =
                        visibleInfoByKey.values
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

                    val orderedKeys = orderedKeysState.value.filter { it in visibleInfoByKey }
                    val visibleKeys = orderedKeys.toSet()
                    val visibleCircleBoundsByKey =
                        deviceCircleBoundsByKey.filterKeys { it in visibleKeys }
                    val groupIdByKey: Map<DeviceKey, Int?> =
                        orderedKeys.associateWith { visibleInfoByKey[it]?.groupId }
                    val dimmedKeys =
                        if (draggingKey == null) {
                            groupLinkCoveredKeysInRoot(
                                circleBoundsInRootByKey = visibleCircleBoundsByKey,
                                groupIdByKey = groupIdByKey
                            )
                        } else {
                            emptySet()
                        }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        GroupLinksOverlay(
                            circleBoundsInRootByKey = visibleCircleBoundsByKey,
                            groupIdByKey = groupIdByKey,
                            visible = true,
                            modifier = Modifier.matchParentSize()
                        )
                        ReorderableKeyGrid(
                            keys = orderedKeys,
                            columns = 4,
                            rowSpacing = 0.dp,
                            draggingKey = draggingKey,
                            pressedKey = pressedKey,
                            modalVisible =
                                pendingDeleteKey != null ||
                                    pendingDropSourceKey != null ||
                                    pendingNoFreeGroupTooltip,
                            onDraggingKeyChange = { draggingKey = it },
                            onPressedKeyChange = { pressedKey = it },
                            onKeysChange = { orderedKeysState.value = it },
                            onCommitOrder = { commitOrder(it) },
                            onDropOverKey = { draggedKey, targetKey ->
                                if (
                                    groupIdForKey(draggedKey) == null &&
                                    supportsGrouping(draggedKey) &&
                                    supportsGrouping(targetKey)
                                ) {
                                    pendingDropSourceKey = draggedKey
                                    pendingDropTargetKey = targetKey
                                    true
                                } else {
                                    false
                                }
                            },
                            onRequestDelete = { k ->
                                pendingDeleteKey = k
                                pendingDeleteTitle = visibleInfoByKey[k]?.titleForDelete.orEmpty()
                            },
                            itemHeight = 128.dp,
                            itemContent = { k, isPressed, suppressClick, m ->
                                visibleInfoByKey[k]?.content?.invoke(
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

        if (pendingDeleteKey != null) {
            val keyToDelete = pendingDeleteKey!!
            val groupId = groupIdForKey(keyToDelete)
            val isGrouped = groupId != null
            Tooltip(
                text = null,
                primaryButtonText = "Удалить устройство",
                tertiaryButtonText = if (isGrouped) "Убрать устройство из группы" else "Убрать из помещения",
                quaternaryButtonText = if (isGrouped) "Убрать группу из помещения" else null,
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            if (groupId != null) {
                                val visibleKeys = groupedKeysFor(groupId)
                                val remaining = orderedKeysState.value.filter { it !in visibleKeys }
                                orderedKeysState.value = remaining
                                pendingDeleteKey = null
                                pendingDeleteTitle = ""
                                pressedKey = null
                                scope.launch {
                                    controllerGroupKeys(groupId).forEach { key ->
                                        when (key.type) {
                                            DeviceType.Luminaire -> db.luminaireDao().deleteById(key.id)
                                            DeviceType.BrightSensor -> db.brightSensorDao().deleteById(key.id)
                                            DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
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
                            } else {
                                deleteKeys(listOf(keyToDelete))
                            }
                        }
                        TooltipResult.Tertiary -> {
                            if (groupId != null) {
                                removeKeysFromGroup(listOf(keyToDelete))
                            } else {
                                moveKeysOutOfRoom(listOf(keyToDelete))
                            }
                        }
                        TooltipResult.Quaternary -> {
                            if (groupId != null) {
                                val visibleKeys = groupedKeysInCurrentOrder(groupId)
                                moveKeysOutOfRoom(visibleKeys)
                                scope.launch {
                                    controllerGroupKeys(groupId).forEach { key ->
                                        when (key.type) {
                                            DeviceType.Luminaire -> db.luminaireDao().moveToRoom(key.id, null)
                                            DeviceType.BrightSensor -> db.brightSensorDao().moveToRoom(key.id, null)
                                            DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                        }
                                    }
                                    appendKeysToLocationEnd(visibleKeys)
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

        if (pendingDropSourceKey != null && pendingDropTargetKey != null) {
            val sourceKey = pendingDropSourceKey!!
            val targetKey = pendingDropTargetKey!!
            Tooltip(
                text = null,
                primaryButtonText = "Переместить",
                tertiaryButtonText = "Сгруппировать",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            pendingDropSourceKey = null
                            pendingDropTargetKey = null
                            moveKeyWithinGrid(sourceKey, targetKey)
                        }
                        TooltipResult.Tertiary -> {
                            pendingDropSourceKey = null
                            pendingDropTargetKey = null
                            scope.launch {
                                val targetGroupId = groupIdForKey(targetKey)
                                if (targetGroupId != null) {
                                    when (sourceKey.type) {
                                        DeviceType.Luminaire ->
                                            db.luminaireDao().moveToGroup(sourceKey.id, targetGroupId)
                                        DeviceType.BrightSensor ->
                                            db.brightSensorDao().moveToGroup(sourceKey.id, targetGroupId)
                                        DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                    }
                                    insertKeyAfterLastVisibleGroupMember(
                                        sourceKey = sourceKey,
                                        groupId = targetGroupId,
                                        fallbackTargetKey = targetKey
                                    )
                                } else {
                                    val emptyGroupId = findEmptyGroupId()
                                    if (emptyGroupId == null) {
                                        pendingNoFreeGroupTooltip = true
                                        return@launch
                                    }
                                    when (targetKey.type) {
                                        DeviceType.Luminaire ->
                                            db.luminaireDao().moveToGroup(targetKey.id, emptyGroupId)
                                        DeviceType.BrightSensor ->
                                            db.brightSensorDao().moveToGroup(targetKey.id, emptyGroupId)
                                        DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                    }
                                    when (sourceKey.type) {
                                        DeviceType.Luminaire ->
                                            db.luminaireDao().moveToGroup(sourceKey.id, emptyGroupId)
                                        DeviceType.BrightSensor ->
                                            db.brightSensorDao().moveToGroup(sourceKey.id, emptyGroupId)
                                        DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                    }
                                    insertKeyAfterTarget(sourceKey, targetKey)
                                }
                            }
                        }
                        else -> {
                            pendingDropSourceKey = null
                            pendingDropTargetKey = null
                        }
                    }
                }
            )
        }

        if (pendingNoFreeGroupTooltip) {
            Tooltip(
                text = "Нет свободной группы",
                primaryButtonText = "Ок",
                secondaryButtonText = "Отмена",
                onResult = {
                    pendingNoFreeGroupTooltip = false
                }
            )
        }
    }
}

