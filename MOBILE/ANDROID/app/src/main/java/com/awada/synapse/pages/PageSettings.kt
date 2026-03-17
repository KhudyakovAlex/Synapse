package com.awada.synapse.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium

/** Экран общих настроек приложения. */
internal val PageSettingsLlmDescriptor = LLMPageDescriptor(
    fileName = "PageSettings",
    screenName = "Settings",
    titleRu = "Настройки",
    description = "Показывает общие настройки приложения и позволяет перейти к поиску контроллера."
)

@Composable
fun PageSettings(
    onBackClick: () -> Unit,
    onFindControllerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Чтобы добавить локацию, подключитесь к ее контроллеру",
                style = TitleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryIconButtonLarge(
                text = "Найти контроллер",
                onClick = { onFindControllerClick?.invoke() },
                modifier = Modifier.fillMaxWidth(),
                enabled = onFindControllerClick != null
            )
        }
    }
}
