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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
import com.awada.synapse.components.RoomIcon
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
            val luminaireIcons = remember {
                listOf(
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
                    com.awada.synapse.R.drawable.luminaire_315_pot_vstr_toch_pov_troynoy,
                    com.awada.synapse.R.drawable.luminaire_316_pot_nakl_lin,
                    com.awada.synapse.R.drawable.luminaire_317_pot_nakl_krugl,
                    com.awada.synapse.R.drawable.luminaire_318_pot_nakl_kvadr,
                    com.awada.synapse.R.drawable.luminaire_319_pot_nakl_toch,
                    com.awada.synapse.R.drawable.luminaire_320_pot_nakl_toch_pov,
                    com.awada.synapse.R.drawable.luminaire_321_nas_bra,
                    com.awada.synapse.R.drawable.luminaire_322_nas_nakl_lin,
                    com.awada.synapse.R.drawable.luminaire_323_nas_nakl_kvadr,
                    com.awada.synapse.R.drawable.luminaire_324_nas_nakl_krugl,
                    com.awada.synapse.R.drawable.luminaire_325_nas_nakl_toch_pov,
                    com.awada.synapse.R.drawable.luminaire_326_trek_lin,
                    com.awada.synapse.R.drawable.luminaire_327_trek_toch_pov,
                    com.awada.synapse.R.drawable.luminaire_328_podsv,
                    com.awada.synapse.R.drawable.luminaire_329_lenta,
                    com.awada.synapse.R.drawable.luminaire_330_torsher,
                    com.awada.synapse.R.drawable.luminaire_331_fasad,
                    com.awada.synapse.R.drawable.luminaire_332_prozh,
                    com.awada.synapse.R.drawable.luminaire_333_stolb_fonar,
                    com.awada.synapse.R.drawable.luminaire_334_grunt_vstr,
                    com.awada.synapse.R.drawable.luminaire_335_grunt_stolb
                )
            }

            val samples = remember {
                val rnd = Random(42)
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
                        iconResId = luminaireIcons[rnd.nextInt(luminaireIcons.size)],
                        statusDotColor = dotColors[rnd.nextInt(dotColors.size)]
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val iconSize = 82.dp
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
                            RoomIcon(
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

                LuminaireMockups(
                    icons = luminaireIcons,
                    iconSize = iconSize,
                    onLumClick = onLumClick
                )
            }
        }
    }
}

@Composable
private fun LuminaireMockups(
    icons: List<Int>,
    iconSize: androidx.compose.ui.unit.Dp,
    onLumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val dotColors = remember {
            listOf(
                PixsoColors.Color_Bg_bg_surface,
                PixsoColors.Color_Border_border_error,
                PixsoColors.Color_Border_border_focus,
                PixsoColors.Color_State_on_disabled
            )
        }
        val items = remember(icons, context) {
            icons.mapIndexed { idx, iconResId ->
                val entryName = context.resources.getResourceEntryName(iconResId)
                LumSample(
                    title = luminaireShortTitle(entryName),
                    brightnessPercent = (idx * 7) % 101,
                    iconResId = iconResId,
                    statusDotColor = dotColors[idx % dotColors.size]
                )
            }
        }

        val perRow = 4
        items.chunked(perRow).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { s ->
                    Lum(
                        title = s.title,
                        iconSize = iconSize,
                        brightnessPercent = s.brightnessPercent,
                        iconResId = s.iconResId,
                        statusDotColor = s.statusDotColor,
                        onClick = onLumClick
                    )
                }
                repeat(perRow - rowItems.size) {
                    Spacer(modifier = Modifier)
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

private fun luminaireShortTitle(entryName: String): String {
    val name = entryName.removePrefix("luminaire_")
    val m = Regex("""^(\d+)(?:_(.+))?$""").matchEntire(name) ?: return entryName
    val code = m.groupValues[1]
    val rest = m.groupValues[2].ifBlank { return code }

    return when (rest) {
        "default" -> "Default"
        "torsher" -> "Торшер"
        "lenta" -> "Лента"
        "fasad" -> "Фасад"
        "prozh" -> "Прожектор"
        "podsv" -> "Подсв."
        "nas_bra" -> "Бра"
        "stolb_fonar" -> "Фонарь"
        "grunt_vstr" -> "Грунт"
        "grunt_stolb" -> "Столб"
        else -> code
    }
}

