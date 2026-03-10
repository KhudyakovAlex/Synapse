package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.Lum
import com.awada.synapse.components.PresSensor
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.ButtonPanelEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.PresSensorEntity
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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
                val dotColors = remember {
                    listOf(
                        PixsoColors.Color_Bg_bg_surface,
                        PixsoColors.Color_Border_border_error,
                        PixsoColors.Color_Border_border_focus,
                        PixsoColors.Color_State_on_disabled
                    )
                }

                val iconSize = 82.dp
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
                                onClick = { onLumClick(e.id) }
                            )
                        }
                    }
                    panels!!.forEach { e ->
                        add {
                            ButtonPanel(
                                title = e.name.ifBlank { "Панель\nкнопок" },
                                iconSize = iconSize,
                                onClick = { onButtonPanelSettingsClick(e.id) }
                            )
                        }
                    }
                    pres!!.forEach { e ->
                        add {
                            PresSensor(
                                title = e.name.ifBlank { "Сенсор\nнажатия" },
                                iconSize = iconSize,
                                onClick = { onSensorPressSettingsClick(e.id) }
                            )
                        }
                    }
                    bright!!.forEach { e ->
                        add {
                            BrightSensor(
                                title = e.name.ifBlank { "Сенсор\nяркости" },
                                iconSize = iconSize,
                                onClick = { onSensorBrightSettingsClick(e.id) }
                            )
                        }
                    }
                }

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

