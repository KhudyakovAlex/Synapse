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

        db.withTransaction {
            LLMDbPatchApplier.apply(
                sqliteDb = db.openHelper.writableDatabase,
                patch = structuredReply.dbPatch ?: LLMDbPatch()
            )
        }

        val assistantText = structuredReply.assistantText.trim().ifBlank {
            if (structuredReply.dbPatch?.updates?.isNotEmpty() == true) "Готово."
            else "Не удалось получить понятный ответ."
        }
        val normalizedReply = structuredReply.copy(assistantText = assistantText)
        val normalizedReplyJson = json.encodeToString(normalizedReply)
        Logdog.i(
            message = "LLM structured response",
            traceId = traceId,
            fields = mapOf(
                "assistantTextChars" to assistantText.length,
                "updateCount" to (normalizedReply.dbPatch?.updates?.size ?: 0),
                "navigationScreen" to normalizedReply.navigation?.screen,
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
            navigation = structuredReply.navigation
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
    }
}
