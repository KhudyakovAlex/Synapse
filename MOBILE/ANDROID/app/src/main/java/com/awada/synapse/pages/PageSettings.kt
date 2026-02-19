package com.awada.synapse.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.LocationsContainer
import com.awada.synapse.components.PrimaryIconLButton
import com.awada.synapse.components.controllerIconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.TitleMedium
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext

/**
 * Settings page.
 * Allows user to configure app settings.
 */
@Composable
fun PageSettings(
    onBackClick: () -> Unit,
    onFindControllerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val controllers by db.controllerDao().observeAll().collectAsState(initial = emptyList())
    val locations = controllers.map { c ->
        com.awada.synapse.components.LocationItem(
            title = c.name.ifBlank { "Контроллер ${c.id}" },
            iconResId = controllerIconResId(c.icoNum),
            enabled = true,
            onClick = null
        )
    }

    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier
    ) {
        if (locations.isEmpty()) {
            LocationsContainer(
                locations = emptyList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onEmptyButtonClick = onFindControllerClick
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = PixsoDimens.Numeric_16)
            ) {
                Text(
                    text = "Чтобы добавить локацию, подключитесь к ее контроллеру",
                    style = TitleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryIconLButton(
                    text = "Найти контроллер",
                    onClick = { onFindControllerClick?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onFindControllerClick != null
                )
                Spacer(modifier = Modifier.height(20.dp))
                LocationsContainer(
                    locations = locations,
                    showTitles = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onEmptyButtonClick = null
                )
            }
        }
    }
}
