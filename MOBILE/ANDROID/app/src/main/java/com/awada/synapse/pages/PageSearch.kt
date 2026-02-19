package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.SearchSwampIndicator
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.delay

@Composable
fun PageSearch(
    onBackClick: () -> Unit,
    onAutoNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(5_000)
        onAutoNavigateNext()
    }

    PageContainer(
        title = "Поиск",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SearchSwampIndicator()
                Text(
                    text = "Ищем контроллер локации...",
                    modifier = Modifier
                        .padding(top = 80.dp),
                    style = BodyLarge,
                    color = PixsoColors.Color_Text_text_1_level,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
