package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.SearchSwarmIndicator
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors
import kotlinx.coroutines.delay

/** Экран имитации инициализации контроллера перед добавлением моковых устройств. */
internal val PageInitializeControllerLlmDescriptor = LLMPageDescriptor(
    fileName = "PageInitializeController",
    screenName = "InitializeController",
    titleRu = "Инициализация контроллера",
    description = "Показывает анимацию инициализации контроллера и затем завершает подготовку устройств."
)

@Composable
fun PageInitializeController(
    onBackClick: () -> Unit,
    onInitializationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(5_000)
        onInitializationComplete()
    }

    PageContainer(
        title = "Инициализация",
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
                SearchSwarmIndicator()
                androidx.compose.material3.Text(
                    text = "Инициализируем контроллер...",
                    modifier = Modifier.padding(top = 80.dp),
                    style = BodyLarge,
                    color = PixsoColors.Color_Text_text_1_level,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
