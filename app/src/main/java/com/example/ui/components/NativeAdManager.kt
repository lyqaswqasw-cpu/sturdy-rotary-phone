package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button as AndroidButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import coil.Coil
import coil.request.ImageRequest
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Current loading lifecycle state of the Google AdMob Native Advanced Ad.
 */
enum class NativeAdLoadStatus {
    IDLE,
    LOADING,
    LOADED,
    FAILED
}

/**
 * Real-time diagnostic snapshot of `NativeAdManager` so that during development and testing,
 * if a `NativeAd` is not yet loaded when the user clicks a channel, movie, or series episode,
 * the transitional screen never silently skips the ad and instead shows the exact reason.
 */
data class NativeAdDiagnosticState(
    val status: NativeAdLoadStatus = NativeAdLoadStatus.IDLE,
    val adUnitId: String = NativeAdManager.TEST_NATIVE_AD_UNIT_ID,
    val isSdkInitialized: Boolean = false,
    val loadAdCalled: Boolean = false,
    val requestCount: Int = 0,
    val lastErrorCode: Int? = null,
    val lastErrorMessage: String? = null,
    val lastErrorDomain: String? = null,
    val loadedHeadline: String? = null,
    val loadedAdvertiser: String? = null,
    val diagnosticReasonAr: String = "في انتظار بدء طلب الإعلان الأول..."
)

/**
 * Independent Google AdMob Native Advanced Ad Manager for FOUL TV.
 *
 * Official AdMob Identifiers:
 * - App ID: `ca-app-pub-8077917786836804~5000681161`
 * - Production Native Ad Unit ID: `ca-app-pub-8077917786836804/1849397509`
 * - Official Google Test Native Ad Unit ID (active during development/testing):
 *   `ca-app-pub-3940256099942544/2247696110`
 *
 * Required Logcat Markers (Tag: `FoulTvNativeAd`):
 * - `NATIVE_AD_REQUESTED`
 * - `NATIVE_AD_LOADED`
 * - `NATIVE_AD_FAILED`
 * - `NATIVE_AD_SCREEN_SHOWN`
 * - `NATIVE_AD_CLOSED`
 * - `CONTENT_PLAYBACK_STARTED`
 */
