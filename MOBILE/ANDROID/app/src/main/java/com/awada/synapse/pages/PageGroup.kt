package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.Lum
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.defaultGroupName
import com.awada.synapse.db.displayName
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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
                        val content: @Composable (Modifier) -> Unit
                    )

                    val iconSize = 82.dp
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
                                    content = { m ->
                                        Lum(
                                            title = e.name.ifBlank { "Светильник" },
                                            iconSize = iconSize,
                                            brightnessPercent = e.bright,
                                            typeId = e.typeId,
                                            hue = e.hue,
                                            saturation = e.saturation,
                                            temperature = e.temperature,
                                            iconResId = icon,
                                            onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                            onClick = { onLumClick(e.id) },
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
                                    content = { m ->
                                        BrightSensor(
                                            title = e.name.ifBlank { "Сенсор\nяркости" },
                                            iconSize = iconSize,
                                            onCircleBoundsInRoot = { r -> deviceCircleBoundsByKey[key] = r },
                                            onClick = { onSensorBrightSettingsClick(e.id) },
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
                        ReorderableKeyGrid(
                            keys = orderedKeys,
                            columns = 4,
                            draggingKey = null,
                            pressedKey = null,
                            modalVisible = true,
                            onDraggingKeyChange = {},
                            onPressedKeyChange = {},
                            onKeysChange = {},
                            onCommitOrder = {},
                            onRequestDelete = {},
                            rowSpacing = 4.dp,
                            itemHeight = 128.dp,
                            itemContent = { key, _, _, m ->
                                infoByKey[key]?.content?.invoke(m)
                            }
                        )
                    }
                }
            }
        }
    }
}
