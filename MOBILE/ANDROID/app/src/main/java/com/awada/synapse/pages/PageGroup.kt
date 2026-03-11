package com.awada.synapse.pages

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.Lum
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.defaultGroupName
import com.awada.synapse.db.displayName
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun PageGroup(
    controllerId: Int,
    groupId: Int,
    onBackClick: () -> Unit,
    onLumClick: (luminaireId: Long) -> Unit,
    onSensorBrightSettingsClick: (sensorId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    val groupOrNull by remember(db, groupId) {
        db.groupDao().observeById(groupId)
    }.collectAsState(initial = null)

    val luminairesOrNull by remember(db, controllerId, groupId) {
        db.luminaireDao()
            .observeAllForGroup(controllerId, groupId)
            .map<List<LuminaireEntity>, List<LuminaireEntity>?> { it }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId, groupId) {
        db.brightSensorDao()
            .observeAllForGroup(controllerId, groupId)
            .map<List<BrightSensorEntity>, List<BrightSensorEntity>?> { it }
    }.collectAsState(initial = null)

    val deviceCircleBoundsByKey = remember { mutableStateMapOf<DeviceKey, Rect>() }
    var pressedKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingRemoveKey by remember { mutableStateOf<DeviceKey?>(null) }
    var pendingRemoveTitle by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = groupOrNull?.displayName() ?: defaultGroupName(groupId),
            onBackClick = onBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            val luminaires = luminairesOrNull
            val brightSensors = brightSensorsOrNull
            val ready = luminaires != null && brightSensors != null

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
                        val titleForRemove: String,
                        val onClick: () -> Unit,
                        val content: @Composable (Boolean, Boolean, Modifier) -> Unit
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val columnSpacing = 16.dp
                        val rowSpacing = 4.dp
                        val columns = 4
                        val itemWidth = (maxWidth - columnSpacing * (columns - 1)) / columns.toFloat()
                        val itemHeight = itemWidth
                        val iconSize = (itemWidth * 0.78f).coerceIn(56.dp, 82.dp)

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
                                        titleForRemove = e.name.ifBlank { "Светильник" },
                                        onClick = { onLumClick(e.id) },
                                        content = { isPressed: Boolean, isClickPressed: Boolean, m: Modifier ->
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
                                                forceSecondaryPressed = isClickPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                onClick = null,
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                            brightSensors!!.forEach { e ->
                                val key = DeviceKey(DeviceType.BrightSensor, e.id)
                                put(
                                    key,
                                    DeviceInfo(
                                        key = key,
                                        gridPos = e.gridPos,
                                        titleForRemove = e.name.ifBlank { "Сенсор яркости" },
                                        onClick = { onSensorBrightSettingsClick(e.id) },
                                        content = { isPressed: Boolean, isClickPressed: Boolean, m: Modifier ->
                                            BrightSensor(
                                                title = e.name.ifBlank { "Сенсор\nяркости" },
                                                iconSize = iconSize,
                                                forcePressed = isPressed,
                                                forceSecondaryPressed = isClickPressed,
                                                onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                                onClick = null,
                                                modifier = m
                                            )
                                        }
                                    )
                                )
                            }
                        }

                        val orderedKeys = infoByKey.values
                            .sortedWith(
                                compareBy<DeviceInfo> { it.gridPos }
                                    .thenBy { it.key.type.ordinal }
                                    .thenBy { it.key.id }
                            )
                            .map { it.key }
                        val groupIdByKey = orderedKeys.associateWith { groupId }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            GroupLinksOverlay(
                                circleBoundsInRootByKey = deviceCircleBoundsByKey,
                                groupIdByKey = groupIdByKey,
                                visible = true,
                                modifier = Modifier.matchParentSize()
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing)
                            ) {
                                orderedKeys.chunked(columns).forEach { rowKeys ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(columnSpacing)
                                    ) {
                                        rowKeys.forEach { key ->
                                            val info = infoByKey[key]
                                            if (info != null) {
                                                val interactionSource = remember { MutableInteractionSource() }
                                                val isClickPressed by interactionSource.collectIsPressedAsState()
                                                Box(
                                                    modifier = Modifier
                                                        .width(itemWidth)
                                                        .height(itemHeight)
                                                        .combinedClickable(
                                                            interactionSource = interactionSource,
                                                            indication = null,
                                                            onClick = info.onClick,
                                                            onLongClick = {
                                                                pressedKey = key
                                                                pendingRemoveKey = key
                                                                pendingRemoveTitle = info.titleForRemove
                                                            }
                                                        ),
                                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                                ) {
                                                    info.content(
                                                        key == pressedKey,
                                                        isClickPressed && key != pressedKey,
                                                        Modifier
                                                    )
                                                }
                                            }
                                        }

                                        repeat(columns - rowKeys.size) {
                                            Box(
                                                modifier = Modifier
                                                    .width(itemWidth)
                                                    .height(itemHeight)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (pendingRemoveKey != null) {
            val keyToRemove = pendingRemoveKey!!
            val text = if (pendingRemoveTitle.isNotBlank()) {
                "Убрать устройство «$pendingRemoveTitle» из группы?"
            } else {
                "Убрать устройство из группы?"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Убрать",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            pressedKey = null
                            pendingRemoveKey = null
                            pendingRemoveTitle = ""
                            scope.launch {
                                when (keyToRemove.type) {
                                    DeviceType.Luminaire -> db.luminaireDao().moveToGroup(keyToRemove.id, null)
                                    DeviceType.BrightSensor -> db.brightSensorDao().moveToGroup(keyToRemove.id, null)
                                    DeviceType.ButtonPanel, DeviceType.PresSensor -> Unit
                                }
                            }
                        }
                        TooltipResult.Tertiary, TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pressedKey = null
                            pendingRemoveKey = null
                            pendingRemoveTitle = ""
                        }
                    }
                }
            )
        }
    }
}
