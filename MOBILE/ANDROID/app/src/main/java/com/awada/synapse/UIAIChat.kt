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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
private val CHAT_HORIZONTAL_PADDING = 16.dp
private val INPUT_BAR_BOTTOM_PADDING = 116.dp

// Chat message types
sealed class ChatItem {
    data class AIMessage(val text: String, val time: String) : ChatItem()
    data class UserMessage(val text: String, val time: String) : ChatItem()
    data class QuickReply(val text: String) : ChatItem()
}

// Mock data for demo
private val mockChatItems = listOf(
    ChatItem.AIMessage(
        text = "Привет! Я Synapse — твой AI-ассистент. Чем могу помочь?",
        time = "10:30"
    ),
    ChatItem.UserMessage(
        text = "Привет! Расскажи о своих возможностях",
        time = "10:31"
    ),
    ChatItem.AIMessage(
        text = "Я могу помочь с ответами на вопросы, написанием текстов, анализом данных и многим другим. Просто напиши, что тебя интересует!",
        time = "10:31"
    ),
    ChatItem.QuickReply(text = "Расскажи подробнее о своих возможностях и как ты можешь мне помочь"),
    ChatItem.QuickReply(text = "Покажи пример"),
    ChatItem.QuickReply(text = "Понятно, спасибо!")
)

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
                // Chat messages list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = CHAT_HORIZONTAL_PADDING,
                            end = CHAT_HORIZONTAL_PADDING,
                            bottom = INPUT_BAR_BOTTOM_PADDING + 56.dp + 16.dp // input bar height + padding
                        )
                ) {
                    items(mockChatItems) { item: ChatItem ->
                        when (item) {
                            is ChatItem.AIMessage -> UIMessageAI(
                                text = item.text,
                                time = item.time
                            )
                            is ChatItem.UserMessage -> UIMessageUser(
                                text = item.text,
                                time = item.time
                            )
                            is ChatItem.QuickReply -> UIQuickReply(
                                text = item.text,
                                onClick = { /* TODO: Handle quick reply */ }
                            )
                        }
                    }
                }
                
                // Input bar at fixed position from bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = CHAT_HORIZONTAL_PADDING,
                            end = CHAT_HORIZONTAL_PADDING,
                            top = 16.dp,
                            bottom = INPUT_BAR_BOTTOM_PADDING
                        )
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
