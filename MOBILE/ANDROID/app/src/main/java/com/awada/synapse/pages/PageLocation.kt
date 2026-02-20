package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.IconRoom
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.Lum
import com.awada.synapse.components.PresSensor
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.random.Random

/**
 * Page for a single location (singular).
 */
@Composable
fun PageLocation(
    location: LocationItem,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRoomClick: (roomTitle: String, roomIconResId: Int) -> Unit,
    onLumClick: () -> Unit,
    onSensorPressSettingsClick: () -> Unit,
    onSensorBrightSettingsClick: () -> Unit,
    onButtonPanelSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            val samples = remember {
                val rnd = Random(42)
                val icons = listOf(
                    com.awada.synapse.R.drawable.luminaire_300_default,
                    com.awada.synapse.R.drawable.luminaire_301_pot_podv_lin,
                    com.awada.synapse.R.drawable.luminaire_302_pot_podv_krugl,
                    com.awada.synapse.R.drawable.luminaire_303_pot_podv_abazh,
                    com.awada.synapse.R.drawable.luminaire_304_pot_podv_lustra,
                    com.awada.synapse.R.drawable.luminaire_305_pot_podv_kvadr,
                    com.awada.synapse.R.drawable.luminaire_306_pot_podv_toch,
                    com.awada.synapse.R.drawable.luminaire_307_pot_vstr_lin,
                    com.awada.synapse.R.drawable.luminaire_308_pot_vstr_krugl,
                    com.awada.synapse.R.drawable.luminaire_309_pot_vstr_kvadr,
                    com.awada.synapse.R.drawable.luminaire_310_pot_vstr_toch,
                    com.awada.synapse.R.drawable.luminaire_311_pot_vstr_toch_dvoynoy,
                    com.awada.synapse.R.drawable.luminaire_312_pot_vstr_toch_troynoy,
                    com.awada.synapse.R.drawable.luminaire_313_pot_vstr_toch_pov,
                    com.awada.synapse.R.drawable.luminaire_314_pot_vstr_toch_pov_dvoynoy,
                    com.awada.synapse.R.drawable.luminaire_315_pot_vstr_toch_pov_troynoy
                )

                val dotColors = listOf(
                    PixsoColors.Color_Bg_bg_surface,
                    PixsoColors.Color_Border_border_error,
                    PixsoColors.Color_Border_border_focus,
                    PixsoColors.Color_State_on_disabled
                )

                List(8) {
                    val title = when (rnd.nextInt(3)) {
                        0 -> "Настенный"
                        1 -> "Торшер"
                        else -> "Настенный\nсветильник"
                    }
                    LumSample(
                        title = title,
                        brightnessPercent = rnd.nextInt(0, 101),
                        iconResId = icons[rnd.nextInt(icons.size)],
                        statusDotColor = dotColors[rnd.nextInt(dotColors.size)]
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val iconSize = 72.dp
                val iconSizePx = with(LocalDensity.current) { iconSize.toPx() }
                val rooms = remember {
                    listOf(
                        "Кухня" to com.awada.synapse.R.drawable.location_208_kuhnya,
                        "Спальня" to com.awada.synapse.R.drawable.location_209_spalnya
                    )
                }
                val showRooms = rooms.isNotEmpty()
                val showDevices = samples.isNotEmpty()

                if (showRooms) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rooms.take(2).forEach { (title, icon) ->
                            IconRoom(
                                text = title,
                                iconResId = icon,
                                onClick = { onRoomClick(title, icon) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (showRooms && showDevices) {
                    Spacer(modifier = Modifier.height(15.dp))
                }

                if (showDevices) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val link0 = remember { mutableStateOf<Offset?>(null) }
                        val link1 = remember { mutableStateOf<Offset?>(null) }
                        val link2 = remember { mutableStateOf<Offset?>(null) }

                        // 2 rows of 4 tiles below (8 slots).
                        repeat(2) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (row == 0) {
                                            Modifier.drawBehind {
                                                val p0 = link0.value
                                                val p1 = link1.value
                                                val p2 = link2.value
                                                val stroke = 5.dp.toPx()
                                                val color = PixsoColors.Color_Border_border_shade_16

                                                if (p0 != null && p1 != null) {
                                                    drawLine(
                                                        color = color,
                                                        start = p0,
                                                        end = p1,
                                                        strokeWidth = stroke
                                                    )
                                                }
                                                if (p1 != null && p2 != null) {
                                                    drawLine(
                                                        color = color,
                                                        start = p1,
                                                        end = p2,
                                                        strokeWidth = stroke
                                                    )
                                                }
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                repeat(4) { col ->
                                    val idx = row * 4 + col // 0..7
                                    val s = samples[idx]

                                    when (idx) {
                                        5 -> PresSensor(
                                            modifier = Modifier,
                                            iconSize = iconSize,
                                            onClick = onSensorPressSettingsClick
                                        )
                                        6 -> BrightSensor(
                                            modifier = Modifier,
                                            iconSize = iconSize,
                                            onClick = onSensorBrightSettingsClick
                                        )
                                        7 -> ButtonPanel(
                                            modifier = Modifier,
                                            iconSize = iconSize,
                                            onClick = onButtonPanelSettingsClick
                                        )
                                        else -> Lum(
                                            modifier = Modifier.then(
                                                if (idx in 0..2) {
                                                    Modifier.onGloballyPositioned { coords ->
                                                        val tl = coords.positionInParent()
                                                        val center = Offset(
                                                            x = tl.x + coords.size.width / 2f,
                                                            // Center of the white circle (icon area), not the full tile.
                                                            y = tl.y + iconSizePx / 2f
                                                        )
                                                        when (idx) {
                                                            0 -> link0.value = center
                                                            1 -> link1.value = center
                                                            2 -> link2.value = center
                                                        }
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                            title = s.title,
                                            iconSize = iconSize,
                                            brightnessPercent = s.brightnessPercent,
                                            iconResId = s.iconResId,
                                            statusDotColor = s.statusDotColor,
                                            onClick = onLumClick
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
}

private data class LumSample(
    val title: String,
    val brightnessPercent: Int,
    val iconResId: Int,
    val statusDotColor: androidx.compose.ui.graphics.Color
)

