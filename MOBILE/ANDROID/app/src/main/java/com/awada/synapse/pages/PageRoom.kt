package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.Lum
import com.awada.synapse.components.PresSensor
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.random.Random

/**
 * Page for a single room (within a location).
 * Mock UI: random device icon set per room.
 */
@Composable
fun PageRoom(
    roomTitle: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLumClick: () -> Unit,
    onSensorPressSettingsClick: () -> Unit,
    onSensorBrightSettingsClick: () -> Unit,
    onButtonPanelSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            val tiles = remember(roomTitle) { roomDeviceTilesMock(roomTitle) }
            val iconSize = 72.dp

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 2 rows of 4 tiles below (8 slots).
                repeat(2) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(4) { col ->
                            val idx = row * 4 + col
                            when (val t = tiles[idx]) {
                                is RoomTileMock.Lum -> Lum(
                                    title = t.title,
                                    iconSize = iconSize,
                                    brightnessPercent = t.brightnessPercent,
                                    iconResId = t.iconResId,
                                    statusDotColor = t.statusDotColor,
                                    onClick = onLumClick
                                )
                                RoomTileMock.PresSensor -> PresSensor(
                                    iconSize = iconSize,
                                    onClick = onSensorPressSettingsClick
                                )
                                RoomTileMock.BrightSensor -> BrightSensor(
                                    iconSize = iconSize,
                                    onClick = onSensorBrightSettingsClick
                                )
                                RoomTileMock.ButtonPanel -> ButtonPanel(
                                    iconSize = iconSize,
                                    onClick = onButtonPanelSettingsClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class RoomTileMock {
    data class Lum(
        val title: String,
        val brightnessPercent: Int,
        val iconResId: Int,
        val statusDotColor: androidx.compose.ui.graphics.Color
    ) : RoomTileMock()

    data object PresSensor : RoomTileMock()
    data object BrightSensor : RoomTileMock()
    data object ButtonPanel : RoomTileMock()
}

private fun roomDeviceTilesMock(roomTitle: String): List<RoomTileMock> {
    val rnd = Random(roomTitle.hashCode())
    val lumIcons = listOf(
        R.drawable.luminaire_300_default,
        R.drawable.luminaire_301_pot_podv_lin,
        R.drawable.luminaire_302_pot_podv_krugl,
        R.drawable.luminaire_303_pot_podv_abazh,
        R.drawable.luminaire_304_pot_podv_lustra,
        R.drawable.luminaire_305_pot_podv_kvadr,
        R.drawable.luminaire_306_pot_podv_toch,
        R.drawable.luminaire_307_pot_vstr_lin,
        R.drawable.luminaire_308_pot_vstr_krugl,
        R.drawable.luminaire_309_pot_vstr_kvadr,
        R.drawable.luminaire_310_pot_vstr_toch,
        R.drawable.luminaire_311_pot_vstr_toch_dvoynoy,
        R.drawable.luminaire_312_pot_vstr_toch_troynoy,
        R.drawable.luminaire_313_pot_vstr_toch_pov,
        R.drawable.luminaire_314_pot_vstr_toch_pov_dvoynoy,
        R.drawable.luminaire_315_pot_vstr_toch_pov_troynoy
    )
    val dotColors = listOf(
        PixsoColors.Color_Bg_bg_surface,
        PixsoColors.Color_Border_border_error,
        PixsoColors.Color_Border_border_focus,
        PixsoColors.Color_State_on_disabled
    )

    val tiles = MutableList<RoomTileMock>(8) {
        when (rnd.nextInt(100)) {
            in 0..9 -> RoomTileMock.PresSensor
            in 10..19 -> RoomTileMock.BrightSensor
            in 20..29 -> RoomTileMock.ButtonPanel
            else -> {
                val title = when (rnd.nextInt(3)) {
                    0 -> "Настенный"
                    1 -> "Торшер"
                    else -> "Настенный\nсветильник"
                }
                RoomTileMock.Lum(
                    title = title,
                    brightnessPercent = rnd.nextInt(0, 101),
                    iconResId = lumIcons[rnd.nextInt(lumIcons.size)],
                    statusDotColor = dotColors[rnd.nextInt(dotColors.size)]
                )
            }
        }
    }

    return tiles
}

