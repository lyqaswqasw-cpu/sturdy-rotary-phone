package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppVersionConfig
import com.example.data.model.BottomNavTab
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelEngagementStats
import com.example.data.model.ChannelServer
import com.example.data.model.ContactMessageItem
import com.example.data.model.EventBanner
import com.example.data.model.LeagueMatchesGroup
import com.example.data.model.LibraryFilterType
import com.example.data.model.LibraryMediaItem
import com.example.data.model.LineupPlayer
import com.example.data.model.LiveChannel
import com.example.data.model.LiveEventMatch
import com.example.data.model.MatchCompetitor
import com.example.data.model.MatchDetailData
import com.example.data.model.MatchItem
import com.example.data.model.MatchPublishedChannel
import com.example.data.model.MatchesSearchResult
import com.example.data.model.MediaDetailInfo
import com.example.data.model.PlaybackRequest
import com.example.data.model.PlayerFullProfile
import com.example.data.model.PlayerGuideChannel
import com.example.data.model.PublishedEventItem
import com.example.data.model.SeriesEpisode
import com.example.data.model.SocialUserProfile
import com.example.data.model.TeamFullProfile
import com.example.data.model.XtreamAccount
import com.example.data.model.XtreamAccountLiveCatalog
import com.example.data.model.XtreamLiveCategory
import com.example.data.model.XtreamLiveStreamItem
import com.example.data.repository.FirebaseRepository
import com.example.data.repository.Scores365Repository
import com.example.data.repository.SocialFirebaseRepository
import com.example.data.repository.XtreamRepository
import com.example.ui.components.NativeAdManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class DateOption(
    val labelAr: String,
    val apiDateString: String // DD/MM/YYYY
)

data class FoulTvUiState(
    val currentTab: BottomNavTab = BottomNavTab.HOME,

    // Home (Firebase Branches & Channels Hierarchy) State
    val isLoadingHome: Boolean = true,
    val homeError: String? = null,
    val categories: List<ChannelCategory> = emptyList(),
    val branchNavigationStack: List<ChannelCategory> = emptyList(),
    val channels: List<LiveChannel> = emptyList(),
    val banners: List<EventBanner> = emptyList(),
    val liveEvents: List<LiveEventMatch> = emptyList(),
    val matchPublishedChannels: List<MatchPublishedChannel> = emptyList(),
    val publishedEvents: List<PublishedEventItem> = emptyList(),
    val xtreamAccounts: List<XtreamAccount> = emptyList(),
    val iptvAccounts: List<XtreamAccount> = emptyList(),
    val telegramUrl: String = FirebaseRepository.DEFAULT_TELEGRAM_URL,
    val versionConfig: AppVersionConfig = AppVersionConfig(),

    // Dedicated IPTV Bottom Tab State
    val selectedIptvAccount: XtreamAccount? = null,
    val isLoadingIptv: Boolean = false,
    val iptvError: String? = null,
    val iptvCategories: List<XtreamLiveCategory> = emptyList(),
    val iptvStreams: List<XtreamLiveStreamItem> = emptyList(),
    val selectedIptvCategoryId: String = "ALL",
    val isIptvSearchOpen: Boolean = false,
    val iptvSearchQuery: String = "",

    // Secondary Firebase Database (`foul-aeed9`): Per-Channel Views/Likes + Ads Control + Online Presence + User Account & Contact Us State
    val channelStatsMap: Map<String, ChannelEngagementStats> = emptyMap(),
    val showChannelViewsAndLikes: Boolean = true,
    val showAdsInApp: Boolean = true,
    val onlineUsersCount: Int = 1,
    val currentSocialUser: SocialUserProfile? = null,
    val contactMessages: List<ContactMessageItem> = emptyList(),
    val developerContactMessages: List<ContactMessageItem> = emptyList(),
    val isContactUsScreenOpen: Boolean = false,
    val isSocialAuthLoading: Boolean = false,
    val isSendingContactMessage: Boolean = false,
    val socialAuthError: String? = null,

    // Developer Authentication & Isolated Panel State

    val isDevLoginSheetOpen: Boolean = false,
    val isLoggingInDeveloper: Boolean = false,
    val devLoginError: String? = null,
    val isDeveloperLoggedIn: Boolean = false,
    val developerEmail: String = "",
    val isDeveloperPanelOpen: Boolean = false,
    val isDevActionLoading: Boolean = false,
    val devStatusMessage: String? = null,

    // Developer "نشر فرع حساب" (Publish Account Branch from Xtream Live Categories & Streams) State
    val isFetchingAccountBranchCatalog: Boolean = false,
    val isFetchingAccountCategoryStreams: Boolean = false,
    val accountBranchFetchError: String? = null,
    val fetchedAccountLiveCatalog: XtreamAccountLiveCatalog? = null,

    // Library (Xtream) State
    val isLoadingLibrary: Boolean = false,
    val libraryError: String? = null,
    val libraryMainFilter: LibraryFilterType = LibraryFilterType.ALL,
    val libraryDisplayedItems: List<LibraryMediaItem> = emptyList(),
    val libraryVisibleCount: Int = XtreamRepository.PAGE_SIZE,
    val libraryHasMore: Boolean = false,

    // Library Search Page State
    val isLibrarySearchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchFilter: LibraryFilterType = LibraryFilterType.ALL,
    val searchResults: List<LibraryMediaItem> = emptyList(),
    val isSearching: Boolean = false,

    // Library Media Detail (Movie / Series) State
    val selectedMediaItem: LibraryMediaItem? = null,
    val isLoadingMediaDetail: Boolean = false,
    val mediaDetailError: String? = null,
    val mediaDetail: MediaDetailInfo? = null,
    val selectedSeasonNumber: Int = 1,

    // Active Video Playback State & Transitional Native Ad Pending State
    val isAdTransitionScreenVisible: Boolean = false,
    val activePlayback: PlaybackRequest? = null,
    val pendingPlaybackAfterAd: PlaybackRequest? = null,
    val pendingMediaDetailAfterAd: LibraryMediaItem? = null,

    // Matches (365Scores) State
    val dateOptions: List<DateOption> = emptyList(),
    val selectedDateString: String = "",
    val isLoadingMatches: Boolean = false,
    val matchesError: String? = null,
    val leagueGroups: List<LeagueMatchesGroup> = emptyList(),

    // Match Detail Page State
    val selectedMatch: MatchItem? = null,
    val isLoadingMatchDetail: Boolean = false,
    val matchDetailError: String? = null,
    val matchDetail: MatchDetailData? = null,

    // Matches Search & Player/Team Full Profiles State
    val isMatchesSearchOpen: Boolean = false,
    val matchesSearchQuery: String = "",
    val isSearchingMatches: Boolean = false,
    val matchesSearchResult: MatchesSearchResult? = null,
    val selectedPlayerProfile: PlayerFullProfile? = null,
    val isLoadingPlayerProfile: Boolean = false,
    val playerProfileError: String? = null,
    val selectedTeamProfile: TeamFullProfile? = null,
    val isLoadingTeamProfile: Boolean = false,
    val teamProfileError: String? = null
) {
    val currentHomeBranch: ChannelCategory?
        get() = branchNavigationStack.lastOrNull()

    /**
     * Branches to display in the vertical strip on the Home screen:
     * - At root (`currentHomeBranch == null`), shows main branches (`parentId.isBlank()`).
     * - Inside a branch, shows child sub-branches (`parentId == currentHomeBranch.id`).
     */
    val visibleHomeBranches: List<ChannelCategory>
        get() {
            val activeBranch = currentHomeBranch
            return if (activeBranch == null) {
                categories.filter { it.parentId.isBlank() }.sortedBy { it.order }
            } else {
                categories.filter { it.parentId == activeBranch.id }.sortedBy { it.order }
            }
        }

    /**
     * Channels to display in the 3x3 grid when inside a selected branch or inner sub-branch.
     */
        val visibleBranchChannels: List<LiveChannel>
        get() {
            val activeBranch = currentHomeBranch ?: return emptyList()
            return channels.filter { ch ->
                ch.branchId == activeBranch.id ||
                    (ch.branchId.isBlank() && ch.category.equals(activeBranch.name, ignoreCase = true))
            }.sortedWith(compareBy<LiveChannel> { if (it.order > 0) it.order else Int.MAX_VALUE })
        }
}

class FoulTvViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
    private val socialRepository: SocialFirebaseRepository = SocialFirebaseRepository(application)
    private val xtreamRepository: XtreamRepository = XtreamRepository()
    private val scoresRepository: Scores365Repository = Scores365Repository()
    val nativeAdManager: NativeAdManager = NativeAdManager(application)

    private val _uiState = MutableStateFlow(
        FoulTvUiState(
            showChannelViewsAndLikes = socialRepository.showChannelViewsAndLikes.value,
            showAdsInApp = socialRepository.showAdsInApp.value
        )
    )
    val uiState: StateFlow<FoulTvUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var iptvLoadJob: Job? = null

    init {
        val dates = buildDateOptions()
        val defaultDate = dates.firstOrNull { it.labelAr == "اليوم" }?.apiDateString
            ?: dates.first().apiDateString
        _uiState.update {
            it.copy(
                dateOptions = dates,
                selectedDateString = defaultDate,
                showChannelViewsAndLikes = socialRepository.showChannelViewsAndLikes.value,
                showAdsInApp = socialRepository.showAdsInApp.value
            )
        }

        // Observe secondary Firebase (`foul-aeed9`) channel views & likes stats
        viewModelScope.launch {
            socialRepository.channelStatsMap.collect { statsMap ->
                _uiState.update { it.copy(channelStatsMap = statsMap) }
            }
        }

        // Observe secondary Firebase (`foul-aeed9`) authenticated user profile
        viewModelScope.launch {
            socialRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentSocialUser = user) }
                if (user != null) {
                    socialRepository.fetchContactMessages()
                }
            }
        }

        // Observe secondary Firebase (`foul-aeed9`) Contact Us messages
        viewModelScope.launch {
            socialRepository.contactMessages.collect { messages ->
                _uiState.update { it.copy(contactMessages = messages) }
            }
        }

        // Observe secondary Firebase (`foul-aeed9`) Unified Developer User Messages
        viewModelScope.launch {
            socialRepository.allDeveloperMessages.collect { devMsgs ->
                _uiState.update { it.copy(developerContactMessages = devMsgs) }
            }
        }

        // Observe online setting for showing/hiding channel views & likes
        viewModelScope.launch {
            socialRepository.showChannelViewsAndLikes.collect { show ->
                _uiState.update { it.copy(showChannelViewsAndLikes = show) }
            }
        }

        // Observe online setting for enabling/disabling all ads (Banner + Native) across the app
        viewModelScope.launch {
            socialRepository.showAdsInApp.collect { showAds ->
                nativeAdManager.setAdsEnabled(showAds)
                _uiState.update { state ->
                    if (!showAds && state.isAdTransitionScreenVisible) {
                        val pendingPlayback = state.pendingPlaybackAfterAd
                        state.copy(
                            showAdsInApp = false,
                            isAdTransitionScreenVisible = false,
                            activePlayback = pendingPlayback ?: state.activePlayback,
                            pendingPlaybackAfterAd = null,
                            pendingMediaDetailAfterAd = null
                        )
                    } else {
                        state.copy(showAdsInApp = showAds)
                    }
                }
            }
        }

        // Observe real-time online users count from secondary Firebase (`foul-aeed9`)
        viewModelScope.launch {
            socialRepository.onlineUsersCount.collect { count ->
                _uiState.update { it.copy(onlineUsersCount = count) }
            }
        }

        // Periodic real-time online App Control (`showAdsInApp` & `showChannelViewsAndLikes`) + presence heartbeat sync
        // Paused automatically while video player is open so 100% of network bandwidth is dedicated to the stream!
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.activePlayback == null) {
                    socialRepository.syncAppControlSettings()
                    socialRepository.heartbeatAndSyncOnlinePresence()
                }
                val intervalMs = if (_uiState.value.isDeveloperPanelOpen) 10_000L else 25_000L
                delay(intervalMs)
            }
        }

        loadHomeData(forceRefresh = false)
        loadMatchesForDate(defaultDate)
    }

    fun refreshOnlineUsersCount() {
        viewModelScope.launch {
            socialRepository.syncAppControlSettings()
            socialRepository.heartbeatAndSyncOnlinePresence()
        }
    }


    private fun buildDateOptions(): List<DateOption> {
        val baghdadTz = TimeZone.getTimeZone("Asia/Baghdad")
        val apiFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { timeZone = baghdadTz }
        val dayLabelFmt = SimpleDateFormat("EEEE dd/MM", Locale("ar")).apply { timeZone = baghdadTz }

        val list = mutableListOf<DateOption>()
        for (offset in -2..2) {
            val cal = Calendar.getInstance(baghdadTz)
            cal.add(Calendar.DAY_OF_YEAR, offset)
            val dateStr = apiFmt.format(cal.time)
            val label = when (offset) {
                -1 -> "أمس"
                0 -> "اليوم"
                1 -> "غداً"
                else -> dayLabelFmt.format(cal.time)
            }
            list.add(DateOption(labelAr = label, apiDateString = dateStr))
        }
        if (list.none { it.apiDateString == "21/10/2026" }) {
            list.add(0, DateOption(labelAr = "الأربعاء 21/10", apiDateString = "21/10/2026"))
        }
        return list
    }

    fun selectTab(tab: BottomNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
        nativeAdManager.preloadNextAd()
        if (tab == BottomNavTab.LIBRARY && _uiState.value.libraryDisplayedItems.isEmpty() && !_uiState.value.isLoadingLibrary) {
            loadLibraryInitial()
        } else if (tab == BottomNavTab.IPTV && _uiState.value.iptvStreams.isEmpty() && !_uiState.value.isLoadingIptv) {
            ensureIptvInitialAccountLoaded()
        }
    }

    // --- Dedicated & Completely Isolated IPTV Section (v4_iptv_accounts ONLY, separate from Library) ---

    val effectiveIptvAccounts: List<XtreamAccount>
        get() = _uiState.value.iptvAccounts

    fun ensureIptvInitialAccountLoaded(forceRefresh: Boolean = false) {
        val accounts = _uiState.value.iptvAccounts
        if (accounts.isEmpty()) {
            _uiState.update {
                it.copy(
                    selectedIptvAccount = null,
                    isLoadingIptv = false,
                    iptvCategories = emptyList(),
                    iptvStreams = emptyList(),
                    iptvError = null
                )
            }
            return
        }
        val currentSelected = _uiState.value.selectedIptvAccount
        val targetAccount = accounts.find { it.id == currentSelected?.id } ?: accounts.first()
        selectIptvAccount(targetAccount, forceRefresh = forceRefresh)
    }

    /**
     * Switches to the selected IPTV account:
     * 1. Immediately clears the previous account's channels & categories ("وتغلق القنوات السابقة")
     * 2. Shows the circular loading indicator ("وعند تنقل تظهر دائرة تحميل")
     * 3. Fetches the new account's live categories & channels ultra-fast ("ويكون بشكل تحميل سريع جدا جدا جدا")
     */
    fun selectIptvAccount(account: XtreamAccount, forceRefresh: Boolean = false) {
        iptvLoadJob?.cancel()
        _uiState.update {
            it.copy(
                selectedIptvAccount = account,
                isLoadingIptv = true,
                iptvError = null,
                iptvCategories = emptyList(),
                iptvStreams = emptyList(),
                selectedIptvCategoryId = "ALL",
                iptvSearchQuery = ""
            )
        }
        iptvLoadJob = viewModelScope.launch {
            val res = xtreamRepository.fetchIptvAccountLiveCatalogFast(
                account = account,
                forceRefresh = forceRefresh
            )
            res.fold(
                onSuccess = { catalog ->
                    _uiState.update { state ->
                        if (state.selectedIptvAccount?.id == account.id) {
                            state.copy(
                                isLoadingIptv = false,
                                iptvError = null,
                                iptvCategories = catalog.categories,
                                iptvStreams = catalog.streams
                            )
                        } else {
                            state
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update { state ->
                        if (state.selectedIptvAccount?.id == account.id) {
                            state.copy(
                                isLoadingIptv = false,
                                iptvError = err.message ?: "تعذر جلب قنوات هذا الحساب حالياً"
                            )
                        } else {
                            state
                        }
                    }
                }
            )
        }
    }

    fun selectIptvCategory(categoryId: String) {
        _uiState.update { it.copy(selectedIptvCategoryId = categoryId) }
    }

    fun toggleIptvSearch() {
        _uiState.update { state ->
            val nextOpen = !state.isIptvSearchOpen
            state.copy(
                isIptvSearchOpen = nextOpen,
                iptvSearchQuery = if (!nextOpen) "" else state.iptvSearchQuery
            )
        }
    }

    fun updateIptvSearchQuery(query: String) {
        _uiState.update { it.copy(iptvSearchQuery = query) }
    }

    fun refreshCurrentIptvAccount() {
        val currentAcc = _uiState.value.selectedIptvAccount
        if (currentAcc != null) {
            selectIptvAccount(currentAcc, forceRefresh = true)
        } else {
            ensureIptvInitialAccountLoaded(forceRefresh = true)
        }
    }

    fun playIptvStream(stream: XtreamLiveStreamItem) {
        val resolvedCatName = stream.categoryName.ifBlank {
            _uiState.value.iptvCategories.find { it.categoryId == stream.categoryId }?.categoryName ?: "IPTV مباشر"
        }
        val liveChannel = stream.toLiveChannel(
            branchId = "iptv_${stream.categoryId}",
            branchName = resolvedCatName
        )
        val request = PlaybackRequest(
            title = liveChannel.name,
            subtitle = resolvedCatName,
            isLiveChannel = true,
            channelId = liveChannel.id,
            activeCategoryId = stream.categoryId,
            activeCategoryName = resolvedCatName,
            isFromIptv = true,
            candidateUrls = liveChannel.servers.map { s -> s.streamUrl },
            servers = liveChannel.servers
        )
        requestPlaybackWithTransitionalAd(request)
    }

    // --- Home Branches & Channels Navigation ---

    fun loadHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHome = true, homeError = null) }
            val result = firebaseRepository.getFirebaseData(forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { payload ->
                    val updatedStack = _uiState.value.branchNavigationStack.mapNotNull { oldBranch ->
                        payload.categories.find { it.id == oldBranch.id }
                    }
                    val isolatedIptvList = payload.iptvAccounts
                    val prevIptvSelected = _uiState.value.selectedIptvAccount
                    val nextIptvSelected = isolatedIptvList.find { it.id == prevIptvSelected?.id }
                        ?: isolatedIptvList.firstOrNull()

                    _uiState.update { state ->
                        state.copy(
                            isLoadingHome = false,
                            homeError = null,
                            categories = payload.categories,
                            branchNavigationStack = updatedStack,
                            channels = payload.channels,
                            banners = payload.banners,
                            liveEvents = payload.liveMatches,
                            matchPublishedChannels = payload.matchPublishedChannels,
                            publishedEvents = payload.publishedEvents,
                            xtreamAccounts = payload.xtreamAccounts,
                            iptvAccounts = isolatedIptvList,
                            selectedIptvAccount = nextIptvSelected,
                            iptvCategories = if (isolatedIptvList.isEmpty()) emptyList() else state.iptvCategories,
                            iptvStreams = if (isolatedIptvList.isEmpty()) emptyList() else state.iptvStreams,
                            telegramUrl = payload.telegramUrl,
                            versionConfig = payload.versionConfig
                        )
                    }
                    // Apply online App Control flags parsed from primary Firebase (`foul-ddbb8`) immediately
                    socialRepository.applyRemoteAppControlFromPrimary(
                        showViews = payload.showChannelViewsAndLikes,
                        showAds = payload.showAdsInApp,
                        remoteUpdatedAt = payload.appControlUpdatedAt
                    )
                    // Sync per-channel views & likes + latest online App Control from both Firebase databases
                    viewModelScope.launch {
                        socialRepository.syncAllChannelStats(payload.channels)
                    }
                    if (_uiState.value.currentTab == BottomNavTab.IPTV && _uiState.value.iptvStreams.isEmpty()) {
                        ensureIptvInitialAccountLoaded()
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingHome = false,
                            homeError = err.message ?: "تعذر الاتصال بقاعدة البيانات"
                        )
                    }
                }
            )
        }
    }

    fun openHomeBranch(branch: ChannelCategory) {
        _uiState.update { state ->
            state.copy(branchNavigationStack = state.branchNavigationStack + branch)
        }
    }

    fun navigateBackHomeBranch() {
        _uiState.update { state ->
            if (state.branchNavigationStack.isNotEmpty()) {
                state.copy(branchNavigationStack = state.branchNavigationStack.dropLast(1))
            } else {
                state
            }
        }
    }

    fun playLiveChannel(channel: LiveChannel) {
        if (channel.servers.isEmpty()) return
        val resolvedCatId = channel.branchId.ifBlank { channel.category }
        val resolvedCatName = _uiState.value.categories.find { it.id == channel.branchId }?.name
            ?.ifBlank { channel.category } ?: channel.category
        val request = PlaybackRequest(
            title = channel.name,
            subtitle = resolvedCatName,
            isLiveChannel = true,
            channelId = channel.id,
            activeCategoryId = resolvedCatId,
            activeCategoryName = resolvedCatName,
            isFromIptv = false,
            candidateUrls = channel.servers.map { s -> s.streamUrl },
            servers = channel.servers
        )
        requestPlaybackWithTransitionalAd(request)
    }

    fun switchChannelInPlayer(guideChannel: PlayerGuideChannel, isFromIptv: Boolean) {
        if (guideChannel.candidateUrls.isEmpty()) return
        val nextRequest = PlaybackRequest(
            title = guideChannel.name,
            subtitle = guideChannel.categoryName,
            isLiveChannel = true,
            channelId = guideChannel.id,
            activeCategoryId = guideChannel.categoryId,
            activeCategoryName = guideChannel.categoryName,
            isFromIptv = isFromIptv,
            candidateUrls = guideChannel.candidateUrls,
            servers = guideChannel.servers
        )
        _uiState.update {
            it.copy(
                isAdTransitionScreenVisible = false,
                activePlayback = nextRequest,
                pendingPlaybackAfterAd = null,
                pendingMediaDetailAfterAd = null
            )
        }
    }

    /**
     * Plays a direct `.mp4` or `.mkv` (or stream) published event video in the smooth ExoPlayer screen.
     */
    fun playPublishedEventVideo(event: PublishedEventItem) {
        val rawUrl = event.videoUrl.trim()
        if (rawUrl.isBlank()) return
        val ext = when {
            rawUrl.lowercase().contains(".mkv") -> "mkv"
            rawUrl.lowercase().contains(".mp4") -> "mp4"
            rawUrl.lowercase().contains(".ts") -> "ts"
            else -> "m3u8"
        }
        val request = PlaybackRequest(
            title = event.title.trim().ifBlank { "حدث مباشر • عرض فيديو" },
            subtitle = event.description.trim().ifBlank {
                if (ext == "mp4" || ext == "mkv") "فيديو حدث منشور ($ext)" else "بث حدث مباشر"
            },
            isLiveChannel = ext == "m3u8" || ext == "ts",
            channelId = "",
            candidateUrls = listOf(rawUrl),
            servers = listOf(
                ChannelServer(
                    id = "${event.id}_video",
                    name = "تشغيل الحدث (${ext.uppercase()})",
                    streamType = ext,
                    streamUrl = rawUrl
                )
            )
        )
        requestPlaybackWithTransitionalAd(request)
    }

    /**
     * Routes a selected channel, movie, or episode through `NativeAdManager`:
     * - Prevents showing more than one ad at the same time.
     * - If the cooldown interval has elapsed:
     *   - If a preloaded `NativeAd` is ready, displays `NativeAdTransitionScreen` before starting playback.
     *   - If the ad is not ready yet:
     *     - In testing/diagnostic mode (`isDiagnosticTestMode == true`), displays `NativeAdTransitionScreen`
     *       with the live diagnostic state (and automatically upgrades to the real `NativeAdView` if it finishes loading).
     *     - In production mode (`isDiagnosticTestMode == false`), starts playback immediately without blocking.
     * - While `isAdTransitionScreenVisible == true`, `activePlayback` remains `null` so stream playback never starts
     *   before the user closes the transitional screen via the `X` button.
     */
    private fun requestPlaybackWithTransitionalAd(request: PlaybackRequest) {
        val currentState = _uiState.value
        if (currentState.isAdTransitionScreenVisible) {
            // Prevent showing more than one ad at the same time; preserve the latest selected content
            _uiState.update {
                it.copy(
                    pendingPlaybackAfterAd = request,
                    pendingMediaDetailAfterAd = null,
                    activePlayback = null
                )
            }
            return
        }

        val isCurrentlyPlayingVideo = currentState.activePlayback != null
        if (isCurrentlyPlayingVideo || !currentState.showAdsInApp || !nativeAdManager.isAdsEnabled) {
            nativeAdManager.logContentPlaybackStarted(request.title, request.subtitle)
            _uiState.update {
                it.copy(
                    isAdTransitionScreenVisible = false,
                    activePlayback = request,
                    pendingPlaybackAfterAd = null,
                    pendingMediaDetailAfterAd = null
                )
            }
            return
        }

        val showTransitionScreen = nativeAdManager.beginTransitionBeforePlayback(
            contentTitle = request.title
        )
        if (showTransitionScreen) {
            _uiState.update {
                it.copy(
                    isAdTransitionScreenVisible = true,
                    pendingPlaybackAfterAd = request,
                    pendingMediaDetailAfterAd = null,
                    activePlayback = null
                )
            }
        } else {
            nativeAdManager.logContentPlaybackStarted(request.title, request.subtitle)
            _uiState.update {
                it.copy(
                    isAdTransitionScreenVisible = false,
                    activePlayback = request,
                    pendingPlaybackAfterAd = null,
                    pendingMediaDetailAfterAd = null
                )
            }
        }
    }

    /**
     * Called when the user taps the independent Close (X) button on the transitional Native Ad screen.
     * Destroys the consumed ad, preloads the next ad in the background, and immediately starts the selected content
     * strictly once.
     */
    fun dismissNativeAdAndStartPlayback() {
        val currentState = _uiState.value
        if (!currentState.isAdTransitionScreenVisible &&
            currentState.pendingPlaybackAfterAd == null &&
            currentState.pendingMediaDetailAfterAd == null
        ) {
            return
        }

        val pendingPlayback = currentState.pendingPlaybackAfterAd
        val pendingMedia = currentState.pendingMediaDetailAfterAd
        val closedTitle = pendingPlayback?.title ?: pendingMedia?.name.orEmpty()

        nativeAdManager.dismissActiveAdAndPreloadNext(contentTitle = closedTitle)

        if (pendingPlayback != null) {
            nativeAdManager.logContentPlaybackStarted(
                contentTitle = pendingPlayback.title,
                contentSubtitle = pendingPlayback.subtitle
            )
        } else if (pendingMedia != null) {
            nativeAdManager.logContentPlaybackStarted(
                contentTitle = pendingMedia.name,
                contentSubtitle = if (pendingMedia.isSeries) "مسلسل • المكتبة" else "فيلم • المكتبة"
            )
        }

        _uiState.update { state ->
            state.copy(
                isAdTransitionScreenVisible = false,
                activePlayback = pendingPlayback ?: state.activePlayback,
                pendingPlaybackAfterAd = null,
                pendingMediaDetailAfterAd = null
            )
        }
    }

    fun toggleChannelLike(channelId: String) {
        if (channelId.isBlank()) return
        viewModelScope.launch {
            socialRepository.toggleChannelLike(channelId)
        }
    }

    // --- Secondary Firebase (`foul-aeed9`) Contact Us & User Account Auth ---

    fun openContactUsScreen() {
        _uiState.update {
            it.copy(
                isContactUsScreenOpen = true,
                socialAuthError = null
            )
        }
        val currentUser = _uiState.value.currentSocialUser
        if (currentUser != null) {
            viewModelScope.launch {
                socialRepository.fetchContactMessages()
            }
        }
    }

    fun closeContactUsScreen() {
        _uiState.update {
            it.copy(
                isContactUsScreenOpen = false,
                socialAuthError = null
            )
        }
    }

    fun clearSocialAuthError() {
        _uiState.update { it.copy(socialAuthError = null) }
    }

    fun loginSocialUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSocialAuthLoading = true, socialAuthError = null) }
            val res = socialRepository.loginUserAccount(email = email, password = password)
            res.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isSocialAuthLoading = false,
                            currentSocialUser = profile,
                            socialAuthError = null
                        )
                    }
                    socialRepository.fetchContactMessages()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSocialAuthLoading = false,
                            socialAuthError = err.message ?: "تعذر تسجيل الدخول"
                        )
                    }
                }
            )
        }
    }

    fun registerSocialUser(
        email: String,
        password: String,
        username: String,
        avatarId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSocialAuthLoading = true, socialAuthError = null) }
            val res = socialRepository.registerUserAccount(
                email = email,
                password = password,
                username = username,
                avatarId = avatarId
            )
            res.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isSocialAuthLoading = false,
                            currentSocialUser = profile,
                            socialAuthError = null
                        )
                    }
                    socialRepository.fetchContactMessages()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSocialAuthLoading = false,
                            socialAuthError = err.message ?: "تعذر إنشاء الحساب"
                        )
                    }
                }
            )
        }
    }

    fun logoutSocialUser() {
        socialRepository.logoutUserAccount()
        _uiState.update {
            it.copy(
                currentSocialUser = null,
                socialAuthError = null
            )
        }
    }

    fun sendContactUsMessage(text: String, replyTo: ContactMessageItem? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingContactMessage = true) }
            socialRepository.sendContactMessage(text, replyTo)
            _uiState.update { it.copy(isSendingContactMessage = false) }
        }
    }

    fun refreshContactUsMessages() {
        viewModelScope.launch {
            socialRepository.fetchContactMessages()
        }
    }

    fun refreshDeveloperUserMessages() {
        viewModelScope.launch {
            socialRepository.fetchAllDeveloperMessages()
        }
    }

    fun sendDeveloperReplyToUser(targetMessage: ContactMessageItem, replyText: String) {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = socialRepository.sendDeveloperReply(
                targetMessage = targetMessage,
                replyText = replyText,
                developerEmail = _uiState.value.developerEmail
            )
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم إرسال الرد الخاص إلى (${targetMessage.senderName}) بنجاح"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر إرسال الرد الخاص للمستخدم"
                        )
                    }
                }
            )
        }
    }


    fun onBannerClick(banner: EventBanner) {
        val state = _uiState.value
        val matchedChannel = state.channels.firstOrNull { ch ->
            ch.id == banner.channelId ||
                banner.channelIds.contains(ch.id) ||
                ch.name.equals(banner.channelName, ignoreCase = true)
        }
        if (matchedChannel != null) {
            playLiveChannel(matchedChannel)
        }
    }

    // --- Developer Login Bottom Sheet & Control Panel ---

    fun openDeveloperLoginSheet() {
        if (!com.example.ui.screens.SHOW_DEVELOPER_PANEL_ENTRY) return
        if (firebaseRepository.isDeveloperLoggedIn) {
            _uiState.update {
                it.copy(
                    isDevLoginSheetOpen = false,
                    isDeveloperLoggedIn = true,
                    developerEmail = firebaseRepository.currentDeveloperEmail,
                    isDeveloperPanelOpen = true,
                    devStatusMessage = null
                )
            }
            refreshDeveloperUserMessages()
            refreshOnlineUsersCount()
        } else {
            _uiState.update {
                it.copy(
                    isDevLoginSheetOpen = true,
                    devLoginError = null
                )
            }
        }
    }

    fun closeDeveloperLoginSheet() {
        _uiState.update {
            it.copy(
                isDevLoginSheetOpen = false,
                devLoginError = null,
                isLoggingInDeveloper = false
            )
        }
    }

    fun loginDeveloper(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingInDeveloper = true, devLoginError = null) }
            val res = firebaseRepository.loginDeveloper(email, password)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoggingInDeveloper = false,
                            isDevLoginSheetOpen = false,
                            isDeveloperLoggedIn = true,
                            developerEmail = firebaseRepository.currentDeveloperEmail,
                            isDeveloperPanelOpen = true,
                            devLoginError = null,
                            devStatusMessage = "تم تسجيل دخول المطور بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                    refreshDeveloperUserMessages()
                    refreshOnlineUsersCount()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoggingInDeveloper = false,
                            devLoginError = err.message ?: "فشل التحقق من بيانات المطور"
                        )
                    }
                }
            )
        }
    }


    fun closeDeveloperPanel() {
        _uiState.update {
            it.copy(
                isDeveloperPanelOpen = false,
                devStatusMessage = null
            )
        }
    }

    fun logoutDeveloper() {
        firebaseRepository.logoutDeveloper()
        _uiState.update {
            it.copy(
                isDeveloperLoggedIn = false,
                developerEmail = "",
                isDeveloperPanelOpen = false,
                devStatusMessage = null
            )
        }
    }

    fun clearDevStatusMessage() {
        _uiState.update { it.copy(devStatusMessage = null) }
    }

    // 1. Branches Management (Create / Edit / Delete / Reorder Up & Down + Save)

    fun saveDeveloperBranch(
        existingId: String?,
        name: String,
        imageUrl: String,
        parentId: String
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال اسم الفرع") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val currentList = _uiState.value.categories
            val existingBranch = existingId?.let { id -> currentList.find { it.id == id } }
            val branch = ChannelCategory(
                id = existingBranch?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                imageUrl = imageUrl.trim(),
                parentId = parentId.trim(),
                order = existingBranch?.order ?: (currentList.size + 1)
            )
            val res = firebaseRepository.saveBranch(branch)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = if (existingBranch != null) "تم تعديل الفرع وحفظه بنجاح" else "تم نشر الفرع بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حفظ الفرع"
                        )
                    }
                }
            )
        }
    }

    fun moveBranchInOrder(branchId: String, moveUp: Boolean) {
        val current = _uiState.value.categories.sortedBy { it.order }.toMutableList()
        val idx = current.indexOfFirst { it.id == branchId }
        if (idx == -1) return
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (targetIdx !in current.indices) return

        val item = current.removeAt(idx)
        current.add(targetIdx, item)
        val reordered = current.mapIndexed { index, cat -> cat.copy(order = index + 1) }
        _uiState.update { it.copy(categories = reordered) }
    }

    fun saveCurrentBranchesOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val ordered = _uiState.value.categories.sortedBy { it.order }
            val res = firebaseRepository.saveBranchesOrder(ordered)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حفظ ترتيب ظهور الأفرع بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حفظ ترتيب الأفرع"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperBranch(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deleteBranch(branchId)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف الفرع بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حذف الفرع"
                        )
                    }
                }
            )
        }
    }

    // 1.B Developer "نشر فرع حساب" (Fetch Xtream Live Categories & Streams -> Select & Reorder -> Publish Branch)

    fun fetchDeveloperAccountLiveCatalog(
        host: String,
        username: String,
        password: String
    ) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(
                    accountBranchFetchError = "يرجى إدخال الهوست واسم المستخدم وكلمة المرور لجلب تصنيفات وقنوات الحساب",
                    devStatusMessage = "يرجى إدخال الهوست واسم المستخدم وكلمة المرور"
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetchingAccountBranchCatalog = true,
                    accountBranchFetchError = null,
                    devStatusMessage = null
                )
            }
            val res = xtreamRepository.fetchLiveAccountCategoriesAndStreams(
                rawHost = host,
                rawUsername = username,
                rawPassword = password
            )
            res.fold(
                onSuccess = { catalog ->
                    _uiState.update {
                        it.copy(
                            isFetchingAccountBranchCatalog = false,
                            accountBranchFetchError = null,
                            fetchedAccountLiveCatalog = catalog,
                            devStatusMessage = "تم جلب (${catalog.categories.size} تصنيف) و(${catalog.streams.size} قناة مباشرة) من الحساب بنجاح"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isFetchingAccountBranchCatalog = false,
                            accountBranchFetchError = err.message ?: "تعذر جلب تصنيفات وقنوات الحساب",
                            devStatusMessage = err.message ?: "تعذر جلب تصنيفات وقنوات الحساب"
                        )
                    }
                }
            )
        }
    }

    fun fetchDeveloperAccountCategoryStreams(
        host: String,
        username: String,
        password: String,
        categoryId: String,
        categoryName: String
    ) {
        if (host.isBlank() || username.isBlank() || password.isBlank() || categoryId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFetchingAccountCategoryStreams = true,
                    accountBranchFetchError = null
                )
            }
            val res = xtreamRepository.fetchLiveStreamsForCategory(
                rawHost = host,
                rawUsername = username,
                rawPassword = password,
                categoryId = categoryId,
                categoryName = categoryName
            )
            res.fold(
                onSuccess = { categoryStreams ->
                    _uiState.update { state ->
                        val existingCatalog = state.fetchedAccountLiveCatalog
                        val mergedStreams = if (existingCatalog != null) {
                            val byId = existingCatalog.streams.associateBy { it.streamId }.toMutableMap()
                            for (s in categoryStreams) {
                                byId[s.streamId] = s
                            }
                            byId.values.toList()
                        } else {
                            categoryStreams
                        }
                        val updatedCategories = existingCatalog?.categories?.map { cat ->
                            if (cat.categoryId == categoryId && categoryStreams.isNotEmpty()) {
                                cat.copy(channelCount = maxOf(cat.channelCount, categoryStreams.size))
                            } else {
                                cat
                            }
                        } ?: listOf(
                            XtreamLiveCategory(
                                categoryId = categoryId,
                                categoryName = categoryName,
                                channelCount = categoryStreams.size
                            )
                        )
                        val cleanHost = if (host.trim().startsWith("http://") || host.trim().startsWith("https://")) {
                            host.trim().removeSuffix("/")
                        } else {
                            "http://${host.trim().removeSuffix("/")}"
                        }
                        state.copy(
                            isFetchingAccountCategoryStreams = false,
                            accountBranchFetchError = null,
                            fetchedAccountLiveCatalog = XtreamAccountLiveCatalog(
                                host = existingCatalog?.host ?: cleanHost,
                                username = existingCatalog?.username ?: username.trim(),
                                password = existingCatalog?.password ?: password.trim(),
                                categories = updatedCategories,
                                streams = mergedStreams
                            ),
                            devStatusMessage = "تم جلب محتوى تصنيف ($categoryName) بعدد (${categoryStreams.size} قناة)"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isFetchingAccountCategoryStreams = false,
                            accountBranchFetchError = err.message ?: "تعذر جلب قنوات التصنيف المختار"
                        )
                    }
                }
            )
        }
    }

    fun clearDeveloperAccountLiveCatalog() {
        _uiState.update {
            it.copy(
                fetchedAccountLiveCatalog = null,
                accountBranchFetchError = null,
                isFetchingAccountBranchCatalog = false,
                isFetchingAccountCategoryStreams = false
            )
        }
    }

    fun publishDeveloperAccountBranch(
        existingBranchId: String?,
        branchName: String,
        branchImageUrl: String,
        parentBranchId: String,
        orderedSelectedStreams: List<XtreamLiveStreamItem>
    ) {
        if (branchName.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال اسم الفرع قبل النشر") }
            return
        }
        if (orderedSelectedStreams.isEmpty()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى اختيار قناة واحدة على الأقل لنشرها داخل الفرع") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val currentBranches = _uiState.value.categories
            val existingBranch = existingBranchId?.takeIf { it.isNotBlank() }?.let { id ->
                currentBranches.find { it.id == id }
            }
            val finalBranchId = existingBranch?.id ?: UUID.randomUUID().toString()
            val cleanBranchName = branchName.trim()
            val cleanBranchImage = branchImageUrl.trim().ifBlank {
                orderedSelectedStreams.firstOrNull { it.streamIcon.isNotBlank() }?.streamIcon.orEmpty()
            }
            val branch = ChannelCategory(
                id = finalBranchId,
                name = cleanBranchName,
                imageUrl = cleanBranchImage,
                parentId = parentBranchId.trim(),
                order = existingBranch?.order ?: (currentBranches.size + 1)
            )
            val channelsToPublish = orderedSelectedStreams.mapIndexed { idx, streamItem ->
                streamItem.toLiveChannel(
                    branchId = finalBranchId,
                    branchName = cleanBranchName,
                    orderIndex = idx + 1
                )
            }
            val res = firebaseRepository.saveAccountBranchWithChannels(
                branch = branch,
                selectedChannels = channelsToPublish
            )
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم نشر فرع الحساب ($cleanBranchName) مع (${channelsToPublish.size} قناة مرتبة) في التطبيق بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر نشر فرع الحساب والقنوات"
                        )
                    }
                }
            )
        }
    }

    // 2. Channels & Multi-Server Management (m3u8 / ts)

    fun saveDeveloperChannel(
        existingId: String?,
        name: String,
        imageUrl: String,
        targetBranch: ChannelCategory?,
        servers: List<ChannelServer>
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال اسم القناة") }
            return
        }
        if (targetBranch == null) {
            _uiState.update { it.copy(devStatusMessage = "يرجى اختيار الفرع لنشر القناة داخله") }
            return
        }
        val validServers = servers.filter { it.streamUrl.isNotBlank() }
        if (validServers.isEmpty()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إضافة سيرفر بث واحد على الأقل (m3u8 أو ts)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val channel = LiveChannel(
                id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                name = name.trim(),
                category = targetBranch.name,
                branchId = targetBranch.id,
                imageUrl = imageUrl.trim(),
                servers = validServers
            )
            val res = firebaseRepository.saveChannel(channel)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = if (existingId != null) "تم تعديل القناة وحفظها بنجاح" else "تم نشر القناة داخل الفرع بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حفظ القناة"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperChannel(channelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deleteChannel(channelId)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف القناة بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حذف القناة"
                        )
                    }
                }
            )
        }
    }

    // 3. Xtream Codes Accounts Management (Add / Edit / Delete + Instant Library Refresh)

    fun saveDeveloperXtreamAccount(
        existingId: String?,
        name: String,
        host: String,
        username: String,
        password: String
    ) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال الهوست واسم المستخدم وكلمة المرور") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val normalizedHost = if (host.trim().startsWith("http://") || host.trim().startsWith("https://")) {
                host.trim().removeSuffix("/")
            } else {
                "http://${host.trim().removeSuffix("/")}"
            }
            val account = XtreamAccount(
                id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                name = name.trim().ifBlank { "حساب مكتبة Xtreme" },
                host = normalizedHost,
                username = username.trim(),
                password = password.trim(),
                streamExtension = "m3u8"
            )
            val res = firebaseRepository.saveXtreamAccount(account)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = if (existingId != null) "تم تعديل حساب Xtreme وتحديث المكتبة" else "تم نشر حساب Xtreme وتفعيل المكتبة بنجاح"
                        )
                    }
                    xtreamRepository.resetCatalog()
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حفظ حساب Xtreme"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperXtreamAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deleteXtreamAccount(accountId)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف حساب Xtreme بنجاح"
                        )
                    }
                    xtreamRepository.resetCatalog()
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حذف حساب Xtreme"
                        )
                    }
                }
            )
        }
    }

    // 3.B IPTV Accounts Management ("نشر حساب IPTV")

    fun saveDeveloperIptvAccount(
        existingId: String?,
        name: String,
        host: String,
        username: String,
        password: String
    ) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال الهوست واسم المستخدم وكلمة المرور لحساب IPTV") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val normalizedHost = if (host.trim().startsWith("http://") || host.trim().startsWith("https://")) {
                host.trim().removeSuffix("/")
            } else {
                "http://${host.trim().removeSuffix("/")}"
            }
            val account = XtreamAccount(
                id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                name = name.trim().ifBlank { "حساب IPTV مباشر" },
                host = normalizedHost,
                username = username.trim(),
                password = password.trim(),
                streamExtension = "m3u8"
            )
            val res = firebaseRepository.saveIptvAccount(account)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = if (existingId != null) "تم تعديل حساب IPTV بنجاح" else "تم نشر حساب IPTV (${account.name}) وتفعيل قنواته بنجاح"
                        )
                    }
                    xtreamRepository.clearIptvCatalogCache()
                    loadHomeData(forceRefresh = true)
                    selectIptvAccount(account, forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حفظ حساب IPTV"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperIptvAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deleteIptvAccount(accountId)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف حساب IPTV بنجاح"
                        )
                    }
                    xtreamRepository.clearIptvCatalogCache()
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل حذف حساب IPTV"
                        )
                    }
                }
            )
        }
    }

    // 4. App Version & Forced Update Management

    fun saveDeveloperVersionConfig(
        requiredVersion: String,
        updateText: String,
        updateUrl: String,
        telegramUrl: String,
        lockOldVersions: Boolean
    ) {
        val cleanVersion = requiredVersion.trim().ifBlank { AppVersionConfig.APP_CURRENT_VERSION }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val config = AppVersionConfig(
                requiredVersion = cleanVersion,
                updateText = updateText.trim().ifBlank { "يرجى تحديث التطبيق إلى الإصدار الجديد للاستمرار." },
                updateUrl = updateUrl.trim(),
                telegramUrl = telegramUrl.trim().ifBlank { FirebaseRepository.DEFAULT_TELEGRAM_URL },
                lockOldVersions = lockOldVersions
            )
            val res = firebaseRepository.saveVersionConfig(config)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            versionConfig = config,
                            telegramUrl = config.telegramUrl,
                            devStatusMessage = "تم نشر إعدادات الإصدار ($cleanVersion) والتحديث بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "فشل نشر إعدادات الإصدار"
                        )
                    }
                }
            )
        }
    }

    // 5. Match Schedule Published Channels ("نشر قناة لجدول المباريات") Online Management (`foul-ddbb8`)

    fun saveDeveloperMatchPublishedChannel(
        item: MatchPublishedChannel,
        extraMainChannels: List<LiveChannel> = emptyList()
    ) {
        if (item.homeTeamName.isBlank() || item.awayTeamName.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى اختيار المباراة أو إدخال اسم الفريق الأول والفريق الثاني") }
            return
        }
        if (item.channelIds.isEmpty() && item.customChannels.isEmpty()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى اختيار قناة موجودة أو إضافة قناة جديدة للمباراة") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            // Also optionally publish any custom channel marked to be saved in main app channels
            for (ch in extraMainChannels) {
                firebaseRepository.saveChannel(ch)
            }
            val res = firebaseRepository.saveMatchPublishedChannel(item)
            res.fold(
                onSuccess = {
                    val totalCount = item.channelIds.size + item.customChannels.size
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم نشر ($totalCount قناة) لمباراة (${item.homeTeamName} ضد ${item.awayTeamName}) وتفعيل زر (شاهد الآن) في جدول المباريات بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر نشر قناة المباراة في قاعدة البيانات"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperMatchPublishedChannel(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deleteMatchPublishedChannel(id)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف نشر قناة المباراة بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر حذف نشر قناة المباراة"
                        )
                    }
                }
            )
        }
    }

    // 6. Published Events & Video Announcements ("نشر حدث") Online Management (`foul-ddbb8`)

    fun saveDeveloperPublishedEvent(event: PublishedEventItem) {
        if (event.eventType == "VIDEO" && event.videoUrl.isBlank()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى إدخال رابط الفيديو المباشر (mp4 أو mkv)") }
            return
        }
        if (event.eventType != "VIDEO" && event.channelIds.isEmpty() && event.customChannels.isEmpty()) {
            _uiState.update { it.copy(devStatusMessage = "يرجى اختيار قناة واحدة أو أكثر (أو إضافة قناة خاصة) لمشاهدة الحدث") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.savePublishedEvent(event)
            res.fold(
                onSuccess = {
                    val label = event.title.ifBlank {
                        if (event.eventType == "VIDEO") "فيديو حدث مباشر" else "حدث مباشر"
                    }
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم نشر الحدث ($label) في التطبيق مباشرة (Online) بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر نشر الحدث في قاعدة البيانات"
                        )
                    }
                }
            )
        }
    }

    fun deleteDeveloperPublishedEvent(eventId: String) {
        if (eventId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDevActionLoading = true, devStatusMessage = null) }
            val res = firebaseRepository.deletePublishedEvent(eventId)
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = "تم حذف الحدث المنشور من التطبيق بنجاح"
                        )
                    }
                    loadHomeData(forceRefresh = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDevActionLoading = false,
                            devStatusMessage = err.message ?: "تعذر حذف الحدث المنشور"
                        )
                    }
                }
            )
        }
    }

    // --- Library (Xtream VOD & Series) ---

    fun loadLibraryInitial() {
        val accounts = _uiState.value.xtreamAccounts
        if (accounts.isEmpty()) {
            viewModelScope.launch {
                val fbRes = firebaseRepository.getFirebaseData(forceRefresh = false)
                val loadedAccounts = fbRes.getOrNull()?.xtreamAccounts.orEmpty()
                if (loadedAccounts.isNotEmpty()) {
                    _uiState.update { it.copy(xtreamAccounts = loadedAccounts) }
                    fetchLibraryFromAccounts(loadedAccounts)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingLibrary = false,
                            libraryError = "لا توجد حسابات مكتبة متاحة حالياً في قاعدة البيانات"
                        )
                    }
                }
            }
        } else {
            fetchLibraryFromAccounts(accounts)
        }
    }

    private fun fetchLibraryFromAccounts(accounts: List<XtreamAccount>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLibrary = true, libraryError = null) }
            val res = xtreamRepository.ensureInitialCatalogLoaded(
                accounts = accounts,
                onBackgroundCatalogUpdated = {
                    refreshLibraryPaginationState()
                    if (_uiState.value.isLibrarySearchOpen) {
                        executeLibrarySearch(_uiState.value.searchQuery, _uiState.value.searchFilter)
                    }
                }
            )
            res.fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isLoadingLibrary = false, libraryError = null) }
                    refreshLibraryPaginationState()
                },
                onFailure = { err ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingLibrary = false,
                            libraryError = err.message ?: "تعذر جلب الأفلام والمسلسلات"
                        )
                    }
                }
            )
        }
    }

    fun selectLibraryMainFilter(filter: LibraryFilterType) {
        _uiState.update {
            it.copy(
                libraryMainFilter = filter,
                libraryVisibleCount = XtreamRepository.PAGE_SIZE
            )
        }
        refreshLibraryPaginationState()
    }

    fun loadMoreLibraryItems() {
        _uiState.update { state ->
            state.copy(libraryVisibleCount = state.libraryVisibleCount + XtreamRepository.PAGE_SIZE)
        }
        refreshLibraryPaginationState()
    }

    private fun refreshLibraryPaginationState() {
        val state = _uiState.value
        val (visible, hasMore) = xtreamRepository.getCatalogSlice(
            filter = state.libraryMainFilter,
            limit = state.libraryVisibleCount
        )
        // Avoid redundant UI state emissions during background catalog indexing when the visible page hasn't changed
        if (visible.size != state.libraryDisplayedItems.size ||
            hasMore != state.libraryHasMore ||
            visible.firstOrNull()?.id != state.libraryDisplayedItems.firstOrNull()?.id
        ) {
            _uiState.update {
                it.copy(
                    libraryDisplayedItems = visible,
                    libraryHasMore = hasMore
                )
            }
        }
    }

    // --- Library Dedicated Search Page ---

    fun openLibrarySearch() {
        val initialResults = xtreamRepository.searchCatalog("", LibraryFilterType.ALL, limit = 120)
        _uiState.update {
            it.copy(
                isLibrarySearchOpen = true,
                searchQuery = "",
                searchFilter = LibraryFilterType.ALL,
                searchResults = initialResults,
                isSearching = false
            )
        }
        // Ensure full catalog is indexed for comprehensive search
        executeLibrarySearch("", LibraryFilterType.ALL)
    }

    fun closeLibrarySearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                isLibrarySearchOpen = false,
                searchQuery = "",
                isSearching = false
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        executeLibrarySearch(query, _uiState.value.searchFilter)
    }

    fun updateSearchFilter(filter: LibraryFilterType) {
        _uiState.update { it.copy(searchFilter = filter) }
        executeLibrarySearch(_uiState.value.searchQuery, filter)
    }

    private fun executeLibrarySearch(query: String, filter: LibraryFilterType) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Instant zero-latency search on everything already indexed in memory
            val instantMatches = xtreamRepository.searchCatalog(query, filter, limit = 600)
            _uiState.update {
                it.copy(
                    searchResults = instantMatches,
                    isSearching = instantMatches.isEmpty() && query.isNotBlank()
                )
            }
            // Comprehensive search across 100% of all server movies & series (never limited to displayed page)
            val accounts = _uiState.value.xtreamAccounts
            val fullResults = xtreamRepository.searchCatalogComprehensive(
                query = query,
                filter = filter,
                accounts = accounts,
                onResultsUpdated = { updatedList ->
                    if (_uiState.value.isLibrarySearchOpen &&
                        _uiState.value.searchQuery == query &&
                        _uiState.value.searchFilter == filter
                    ) {
                        _uiState.update {
                            it.copy(
                                searchResults = updatedList,
                                isSearching = false
                            )
                        }
                    }
                }
            )
            _uiState.update {
                it.copy(
                    searchResults = fullResults,
                    isSearching = false
                )
            }
        }
    }

    // --- Library Media Detail ---

    fun openMediaDetail(item: LibraryMediaItem) {
        val currentState = _uiState.value
        val showTransitionScreen = if (currentState.showAdsInApp &&
            nativeAdManager.isAdsEnabled &&
            !currentState.isAdTransitionScreenVisible &&
            currentState.activePlayback == null
        ) {
            nativeAdManager.beginTransitionBeforePlayback(contentTitle = item.name)
        } else {
            false
        }
        _uiState.update {
            it.copy(
                isAdTransitionScreenVisible = showTransitionScreen || it.isAdTransitionScreenVisible,
                selectedMediaItem = item,
                pendingMediaDetailAfterAd = if (showTransitionScreen) item else it.pendingMediaDetailAfterAd,
                isLoadingMediaDetail = true,
                mediaDetailError = null,
                mediaDetail = null,
                selectedSeasonNumber = 1
            )
        }
        viewModelScope.launch {
            val res = xtreamRepository.getMediaDetails(item)
            res.fold(
                onSuccess = { detail ->
                    val firstSeason = detail.seasons.firstOrNull()?.seasonNumber
                        ?: detail.episodesBySeason.keys.firstOrNull()
                        ?: 1
                    _uiState.update {
                        it.copy(
                            isLoadingMediaDetail = false,
                            mediaDetail = detail,
                            selectedSeasonNumber = firstSeason
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingMediaDetail = false,
                            mediaDetailError = err.message ?: "تعذر جلب تفاصيل العمل"
                        )
                    }
                }
            )
        }
    }

    fun selectSeason(seasonNum: Int) {
        _uiState.update { it.copy(selectedSeasonNumber = seasonNum) }
    }

    fun closeMediaDetail() {
        _uiState.update {
            it.copy(
                isAdTransitionScreenVisible = false,
                selectedMediaItem = null,
                pendingMediaDetailAfterAd = null,
                mediaDetail = null,
                mediaDetailError = null
            )
        }
    }

    fun playLibraryMovie(item: LibraryMediaItem) {
        val candidates = xtreamRepository.buildLibraryStreamCandidates(
            account = item.account,
            streamId = item.id,
            isSeries = false,
            preferredExtension = item.containerExtension
        )
        val request = PlaybackRequest(
            title = item.name,
            subtitle = if (item.releaseYear.isNotEmpty()) "فيلم • ${item.releaseYear}" else "فيلم",
            isLiveChannel = false,
            candidateUrls = candidates
        )
        requestPlaybackWithTransitionalAd(request)
    }

    fun playLibraryEpisode(seriesItem: LibraryMediaItem, episode: SeriesEpisode) {
        val candidates = xtreamRepository.buildLibraryStreamCandidates(
            account = seriesItem.account,
            streamId = episode.id,
            isSeries = true,
            preferredExtension = episode.containerExtension
        )
        val request = PlaybackRequest(
            title = seriesItem.name,
            subtitle = "الموسم ${episode.seasonNum} • الحلقة ${episode.episodeNum} - ${episode.title}",
            isLiveChannel = false,
            candidateUrls = candidates
        )
        requestPlaybackWithTransitionalAd(request)
    }

    fun closePlayer() {
        val closedChannelId = _uiState.value.activePlayback?.channelId.orEmpty()
        _uiState.update {
            it.copy(
                isAdTransitionScreenVisible = false,
                activePlayback = null,
                pendingPlaybackAfterAd = null,
                pendingMediaDetailAfterAd = null
            )
        }
        if (closedChannelId.isNotBlank()) {
            viewModelScope.launch {
                val baseViews = _uiState.value.channels.find { it.id == closedChannelId }?.views ?: 0
                socialRepository.incrementChannelView(closedChannelId, baseViews)
            }
        }
        nativeAdManager.preloadNextAd(reason = "PLAYER_CLOSED")
    }

    // --- Matches Schedule & Details (365Scores) ---

    fun loadMatchesForDate(dateString: String) {
        _uiState.update {
            it.copy(
                selectedDateString = dateString,
                isLoadingMatches = true,
                matchesError = null
            )
        }
        viewModelScope.launch {
            val res = scoresRepository.getMatchesByDate(dateString)
            res.fold(
                onSuccess = { groups ->
                    _uiState.update {
                        it.copy(
                            isLoadingMatches = false,
                            leagueGroups = groups,
                            matchesError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingMatches = false,
                            matchesError = err.message ?: "تعذر جلب جدول المباريات"
                        )
                    }
                }
            )
        }
    }

    fun openMatchDetail(match: MatchItem) {
        _uiState.update {
            it.copy(
                selectedMatch = match,
                isLoadingMatchDetail = true,
                matchDetailError = null,
                matchDetail = null
            )
        }
        viewModelScope.launch {
            val res = scoresRepository.getMatchFullDetails(match)
            res.fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            isLoadingMatchDetail = false,
                            matchDetail = detail,
                            matchDetailError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingMatchDetail = false,
                            matchDetailError = err.message ?: "تعذر جلب تفاصيل المباراة"
                        )
                    }
                }
            )
        }
    }

    fun closeMatchDetail() {
        _uiState.update {
            it.copy(
                selectedMatch = null,
                matchDetail = null,
                matchDetailError = null
            )
        }
    }

    // --- Developer Online App Control: Toggle Channel Views/Likes & Toggle All App Ads ---

    fun toggleShowChannelViewsAndLikesOnline(enabled: Boolean) {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                showChannelViewsAndLikes = enabled,
                isDevActionLoading = true,
                devStatusMessage = null
            )
        }
        viewModelScope.launch {
            val currentAds = _uiState.value.showAdsInApp
            val primaryRes = firebaseRepository.saveAppControlConfig(
                showChannelViewsAndLikes = enabled,
                showAdsInApp = currentAds,
                updatedAt = now
            )
            val secondaryRes = socialRepository.setShowChannelViewsAndLikesOnline(
                enabled = enabled,
                now = now
            )
            if (primaryRes.isSuccess || secondaryRes.getOrDefault(false)) {
                _uiState.update {
                    it.copy(
                        isDevActionLoading = false,
                        showChannelViewsAndLikes = enabled,
                        devStatusMessage = if (enabled) {
                            "تم إظهار عداد المشاهدات والإعجابات في جميع القنوات مباشرة لجميع المستخدمين (Online)"
                        } else {
                            "تم إخفاء عداد المشاهدات والإعجابات من جميع القنوات مباشرة لجميع المستخدمين (Online)"
                        }
                    )
                }
            } else {
                val errMsg = primaryRes.exceptionOrNull()?.message
                    ?: secondaryRes.exceptionOrNull()?.message
                    ?: "تعذر حفظ إعداد المشاهدات واللايكات في قاعدة البيانات"
                _uiState.update {
                    it.copy(
                        isDevActionLoading = false,
                        devStatusMessage = errMsg
                    )
                }
            }
        }
    }

    fun toggleShowAdsInAppOnline(enabled: Boolean) {
        val now = System.currentTimeMillis()
        // Apply immediately in memory so hiding/showing ads happens with zero latency
        nativeAdManager.setAdsEnabled(enabled)
        _uiState.update { state ->
            val pendingPlayback = if (!enabled && state.isAdTransitionScreenVisible) {
                state.pendingPlaybackAfterAd
            } else {
                null
            }
            state.copy(
                showAdsInApp = enabled,
                isAdTransitionScreenVisible = if (!enabled) false else state.isAdTransitionScreenVisible,
                activePlayback = pendingPlayback ?: state.activePlayback,
                pendingPlaybackAfterAd = if (!enabled) null else state.pendingPlaybackAfterAd,
                pendingMediaDetailAfterAd = if (!enabled) null else state.pendingMediaDetailAfterAd,
                isDevActionLoading = true,
                devStatusMessage = null
            )
        }
        viewModelScope.launch {
            val currentViews = _uiState.value.showChannelViewsAndLikes
            val primaryRes = firebaseRepository.saveAppControlConfig(
                showChannelViewsAndLikes = currentViews,
                showAdsInApp = enabled,
                updatedAt = now
            )
            val secondaryRes = socialRepository.setShowAdsInAppOnline(
                enabled = enabled,
                now = now
            )
            if (primaryRes.isSuccess || secondaryRes.getOrDefault(false)) {
                _uiState.update {
                    it.copy(
                        isDevActionLoading = false,
                        showAdsInApp = enabled,
                        devStatusMessage = if (enabled) {
                            "تم تفعيل وإظهار الإعلانات لجميع المستخدمين في قاعدة البيانات (Online)"
                        } else {
                            "تم إخفاء وإزالة جميع الإعلانات نهائياً لجميع المستخدمين في قاعدة البيانات (Online)"
                        }
                    )
                }
            } else {
                val errMsg = primaryRes.exceptionOrNull()?.message
                    ?: secondaryRes.exceptionOrNull()?.message
                    ?: "تعذر حفظ إعدادات الإعلانات في قاعدة البيانات"
                _uiState.update {
                    it.copy(
                        isDevActionLoading = false,
                        devStatusMessage = errMsg
                    )
                }
            }
        }
    }

    // --- Matches Search (Players & Teams) + Full Player & Team Profiles ---

    private var matchesSearchJob: Job? = null

    fun openMatchesSearch() {
        _uiState.update {
            it.copy(
                isMatchesSearchOpen = true,
                matchesSearchQuery = "",
                isSearchingMatches = false,
                matchesSearchResult = null
            )
        }
    }

    fun closeMatchesSearch() {
        matchesSearchJob?.cancel()
        _uiState.update {
            it.copy(
                isMatchesSearchOpen = false,
                matchesSearchQuery = "",
                isSearchingMatches = false,
                matchesSearchResult = null
            )
        }
    }

    fun updateMatchesSearchQuery(query: String) {
        _uiState.update { it.copy(matchesSearchQuery = query) }
        matchesSearchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    isSearchingMatches = false,
                    matchesSearchResult = null
                )
            }
            return
        }

        matchesSearchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearchingMatches = true) }
            delay(250)
            val res = scoresRepository.searchPlayersAndTeams(trimmed)
            res.fold(
                onSuccess = { searchRes ->
                    if (_uiState.value.matchesSearchQuery.trim() == trimmed) {
                        _uiState.update {
                            it.copy(
                                isSearchingMatches = false,
                                matchesSearchResult = searchRes
                            )
                        }
                    }
                },
                onFailure = {
                    if (_uiState.value.matchesSearchQuery.trim() == trimmed) {
                        _uiState.update {
                            it.copy(
                                isSearchingMatches = false,
                                matchesSearchResult = MatchesSearchResult(query = trimmed)
                            )
                        }
                    }
                }
            )
        }
    }

    fun openPlayerProfileFromLineup(
        player: LineupPlayer,
        teamId: Int,
        teamName: String,
        teamLogoUrl: String
    ) {
        val initialProfile = PlayerFullProfile(
            id = player.id,
            athleteId = player.athleteId,
            name = player.name,
            shortName = player.shortName,
            photoUrl = player.photoUrl,
            positionName = player.positionName,
            jerseyNumber = player.jerseyNumber,
            nationality = "",
            currentTeamId = teamId,
            currentTeamName = teamName,
            currentTeamLogoUrl = teamLogoUrl
        )
        _uiState.update {
            it.copy(
                selectedPlayerProfile = initialProfile,
                isLoadingPlayerProfile = true,
                playerProfileError = null
            )
        }
        viewModelScope.launch {
            val res = scoresRepository.getPlayerFullProfile(
                athleteId = player.athleteId,
                playerId = player.id,
                fallbackName = player.name,
                fallbackPhotoUrl = player.photoUrl,
                fallbackPosition = player.positionName,
                fallbackJersey = player.jerseyNumber,
                fallbackTeamId = teamId,
                fallbackTeamName = teamName,
                fallbackTeamLogoUrl = teamLogoUrl
            )
            res.fold(
                onSuccess = { fullProfile ->
                    _uiState.update {
                        it.copy(
                            selectedPlayerProfile = fullProfile,
                            isLoadingPlayerProfile = false,
                            playerProfileError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingPlayerProfile = false,
                            playerProfileError = err.message
                        )
                    }
                }
            )
        }
    }

    fun openPlayerProfileFromSearch(player: PlayerFullProfile) {
        _uiState.update {
            it.copy(
                selectedPlayerProfile = player,
                isLoadingPlayerProfile = true,
                playerProfileError = null
            )
        }
        viewModelScope.launch {
            val res = scoresRepository.getPlayerFullProfile(
                athleteId = player.athleteId,
                playerId = player.id,
                fallbackName = player.name,
                fallbackPhotoUrl = player.photoUrl,
                fallbackPosition = player.positionName,
                fallbackJersey = player.jerseyNumber,
                fallbackTeamId = player.currentTeamId,
                fallbackTeamName = player.currentTeamName,
                fallbackTeamLogoUrl = player.currentTeamLogoUrl
            )
            res.fold(
                onSuccess = { fullProfile ->
                    _uiState.update {
                        it.copy(
                            selectedPlayerProfile = fullProfile,
                            isLoadingPlayerProfile = false,
                            playerProfileError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingPlayerProfile = false,
                            playerProfileError = err.message
                        )
                    }
                }
            )
        }
    }

    fun closePlayerProfile() {
        _uiState.update {
            it.copy(
                selectedPlayerProfile = null,
                isLoadingPlayerProfile = false,
                playerProfileError = null
            )
        }
    }

    fun openTeamProfile(team: MatchCompetitor) {
        _uiState.update {
            it.copy(
                selectedTeamProfile = null,
                isLoadingTeamProfile = true,
                teamProfileError = null
            )
        }
        viewModelScope.launch {
            val res = scoresRepository.getTeamFullProfile(
                teamId = team.id,
                fallbackName = team.name,
                fallbackLogoUrl = team.logoUrl
            )
            res.fold(
                onSuccess = { fullTeam ->
                    _uiState.update {
                        it.copy(
                            selectedTeamProfile = fullTeam,
                            isLoadingTeamProfile = false,
                            teamProfileError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingTeamProfile = false,
                            teamProfileError = err.message ?: "تعذر جلب معلومات الفريق"
                        )
                    }
                }
            )
        }
    }

    fun closeTeamProfile() {
        _uiState.update {
            it.copy(
                selectedTeamProfile = null,
                isLoadingTeamProfile = false,
                teamProfileError = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        nativeAdManager.releaseAll()
    }
}