class NativeAdManager(
    context: Context,
    private val useProductionAdUnit: Boolean = false,
    val isDiagnosticTestMode: Boolean = !useProductionAdUnit
) {
    companion object {
        const val TAG = "FoulTvNativeAd"

        /** User's Official Production Native Advanced Ad Unit ID */
        const val PRODUCTION_NATIVE_AD_UNIT_ID = "ca-app-pub-8077917786836804/1849397509"

        /** Google's Official Test Native Advanced Ad Unit ID for development & functional testing */
        const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

        /**
         * Default minimum time interval between two transitional Native Ads (30 seconds).
         * Prevents showing an ad on every single back-to-back click.
         */
        const val DEFAULT_COOLDOWN_INTERVAL_MS: Long = 30_000L

        fun explainAdErrorCodeAr(code: Int): String {
            return when (code) {
                AdRequest.ERROR_CODE_INTERNAL_ERROR ->
                    "خطأ داخلي في Google Mobile Ads SDK (ERROR_CODE_INTERNAL_ERROR = 0)"
                AdRequest.ERROR_CODE_INVALID_REQUEST ->
                    "طلب إعلان غير صالح، تحقق من معرف الوحدة الإعلانية (ERROR_CODE_INVALID_REQUEST = 1)"
                AdRequest.ERROR_CODE_NETWORK_ERROR ->
                    "تعذر الاتصال بخادم AdMob عبر الشبكة أو خدمات Google Play (ERROR_CODE_NETWORK_ERROR = 2)"
                AdRequest.ERROR_CODE_NO_FILL ->
                    "لا يتوفر مخزون إعلاني حالياً من خادم AdMob لهذه الوحدة (ERROR_CODE_NO_FILL = 3)"
                else ->
                    "فشل تحميل الإعلان من خادم AdMob (Error Code = $code)"
            }
        }
    }

    private val appContext: Context = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val requestCounter = AtomicInteger(0)

    /** Adjustable cooldown interval between consecutive Native Ad displays */
    @Volatile
    var minIntervalBetweenAdsMs: Long = DEFAULT_COOLDOWN_INTERVAL_MS

    /** Master switch controlled from Developer Settings to enable or completely disable ads */
    @Volatile
    var isAdsEnabled: Boolean = AdMobConfig.isAdsEnabledInPrefs(appContext)
        private set

    @Volatile
    private var lastAdShownTimestampMs: Long = 0L

    @Volatile
    private var isLoadingAd: Boolean = false

    @Volatile
    private var retryAttemptCount: Int = 0

    @Volatile
    private var cachedReadyAd: NativeAd? = null

    private val _activeTransitionAd = MutableStateFlow<NativeAd?>(null)
    val activeTransitionAd: StateFlow<NativeAd?> = _activeTransitionAd.asStateFlow()

    private val _isTransitionSessionActive = MutableStateFlow(false)
    val isTransitionSessionActive: StateFlow<Boolean> = _isTransitionSessionActive.asStateFlow()

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    val resolvedAdUnitId: String
        get() = if (useProductionAdUnit) {
            PRODUCTION_NATIVE_AD_UNIT_ID
        } else {
            TEST_NATIVE_AD_UNIT_ID
        }

    private val _diagnosticState = MutableStateFlow(
        NativeAdDiagnosticState(
            status = NativeAdLoadStatus.IDLE,
            adUnitId = resolvedAdUnitId
        )
    )
    val diagnosticState: StateFlow<NativeAdDiagnosticState> = _diagnosticState.asStateFlow()

    init {
        if (isAdsEnabled) {
            // 1. Schedule Google Mobile Ads SDK initialization after first UI frame
            AdMobConfig.initializeOnce(appContext)

            // 2. Mark startup preload state immediately; actual AdLoader runs once SDK initialization completes
            preloadNextAd(reason = "APP_STARTUP")
        }

        // 3. Observe SDK initialization completion and trigger the actual AdLoader request after UI startup
        managerScope.launch {
            AdMobConfig.isInitialized.collect { initialized ->
                _diagnosticState.update { it.copy(isSdkInitialized = initialized) }
                if (isAdsEnabled && initialized && cachedReadyAd == null && !isLoadingAd && _activeTransitionAd.value == null) {
                    mainHandler.post {
                        preloadNextAd(reason = "SDK_INITIALIZED")
                    }
                }
            }
        }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    /**
     * Updates the minimum time interval (in milliseconds) between consecutive Native Ads.
     */
    fun setAdCooldownIntervalMs(intervalMs: Long) {
        minIntervalBetweenAdsMs = intervalMs.coerceAtLeast(0L)
    }

    /**
     * Resets the cooldown timer (used in unit/functional tests to test consecutive content types).
     */
    fun resetCooldownForTesting() {
        lastAdShownTimestampMs = 0L
    }

    /**
     * Enables or completely disables Native Ads across the app from Developer Settings.
     * When disabled (`enabled == false`), any active or cached ad is immediately destroyed and cleared.
     * When re-enabled (`enabled == true`), initializes AdMob if needed and preloads a fresh Native Ad immediately.
     */
    fun setAdsEnabled(enabled: Boolean) {
        val changed = (isAdsEnabled != enabled)
        isAdsEnabled = enabled
        AdMobConfig.setAdsEnabledState(appContext, enabled)
        if (!enabled) {
            releaseAll()
        } else if (changed || (cachedReadyAd == null && !isLoadingAd)) {
            AdMobConfig.initializeOnce(appContext, force = true)
            preloadNextAd(reason = "ADS_ENABLED_BY_DEVELOPER")
        }
    }

    /**
     * Preloads a Native Advanced Ad in the background if none is currently cached or loading.
     * Uses static image & text assets only (no auto-playing video).
     */
    fun preloadNextAd(
        overrideAdUnitId: String? = null,
        forceReload: Boolean = false,
        reason: String = "PRELOAD"
    ) {
        if (!isAdsEnabled) return
        runOnMainThread {
            if (!isAdsEnabled) return@runOnMainThread
            if (!forceReload && (cachedReadyAd != null || isLoadingAd)) {
                return@runOnMainThread
            }
            val unitIdToLoad = overrideAdUnitId ?: resolvedAdUnitId
            val currentReqNum = requestCounter.incrementAndGet()
            val sdkReady = AdMobConfig.isInitialized.value

            _diagnosticState.update {
                it.copy(
                    status = NativeAdLoadStatus.LOADING,
                    adUnitId = unitIdToLoad,
                    isSdkInitialized = sdkReady,
                    loadAdCalled = true,
                    requestCount = currentReqNum,
                    diagnosticReasonAr = "جاري تحميل إعلان Native من Google AdMob (الطلب #$currentReqNum)..."
                )
            }

            // Never construct AdLoader or touch WebView before MobileAds.initialize completes after first UI frame
            if (!sdkReady) {
                AdMobConfig.initializeOnce(appContext, force = true)
                return@runOnMainThread
            }

            isLoadingAd = true
            Log.d(
                TAG,
                "NATIVE_AD_REQUESTED | unitId=$unitIdToLoad | reason=$reason | requestCount=$currentReqNum"
            )

            mainHandler.post {
                if (!isAdsEnabled) {
                    isLoadingAd = false
                    return@post
                }
                try {
                    val nativeAdOptions = NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .setRequestMultipleImages(false)
                        .setReturnUrlsForImageAssets(false)
                        .build()

                    val adLoader = AdLoader.Builder(appContext, unitIdToLoad)
                        .forNativeAd { loadedAd ->
                        isLoadingAd = false
                        retryAttemptCount = 0

                        if (!isAdsEnabled) {
                            try {
                                loadedAd.destroy()
                            } catch (_: Throwable) {
                            }
                            return@forNativeAd
                        }

                        Log.d(
                            TAG,
                            "NATIVE_AD_LOADED | unitId=$unitIdToLoad | headline=${loadedAd.headline} | advertiser=${loadedAd.advertiser}"
                        )

                        _diagnosticState.update {
                            it.copy(
                                status = NativeAdLoadStatus.LOADED,
                                adUnitId = unitIdToLoad,
                                lastErrorCode = null,
                                lastErrorMessage = null,
                                lastErrorDomain = null,
                                loadedHeadline = loadedAd.headline,
                                loadedAdvertiser = loadedAd.advertiser,
                                diagnosticReasonAr = "تم تحميل إعلان Native بنجاح (${loadedAd.headline ?: "إعلان جاهز"})"
                            )
                        }

                        // If the user is already on the transitional screen waiting in diagnostic/loading state,
                        // immediately display this loaded NativeAd on the open screen!
                        if (_isTransitionSessionActive.value && _activeTransitionAd.value == null) {
                            _activeTransitionAd.value = loadedAd
                            _isAdReady.value = false
                            lastAdShownTimestampMs = System.currentTimeMillis()
                            Log.d(
                                TAG,
                                "NATIVE_AD_SCREEN_SHOWN | state=PROMOTED_LIVE_ON_SCREEN | headline=${loadedAd.headline}"
                            )
                        } else {
                            cachedReadyAd?.destroy()
                            cachedReadyAd = loadedAd
                            _isAdReady.value = true
                        }
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isLoadingAd = false
                            _isAdReady.value = cachedReadyAd != null
                            val explanationAr = explainAdErrorCodeAr(loadAdError.code)

                            Log.e(
                                TAG,
                                "NATIVE_AD_FAILED | unitId=$unitIdToLoad | errorCode=${loadAdError.code} | message=${loadAdError.message} | domain=${loadAdError.domain}"
                            )

                            _diagnosticState.update {
                                it.copy(
                                    status = if (cachedReadyAd != null) NativeAdLoadStatus.LOADED else NativeAdLoadStatus.FAILED,
                                    adUnitId = unitIdToLoad,
                                    lastErrorCode = loadAdError.code,
                                    lastErrorMessage = loadAdError.message,
                                    lastErrorDomain = loadAdError.domain,
                                    diagnosticReasonAr = "$explanationAr — ${loadAdError.message}"
                                )
                            }

                            if (retryAttemptCount < 2) {
                                retryAttemptCount++
                                mainHandler.postDelayed({
                                    if (cachedReadyAd == null && !isLoadingAd) {
                                        preloadNextAd(reason = "AUTO_RETRY_$retryAttemptCount")
                                    }
                                }, 2500L)
                            }
                        }
                    })
                    .withNativeAdOptions(nativeAdOptions)
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())
            } catch (t: Throwable) {
                isLoadingAd = false
                Log.e(
                    TAG,
                    "NATIVE_AD_FAILED | unitId=$unitIdToLoad | errorCode=-1 | message=${t.message ?: t.javaClass.simpleName}"
                )
                _diagnosticState.update {
                    it.copy(
                        status = NativeAdLoadStatus.FAILED,
                        adUnitId = unitIdToLoad,
                        lastErrorCode = -1,
                        lastErrorMessage = t.message ?: t.javaClass.simpleName,
                        lastErrorDomain = "AndroidException",
                        diagnosticReasonAr = "استثناء أثناء استدعاء loadAd: ${t.message ?: t.javaClass.simpleName}"
                    )
                }
            }
            }
        }
    }

    /**
     * Manual retry button handler from the diagnostic transitional screen.
     */
    fun retryLoadAdNow() {
        retryAttemptCount = 0
        preloadNextAd(forceReload = true, reason = "USER_DIAGNOSTIC_RETRY")
    }

    /**
     * Checks whether the cooldown period has elapsed so a transitional Native Ad is allowed to be shown.
     */
    fun isCooldownElapsed(nowMs: Long = System.currentTimeMillis()): Boolean {
        return lastAdShownTimestampMs == 0L || (nowMs - lastAdShownTimestampMs) >= minIntervalBetweenAdsMs
    }

    /**
     * Synchronous check for an already-loaded `NativeAd` in memory.
     * Returns `true` ONLY when a real `NativeAd` object is cached and ready in memory.
     */
    fun tryShowReadyAdBeforePlayback(contentTitle: String = ""): Boolean {
        if (!isAdsEnabled || _isTransitionSessionActive.value) {
            return false
        }
        val now = System.currentTimeMillis()
        if (!isCooldownElapsed(now)) {
            preloadNextAd(reason = "COOLDOWN_CHECK")
            return false
        }

        val readyAd = cachedReadyAd
        if (readyAd == null) {
            preloadNextAd(reason = "TRY_SHOW_CACHE_EMPTY")
            return false
        }

        cachedReadyAd = null
        _isAdReady.value = false
        lastAdShownTimestampMs = now
        _activeTransitionAd.value = readyAd
        _isTransitionSessionActive.value = true
        Log.d(
            TAG,
            "NATIVE_AD_SCREEN_SHOWN | content=$contentTitle | state=READY_AD | headline=${readyAd.headline}"
        )
        return true
    }

    /**
     * Evaluates whether the transitional Native Ad screen should be shown before starting `contentTitle`:
     *
     * 1. If ads are disabled by the developer (`!isAdsEnabled`), immediately returns `false` so no ad ever appears.
     * 2. Prevents showing more than one ad at the same time (`_isTransitionSessionActive.value == true`).
     * 3. Respects the cooldown interval (`isCooldownElapsed(now)`): does not show an ad on every single click.
     * 4. If a `NativeAd` is preloaded and ready (`cachedReadyAd != null`), activates it and returns `true`
     *    so `NativeAdTransitionScreen` is displayed before playback.
     * 5. If `cachedReadyAd == null`:
     *    - Triggers `preloadNextAd` if not already loading.
     *    - **During testing/development (`isDiagnosticTestMode == true`)**: does NOT silently skip the screen!
     *      Activates the transitional session (`_isTransitionSessionActive = true`) and returns `true` so
     *      `NativeAdTransitionScreen` shows the live diagnostic/loading state (and automatically upgrades in-place
     *      if `onNativeAdLoaded` arrives while open).
     *    - **In production (`isDiagnosticTestMode == false`)**: returns `false` immediately so content starts directly.
     */
    fun beginTransitionBeforePlayback(contentTitle: String): Boolean {
        if (!isAdsEnabled) {
            return false
        }
        if (_isTransitionSessionActive.value) {
            return true
        }

        val now = System.currentTimeMillis()
        if (!isCooldownElapsed(now)) {
            return false
        }

        // Case 1: A preloaded NativeAd is ready in memory
        if (tryShowReadyAdBeforePlayback(contentTitle = contentTitle)) {
            return true
        }

        // Case 2: No NativeAd is in memory yet — ensure a request is running only if in diagnostic test mode
        if (isDiagnosticTestMode) {
            preloadNextAd(reason = "ON_CONTENT_SELECTED")
            lastAdShownTimestampMs = now
            _isTransitionSessionActive.value = true
            val diag = _diagnosticState.value
            Log.d(
                TAG,
                "NATIVE_AD_SCREEN_SHOWN | content=$contentTitle | state=DIAGNOSTIC_${diag.status} | errorCode=${diag.lastErrorCode} | message=${diag.lastErrorMessage}"
            )
            return true
        }
        return false
    }

    /**
     * Dismisses and destroys the currently displayed transitional `NativeAd`
     * and logs `NATIVE_AD_CLOSED`. Next ad preloading is deferred until `closePlayer()`
     * so 100% of CPU and network bandwidth go to the video stream.
     */
    fun dismissActiveAdAndPreloadNext(contentTitle: String = "") {
        val currentAd = _activeTransitionAd.value
        val wasSessionActive = _isTransitionSessionActive.value
        _activeTransitionAd.value = null
        _isTransitionSessionActive.value = false

        if (wasSessionActive || currentAd != null) {
            Log.d(TAG, "NATIVE_AD_CLOSED | content=$contentTitle")
        }

        runOnMainThread {
            try {
                currentAd?.destroy()
            } catch (_: Throwable) {
            }
        }
    }

    /**
     * Logs `CONTENT_PLAYBACK_STARTED` when the selected channel, movie, or episode begins playing.
     */
    fun logContentPlaybackStarted(contentTitle: String, contentSubtitle: String) {
        Log.d(
            TAG,
            "CONTENT_PLAYBACK_STARTED | title=$contentTitle | subtitle=$contentSubtitle"
        )
    }

    /**
     * Releases all cached and active NativeAd instances when ViewModel is cleared.
     */
    fun releaseAll() {
        val active = _activeTransitionAd.value
        val cached = cachedReadyAd
        _activeTransitionAd.value = null
        _isTransitionSessionActive.value = false
        cachedReadyAd = null
        _isAdReady.value = false
        runOnMainThread {
            try {
                active?.destroy()
                cached?.destroy()
            } catch (_: Throwable) {
            }
        }
    }
}

