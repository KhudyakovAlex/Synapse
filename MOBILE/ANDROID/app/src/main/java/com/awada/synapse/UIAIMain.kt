package com.awada.synapse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIAIMain(
    modifier: Modifier = Modifier,
    anchoredDraggableState: AnchoredDraggableState<ChatState>
) {
    var isAIEnabled by remember { mutableStateOf(true) }
    var isVolumeEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(
                RoundedCornerShape(
                    topStart = PixsoDimens.Radius_Radius_L,
                    topEnd = PixsoDimens.Radius_Radius_L,
                    bottomStart = PixsoDimens.Radius_Radius_None,
                    bottomEnd = PixsoDimens.Radius_Radius_None
                )
            )
            .background(PixsoColors.Color_Bg_bg_elevated)
            .anchoredDraggable(
                state = anchoredDraggableState,
                orientation = Orientation.Vertical
            )
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left toggle - AI on/off
            UIToggle(
                isChecked = isAIEnabled,
                onCheckedChange = { isAIEnabled = it },
                iconOn = R.drawable.ic_ai,
                iconOff = R.drawable.ic_ai_off
            )

            // Center FAB button
            UIFabButton(
                state = if (isAIEnabled) FabState.Default else FabState.Disabled,
                onClick = { /* TODO: Handle mic action */ },
                icon = R.drawable.ic_microphone,
                iconDisabled = R.drawable.ic_microphone_off
            )

            // Right toggle - Volume on/off
            UIToggle(
                isChecked = isVolumeEnabled,
                onCheckedChange = { isVolumeEnabled = it },
                iconOn = R.drawable.ic_volume,
                iconOff = R.drawable.ic_volume_off,
                enabled = isAIEnabled
            )
        }
    }
}
