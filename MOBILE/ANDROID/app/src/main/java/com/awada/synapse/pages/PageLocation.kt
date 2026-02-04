package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult

/**
 * Initial page for Locations.
 * Always placed below AI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTooltipOneButton by remember { mutableStateOf(false) }
    var showTooltipTwoButtons by remember { mutableStateOf(false) }
    var lastTooltipResult by remember { mutableStateOf<TooltipResult?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onPasswordClick) {
                    Text("Ввести пароль")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showTooltipOneButton = true }) {
                    Text("Показать Tooltip (1 кнопка)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showTooltipTwoButtons = true }) {
                    Text("Показать Tooltip (2 кнопки)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                lastTooltipResult?.let {
                    Text("Последний результат: $it")
                }
            }
        }

        // Tooltip with one button
        if (showTooltipOneButton) {
            Tooltip(
                text = "Это пример tooltip окна с одной кнопкой. AI панель внизу остаётся активной!",
                primaryButtonText = "Понятно",
                onResult = { result ->
                    lastTooltipResult = result
                    showTooltipOneButton = false
                }
            )
        }

        // Tooltip with two buttons
        if (showTooltipTwoButtons) {
            Tooltip(
                text = "Это пример с двумя кнопками. Вы можете подтвердить или отменить действие.",
                primaryButtonText = "Подтвердить",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    lastTooltipResult = result
                    showTooltipTwoButtons = false
                }
            )
        }
    }
}
