package com.awada.synapse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlin.math.roundToInt

private val DRAG_HANDLE_HEIGHT = 48.dp
private val DRAG_HANDLE_BAR_WIDTH = 40.dp
private val DRAG_HANDLE_BAR_HEIGHT = 4.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIAIChat(
    modifier: Modifier = Modifier,
    currentOffsetPx: Float,
    screenHeightPx: Float,
    dragHandleHeightPx: Float,
    anchoredDraggableState: AnchoredDraggableState<ChatState>
) {
    val density = LocalDensity.current
    val chatHeight = with(density) { (screenHeightPx - currentOffsetPx).toDp() }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, currentOffsetPx.roundToInt()) }
        ) {
            // Drag handle area - only this part can drag down to collapse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DRAG_HANDLE_HEIGHT)
                    .anchoredDraggable(
                        state = anchoredDraggableState,
                        orientation = Orientation.Vertical
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                // The drag handle bar
                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .size(width = DRAG_HANDLE_BAR_WIDTH, height = DRAG_HANDLE_BAR_HEIGHT)
                        .clip(RoundedCornerShape(PixsoDimens.Radius_Radius_Full))
                        .background(PixsoColors.Color_Bg_bg_elevated)
                )
            }
            
            // Gray content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chatHeight - DRAG_HANDLE_HEIGHT)
                    .clip(
                        RoundedCornerShape(
                            topStart = PixsoDimens.Radius_Radius_L,
                            topEnd = PixsoDimens.Radius_Radius_L,
                            bottomStart = PixsoDimens.Radius_Radius_None,
                            bottomEnd = PixsoDimens.Radius_Radius_None
                        )
                    )
                    .background(PixsoColors.Color_Bg_bg_canvas)
            ) {
                // Chat content will go here
            }
        }
    }
}
