package com.awada.synapse.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.BrightSensor
import com.awada.synapse.components.ButtonPanel
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

                List(16) {
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
                val density = LocalDensity.current
                val iconSizePx = with(density) { iconSize.toPx() }
                val centers = remember { mutableStateListOf<Offset?>().apply { repeat(16) { add(null) } } }
                var canvasOriginInRoot by remember { mutableStateOf(Offset.Zero) }

                val groups = remember {
                    listOf(
                        listOf(0, 1, 2, 3),
                        listOf(0, 5)
                    )
                }

                Box {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .onGloballyPositioned { coords ->
                                canvasOriginInRoot = coords.positionInRoot()
                            }
                    ) {
                        for (g in groups) {
                            for (i in 0 until g.size - 1) {
                                val a = centers[g[i]] ?: continue
                                val b = centers[g[i + 1]] ?: continue
                                drawLine(
                                    color = PixsoColors.Color_Bg_bg_surface,
                                    start = a,
                                    end = b,
                                    strokeWidth = 15.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(4) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                repeat(4) { col ->
                                    val idx = row * 4 + col
                                    val s = samples[idx]
                                    val posModifier = Modifier.onGloballyPositioned { coords ->
                                        val posInRoot = coords.positionInRoot()
                                        val centerX = (posInRoot.x - canvasOriginInRoot.x) + coords.size.width / 2f
                                        val centerY = (posInRoot.y - canvasOriginInRoot.y) + iconSizePx / 2f
                                        centers[idx] = Offset(centerX, centerY)
                                    }

                                    when (idx) {
                                        12 -> PresSensor(
                                            modifier = posModifier,
                                            iconSize = iconSize,
                                            onClick = onSensorPressSettingsClick
                                        )
                                        13 -> BrightSensor(
                                            modifier = posModifier,
                                            iconSize = iconSize,
                                            onClick = onSensorBrightSettingsClick
                                        )
                                        14 -> ButtonPanel(
                                            modifier = posModifier,
                                            iconSize = iconSize,
                                            onClick = onButtonPanelSettingsClick
                                        )
                                        else -> Lum(
                                            modifier = posModifier,
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

