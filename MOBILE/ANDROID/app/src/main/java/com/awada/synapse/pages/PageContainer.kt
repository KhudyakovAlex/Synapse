package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.AppBar
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Base container for app pages.
 * Provides a fixed UIAppBar at the top and a content area below.
 *
 * @param title Page title for the AppBar.
 * @param onBackClick Callback for the back button. If null, button is hidden.
 * @param onSettingsClick Callback for the settings button. If null, button is hidden.
 * @param isScrollable Whether the content area should be scrollable.
 * @param content The main content of the page.
 */
@Composable
fun PageContainer(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    isScrollable: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixsoColors.Color_Bg_bg_canvas)
    ) {
        // Fixed AppBar at the top
        AppBar(
            title = title,
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Content area
        val scrollState = rememberScrollState()
        val contentModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)

        Column(modifier = contentModifier) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
            // Spacer to prevent AI panel from covering content
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}
