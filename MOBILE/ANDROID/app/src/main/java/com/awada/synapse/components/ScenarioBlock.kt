package com.awada.synapse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun ScenarioBlock(
    scenarios: List<ScheduleScenario>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(shape)
            .background(PixsoColors.Color_Bg_bg_surface)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(PixsoDimens.Numeric_8),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_0),
    ) {
        scenarios.forEach { scenario ->
            val itemOnClick = onClick ?: scenario.onClick
            ScenarioButton(
                text = scenario.text,
                onClick = itemOnClick,
                enabled = scenario.enabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