/**
 * Full-Screen Transitional Native Advanced Ad Screen for FOUL TV.
 *
 * Google AdMob Policy & UX Compliance:
 * - Pitch-black & deep royal purple FOUL TV identity.
 * - Independent, prominent Close (`X`) button at the top of the screen outside the `NativeAdView`
 *   so the user can close the ad at any time and immediately start their chosen content.
 * - When `nativeAd != null`, renders the official `com.google.android.gms.ads.nativead.NativeAdView`
 *   via `AndroidView` with properly registered asset views (`headlineView`, `bodyView`, `advertiserView`,
 *   `iconView`, `imageView`, `callToActionView`, `adChoicesView`).
 * - Displays clear `"إعلان"` attribution and official `AdChoices` badge.
 * - Strictly uses static image & text assets only (never uses `MediaView` or plays video ads).
 * - When `nativeAd == null` (in Diagnostic Test Mode when the ad is still loading or failed to load),
 *   displays a clear diagnostic state explaining why the ad has not appeared yet, while keeping the top `X`
 *   button active to start the selected content immediately.
 */
@Composable
fun NativeAdTransitionScreen(
    nativeAd: NativeAd?,
    diagnosticState: NativeAdDiagnosticState,
    pendingContentTitle: String,
    pendingContentSubtitle: String,
    isLiveChannel: Boolean,
    onRetryLoadAd: () -> Unit,
    onCloseAdAndPlayContent: () -> Unit
) {
    // Allow hardware/system back press to close the ad and start the selected content immediately
    BackHandler(onBack = onCloseAdAndPlayContent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FoulBlackBg,
                        FoulPurpleDeep.copy(alpha = 0.38f),
                        FoulBlackBg
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .testTag("native_ad_transition_screen"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==================== 1. TOP INDEPENDENT BAR (OUTSIDE NativeAdView) ====================
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FoulBottomBarBlack)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Selected Content Info Badge (Preserved untouched during the ad)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FoulPurpleDeep)
                            .border(1.dp, FoulPurpleBright.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiveChannel) Icons.Default.LiveTv else Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جاهز للتشغيل بعد إغلاق الإعلان:",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTeamAwayCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pendingContentTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("native_ad_pending_content_title")
                        )
                        if (pendingContentSubtitle.isNotBlank()) {
                            Text(
                                text = pendingContentSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                val closeAdFocusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    delay(80L)
                    runCatching { closeAdFocusRequester.requestFocus() }
                }

                // Independent Clear Close (X) Button (Outside NativeAdView, compliant with Google Policies)
                Surface(
                    color = FoulCardDark,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .focusRequester(closeAdFocusRequester)
                        .clip(RoundedCornerShape(24.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(24.dp),
                            scaleOnFocus = 1.05f,
                            onClick = onCloseAdAndPlayContent
                        )
                        .testTag("native_ad_close_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "إغلاق X",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(FoulPurpleBright),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق الإعلان وتشغيل المحتوى",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            OrderedColorfulDivider(thickness = 1.5.dp)
        }

        // ==================== 2. CENTER NATIVE AD CARD OR DIAGNOSTIC STATE ====================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (nativeAd != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(FoulPurpleBright, FoulPurpleDeep, FoulDivider)
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .testTag("native_ad_card_container")
                ) {
                    // Official SDK NativeAdView with registered Android Views (no overlays on top)
                    AndroidView(
                        factory = { ctx ->
                            createAndBindSdkNativeAdView(ctx, nativeAd)
                        },
                        update = { adView ->
                            bindNativeAdToView(adView, nativeAd)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("native_ad_sdk_view")
                    )
                }
            } else {
                NativeAdDiagnosticCard(
                    diagnosticState = diagnosticState,
                    pendingContentTitle = pendingContentTitle,
                    onRetryLoadAd = onRetryLoadAd,
                    onCloseAdAndPlayContent = onCloseAdAndPlayContent
                )
            }
        }

        // ==================== 3. BOTTOM FOOTER BAR (OUTSIDE NativeAdView) ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulBottomBarBlack)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "إعلان ممول عبر Google AdMob Native Advanced",
                style = MaterialTheme.typography.labelSmall,
                color = FoulTextSecondary
            )
            Text(
                text = "اضغط على (إغلاق X) في الأعلى لبدء المشاهدة فوراً",
                style = MaterialTheme.typography.labelSmall,
                color = FoulGoldStar,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NativeAdDiagnosticCard(
    diagnosticState: NativeAdDiagnosticState,
    pendingContentTitle: String,
    onRetryLoadAd: () -> Unit,
    onCloseAdAndPlayContent: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(FoulPurpleBright, FoulPurpleDeep, FoulDivider)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .testTag("native_ad_diagnostic_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Attribution & Diagnostic Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = FoulPurpleDeep,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "إعلان • AdChoices",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = FoulCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FoulDivider)
                ) {
                    val statusLabel = when (diagnosticState.status) {
                        NativeAdLoadStatus.LOADING -> "جاري التحميل (LOADING)"
                        NativeAdLoadStatus.FAILED -> "فشل التحميل (FAILED)"
                        NativeAdLoadStatus.LOADED -> "جاهز (LOADED)"
                        NativeAdLoadStatus.IDLE -> "قيد التهيئة (IDLE)"
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diagnosticState.status == NativeAdLoadStatus.FAILED) {
                            Color(0xFFFF6B6B)
                        } else {
                            FoulGoldStar
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Status Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (diagnosticState.status == NativeAdLoadStatus.LOADING ||
                    diagnosticState.status == NativeAdLoadStatus.IDLE
                ) {
                    CircularProgressIndicator(
                        color = FoulPurpleBright,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF5252)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (diagnosticState.status == NativeAdLoadStatus.LOADING) {
                            "جاري تحميل إعلان Google AdMob Native..."
                        } else {
                            "حالة تشخيصية: الإعلان غير جاهز حالياً"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = diagnosticState.diagnosticReasonAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )
                }
            }

            HorizontalDivider(color = FoulDivider)

            // Technical Diagnostic Details Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FoulBottomBarBlack)
                    .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FoulTeamAwayCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "تشخيص Google AdMob Native Advanced:",
                        style = MaterialTheme.typography.labelMedium,
                        color = FoulTeamAwayCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                DiagnosticDetailRow(
                    label = "معرف الوحدة (Ad Unit ID):",
                    value = diagnosticState.adUnitId
                )
                DiagnosticDetailRow(
                    label = "تهيئة MobileAds.initialize:",
                    value = if (diagnosticState.isSdkInitialized) "مكتملة بنجاح" else "قيد التهيئة"
                )
                DiagnosticDetailRow(
                    label = "استدعاء AdLoader.loadAd:",
                    value = if (diagnosticState.loadAdCalled) {
                        "تم الاستدعاء فعلياً (عدد الطلبات: ${diagnosticState.requestCount})"
                    } else {
                        "لم يتم الاستدعاء بعد"
                    }
                )
                if (diagnosticState.lastErrorCode != null) {
                    DiagnosticDetailRow(
                        label = "رمز الخطأ (Error Code):",
                        value = "${diagnosticState.lastErrorCode}",
                        highlightError = true
                    )
                }
                if (!diagnosticState.lastErrorMessage.isNullOrBlank()) {
                    DiagnosticDetailRow(
                        label = "رسالة الخطأ (Error Message):",
                        value = diagnosticState.lastErrorMessage,
                        highlightError = true
                    )
                }
                DiagnosticDetailRow(
                    label = "المحتوى المحفوظ للتشغيل:",
                    value = pendingContentTitle,
                    highlightSuccess = true
                )
            }

            // Action Buttons (Retry Load Ad OR Start Selected Content Immediately)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRetryLoadAd,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FoulPurpleBright),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("native_ad_retry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "إعادة طلب الإعلان",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCloseAdAndPlayContent,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("native_ad_diagnostic_continue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "تشغيل المحتوى الآن",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticDetailRow(
    label: String,
    value: String,
    highlightError: Boolean = false,
    highlightSuccess: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FoulTextSecondary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                highlightError -> Color(0xFFFF6B6B)
                highlightSuccess -> FoulLiveGreen
                else -> Color.White
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.58f)
        )
    }
}

