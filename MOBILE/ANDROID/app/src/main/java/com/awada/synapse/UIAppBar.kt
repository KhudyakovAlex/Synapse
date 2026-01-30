package com.awada.synapse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.HeadlineSmall
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Typical App Bar component based on Pixso design.
 * 
 * @param title The text to display in the center.
 * @param onBackClick If not null, shows a back button that triggers this callback.
 * @param onSettingsClick If not null, shows a settings button that triggers this callback.
 */
@Composable
fun UIAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PixsoColors.Color_Bg_bg_subtle)
            .padding(top = statusBarPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left slot: Back button
            if (onBackClick != null) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp),
                            tint = PixsoColors.Color_Text_text_1_level
                        )
                    }
                }
            }

            // Center slot: Title (aligned to start)
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (onBackClick == null) 12.dp else 0.dp),
                style = HeadlineSmall,
                color = PixsoColors.Color_Text_text_1_level,
                textAlign = TextAlign.Start
            )

            // Right slot: Settings button
            if (onSettingsClick != null) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(32.dp),
                            tint = PixsoColors.Color_Text_text_1_level
                        )
                    }
                }
            }
        }

        // Bottom border
        HorizontalDivider(
            thickness = 1.dp,
            color = PixsoColors.Color_Border_border_shade_8
        )
    }
}
