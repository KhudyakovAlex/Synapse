package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.PanelButton
import com.awada.synapse.components.PanelButtonVariant
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ButtonEntity
import kotlinx.coroutines.flow.flowOf

@Composable
fun PageButtonPanel(
    buttonPanelId: Long?,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onButtonLongClick: (buttonNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    val buttonPanelOrNull by remember(db, buttonPanelId) {
        if (buttonPanelId == null) {
            flowOf(null)
        } else {
            db.buttonPanelDao().observeById(buttonPanelId)
        }
    }.collectAsState(initial = null)

    val buttons by remember(db, buttonPanelId) {
        if (buttonPanelId == null) {
            flowOf(emptyList())
        } else {
            db.buttonDao().observeAllForPanel(buttonPanelId)
        }
    }.collectAsState(initial = emptyList())

    val visibleButtons = buttons.sortedBy { it.num }.take(8)
    val buttonRows = visibleButtons.chunked(2)

    PageContainer(
        title = buttonPanelOrNull?.name?.ifBlank { "Кнопочная панель" } ?: "Кнопочная панель",
        onBackClick = onBackClick,
        onSettingsClick = onSettingsClick,
        isScrollable = false,
        modifier = modifier.fillMaxSize(),
    ) {
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
                buttonRows.forEach { rowButtons ->
                    ButtonPanelRow(
                        buttons = rowButtons,
                        buttonSize = buttonSize,
                        gap = gap,
                        gridWidth = gridWidth,
                        onButtonLongClick = onButtonLongClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonPanelRow(
    buttons: List<ButtonEntity>,
    buttonSize: Dp,
    gap: Dp,
    gridWidth: Dp,
    onButtonLongClick: (buttonNumber: Int) -> Unit,
) {
    Row(
        modifier = Modifier.width(gridWidth),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        buttons.forEach { button ->
            PanelButton(
                text = button.num.toString(),
                variant = PanelButtonVariant.Def,
                size = buttonSize,
                tapToActiveHoldMs = 1000L,
                onClick = {},
                onLongClick = { onButtonLongClick(button.num) },
            )
        }
        repeat(2 - buttons.size) {
            Spacer(modifier = Modifier.size(buttonSize))
        }
    }
}

