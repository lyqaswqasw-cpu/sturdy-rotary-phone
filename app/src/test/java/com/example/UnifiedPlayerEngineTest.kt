package com.example

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import com.example.data.model.ChannelServer
import com.example.ui.player.PlaybackRecoveryManager
import com.example.ui.player.PlayerDiagnosticEvent
import com.example.ui.player.PlayerDiagnostics
import com.example.ui.player.RecoveryDecision
import com.example.ui.player.StreamFailureCategory
import com.example.ui.player.StreamSourceResolver
import com.example.ui.player.StreamSourceType
import com.example.ui.player.WatchdogPlayerSnapshot
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnifiedPlayerEngineTest {

    @Before
    fun setUp() {
        PlayerDiagnostics.clearHistory()
        PlayerDiagnostics.isVerboseLoggingEnabled = false
    }

    @Test
    fun `detectStreamSourceType accurately identifies HLS M3U8, MPEG-TS, MP4, MKV, and Xtream URLs without mutating URLs`() {
        // 1. Standard HLS M3U8
        assertEquals(
            StreamSourceType.HLS,
            StreamSourceResolver.detectStreamSourceType(
                url = "http://iptv.example.com:8080/live/user123/pass456/9988.m3u8",
                isLiveChannel = true
            )
        )

        // 2. HLS M3U8 with query parameters
        assertEquals(
            StreamSourceType.HLS,
            StreamSourceResolver.detectStreamSourceType(
                url = "https://cdn.example.net/stream/index.m3u8?token=abc123xyz&expires=199999",
                isLiveChannel = true
            )
        )

        // 3. Direct MPEG-TS (.ts)
        assertEquals(
            StreamSourceType.MPEG_TS,
            StreamSourceResolver.detectStreamSourceType(
                url = "http://iptv.example.com:8080/live/user123/pass456/9988.ts",
                isLiveChannel = true
            )
        )

        // 4. Extensionless Xtream live URL -> MPEG-TS
        assertEquals(
            StreamSourceType.MPEG_TS,
            StreamSourceResolver.detectStreamSourceType(
                url = "http://iptv.example.com:8080/live/user123/pass456/9988",
                isLiveChannel = true
            )
        )

        // 5. Explicit serverStreamTypeHint overrides ambiguous URL
        assertEquals(
            StreamSourceType.HLS,
            StreamSourceResolver.detectStreamSourceType(
                url = "https://stream.example.org/play/channel_1",
                serverStreamTypeHint = "m3u8",
                isLiveChannel = true
            )
        )
        assertEquals(
            StreamSourceType.MPEG_TS,
            StreamSourceResolver.detectStreamSourceType(
                url = "https://stream.example.org/play/channel_1",
                serverStreamTypeHint = "ts",
                isLiveChannel = true
            )
        )

        // 6. VOD MP4 & MKV
        assertEquals(
            StreamSourceType.MP4,
            StreamSourceResolver.detectStreamSourceType(
                url = "http://vod.example.com/movie/user/pass/54321.mp4",
                isLiveChannel = false
            )
        )
        assertEquals(
            StreamSourceType.MKV,
            StreamSourceResolver.detectStreamSourceType(
                url = "http://vod.example.com/series/user/pass/77777.mkv",
                isLiveChannel = false
            )
        )
    }

    @Test
    fun `selectOptimalInitialServerIndex avoids 4K FHD and HEVC as initial default when safer HD or SD server exists`() {
        val serversWith4kAndHevcFirst = listOf(
            ChannelServer(id = "s1", name = "سيرفر 4K UHD", streamUrl = "https://a.com/4k.m3u8", streamType = "m3u8"),
            ChannelServer(id = "s2", name = "سيرفر HEVC H265", streamUrl = "https://a.com/hevc.m3u8", streamType = "m3u8"),
            ChannelServer(id = "s3", name = "سيرفر HD 720p", streamUrl = "https://a.com/hd.m3u8", streamType = "m3u8"),
            ChannelServer(id = "s4", name = "سيرفر SD 480p", streamUrl = "https://a.com/sd.m3u8", streamType = "m3u8")
        )

        val chosenIdx = StreamSourceResolver.selectOptimalInitialServerIndex(
            servers = serversWith4kAndHevcFirst,
            isLiveChannel = true
        )
        // Must pick index 2 ("سيرفر HD 720p") and NOT 4K (0) or HEVC (1)
        assertEquals(2, chosenIdx)

        // If Server 0 is already standard H.264 ("سيرفر أساسي 1"), keep index 0
        val standardServers = listOf(
            ChannelServer(id = "s1", name = "سيرفر 1 (HLS)", streamUrl = "https://a.com/1.m3u8", streamType = "m3u8"),
            ChannelServer(id = "s2", name = "سيرفر 2 (TS)", streamUrl = "https://a.com/1.ts", streamType = "ts")
        )
        assertEquals(
            0,
            StreamSourceResolver.selectOptimalInitialServerIndex(standardServers, isLiveChannel = true)
        )
    }

    @Test
    fun `PlaybackRecoveryManager performs immediate server switch on HTTP 404 or 403 and avoids infinite server loops`() {
        val recovery = PlaybackRecoveryManager(maxRetriesPerServer = 2)
        recovery.onChannelOrManualServerSelected(initialServerIndex = 0, totalServers = 2, nowRealtimeMs = 1_000L)

        // First server returns HTTP 404 -> must immediately switch to server #1 without wasting retries on server #0
        val decision1 = recovery.onPlayerErrorDetails(
            errorCode = PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            errorCodeName = "ERROR_CODE_IO_BAD_HTTP_STATUS",
            message = "Response code: 404 Not Found",
            currentUrl = "http://host.com/live/myuser/mypass/101.m3u8",
            sourceType = StreamSourceType.HLS,
            isLiveChannel = true,
            totalServers = 2
        )
        assertTrue(decision1 is RecoveryDecision.SwitchToNextServer)
        assertEquals(1, (decision1 as RecoveryDecision.SwitchToNextServer).nextServerIndex)

        // Apply automatic switch to server #1
        recovery.onAutoServerSwitched(newServerIndex = 1, totalServers = 2, nowRealtimeMs = 2_000L)

        // Second server also fails with HTTP 404 -> must NOT loop back to server #0; must report AllServersExhausted
        val decision2 = recovery.onPlayerErrorDetails(
            errorCode = PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            errorCodeName = "ERROR_CODE_IO_BAD_HTTP_STATUS",
            message = "Response code: 404 Not Found",
            currentUrl = "http://host.com/live/myuser/mypass/101.ts",
            sourceType = StreamSourceType.MPEG_TS,
            isLiveChannel = true,
            totalServers = 2
        )
        assertTrue(decision2 is RecoveryDecision.AllServersExhausted)
        assertEquals(
            StreamFailureCategory.HTTP_FATAL_STATUS,
            (decision2 as RecoveryDecision.AllServersExhausted).category
        )
    }

    @Test
    fun `PlaybackRecoveryManager performs graduated retries on transient network failure before switching servers`() {
        val recovery = PlaybackRecoveryManager(maxRetriesPerServer = 2)
        recovery.onChannelOrManualServerSelected(initialServerIndex = 0, totalServers = 2, nowRealtimeMs = 1_000L)

        // Attempt 1 -> ReloadCurrentServer #1 (1400ms backoff)
        val d1 = recovery.onPlayerErrorDetails(
            errorCode = PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            errorCodeName = "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT",
            message = "Network timeout",
            currentUrl = "http://host.com/live/user/pass/55.m3u8",
            sourceType = StreamSourceType.HLS,
            isLiveChannel = true,
            totalServers = 2
        )
        assertTrue(d1 is RecoveryDecision.ReloadCurrentServer)
        assertEquals(1, (d1 as RecoveryDecision.ReloadCurrentServer).attemptNumber)

        // Attempt 2 -> ReloadCurrentServer #2 (2800ms backoff + bitrate downgrade hint)
        val d2 = recovery.onPlayerErrorDetails(
            errorCode = PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            errorCodeName = "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT",
            message = "Network timeout",
            currentUrl = "http://host.com/live/user/pass/55.m3u8",
            sourceType = StreamSourceType.HLS,
            isLiveChannel = true,
            totalServers = 2
        )
        assertTrue(d2 is RecoveryDecision.ReloadCurrentServer)
        assertEquals(2, (d2 as RecoveryDecision.ReloadCurrentServer).attemptNumber)
        assertTrue(d2.downgradeBitrateHint)

        // Attempt 3 -> SwitchToNextServer (#1)
        val d3 = recovery.onPlayerErrorDetails(
            errorCode = PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            errorCodeName = "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT",
            message = "Network timeout",
            currentUrl = "http://host.com/live/user/pass/55.m3u8",
            sourceType = StreamSourceType.HLS,
            isLiveChannel = true,
            totalServers = 2
        )
        assertTrue(d3 is RecoveryDecision.SwitchToNextServer)
        assertEquals(1, (d3 as RecoveryDecision.SwitchToNextServer).nextServerIndex)
    }

    @Test
    fun `PlaybackRecoveryManager watchdog distinguishes active slow buffering from frozen stall and escalates gracefully`() {
        val recovery = PlaybackRecoveryManager(maxRetriesPerServer = 2)
        recovery.onChannelOrManualServerSelected(initialServerIndex = 0, totalServers = 2, nowRealtimeMs = 1_000L)

        // 1. Start buffering at t=1000ms
        val tick1 = recovery.evaluateWatchdogTick(
            WatchdogPlayerSnapshot(
                playbackState = Player.STATE_BUFFERING,
                playWhenReady = true,
                isPlaying = false,
                isLoading = true,
                currentPositionMs = 5_000L,
                bufferedPositionMs = 5_200L,
                isLiveChannel = true,
                currentServerIndex = 0,
                totalServersCount = 2,
                currentUrl = "http://host.com/live/user/pass/55.m3u8",
                sourceType = StreamSourceType.HLS,
                nowRealtimeMs = 1_000L
            )
        )
        assertEquals(RecoveryDecision.None, tick1)

        // 2. At t=9000ms (8s later), buffer is still growing (bufferedPositionMs = 8_500L) -> should NOT interrupt slow loading!
        val tick2 = recovery.evaluateWatchdogTick(
            WatchdogPlayerSnapshot(
                playbackState = Player.STATE_BUFFERING,
                playWhenReady = true,
                isPlaying = false,
                isLoading = true,
                currentPositionMs = 5_000L,
                bufferedPositionMs = 8_500L,
                isLiveChannel = true,
                currentServerIndex = 0,
                totalServersCount = 2,
                currentUrl = "http://host.com/live/user/pass/55.m3u8",
                sourceType = StreamSourceType.HLS,
                nowRealtimeMs = 9_000L
            )
        )
        assertEquals(RecoveryDecision.None, tick2)

        // 3. Now simulate a completely frozen buffer for >10s (t=20_000ms, no buffer growth since t=9_000ms)
        val tick3 = recovery.evaluateWatchdogTick(
            WatchdogPlayerSnapshot(
                playbackState = Player.STATE_BUFFERING,
                playWhenReady = true,
                isPlaying = false,
                isLoading = false,
                currentPositionMs = 5_000L,
                bufferedPositionMs = 8_500L,
                isLiveChannel = true,
                currentServerIndex = 0,
                totalServersCount = 2,
                currentUrl = "http://host.com/live/user/pass/55.m3u8",
                sourceType = StreamSourceType.HLS,
                nowRealtimeMs = 20_000L
            )
        )
        assertTrue(tick3 is RecoveryDecision.ReloadCurrentServer)
    }

    @Test
    fun `PlayerDiagnostics redacts Xtream usernames passwords and tokens from all URLs`() {
        val rawXtreamUrl = "http://vip-server.tv:8080/live/ahmed_user_99/secretPass2026/14920.m3u8?token=super_secret_jwt_token"
        val sanitized = PlayerDiagnostics.sanitizeUrlForLogs(rawXtreamUrl)

        assertFalse("Must not contain raw username", sanitized.contains("ahmed_user_99"))
        assertFalse("Must not contain raw password", sanitized.contains("secretPass2026"))
        assertFalse("Must not contain raw token", sanitized.contains("super_secret_jwt_token"))
        assertTrue("Must preserve stream file name and redaction markers", sanitized.contains("/live/[REDACTED]/[REDACTED]/14920.m3u8"))
        assertTrue("Must redact token parameter", sanitized.contains("token=[REDACTED]"))

        PlayerDiagnostics.record(
            event = PlayerDiagnosticEvent.PLAY_REQUESTED,
            rawUrl = rawXtreamUrl,
            serverIndex = 0,
            sourceType = StreamSourceType.HLS.label
        )
        val entries = PlayerDiagnostics.getRecentEntries()
        assertEquals(1, entries.size)
        assertFalse(entries.first().sanitizedUrl.contains("secretPass2026"))
    }
}
