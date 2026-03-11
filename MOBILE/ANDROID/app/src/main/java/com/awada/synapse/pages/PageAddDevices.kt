package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.Lum
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.displayName
import kotlinx.coroutines.flow.map

@Composable
fun PageAddDevices(
    controllerId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    var selectedRoomId by remember { mutableStateOf<Int?>(null) }

    val roomsOrNull by remember(db, controllerId) {
        db.roomDao()
            .observeAll(controllerId)
            .map<List<RoomEntity>, List<RoomEntity>?> { it }
    }.collectAsState(initial = null)

    val luminairesOrNull by remember(db, controllerId, selectedRoomId) {
        db.luminaireDao()
            .observeAll(controllerId, selectedRoomId)
            .map<List<LuminaireEntity>, List<LuminaireEntity>?> { it }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId, selectedRoomId) {
        db.brightSensorDao()
            .observeAll(controllerId, selectedRoomId)
            .map<List<BrightSensorEntity>, List<BrightSensorEntity>?> { it }
    }.collectAsState(initial = null)

    val handleBackClick = {
        if (selectedRoomId != null) {
            selectedRoomId = null
        } else {
            onBackClick()
        }
    }

    BackHandler(onBack = handleBackClick)

    PageContainer(
        title = "Добавление устройств",
        onBackClick = handleBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize()
    ) {
        val rooms = roomsOrNull
        val luminaires = luminairesOrNull
        val brightSensors = brightSensorsOrNull
        val ready = rooms != null && luminaires != null && brightSensors != null

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (ready && selectedRoomId == null && rooms!!.isNotEmpty()) {
                RoomsGrid(
                    rooms = rooms,
                    onRoomClick = { roomId -> selectedRoomId = roomId },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (ready) {
                DevicesGrid(
                    luminaires = luminaires!!,
                    brightSensors = brightSensors!!,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoomsGrid(
    rooms: List<RoomEntity>,
    onRoomClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        val columns = 2
        val spacing = 16.dp
        val itemWidth = (maxWidth - spacing * (columns - 1)) / columns.toFloat()

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            rooms.chunked(columns).forEach { rowRooms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    rowRooms.forEach { room ->
                        RoomIcon(
                            text = room.displayName(),
                            iconResId = iconResId(context, room.icoNum),
                            onClick = { onRoomClick(room.id) },
                            modifier = Modifier.width(itemWidth)
                        )
                    }

                    repeat(columns - rowRooms.size) {
                        Box(
                            modifier = Modifier
                                .width(itemWidth)
                                .height(96.dp * 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesGrid(
    luminaires: List<LuminaireEntity>,
    brightSensors: List<BrightSensorEntity>,
    modifier: Modifier = Modifier
) {
    data class DeviceCard(
        val sortOrder: Int,
        val stableId: Long,
        val content: @Composable (Modifier, androidx.compose.ui.unit.Dp) -> Unit
    )

    val context = LocalContext.current
    val devices = buildList {
        luminaires.forEach { luminaire ->
            val icon = iconResId(
                context = context,
                iconId = luminaire.icoNum,
                fallback = com.awada.synapse.R.drawable.luminaire_300_default
            )
            add(
                DeviceCard(
                    sortOrder = luminaire.gridPos,
                    stableId = luminaire.id,
                    content = { itemModifier, iconSize ->
                        Lum(
                            title = luminaire.name.ifBlank { "Светильник" },
                            iconSize = iconSize,
                            brightnessPercent = luminaire.bright,
                            typeId = luminaire.typeId,
                            hue = luminaire.hue,
                            saturation = luminaire.saturation,
                            temperature = luminaire.temperature,
                            iconResId = icon,
                            onClick = null,
                            modifier = itemModifier
                        )
                    }
                )
            )
        }
        brightSensors.forEach { sensor ->
            add(
                DeviceCard(
                    sortOrder = sensor.gridPos,
                    stableId = sensor.id,
                    content = { itemModifier, iconSize ->
                        BrightSensor(
                            title = sensor.name.ifBlank { "Сенсор\nяркости" },
                            iconSize = iconSize,
                            onClick = null,
                            modifier = itemModifier
                        )
                    }
                )
            )
        }
    }.sortedWith(compareBy<DeviceCard> { it.sortOrder }.thenBy { it.stableId })

    BoxWithConstraints(modifier = modifier) {
        val columns = 4
        val columnSpacing = 16.dp
        val rowSpacing = 4.dp
        val itemWidth = (maxWidth - columnSpacing * (columns - 1)) / columns.toFloat()
        val itemHeight = itemWidth
        val iconSize = (itemWidth * 0.78f).coerceIn(56.dp, 82.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            devices.chunked(columns).forEach { rowDevices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(columnSpacing)
                ) {
                    rowDevices.forEach { device ->
                        Box(
                            modifier = Modifier
                                .width(itemWidth)
                                .height(itemHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            device.content(Modifier, iconSize)
                        }
                    }

                    repeat(columns - rowDevices.size) {
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
