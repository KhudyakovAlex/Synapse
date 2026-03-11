package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
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

    val visibleButtons = buttons
        .filter { it.matrixRow in 0 until ButtonEntity.MATRIX_SIZE && it.matrixCol in 0 until ButtonEntity.MATRIX_SIZE }
        .sortedBy { it.num }
        .take(8)

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
            val buttonSize = 96.dp
            val gap = 2.dp
            val minRow = visibleButtons.minOfOrNull { it.matrixRow } ?: 0
            val maxRow = visibleButtons.maxOfOrNull { it.matrixRow } ?: 0
            val minCol = visibleButtons.minOfOrNull { it.matrixCol } ?: 0
            val maxCol = visibleButtons.maxOfOrNull { it.matrixCol } ?: 0
            val rowSpan = (maxRow - minRow + 1).coerceAtLeast(1)
            val colSpan = (maxCol - minCol + 1).coerceAtLeast(1)
            val contentWidth = buttonSize * colSpan.toFloat() + gap * (colSpan - 1).toFloat()
            val contentHeight = buttonSize * rowSpan.toFloat() + gap * (rowSpan - 1).toFloat()

            Box(
                modifier = Modifier
                    .size(width = contentWidth, height = contentHeight),
                contentAlignment = Alignment.TopStart,
            ) {
                visibleButtons.forEach { button ->
                    val xOffset = (buttonSize + gap) * (button.matrixCol - minCol).toFloat()
                    val yOffset = (buttonSize + gap) * (button.matrixRow - minRow).toFloat()
                    PanelButton(
                        text = button.num.toString(),
                        variant = PanelButtonVariant.Def,
                        size = buttonSize,
                        tapToActiveHoldMs = 1000L,
                        onClick = {},
                        onLongClick = { onButtonLongClick(button.num) },
                        modifier = Modifier.offset {
                            IntOffset(xOffset.roundToPx(), yOffset.roundToPx())
                        },
                    )
                }
            }
        }
    }
}

