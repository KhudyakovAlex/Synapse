package com.awada.synapse.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object OllamaClient {
    private const val MODEL = "gpt-oss:20b"
    private const val URL = "http://10.10.1.184:11434/api/generate"

    private val http = OkHttpClient()
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
        val body = json.encodeToString(
            GenerateRequest.serializer(),
            GenerateRequest(model = MODEL, prompt = prompt, stream = false)
        )
        val req = Request.Builder()
            .url(URL)
            .post(body.toRequestBody(mediaType))
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                return "Ошибка Ollama HTTP ${resp.code}"
            }
            val raw = resp.body?.string().orEmpty()
            val parsed = runCatching { json.decodeFromString(GenerateResponse.serializer(), raw) }.getOrNull()
            return (parsed?.response ?: raw).trim()
        }
    }
}

