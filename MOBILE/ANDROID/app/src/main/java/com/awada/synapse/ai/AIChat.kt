package com.awada.synapse.ai

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.awada.synapse.components.InputBar
import com.awada.synapse.db.AIMessageEntity
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val DRAG_HANDLE_HEIGHT = 48.dp
private val DRAG_HANDLE_BAR_WIDTH = 40.dp
private val DRAG_HANDLE_BAR_HEIGHT = 4.dp
private val CHAT_HORIZONTAL_PADDING = 16.dp

private const val ROLE_USER = "USER"
private const val ROLE_AI = "AI"
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatTime(timestampMs: Long): String {
    return Instant.ofEpochMilli(timestampMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(TIME_FORMATTER)
}

private fun buildPrompt(history: List<AIMessageEntity>): String {
    val sb = StringBuilder()
    sb.appendLine("Ты Synapse — ассистент приложения. Отвечай по-русски, кратко и по делу.")
    sb.appendLine()
    history.forEach { msg ->
        when (msg.role) {
            ROLE_USER -> sb.append("User: ").appendLine(msg.text)
            ROLE_AI -> sb.append("Assistant: ").appendLine(msg.text)
            else -> Unit
        }
    }
    sb.append("Assistant:")
    return sb.toString()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AIChat(
    modifier: Modifier = Modifier,
    currentOffsetPx: Float,
    screenHeightPx: Float,
    expandedTopOffsetPx: Float,
    mainPanelHeightPx: Float,
    anchoredDraggableState: AnchoredDraggableState<ChatState>
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val dao = remember { db.aiMessageDao() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by dao.observeAll().collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (dao.count() == 0) {
            dao.insert(
                AIMessageEntity(
                    role = ROLE_AI,
                    text = "Привет! Я Synapse — твой AI-ассистент. Чем могу помочь?",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val density = LocalDensity.current
    // Fixed height: from expanded top offset to bottom of screen, minus drag handle
    // Note: this extends under main panel to fill corner gaps (no clip on container)
    val fixedChatContentHeight = with(density) { 
        (screenHeightPx - expandedTopOffsetPx - DRAG_HANDLE_HEIGHT.toPx()).toDp()
    }
    
    // Convert main panel height to dp for padding calculations
    val mainPanelHeightDp = with(density) { mainPanelHeightPx.toDp() }
    
    // Calculate keyboard offset: lift chat content when keyboard appears
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val dragHandleHeightPx = with(density) { DRAG_HANDLE_HEIGHT.toPx() }
    val extraOffsetPx = with(density) { 30.dp.toPx() }
    val keyboardLiftPx = (imeBottomPx - mainPanelHeightPx * 2 - dragHandleHeightPx - extraOffsetPx).coerceAtLeast(0f)
    
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (currentOffsetPx - keyboardLiftPx).roundToInt()) }
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
                            bottom = mainPanelHeightDp + 24.dp + 56.dp + 16.dp // input bar position + input bar height + padding
                        ),
                    state = listState
                ) {
                    items(
                        count = messages.size,
                        key = { idx -> messages[idx].id }
                    ) { idx ->
                        val item = messages[idx]
                        val time = formatTime(item.createdAt)
                        when (item.role) {
                            ROLE_AI -> UIMessageAI(text = item.text, time = time)
                            else -> UIMessageUser(text = item.text, time = time)
                        }
                    }
                }
                
                // Input bar at fixed position from bottom (above main panel)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = CHAT_HORIZONTAL_PADDING,
                            end = CHAT_HORIZONTAL_PADDING,
                            top = 16.dp,
                            bottom = mainPanelHeightDp + 20.dp
                        )
                ) {
                    InputBar(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSendClick = {
                            val text = inputText.trim()
                            if (text.isEmpty() || isSending) return@InputBar
                            inputText = ""
                            isSending = true

                            scope.launch {
                                val now = System.currentTimeMillis()
                                dao.insert(
                                    AIMessageEntity(
                                        role = ROLE_USER,
                                        text = text,
                                        createdAt = now
                                    )
                                )

                                val recent = dao.getRecent(limit = 24).reversed()
                                val prompt = buildPrompt(recent)
                                val reply = withContext(Dispatchers.IO) {
                                    runCatching { OllamaClient.generateText(prompt) }
                                        .getOrElse { "Ошибка запроса к Ollama: ${it.message ?: "unknown"}" }
                                }.trim()

                                dao.insert(
                                    AIMessageEntity(
                                        role = ROLE_AI,
                                        text = if (reply.isNotEmpty()) reply else "…",
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                isSending = false
                            }
                        }
                    )
                }
            }
        }
    }
}
