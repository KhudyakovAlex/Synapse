package com.awada.synapse.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object OllamaClient {
    private const val MODEL = "glm-4.7-flash:latest"
    private const val URL = "http://10.10.1.184:11434/api/chat"
    private const val TEMPERATURE = 0.1

    private val http = OkHttpClient.Builder()
        // LLM can be slow; defaults are too aggressive for local models.
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    @Serializable
    private data class ChatOptions(
        @SerialName("temperature") val temperature: Double? = null
    )

    @Serializable
    private data class ChatRequest(
        @SerialName("model") val model: String,
        @SerialName("messages") val messages: List<LLMChatMessage>,
        @SerialName("stream") val stream: Boolean,
        @SerialName("format") val format: String? = null,
        @SerialName("options") val options: ChatOptions? = null
    )

    @Serializable
    private data class ChatResponseMessage(
        @SerialName("role") val role: String? = null,
        @SerialName("content") val content: String? = null
    )

    @Serializable
    private data class ChatResponse(
        @SerialName("message") val message: ChatResponseMessage? = null
    )

    fun chat(messages: List<LLMChatMessage>, requireJson: Boolean = false): String {
        val totalChars = messages.sumOf { it.content.length }
        LLMDebugLog.log(
            "Ollama chat: start model=$MODEL url=$URL messages=${messages.size} chars=$totalChars requireJson=$requireJson temperature=$TEMPERATURE"
        )
        val body = json.encodeToString(
            ChatRequest(
                model = MODEL,
                messages = messages,
                stream = false,
                format = if (requireJson) "json" else null,
                options = ChatOptions(temperature = TEMPERATURE)
            )
        )
        messages.takeLast(3).forEachIndexed { index, message ->
            val preview = message.content
                .replace("\r", "")
                .replace("\n", "\\n")
                .take(200)
            if (preview.isNotBlank()) {
                LLMDebugLog.log(
                    "Ollama chat: message[$index] role=${message.role} preview=\"$preview\""
                )
            }
        }
        val req = Request.Builder()
            .url(URL)
            .post(body.toRequestBody(mediaType))
            .build()

        val t0 = System.currentTimeMillis()
        try {
            http.newCall(req).execute().use { resp ->
                val dtMs = System.currentTimeMillis() - t0
                val raw = resp.body?.string().orEmpty()
                LLMDebugLog.log("Ollama chat: http=${resp.code} ok=${resp.isSuccessful} dtMs=$dtMs rawChars=${raw.length}")

                if (!resp.isSuccessful) {
                    val rawPreview = raw.replace("\r", "").replace("\n", "\\n").take(200)
                    if (rawPreview.isNotBlank()) {
                        LLMDebugLog.log("Ollama chat: httpErrorBodyPreview=\"$rawPreview\"")
                    }
                    return "Ошибка Ollama HTTP ${resp.code}"
                }

                val parsed = runCatching { json.decodeFromString<ChatResponse>(raw) }.getOrNull()
                val text = (parsed?.message?.content ?: raw).trim()
                val preview = text.replace("\r", "").replace("\n", "\\n").take(200)
                if (preview.isNotBlank()) {
                    LLMDebugLog.log("Ollama chat: responsePreview=\"$preview\"")
                }
                return text
            }
        } catch (t: Throwable) {
            val dtMs = System.currentTimeMillis() - t0
            LLMDebugLog.log("Ollama chat: exception dtMs=$dtMs type=${t::class.java.simpleName} msg=${t.message ?: "null"}")
            throw t
        }
    }
}

