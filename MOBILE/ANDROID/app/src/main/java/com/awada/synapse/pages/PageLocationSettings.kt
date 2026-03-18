package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.Switch
import com.awada.synapse.components.TextField
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.db.defaultRoomName
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Экран изменения параметров выбранной локации и перехода к смене пароля. */
internal val PageLocationSettingsLlmDescriptor = LLMPageDescriptor(
    fileName = "PageLocationSettings",
    screenName = "LocationSettings",
    titleRu = "Настройки локации",
    description = "Позволяет редактировать основные параметры локации и перейти к управлению паролем контроллера."
)

@Composable
fun PageLocationSettings(
    controllerId: Int?,
    onBackClick: () -> Unit,
    onSaved: ((name: String, iconId: Int) -> Unit)? = null,
    onRoomAdded: ((roomId: Int) -> Unit)? = null,
    onInitializeControllerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showIconSelect by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var showGraphs by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var draftIconId by remember { mutableIntStateOf(100) }
    var draftIsSchedule by remember { mutableStateOf(false) }
    var draftIsGraphs by remember { mutableStateOf(false) }
    var loadedForId by remember { mutableStateOf<Int?>(null) }
    var showReinitializeTooltip by remember { mutableStateOf(false) }

    LaunchedEffect(controllerId) {
        if (controllerId == null) return@LaunchedEffect
        if (loadedForId == controllerId) return@LaunchedEffect
        val c = db.controllerDao().getById(controllerId)
        if (c != null) {
            draftName = c.name
            draftIconId = c.icoNum
            draftIsSchedule = c.isSchedule
            draftIsGraphs = c.isGraphs
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
            controllerId = controllerId,
            onBackClick = { showSchedule = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (showGraphs) {
        PageGraphs(
            controllerId = controllerId,
            onBackClick = { showGraphs = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val iconRes = iconResId(context, draftIconId)
    val roomsOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<List<RoomEntity>?>(emptyList())
        } else {
            db.roomDao()
                .observeAll(controllerId)
                .map<List<RoomEntity>, List<RoomEntity>?> { it }
        }
    }.collectAsState(initial = null)
    val rooms = roomsOrNull
    val luminaireCountOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<Int?>(0)
        } else {
            db.luminaireDao()
                .observeCountForController(controllerId)
                .map<Int, Int?> { it }
        }
    }.collectAsState(initial = null)
    val buttonPanelCountOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<Int?>(0)
        } else {
            db.buttonPanelDao()
                .observeCountForController(controllerId)
                .map<Int, Int?> { it }
        }
    }.collectAsState(initial = null)
    val presSensorCountOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<Int?>(0)
        } else {
            db.presSensorDao()
                .observeCountForController(controllerId)
                .map<Int, Int?> { it }
        }
    }.collectAsState(initial = null)
    val brightSensorCountOrNull by remember(db, controllerId) {
        if (controllerId == null) {
            flowOf<Int?>(0)
        } else {
            db.brightSensorDao()
                .observeCountForController(controllerId)
                .map<Int, Int?> { it }
        }
    }.collectAsState(initial = null)
    val hasAnyControllerDevices =
        (luminaireCountOrNull ?: 0) +
            (buttonPanelCountOrNull ?: 0) +
            (presSensorCountOrNull ?: 0) +
            (brightSensorCountOrNull ?: 0) > 0

    val handleBackClick: () -> Unit = {
        val id = controllerId
        if (id == null) {
            onBackClick()
        } else {
            scope.launch {
                val current = db.controllerDao().getById(id)
                if (current != null) {
                    db.controllerDao().update(
                        current.copy(
                            name = draftName,
                            icoNum = draftIconId,
                            isSchedule = draftIsSchedule,
                            isGraphs = draftIsGraphs,
                        )
                    )
                }
                onSaved?.invoke(draftName, draftIconId)
                onBackClick()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PageContainer(
            title = "Настройки\nлокации",
            onBackClick = handleBackClick,
            isScrollable = true,
            modifier = Modifier.fillMaxSize()
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

                FeatureSettingsCard(
                    title = "Расписание",
                    isEnabled = draftIsSchedule,
                    onEnabledChange = { draftIsSchedule = it },
                    modifier = Modifier.fillMaxWidth(),
                    onConfigureClick = { showSchedule = true }
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16))

                FeatureSettingsCard(
                    title = "Графики",
                    isEnabled = draftIsGraphs,
                    onEnabledChange = { draftIsGraphs = it },
                    modifier = Modifier.fillMaxWidth(),
                    onConfigureClick = { showGraphs = true }
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                val canAddRoom = controllerId != null && rooms != null && rooms.size < 16
                SecondaryButton(
                    text = "Добавить помещение",
                    enabled = canAddRoom,
                    onClick = {
                        val cid = controllerId ?: return@SecondaryButton
                        val current = rooms ?: return@SecondaryButton
                        val usedIds = current.asSequence().map { it.id }.toHashSet()
                        val newId = (0..15).firstOrNull { it !in usedIds } ?: return@SecondaryButton
                        val nextPos = (current.maxOfOrNull { it.gridPos } ?: -1) + 1
                        scope.launch {
                            val currentController = db.controllerDao().getById(cid)
                            if (currentController != null) {
                                db.controllerDao().update(
                                    currentController.copy(
                                        name = draftName,
                                        icoNum = draftIconId,
                                        isSchedule = draftIsSchedule,
                                        isGraphs = draftIsGraphs,
                                    )
                                )
                            }
                            onSaved?.invoke(draftName, draftIconId)
                            db.roomDao().insert(
                                RoomEntity(
                                    controllerId = cid,
                                    id = newId,
                                    name = defaultRoomName(newId),
                                    gridPos = nextPos
                                )
                            )
                            onRoomAdded?.invoke(newId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                SecondaryButton(
                    text = "Сменить пароль контроллера",
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasAnyControllerDevices) {
                    Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                    SecondaryButton(
                        text = "Инициализировать контроллер",
                        onClick = { showReinitializeTooltip = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showReinitializeTooltip) {
            Tooltip(
                text = "Вы уверены, что хотите инициализировать контроллер заново? Все его настройки будут сброшены",
                primaryButtonText = "Да",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    when (result) {
                        TooltipResult.Primary -> {
                            showReinitializeTooltip = false
                            val cid = controllerId ?: return@Tooltip
                            scope.launch {
                                val currentController = db.controllerDao().getById(cid)
                                if (currentController != null) {
                                    db.controllerDao().update(
                                        currentController.copy(
                                            name = draftName,
                                            icoNum = draftIconId,
                                            isSchedule = draftIsSchedule,
                                            isGraphs = draftIsGraphs,
                                        )
                                    )
                                }
                                onSaved?.invoke(draftName, draftIconId)
                                onInitializeControllerClick?.invoke()
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Tertiary, TooltipResult.Quaternary, TooltipResult.Dismissed -> {
                            showReinitializeTooltip = false
                        }
                    }
                }
            )
        }

    }
}

@Composable
private fun FeatureSettingsCard(
    title: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = title,
                style = HeadlineExtraSmall,
                color = PixsoColors.Color_Text_text_1_level
            )

            Switch(
                isChecked = isEnabled,
                onCheckedChange = onEnabledChange,
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
