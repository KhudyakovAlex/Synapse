package com.awada.synapse.logdog

import com.awada.synapse.BuildConfig
import com.awada.synapse.ai.LLMDebugLog
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object Logdog {
    private const val TAG = "Logdog"
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun boolField(name: String, default: Boolean): Boolean =
        runCatching { BuildConfig::class.java.getField(name).getBoolean(null) }.getOrDefault(default)

    private fun intField(name: String, default: Int): Int =
        runCatching { BuildConfig::class.java.getField(name).getInt(null) }.getOrDefault(default)

    private fun stringField(name: String, default: String): String =
        runCatching { BuildConfig::class.java.getField(name).get(null) as? String }.getOrDefault(default) ?: default

    fun d(message: String, traceId: String? = null, fields: Map<String, Any?> = emptyMap()) =
        send("debug", message, traceId, fields)

    fun i(message: String, traceId: String? = null, fields: Map<String, Any?> = emptyMap()) =
        send("info", message, traceId, fields)

    fun w(message: String, traceId: String? = null, fields: Map<String, Any?> = emptyMap()) =
        send("warn", message, traceId, fields)

    fun e(message: String, traceId: String? = null, fields: Map<String, Any?> = emptyMap()) =
        send("error", message, traceId, fields)

    fun send(level: String, message: String, traceId: String? = null, fields: Map<String, Any?> = emptyMap()) {
        val enabled = boolField("LOGDOG_ENABLED", default = false)
        if (!enabled) return

        val host = stringField("LOGDOG_HOST", default = "")
        if (host.isBlank()) return

        val port = intField("LOGDOG_PORT", default = 3000)
        val app = stringField("LOGDOG_APP", default = "synapse-android")

        val url = "http://$host:$port/logs"
        val bodyJson = JSONObject().apply {
            put("level", level)
            put("app", app)
            put("message", message)
            traceId?.let { put("traceId", it) }
            if (fields.isNotEmpty()) put("fields", JSONObject(fields))
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

