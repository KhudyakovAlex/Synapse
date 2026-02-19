package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.LocationsContainer

/**
 * Locations list page (plural).
 */
@Composable
fun PageLocations(
    onSettingsClick: () -> Unit,
    locations: List<LocationItem>,
    onLocationClick: ((LocationItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val boundLocations = if (onLocationClick != null) {
        locations.map { item ->
            item.copy(onClick = { onLocationClick(item) })
        }
    } else {
        locations
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
                .weight(1f)
        )
    }
}
