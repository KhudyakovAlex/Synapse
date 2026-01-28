package com.awada.synapse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    expandedTopOffsetPx: Float,
    anchoredDraggableState: AnchoredDraggableState<ChatState>
) {
    val density = LocalDensity.current
    // Fixed height: from expanded top offset to bottom of screen, minus drag handle
    val fixedChatContentHeight = with(density) { 
        (screenHeightPx - expandedTopOffsetPx - DRAG_HANDLE_HEIGHT.toPx()).toDp()
    }

    var inputText by remember { mutableStateOf("") }
    
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
            
            // Gray content area with FIXED height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fixedChatContentHeight)
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
                
                // Input bar at fixed position from bottom (116dp from bottom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 116.dp)
                ) {
                    UIInputBar(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSendClick = {
                            // TODO: Handle send
                            inputText = ""
                        }
                    )
                }
            }
        }
    }
}
