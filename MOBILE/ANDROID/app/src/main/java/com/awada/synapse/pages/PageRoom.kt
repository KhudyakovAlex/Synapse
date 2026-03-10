package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
@Composable
fun PageRoom(
    roomTitle: String,
    controllerId: Int,
    roomId: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
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

    PageContainer(
        title = roomTitle,
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val luminaires = luminairesOrNull
            val panels = buttonPanelsOrNull
            val pres = presSensorsOrNull
            val bright = brightSensorsOrNull
            val ready = luminaires != null && panels != null && pres != null && bright != null

            if (ready) {
                data class DeviceInfo(
                    val key: DeviceKey,
                    val gridPos: Int,
                    val titleForDelete: String,
                    val content: @Composable (Boolean, Boolean, Modifier) -> Unit
                )

                var draggingKey by remember { mutableStateOf<DeviceKey?>(null) }
                var pressedKey by remember { mutableStateOf<DeviceKey?>(null) }
                var pendingDeleteKey by remember { mutableStateOf<DeviceKey?>(null) }
                var pendingDeleteTitle by remember { mutableStateOf("") }
                val orderedKeysState = remember { mutableStateOf<List<DeviceKey>>(emptyList()) }

                val dotColors = remember {
                    listOf(
                        PixsoColors.Color_Bg_bg_surface,
                        PixsoColors.Color_Border_border_error,
                        PixsoColors.Color_Border_border_focus,
                        PixsoColors.Color_State_on_disabled
                    )
                }

                val iconSize = 82.dp

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
                    onRequestDelete = { k ->
                        pendingDeleteKey = k
                        pendingDeleteTitle = infoByKey[k]?.titleForDelete.orEmpty()
                    },
                    itemHeight = 128.dp,
                    itemContent = { k, isPressed, suppressClick, m ->
                        infoByKey[k]?.content?.invoke(isPressed, suppressClick, m)
                    }
                )

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
    }
}

