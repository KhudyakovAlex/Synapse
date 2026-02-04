package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Initial page for Locations.
 * Always placed below UIAI layer in MainActivity.
 */
@Composable
fun PageLocation(
    onSettingsClick: () -> Unit,
    onPasswordClick: () -> Unit,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onPasswordClick) {
                Text("Ввести пароль")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Future location content goes here")
        }
    }
}
