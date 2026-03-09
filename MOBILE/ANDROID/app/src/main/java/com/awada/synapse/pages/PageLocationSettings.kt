package com.awada.synapse.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.RoomIcon
import com.awada.synapse.components.SecondaryButton
import com.awada.synapse.components.Switch
import com.awada.synapse.components.TextField
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.ui.theme.HeadlineExtraSmall
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun PageLocationSettings(
    controllerId: Int?,
    onBackClick: () -> Unit,
    onSaved: ((name: String, iconId: Int) -> Unit)? = null,
    onRoomClick: ((roomId: Int, roomTitle: String, roomIconId: Int) -> Unit)? = null,
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
    var pendingDeleteRoomId by remember { mutableIntStateOf(-1) }
    var pendingDeleteRoomTitle by remember { mutableStateOf("") }

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

    val handleBackClick: () -> Unit = {
        val id = controllerId
        if (id == null) {
            onBackClick()
        } else {
            scope.launch {
                db.controllerDao().updateNameAndIcon(id, draftName, draftIconId)
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

                ScheduleCard(
                    modifier = Modifier.fillMaxWidth(),
                    onConfigureClick = { showSchedule = true }
                )

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16)) {
                    Column(verticalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16)) {
                        (rooms ?: emptyList()).chunked(2).forEach { rowRooms ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PixsoDimens.Numeric_16)
                            ) {
                                rowRooms.forEach { r ->
                                    val title = r.name.ifBlank { "Помещение ${r.id + 1}" }
                                    val icon = iconResId(
                                        context = context,
                                        iconId = r.icoNum,
                                        fallback = R.drawable.location_208_kuhnya
                                    )
                                    RoomIcon(
                                        text = title,
                                        iconResId = icon,
                                        onClick = onRoomClick?.let { cb -> { cb(r.id, title, r.icoNum) } },
                                        onLongClick = {
                                            pendingDeleteRoomId = r.id
                                            pendingDeleteRoomTitle = title
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowRooms.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

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
                                db.roomDao().insert(
                                    RoomEntity(
                                        controllerId = cid,
                                        id = newId,
                                        gridPos = nextPos
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(PixsoDimens.Numeric_16 * 2))

                SecondaryButton(
                    text = "Сменить пароль контроллера",
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (pendingDeleteRoomId != -1) {
            val text = if (pendingDeleteRoomTitle.isNotBlank()) {
                "Удалить помещение «$pendingDeleteRoomTitle»?"
            } else {
                "Удалить помещение?"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Удалить",
                secondaryButtonText = "Отмена",
                onResult = { res ->
                    when (res) {
                        TooltipResult.Primary -> {
                            val cid = controllerId
                            val rid = pendingDeleteRoomId
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                            if (cid != null && rid != -1) {
                                scope.launch {
                                    val roomDao = db.roomDao()
                                    roomDao.deleteById(cid, rid)
                                    val remaining = roomDao.getAllOrdered(cid)
                                    remaining.forEachIndexed { index, r ->
                                        roomDao.setGridPos(cid, r.id, index)
                                    }
                                }
                            }
                        }

                        TooltipResult.Secondary, TooltipResult.Dismissed -> {
                            pendingDeleteRoomId = -1
                            pendingDeleteRoomTitle = ""
                        }
                    }
                }
            )
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
                style = HeadlineExtraSmall,
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
