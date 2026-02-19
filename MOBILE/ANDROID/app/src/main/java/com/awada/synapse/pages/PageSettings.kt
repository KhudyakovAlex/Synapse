package com.awada.synapse.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.awada.synapse.components.LocationItem
import com.awada.synapse.components.LocationsContainer

/**
 * Settings page.
 * Allows user to configure app settings.
 */
@Composable
fun PageSettings(
    onBackClick: () -> Unit,
    locations: List<LocationItem>,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier
    ) {
        LocationsContainer(
            locations = locations,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
