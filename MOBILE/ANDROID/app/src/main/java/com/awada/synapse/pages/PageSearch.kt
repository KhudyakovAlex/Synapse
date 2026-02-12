package com.awada.synapse.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.SearchSwampIndicator
import com.awada.synapse.ui.theme.PixsoColors

@Composable
fun PageSearch(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PageContainer(
        title = "Поиск",
        onBackClick = onBackClick,
        isScrollable = false,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Поиск…",
                    modifier = Modifier
                        .alpha(0.8f)
                        .padding(bottom = 18.dp),
                    color = PixsoColors.Color_Text_text_2_level
                )
                SearchSwampIndicator()
            }
        }
    }
}
