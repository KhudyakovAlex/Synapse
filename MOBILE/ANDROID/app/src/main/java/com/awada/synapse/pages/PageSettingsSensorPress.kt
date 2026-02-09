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
import androidx.compose.ui.Modifier
import com.awada.synapse.R
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Press sensor settings page.
 * Configure press sensor parameters.
 */
@Composable
fun PageSettingsSensorPress(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Add state management for all fields
    // TODO: Load actual data for dropdowns (rooms)
    // TODO: Implement save logic
    
    // Mock data for dropdown
    val roomItems = listOf(
        DropdownItem(1, "Гостиная"),
        DropdownItem(2, "Спальня"),
        DropdownItem(3, "Кухня"),
        DropdownItem(4, "Ванная")
    )
    
    PageContainer(
        title = "Настройки",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            // 1. Название датчика присутствия
            TextField(
                value = "", // TODO: Bind to state
                onValueChange = { /* TODO */ },
                label = "Название датчика присутствия",
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
                    icon = R.drawable.controller_100_default, // TODO: Bind to selected icon
                    onClick = { /* TODO: Open icon selector */ }
                )
            }
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 3. Помещение
            TextFieldForList(
                value = null, // TODO: Bind to state
                onValueChange = { /* TODO */ },
                icon = R.drawable.ic_chevron_down,
                label = "Помещение",
                placeholder = "Не выбрано",
                enabled = true,
                dropdownItems = roomItems
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
            
            // 4. Задержка
            TextField(
                value = "", // TODO: Bind to state
                onValueChange = { /* TODO */ },
                label = "Задержка, сек",
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
                    onClick = { /* TODO: Save logic */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
