package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.flow.map

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
    val controllersOrNull by remember(db) {
        db.controllerDao()
            .observeAll()
            .map<List<com.awada.synapse.db.ControllerEntity>, List<com.awada.synapse.db.ControllerEntity>?> { it }
    }.collectAsState(initial = null)

    // Prevent flicker: don't show "empty" CTA until first DB emission arrives.
    if (controllersOrNull == null) {
        PageContainer(
            title = "Локации",
            onSettingsClick = onSettingsClick,
            isScrollable = false,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
        return
    }

    val controllers = controllersOrNull ?: emptyList()

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
