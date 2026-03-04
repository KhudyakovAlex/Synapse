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
    private const val URL = "http://10.10.1.184:11434/api/generate"

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
    private data class GenerateRequest(
        @SerialName("model") val model: String,
        @SerialName("prompt") val prompt: String,
        @SerialName("stream") val stream: Boolean
    )

    @Serializable
    private data class GenerateResponse(
        @SerialName("response") val response: String? = null
    )

    fun generateText(prompt: String): String {
        LLMDebugLog.log("Ollama generate: start model=$MODEL url=$URL promptChars=${prompt.length}")
        val body = json.encodeToString(GenerateRequest(model = MODEL, prompt = prompt, stream = false))
        val promptPreview = prompt
            .replace("\r", "")
            .replace("\n", "\\n")
            .take(200)
        if (promptPreview.isNotBlank()) {
            LLMDebugLog.log("Ollama generate: promptPreview=\"$promptPreview\"")
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
                LLMDebugLog.log("Ollama generate: http=${resp.code} ok=${resp.isSuccessful} dtMs=$dtMs rawChars=${raw.length}")

                if (!resp.isSuccessful) {
                    val rawPreview = raw.replace("\r", "").replace("\n", "\\n").take(200)
                    if (rawPreview.isNotBlank()) {
                        LLMDebugLog.log("Ollama generate: httpErrorBodyPreview=\"$rawPreview\"")
                    }
                    return "Ошибка Ollama HTTP ${resp.code}"
                }

                val parsed = runCatching { json.decodeFromString<GenerateResponse>(raw) }.getOrNull()
                val text = (parsed?.response ?: raw).trim()
                val preview = text.replace("\r", "").replace("\n", "\\n").take(200)
                if (preview.isNotBlank()) {
                    LLMDebugLog.log("Ollama generate: responsePreview=\"$preview\"")
                }
                return text
            }
        } catch (t: Throwable) {
            val dtMs = System.currentTimeMillis() - t0
            LLMDebugLog.log("Ollama generate: exception dtMs=$dtMs type=${t::class.java.simpleName} msg=${t.message ?: "null"}")
            throw t
        }
    }
}

