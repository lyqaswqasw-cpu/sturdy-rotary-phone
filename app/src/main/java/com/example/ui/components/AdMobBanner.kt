package com.example.ui.components

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulTextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Official AdMob Configuration & One-Time SDK Initializer for FOUL TV.
 *
 * - Uses Google's official Test Adaptive Banner Ad Unit ID during development/debug builds
 *   so real ads are never requested during testing.
 * - Automatically switches to the production Banner Ad Unit ID (`ca-app-pub-8077917786836804/6112076148`)
 *   in release builds (or when `useProductionAdUnit = true` is explicitly passed).
 */
object AdMobConfig {
    private const val TAG = "FoulTvAdMob"

    /** User's Official AdMob App ID (also declared in AndroidManifest.xml) */
    const val APP_ID = "ca-app-pub-8077917786836804~5000681161"

    /** User's Official Production Banner Ad Unit ID */
    const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-8077917786836804/6112076148"

    /** User's Official Production Native Advanced Ad Unit ID */
    const val PRODUCTION_NATIVE_AD_UNIT_ID = "ca-app-pub-8077917786836804/1849397509"

    /** Google's Official Test Adaptive Banner Ad Unit ID for safe development & testing */
    const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    /** Google's Official Standard Test Banner Ad Unit ID */
    const val TEST_STANDARD_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /** Google's Official Test Native Advanced Ad Unit ID for safe development & testing */
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    private val initStarted = AtomicBoolean(false)
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _adsEnabledFlow = MutableStateFlow(true)
    val adsEnabledFlow: StateFlow<Boolean> = _adsEnabledFlow.asStateFlow()

    /**
     * Checks whether ads are currently enabled in local SharedPreferences (`foul_aeed9_social_prefs`)
     * and synchronizes `adsEnabledFlow`.
     */
    fun isAdsEnabledInPrefs(context: Context): Boolean {
        return try {
            val enabled = context.applicationContext
                .getSharedPreferences("foul_aeed9_social_prefs", Context.MODE_PRIVATE)
                .getBoolean("show_ads_in_app", true)
            _adsEnabledFlow.value = enabled
            enabled
        } catch (_: Throwable) {
            _adsEnabledFlow.value
        }
    }

