package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.awada.synapse.ui.theme.HeadlineMedium
import com.awada.synapse.ui.theme.LabelSmall
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

data class ScheduleScenario(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

@Composable
fun SchedulePoint(
    timeText: String,
    days: List<String>,
    scenarios: List<ScheduleScenario>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = PixsoColors.Color_Bg_bg_surface,
                shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M),
            )
            .padding(PixsoDimens.Numeric_8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(104f)
                    .padding(
                        start = PixsoDimens.Numeric_16,
                        top = PixsoDimens.Numeric_8,
                        bottom = PixsoDimens.Numeric_4,
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = timeText,
                    style = HeadlineMedium, // Synapse/Headline/Headline M
                    color = PixsoColors.Color_Text_text_1_level,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            style = LabelSmall, // Synapse/Label/Label S
                            color = PixsoColors.Color_Text_text_3_level,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(204f)
                    .padding(vertical = PixsoDimens.Numeric_4),
                verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_0),
                horizontalAlignment = Alignment.Start,
            ) {
                scenarios.forEach { scenario ->
                    ScenarioButton(
                        text = scenario.text,
                        onClick = scenario.onClick,
                        enabled = scenario.enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

