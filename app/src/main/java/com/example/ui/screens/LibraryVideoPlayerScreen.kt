package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChannelEngagementStats
import com.example.data.model.PlaybackRequest
import com.example.data.model.PlayerGuideCategory
import com.example.data.model.PlayerGuideChannel
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.components.isConfirmKey
import com.example.ui.components.tvFocusHighlight
import com.example.ui.components.tvFocusableClickable
import com.example.ui.player.PlaybackRecoveryManager
import com.example.ui.player.PlayerDiagnosticEvent
import com.example.ui.player.PlayerDiagnostics
import com.example.ui.player.RecoveryDecision
import com.example.ui.player.StreamSourceResolver
import com.example.ui.player.StreamSourceType
import com.example.ui.player.UnifiedPlayerManager
import com.example.ui.player.WatchdogPlayerSnapshot
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(UnstableApi::class)
private enum class PlayerAspectMode(val resizeMode: Int, val labelAr: String) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT, "الأبعاد الأصلية"),
    ZOOM(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "تكبير الأطراف"),
    FILL(AspectRatioFrameLayout.RESIZE_MODE_FILL, "ملء الشاشة")
}

/**
 * Real Video Quality Track inspected directly from the active ExoPlayer Media3 stream
 */
@Immutable
private data class RealVideoQualityOption(
    val id: String,
    val trackGroup: TrackGroup,
    val trackIndex: Int,
    val width: Int,
    val height: Int,
    val bitrateBps: Int,
    val frameRate: Float,
    val shortBadge: String,
    val detailSubtitle: String
)

/**
 * Real Audio or Subtitle Track inspected from the active ExoPlayer Media3 stream
 */
@Immutable
private data class RealMediaTrackOption(
    val id: String,
    val trackGroup: TrackGroup,
    val trackIndex: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean
)

private fun formatResolutionShortBadge(width: Int, height: Int): String {
    val effectiveHeight = if (height > 0) height else (width * 9) / 16
    return when {
        effectiveHeight >= 2160 || width >= 3840 -> "4K UHD"
        effectiveHeight >= 1440 || width >= 2560 -> "2K QHD"
        effectiveHeight >= 1080 || width >= 1920 -> "1080p FHD"
        effectiveHeight >= 720 || width >= 1280 -> "720p HD"
        effectiveHeight >= 540 -> "576p SD"
        effectiveHeight >= 480 -> "480p SD"
        effectiveHeight >= 360 -> "360p"
        effectiveHeight > 0 -> "${effectiveHeight}p"
        else -> "HD"
    }
}

private fun formatQualityDetailSubtitle(width: Int, height: Int, bitrateBps: Int, frameRate: Float): String {
    val parts = mutableListOf<String>()
    if (width > 0 && height > 0) {
        parts.add("${width}×${height}")
    } else if (height > 0) {
        parts.add("${height}p")
    }
    if (frameRate >= 48f) {
        parts.add("${frameRate.toInt()}fps")
    }
    if (bitrateBps > 0) {
        val mbps = bitrateBps / 1_000_000f
        if (mbps >= 1f) {
            parts.add(String.format(Locale.US, "%.1f Mbps", mbps))
        } else {
            val kbps = bitrateBps / 1_000
            if (kbps > 0) parts.add("$kbps Kbps")
        }
    }
    return parts.joinToString(" • ").ifBlank { "بث مباشر أصلي" }
}

