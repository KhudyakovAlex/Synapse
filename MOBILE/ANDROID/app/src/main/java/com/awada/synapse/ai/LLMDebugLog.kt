package com.awada.synapse.ai

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

object LLMDebugLog {
    private const val MAX_LINES = 2000

    private val lock = Any()
    private val lines = ArrayDeque<String>(MAX_LINES)
    private val initializedThisProcess = AtomicBoolean(false)

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())

    fun clearOnProcessStart() {
        if (initializedThisProcess.compareAndSet(false, true)) {
            clear()
            log("LLM log started (process)")
        }
    }

    fun clear() {
        synchronized(lock) {
            lines.clear()
        }
    }

    fun log(message: String) {
        val ts = formatter.format(Instant.now())
        synchronized(lock) {
            if (lines.size >= MAX_LINES) {
                lines.removeFirst()
            }
            lines.addLast("$ts | $message")
        }
    }

    fun dump(): String {
        synchronized(lock) {
            return lines.joinToString(separator = "\n")
        }
    }
}

