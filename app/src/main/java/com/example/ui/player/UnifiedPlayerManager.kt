package com.example.ui.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized, single-engine Media3 [ExoPlayer] controller for FOUL TV.
 *
 * Guarantees:
 * - Only ONE active [ExoPlayer] instance exists during a playback session.
 * - Channel switching and server switching reuse the SAME [ExoPlayer] instance (`stop()` -> `clearMediaItems()` -> `setMediaSource()` -> `prepare()`),
 *   preventing decoder/surface recreation overhead and eliminating use-after-release bugs.
 * - Balanced [DefaultLoadControl] tuned for fast initial startup (1.2s bufferForPlayback) and smooth anti-jitter buffering (12s–36s) without exhausting RAM on budget phones or Android TV boxes.
 * - Automatic HLS/MPEG-TS/MP4/MKV routing via [StreamSourceResolver].
 * - Built-in [AnalyticsListener] hooks that feed segment/playlist events into [PlayerDiagnostics].
 * - Thread-safe `isReleased` guard so no late coroutine or callback ever invokes methods on a released player.
 */
@OptIn(UnstableApi::class)
class UnifiedPlayerManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val released = AtomicBoolean(false)

    var activeUrl: String = ""
        private set
    var activeServerIndex: Int = 0
        private set
    var activeSourceType: StreamSourceType = StreamSourceType.HLS
        private set
    var activeIsLive: Boolean = true
        private set

    val trackSelector: DefaultTrackSelector = DefaultTrackSelector(
        appContext,
        AdaptiveTrackSelection.Factory(
            /* minDurationForQualityIncreaseMs = */ 6_000,
            /* maxDurationForQualityDecreaseMs = */ 8_000,
            /* minDurationToRetainAfterDiscardMs = */ 12_000,
            /* bandwidthFraction = */ 0.78f
        )
    ).apply {
        setParameters(
            buildUponParameters()
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowVideoNonSeamlessAdaptiveness(true)
                .setAllowAudioMixedMimeTypeAdaptiveness(true)
                .setAllowAudioMixedSampleRateAdaptiveness(true)
                // Default initial cap at 1080p so weak devices don't choke on unexpected 4K variants
                .setMaxVideoSize(1920, 1080)
        )
    }

    private val loadControl: DefaultLoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ 12_000,
            /* maxBufferMs = */ 36_000,
            /* bufferForPlaybackMs = */ 1_200,
            /* bufferForPlaybackAfterRebufferMs = */ 2_500
        )
        .setTargetBufferBytes(24 * 1024 * 1024)
        .setPrioritizeTimeOverSizeThresholds(true)
        .setBackBuffer(
            /* backBufferDurationMs = */ 8_000,
            /* retainBackBufferFromKeyframe = */ false
        )
        .build()

    private val renderersFactory: DefaultRenderersFactory = DefaultRenderersFactory(appContext)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        .setEnableDecoderFallback(true)

    private val okHttpFactory = OkHttpStreamDataSource.Factory(
        client = StreamOkHttpClientProvider.sharedClient,
        userAgent = StreamOkHttpClientProvider.defaultUserAgent,
        onHttpCodeError = { statusCode, errorUrl ->
            PlayerDiagnostics.record(
                event = PlayerDiagnosticEvent.HTTP_ERROR,
                rawUrl = errorUrl,
                serverIndex = activeServerIndex,
                sourceType = activeSourceType.label,
                details = "HTTP response status $statusCode"
            )
        }
    )

    private val dataSourceFactory = DefaultDataSource.Factory(appContext, okHttpFactory)

    private val analyticsListener = object : AnalyticsListener {
        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            when (mediaLoadData.dataType) {
                C.DATA_TYPE_MEDIA -> {
                    PlayerDiagnostics.record(
                        event = PlayerDiagnosticEvent.SEGMENT_LOADING,
                        rawUrl = loadEventInfo.uri.toString(),
                        serverIndex = activeServerIndex,
                        sourceType = activeSourceType.label,
                        details = "trackType=${mediaLoadData.trackType}"
                    )
                }
            }
        }

        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            if (mediaLoadData.dataType == C.DATA_TYPE_MANIFEST) {
                PlayerDiagnostics.record(
                    event = PlayerDiagnosticEvent.PLAYLIST_LOADED,
                    rawUrl = loadEventInfo.uri.toString(),
                    serverIndex = activeServerIndex,
                    sourceType = activeSourceType.label,
                    details = "durationMs=${loadEventInfo.loadDurationMs} bytes=${loadEventInfo.bytesLoaded}"
                )
            }
        }

        override fun onLoadError(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) {
            if (!wasCanceled) {
                PlayerDiagnostics.record(
                    event = PlayerDiagnosticEvent.SEGMENT_FAILED,
                    rawUrl = loadEventInfo.uri.toString(),
                    serverIndex = activeServerIndex,
                    sourceType = activeSourceType.label,
                    details = "dataType=${mediaLoadData.dataType} err=${error.javaClass.simpleName}: ${error.message.orEmpty().take(80)}"
                )
            }
        }
    }

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext, renderersFactory)
        .setTrackSelector(trackSelector)
        .setLoadControl(loadControl)
        .setSeekParameters(SeekParameters.CLOSEST_SYNC)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()
        .apply {
            playWhenReady = true
            addAnalyticsListener(analyticsListener)
        }

    fun isReleased(): Boolean = released.get()

    /**
     * Loads and prepares a stream URL on the single [ExoPlayer] instance without recreating the player.
     * Cleanly aborts any previous network load before starting the new MediaSource.
     */
    fun loadStream(
        url: String,
        serverIndex: Int,
        serverStreamTypeHint: String = "",
        isLiveChannel: Boolean,
        startPositionMs: Long = C.TIME_UNSET,
        playImmediately: Boolean = true,
        downgradeBitrateCap: Boolean = false
    ): StreamSourceType {
        if (released.get()) return activeSourceType

        val cleanUrl = url.trim()
        val resolvedType = StreamSourceResolver.detectStreamSourceType(
            url = cleanUrl,
            serverStreamTypeHint = serverStreamTypeHint,
            isLiveChannel = isLiveChannel
        )

        activeUrl = cleanUrl
        activeServerIndex = serverIndex
        activeSourceType = resolvedType
        activeIsLive = isLiveChannel

        PlayerDiagnostics.record(
            event = PlayerDiagnosticEvent.PLAY_REQUESTED,
            rawUrl = cleanUrl,
            serverIndex = serverIndex,
            sourceType = resolvedType.label,
            details = "isLive=$isLiveChannel downgradeCap=$downgradeBitrateCap"
        )

        // If recovery requested a safer bitrate cap (e.g. after a stall or decoder hiccup),
        // constrain video selection to 720p HD while keeping adaptive switching active.
        if (downgradeBitrateCap) {
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setMaxVideoSize(1280, 720)
                .setMaxVideoBitrate(2_800_000)
                .build()
        }

        // Stop and clear any previous stream cleanly before setting the new MediaSource
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        if (isLiveChannel && exoPlayer.playbackParameters.speed != 1.0f) {
            exoPlayer.playbackParameters = PlaybackParameters(1.0f)
        }

        val mediaSource = StreamSourceResolver.createMediaSource(
            url = cleanUrl,
            sourceType = resolvedType,
            isLiveChannel = isLiveChannel,
            dataSourceFactory = dataSourceFactory
        )

        if (startPositionMs != C.TIME_UNSET && startPositionMs > 0L && !isLiveChannel) {
            exoPlayer.setMediaSource(mediaSource, startPositionMs)
        } else {
            exoPlayer.setMediaSource(mediaSource, /* resetPosition = */ true)
        }

        exoPlayer.playWhenReady = playImmediately
        exoPlayer.prepare()
        return resolvedType
    }

    /**
     * Performs a fast, non-destructive live-edge resync without tearing down the MediaSource.
     */
    fun seekToLiveEdgeNonDestructive() {
        if (released.get()) return
        if (exoPlayer.isCurrentMediaItemLive || activeIsLive) {
            exoPlayer.seekToDefaultPosition()
        }
        exoPlayer.playWhenReady = true
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        } else {
            exoPlayer.play()
        }
    }

    /**
     * Applies user-selected adaptive quality / data-saving mode to the underlying [ExoPlayer].
     */
    fun applyQualityMode(
        isAutoQuality: Boolean,
        isDataSaver: Boolean,
        manualTrackModification: ((TrackSelectionParameters.Builder) -> Unit)? = null
    ) {
        if (released.get()) return
        val builder = exoPlayer.trackSelectionParameters.buildUpon()
        builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)

        when {
            manualTrackModification != null -> {
                manualTrackModification(builder)
            }
            isDataSaver -> {
                builder.setMaxVideoSize(854, 480)
                builder.setMaxVideoBitrate(1_100_000)
            }
            isAutoQuality -> {
                builder.setMaxVideoSize(1920, 1080)
                builder.setMaxVideoBitrate(Int.MAX_VALUE)
            }
        }
        exoPlayer.trackSelectionParameters = builder.build()
    }

    /**
     * Safely releases the [ExoPlayer] once and only once when the player screen exits.
     */
    fun release() {
        if (released.compareAndSet(false, true)) {
            try {
                exoPlayer.removeAnalyticsListener(analyticsListener)
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.release()
            } catch (_: Throwable) {
            }
        }
    }
}
