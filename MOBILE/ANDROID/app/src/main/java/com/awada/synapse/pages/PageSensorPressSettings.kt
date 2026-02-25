package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.DropdownItem
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.TextFieldForList
import com.awada.synapse.ui.theme.PixsoDimens

/**
 * Press sensor settings page.
 * Configure press sensor parameters.
 */
@Composable
fun PageSensorPressSettings(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var roomId by remember { mutableIntStateOf(-1) } // -1 = null
    var delaySec by remember { mutableStateOf("") }
    
    // Mock data for dropdown
    val roomItems = listOf(
        DropdownItem(1, "Гостиная"),
        DropdownItem(2, "Спальня"),
        DropdownItem(3, "Кухня"),
        DropdownItem(4, "Ванная")
    )
    
    PageContainer(
        title = "Настройки\nдатчика присутствия",
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
            
            // 2. Помещение
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
            
            // 3. Задержка
            TextField(
                value = delaySec,
                onValueChange = { delaySec = it },
                label = "Задержка, сек",
                placeholder = "",
                enabled = true
            )
            
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))
            Spacer(modifier = Modifier.height(20.dp))

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
