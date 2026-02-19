package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.components.iconResId
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Luminaire settings page.
 * Configure luminaire parameters.
 */
@Composable
fun PageSettingsLum(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showIconSelect by remember { mutableStateOf(false) }
    var iconId by remember { mutableIntStateOf(300) }
    var name by remember { mutableStateOf("") }
    var roomId by remember { mutableIntStateOf(-1) }
    var groupId by remember { mutableIntStateOf(-1) }
    var minBrightness by remember { mutableStateOf("") }
    var maxBrightness by remember { mutableStateOf("") }
    var fadeSec by remember { mutableStateOf("") }

    if (showIconSelect) {
        PageIconSelect(
            category = "luminaire",
            currentIconId = iconId,
            onIconSelected = { newId ->
                iconId = newId
                showIconSelect = false
            },
            onBackClick = { showIconSelect = false },
            modifier = modifier
        )
        return
    }

    // TODO: Add state management for all fields
    // TODO: Load actual data for dropdowns (rooms, groups)
    // TODO: Implement save logic
    
    // Mock data for dropdowns
    val roomItems = listOf(
        DropdownItem(1, "Гостиная"),
        DropdownItem(2, "Спальня"),
        DropdownItem(3, "Кухня"),
        DropdownItem(4, "Ванная")
    )
    
    val groupItems = listOf(
        DropdownItem(1, "Основное освещение"),
        DropdownItem(2, "Декоративное"),
        DropdownItem(3, "Рабочее")
    )
    
    PageContainer(
        title = "Настройки\nсветильника",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            // 1. Название
            TextField(
                value = name,
                onValueChange = { name = it },
                label = "Название",
                placeholder = "",
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 2. Иконка
            Column(
                verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8)
            ) {
                Text(
                    text = "Иконка",
                    style = LabelLarge,
                    color = PixsoColors.Color_Text_text_3_level,
                    modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12)
                )
                IconSelectButton(
                    icon = iconResId(context, iconId, fallback = R.drawable.luminaire_300_default),
                    onClick = { showIconSelect = true }
                )
            }
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 3. Помещение
            TextFieldForList(
                value = roomId.takeIf { it >= 0 },
                onValueChange = { roomId = it },
                icon = R.drawable.ic_chevron_down,
                label = "Помещение",
                placeholder = "Не выбрано",
                enabled = true,
                dropdownItems = roomItems
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 4. Группа
            TextFieldForList(
                value = groupId.takeIf { it >= 0 },
                onValueChange = { groupId = it },
                icon = R.drawable.ic_chevron_down,
                label = "Группа",
                placeholder = "Не выбрано",
                enabled = true,
                dropdownItems = groupItems
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 5. Минимальная яркость
            TextField(
                value = minBrightness,
                onValueChange = { minBrightness = it },
                label = "Минимальная яркость, %",
                placeholder = "",
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 6. Максимальная яркость
            TextField(
                value = maxBrightness,
                onValueChange = { maxBrightness = it },
                label = "Максимальная яркость, %",
                placeholder = "",
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 7. Время затухания
            TextField(
                value = fadeSec,
                onValueChange = { fadeSec = it },
                label = "Время затухания, сек",
                placeholder = "",
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))
            
            // Bottom buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16)
            ) {
                SecondaryButton(
                    text = "Отменить",
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                )
                
                PrimaryButton(
                    text = "Сохранить",
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
