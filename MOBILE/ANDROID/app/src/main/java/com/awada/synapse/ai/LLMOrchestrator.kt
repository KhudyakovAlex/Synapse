package com.awada.synapse.ai

import android.content.Context
import androidx.room.withTransaction
import com.awada.synapse.db.AIMessageEntity
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.logdog.Logdog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val ROLE_USER = "USER"
private const val ROLE_AI = "AI"
private val SUPPORTED_ACTION_TYPES = setOf(
    "deleteLocation",
    "reinitializeController",
    "createRoom",
    "createRooms",
    "deleteRoom",
    "deleteRooms",
    "saveGraph",
    "saveLuminaireScene",
    "saveScheduleEvent",
    "deleteScheduleEvent"
)
private val EFFECT_PREFIXES = listOf(
    "готово",
    "добавил",
    "добавила",
    "удалил",
    "удалила",
    "создал",
    "создала",
    "сохранил",
    "сохранила",
    "изменил",
    "изменила",
    "обновил",
    "обновила",
    "переименовал",
    "переименовала",
    "переместил",
    "переместила",
    "настроил",
    "настроила",
    "включил",
    "включила",
    "выключил",
    "выключила",
    "открыл",
    "открыла",
    "перешел",
    "перешла",
    "зашел",
    "зашла"
)

object LLMOrchestrator {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    suspend fun processUserMessage(
        context: Context,
        db: AppDatabase,
        history: List<AIMessageEntity>,
        uiContext: LLMUiContext,
        traceId: String? = null
    ): LLMConversationResult {
        val scopedControllerId = resolveScopedControllerId(db, uiContext)
        val appStateJson = AppStateExporter.exportAsJson(db, controllerId = scopedControllerId)
        val uiContextJson = json.encodeToString(uiContext)
        val systemPrompt = loadSystemPrompt(context)
        LLMDebugLog.log(
            "LLM orchestrator: controllerScope=${scopedControllerId ?: "all"} appStateChars=${appStateJson.length}"
        )
        Logdog.i(
            message = "LLM hidden context",
            traceId = traceId,
            fields = mapOf(
                "controllerScopeId" to scopedControllerId,
                "uiContextChars" to uiContextJson.length,
                "appStateChars" to appStateJson.length,
            ),
            attachments = listOf(
                Logdog.Attachment(
                    kind = "json",
                    name = "ui-context.json",
                    content = uiContextJson
                ),
                Logdog.Attachment(
                    kind = "json",
                    name = "app-db-state.json",
                    content = appStateJson
                )
            )
        )
        val messages = buildMessages(
            systemPrompt = systemPrompt,
            history = history,
            uiContextJson = uiContextJson,
            appStateJson = appStateJson
        )
        val rawReply = OllamaClient.chat(messages = messages, requireJson = true)
        val structuredReply = parseStructuredReply(rawReply)
        val normalizedReply = normalizeStructuredReply(structuredReply)

        db.withTransaction {
            val connectedControllers = AppStateExporter.getConnectedControllerIds(db)
            LLMDbPatchApplier.apply(
                sqliteDb = db.openHelper.writableDatabase,
                patch = normalizedReply.dbPatch ?: LLMDbPatch(),
                connectedControllers = connectedControllers,
                currentScreenName = uiContext.currentScreen.name
            )
        }

        val assistantText = normalizedReply.assistantText
        val normalizedReplyJson = json.encodeToString(normalizedReply)
        Logdog.i(
            message = "LLM structured response",
            traceId = traceId,
            fields = mapOf(
                "assistantTextChars" to assistantText.length,
                "updateCount" to (normalizedReply.dbPatch?.updates?.size ?: 0),
                "navigationScreen" to normalizedReply.navigation?.screen,
                "actionType" to normalizedReply.action?.type,
            ),
            attachments = listOf(
                Logdog.Attachment(
                    kind = "json",
                    name = "llm-structured-response.json",
                    content = normalizedReplyJson
                )
            )
        )
        return LLMConversationResult(
            assistantText = assistantText,
            navigation = normalizedReply.navigation,
            action = normalizedReply.action
        )
    }

    private fun buildMessages(
        systemPrompt: String,
        history: List<AIMessageEntity>,
        uiContextJson: String,
        appStateJson: String
    ): List<LLMChatMessage> {
        val messages = mutableListOf(
            LLMChatMessage(role = "system", content = systemPrompt),
            LLMChatMessage(role = "system", content = "UI_CONTEXT_JSON:\n$uiContextJson"),
            LLMChatMessage(role = "system", content = "APP_DB_STATE_JSON:\n$appStateJson")
        )
        history.forEach { msg ->
            messages += when (msg.role) {
                ROLE_USER -> LLMChatMessage(role = "user", content = msg.text)
                ROLE_AI -> LLMChatMessage(role = "assistant", content = msg.text)
                else -> return@forEach
            }
        }
        return messages
    }

    private fun parseStructuredReply(rawReply: String): LLMStructuredReply {
        val normalized = rawReply
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return try {
            json.decodeFromString<LLMStructuredReply>(normalized)
        } catch (t: Throwable) {
            LLMDebugLog.log(
                "LLM orchestrator: parse failure type=${t::class.java.simpleName} msg=${t.message ?: "null"}"
            )
            LLMStructuredReply(assistantText = rawReply.trim())
        }
    }

    private fun normalizeStructuredReply(reply: LLMStructuredReply): LLMStructuredReply {
        val normalizedAction = reply.action?.takeIf { it.type in SUPPORTED_ACTION_TYPES }
        val hasDbPatch = reply.dbPatch?.updates?.isNotEmpty() == true
        val hasNavigation = reply.navigation != null
        val hasAction = normalizedAction != null
        val hasEffect = hasDbPatch || hasNavigation || hasAction
        val assistantText = reply.assistantText.trim()
        val normalizedAssistantText = when {
            assistantText.isBlank() && hasEffect -> "Готово."
            !hasEffect && reply.action != null -> unsupportedRequestText()
            !hasEffect && looksLikeEffectOrConfirmationText(assistantText) -> unsupportedRequestText()
            assistantText.isBlank() -> "Не удалось получить понятный ответ."
            else -> assistantText
        }
        return reply.copy(
            assistantText = normalizedAssistantText,
            action = normalizedAction
        )
    }

    private fun looksLikeEffectOrConfirmationText(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase().trim()
        if ("подтверд" in normalized) return true
        return EFFECT_PREFIXES.any { normalized.startsWith(it) }
    }

    private fun unsupportedRequestText(): String {
        return "Пока не умею выполнить такой запрос через AI в приложении."
    }

    private fun loadSystemPrompt(context: Context): String {
        return com.awada.synapse.ai.loadSystemPrompt(context)
    }

    private suspend fun resolveScopedControllerId(
        db: AppDatabase,
        uiContext: LLMUiContext
    ): Int? {
        val params = uiContext.currentScreen.params
        return params.controllerId
            ?: params.luminaireId?.let { db.luminaireDao().getById(it)?.controllerId }
            ?: params.buttonPanelId?.let { db.buttonPanelDao().getById(it)?.controllerId }
            ?: params.presSensorId?.let { db.presSensorDao().getById(it)?.controllerId }
            ?: params.brightSensorId?.let { db.brightSensorDao().getById(it)?.controllerId }
            ?: params.scenarioId?.let { db.scenarioDao().getById(it)?.controllerId }
            ?: params.graphId?.let { db.graphDao().getById(it)?.controllerId }
            ?: params.eventId?.let { db.eventDao().getById(it)?.controllerId }
    }
}
