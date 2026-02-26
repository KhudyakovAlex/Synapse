package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.awada.synapse.components.PrimaryIconButtonLarge
import com.awada.synapse.components.ScenarioPoint
import com.awada.synapse.components.ScenarioPointField
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun PageScenario(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(
        title = "Сенарий",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16),
            verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_12),
        ) {
            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))

            var p1Expanded by remember { mutableStateOf(false) }
            ScenarioPoint(
                title = "Кухня – Вкл",
                expanded = p1Expanded,
                onExpandedChange = { p1Expanded = it },
                whereField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                whatField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                valueField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                modifier = Modifier.fillMaxWidth(),
            )

            var p2Expanded by remember { mutableStateOf(false) }
            ScenarioPoint(
                title = "Спальня – Сцена 2",
                expanded = p2Expanded,
                onExpandedChange = { p2Expanded = it },
                whereField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                whatField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                valueField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                modifier = Modifier.fillMaxWidth(),
            )

            var p3Expanded by remember { mutableStateOf(false) }
            ScenarioPoint(
                title = "Гостиная – Выкл",
                expanded = p3Expanded,
                onExpandedChange = { p3Expanded = it },
                whereField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                whatField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                valueField = ScenarioPointField(value = null, onValueChange = {}, placeholder = "Не выбрано"),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_12))
            PrimaryIconButtonLarge(
                text = "Добавить",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

