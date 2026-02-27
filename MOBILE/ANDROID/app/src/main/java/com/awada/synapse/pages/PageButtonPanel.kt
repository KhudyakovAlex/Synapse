package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.PanelButton
import com.awada.synapse.components.PanelButtonVariant

@Composable
fun PageButtonPanel(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(
        title = "Панель 7",
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize(),
    ) {
        // Pixso: 4 buttons 108×108, grid 2×2, gap 4, side padding 70.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val buttonSize = 108.dp
            val gap = 4.dp
            val gridWidth = buttonSize * 2 + gap

            Column(
                verticalArrangement = Arrangement.spacedBy(gap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.width(gridWidth),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    PanelButton(
                        text = "1",
                        variant = PanelButtonVariant.Def,
                        size = buttonSize,
                        tapToActiveHoldMs = 1000L,
                        onClick = {},
                    )
                    PanelButton(
                        text = "2",
                        variant = PanelButtonVariant.Def,
                        size = buttonSize,
                        tapToActiveHoldMs = 1000L,
                        onClick = {},
                    )
                }
                Row(
                    modifier = Modifier.width(gridWidth),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    PanelButton(
                        text = "3",
                        variant = PanelButtonVariant.Def,
                        size = buttonSize,
                        tapToActiveHoldMs = 1000L,
                        onClick = {},
                    )
                    PanelButton(
                        text = "4",
                        variant = PanelButtonVariant.Def,
                        size = buttonSize,
                        tapToActiveHoldMs = 1000L,
                        onClick = {},
                    )
                }
            }
        }
    }
}

