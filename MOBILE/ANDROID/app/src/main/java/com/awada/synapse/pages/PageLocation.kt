package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.Lum
import com.awada.synapse.components.PresSensor
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Page for a single location (singular).
 */
@Composable
fun PageLocation(
    location: LocationItem,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRoomClick: (roomId: Int, roomTitle: String, roomIconId: Int) -> Unit,
    onLumClick: () -> Unit,
    onSensorPressSettingsClick: () -> Unit,
    onSensorBrightSettingsClick: () -> Unit,
    onButtonPanelSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controllerId = location.controllerId
    val db = remember { AppDatabase.getInstance(context) }
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

    PageContainer(
        title = location.title,
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
                                            modifier = Modifier.weight(1f)
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
                    val dotColors = remember {
                        listOf(
                            PixsoColors.Color_Bg_bg_surface,
                            PixsoColors.Color_Border_border_error,
                            PixsoColors.Color_Border_border_focus,
                            PixsoColors.Color_State_on_disabled
                        )
                    }

                    val tiles = buildList<@Composable () -> Unit> {
                        luminaires!!.forEachIndexed { idx, e ->
                            val icon = iconResId(
                                context = context,
                                iconId = e.icoNum,
                                fallback = com.awada.synapse.R.drawable.luminaire_300_default
                            )
                            add {
                                Lum(
                                    title = e.name.ifBlank { "Светильник" },
                                    iconSize = iconSize,
                                    brightnessPercent = 35,
                                    iconResId = icon,
                                    statusDotColor = dotColors[idx % dotColors.size],
                                    onClick = onLumClick
                                )
                            }
                        }
                        panels!!.forEach { _ ->
                            add {
                                ButtonPanel(
                                    iconSize = iconSize,
                                    onClick = onButtonPanelSettingsClick
                                )
                            }
                        }
                        pres!!.forEach {
                            add {
                                PresSensor(
                                    iconSize = iconSize,
                                    onClick = onSensorPressSettingsClick
                                )
                            }
                        }
                        bright!!.forEach {
                            add {
                                BrightSensor(
                                    iconSize = iconSize,
                                    onClick = onSensorBrightSettingsClick
                                )
                            }
                        }
                    }

                    // Unified matrix: 4 columns, as many rows as needed.
                    val perRow = 4
                    tiles.chunked(perRow).forEach { rowTiles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowTiles.forEach { content ->
                                content()
                            }
                            repeat(perRow - rowTiles.size) {
                                Spacer(modifier = Modifier)
                            }
                        }
                    }
                }
            }
        }
    }
}

