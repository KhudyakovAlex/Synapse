package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Initial page for Locations.
 * Always placed below AI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    PageContainer(
        title = "Локации",
        onSettingsClick = onSettingsClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Поиск")
            }
        }
    }
}
