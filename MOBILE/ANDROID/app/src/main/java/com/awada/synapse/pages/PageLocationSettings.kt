package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.PrimaryButton
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.Switch
import com.awada.synapse.components.TextField
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.HeadlineSmall
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.launch

@Composable
fun PageLocationSettings(
    controllerId: Int?,
    onBackClick: () -> Unit,
    onSaved: ((name: String, iconId: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showIconSelect by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var draftIconId by remember { mutableIntStateOf(100) }
    var loadedForId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(controllerId) {
        if (controllerId == null) return@LaunchedEffect
        if (loadedForId == controllerId) return@LaunchedEffect
        val c = db.controllerDao().getById(controllerId)
        if (c != null) {
            draftName = c.name
            draftIconId = c.icoNum
            loadedForId = controllerId
        }
    }

    if (showIconSelect) {
        PageIconSelect(
            category = "controller",
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

    if (showChangePassword) {
        PageChangePassword(
            onBackClick = { showChangePassword = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (showSchedule) {
        PageSchedule(
            onBackClick = { showSchedule = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val iconRes = iconResId(context, draftIconId)

    PageContainer(
        title = "Настройки\nлокации",
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

            ScheduleCard(
                modifier = Modifier.fillMaxWidth(),
                onConfigureClick = { showSchedule = true }
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

            SecondaryButton(
                text = "Сменить пароль",
                onClick = { showChangePassword = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))
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
                        val id = controllerId ?: return@PrimaryButton
                        scope.launch {
                            db.controllerDao().updateNameAndIcon(id, draftName, draftIconId)
                            onSaved?.invoke(draftName, draftIconId)
                            onBackClick()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_M))
            .background(PixsoColors.Color_Bg_bg_surface)
            .padding(PixsoDimens.Numeric_20),
        verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_24)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = "Расписание",
                style = HeadlineSmall,
                color = PixsoColors.Color_Text_text_1_level
            )

            Switch(
                isChecked = isEnabled,
                onCheckedChange = { isEnabled = it },
                enabled = true
            )
        }

        SecondaryButton(
            text = "Настроить",
            onClick = onConfigureClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
