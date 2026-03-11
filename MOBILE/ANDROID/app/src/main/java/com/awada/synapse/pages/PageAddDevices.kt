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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.Lum
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.defaultGroupName
import com.awada.synapse.db.displayName
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private enum class AddableDeviceType {
    Luminaire,
    BrightSensor
}

private data class PendingAddDevice(
    val type: AddableDeviceType,
    val id: Long,
    val title: String
)

@Composable
fun PageAddDevices(
    controllerId: Int,
    groupId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var selectedRoomId by remember { mutableStateOf<Int?>(null) }
    var pendingAddDevice by remember { mutableStateOf<PendingAddDevice?>(null) }

    val groupOrNull by remember(db, groupId) {
        db.groupDao().observeById(groupId)
    }.collectAsState(initial = null)

    val handleBackClick = {
        if (pendingAddDevice != null) {
            pendingAddDevice = null
        } else if (selectedRoomId != null) {
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
        key(selectedRoomId) {
            if (selectedRoomId == null) {
                AddDevicesRootContent(
                    controllerId = controllerId,
                    pendingAddDevice = pendingAddDevice,
                    onRoomClick = { roomId -> selectedRoomId = roomId },
                    onLuminaireClick = { luminaire ->
                        pendingAddDevice = PendingAddDevice(
                            type = AddableDeviceType.Luminaire,
                            id = luminaire.id,
                            title = luminaire.name.ifBlank { "Светильник" }
                        )
                    },
                    onBrightSensorClick = { sensor ->
                        pendingAddDevice = PendingAddDevice(
                            type = AddableDeviceType.BrightSensor,
                            id = sensor.id,
                            title = sensor.name.ifBlank { "Сенсор яркости" }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AddDevicesRoomContent(
                    controllerId = controllerId,
                    roomId = selectedRoomId!!,
                    pendingAddDevice = pendingAddDevice,
                    onLuminaireClick = { luminaire ->
                        pendingAddDevice = PendingAddDevice(
                            type = AddableDeviceType.Luminaire,
                            id = luminaire.id,
                            title = luminaire.name.ifBlank { "Светильник" }
                        )
                    },
                    onBrightSensorClick = { sensor ->
                        pendingAddDevice = PendingAddDevice(
                            type = AddableDeviceType.BrightSensor,
                            id = sensor.id,
                            title = sensor.name.ifBlank { "Сенсор яркости" }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (pendingAddDevice != null) {
        val deviceToAdd = pendingAddDevice!!
        val groupTitle = groupOrNull?.displayName() ?: defaultGroupName(groupId)
        Tooltip(
            text = "Добавить устройство «${deviceToAdd.title}» в группу «$groupTitle»?",
            primaryButtonText = "Добавить",
            secondaryButtonText = "Отмена",
            onResult = { res ->
                when (res) {
                    TooltipResult.Primary -> {
                        pendingAddDevice = null
                        scope.launch {
                            when (deviceToAdd.type) {
                                AddableDeviceType.Luminaire ->
                                    db.luminaireDao().moveToGroup(deviceToAdd.id, groupId)

                                AddableDeviceType.BrightSensor ->
                                    db.brightSensorDao().moveToGroup(deviceToAdd.id, groupId)
                            }
                        }
                    }

                    TooltipResult.Tertiary, TooltipResult.Secondary, TooltipResult.Dismissed -> {
                        pendingAddDevice = null
                    }
                }
            }
        )
    }
}

@Composable
private fun AddDevicesRootContent(
    controllerId: Int,
    pendingAddDevice: PendingAddDevice?,
    onRoomClick: (Int) -> Unit,
    onLuminaireClick: (LuminaireEntity) -> Unit,
    onBrightSensorClick: (BrightSensorEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    val roomsOrNull by remember(db, controllerId) {
        db.roomDao()
            .observeAll(controllerId)
            .map<List<RoomEntity>, List<RoomEntity>?> { it }
    }.collectAsState(initial = null)

    val luminairesOrNull by remember(db, controllerId) {
        db.luminaireDao()
            .observeAll(controllerId, null)
            .map<List<LuminaireEntity>, List<LuminaireEntity>?> { it }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId) {
        db.brightSensorDao()
            .observeAll(controllerId, null)
            .map<List<BrightSensorEntity>, List<BrightSensorEntity>?> { it }
    }.collectAsState(initial = null)

    val rooms = roomsOrNull
    val luminaires = luminairesOrNull
    val brightSensors = brightSensorsOrNull
    val ready = rooms != null && luminaires != null && brightSensors != null
    val availableLuminaires = luminaires?.filter { it.groupId == null }.orEmpty()
    val availableBrightSensors = brightSensors?.filter { it.groupId == null }.orEmpty()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (ready && rooms!!.isNotEmpty()) {
            RoomsGrid(
                rooms = rooms,
                onRoomClick = onRoomClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (ready) {
            DevicesGrid(
                luminaires = availableLuminaires,
                brightSensors = availableBrightSensors,
                pendingAddDevice = pendingAddDevice,
                onLuminaireClick = onLuminaireClick,
                onBrightSensorClick = onBrightSensorClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddDevicesRoomContent(
    controllerId: Int,
    roomId: Int,
    pendingAddDevice: PendingAddDevice?,
    onLuminaireClick: (LuminaireEntity) -> Unit,
    onBrightSensorClick: (BrightSensorEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    val luminairesOrNull by remember(db, controllerId, roomId) {
        db.luminaireDao()
            .observeAll(controllerId, roomId)
            .map<List<LuminaireEntity>, List<LuminaireEntity>?> { it }
    }.collectAsState(initial = null)

    val brightSensorsOrNull by remember(db, controllerId, roomId) {
        db.brightSensorDao()
            .observeAll(controllerId, roomId)
            .map<List<BrightSensorEntity>, List<BrightSensorEntity>?> { it }
    }.collectAsState(initial = null)

    val luminaires = luminairesOrNull
    val brightSensors = brightSensorsOrNull
    val ready = luminaires != null && brightSensors != null
    val availableLuminaires = luminaires?.filter { it.groupId == null }.orEmpty()
    val availableBrightSensors = brightSensors?.filter { it.groupId == null }.orEmpty()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (ready) {
            DevicesGrid(
                luminaires = availableLuminaires,
                brightSensors = availableBrightSensors,
                pendingAddDevice = pendingAddDevice,
                onLuminaireClick = onLuminaireClick,
                onBrightSensorClick = onBrightSensorClick,
                modifier = Modifier.fillMaxWidth()
            )
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
    pendingAddDevice: PendingAddDevice?,
    onLuminaireClick: (LuminaireEntity) -> Unit,
    onBrightSensorClick: (BrightSensorEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    data class DeviceCard(
        val sortOrder: Int,
        val stableId: Long,
        val type: AddableDeviceType,
        val content: @Composable (Modifier, androidx.compose.ui.unit.Dp, Boolean) -> Unit
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
                    type = AddableDeviceType.Luminaire,
                    content = { itemModifier, iconSize, isSelected ->
                        Lum(
                            title = luminaire.name.ifBlank { "Светильник" },
                            iconSize = iconSize,
                            brightnessPercent = luminaire.bright,
                            typeId = luminaire.typeId,
                            hue = luminaire.hue,
                            saturation = luminaire.saturation,
                            temperature = luminaire.temperature,
                            iconResId = icon,
                            forceSecondaryPressed = isSelected,
                            onClick = { onLuminaireClick(luminaire) },
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
                    type = AddableDeviceType.BrightSensor,
                    content = { itemModifier, iconSize, isSelected ->
                        BrightSensor(
                            title = sensor.name.ifBlank { "Сенсор\nяркости" },
                            iconSize = iconSize,
                            forceSecondaryPressed = isSelected,
                            onClick = { onBrightSensorClick(sensor) },
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
                            val isSelected =
                                pendingAddDevice?.type == device.type &&
                                    pendingAddDevice.id == device.stableId
                            device.content(Modifier, iconSize, isSelected)
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
