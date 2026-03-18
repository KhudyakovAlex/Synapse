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
import androidx.compose.foundation.layout.navigationBars
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
import com.awada.synapse.logdog.Logdog
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.PixsoDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt

private val DRAG_HANDLE_HEIGHT = 48.dp
private val DRAG_HANDLE_BAR_WIDTH = 40.dp
private val DRAG_HANDLE_BAR_HEIGHT = 4.dp
private val CHAT_HORIZONTAL_PADDING = 16.dp

private const val ROLE_USER = "USER"
private const val ROLE_AI = "AI"
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private const val LOGDOG_MAX_TEXT_CHARS = 40_000

private fun clipForLogdog(text: String, maxChars: Int = LOGDOG_MAX_TEXT_CHARS): Pair<String, Boolean> {
    if (text.length <= maxChars) return text to false
    return text.take(maxChars) to true
}

private fun formatTime(timestampMs: Long): String {
    return Instant.ofEpochMilli(timestampMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(TIME_FORMATTER)
}

private suspend fun isControllerConnected(db: AppDatabase, controllerId: Int): Boolean {
    return db.controllerDao().getById(controllerId)?.isConnected == true
}

private suspend fun hasAnyControllerDevices(db: AppDatabase, controllerId: Int): Boolean {
    return db.luminaireDao().observeCountForController(controllerId).first() +
        db.buttonPanelDao().observeCountForController(controllerId).first() +
        db.presSensorDao().observeCountForController(controllerId).first() +
        db.brightSensorDao().observeCountForController(controllerId).first() > 0
}

private suspend fun resolveVisibleAssistantText(
    db: AppDatabase,
    result: LLMConversationResult
): String {
    return when (result.action?.type) {
        "deleteLocation" -> "Подтвердите удаление локации."
        "reinitializeController" -> "Подтвердите переинициализацию контроллера."
        else -> {
            val navigation = result.navigation
            if (navigation?.screen == "InitializeController") {
                val controllerId = navigation.controllerId
                if (controllerId != null) {
                    when {
                        !isControllerConnected(db, controllerId) ->
                            "Сначала подключитесь к контроллеру этой локации."
                        hasAnyControllerDevices(db, controllerId) ->
                            "Подтвердите переинициализацию контроллера."
                        else ->
                            result.assistantText.trim()
                    }
                } else {
                    result.assistantText.trim()
                }
            } else {
                result.assistantText.trim()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AIChat(
    modifier: Modifier = Modifier,
    currentOffsetPx: Float,
    screenHeightPx: Float,
    expandedTopOffsetPx: Float,
    mainPanelHeightPx: Float,
    anchoredDraggableState: AnchoredDraggableState<ChatState>,
    isSending: Boolean,
    onSendingChange: (Boolean) -> Unit = {},
    uiContext: LLMUiContext,
    onNavigationCommand: (LLMNavigationCommand) -> Unit = {},
    onActionCommand: (LLMActionCommand) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val dao = remember { db.aiMessageDao() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by dao.observeAll().collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size, isSending) {
        val lastIndex = messages.lastIndex + if (isSending) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
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
    
    // With adjustNothing the whole sheet stays fixed, so lift only the bottom area
    // by the visible IME overlap above navigation bars.
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navigationBarsBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
    val imeOverlapDp = with(density) {
        (imeBottomPx - navigationBarsBottomPx).coerceAtLeast(0f).toDp()
    }
    val inputBarBottomPadding = if (imeOverlapDp > mainPanelHeightDp) {
        imeOverlapDp + 20.dp
    } else {
        mainPanelHeightDp + 20.dp
    }
    val messagesBottomPadding = inputBarBottomPadding + 4.dp + 56.dp + 16.dp
    
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
                            bottom = messagesBottomPadding
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
                    if (isSending) {
                        item(key = "llm_loading") {
                            UIMessageAILoading()
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
                            bottom = inputBarBottomPadding
                        )
                ) {
                    InputBar(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSendClick = {
                            val text = inputText.trim()
                            if (text.isEmpty() || isSending) return@InputBar
                            inputText = ""
                            onSendingChange(true)
                            val traceId = UUID.randomUUID().toString()
                            run {
                                val preview = text
                                    .replace("\r", "")
                                    .replace("\n", "\\n")
                                    .take(200)
                                LLMDebugLog.log("UI send: chars=${text.length} preview=\"$preview\"")
                            }

                            scope.launch {
                                try {
                                    run {
                                        val (userClipped, userTruncated) = clipForLogdog(text)
                                        Logdog.i(
                                            message = "USER:\n$userClipped",
                                            traceId = traceId,
                                            fields = mapOf<String, Any?>(
                                                "chars" to text.length,
                                                "truncated" to userTruncated,
                                            )
                                        )
                                    }
                                    val now = System.currentTimeMillis()
                                    dao.insert(
                                        AIMessageEntity(
                                            role = ROLE_USER,
                                            text = text,
                                            createdAt = now
                                        )
                                    )

                                    val recent = dao.getRecent(limit = 24).reversed()
                                    val result = withContext(Dispatchers.IO) {
                                        runCatching {
                                            LLMOrchestrator.processUserMessage(
                                                context = context.applicationContext,
                                                db = db,
                                                history = recent,
                                                uiContext = uiContext,
                                                traceId = traceId
                                            )
                                        }.getOrElse {
                                            LLMConversationResult(
                                                assistantText = "Не удалось выполнить запрос: ${it.message ?: "unknown"}"
                                            )
                                        }
                                    }
                                    val reply = withContext(Dispatchers.IO) {
                                        resolveVisibleAssistantText(
                                            db = db,
                                            result = result
                                        )
                                    }.ifBlank { result.assistantText.trim() }

                                    run {
                                        val (replyClipped, replyTruncated) = clipForLogdog(reply)
                                        Logdog.i(
                                            message = "LLM:\n$replyClipped",
                                            traceId = traceId,
                                            fields = mapOf<String, Any?>(
                                                "chars" to reply.length,
                                                "truncated" to replyTruncated,
                                                "navigationScreen" to result.navigation?.screen,
                                                "actionType" to result.action?.type,
                                            )
                                        )
                                    }
                                    dao.insert(
                                        AIMessageEntity(
                                            role = ROLE_AI,
                                            text = if (reply.isNotEmpty()) reply else "…",
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    val navigation = result.navigation
                                    if (navigation != null) {
                                        onNavigationCommand(navigation)
                                    }
                                    val action = result.action
                                    if (action != null) {
                                        onActionCommand(action)
                                    }
                                } finally {
                                    onSendingChange(false)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
