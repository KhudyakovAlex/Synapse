package com.awada.synapse.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.awada.synapse.components.TextField
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.launch

/**
 * Brightness sensor settings page.
 * Configure brightness sensor parameters.
 */
/** Экран настройки датчика освещенности. */
internal val PageSensorBrightSettingsLlmDescriptor = LLMPageDescriptor(
    fileName = "PageSensorBrightSettings",
    screenName = "SensorBrightSettings",
    titleRu = "Настройки датчика освещенности",
    description = "Позволяет изменить название и параметры выбранного датчика освещенности."
)

@Composable
fun PageSensorBrightSettings(
    sensorId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAiChatExpanded = LocalAiChatExpanded.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }

    LaunchedEffect(sensorId) {
        val id = sensorId ?: return@LaunchedEffect
        val e = db.brightSensorDao().getById(id) ?: return@LaunchedEffect
        name = e.name
    }

    fun saveAndBack() {
        scope.launch {
            val id = sensorId
            if (id != null) {
                db.brightSensorDao().setName(id = id, name = name)
            }
            onBackClick()
        }
    }
    
    BackHandler(enabled = !isAiChatExpanded) { saveAndBack() }

    PageContainer(
        title = "Настройки\nдатчика освещённости",
        onBackClick = ::saveAndBack,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            // 1. Название
            TextField(
                value = name,
                onValueChange = { name = it },
                label = "Название",
                placeholder = "",
                enabled = true
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
        }
    }
}
