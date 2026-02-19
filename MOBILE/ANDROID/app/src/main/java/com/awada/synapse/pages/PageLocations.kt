package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.components.LocationsContainer
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.controllerIconResId
import com.awada.synapse.db.AppDatabase

/**
 * Locations list page (plural).
 */
@Composable
fun PageLocations(
    onSettingsClick: () -> Unit,
    onLocationClick: (LocationItem) -> Unit,
    onFindControllerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val controllers by db.controllerDao().observeAll().collectAsState(initial = emptyList())

    val boundLocations = controllers.map { c ->
        val item = LocationItem(
            title = c.name.ifBlank { "Контроллер ${c.id}" },
            iconResId = controllerIconResId(c.icoNum)
        )
        item.copy(onClick = { onLocationClick(item) })
    }

    PageContainer(
        title = "Локации",
        onSettingsClick = onSettingsClick,
        isScrollable = false,
        modifier = modifier
    ) {
        LocationsContainer(
            locations = boundLocations,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onEmptyButtonClick = { onFindControllerClick?.invoke() }
        )
    }
}
