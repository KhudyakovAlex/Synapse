package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.Lum
import com.awada.synapse.ui.theme.PixsoColors
import kotlin.random.Random

/**
 * Initial page for Locations.
 * Always placed below AI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    PageContainer(
        title = "Локации",
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
            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Поиск")
            }

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
                repeat(4) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(4) { col ->
                            val s = samples[row * 4 + col]
                            Lum(
                                title = s.title,
                                iconSize = 72.dp,
                                brightnessPercent = s.brightnessPercent,
                                iconResId = s.iconResId,
                                statusDotColor = s.statusDotColor,
                                onClick = null
                            )
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