/**
 * View holder tags used to bind `NativeAd` assets to real Android `View`s inside `NativeAdView`.
 */
private class NativeAdViewHolder(
    val adAttributionBadge: TextView,
    val adChoicesLabel: TextView,
    val iconImageView: ImageView,
    val headlineTextView: TextView,
    val advertiserTextView: TextView,
    val mainImageView: ImageView,
    val bodyTextView: TextView,
    val ctaButton: AndroidButton
)

private fun Context.dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()
}

/**
 * Constructs a genuine `com.google.android.gms.ads.nativead.NativeAdView` containing native Android
 * views and registers each asset view with `NativeAdView` so the Google Mobile Ads SDK handles
 * impression tracking, AdChoices overlay placement, and advertiser click handling automatically.
 *
 * Strictly uses static `ImageView` and `TextView` / `Button` assets (no `MediaView`) so video ads
 * are never played.
 */
@SuppressLint("SetTextI18n")
private fun createAndBindSdkNativeAdView(
    context: Context,
    nativeAd: NativeAd
): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val rootColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val pad = context.dpToPx(16)
        setPadding(pad, pad, pad, pad)
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                "#1A0B36".toColorInt(),
                "#0D0D15".toColorInt()
            )
        ).apply {
            cornerRadius = context.dpToPx(18).toFloat()
        }
    }

    // 1. Top Attribution Row ("إعلان" badge + Official AdChoices container)
    val attributionRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = context.dpToPx(12)
        }
    }

    val adAttributionBadge = TextView(context).apply {
        text = "إعلان • Ad"
        setTextColor(AndroidColor.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTypeface(typeface, Typeface.BOLD)
        val hPad = context.dpToPx(10)
        val vPad = context.dpToPx(4)
        setPadding(hPad, vPad, hPad, vPad)
        background = GradientDrawable().apply {
            setColor("#7C3AED".toColorInt())
            cornerRadius = context.dpToPx(8).toFloat()
            setStroke(context.dpToPx(1), "#55FFFFFF".toColorInt())
        }
    }

    val attributionSpacer = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
    }

    val adChoicesContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val hPad = context.dpToPx(8)
        val vPad = context.dpToPx(3)
        setPadding(hPad, vPad, hPad, vPad)
        background = GradientDrawable().apply {
            setColor("#181826".toColorInt())
            cornerRadius = context.dpToPx(6).toFloat()
            setStroke(context.dpToPx(1), "#33FFFFFF".toColorInt())
        }
    }

    val adChoicesLabel = TextView(context).apply {
        text = "AdChoices"
        setTextColor("#B8B8D0".toColorInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTypeface(typeface, Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = context.dpToPx(4)
        }
    }

    val sdkAdChoicesView = AdChoicesView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    adChoicesContainer.addView(adChoicesLabel)
    adChoicesContainer.addView(sdkAdChoicesView)

    attributionRow.addView(adAttributionBadge)
    attributionRow.addView(attributionSpacer)
    attributionRow.addView(adChoicesContainer)
    rootColumn.addView(attributionRow)

    // 2. Header Row: Icon + (Headline + Advertiser)
    val headerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = context.dpToPx(12)
        }
    }

    val iconImageView = ImageView(context).apply {
        val iconSize = context.dpToPx(52)
        layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
            marginEnd = context.dpToPx(12)
        }
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = GradientDrawable().apply {
            setColor("#181826".toColorInt())
            cornerRadius = context.dpToPx(12).toFloat()
        }
        clipToOutline = true
    }

    val titleColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    val headlineTextView = TextView(context).apply {
        setTextColor(AndroidColor.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        setTypeface(typeface, Typeface.BOLD)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }

    val advertiserTextView = TextView(context).apply {
        setTextColor("#00E5FF".toColorInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTypeface(typeface, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = context.dpToPx(3)
        }
    }

    titleColumn.addView(headlineTextView)
    titleColumn.addView(advertiserTextView)
    headerRow.addView(iconImageView)
    headerRow.addView(titleColumn)
    rootColumn.addView(headerRow)

    // 3. Static Main Image Asset (No MediaView / no auto-playing video; hidden gracefully if image asset is unavailable)
    val mainImageView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.dpToPx(195)
        ).apply {
            bottomMargin = context.dpToPx(12)
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        adjustViewBounds = true
        background = GradientDrawable().apply {
            setColor("#08080E".toColorInt())
            cornerRadius = context.dpToPx(14).toFloat()
            setStroke(context.dpToPx(1), "#2A2A40".toColorInt())
        }
    }
    rootColumn.addView(mainImageView)

    // 4. Body / Description Text
    val bodyTextView = TextView(context).apply {
        setTextColor("#D8D8E8".toColorInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = context.dpToPx(16)
        }
    }
    rootColumn.addView(bodyTextView)

    // 5. Official Call-To-Action Button (Registered with NativeAdView so SDK handles clicks natively)
    val ctaButton = AndroidButton(context).apply {
        isAllCaps = false
        setTextColor(AndroidColor.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.dpToPx(48)
        )
        background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                "#7C3AED".toColorInt(),
                "#5B21B6".toColorInt()
            )
        ).apply {
            cornerRadius = context.dpToPx(12).toFloat()
            setStroke(context.dpToPx(1), "#88FFFFFF".toColorInt())
        }
    }
    rootColumn.addView(ctaButton)

    nativeAdView.addView(rootColumn)

    // Register views with NativeAdView (Notice: mediaView is intentionally NOT registered so video never plays)
    nativeAdView.adChoicesView = sdkAdChoicesView
    nativeAdView.iconView = iconImageView
    nativeAdView.headlineView = headlineTextView
    nativeAdView.advertiserView = advertiserTextView
    nativeAdView.imageView = mainImageView
    nativeAdView.bodyView = bodyTextView
    nativeAdView.callToActionView = ctaButton

    val holder = NativeAdViewHolder(
        adAttributionBadge = adAttributionBadge,
        adChoicesLabel = adChoicesLabel,
        iconImageView = iconImageView,
        headlineTextView = headlineTextView,
        advertiserTextView = advertiserTextView,
        mainImageView = mainImageView,
        bodyTextView = bodyTextView,
        ctaButton = ctaButton
    )
    nativeAdView.tag = holder

    bindNativeAdToView(nativeAdView, nativeAd)
    return nativeAdView
}

