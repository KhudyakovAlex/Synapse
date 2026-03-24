package com.awada.synapse.logdog

import com.awada.synapse.BuildConfig
import com.awada.synapse.ai.LLMDebugLog
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom

object Logdog {
    private const val TAG = "Logdog"
    private const val APP_INSTANCE_ID_LENGTH = 10
    private const val APP_INSTANCE_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val secureRandom = SecureRandom()
    @Volatile
    private var appInstanceId: String? = null

    private fun boolField(name: String, default: Boolean): Boolean =
        runCatching { BuildConfig::class.java.getField(name).getBoolean(null) }.getOrDefault(default)

    private fun intField(name: String, default: Int): Int =
        runCatching { BuildConfig::class.java.getField(name).getInt(null) }.getOrDefault(default)

    private fun stringField(name: String, default: String): String =
        runCatching { BuildConfig::class.java.getField(name).get(null) as? String }.getOrDefault(default) ?: default

    data class Attachment(
        val kind: String,
        val name: String,
        val content: String
    )

    fun ensureSessionAppInstanceId(): String {
        appInstanceId?.let { return it }
        synchronized(this) {
            appInstanceId?.let { return it }
            val created = buildString(APP_INSTANCE_ID_LENGTH) {
                repeat(APP_INSTANCE_ID_LENGTH) {
                    append(APP_INSTANCE_ID_ALPHABET[secureRandom.nextInt(APP_INSTANCE_ID_ALPHABET.length)])
                }
            }
            appInstanceId = created
            return created
        }
    }

    fun d(
        message: String,
        traceId: String? = null,
        fields: Map<String, Any?> = emptyMap(),
        attachments: List<Attachment> = emptyList()
    ) = send("debug", message, traceId, fields, attachments)

    fun i(
        message: String,
        traceId: String? = null,
        fields: Map<String, Any?> = emptyMap(),
        attachments: List<Attachment> = emptyList()
    ) = send("info", message, traceId, fields, attachments)

    fun w(
        message: String,
        traceId: String? = null,
        fields: Map<String, Any?> = emptyMap(),
        attachments: List<Attachment> = emptyList()
    ) = send("warn", message, traceId, fields, attachments)

    fun e(
        message: String,
        traceId: String? = null,
        fields: Map<String, Any?> = emptyMap(),
        attachments: List<Attachment> = emptyList()
    ) = send("error", message, traceId, fields, attachments)

    fun send(
        level: String,
        message: String,
        traceId: String? = null,
        fields: Map<String, Any?> = emptyMap(),
        attachments: List<Attachment> = emptyList()
    ) {
        val enabled = boolField("LOGDOG_ENABLED", default = false)
        if (!enabled) return

        val host = stringField("LOGDOG_HOST", default = "")
        if (host.isBlank()) return

        val port = intField("LOGDOG_PORT", default = 3000)
        val baseApp = stringField("LOGDOG_APP", default = "synapse-android")
        val sessionAppInstanceId = ensureSessionAppInstanceId()
        val app = "$baseApp-$sessionAppInstanceId"
        val effectiveFields = fields + ("appInstanceId" to sessionAppInstanceId)

        val url = "http://$host:$port/logs"
        val bodyJson = JSONObject().apply {
            put("level", level)
            put("app", app)
            put("message", message)
            traceId?.let { put("traceId", it) }
            if (effectiveFields.isNotEmpty()) put("fields", JSONObject(effectiveFields))
            if (attachments.isNotEmpty()) {
                put(
                    "attachments",
                    JSONArray().apply {
                        attachments.forEach { attachment ->
                            put(
                                JSONObject().apply {
                                    put("kind", attachment.kind)
                                    put("name", attachment.name)
                                    put("content", attachment.content)
                                }
                            )
                        }
                    }
                )
            }
        }.toString()

        val req = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        client.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                val msg = "send failed url=$url type=${e::class.java.simpleName} msg=${e.message ?: "null"}"
                LLMDebugLog.log("Logdog: $msg")
                Log.w(TAG, msg, e)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    val msg = "send http=${response.code} url=$url"
                    LLMDebugLog.log("Logdog: $msg")
                    Log.w(TAG, msg)
                }
                response.close()
            }
        })
    }
}