@OptIn(UnstableApi::class)
@Composable
fun LibraryVideoPlayerScreen(
    playbackRequest: PlaybackRequest,
    channelStats: ChannelEngagementStats? = null,
    showChannelViewsAndLikes: Boolean = true,
    guideCategories: List<PlayerGuideCategory> = emptyList(),
    guideChannels: List<PlayerGuideChannel> = emptyList(),
    onSelectGuideChannel: (PlayerGuideChannel) -> Unit = {},
    onToggleChannelLike: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Immediately lock into Landscape mode, keep screen awake, & enter immersive fullscreen
    DisposableEffect(activity) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val insetsController = if (window != null) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else null
        insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = originalOrientation
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val candidateUrls = playbackRequest.candidateUrls
    val initialServerIndex = remember(playbackRequest) {
        StreamSourceResolver.selectOptimalInitialServerIndex(
            servers = playbackRequest.servers,
            isLiveChannel = playbackRequest.isLiveChannel
        )
    }
    var currentUrlIndex by remember(playbackRequest) { mutableIntStateOf(initialServerIndex) }
    var reloadTrigger by remember(playbackRequest) { mutableIntStateOf(0) }
    var downgradeBitrateOnReload by remember(playbackRequest, currentUrlIndex) { mutableStateOf(false) }
    var isBuffering by remember(playbackRequest) { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var allExtensionsFailed by remember(playbackRequest) { mutableStateOf(false) }
    var errorDescriptionAr by remember(playbackRequest) { mutableStateOf("تعذر تشغيل هذا المصدر حالياً") }

    var durationMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var positionMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var bufferedPositionMs by remember(playbackRequest) { mutableLongStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    var isControlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }
    val lastPointerMoveMs = remember { LongArray(1) { 0L } }
    var isScreenLocked by remember { mutableStateOf(false) }
    var aspectMode by remember { mutableStateOf(PlayerAspectMode.FIT) }
    var showAspectToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Real Stream Quality State (inspected from active VideoSize & Tracks)
    var realDecodedWidth by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realDecodedHeight by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realStreamBitrate by remember(playbackRequest, currentUrlIndex) { mutableIntStateOf(0) }
    var realStreamFps by remember(playbackRequest, currentUrlIndex) { mutableFloatStateOf(0f) }
    var availableQualityTracks by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<List<RealVideoQualityOption>>(emptyList())
    }
    var availableAudioTracks by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<List<RealMediaTrackOption>>(emptyList())
    }
    var availableSubtitleTracks by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<List<RealMediaTrackOption>>(emptyList())
    }
    var isSubtitlesDisabled by remember(playbackRequest, currentUrlIndex) { mutableStateOf(false) }

    // null = Auto quality ("تلقائي"), non-null = locked to specific RealVideoQualityOption.id
    var selectedQualityTrackId by remember(playbackRequest, currentUrlIndex) {
        mutableStateOf<String?>(null)
    }
    var isLowDataMode by remember { mutableStateOf(false) }
    var isQualityPopupOpen by remember { mutableStateOf(false) }
    var isAudioSubsPopupOpen by remember { mutableStateOf(false) }
    var isServersPopupOpen by remember { mutableStateOf(false) }
    var isChannelSplitOpen by remember { mutableStateOf(false) }

    // TV Remote D-Pad FocusRequesters for instant, effortless control when HUD or overlays appear
    val centerPlayPauseFocusRequester = remember { FocusRequester() }
    val unlockFocusRequester = remember { FocusRequester() }
    val retryFocusRequester = remember { FocusRequester() }
    val qualityPopupCloseFocusRequester = remember { FocusRequester() }
    val serversPopupCloseFocusRequester = remember { FocusRequester() }
    val audioSubsPopupCloseFocusRequester = remember { FocusRequester() }

    // Double-tap / Remote / Button seek feedback (-10s or +10s)
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var seekFeedbackIsForward by remember { mutableStateOf(true) }
    var seekFeedbackTrigger by remember { mutableIntStateOf(0) }

    // Smart BackHandler: closes split-screen channel browser or popups first, or exits player cleanly
    BackHandler {
        when {
            isChannelSplitOpen -> {
                isChannelSplitOpen = false
            }
            isQualityPopupOpen || isAudioSubsPopupOpen || isServersPopupOpen -> {
                isQualityPopupOpen = false
                isAudioSubsPopupOpen = false
                isServersPopupOpen = false
            }
            isScreenLocked && !isControlsVisible -> {
                isControlsVisible = true
                interactionTick++
            }
            else -> onBack()
        }
    }

    // Resolve the active URL and Server hint without mutating URL extensions (.m3u8 <-> .ts)
    val resolvedActiveStreamUrl = remember(candidateUrls, currentUrlIndex) {
        if (currentUrlIndex !in candidateUrls.indices) {
            ""
        } else {
            candidateUrls[currentUrlIndex].trim()
        }
    }
    val activeServerTypeHint = remember(playbackRequest.servers, currentUrlIndex) {
        playbackRequest.servers.getOrNull(currentUrlIndex)?.streamType.orEmpty()
    }
    val activeSourceType = remember(resolvedActiveStreamUrl, activeServerTypeHint, playbackRequest.isLiveChannel) {
        StreamSourceResolver.detectStreamSourceType(
            url = resolvedActiveStreamUrl,
            serverStreamTypeHint = activeServerTypeHint,
            isLiveChannel = playbackRequest.isLiveChannel
        )
    }

    // Single Unified Media3 ExoPlayer Controller & Non-Destructive Watchdog/Recovery Manager
    val playerManager = remember(context) { UnifiedPlayerManager(context) }
    val exoPlayer = playerManager.exoPlayer
    val recoveryManager = remember { PlaybackRecoveryManager(maxRetriesPerServer = 2) }
    val coroutineScope = rememberCoroutineScope()
    var scheduledRecoveryJob by remember { mutableStateOf<Job?>(null) }

    // Reset recovery tracking whenever a new channel is opened
    LaunchedEffect(playbackRequest) {
        scheduledRecoveryJob?.cancel()
        recoveryManager.onChannelOrManualServerSelected(
            initialServerIndex = initialServerIndex,
            totalServers = candidateUrls.size.coerceAtLeast(1)
        )
    }

    // Helper for manual server selection by the user
    val selectServerManually: (Int, String?) -> Unit = { targetIndex, customToast ->
        if (targetIndex in candidateUrls.indices) {
            scheduledRecoveryJob?.cancel()
            allExtensionsFailed = false
            downgradeBitrateOnReload = false
            isBuffering = true
            recoveryManager.onChannelOrManualServerSelected(
                initialServerIndex = targetIndex,
                totalServers = candidateUrls.size.coerceAtLeast(1)
            )
            if (currentUrlIndex == targetIndex) {
                reloadTrigger += 1
            } else {
                currentUrlIndex = targetIndex
            }
            if (!customToast.isNullOrBlank()) {
                toastMessage = customToast
                showAspectToast = true
            }
        }
    }

    // Load current candidate URL on the single Media3 ExoPlayer instance
    LaunchedEffect(resolvedActiveStreamUrl, reloadTrigger, allExtensionsFailed, playbackRequest) {
        if (playerManager.isReleased()) return@LaunchedEffect
        if (!allExtensionsFailed && resolvedActiveStreamUrl.isNotBlank()) {
            isBuffering = true
            playerManager.applyQualityMode(
                isAutoQuality = !isLowDataMode,
                isDataSaver = isLowDataMode
            )
            selectedQualityTrackId = null

            val normalizedUrl = if (resolvedActiveStreamUrl.startsWith("/")) {
                Uri.fromFile(File(resolvedActiveStreamUrl)).toString()
            } else {
                resolvedActiveStreamUrl
            }

            playerManager.loadStream(
                url = normalizedUrl,
                serverIndex = currentUrlIndex,
                serverStreamTypeHint = activeServerTypeHint,
                isLiveChannel = playbackRequest.isLiveChannel,
                playImmediately = true,
                downgradeBitrateCap = downgradeBitrateOnReload
            )
        } else if (candidateUrls.isEmpty()) {
            isBuffering = false
            allExtensionsFailed = true
        }
    }

    // Helper to inspect video, audio, and subtitle tracks & active video format from ExoPlayer
    val updateRealQualityFromPlayer: (Tracks, VideoSize) -> Unit = remember(exoPlayer) {
        { tracks: Tracks, videoSize: VideoSize ->
            val discoveredVideo = mutableListOf<RealVideoQualityOption>()
            val discoveredAudio = mutableListOf<RealMediaTrackOption>()
            val discoveredSubs = mutableListOf<RealMediaTrackOption>()
            var activeFormatHeight = 0
            var activeFormatWidth = 0
            var activeFormatBitrate = 0
            var activeFormatFps = 0f

            for (groupIndex in 0 until tracks.groups.size) {
                val group = tracks.groups[groupIndex]
                val mediaTrackGroup = group.mediaTrackGroup
                when (group.type) {
                    C.TRACK_TYPE_VIDEO -> {
                        for (trackIndex in 0 until group.length) {
                            val format = group.getTrackFormat(trackIndex)
                            val w = if (format.width > 0) format.width else 0
                            val h = if (format.height > 0) format.height else 0
                            val br = if (format.bitrate > 0) format.bitrate else 0
                            val fps = if (format.frameRate > 0f) format.frameRate else 0f

                            if (group.isTrackSelected(trackIndex)) {
                                if (h > 0) activeFormatHeight = h
                                if (w > 0) activeFormatWidth = w
                                if (br > 0) activeFormatBitrate = br
                                if (fps > 0f) activeFormatFps = fps
                            }

                            if (group.isTrackSupported(trackIndex) && (h > 0 || w > 0 || br > 0)) {
                                val badge = formatResolutionShortBadge(w, h)
                                val subtitle = formatQualityDetailSubtitle(w, h, br, fps)
                                val optionId = "${groupIndex}_${trackIndex}_${w}x${h}_$br"
                                discoveredVideo.add(
                                    RealVideoQualityOption(
                                        id = optionId,
                                        trackGroup = mediaTrackGroup,
                                        trackIndex = trackIndex,
                                        width = w,
                                        height = h,
                                        bitrateBps = br,
                                        frameRate = fps,
                                        shortBadge = badge,
                                        detailSubtitle = subtitle
                                    )
                                )
                            }
                        }
                    }

                    C.TRACK_TYPE_AUDIO -> {
                        for (trackIndex in 0 until group.length) {
                            if (!group.isTrackSupported(trackIndex)) continue
                            val format = group.getTrackFormat(trackIndex)
                            val lang = format.language?.uppercase(Locale.US).orEmpty()
                            val label = format.label?.takeIf { it.isNotBlank() }
                                ?: if (lang.isNotBlank()) "مسار صوتي ($lang)" else "مسار صوتي ${discoveredAudio.size + 1}"
                            discoveredAudio.add(
                                RealMediaTrackOption(
                                    id = "aud_${groupIndex}_$trackIndex",
                                    trackGroup = mediaTrackGroup,
                                    trackIndex = trackIndex,
                                    label = label,
                                    language = lang,
                                    isSelected = group.isTrackSelected(trackIndex)
                                )
                            )
                        }
                    }

                    C.TRACK_TYPE_TEXT -> {
                        for (trackIndex in 0 until group.length) {
                            if (!group.isTrackSupported(trackIndex)) continue
                            val format = group.getTrackFormat(trackIndex)
                            val lang = format.language?.uppercase(Locale.US).orEmpty()
                            val label = format.label?.takeIf { it.isNotBlank() }
                                ?: if (lang.isNotBlank()) "ترجمة ($lang)" else "ترجمة ${discoveredSubs.size + 1}"
                            discoveredSubs.add(
                                RealMediaTrackOption(
                                    id = "sub_${groupIndex}_$trackIndex",
                                    trackGroup = mediaTrackGroup,
                                    trackIndex = trackIndex,
                                    label = label,
                                    language = lang,
                                    isSelected = group.isTrackSelected(trackIndex)
                                )
                            )
                        }
                    }
                }
            }

            val sortedDistinctVideo = discoveredVideo
                .distinctBy { "${it.height}_${it.width}_${it.bitrateBps / 50_000}" }
                .sortedWith(
                    compareByDescending<RealVideoQualityOption> { it.height }
                        .thenByDescending { it.width }
                        .thenByDescending { it.bitrateBps }
                )

            if (sortedDistinctVideo.size != availableQualityTracks.size ||
                sortedDistinctVideo.zip(availableQualityTracks).any { (a, b) -> a.id != b.id }
            ) {
                availableQualityTracks = sortedDistinctVideo
            }
            if (discoveredAudio.size != availableAudioTracks.size ||
                discoveredAudio.zip(availableAudioTracks).any { (a, b) -> a.id != b.id || a.isSelected != b.isSelected }
            ) {
                availableAudioTracks = discoveredAudio
            }
            if (discoveredSubs.size != availableSubtitleTracks.size ||
                discoveredSubs.zip(availableSubtitleTracks).any { (a, b) -> a.id != b.id || a.isSelected != b.isSelected }
            ) {
                availableSubtitleTracks = discoveredSubs
            }

            val finalWidth = if (videoSize.width > 0) videoSize.width else activeFormatWidth
            val finalHeight = if (videoSize.height > 0) videoSize.height else activeFormatHeight
            if (finalWidth > 0 && realDecodedWidth != finalWidth) realDecodedWidth = finalWidth
            if (finalHeight > 0 && realDecodedHeight != finalHeight) realDecodedHeight = finalHeight
            if (activeFormatBitrate > 0 && realStreamBitrate != activeFormatBitrate) realStreamBitrate = activeFormatBitrate
            if (activeFormatFps > 0f && realStreamFps != activeFormatFps) realStreamFps = activeFormatFps
        }
    }

    // Centralized executor for RecoveryDecision from either onPlayerError or the periodic Watchdog
    val executeRecoveryDecision: (RecoveryDecision) -> Unit = { decision ->
        when (decision) {
            is RecoveryDecision.None -> Unit

            is RecoveryDecision.SeekToDefaultLiveEdge -> {
                scheduledRecoveryJob?.cancel()
                isBuffering = true
                playerManager.seekToLiveEdgeNonDestructive()
            }

            is RecoveryDecision.ReloadCurrentServer -> {
                scheduledRecoveryJob?.cancel()
                isBuffering = true
                scheduledRecoveryJob = coroutineScope.launch {
                    if (decision.delayMs > 0L) delay(decision.delayMs)
                    if (!playerManager.isReleased()) {
                        downgradeBitrateOnReload = decision.downgradeBitrateHint
                        reloadTrigger += 1
                    }
                }
            }

            is RecoveryDecision.SwitchToNextServer -> {
                scheduledRecoveryJob?.cancel()
                isBuffering = true
                scheduledRecoveryJob = coroutineScope.launch {
                    if (decision.delayMs > 0L) delay(decision.delayMs)
                    if (!playerManager.isReleased()) {
                        recoveryManager.onAutoServerSwitched(
                            newServerIndex = decision.nextServerIndex,
                            totalServers = candidateUrls.size.coerceAtLeast(1)
                        )
                        downgradeBitrateOnReload = false
                        currentUrlIndex = decision.nextServerIndex
                        val srvName = playbackRequest.servers.getOrNull(decision.nextServerIndex)?.name
                        if (!srvName.isNullOrBlank()) {
                            toastMessage = "تم التحويل تلقائياً إلى: ${cleanServerDisplayName(srvName, decision.nextServerIndex)}"
                            showAspectToast = true
                        }
                    }
                }
            }

            is RecoveryDecision.AllServersExhausted -> {
                scheduledRecoveryJob?.cancel()
                isBuffering = false
                errorDescriptionAr = decision.userMessageAr
                allExtensionsFailed = true
            }
        }
    }

    // Keep latest state references for the single Player.Listener so we NEVER detach/release on channel switch
    val latestPlaybackRequest by rememberUpdatedState(playbackRequest)
    val latestCandidateUrls by rememberUpdatedState(candidateUrls)
    val latestCurrentUrlIndex by rememberUpdatedState(currentUrlIndex)
    val latestResolvedUrl by rememberUpdatedState(resolvedActiveStreamUrl)
    val latestSourceType by rememberUpdatedState(activeSourceType)
    val latestExecuteRecovery by rememberUpdatedState(executeRecoveryDecision)

    // CRITICAL LIFECYCLE FIX:
    // Keyed strictly on `playerManager` (never on `candidateUrls` or `playbackRequest`),
    // so switching channels or servers reuses the same ExoPlayer instance and only releases on screen exit.
    DisposableEffect(playerManager) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playerManager.isReleased()) return
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        PlayerDiagnostics.record(
                            event = PlayerDiagnosticEvent.BUFFERING_STARTED,
                            rawUrl = latestResolvedUrl,
                            serverIndex = latestCurrentUrlIndex,
                            sourceType = latestSourceType.label
                        )
                    }
                    Player.STATE_READY -> {
                        scheduledRecoveryJob?.cancel()
                        isBuffering = false
                        allExtensionsFailed = false
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        updateRealQualityFromPlayer(exoPlayer.currentTracks, exoPlayer.videoSize)
                        PlayerDiagnostics.record(
                            event = PlayerDiagnosticEvent.BUFFERING_ENDED,
                            rawUrl = latestResolvedUrl,
                            serverIndex = latestCurrentUrlIndex,
                            sourceType = latestSourceType.label
                        )
                        PlayerDiagnostics.record(
                            event = PlayerDiagnosticEvent.PLAYBACK_READY,
                            rawUrl = latestResolvedUrl,
                            serverIndex = latestCurrentUrlIndex,
                            sourceType = latestSourceType.label,
                            details = "video=${exoPlayer.videoSize.width}x${exoPlayer.videoSize.height}"
                        )
                    }
                    Player.STATE_ENDED -> {
                        if (latestPlaybackRequest.isLiveChannel) {
                            // Live broadcast shouldn't end normally; trigger watchdog/recovery
                            val snapshot = WatchdogPlayerSnapshot(
                                playbackState = playbackState,
                                playWhenReady = exoPlayer.playWhenReady,
                                isPlaying = exoPlayer.isPlaying,
                                isLoading = exoPlayer.isLoading,
                                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                                bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L),
                                isLiveChannel = true,
                                currentServerIndex = latestCurrentUrlIndex,
                                totalServersCount = latestCandidateUrls.size.coerceAtLeast(1),
                                currentUrl = latestResolvedUrl,
                                sourceType = latestSourceType,
                                nowRealtimeMs = System.currentTimeMillis()
                            )
                            latestExecuteRecovery(recoveryManager.evaluateWatchdogTick(snapshot))
                        } else {
                            isBuffering = false
                            isPlaying = false
                        }
                    }
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    realDecodedWidth = videoSize.width
                    realDecodedHeight = videoSize.height
                }
                updateRealQualityFromPlayer(exoPlayer.currentTracks, videoSize)
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateRealQualityFromPlayer(tracks, exoPlayer.videoSize)
            }

            override fun onPlayerError(error: PlaybackException) {
                if (playerManager.isReleased()) return
                val decision = recoveryManager.onPlayerError(
                    error = error,
                    currentUrl = latestResolvedUrl,
                    sourceType = latestSourceType,
                    isLiveChannel = latestPlaybackRequest.isLiveChannel,
                    totalServers = latestCandidateUrls.size.coerceAtLeast(1)
                )
                latestExecuteRecovery(decision)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            scheduledRecoveryJob?.cancel()
            exoPlayer.removeListener(listener)
            playerManager.release()
        }
    }

    // Non-destructive Watchdog: monitors real playback position & buffer advancement every 1000ms
    LaunchedEffect(playerManager, playbackRequest, currentUrlIndex, allExtensionsFailed) {
        if (allExtensionsFailed || candidateUrls.isEmpty()) return@LaunchedEffect
        while (!playerManager.isReleased() && !allExtensionsFailed) {
            delay(1_000L)
            if (playerManager.isReleased() || allExtensionsFailed) break
            // Do not interfere while a scheduled backoff reload/switch is already counting down
            if (scheduledRecoveryJob?.isActive == true) continue

            val snapshot = WatchdogPlayerSnapshot(
                playbackState = exoPlayer.playbackState,
                playWhenReady = exoPlayer.playWhenReady,
                isPlaying = exoPlayer.isPlaying,
                isLoading = exoPlayer.isLoading,
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L),
                isLiveChannel = playbackRequest.isLiveChannel,
                currentServerIndex = currentUrlIndex,
                totalServersCount = candidateUrls.size.coerceAtLeast(1),
                currentUrl = resolvedActiveStreamUrl,
                sourceType = activeSourceType,
                nowRealtimeMs = System.currentTimeMillis()
            )
            val decision = recoveryManager.evaluateWatchdogTick(snapshot)
            if (decision !is RecoveryDecision.None) {
                executeRecoveryDecision(decision)
            }
        }
    }

    // Poll playback position & buffer smoothly for VOD seekbar without interrupting user scrubbing
    LaunchedEffect(exoPlayer, isPlaying, playbackRequest.isLiveChannel, isScrubbing) {
        if (playbackRequest.isLiveChannel) return@LaunchedEffect
        while (true) {
            if (!isScrubbing) {
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            }
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration
            if (dur > 0L) durationMs = dur
            delay(180L)
        }
    }

    // Auto-hide controls after 4.5 seconds of active playback (resets whenever user interacts via Remote, Mouse, or Touch)
    LaunchedEffect(
        isControlsVisible,
        isPlaying,
        isBuffering,
        isScrubbing,
        isQualityPopupOpen,
        isAudioSubsPopupOpen,
        isServersPopupOpen,
        isChannelSplitOpen,
        interactionTick
    ) {
        if (
            isControlsVisible &&
            isPlaying &&
            !isBuffering &&
            !allExtensionsFailed &&
            !isScrubbing &&
            !isQualityPopupOpen &&
            !isAudioSubsPopupOpen &&
            !isServersPopupOpen &&
            !isChannelSplitOpen
        ) {
            delay(4500L)
            isControlsVisible = false
        }
    }

    // Hide seek feedback badge after 700ms
    LaunchedEffect(seekFeedbackTrigger) {
        if (seekFeedbackText != null) {
            delay(700L)
            seekFeedbackText = null
        }
    }

    // Hide aspect ratio / status toast after 1.3s
    LaunchedEffect(showAspectToast, aspectMode, toastMessage) {
        if (showAspectToast) {
            delay(1300L)
            showAspectToast = false
        }
    }

    // Helper for accurate ±10s seeking (Left = -10s Rewind, Right = +10s Forward)
    val seekByDeltaMs: (Long) -> Unit = { deltaMs ->
        val totalDur = exoPlayer.duration.takeIf { it > 0L } ?: durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE
        val targetPos = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, totalDur)
        exoPlayer.seekTo(targetPos)
        positionMs = targetPos
        scrubPositionMs = targetPos
        seekFeedbackIsForward = deltaMs > 0L
        seekFeedbackText = if (deltaMs > 0L) "+10 ثواني تقديم" else "-10 ثواني تأخير"
        seekFeedbackTrigger++
        interactionTick++
    }

    // Helper to toggle Mute/Unmute cleanly
    val toggleMute: () -> Unit = {
        val nextMuted = !isMuted
        isMuted = nextMuted
        exoPlayer.volume = if (nextMuted) 0f else 1f
        toastMessage = if (nextMuted) "تم كتم الصوت" else "تم تفعيل الصوت"
        showAspectToast = true
        interactionTick++
    }

    // Compute the real quality badge text
    val realActiveResolutionBadge = remember(realDecodedWidth, realDecodedHeight, availableQualityTracks, selectedQualityTrackId, isBuffering) {
        val lockedOption = availableQualityTracks.firstOrNull { it.id == selectedQualityTrackId }
        when {
            lockedOption != null -> lockedOption.shortBadge
            realDecodedHeight > 0 || realDecodedWidth > 0 -> formatResolutionShortBadge(realDecodedWidth, realDecodedHeight)
            availableQualityTracks.isNotEmpty() -> availableQualityTracks.first().shortBadge
            else -> "HD"
        }
    }

    val isSplitViewActive = isChannelSplitOpen && playbackRequest.isLiveChannel && guideChannels.isNotEmpty()

    // Helper to switch to Previous / Next Live Channel directly from the player via Remote or Mouse
    val switchRelativeGuideChannel: (Int) -> Unit = { delta ->
        if (guideChannels.size > 1) {
            val currentIdx = guideChannels.indexOfFirst { it.id == playbackRequest.channelId }
            val nextIdx = if (currentIdx >= 0) {
                (currentIdx + delta + guideChannels.size) % guideChannels.size
            } else {
                0
            }
            val nextChannel = guideChannels[nextIdx]
            interactionTick++
            onSelectGuideChannel(nextChannel)
            toastMessage = "القناة: ${nextChannel.name}"
            showAspectToast = true
        }
    }

    // Automatically focus the primary button when HUD or overlays open so TV Remote D-Pad works immediately
    LaunchedEffect(
        isControlsVisible,
        isScreenLocked,
        allExtensionsFailed,
        isQualityPopupOpen,
        isServersPopupOpen,
        isAudioSubsPopupOpen,
        isSplitViewActive
    ) {
        delay(60L)
        runCatching {
            when {
                allExtensionsFailed -> retryFocusRequester.requestFocus()
                isQualityPopupOpen && !isScreenLocked -> qualityPopupCloseFocusRequester.requestFocus()
                isServersPopupOpen && !isScreenLocked -> serversPopupCloseFocusRequester.requestFocus()
                isAudioSubsPopupOpen && !isScreenLocked -> audioSubsPopupCloseFocusRequester.requestFocus()
                isControlsVisible && isScreenLocked -> unlockFocusRequester.requestFocus()
                isControlsVisible && !isScreenLocked && !isSplitViewActive -> centerPlayPauseFocusRequester.requestFocus()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ==================== HALF 1 (OR FULLSCREEN): VIDEO PLAYER AREA ====================
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (isSplitViewActive) 0.5f else 1f)
                .align(Alignment.CenterStart)
                .background(Color.Black)
                // Universal TV Remote Control (D-Pad) & PC Keyboard Handler
                .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    interactionTick++
                    when (event.key) {
                        Key.MediaPlayPause -> {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isControlsVisible = true
                            true
                        }
                        Key.MediaPlay -> {
                            exoPlayer.play()
                            isControlsVisible = true
                            true
                        }
                        Key.MediaPause -> {
                            exoPlayer.pause()
                            isControlsVisible = true
                            true
                        }
                        Key.MediaFastForward -> {
                            if (!isScreenLocked && !playbackRequest.isLiveChannel && !allExtensionsFailed) {
                                seekByDeltaMs(10_000L)
                            } else if (playbackRequest.isLiveChannel && playbackRequest.servers.size > 1) {
                                val nextIdx = (currentUrlIndex + 1) % playbackRequest.servers.size
                                selectServerManually(nextIdx, "تبديل إلى: ${cleanServerDisplayName(playbackRequest.servers[nextIdx].name, nextIdx)}")
                            }
                            true
                        }
                        Key.MediaRewind -> {
                            if (!isScreenLocked && !playbackRequest.isLiveChannel && !allExtensionsFailed) {
                                seekByDeltaMs(-10_000L)
                            } else if (playbackRequest.isLiveChannel && playbackRequest.servers.size > 1) {
                                val prevIdx = (currentUrlIndex - 1 + playbackRequest.servers.size) % playbackRequest.servers.size
                                selectServerManually(prevIdx, "تبديل إلى: ${cleanServerDisplayName(playbackRequest.servers[prevIdx].name, prevIdx)}")
                            }
                            true
                        }
                        Key.ChannelUp, Key.PageUp -> {
                            if (playbackRequest.isLiveChannel && !isScreenLocked) {
                                if (guideChannels.size > 1) {
                                    switchRelativeGuideChannel(-1)
                                    true
                                } else if (playbackRequest.servers.size > 1) {
                                    val nextIdx = (currentUrlIndex + 1) % playbackRequest.servers.size
                                    selectServerManually(nextIdx, "تبديل إلى: ${cleanServerDisplayName(playbackRequest.servers[nextIdx].name, nextIdx)}")
                                    true
                                } else false
                            } else false
                        }
                        Key.ChannelDown, Key.PageDown -> {
                            if (playbackRequest.isLiveChannel && !isScreenLocked) {
                                if (guideChannels.size > 1) {
                                    switchRelativeGuideChannel(1)
                                    true
                                } else if (playbackRequest.servers.size > 1) {
                                    val prevIdx = (currentUrlIndex - 1 + playbackRequest.servers.size) % playbackRequest.servers.size
                                    selectServerManually(prevIdx, "تبديل إلى: ${cleanServerDisplayName(playbackRequest.servers[prevIdx].name, prevIdx)}")
                                    true
                                } else false
                            } else false
                        }
                        Key.M, Key.VolumeMute -> {
                            toggleMute()
                            true
                        }
                        Key.F -> {
                            if (!isScreenLocked) {
                                aspectMode = when (aspectMode) {
                                    PlayerAspectMode.FIT -> PlayerAspectMode.ZOOM
                                    PlayerAspectMode.ZOOM -> PlayerAspectMode.FILL
                                    PlayerAspectMode.FILL -> PlayerAspectMode.FIT
                                }
                                toastMessage = aspectMode.labelAr
                                showAspectToast = true
                                true
                            } else false
                        }
                        Key.DirectionLeft -> {
                            // If HUD is hidden in VOD, D-Pad Left seeks -10s immediately; otherwise reveals HUD / moves D-Pad focus
                            if (!isControlsVisible && !isScreenLocked && !allExtensionsFailed) {
                                if (!playbackRequest.isLiveChannel) {
                                    seekByDeltaMs(-10_000L)
                                } else {
                                    isControlsVisible = true
                                }
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionRight -> {
                            // If HUD is hidden in VOD, D-Pad Right seeks +10s immediately; otherwise reveals HUD / moves D-Pad focus
                            if (!isControlsVisible && !isScreenLocked && !allExtensionsFailed) {
                                if (!playbackRequest.isLiveChannel) {
                                    seekByDeltaMs(10_000L)
                                } else {
                                    isControlsVisible = true
                                }
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            if (!isControlsVisible) {
                                isControlsVisible = true
                                true
                            } else {
                                false
                            }
                        }
                        else -> {
                            if (event.key.isConfirmKey() && !isControlsVisible && !allExtensionsFailed) {
                                isControlsVisible = true
                                true
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    false
                }
            }
            // PC Mouse Move Detection: reveals HUD controls automatically when moving the mouse cursor (throttled to avoid 60fps recomposition)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move) {
                            if (!isControlsVisible) {
                                isControlsVisible = true
                            }
                            val now = System.currentTimeMillis()
                            if (now - lastPointerMoveMs[0] > 900L) {
                                lastPointerMoveMs[0] = now
                                interactionTick++
                            }
                        }
                    }
                }
            }
            // Touch / Mouse Click & Double-Tap Gestures
            .pointerInput(playbackRequest.isLiveChannel, allExtensionsFailed, isQualityPopupOpen, isAudioSubsPopupOpen, isServersPopupOpen, isScreenLocked) {
                detectTapGestures(
                    onTap = {
                        interactionTick++
                        if (isQualityPopupOpen || isAudioSubsPopupOpen || isServersPopupOpen) {
                            isQualityPopupOpen = false
                            isAudioSubsPopupOpen = false
                            isServersPopupOpen = false
                        } else {
                            isControlsVisible = !isControlsVisible
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!isScreenLocked && !playbackRequest.isLiveChannel && !allExtensionsFailed) {
                            val screenWidth = size.width
                            // Universal Video Timeline Convention:
                            // Right side of screen -> +10s forward (تقديم 10 ثواني)
                            // Left side of screen -> -10s backward (تأخير 10 ثواني)
                            if (offset.x > screenWidth / 2f) {
                                seekByDeltaMs(10_000L)
                            } else {
                                seekByDeltaMs(-10_000L)
                            }
                        }
                    }
                )
            }
    ) {
        // 1. Unified Hardware-Accelerated AndroidX Media3 ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = aspectMode.resizeMode
                    keepScreenOn = true
                    isFocusable = false
                    isFocusableInTouchMode = false
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player !== exoPlayer) {
                    playerView.player = exoPlayer
                }
                if (playerView.resizeMode != aspectMode.resizeMode) {
                    playerView.resizeMode = aspectMode.resizeMode
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Pure Circular Buffering Indicator WITHOUT ANY TEXT ("من دون وجود نصوص جاري تجهيز عرض سينما... لا اريد ذلك اريد فقط دائرة ومن دون نصوص")
        if (isBuffering && !allExtensionsFailed) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = FoulBottomBarBlack.copy(alpha = 0.72f),
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(FoulPurpleBright, FoulTeamAwayCyan, FoulPurpleBright)
                        )
                    ),
                    modifier = Modifier.size(66.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = FoulPurpleBright,
                            trackColor = Color.White.copy(alpha = 0.14f),
                            strokeWidth = 3.5.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("player_loading_spinner")
                        )
                    }
                }
            }
        }

        // 3. Error State if all extensions / active server failed
        if (allExtensionsFailed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FoulBlackBg.copy(alpha = 0.94f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.6f)),
                    modifier = Modifier.widthIn(max = 460.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "خطأ في التشغيل",
                            tint = FoulLiveRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorDescriptionAr,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        if (playbackRequest.isLiveChannel && playbackRequest.servers.size > 1) {
                            Text(
                                text = "اختر سيرفر بث آخر فوراً من القائمة أو أعد المحاولة",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(playbackRequest.servers) { idx, srv ->
                                    val isCurrent = idx == currentUrlIndex
                                    val cleanName = cleanServerDisplayName(srv.name, idx)
                                    Surface(
                                        color = if (isCurrent) FoulPurpleBright else FoulCardElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .tvFocusableClickable(
                                                shape = RoundedCornerShape(8.dp),
                                                scaleOnFocus = 1.06f,
                                                onClick = {
                                                    selectServerManually(idx, "جاري التشغيل على: $cleanName")
                                                }
                                            )
                                    ) {
                                        Text(
                                            text = cleanName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val retryIdx = if (currentUrlIndex in candidateUrls.indices) currentUrlIndex else 0
                                    selectServerManually(retryIdx, null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                                modifier = Modifier
                                    .focusRequester(retryFocusRequester)
                                    .tvFocusHighlight(shape = RoundedCornerShape(20.dp))
                                    .testTag("player_retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("إعادة المحاولة", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated),
                                modifier = Modifier
                                    .tvFocusHighlight(shape = RoundedCornerShape(20.dp))
                                    .testTag("player_error_back_button")
                            ) {
                                Text("رجوع", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 4. Double-Tap / Remote / Button Seek Feedback Badge (Pinned to Physical Left for -10s and Physical Right for +10s)
        AnimatedVisibility(
            visible = seekFeedbackText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                if (seekFeedbackIsForward) AbsoluteAlignment.CenterRight else AbsoluteAlignment.CenterLeft
            )
        ) {
            Surface(
                color = FoulPurpleDeep.copy(alpha = 0.92f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, FoulTeamAwayCyan.copy(alpha = 0.65f)),
                modifier = Modifier.padding(horizontal = 42.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (seekFeedbackIsForward) Icons.Default.Forward10 else Icons.Default.Replay10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = seekFeedbackText.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 5. Aspect Ratio / Action Toast Badge
        AnimatedVisibility(
            visible = showAspectToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
        ) {
            Surface(
                color = FoulPurpleDeep.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, FoulPurpleBright)
            ) {
                Text(
                    text = toastMessage.ifBlank { aspectMode.labelAr },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // 6.A SCREEN LOCKED FLOATING UNLOCK PILL (Prevents accidental touches during live match or movie)
        AnimatedVisibility(
            visible = isControlsVisible && isScreenLocked && !allExtensionsFailed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp)
        ) {
            Surface(
                color = FoulBottomBarBlack.copy(alpha = 0.90f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, FoulPurpleBright),
                modifier = Modifier
                    .focusRequester(unlockFocusRequester)
                    .clip(RoundedCornerShape(24.dp))
                    .tvFocusableClickable(
                        shape = RoundedCornerShape(24.dp),
                        scaleOnFocus = 1.06f,
                        onClick = {
                            isScreenLocked = false
                            toastMessage = "تم فتح قفل التحكم"
                            showAspectToast = true
                        }
                    )
                    .testTag("player_unlock_screen_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "إلغاء قفل الشاشة",
                        tint = FoulGoldStar,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "الشاشة مقفلة • اضغط لفتح التحكم",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 6.B FULL PROFESSIONAL PLAYER CONTROLS OVERLAY (Live Broadcast HUD & Library Cinema HUD)
        AnimatedVisibility(
            visible = isControlsVisible && !isScreenLocked && !allExtensionsFailed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.86f),
                                Color.Black.copy(alpha = 0.14f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            ) {
                // ==================== TOP BAR ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title, Subtitle & Back Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(FoulCardDark.copy(alpha = 0.88f))
                                .tvFocusableClickable(
                                    shape = CircleShape,
                                    scaleOnFocus = 1.08f,
                                    onClick = onBack
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.widthIn(max = if (isSplitViewActive) 150.dp else 320.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (playbackRequest.isLiveChannel) {
                                    Surface(
                                        color = FoulLiveRed,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Text(
                                                text = "بث مباشر",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = FoulPurpleDeep.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, FoulPurpleBright.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "CINEMA PRO",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                            color = FoulTeamAwayCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = playbackRequest.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (playbackRequest.subtitle.isNotBlank()) {
                                Text(
                                    text = playbackRequest.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    // Top Bar Action Pills: Horizontally scrollable so every button is 100% accessible on TV Remote & Mouse even in Split-Screen mode
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Live Channel Engagement Pills (Views Counter + Like Button)
                        if (playbackRequest.isLiveChannel && showChannelViewsAndLikes) {
                            val viewsCount = channelStats?.views ?: 0L
                            val likesCount = channelStats?.likes ?: 0L
                            val isLiked = channelStats?.isLikedByMe == true

                            Surface(
                                color = FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .testTag("player_views_counter_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "عدد المشاهدات",
                                        tint = FoulTeamAwayCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$viewsCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = if (isLiked) FoulLiveRed.copy(alpha = 0.90f) else FoulPurpleDeep.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        scaleOnFocus = 1.06f,
                                        onClick = { onToggleChannelLike() }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isLiked) Color.White else Color.White.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("player_like_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "إعجاب بالقناة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$likesCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Split-Screen Channels & Categories Guide Trigger in Top Bar (فتح نص شاشة انقسام لعرض التصنيفات والقنوات)
                        if (playbackRequest.isLiveChannel && guideChannels.isNotEmpty()) {
                            Surface(
                                color = if (isChannelSplitOpen) {
                                    FoulPurpleBright
                                } else {
                                    FoulPurpleDeep.copy(alpha = 0.90f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        scaleOnFocus = 1.06f,
                                        onClick = {
                                            interactionTick++
                                            isQualityPopupOpen = false
                                            isAudioSubsPopupOpen = false
                                            isServersPopupOpen = false
                                            isChannelSplitOpen = !isChannelSplitOpen
                                        }
                                    )
                                    .border(
                                        width = 1.1.dp,
                                        color = if (isChannelSplitOpen) Color.White else FoulTeamAwayCyan.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("player_split_channels_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerticalSplit,
                                        contentDescription = "عرض القنوات في نصف شاشة انقسام",
                                        tint = if (isChannelSplitOpen) Color.White else FoulTeamAwayCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isChannelSplitOpen) "إغلاق القنوات" else "القنوات (${guideChannels.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Dedicated Real Stream Qualities Button in Top Bar ("زر لعرض الجودات الفعلية للبث")
                        Surface(
                            color = if (isQualityPopupOpen) {
                                FoulPurpleBright
                            } else if (isLowDataMode) {
                                FoulLiveGreen.copy(alpha = 0.25f)
                            } else {
                                FoulCardDark.copy(alpha = 0.88f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    scaleOnFocus = 1.06f,
                                    onClick = {
                                        interactionTick++
                                        isServersPopupOpen = false
                                        isAudioSubsPopupOpen = false
                                        isQualityPopupOpen = !isQualityPopupOpen
                                    }
                                )
                                .border(
                                    width = 1.1.dp,
                                    color = when {
                                        isQualityPopupOpen -> Color.White
                                        isLowDataMode -> FoulLiveGreen
                                        else -> FoulTeamAwayCyan.copy(alpha = 0.85f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("player_quality_popup_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = "الجودات الفعلية للبث",
                                    tint = if (isQualityPopupOpen) Color.White else FoulTeamAwayCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isLowDataMode) {
                                        "الجودات (نت ضعيف)"
                                    } else {
                                        "الجودات ($realActiveResolutionBadge)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // TOD-Style Dedicated Servers Popup Trigger in Top Bar (For Live Channels)
                        if (playbackRequest.isLiveChannel && playbackRequest.servers.isNotEmpty()) {
                            Surface(
                                color = if (isServersPopupOpen) FoulPurpleBright else FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        scaleOnFocus = 1.06f,
                                        onClick = {
                                            isQualityPopupOpen = false
                                            isAudioSubsPopupOpen = false
                                            isServersPopupOpen = !isServersPopupOpen
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isServersPopupOpen) Color.White else FoulPurpleBright.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("player_servers_popup_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = "سيرفرات البث",
                                        tint = FoulLiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "السيرفرات (${playbackRequest.servers.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Audio & Subtitle Track Button for Library VOD (Always accessible in Library Player)
                        if (!playbackRequest.isLiveChannel) {
                            Surface(
                                color = if (isAudioSubsPopupOpen) FoulPurpleBright else FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        scaleOnFocus = 1.06f,
                                        onClick = {
                                            isQualityPopupOpen = false
                                            isAudioSubsPopupOpen = !isAudioSubsPopupOpen
                                        }
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .testTag("player_audio_subs_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = "الصوت والترجمة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "الصوت والترجمة",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Playback Speed Pill for Library VOD (0.75x -> 1x -> 1.25x -> 1.5x -> 2x)
                        if (!playbackRequest.isLiveChannel) {
                            Surface(
                                color = FoulCardDark.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        scaleOnFocus = 1.06f,
                                        onClick = {
                                            val nextSpeed = when (playbackSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                2.0f -> 0.75f
                                                else -> 1.0f
                                            }
                                            playbackSpeed = nextSpeed
                                            exoPlayer.playbackParameters = PlaybackParameters(nextSpeed)
                                            toastMessage = "سرعة التشغيل: ${nextSpeed}x"
                                            showAspectToast = true
                                        }
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .testTag("player_speed_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "سرعة التشغيل",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${playbackSpeed}x",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Mute / Unmute Audio Pill (Helpful on TV Box, PC, and Mobile)
                        Surface(
                            color = if (isMuted) FoulLiveRed.copy(alpha = 0.88f) else FoulCardDark.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    scaleOnFocus = 1.06f,
                                    onClick = toggleMute
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .testTag("player_mute_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isMuted) "إلغاء كتم الصوت" else "كتم الصوت",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Aspect Ratio / Screen Fit Toggle Button
                        Surface(
                            color = FoulCardDark.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    scaleOnFocus = 1.06f,
                                    onClick = {
                                        aspectMode = when (aspectMode) {
                                            PlayerAspectMode.FIT -> PlayerAspectMode.ZOOM
                                            PlayerAspectMode.ZOOM -> PlayerAspectMode.FILL
                                            PlayerAspectMode.FILL -> PlayerAspectMode.FIT
                                        }
                                        toastMessage = aspectMode.labelAr
                                        showAspectToast = true
                                    }
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .testTag("player_aspect_ratio_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = when (aspectMode) {
                                        PlayerAspectMode.FIT -> Icons.Default.AspectRatio
                                        PlayerAspectMode.ZOOM -> Icons.Default.Fullscreen
                                        PlayerAspectMode.FILL -> Icons.Default.FullscreenExit
                                    },
                                    contentDescription = "تغيير أبعاد الشاشة",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = aspectMode.labelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Screen Lock Button (Prevents accidental touches)
                        Surface(
                            color = FoulCardDark.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    scaleOnFocus = 1.06f,
                                    onClick = {
                                        isQualityPopupOpen = false
                                        isAudioSubsPopupOpen = false
                                        isServersPopupOpen = false
                                        isScreenLocked = true
                                        toastMessage = "تم قفل أزرار المشغل"
                                        showAspectToast = true
                                    }
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .testTag("player_lock_screen_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "قفل الشاشة",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // ==================== CENTER TRANSPORT CONTROLS (LTR ORDER - ALWAYS FOCUSABLE ON REMOTE & MOUSE) ====================
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(34.dp)
                    ) {
                        if (!playbackRequest.isLiveChannel) {
                            // LEFT: -10s Rewind Button (Glassmorphic Cinema Pill)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(FoulBottomBarBlack.copy(alpha = 0.82f))
                                    .tvFocusableClickable(
                                        shape = CircleShape,
                                        scaleOnFocus = 1.08f,
                                        onClick = { seekByDeltaMs(-10_000L) }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.45f), FoulPurpleBright)
                                        ),
                                        shape = CircleShape
                                    )
                                    .testTag("player_replay_10_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "تأخير 10 ثواني",
                                    tint = Color.White,
                                    modifier = Modifier.size(31.dp)
                                )
                            }
                        } else if (guideChannels.size > 1) {
                            // LEFT (Live Channel): Previous Channel Quick Button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(FoulBottomBarBlack.copy(alpha = 0.82f))
                                    .tvFocusableClickable(
                                        shape = CircleShape,
                                        scaleOnFocus = 1.08f,
                                        onClick = { switchRelativeGuideChannel(-1) }
                                    )
                                    .border(
                                        width = 1.4.dp,
                                        color = FoulTeamAwayCyan.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                                    .testTag("player_prev_channel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "القناة السابقة",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // CENTER: Play / Pause Button (Primary D-Pad Focus Target when HUD opens)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(74.dp)
                                .focusRequester(centerPlayPauseFocusRequester)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(FoulPurpleBright, FoulPurpleDeep)
                                    )
                                )
                                .tvFocusableClickable(
                                    shape = CircleShape,
                                    scaleOnFocus = 1.08f,
                                    onClick = {
                                        interactionTick++
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    }
                                )
                                .border(2.dp, Color.White, CircleShape)
                                .testTag("player_center_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        if (!playbackRequest.isLiveChannel) {
                            // RIGHT: +10s Forward Button (Glassmorphic Cinema Pill)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(FoulBottomBarBlack.copy(alpha = 0.82f))
                                    .tvFocusableClickable(
                                        shape = CircleShape,
                                        scaleOnFocus = 1.08f,
                                        onClick = { seekByDeltaMs(10_000L) }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(FoulTeamAwayCyan, Color.White.copy(alpha = 0.45f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .testTag("player_forward_10_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "تقديم 10 ثواني",
                                    tint = Color.White,
                                    modifier = Modifier.size(31.dp)
                                )
                            }
                        } else if (guideChannels.size > 1) {
                            // RIGHT (Live Channel): Next Channel Quick Button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(FoulBottomBarBlack.copy(alpha = 0.82f))
                                    .tvFocusableClickable(
                                        shape = CircleShape,
                                        scaleOnFocus = 1.08f,
                                        onClick = { switchRelativeGuideChannel(1) }
                                    )
                                    .border(
                                        width = 1.4.dp,
                                        color = FoulTeamAwayCyan.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                                    .testTag("player_next_channel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "القناة التالية",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // ==================== BOTTOM BAR ====================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    if (playbackRequest.isLiveChannel) {
                        // ==================== TOD-PRO LIVE BROADCAST DOCK (مشغل الرئيسية الاحترافي بتصميم TOD وتنظيم مثالي للسيرفرات لجميع الأجهزة) ====================
                        val activeServerObj = playbackRequest.servers.getOrNull(currentUrlIndex)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Strip of Live Dock: TOD Live Edge Indicator + Active Server Info + Sync & Quality Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: TOD-Style LIVE Broadcast Status Pill + Active Server Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    // Live Sync Edge Pill ("LIVE • مباشر مستقر")
                                    Surface(
                                        color = FoulLiveRed.copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .tvFocusableClickable(
                                                shape = RoundedCornerShape(10.dp),
                                                scaleOnFocus = 1.06f,
                                                onClick = {
                                                    interactionTick++
                                                    if (exoPlayer.isCurrentMediaItemLive && exoPlayer.duration > 0) {
                                                        exoPlayer.seekToDefaultPosition()
                                                    } else {
                                                        exoPlayer.prepare()
                                                    }
                                                    exoPlayer.playWhenReady = true
                                                    toastMessage = "تمت مزامنة البث المباشر بأقصى سرعة"
                                                    showAspectToast = true
                                                }
                                            )
                                            .testTag("live_sync_edge_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Text(
                                                text = "LIVE • مباشر مستقر",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    if (activeServerObj != null) {
                                        val cleanActiveName = cleanServerDisplayName(activeServerObj.name, currentUrlIndex)
                                        Surface(
                                            color = FoulBottomBarBlack.copy(alpha = 0.78f),
                                            shape = RoundedCornerShape(9.dp),
                                            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sensors,
                                                    contentDescription = null,
                                                    tint = FoulLiveGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = cleanActiveName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right: Split-Screen Channel Browser + Quick Next Server + Real Resolution Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (guideChannels.isNotEmpty()) {
                                        Surface(
                                            color = if (isChannelSplitOpen) {
                                                FoulPurpleBright
                                            } else {
                                                FoulBottomBarBlack.copy(alpha = 0.85f)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isChannelSplitOpen) Color.White else FoulTeamAwayCyan.copy(alpha = 0.8f)
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .tvFocusableClickable(
                                                    shape = RoundedCornerShape(10.dp),
                                                    scaleOnFocus = 1.06f,
                                                    onClick = {
                                                        interactionTick++
                                                        isQualityPopupOpen = false
                                                        isAudioSubsPopupOpen = false
                                                        isServersPopupOpen = false
                                                        isChannelSplitOpen = !isChannelSplitOpen
                                                    }
                                                )
                                                .testTag("live_dock_split_channels_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                                    contentDescription = "قائمة القنوات والتصنيفات",
                                                    tint = if (isChannelSplitOpen) Color.White else FoulTeamAwayCyan,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = if (isChannelSplitOpen) "إخفاء القنوات" else "تصفح القنوات",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }

                                    if (playbackRequest.servers.size > 1) {
                                        Surface(
                                            color = FoulBottomBarBlack.copy(alpha = 0.82f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.75f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .tvFocusableClickable(
                                                    shape = RoundedCornerShape(10.dp),
                                                    scaleOnFocus = 1.06f,
                                                    onClick = {
                                                        interactionTick++
                                                        val nextIdx = (currentUrlIndex + 1) % playbackRequest.servers.size
                                                        selectServerManually(
                                                            nextIdx,
                                                            "تم التبديل إلى: ${cleanServerDisplayName(playbackRequest.servers[nextIdx].name, nextIdx)}"
                                                        )
                                                    }
                                                )
                                                .testTag("live_next_server_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SkipNext,
                                                    contentDescription = "السيرفر التالي",
                                                    tint = FoulTeamAwayCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "السيرفر التالي",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Real Verified Stream Qualities Button in Live Bottom Dock
                                    Surface(
                                        color = if (isQualityPopupOpen) {
                                            FoulPurpleBright
                                        } else {
                                            FoulBottomBarBlack.copy(alpha = 0.85f)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isQualityPopupOpen) Color.White else FoulTeamAwayCyan.copy(alpha = 0.85f)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .tvFocusableClickable(
                                                shape = RoundedCornerShape(10.dp),
                                                scaleOnFocus = 1.06f,
                                                onClick = {
                                                    interactionTick++
                                                    isServersPopupOpen = false
                                                    isAudioSubsPopupOpen = false
                                                    isQualityPopupOpen = !isQualityPopupOpen
                                                }
                                            )
                                            .testTag("live_dock_quality_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = "الجودات الفعلية للبث",
                                                tint = if (isQualityPopupOpen) Color.White else FoulTeamAwayCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                                    "الجودات: $realActiveResolutionBadge (${realDecodedWidth}×${realDecodedHeight})"
                                                } else {
                                                    "الجودات: $realActiveResolutionBadge"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }

                            // TOD-Style Dedicated Full-Width colorful progress indicator line for Live Broadcast
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                FoulLiveRed,
                                                FoulPurpleBright,
                                                FoulTeamAwayCyan,
                                                FoulLiveGreen
                                            )
                                        )
                                    )
                            )

                            // Dedicated Compact Multi-Server Strip (Only server name, compact proportions, no TS/M3U8 tags)
                            if (playbackRequest.servers.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = FoulBottomBarBlack.copy(alpha = 0.82f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.8.dp, FoulDivider)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Dns,
                                                contentDescription = null,
                                                tint = FoulPurpleBright,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "السيرفرات:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        itemsIndexed(
                                            items = playbackRequest.servers,
                                            key = { idx, srv -> "${srv.id}_$idx" }
                                        ) { idx, srv ->
                                            val selected = idx == currentUrlIndex
                                            val cleanName = cleanServerDisplayName(srv.name, idx)
                                            Surface(
                                                color = if (selected) {
                                                    FoulPurpleBright
                                                } else {
                                                    FoulBottomBarBlack.copy(alpha = 0.85f)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .tvFocusableClickable(
                                                        shape = RoundedCornerShape(8.dp),
                                                        scaleOnFocus = 1.06f,
                                                        onClick = {
                                                            interactionTick++
                                                            if (currentUrlIndex != idx || allExtensionsFailed) {
                                                                selectServerManually(idx, "جاري التشغيل على: $cleanName")
                                                            }
                                                        }
                                                    )
                                                    .border(
                                                        width = if (selected) 1.2.dp else 0.8.dp,
                                                        color = if (selected) Color.White else FoulPurpleBright.copy(alpha = 0.45f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .testTag("live_server_pill_$idx")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    // Subtle Active Live Dot
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.5.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (selected) FoulLiveGreen else Color.White.copy(alpha = 0.35f)
                                                            )
                                                    )

                                                    // Strictly Server Name ONLY (Compact, neat size)
                                                    Text(
                                                        text = cleanName,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                                        color = Color.White,
                                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ==================== LEGENDARY LIBRARY VOD CINEMA CONSOLE (من دون المستطيل إلي تحت • شريط ملون وخلفية شفافة بالكامل) ====================
                        val activeDisplayPositionMs = if (isScrubbing) scrubPositionMs else positionMs
                        val safeDurationMs = durationMs.coerceAtLeast(1L)
                        val progressFraction = (activeDisplayPositionMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
                        val bufferedFraction = (bufferedPositionMs.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
                        val remainingMs = (durationMs - activeDisplayPositionMs).coerceAtLeast(0L)
                        var isTimelineFocused by remember { mutableStateOf(false) }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                // Frameless Colorful Timeline Bar ("شريط ملون وخلفية يكون بشكل شفاف ومن دون المستطيل إلي تحت")
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .onFocusChanged { isTimelineFocused = it.isFocused || it.hasFocus }
                                        .onKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyDown) {
                                                when (event.key) {
                                                    Key.DirectionLeft -> {
                                                        seekByDeltaMs(-10_000L)
                                                        true
                                                    }
                                                    Key.DirectionRight -> {
                                                        seekByDeltaMs(10_000L)
                                                        true
                                                    }
                                                    else -> false
                                                }
                                            } else false
                                        }
                                        .focusable(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Background & Buffered Track Layer over Transparent Backdrop
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (isTimelineFocused || isScrubbing) 9.dp else 7.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.22f))
                                            .then(
                                                if (isTimelineFocused) {
                                                    Modifier.border(1.dp, Color.White, RoundedCornerShape(50))
                                                } else Modifier
                                            )
                                    ) {
                                        // Buffered Progress Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(bufferedFraction)
                                                .height(if (isTimelineFocused || isScrubbing) 9.dp else 7.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color.White.copy(alpha = 0.45f))
                                        )
                                        // Vibrant Multi-Color Progress Bar ("شريط ملون")
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progressFraction)
                                                .height(if (isTimelineFocused || isScrubbing) 9.dp else 7.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            FoulPurpleBright,
                                                            Color(0xFFEC4899),
                                                            FoulTeamAwayCyan,
                                                            FoulLiveGreen,
                                                            FoulGoldStar
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    // Interactive Smooth Slider on top
                                    Slider(
                                        value = activeDisplayPositionMs.toFloat().coerceIn(0f, safeDurationMs.toFloat()),
                                        onValueChange = { newVal ->
                                            interactionTick++
                                            isScrubbing = true
                                            scrubPositionMs = newVal.toLong()
                                        },
                                        onValueChangeFinished = {
                                            val targetSeek = scrubPositionMs.coerceIn(0L, safeDurationMs)
                                            exoPlayer.seekTo(targetSeek)
                                            positionMs = targetSeek
                                            isScrubbing = false
                                        },
                                        valueRange = 0f..safeDurationMs.toFloat(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = if (isTimelineFocused || isScrubbing) FoulGoldStar else Color.White,
                                            activeTrackColor = Color.Transparent,
                                            inactiveTrackColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("player_timeline_slider")
                                    )
                                }

                                // Frameless Transparent Bottom Info & Quick Transport Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Transparent),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Quick -10s / +10s Controls with subtle transparent glass
                                        Surface(
                                            color = Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .tvFocusableClickable(
                                                    shape = RoundedCornerShape(8.dp),
                                                    scaleOnFocus = 1.08f,
                                                    onClick = { seekByDeltaMs(-10_000L) }
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Replay10,
                                                    contentDescription = "-10s",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "-10s",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            color = Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .tvFocusableClickable(
                                                    shape = RoundedCornerShape(8.dp),
                                                    scaleOnFocus = 1.08f,
                                                    onClick = { seekByDeltaMs(10_000L) }
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Forward10,
                                                    contentDescription = "+10s",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "+10s",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = formatDurationMs(activeDisplayPositionMs),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isScrubbing) FoulGoldStar else Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "/",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = formatDurationMs(durationMs),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.88f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    // Center Status Text directly over transparent background
                                    Text(
                                        text = if (isScrubbing) {
                                            "الانتقال السريع إلى ${formatDurationMs(scrubPositionMs)}"
                                        } else {
                                            ""
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isScrubbing) FoulGoldStar else Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Right: Remaining Time directly on transparent background
                                    Text(
                                        text = "-${formatDurationMs(remainingMs)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FoulTeamAwayCyan,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. REAL STREAM QUALITIES & NETWORK STABILITY POPUP (زر وعرض الجودات الفعلية الحقيقية للبث + وضع النت الضعيف)
        AnimatedVisibility(
            visible = isQualityPopupOpen && !isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, start = 18.dp, end = 18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulBottomBarBlack.copy(alpha = 0.97f)),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(FoulTeamAwayCyan, FoulPurpleBright, FoulLiveGreen)
                    )
                ),
                modifier = Modifier
                    .widthIn(min = 310.dp, max = 390.dp)
                    .heightIn(max = 390.dp)
                    .testTag("player_quality_popup")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = FoulTeamAwayCyan,
                                modifier = Modifier.size(19.dp)
                            )
                            Column {
                                Text(
                                    text = "الجودات الفعلية للبث",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = buildString {
                                        append("الدقة الحالية: $realActiveResolutionBadge")
                                        if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                            append(" (${realDecodedWidth}×${realDecodedHeight})")
                                        }
                                        if (realStreamFps >= 24f) {
                                            append(" • ${realStreamFps.toInt()}fps")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulLiveGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = FoulCardElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, FoulDivider),
                            modifier = Modifier
                                .focusRequester(qualityPopupCloseFocusRequester)
                                .clip(RoundedCornerShape(8.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(8.dp),
                                    scaleOnFocus = 1.08f,
                                    onClick = { isQualityPopupOpen = false }
                                )
                                .testTag("player_quality_popup_close")
                        ) {
                            Text(
                                text = "إغلاق",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OrderedColorfulDivider()
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item(key = "header_quality_options") {
                            Text(
                                text = "الجودة الفعلية واستقرار الشبكة:",
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTeamAwayCyan,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        // 1. Smart Auto Quality Option
                        item(key = "quality_auto_smart") {
                            val isAutoSelected = selectedQualityTrackId == null && !isLowDataMode
                            Surface(
                                color = if (isAutoSelected) FoulPurpleBright.copy(alpha = 0.25f) else FoulCardDark,
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(
                                    width = if (isAutoSelected) 1.3.dp else 0.8.dp,
                                    color = if (isAutoSelected) FoulPurpleBright else FoulDivider
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(11.dp),
                                        scaleOnFocus = 1.03f,
                                        onClick = {
                                            isLowDataMode = false
                                            selectedQualityTrackId = null
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                .clearVideoSizeConstraints()
                                                .setMaxVideoBitrate(Int.MAX_VALUE)
                                                .build()
                                            isQualityPopupOpen = false
                                            toastMessage = "تم تفعيل الجودة التلقائية الذكية ($realActiveResolutionBadge)"
                                            showAspectToast = true
                                        }
                                    )
                                    .testTag("quality_option_auto")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تلقائي ذكي (موصى به)",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = if (realDecodedWidth > 0 && realDecodedHeight > 0) {
                                                "يبث الآن بدقة حقيقية ${realDecodedWidth}×${realDecodedHeight} ($realActiveResolutionBadge)"
                                            } else {
                                                "ضبط تلقائي لأفضل دقة واستقرار حسب سرعة الإنترنت"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FoulTextSecondary
                                        )
                                    }
                                    if (isAutoSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulLiveGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Weak Internet / Anti-Buffering Mode
                        item(key = "quality_low_data_mode") {
                            val isWeakNetSelected = isLowDataMode && selectedQualityTrackId == null
                            Surface(
                                color = if (isWeakNetSelected) FoulLiveGreen.copy(alpha = 0.22f) else FoulCardDark,
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(
                                    width = if (isWeakNetSelected) 1.3.dp else 0.8.dp,
                                    color = if (isWeakNetSelected) FoulLiveGreen else FoulDivider
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(11.dp),
                                        scaleOnFocus = 1.03f,
                                        onClick = {
                                            isLowDataMode = true
                                            selectedQualityTrackId = null
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                .setMaxVideoSize(1280, 720)
                                                .setMaxVideoBitrate(1_600_000)
                                                .build()
                                            isQualityPopupOpen = false
                                            toastMessage = "تم تفعيل وضع النت الضعيف (فتح سريع ومنع التقطيع)"
                                            showAspectToast = true
                                        }
                                    )
                                    .testTag("quality_option_weak_internet")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "وضع النت الضعيف (منع التقطيع)",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "يقلل استهلاك الإنترنت ويفتح القناة بأقصى سرعة وبدون تقطيع",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FoulLiveGreen
                                        )
                                    }
                                    if (isWeakNetSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulLiveGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Real Embedded Video Quality Tracks discovered from the active HLS/DASH/Stream
                        if (availableQualityTracks.isNotEmpty()) {
                            item(key = "header_embedded_tracks") {
                                Text(
                                    text = "مسارات الجودة الفعلية داخل البث (${availableQualityTracks.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulTeamAwayCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }

                            items(
                                items = availableQualityTracks,
                                key = { "track_${it.id}" }
                            ) { option ->
                                val isSelected = selectedQualityTrackId == option.id
                                Surface(
                                    color = if (isSelected) FoulPurpleBright.copy(alpha = 0.25f) else FoulCardDark,
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.3.dp else 0.8.dp,
                                        color = if (isSelected) FoulTeamAwayCyan else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(11.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(11.dp),
                                            scaleOnFocus = 1.03f,
                                            onClick = {
                                                isLowDataMode = false
                                                selectedQualityTrackId = option.id
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .clearVideoSizeConstraints()
                                                    .setMaxVideoBitrate(Int.MAX_VALUE)
                                                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                    .addOverride(
                                                        TrackSelectionOverride(
                                                            option.trackGroup,
                                                            listOf(option.trackIndex)
                                                        )
                                                    )
                                                    .build()
                                                isQualityPopupOpen = false
                                                toastMessage = "تم تثبيت الجودة الفعلية: ${option.shortBadge} (${option.detailSubtitle})"
                                                showAspectToast = true
                                            }
                                        )
                                        .testTag("quality_track_${option.shortBadge}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.shortBadge,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = option.detailSubtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FoulTextSecondary
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Channel Multi-Quality Servers (e.g., 4K, FHD, HD, SD) if available on this channel
                        if (playbackRequest.isLiveChannel && playbackRequest.servers.size > 1) {
                            item(key = "header_server_qualities") {
                                Text(
                                    text = "جودات وسيرفرات القناة المتوفرة (${playbackRequest.servers.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulGoldStar,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                                )
                            }

                            itemsIndexed(
                                items = playbackRequest.servers,
                                key = { idx, srv -> "q_srv_${srv.id}_$idx" }
                            ) { idx, srv ->
                                val isCurrentServer = idx == currentUrlIndex
                                val cleanSrvName = cleanServerDisplayName(srv.name, idx)
                                Surface(
                                    color = if (isCurrentServer) FoulPurpleBright.copy(alpha = 0.25f) else FoulCardDark,
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(
                                        width = if (isCurrentServer) 1.3.dp else 0.8.dp,
                                        color = if (isCurrentServer) FoulGoldStar else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(11.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(11.dp),
                                            scaleOnFocus = 1.03f,
                                            onClick = {
                                                isQualityPopupOpen = false
                                                if (currentUrlIndex != idx || allExtensionsFailed) {
                                                    selectServerManually(idx, "تم التبديل إلى جودة/سيرفر: $cleanSrvName")
                                                }
                                            }
                                        )
                                        .testTag("quality_server_option_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Dns,
                                                contentDescription = null,
                                                tint = if (isCurrentServer) FoulLiveGreen else FoulPurpleBright,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = cleanSrvName,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                if (isCurrentServer && realDecodedWidth > 0 && realDecodedHeight > 0) {
                                                    Text(
                                                        text = "يعمل الآن بدقة فعلية ${realDecodedWidth}×${realDecodedHeight}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = FoulLiveGreen
                                                    )
                                                }
                                            }
                                        }
                                        if (isCurrentServer) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. COMPACT AUDIO & SUBTITLES SELECTOR POPUP (For Library Movies & Series)
        AnimatedVisibility(
            visible = isAudioSubsPopupOpen && !isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, start = 18.dp, end = 18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulBottomBarBlack.copy(alpha = 0.96f)),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(FoulPurpleBright, FoulTeamAwayCyan)
                    )
                ),
                modifier = Modifier
                    .widthIn(min = 235.dp, max = 290.dp)
                    .heightIn(max = 275.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("player_audio_subs_popup")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = FoulTeamAwayCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "المسارات الصوتية والترجمة",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .focusRequester(audioSubsPopupCloseFocusRequester)
                                .clip(CircleShape)
                                .background(FoulCardDark)
                                .tvFocusableClickable(
                                    shape = CircleShape,
                                    scaleOnFocus = 1.08f,
                                    onClick = { isAudioSubsPopupOpen = false }
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    OrderedColorfulDivider(thickness = 1.dp)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (availableAudioTracks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "الصوت (${availableAudioTracks.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulTeamAwayCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(availableAudioTracks, key = { it.id }) { track ->
                                Surface(
                                    color = if (track.isSelected) FoulPurpleDeep.copy(alpha = 0.85f) else FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (track.isSelected) FoulPurpleBright else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(10.dp),
                                            scaleOnFocus = 1.03f,
                                            onClick = {
                                                val override = TrackSelectionOverride(
                                                    track.trackGroup,
                                                    listOf(track.trackIndex)
                                                )
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                                                    .addOverride(override)
                                                    .build()
                                                isAudioSubsPopupOpen = false
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = track.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (track.isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Surface(
                                    color = FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "المسار الصوتي الأصلي (نشط)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulLiveGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (availableSubtitleTracks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "الترجمة (${availableSubtitleTracks.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulGoldStar,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            item(key = "sub_off") {
                                Surface(
                                    color = if (isSubtitlesDisabled) FoulPurpleDeep.copy(alpha = 0.85f) else FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSubtitlesDisabled) FoulPurpleBright else FoulDivider),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(10.dp),
                                            scaleOnFocus = 1.03f,
                                            onClick = {
                                                isSubtitlesDisabled = true
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                                    .build()
                                                isAudioSubsPopupOpen = false
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "إيقاف الترجمة",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSubtitlesDisabled) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            items(availableSubtitleTracks, key = { it.id }) { subTrack ->
                                val selected = !isSubtitlesDisabled && subTrack.isSelected
                                Surface(
                                    color = if (selected) FoulPurpleDeep.copy(alpha = 0.85f) else FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) FoulTeamAwayCyan else FoulDivider),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(10.dp),
                                            scaleOnFocus = 1.03f,
                                            onClick = {
                                                isSubtitlesDisabled = false
                                                val override = TrackSelectionOverride(
                                                    subTrack.trackGroup,
                                                    listOf(subTrack.trackIndex)
                                                )
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                                    .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                                    .addOverride(override)
                                                    .build()
                                                isAudioSubsPopupOpen = false
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = subTrack.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Surface(
                                    color = FoulCardDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "الترجمة مدمجة أو غير منفصلة في المصدر",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FoulTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. TOD-STYLE MULTI-SERVER SELECTOR POPUP (When user clicks "السيرفرات" in Top Bar)
        AnimatedVisibility(
            visible = isServersPopupOpen && !isScreenLocked && playbackRequest.isLiveChannel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, start = 18.dp, end = 18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulBottomBarBlack.copy(alpha = 0.96f)),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(FoulPurpleBright, FoulLiveGreen)
                    )
                ),
                modifier = Modifier
                    .widthIn(min = 240.dp, max = 300.dp)
                    .heightIn(max = 290.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("player_servers_popup")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = FoulLiveGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "سيرفرات القناة المتاحة (${playbackRequest.servers.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "اختر أي سيرفر للتبديل الفوري بدون خروج",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = FoulTextSecondary
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .focusRequester(serversPopupCloseFocusRequester)
                                .clip(CircleShape)
                                .background(FoulCardDark)
                                .tvFocusableClickable(
                                    shape = CircleShape,
                                    scaleOnFocus = 1.08f,
                                    onClick = { isServersPopupOpen = false }
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    OrderedColorfulDivider(thickness = 1.dp)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(
                            items = playbackRequest.servers,
                            key = { idx, srv -> "popup_${srv.id}_$idx" }
                        ) { idx, srv ->
                            val isSelected = idx == currentUrlIndex
                            val cleanName = cleanServerDisplayName(srv.name, idx)
                            Surface(
                                color = if (isSelected) FoulPurpleDeep.copy(alpha = 0.90f) else FoulCardDark,
                                shape = RoundedCornerShape(9.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) FoulLiveGreen else FoulDivider
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(9.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(9.dp),
                                        scaleOnFocus = 1.03f,
                                        onClick = {
                                            interactionTick++
                                            isServersPopupOpen = false
                                            selectServerManually(idx, "تم التبديل إلى: $cleanName")
                                        }
                                    )
                                    .testTag("popup_server_option_$idx")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) FoulLiveGreen else FoulPurpleBright)
                                        )
                                        Text(
                                            text = cleanName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FoulLiveGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // ==================== HALF 2: 50% SPLIT-SCREEN LIVE CHANNELS & CATEGORIES BROWSER (فتح نص شاشة انقسام) ====================
        if (isSplitViewActive) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(FoulBlackBg)
            ) {
                PlayerHalfScreenChannelBrowser(
                    activeChannelId = playbackRequest.channelId,
                    activeChannelName = playbackRequest.title,
                    activeCategoryId = playbackRequest.activeCategoryId,
                    activeCategoryName = playbackRequest.activeCategoryName,
                    categories = guideCategories,
                    channels = guideChannels,
                    onSelectChannel = { selectedChannel ->
                        interactionTick++
                        onSelectGuideChannel(selectedChannel)
                        toastMessage = "جاري تشغيل: ${selectedChannel.name}"
                        showAspectToast = true
                    },
                    onCloseSplit = {
                        isChannelSplitOpen = false
                    }
                )
            }
        }
    }
}

/**
 * Half-Screen Split-View Channel & Category Browser inside the Video Player:
 * - Opens alongside the live video (50% video / 50% channel browser)
 * - Automatically selects, highlights, and scrolls to the currently active Category AND currently playing Channel
 * - Filters BOTH Categories and Channels when searching
 * - Displays 100% of every Category name and every Channel name in full without any '...' truncation
 */
@Composable
private fun PlayerHalfScreenChannelBrowser(
    activeChannelId: String,
    activeChannelName: String,
    activeCategoryId: String,
    activeCategoryName: String,
    categories: List<PlayerGuideCategory>,
    channels: List<PlayerGuideChannel>,
    onSelectChannel: (PlayerGuideChannel) -> Unit,
    onCloseSplit: () -> Unit
) {
    val context = LocalContext.current

    // Determine the real category of the currently playing channel so it's always highlighted accurately
    val resolvedPlayingCategoryId = remember(activeChannelId, activeCategoryId, channels) {
        channels.firstOrNull { it.id == activeChannelId }?.categoryId
            ?.takeIf { it.isNotBlank() }
            ?: activeCategoryId
    }

    val resolvedPlayingCategoryName = remember(resolvedPlayingCategoryId, activeCategoryName, categories, channels) {
        categories.firstOrNull { it.id == resolvedPlayingCategoryId }?.name
            ?.takeIf { it.isNotBlank() }
            ?: channels.firstOrNull { it.id == activeChannelId }?.categoryName?.takeIf { it.isNotBlank() }
            ?: activeCategoryName.ifBlank { "جميع القنوات" }
    }

    // Selected category tab inside the split-screen browser (defaults to the category the user is currently in!)
    var selectedBrowserCategoryId by remember(resolvedPlayingCategoryId) {
        mutableStateOf(
            if (resolvedPlayingCategoryId.isNotBlank() && categories.any { it.id == resolvedPlayingCategoryId }) {
                resolvedPlayingCategoryId
            } else {
                "ALL"
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val trimmedQuery = remember(searchQuery) { searchQuery.trim() }

    // 1. Filter channels by search query first so we can also filter categories by search
    val searchMatchingChannels = remember(channels, trimmedQuery) {
        if (trimmedQuery.isBlank()) {
            channels
        } else {
            channels.filter { ch ->
                ch.name.contains(trimmedQuery, ignoreCase = true) ||
                    ch.categoryName.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    // 2. Filter categories according to search query as well (تصفية التصنيف أيضاً عند البحث)
    val displayedCategories = remember(categories, searchMatchingChannels, trimmedQuery) {
        val countsByCat = searchMatchingChannels.groupingBy { it.categoryId }.eachCount()
        if (trimmedQuery.isBlank()) {
            categories.map { cat ->
                cat.copy(channelCount = countsByCat[cat.id] ?: cat.channelCount)
            }
        } else {
            categories.mapNotNull { cat ->
                val matchingCount = countsByCat[cat.id] ?: 0
                val catNameMatches = cat.name.contains(trimmedQuery, ignoreCase = true)
                if (matchingCount > 0 || catNameMatches) {
                    cat.copy(channelCount = matchingCount)
                } else {
                    null
                }
            }
        }
    }

    val activeCategoryStillVisible = remember(displayedCategories, selectedBrowserCategoryId) {
        selectedBrowserCategoryId == "ALL" || displayedCategories.any { it.id == selectedBrowserCategoryId }
    }
    val effectiveCategoryId = if (activeCategoryStillVisible) selectedBrowserCategoryId else "ALL"

    // 3. Final displayed channels for the selected category + search
    val displayedChannels = remember(searchMatchingChannels, effectiveCategoryId) {
        if (effectiveCategoryId == "ALL") {
            searchMatchingChannels
        } else {
            searchMatchingChannels.filter { it.categoryId == effectiveCategoryId }
        }
    }

    val categoryListState = rememberLazyListState()
    val channelListState = rememberLazyListState()
    val splitCloseFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(80L)
        runCatching {
            splitCloseFocusRequester.requestFocus()
        }
    }

    // Auto-scroll categories strip to the currently active category
    LaunchedEffect(effectiveCategoryId, displayedCategories.size) {
        if (effectiveCategoryId == "ALL") {
            categoryListState.scrollToItem(0)
        } else {
            val catIndex = displayedCategories.indexOfFirst { it.id == effectiveCategoryId }
            if (catIndex >= 0) {
                // +1 because item 0 is "الكل"
                categoryListState.animateScrollToItem((catIndex + 1).coerceAtLeast(0))
            }
        }
    }

    // Auto-scroll channels list to the currently playing channel
    LaunchedEffect(activeChannelId, effectiveCategoryId, displayedChannels.size) {
        val playingIdx = displayedChannels.indexOfFirst { it.id == activeChannelId }
        if (playingIdx >= 0) {
            channelListState.scrollToItem((playingIdx - 1).coerceAtLeast(0))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("player_half_screen_channel_browser")
    ) {
        // Vertical glowing divider between the video half and the channel browser half
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FoulPurpleBright,
                            FoulTeamAwayCyan,
                            FoulLiveGreen,
                            FoulPurpleBright
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FoulPurpleDeep.copy(alpha = 0.28f),
                            FoulBlackBg,
                            FoulBlackBg
                        )
                    )
                )
        ) {
            // ==================== 1. SPLIT BROWSER HEADER & ACTIVE LOCATION SUMMARY ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FoulBottomBarBlack)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = FoulPurplePrimary.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(9.dp),
                            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.7f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "دليل القنوات والتصنيفات المباشر",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "التصنيف الحالي: $resolvedPlayingCategoryName",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = FoulTeamAwayCyan,
                                fontWeight = FontWeight.Bold,
                                softWrap = true
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Search toggle button inside split browser
                        Surface(
                            color = if (isSearchExpanded || searchQuery.isNotBlank()) {
                                FoulPurplePrimary
                            } else {
                                FoulCardDark
                            },
                            shape = RoundedCornerShape(9.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSearchExpanded || searchQuery.isNotBlank()) FoulPurpleBright else FoulDivider
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(9.dp),
                                    scaleOnFocus = 1.05f,
                                    onClick = {
                                        isSearchExpanded = !isSearchExpanded
                                        if (!isSearchExpanded) searchQuery = ""
                                    }
                                )
                                .testTag("split_browser_search_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isSearchExpanded) "إغلاق" else "بحث",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Close split screen button
                        Surface(
                            color = FoulCardDark,
                            shape = RoundedCornerShape(9.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .focusRequester(splitCloseFocusRequester)
                                .clip(RoundedCornerShape(9.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(9.dp),
                                    scaleOnFocus = 1.05f,
                                    onClick = onCloseSplit
                                )
                                .testTag("split_browser_close_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق الانقسام",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "إغلاق",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Active Playing Channel Status Banner (يعرض التصنيف والقناة الداخل عليها حالياً بشكل كامل ومرتب)
                Surface(
                    color = FoulCardDark.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FoulLiveGreen.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(FoulLiveGreen)
                            )
                            Text(
                                text = "تتابع الآن: $activeChannelName",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.2.sp,
                                    lineHeight = 14.5.sp
                                ),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                softWrap = true
                            )
                        }

                        if (resolvedPlayingCategoryId.isNotBlank() && effectiveCategoryId != resolvedPlayingCategoryId) {
                            Surface(
                                color = FoulPurpleDeep,
                                shape = RoundedCornerShape(7.dp),
                                border = BorderStroke(0.8.dp, FoulPurpleBright),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .tvFocusableClickable(
                                        shape = RoundedCornerShape(7.dp),
                                        scaleOnFocus = 1.04f,
                                        onClick = {
                                            selectedBrowserCategoryId = resolvedPlayingCategoryId
                                        }
                                    )
                            ) {
                                Text(
                                    text = "تصنيف القناة",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Search Input Field inside Split Browser (filters both Categories and Channels)
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_browser_search_input"),
                        placeholder = {
                            Text(
                                text = "ابحث عن قناة أو تصنيف...",
                                color = FoulTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = FoulPurpleBright,
                                modifier = Modifier.size(17.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FoulCardDark,
                            unfocusedContainerColor = FoulCardDark,
                            focusedBorderColor = FoulPurpleBright,
                            unfocusedBorderColor = FoulDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = FoulPurpleBright
                        )
                    )
                }
            }

            OrderedColorfulDivider(thickness = 1.dp)

            // ==================== 2. CATEGORIES HORIZONTAL STRIP (كامل الاسم وتحديد التصنيف الداخل عليه) ====================
            if (categories.isNotEmpty()) {
                LazyRow(
                    state = categoryListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FoulBottomBarBlack.copy(alpha = 0.7f))
                        .padding(vertical = 8.dp)
                        .testTag("split_browser_categories_row"),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item(key = "split_cat_ALL") {
                        SplitBrowserCategoryChip(
                            title = "الكل",
                            count = searchMatchingChannels.size,
                            isSelected = effectiveCategoryId == "ALL",
                            isCurrentPlayingCategory = false,
                            onClick = { selectedBrowserCategoryId = "ALL" },
                            testTag = "split_cat_chip_all"
                        )
                    }

                    items(
                        items = displayedCategories,
                        key = { "split_cat_${it.id}" }
                    ) { category ->
                        val isPlayingCat = category.id == resolvedPlayingCategoryId
                        SplitBrowserCategoryChip(
                            title = category.name,
                            count = category.channelCount,
                            isSelected = effectiveCategoryId == category.id,
                            isCurrentPlayingCategory = isPlayingCat,
                            onClick = { selectedBrowserCategoryId = category.id },
                            testTag = "split_cat_chip_${category.id}"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.8.dp)
                        .background(FoulDivider.copy(alpha = 0.6f))
                )
            }

            // ==================== 3. CHANNELS LIST (كل الاسم يظهر بالكامل وبترتيب أنيق وتحديد القناة النشطة) ====================
            if (displayedChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد قنوات مطابقة للبحث في هذا التصنيف",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FoulTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = channelListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("split_browser_channels_list"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    itemsIndexed(
                        items = displayedChannels,
                        key = { idx, ch -> "split_ch_${ch.id}_$idx" }
                    ) { idx, channel ->
                        val isCurrentlyPlaying = channel.id == activeChannelId
                        var logoLoadFailed by remember(channel.logoUrl) { mutableStateOf(false) }

                        Card(
                            shape = RoundedCornerShape(13.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentlyPlaying) {
                                    FoulPurpleDeep.copy(alpha = 0.92f)
                                } else {
                                    FoulCardDark
                                }
                            ),
                            border = BorderStroke(
                                width = if (isCurrentlyPlaying) 1.5.dp else 0.9.dp,
                                color = if (isCurrentlyPlaying) {
                                    FoulLiveGreen
                                } else {
                                    FoulPurpleBright.copy(alpha = 0.25f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(13.dp),
                                    scaleOnFocus = 1.02f,
                                    onClick = { onSelectChannel(channel) }
                                )
                                .testTag("split_channel_item_${channel.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Channel Index Pill
                                Surface(
                                    color = if (isCurrentlyPlaying) {
                                        FoulLiveGreen.copy(alpha = 0.22f)
                                    } else {
                                        FoulBottomBarBlack
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isCurrentlyPlaying) FoulLiveGreen else FoulDivider
                                    )
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = if (isCurrentlyPlaying) FoulLiveGreen else FoulTextSecondary,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }

                                // Channel Logo Badge with Fallback LiveTv Icon
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .border(
                                            width = 1.1.dp,
                                            color = if (isCurrentlyPlaying) {
                                                FoulLiveGreen
                                            } else {
                                                FoulPurpleBright.copy(alpha = 0.45f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (channel.logoUrl.isNotBlank() && !logoLoadFailed) {
                                        AsyncImage(
                                            model = remember(channel.logoUrl, context) {
                                                ImageRequest.Builder(context)
                                                    .data(channel.logoUrl)
                                                    .crossfade(false)
                                                    .size(120, 120)
                                                    .build()
                                            },
                                            contentDescription = channel.name,
                                            contentScale = ContentScale.Fit,
                                            onError = { logoLoadFailed = true },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = channel.name,
                                            tint = if (isCurrentlyPlaying) FoulLiveGreen else FoulPurpleBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Full Untruncated Channel Name & Category Name
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 12.8.sp,
                                            lineHeight = 16.5.sp
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        softWrap = true
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = channel.categoryName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.2.sp),
                                            color = if (isCurrentlyPlaying) {
                                                FoulTeamAwayCyan
                                            } else {
                                                FoulTextSecondary
                                            },
                                            fontWeight = FontWeight.SemiBold,
                                            softWrap = true
                                        )
                                    }
                                }

                                // Active Playing Badge or Play Icon
                                if (isCurrentlyPlaying) {
                                    Surface(
                                        color = FoulLiveGreen.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, FoulLiveGreen)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(FoulLiveGreen)
                                            )
                                            Text(
                                                text = "نشط الآن",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = FoulLiveGreen,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = FoulBottomBarBlack,
                                        shape = CircleShape,
                                        border = BorderStroke(0.8.dp, FoulPurpleBright.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "تشغيل القناة",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitBrowserCategoryChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    isCurrentPlayingCategory: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = when {
            isSelected -> FoulPurplePrimary
            isCurrentPlayingCategory -> FoulPurpleDeep.copy(alpha = 0.85f)
            else -> FoulCardDark
        },
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(
            width = if (isSelected || isCurrentPlayingCategory) 1.2.dp else 0.8.dp,
            color = when {
                isSelected -> FoulPurpleBright
                isCurrentPlayingCategory -> FoulLiveGreen
                else -> FoulDivider
            }
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(11.dp),
                scaleOnFocus = 1.04f,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isCurrentPlayingCategory) {
                Box(
                    modifier = Modifier
                        .size(6.5.dp)
                        .clip(CircleShape)
                        .background(FoulLiveGreen)
                )
            } else if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            // Full Category Name without any '...' truncation
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.2.sp),
                color = Color.White,
                fontWeight = if (isSelected || isCurrentPlayingCategory) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.SemiBold
                },
                softWrap = false
            )

            Surface(
                color = if (isSelected) {
                    Color.Black.copy(alpha = 0.35f)
                } else {
                    FoulBottomBarBlack
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = if (isSelected) FoulGoldStar else FoulTextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                )
            }
        }
    }
}

/**
 * Strips technical stream extension labels (like TS, M3U8, HLS) so only the clean server name is displayed.
 */
private fun cleanServerDisplayName(rawName: String, fallbackIndex: Int): String {
    val cleaned = rawName
        .replace(Regex("\\b(TS|M3U8|HLS|MPEGTS)\\b", RegexOption.IGNORE_CASE), "")
        .replace(Regex("[•|()\\-]+\\s*$"), "")
        .replace(Regex("^\\s*[•|()\\-]+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    return cleaned.ifBlank { "سيرفر ${fallbackIndex + 1}" }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
