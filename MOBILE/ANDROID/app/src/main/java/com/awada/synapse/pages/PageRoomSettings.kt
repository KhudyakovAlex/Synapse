package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.iconResId
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@Composable
fun PageRoomSettings(
    initialName: String,
    initialIconId: Int,
    onBackClick: () -> Unit,
    onSaved: (name: String, iconId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showIconSelect by remember { mutableStateOf(false) }
    var draftName by remember(initialName) { mutableStateOf(initialName) }
    var draftIconId by remember(initialIconId) { mutableIntStateOf(initialIconId) }

    if (showIconSelect) {
        PageIconSelect(
            category = "room",
            currentIconId = draftIconId,
            onIconSelected = { newId ->
                draftIconId = newId
                showIconSelect = false
            },
            onBackClick = { showIconSelect = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val iconRes = iconResId(context, draftIconId, fallback = R.drawable.location_208_kuhnya)

    PageContainer(
        title = "Настройки\nпомещения",
        onBackClick = onBackClick,
        isScrollable = true,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PixsoDimens.Numeric_16)
        ) {
            TextField(
                value = draftName,
                onValueChange = { draftName = it },
                label = "Название",
                placeholder = "",
                enabled = true
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

            Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_8)) {
                androidx.compose.material3.Text(
                    text = "Иконка",
                    style = LabelLarge,
                    color = PixsoColors.Color_Text_text_3_level,
                    modifier = Modifier.padding(horizontal = PixsoDimens.Numeric_12)
                )
                IconSelectButton(
                    icon = iconRes,
                    onClick = { showIconSelect = true }
                )
            }

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))
            Spacer(modifier = Modifier.height(20.dp))

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
                    onClick = {
                        onSaved(draftName, draftIconId)
                        onBackClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

