package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChannelServer
import com.example.data.model.LibraryMediaItem
import com.example.data.model.LiveChannel
import com.example.data.model.SeriesEpisode
import com.example.data.model.XtreamAccount
import com.example.data.repository.XtreamRepository
import com.example.ui.components.AdMobConfig
import com.example.ui.components.NativeAdManager
import com.example.ui.components.NativeAdTransitionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FoulTvViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Foul TV", appName)
    }

    @Test
    fun `xtream library stream candidates follow mp4 then mkv then m3u8 order`() {
        val repo = XtreamRepository()
        val account = XtreamAccount(
            id = "1",
            name = "Test",
            host = "http://grace4k.com/",
            username = "user1",
            password = "pass1"
        )
        val candidates = repo.buildLibraryStreamCandidates(
            account = account,
            streamId = "1501766",
            isSeries = false
        )
        assertEquals(
            listOf(
                "http://grace4k.com/movie/user1/pass1/1501766.mp4",
                "http://grace4k.com/movie/user1/pass1/1501766.mkv",
                "http://grace4k.com/movie/user1/pass1/1501766.m3u8"
            ),
            candidates
        )
    }

    @Test
    fun `admob config and native ad manager resolve official production and test ad unit ids and preload on init`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals("ca-app-pub-8077917786836804~5000681161", AdMobConfig.APP_ID)
        assertEquals("ca-app-pub-8077917786836804/6112076148", AdMobConfig.resolveBannerAdUnitId(useProductionAdUnit = true))
        assertEquals("ca-app-pub-3940256099942544/9214589741", AdMobConfig.resolveBannerAdUnitId(useProductionAdUnit = false))

        assertEquals("ca-app-pub-8077917786836804/1849397509", AdMobConfig.resolveNativeAdUnitId(useProductionAdUnit = true))
        assertEquals("ca-app-pub-3940256099942544/2247696110", AdMobConfig.resolveNativeAdUnitId(useProductionAdUnit = false))

        val prodManager = NativeAdManager(context, useProductionAdUnit = true)
        assertEquals("ca-app-pub-8077917786836804/1849397509", prodManager.resolvedAdUnitId)
        assertFalse(prodManager.isDiagnosticTestMode)
        // Production manager returns false immediately when no ad is cached so production playback is never blocked
        assertFalse(prodManager.beginTransitionBeforePlayback("beIN Sports 1"))

        val testManager = NativeAdManager(context, useProductionAdUnit = false)
        assertEquals("ca-app-pub-3940256099942544/2247696110", testManager.resolvedAdUnitId)
        assertTrue(testManager.isDiagnosticTestMode)
        assertTrue(testManager.isCooldownElapsed())
        // Verify loadAd was called immediately at NativeAdManager startup
        assertTrue(testManager.diagnosticState.value.loadAdCalled)
        assertTrue(testManager.diagnosticState.value.requestCount >= 1)

        prodManager.releaseAll()
        testManager.releaseAll()
    }

    @Test
    fun `clicking channel movie and series episode shows transitional ad screen with X button before starting playback`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = FoulTvViewModel(app)

        // 1. Test clicking a Live Channel
        val testChannel = LiveChannel(
            id = "ch_bein_1",
            name = "beIN Sports 1 HD",
            category = "قنوات رياضية",
            imageUrl = "",
            servers = listOf(
                ChannelServer(
                    id = "s1",
                    name = "FHD",
                    streamType = "m3u8",
                    streamUrl = "https://example.com/live/bein1.m3u8"
                )
            )
        )
        viewModel.playLiveChannel(testChannel)

        // Verify playback does NOT start before closing the transitional ad screen
        assertTrue(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNull(viewModel.uiState.value.activePlayback)
        assertNotNull(viewModel.uiState.value.pendingPlaybackAfterAd)
        assertEquals("beIN Sports 1 HD", viewModel.uiState.value.pendingPlaybackAfterAd?.title)

        // Render the transitional ad screen and click the independent Close (X) button
        composeTestRule.setContent {
            MyApplicationTheme {
                val pending = viewModel.uiState.value.pendingPlaybackAfterAd
                NativeAdTransitionScreen(
                    nativeAd = viewModel.nativeAdManager.activeTransitionAd.value,
                    diagnosticState = viewModel.nativeAdManager.diagnosticState.value,
                    pendingContentTitle = pending?.title.orEmpty(),
                    pendingContentSubtitle = pending?.subtitle.orEmpty(),
                    isLiveChannel = pending?.isLiveChannel == true,
                    onRetryLoadAd = { viewModel.nativeAdManager.retryLoadAdNow() },
                    onCloseAdAndPlayContent = viewModel::dismissNativeAdAndStartPlayback
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("native_ad_transition_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("native_ad_close_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("native_ad_close_button").performClick()
        composeTestRule.waitForIdle()

        // Verify pressing X immediately starts the selected Live Channel and closes the ad screen
        assertFalse(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNull(viewModel.uiState.value.pendingPlaybackAfterAd)
        assertNotNull(viewModel.uiState.value.activePlayback)
        assertEquals("beIN Sports 1 HD", viewModel.uiState.value.activePlayback?.title)
        assertTrue(viewModel.uiState.value.activePlayback?.isLiveChannel == true)

        // Close player and test cooldown protection (immediate second click within 30s starts directly without ad)
        viewModel.closePlayer()
        val secondChannel = testChannel.copy(id = "ch_bein_2", name = "beIN Sports 2 HD")
        viewModel.playLiveChannel(secondChannel)
        assertFalse(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertEquals("beIN Sports 2 HD", viewModel.uiState.value.activePlayback?.title)

        // 2. Test clicking a Movie after cooldown elapsed
        viewModel.closePlayer()
        viewModel.nativeAdManager.resetCooldownForTesting()
        val testAccount = XtreamAccount(
            id = "acc1",
            name = "Main",
            host = "http://grace4k.com",
            username = "u1",
            password = "p1"
        )
        val testMovie = LibraryMediaItem(
            id = "9001",
            name = "Oppenheimer",
            lowerName = "oppenheimer",
            posterUrl = "",
            rating = "8.9",
            releaseYear = "2023",
            isSeries = false,
            containerExtension = "mp4",
            categoryId = "cat_movies",
            account = testAccount
        )
        viewModel.playLibraryMovie(testMovie)
        assertTrue(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNull(viewModel.uiState.value.activePlayback)
        assertEquals("Oppenheimer", viewModel.uiState.value.pendingPlaybackAfterAd?.title)

        // Press X to start the Movie
        viewModel.dismissNativeAdAndStartPlayback()
        assertFalse(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNotNull(viewModel.uiState.value.activePlayback)
        assertEquals("Oppenheimer", viewModel.uiState.value.activePlayback?.title)

        // 3. Test clicking a Series Episode after cooldown elapsed
        viewModel.closePlayer()
        viewModel.nativeAdManager.resetCooldownForTesting()
        val testSeries = LibraryMediaItem(
            id = "7001",
            name = "House of the Dragon",
            lowerName = "house of the dragon",
            posterUrl = "",
            rating = "8.5",
            releaseYear = "2024",
            isSeries = true,
            containerExtension = "mp4",
            categoryId = "cat_series",
            account = testAccount
        )
        val testEpisode = SeriesEpisode(
            id = "ep_101",
            episodeNum = 1,
            seasonNum = 1,
            title = "The Heirs of the Dragon",
            plot = "",
            duration = "01:02:00",
            rating = "8.8",
            releaseDate = "2024",
            containerExtension = "mp4",
            coverUrl = ""
        )
        viewModel.playLibraryEpisode(testSeries, testEpisode)
        assertTrue(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNull(viewModel.uiState.value.activePlayback)
        assertEquals("House of the Dragon", viewModel.uiState.value.pendingPlaybackAfterAd?.title)

        // Press X to start the Series Episode
        viewModel.dismissNativeAdAndStartPlayback()
        assertFalse(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNotNull(viewModel.uiState.value.activePlayback)
        assertEquals("House of the Dragon", viewModel.uiState.value.activePlayback?.title)
        assertTrue(viewModel.uiState.value.activePlayback?.subtitle?.contains("الحلقة 1") == true)

        // 4. Test Developer Control Panel Ads Disable / Enable toggle
        viewModel.closePlayer()
        viewModel.nativeAdManager.resetCooldownForTesting()
        viewModel.toggleShowAdsInAppOnline(false)
        assertFalse("Ads should be disabled in UI state", viewModel.uiState.value.showAdsInApp)
        assertFalse("NativeAdManager should be disabled", viewModel.nativeAdManager.isAdsEnabled)

        // When ads are disabled, clicking a channel starts playback immediately with zero ad screen
        viewModel.playLiveChannel(testChannel)
        assertFalse("Ad transition screen must NOT appear when ads are disabled", viewModel.uiState.value.isAdTransitionScreenVisible)
        assertNotNull("Playback must start immediately when ads are disabled", viewModel.uiState.value.activePlayback)
        assertEquals("beIN Sports 1 HD", viewModel.uiState.value.activePlayback?.title)

        // Re-enable ads from Developer Control Panel and verify ad transition screen works again
        viewModel.closePlayer()
        viewModel.nativeAdManager.resetCooldownForTesting()
        viewModel.toggleShowAdsInAppOnline(true)
        assertTrue("Ads should be re-enabled in UI state", viewModel.uiState.value.showAdsInApp)
        assertTrue("NativeAdManager should be re-enabled", viewModel.nativeAdManager.isAdsEnabled)

        viewModel.playLibraryMovie(testMovie)
        assertTrue("Ad transition screen should appear again when ads are re-enabled", viewModel.uiState.value.isAdTransitionScreenVisible)
        viewModel.dismissNativeAdAndStartPlayback()
        assertFalse(viewModel.uiState.value.isAdTransitionScreenVisible)
        assertEquals("Oppenheimer", viewModel.uiState.value.activePlayback?.title)

        viewModel.nativeAdManager.releaseAll()
    }
}
