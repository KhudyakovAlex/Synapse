package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton

/**
 * Luminaire settings page.
 * Configure luminaire parameters.
 */
@Composable
fun PageSettingsLum(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier
    ) {
        // Future luminaire settings content goes here
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Отменить",
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Сохранить",
                onClick = { /* TODO: Save logic */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
