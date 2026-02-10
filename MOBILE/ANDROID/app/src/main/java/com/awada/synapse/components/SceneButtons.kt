package com.awada.synapse.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import com.awada.synapse.ui.theme.ButtonMedium

/**
 * Light scene configuration
 */
data class LightScene(
    val id: Int,
    val name: String,
    @DrawableRes val icon: Int
)

/**
 * Scene button with 3 states: Default, Pressed, Disabled
 */
@Composable
fun SceneButton(
    scene: LightScene,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    onSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cornerRadius = PixsoDimens.Radius_Radius_M

    // Determine colors based on state
    val backgroundColor = when {
        !isEnabled -> Color.Transparent
        isSelected -> PixsoColors.Color_State_tertiary
        else -> Color.Transparent
    }

    val textColor = when {
        !isEnabled -> PixsoColors.Color_State_on_disabled
        isSelected -> PixsoColors.Color_State_tertiary
        else -> PixsoColors.Color_State_on_tertiary
    }

    val borderColor = when {
        !isEnabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_State_on_tertiary
    }

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(enabled = isEnabled, onClick = onSelected)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon
            Icon(
                painter = painterResource(id = scene.icon),
                contentDescription = scene.name,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            // Label
            Text(
                text = scene.name,
                style = ButtonMedium,
                color = textColor
            )
        }
    }
}

/**
 * Panel with scene buttons
 */
@Composable
fun SceneButtonsPanel(
    scenes: List<LightScene> = emptyList(),
    onSceneSelected: (LightScene) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedSceneId = remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scene buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            scenes.forEach { scene ->
                SceneButton(
                    scene = scene,
                    isSelected = selectedSceneId.value == scene.id,
                    onSelected = {
                        selectedSceneId.value = scene.id
                        onSceneSelected(scene)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ===== QUICK BUTTONS =====

data class QuickButtonItem(
    val id: Int,
    val label: String
)

/**
 * Individual quick button (Off, 1, 2, 3, On)
 */
@Composable
fun QuickButton(
    item: QuickButtonItem,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    isLarge: Boolean = false,
    onSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isEnabled -> Color.Transparent
        isSelected -> PixsoColors.Color_State_on_primary
        else -> Color.Transparent
    }

    val textColor = when {
        !isEnabled -> PixsoColors.Color_State_on_disabled
        isSelected -> PixsoColors.Color_State_tertiary
        else -> PixsoColors.Color_State_on_tertiary
    }

    val borderColor = when {
        !isEnabled -> PixsoColors.Color_State_on_disabled
        else -> PixsoColors.Color_State_on_tertiary
    }

    val buttonWidth = if (isLarge) 75.dp else 44.dp

    Box(
        modifier = modifier
            .width(buttonWidth)
            .height(44.dp)
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_M))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
            )
            .clickable(enabled = isEnabled, onClick = onSelected)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(PixsoDimens.Radius_Radius_M)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.label,
            style = ButtonMedium,
            color = textColor
        )
    }
}

/**
 * Quick buttons row (Off, 1, 2, 3, On)
 */
@Composable
fun QuickButtonsRow(
    buttons: List<QuickButtonItem> = defaultQuickButtons,
    selectedId: Int? = null,
    onButtonSelected: (QuickButtonItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selected = remember { mutableStateOf(selectedId) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEachIndexed { index, button ->
            QuickButton(
                item = button,
                isSelected = selected.value == button.id,
                isLarge = index == 0 || index == buttons.size - 1,
                onSelected = {
                    selected.value = button.id
                    onButtonSelected(button)
                },
                modifier = if (index == 0 || index == buttons.size - 1) {
                    Modifier.weight(1.2f)
                } else {
                    Modifier.weight(0.6f)
                }
            )
        }
    }
}

val defaultQuickButtons = listOf(
    QuickButtonItem(id = 0, label = "Выкл"),
    QuickButtonItem(id = 1, label = "1"),
    QuickButtonItem(id = 2, label = "2"),
    QuickButtonItem(id = 3, label = "3"),
    QuickButtonItem(id = 4, label = "Вкл")
)
