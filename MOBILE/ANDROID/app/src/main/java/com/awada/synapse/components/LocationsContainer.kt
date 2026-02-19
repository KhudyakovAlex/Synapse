package com.awada.synapse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.TitleMedium

data class LocationItem(
    val title: String,
    val iconResId: Int,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null
)

@Composable
fun LocationsContainer(
    locations: List<LocationItem>,
    modifier: Modifier = Modifier,
    cardSize: Dp = 156.dp,
    iconSize: Dp = 56.dp,
    singleScale: Float = 1.5f,
    contentOffsetY: Dp = 8.dp,
    spacing: Dp = 24.dp,
    showTitles: Boolean = true,
    emptyText: String = "Локации отсутсвуют. Подключитесь к контроллеру локации",
    emptyButtonText: String = "Найти контроллер",
    onEmptyButtonClick: (() -> Unit)? = null
) {
    val items = locations.take(5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (items.size) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = emptyText,
                        style = TitleMedium,
                        textAlign = TextAlign.Center
                    )
                    if (onEmptyButtonClick != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        PrimaryIconLButton(
                            text = emptyButtonText,
                            onClick = onEmptyButtonClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            1 -> {
                val scaledCard = cardSize * singleScale
                val scaledIcon = iconSize * singleScale
                LocationIcon(
                    title = items[0].title,
                    iconResId = items[0].iconResId,
                    showTitle = showTitles,
                    enabled = items[0].enabled,
                    onClick = items[0].onClick,
                    cardSize = scaledCard,
                    iconSize = scaledIcon,
                    contentOffsetY = contentOffsetY * singleScale
                )
            }
            2 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    items.forEach { item ->
                        LocationIcon(
                            title = item.title,
                            iconResId = item.iconResId,
                            showTitle = showTitles,
                            enabled = item.enabled,
                            onClick = item.onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                }
            }
            3 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        LocationIcon(
                            title = items[0].title,
                            iconResId = items[0].iconResId,
                            showTitle = showTitles,
                            enabled = items[0].enabled,
                            onClick = items[0].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                        LocationIcon(
                            title = items[1].title,
                            iconResId = items[1].iconResId,
                            showTitle = showTitles,
                            enabled = items[1].enabled,
                            onClick = items[1].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                    LocationIcon(
                        title = items[2].title,
                        iconResId = items[2].iconResId,
                        showTitle = showTitles,
                        enabled = items[2].enabled,
                        onClick = items[2].onClick,
                        cardSize = cardSize,
                        iconSize = iconSize,
                        contentOffsetY = contentOffsetY
                    )
                }
            }
            4 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        LocationIcon(
                            title = items[0].title,
                            iconResId = items[0].iconResId,
                            showTitle = showTitles,
                            enabled = items[0].enabled,
                            onClick = items[0].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                        LocationIcon(
                            title = items[1].title,
                            iconResId = items[1].iconResId,
                            showTitle = showTitles,
                            enabled = items[1].enabled,
                            onClick = items[1].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        LocationIcon(
                            title = items[2].title,
                            iconResId = items[2].iconResId,
                            showTitle = showTitles,
                            enabled = items[2].enabled,
                            onClick = items[2].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                        LocationIcon(
                            title = items[3].title,
                            iconResId = items[3].iconResId,
                            showTitle = showTitles,
                            enabled = items[3].enabled,
                            onClick = items[3].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                }
            }
            else -> {
                // 5: 2x2 grid + one centered below
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        LocationIcon(
                            title = items[0].title,
                            iconResId = items[0].iconResId,
                            showTitle = showTitles,
                            enabled = items[0].enabled,
                            onClick = items[0].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                        LocationIcon(
                            title = items[1].title,
                            iconResId = items[1].iconResId,
                            showTitle = showTitles,
                            enabled = items[1].enabled,
                            onClick = items[1].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        LocationIcon(
                            title = items[2].title,
                            iconResId = items[2].iconResId,
                            showTitle = showTitles,
                            enabled = items[2].enabled,
                            onClick = items[2].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                        LocationIcon(
                            title = items[3].title,
                            iconResId = items[3].iconResId,
                            showTitle = showTitles,
                            enabled = items[3].enabled,
                            onClick = items[3].onClick,
                            cardSize = cardSize,
                            iconSize = iconSize,
                            contentOffsetY = contentOffsetY
                        )
                    }
                    LocationIcon(
                        title = items[4].title,
                        iconResId = items[4].iconResId,
                        showTitle = showTitles,
                        enabled = items[4].enabled,
                        onClick = items[4].onClick,
                        cardSize = cardSize,
                        iconSize = iconSize,
                        contentOffsetY = contentOffsetY
                    )
                }
            }
        }
    }
}

