package com.awada.synapse.pages

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Password change page.
 * TODO: Implement password change functionality
 *
 * @param onBackClick Callback for back button
 */
@Composable
fun PageChangePassword(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Сменить пароль",
            onBackClick = onBackClick,
            isScrollable = false,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TODO: Add password change UI content
                }
            }
        }
    }
}
