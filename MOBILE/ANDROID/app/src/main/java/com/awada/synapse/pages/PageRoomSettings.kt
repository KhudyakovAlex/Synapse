package com.awada.synapse.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import com.awada.synapse.R
import com.awada.synapse.components.IconSelectButton
import com.awada.synapse.components.TextField
import com.awada.synapse.components.iconResId
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.RoomEntity
import com.awada.synapse.ui.theme.LabelLarge
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun PageRoomSettings(
    controllerId: Int? = null,
    roomId: Int? = null,
    initialName: String = "",
    initialIconId: Int = 200,
    onBackClick: () -> Unit,
    onSaved: (name: String, iconId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showIconSelect by remember { mutableStateOf(false) }
    var draftName by remember(initialName) { mutableStateOf(initialName) }
    var draftIconId by remember(initialIconId) { mutableIntStateOf(initialIconId) }
    var loadedKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val entityOrNull by remember(db, controllerId, roomId) {
        if (controllerId == null || roomId == null) {
            flowOf<RoomEntity?>(null)
        } else {
            db.roomDao()
                .observeAll(controllerId)
                .map { list -> list.firstOrNull { it.id == roomId } }
        }
    }.collectAsState(initial = null)

    LaunchedEffect(entityOrNull, controllerId, roomId) {
        val cid = controllerId ?: return@LaunchedEffect
        val rid = roomId ?: return@LaunchedEffect
        val key = cid to rid
        if (loadedKey == key) return@LaunchedEffect
        val e = entityOrNull
        if (e != null) {
            draftName = e.name
            draftIconId = e.icoNum
            loadedKey = key
        }
    }

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
    val handleBackClick: () -> Unit = handle@{
        val cid = controllerId
        val rid = roomId
        if (cid == null || rid == null) {
            onBackClick()
            return@handle
        }
        scope.launch {
            val current = db.roomDao().getById(cid, rid)
            val updated = (current ?: RoomEntity(controllerId = cid, id = rid))
                .copy(name = draftName, icoNum = draftIconId)
            if (current == null) {
                db.roomDao().insert(updated)
            } else {
                db.roomDao().update(updated)
            }
            onSaved(draftName, draftIconId)
            onBackClick()
        }
    }

    PageContainer(
        title = "Настройки\nпомещения",
        onBackClick = handleBackClick,
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
                enabled = controllerId != null && roomId != null
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
                    onClick = { if (controllerId != null && roomId != null) showIconSelect = true }
                )
            }
        }
    }
}

