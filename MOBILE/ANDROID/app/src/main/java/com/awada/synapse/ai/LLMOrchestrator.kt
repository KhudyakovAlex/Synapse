package com.awada.synapse.ai

import android.content.Context
import androidx.room.withTransaction
import com.awada.synapse.db.AIMessageEntity
import com.awada.synapse.db.AppDatabase
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
        uiContext: LLMUiContext
    ): LLMConversationResult {
        val appStateJson = AppStateExporter.exportAsJson(db)
        val systemPrompt = loadSystemPrompt(context)
        LLMDebugLog.log("LLM orchestrator: appStateChars=${appStateJson.length}")
        val messages = buildMessages(
            systemPrompt = systemPrompt,
            history = history,
            uiContext = uiContext,
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
        return LLMConversationResult(
            assistantText = assistantText,
            navigation = structuredReply.navigation
        )
    }

    private fun buildMessages(
        systemPrompt: String,
        history: List<AIMessageEntity>,
        uiContext: LLMUiContext,
        appStateJson: String
    ): List<LLMChatMessage> {
        val messages = mutableListOf(
            LLMChatMessage(role = "system", content = systemPrompt),
            LLMChatMessage(role = "system", content = "UI_CONTEXT_JSON:\n${json.encodeToString(uiContext)}"),
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
}
