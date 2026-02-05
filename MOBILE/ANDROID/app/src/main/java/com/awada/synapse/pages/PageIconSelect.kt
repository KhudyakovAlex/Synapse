package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.data.IconCatalogManager
import com.awada.synapse.ui.theme.BodyLarge
import com.awada.synapse.ui.theme.PixsoColors

/**
 * Page for icon selection from specific category.
 * 
 * @param category Icon category name (controller, location, luminaire)
 * @param currentIconId Currently selected icon ID
 * @param onIconSelected Callback when user selects an icon
 * @param onBackClick Callback for back button
 */
@Composable
fun PageIconSelect(
    category: String,
    currentIconId: Int,
    onIconSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val catalog = remember { IconCatalogManager.load(context) }
    val icons = remember(category) { catalog.getByCategory(category) }
    
    val density = LocalDensity.current
    val navigationBarHeightDp = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
    val bottomPadding = 100.dp + navigationBarHeightDp + 32.dp  // AI panel + nav bar + spacing (16dp + 2×8dp grid spacing)

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Выбор иконки",
            onBackClick = onBackClick,
            onSettingsClick = null,
            isScrollable = false,
            modifier = Modifier.fillMaxSize()
        ) {
            if (icons.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет иконок в категории \"$category\"",
                        style = BodyLarge,
                        color = PixsoColors.Color_Text_text_2_level
                    )
                }
            } else {
                // Icon grid: 4 columns, 8dp spacing
                // Bottom padding: AI panel (100dp) + navigation bar + spacing (16dp)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = bottomPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(icons) { iconInfo ->
                        val resourceId = remember(iconInfo.resourceName) {
                            context.resources.getIdentifier(
                                iconInfo.resourceName,
                                "drawable",
                                context.packageName
                            )
                        }

                        if (resourceId != 0) {
                            IconSelectButton(
                                icon = resourceId,
                                onClick = { onIconSelected(iconInfo.id) },
                                isActive = iconInfo.id == currentIconId,
                                modifier = Modifier.aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