/**
 * Populates the registered views with the `NativeAd` assets and invokes `nativeAdView.setNativeAd(nativeAd)`.
 * Static images and text assets are used exclusively; missing assets are hidden cleanly.
 */
private fun bindNativeAdToView(
    nativeAdView: NativeAdView,
    nativeAd: NativeAd
) {
    val holder = nativeAdView.tag as? NativeAdViewHolder ?: return
    val context = nativeAdView.context

    // 1. Headline (Required asset)
    holder.headlineTextView.text = nativeAd.headline ?: "إعلان مميز"
    holder.headlineTextView.visibility = View.VISIBLE

    // 2. Advertiser (Optional asset)
    val advertiser = nativeAd.advertiser ?: nativeAd.store
    if (!advertiser.isNullOrBlank()) {
        holder.advertiserTextView.text = advertiser
        holder.advertiserTextView.visibility = View.VISIBLE
    } else {
        holder.advertiserTextView.visibility = View.GONE
    }

    // 3. Icon (Optional static image asset)
    val iconAsset = nativeAd.icon
    val iconDrawable = iconAsset?.drawable
    val iconUri = iconAsset?.uri
    if (iconDrawable != null) {
        holder.iconImageView.setImageDrawable(iconDrawable)
        holder.iconImageView.visibility = View.VISIBLE
    } else if (iconUri != null) {
        holder.iconImageView.visibility = View.VISIBLE
        Coil.imageLoader(context).enqueue(
            ImageRequest.Builder(context)
                .data(iconUri)
                .target(holder.iconImageView)
                .build()
        )
    } else {
        holder.iconImageView.visibility = View.GONE
    }

    // 4. Static Main Image Asset (Strictly static image; never uses MediaView or auto-plays video)
    val firstImageAsset = nativeAd.images.firstOrNull()
    val mainImageDrawable = firstImageAsset?.drawable ?: nativeAd.mediaContent?.mainImage
    val mainImageUri = firstImageAsset?.uri
    if (mainImageDrawable != null) {
        holder.mainImageView.setImageDrawable(mainImageDrawable)
        holder.mainImageView.visibility = View.VISIBLE
    } else if (mainImageUri != null) {
        holder.mainImageView.visibility = View.VISIBLE
        Coil.imageLoader(context).enqueue(
            ImageRequest.Builder(context)
                .data(mainImageUri)
                .target(holder.mainImageView)
                .build()
        )
    } else {
        holder.mainImageView.setImageDrawable(null)
        holder.mainImageView.visibility = View.GONE
    }

    // 5. Body / Description (Optional asset)
    val body = nativeAd.body
    if (!body.isNullOrBlank()) {
        holder.bodyTextView.text = body
        holder.bodyTextView.visibility = View.VISIBLE
    } else {
        holder.bodyTextView.visibility = View.GONE
    }

    // 6. Call-To-Action Button (Optional asset)
    val ctaText = nativeAd.callToAction
    if (!ctaText.isNullOrBlank()) {
        holder.ctaButton.text = ctaText
        holder.ctaButton.visibility = View.VISIBLE
    } else {
        holder.ctaButton.visibility = View.GONE
    }

    // Register the NativeAd object with the NativeAdView so Google Mobile Ads SDK tracks impressions & clicks
    nativeAdView.setNativeAd(nativeAd)
}