    /**
     * Updates the global in-memory and persisted state for whether ads are enabled across the app.
     */
    fun setAdsEnabledState(context: Context, enabled: Boolean) {
        _adsEnabledFlow.value = enabled
        try {
            context.applicationContext
                .getSharedPreferences("foul_aeed9_social_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("show_ads_in_app", enabled)
                .apply()
        } catch (_: Throwable) {
        }
        if (enabled) {
            initializeOnce(context, force = true)
        }
    }

    /**
     * Resolves the appropriate Banner Ad Unit ID:
     * - Debug / Test mode (`BuildConfig.DEBUG == true`): returns Google Test Adaptive Banner ID.
     * - Production / Release mode (`BuildConfig.DEBUG == false` or `useProductionAdUnit == true`):
     *   returns the user's production Banner Ad Unit ID (`ca-app-pub-8077917786836804/6112076148`).
     */
    fun resolveBannerAdUnitId(useProductionAdUnit: Boolean = !BuildConfig.DEBUG): String {
        return if (useProductionAdUnit) {
            PRODUCTION_BANNER_AD_UNIT_ID
        } else {
            TEST_ADAPTIVE_BANNER_AD_UNIT_ID
        }
    }

    /**
     * Resolves the appropriate Native Advanced Ad Unit ID:
     * - Debug / Test mode (`BuildConfig.DEBUG == true`): returns Google Test Native Ad Unit ID
     *   (`ca-app-pub-3940256099942544/2247696110`).
     * - Production / Release mode (`BuildConfig.DEBUG == false` or `useProductionAdUnit == true`):
     *   returns the user's production Native Ad Unit ID (`ca-app-pub-8077917786836804/1849397509`).
     */
    fun resolveNativeAdUnitId(useProductionAdUnit: Boolean = !BuildConfig.DEBUG): String {
        return if (useProductionAdUnit) {
            PRODUCTION_NATIVE_AD_UNIT_ID
        } else {
            TEST_NATIVE_AD_UNIT_ID
        }
    }

    /**
     * Initializes `MobileAds.initialize` strictly ONCE per application process lifecycle,
     * after the first UI frame is already drawn on screen so startup is never blocked.
     */
    fun initializeOnce(context: Context, force: Boolean = false) {
        if (!force && !isAdsEnabledInPrefs(context)) {
            return
        }
        if (!initStarted.compareAndSet(false, true)) {
            return
        }
        val appContext = context.applicationContext
        initScope.launch {
            try {
                // Allow the Activity's first Compose frame to draw and dismiss the splash screen first
                delay(900L)
                MobileAds.initialize(appContext) { initializationStatus ->
                    _isInitialized.value = true
                    Log.d(TAG, "MobileAds initialized once: ${initializationStatus.adapterStatusMap.keys}")
                }
                // Fallback in case Play Services callback is delayed on emulator
                delay(1200L)
                _isInitialized.value = true
            } catch (t: Throwable) {
                Log.w(TAG, "MobileAds initialization failed gracefully: ${t.message}")
                _isInitialized.value = true
            }
        }
    }
}

private enum class BannerLoadState {
    LOADING,
    LOADED,
    FAILED
}

/**
 * Standalone, lifecycle-aware Jetpack Compose `AdMobBanner` using `AndroidView` and `AdView`
 * with Anchored Adaptive Banner sizing (`getCurrentOrientationAnchoredAdaptiveBannerAdSize`).
 *
 * Features:
 * - Responsive to screen/container width via Anchored Adaptive Banner.
 * - Uses Google Test Ad Unit ID during development/testing and supports Production Ad Unit ID in release.
 * - Prevents duplicate ad requests across recompositions.
 * - Properly hooks `AdView.resume()`, `AdView.pause()`, and `AdView.destroy()` to the Activity lifecycle.
 * - Displays a clean fallback space when ad loading fails or is in progress.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    useProductionAdUnit: Boolean = !BuildConfig.DEBUG,
    customAdUnitId: String? = null
) {
    val isInspectionMode = LocalInspectionMode.current
    val context = LocalContext.current
    val adsEnabled by AdMobConfig.adsEnabledFlow.collectAsState()
    if (!adsEnabled) {
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val resolvedAdUnitId = remember(useProductionAdUnit, customAdUnitId) {
        customAdUnitId?.takeIf { it.isNotBlank() }
            ?: AdMobConfig.resolveBannerAdUnitId(useProductionAdUnit = useProductionAdUnit)
    }

    // Ensure MobileAds is initialized once even if called before MainActivity.onCreate finishes
    LaunchedEffect(context, adsEnabled) {
        if (!isInspectionMode && adsEnabled) {
            AdMobConfig.initializeOnce(context, force = false)
        }
    }

    val isSdkInitialized by AdMobConfig.isInitialized.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FoulBottomBarBlack)
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        val availableWidthDp = remember(screenWidthDp) {
            screenWidthDp.coerceAtLeast(320)
        }

        if (isInspectionMode || !isSdkInitialized) {
            AdMobFallbackPlaceholder(
                message = if (isInspectionMode) {
                    "مساحة إعلانية (AdMob Adaptive Banner)"
                } else {
                    "جاري تحميل الإعلان..."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admob_loading_placeholder")
            )
        } else {
            var loadState by remember(resolvedAdUnitId, availableWidthDp) {
                mutableStateOf(BannerLoadState.LOADING)
            }
            var hasRequestedAd by remember(resolvedAdUnitId, availableWidthDp) {
                mutableStateOf(false)
            }

            // Compute Anchored Adaptive Banner AdSize matching current orientation and available width
            val adaptiveAdSize = remember(context, availableWidthDp) {
                try {
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, availableWidthDp)
                } catch (_: Throwable) {
                    AdSize.BANNER
                }
            }

            // Create and remember a single AdView instance per (adUnitId, width) only after SDK is initialized
            val adView = remember(context, resolvedAdUnitId, availableWidthDp) {
                AdView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    visibility = View.GONE
                    setAdSize(adaptiveAdSize)
                    adUnitId = resolvedAdUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            visibility = View.VISIBLE
                            loadState = BannerLoadState.LOADED
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            visibility = View.GONE
                            loadState = BannerLoadState.FAILED
                        }
                    }
                }
            }

            // Trigger AdRequest once when SDK initialization completes
            LaunchedEffect(adView, isSdkInitialized, hasRequestedAd) {
                if (isSdkInitialized && !hasRequestedAd) {
                    hasRequestedAd = true
                    try {
                        val adRequest = AdRequest.Builder().build()
                        adView.loadAd(adRequest)
                    } catch (_: Throwable) {
                        adView.visibility = View.GONE
                        loadState = BannerLoadState.FAILED
                    }
                }
            }

            // Bind AdView to Activity Lifecycle (ON_RESUME / ON_PAUSE / onDispose -> destroy)
            DisposableEffect(lifecycleOwner, adView) {
                val observer = LifecycleEventObserver { _, event ->
                    try {
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> adView.resume()
                            Lifecycle.Event.ON_PAUSE -> adView.pause()
                            else -> Unit
                        }
                    } catch (_: Throwable) {
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    try {
                        adView.destroy()
                    } catch (_: Throwable) {
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                // Render the native AdView via AndroidView
                AndroidView(
                    factory = {
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        adView
                    },
                    update = { view ->
                        view.visibility = if (loadState == BannerLoadState.LOADED) View.VISIBLE else View.GONE
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admob_adview")
                )

                // Show appropriate non-intrusive fallback space when loading fails or before first ad frame arrives
                if (loadState == BannerLoadState.FAILED) {
                    AdMobFallbackPlaceholder(
                        message = "مساحة إعلانية • FOUL TV",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admob_fallback_placeholder")
                    )
                } else if (loadState == BannerLoadState.LOADING) {
                    AdMobFallbackPlaceholder(
                        message = "جاري تحميل الإعلان...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admob_loading_placeholder")
                    )
                }
            }
        }
    }
}

@Composable
private fun AdMobFallbackPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FoulCardDark.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .heightIn(min = 44.dp)
            .border(1.dp, FoulDivider, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = FoulPurpleDeep,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "AD",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = FoulTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (BuildConfig.DEBUG) "Test Mode" else "Sponsored",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = FoulTextSecondary.copy(alpha = 0.65f)
            )
        }
    }
}
