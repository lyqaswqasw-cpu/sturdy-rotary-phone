package com.example.ui.player

import android.net.Uri
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Structured diagnostic events for the Unified Media3 ExoPlayer Engine.
 * Covers all lifecycle, network, playlist/segment, decoder, watchdog, and server-failover stages.
 */
enum class PlayerDiagnosticEvent {
    PLAY_REQUESTED,
    PLAYLIST_LOADED,
    SEGMENT_LOADING,
    SEGMENT_FAILED,
    BUFFERING_STARTED,
    BUFFERING_ENDED,
    POSITION_STALLED,
    NETWORK_ERROR,
    HTTP_ERROR,
    DECODER_ERROR,
    PLAYER_ERROR,
    RECOVERY_ATTEMPT,
    RECOVERY_SUCCESS,
    RECOVERY_FAILED,
    SERVER_SWITCHED,
    PLAYBACK_READY
}

data class PlayerDiagnosticEntry(
    val timestampMs: Long,
    val event: PlayerDiagnosticEvent,
    val sanitizedUrl: String,
    val serverIndex: Int,
    val sourceType: String,
    val details: String
)

/**
 * Privacy-safe, toggleable diagnostics logger for FOUL TV's Unified Player Engine.
 * Never logs raw credentials, Xtream usernames/passwords, tokens, or sensitive query parameters.
 */
object PlayerDiagnostics {
    private const val TAG = "FoulTvUnifiedPlayer"
    private const val MAX_HISTORY_SIZE = 120

    /**
     * Disabled by default for regular users to avoid logcat overhead in production,
     * while retaining lightweight in-memory ring-buffer tracking for recovery diagnostics and tests.
     */
    @Volatile
    var isVerboseLoggingEnabled: Boolean = false

    private val recentEntries = CopyOnWriteArrayList<PlayerDiagnosticEntry>()

    /**
     * Redacts Xtream credentials (`/live/user/pass/123.m3u8` -> `/live/[REDACTED]/[REDACTED]/123.m3u8`),
     * basic auth (`user:pass@host`), and sensitive query tokens (`password=`, `token=`, `username=`, `auth=`).
     */
    fun sanitizeUrlForLogs(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""

        return try {
            val parsed = java.net.URI(trimmed)
            val scheme = parsed.scheme ?: "http"
            val host = parsed.host ?: "unknown-host"
            val portSuffix = if (parsed.port != -1) ":${parsed.port}" else ""
            val segments = parsed.rawPath
                ?.split('/')
                ?.filter { it.isNotEmpty() }
                .orEmpty()

            val sanitizedPath = when {
                // Xtream standard pattern: /live/{user}/{pass}/{streamId.ext} or /movie/{user}/{pass}/{id} or /series/{user}/{pass}/{id}
                segments.size >= 4 && segments[0].lowercase() in setOf("live", "movie", "series") -> {
                    val prefix = segments[0]
                    val tail = segments.drop(3).joinToString("/")
                    "/$prefix/[REDACTED]/[REDACTED]/$tail"
                }
                // Xtream 3-segment pattern: /{user}/{pass}/{streamId}
                segments.size == 3 && segments[2].any { it.isDigit() } -> {
                    "/[REDACTED]/[REDACTED]/${segments[2]}"
                }
                segments.isNotEmpty() -> {
                    val lastSegment = segments.last()
                    if (segments.size > 1) "/.../$lastSegment" else "/$lastSegment"
                }
                else -> "/"
            }

            val rawQuery = parsed.rawQuery.orEmpty()
            val sanitizedQuery = if (rawQuery.isNotEmpty()) {
                val safeParams = rawQuery.split('&').filter { it.isNotBlank() }.map { pair ->
                    val key = pair.substringBefore('=')
                    val value = pair.substringAfter('=', "")
                    val lowerKey = key.lowercase()
                    if (
                        lowerKey.contains("pass") ||
                        lowerKey.contains("user") ||
                        lowerKey.contains("token") ||
                        lowerKey.contains("auth") ||
                        lowerKey.contains("key") ||
                        lowerKey.contains("sig")
                    ) {
                        "$key=[REDACTED]"
                    } else {
                        if (value.length > 16) "$key=[MASKED]" else "$key=$value"
                    }
                }
                "?" + safeParams.joinToString("&")
            } else {
                ""
            }

            "$scheme://$host$portSuffix$sanitizedPath$sanitizedQuery"
        } catch (_: Throwable) {
            // Fallback regex sanitization if URI parsing fails
            trimmed
                .replace(Regex("://[^/@]+@"), "://[REDACTED]@")
                .replace(Regex("/(live|movie|series)/[^/]+/[^/]+/"), "/$1/[REDACTED]/[REDACTED]/")
                .replace(Regex("(?i)(password|username|token|auth|key)=([^&]+)"), "$1=[REDACTED]")
        }
    }

    fun record(
        event: PlayerDiagnosticEvent,
        rawUrl: String = "",
        serverIndex: Int = 0,
        sourceType: String = "",
        details: String = ""
    ) {
        val safeUrl = sanitizeUrlForLogs(rawUrl)
        val entry = PlayerDiagnosticEntry(
            timestampMs = System.currentTimeMillis(),
            event = event,
            sanitizedUrl = safeUrl,
            serverIndex = serverIndex,
            sourceType = sourceType,
            details = details
        )

        recentEntries.add(entry)
        while (recentEntries.size > MAX_HISTORY_SIZE) {
            recentEntries.removeAt(0)
        }

        if (isVerboseLoggingEnabled) {
            val message = buildString {
                append("[")
                append(event.name)
                append("] srv=#")
                append(serverIndex + 1)
                if (sourceType.isNotBlank()) {
                    append(" type=")
                    append(sourceType)
                }
                if (safeUrl.isNotBlank()) {
                    append(" url=")
                    append(safeUrl)
                }
                if (details.isNotBlank()) {
                    append(" | ")
                    append(details)
                }
            }
            when (event) {
                PlayerDiagnosticEvent.SEGMENT_FAILED,
                PlayerDiagnosticEvent.POSITION_STALLED,
                PlayerDiagnosticEvent.NETWORK_ERROR,
                PlayerDiagnosticEvent.HTTP_ERROR,
                PlayerDiagnosticEvent.DECODER_ERROR,
                PlayerDiagnosticEvent.PLAYER_ERROR,
                PlayerDiagnosticEvent.RECOVERY_FAILED -> Log.w(TAG, message)
                else -> Log.d(TAG, message)
            }
        }
    }

    fun getRecentEntries(): List<PlayerDiagnosticEntry> = recentEntries.toList()

    fun clearHistory() {
        recentEntries.clear()
    }
}
