package com.example.data.model

import androidx.compose.runtime.Immutable

enum class BottomNavTab {
    HOME,
    IPTV,
    LIBRARY,
    MATCHES,
    SETTINGS
}

enum class LibraryFilterType(val labelAr: String) {
    ALL("الكل"),
    MOVIES("الأفلام"),
    SERIES("المسلسلات")
}

// --- Firebase Realtime Database Models ---

/**
 * Represents a Branch (فرع رئيسي) or Sub-Branch (فرع داخل فرع) on the Home screen.
 */
@Immutable
data class ChannelCategory(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val parentId: String = "",
    val order: Int = 0
)

@Immutable
data class ChannelServer(
    val id: String,
    val name: String,
    val streamType: String, // "m3u8" or "ts"
    val streamUrl: String
)

@Immutable
data class LiveChannel(
    val id: String,
    val name: String,
    val category: String,
    val branchId: String = "",
    val imageUrl: String,
    val servers: List<ChannelServer>,
    val views: Int = 0,
    val order: Int = 0
)

@Immutable
data class XtreamLiveCategory(
    val categoryId: String,
    val categoryName: String,
    val channelCount: Int = 0
)

@Immutable
data class XtreamLiveStreamItem(
    val streamId: String,
    val name: String,
    val streamIcon: String,
    val categoryId: String,
    val categoryName: String,
    val host: String,
    val username: String,
    val password: String,
    val mergedServers: List<ChannelServer> = emptyList()
) {
    val cleanHost: String
        get() = host.trim().removeSuffix("/")

    fun toLiveChannel(
        branchId: String,
        branchName: String,
        orderIndex: Int = 0
    ): LiveChannel {
        val baseHost = cleanHost
        val cleanUser = username.trim()
        val cleanPass = password.trim()
        val servers = if (mergedServers.isNotEmpty()) {
            mergedServers
        } else {
            listOf(
                ChannelServer(
                    id = "xt_m3u8_${streamId}_1",
                    name = "سيرفر 1",
                    streamType = "m3u8",
                    streamUrl = "$baseHost/live/$cleanUser/$cleanPass/$streamId.m3u8"
                ),
                ChannelServer(
                    id = "xt_ts_${streamId}_2",
                    name = "سيرفر 2",
                    streamType = "ts",
                    streamUrl = "$baseHost/live/$cleanUser/$cleanPass/$streamId.ts"
                )
            )
        }
        return LiveChannel(
            id = "xtch_${branchId}_${streamId}",
            name = name.trim().ifBlank { "قناة $streamId" },
            category = branchName.trim(),
            branchId = branchId.trim(),
            imageUrl = streamIcon.trim(),
            servers = servers,
            views = 0,
            order = orderIndex
        )
    }
}

@Immutable
data class XtreamAccountLiveCatalog(
    val host: String,
    val username: String,
    val password: String,
    val categories: List<XtreamLiveCategory>,
    val streams: List<XtreamLiveStreamItem>
)

@Immutable
data class EventBanner(
    val id: String,
    val title: String,
    val imageUrl: String,
    val channelId: String,
    val channelName: String,
    val channelIds: List<String>,
    val channelNames: List<String>,
    val showWatchNow: Boolean,
    val watchNowUrl: String
)

@Immutable
data class LiveEventMatch(
    val id: String,
    val name: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val imageUrl: String,
    val broadcaster: String,
    val leagueName: String,
    val isLive: Boolean,
    val targetChannelId: String
)

@Immutable
data class XtreamAccount(
    val id: String,
    val name: String,
    val host: String,
    val username: String,
    val password: String,
    val streamExtension: String = "mp4"
) {
    val cleanHost: String
        get() = host.trim().removeSuffix("/")
}

@Immutable
data class AppVersionConfig(
    val requiredVersion: String = APP_CURRENT_VERSION,
    val updateText: String = "يرجى تحديث التطبيق إلى الإصدار الجديد للاستمرار في المشاهدة.",
    val updateUrl: String = "",
    val telegramUrl: String = "https://t.me/jdj_q",
    val lockOldVersions: Boolean = true
) {
    val isForcedUpdateLocked: Boolean
        get() {
            val cleanReq = requiredVersion.trim()
            if (cleanReq.isEmpty()) return false
            return lockOldVersions &&
                cleanReq != APP_CURRENT_VERSION &&
                cleanReq != "1.0" &&
                cleanReq != "1.0.0" &&
                cleanReq != "1.1" &&
                cleanReq != "1.1.0" &&
                cleanReq != "1.2" &&
                cleanReq != "1.2.0" &&
                cleanReq != "1.2.1"
        }

    companion object {
        const val APP_CURRENT_VERSION = "1.2.1"
    }
}

@Immutable
data class MatchPublishedChannel(
    val id: String,
    val matchId: Int = 0,
    val homeTeamName: String,
    val awayTeamName: String,
    val competitionName: String = "",
    val matchDate: String = "",
    val channelIds: List<String> = emptyList(),
    val customChannels: List<LiveChannel> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
data class PublishedEventItem(
    val id: String,
    val eventType: String = "CHANNELS", // "CHANNELS" or "VIDEO"
    val title: String = "",             // Optional event/video title
    val description: String = "",       // Optional event/video text
    val imageUrl: String = "",          // Optional or event image URL
    val showWatchNow: Boolean = true,
    val channelIds: List<String> = emptyList(),
    val customChannels: List<LiveChannel> = emptyList(),
    val videoUrl: String = "",          // Direct mp4 / mkv / stream URL when eventType == "VIDEO"
    val createdAt: Long = System.currentTimeMillis()
)

data class FirebaseHomePayload(
    val categories: List<ChannelCategory>,
    val channels: List<LiveChannel>,
    val banners: List<EventBanner>,
    val liveMatches: List<LiveEventMatch>,
    val xtreamAccounts: List<XtreamAccount>,
    val iptvAccounts: List<XtreamAccount> = emptyList(),
    val telegramUrl: String,
    val versionConfig: AppVersionConfig,
    val matchPublishedChannels: List<MatchPublishedChannel> = emptyList(),
    val publishedEvents: List<PublishedEventItem> = emptyList(),
    val showChannelViewsAndLikes: Boolean? = null,
    val showAdsInApp: Boolean? = null,
    val appControlUpdatedAt: Long = 0L
)

// --- Library (Xtream Codes) Models ---

@Immutable
data class LibraryMediaItem(
    val id: String,
    val name: String,
    val lowerName: String,
    val posterUrl: String,
    val rating: String,
    val releaseYear: String,
    val isSeries: Boolean,
    val containerExtension: String,
    val categoryId: String,
    val plotPreview: String = "",
    val account: XtreamAccount
)

@Immutable
data class SeriesSeason(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val coverUrl: String,
    val overview: String = ""
)

@Immutable
data class SeriesEpisode(
    val id: String,
    val episodeNum: Int,
    val seasonNum: Int,
    val title: String,
    val plot: String,
    val duration: String,
    val rating: String,
    val releaseDate: String,
    val containerExtension: String,
    val coverUrl: String
)

@Immutable
data class MediaDetailInfo(
    val item: LibraryMediaItem,
    val description: String,
    val genre: String,
    val releaseDate: String,
    val cast: String,
    val director: String,
    val duration: String,
    val rating: String,
    val backdropUrl: String,
    val seasons: List<SeriesSeason> = emptyList(),
    val episodesBySeason: Map<Int, List<SeriesEpisode>> = emptyMap()
)

@Immutable
data class PlayerGuideCategory(
    val id: String,
    val name: String,
    val channelCount: Int = 0
)

@Immutable
data class PlayerGuideChannel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val categoryId: String,
    val categoryName: String,
    val candidateUrls: List<String>,
    val servers: List<ChannelServer>
)

@Immutable
data class PlaybackRequest(
    val title: String,
    val subtitle: String = "",
    val isLiveChannel: Boolean,
    val candidateUrls: List<String>,
    val servers: List<ChannelServer> = emptyList(),
    val channelId: String = "",
    val activeCategoryId: String = "",
    val activeCategoryName: String = "",
    val isFromIptv: Boolean = false
)

// --- 365Scores Matches & Details Models ---

data class MatchCompetitor(
    val id: Int,
    val name: String,
    val symbolicName: String,
    val score: Int,
    val hasScore: Boolean,
    val imageVersion: Int,
    val color: String,
    val awayColor: String,
    val redCards: Int = 0
) {
    val logoUrl: String
        get() = "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v${imageVersion.coerceAtLeast(1)}/Competitors/$id"
}

data class MatchItem(
    val id: Int,
    val competitionId: Int,
    val competitionName: String,
    val competitionImageVersion: Int,
    val startTimeIso: String,
    val displayTime: String,
    val statusGroup: Int,
    val statusText: String,
    val gameTimeDisplay: String,
    val roundName: String,
    val homeTeam: MatchCompetitor,
    val awayTeam: MatchCompetitor
) {
    val isLive: Boolean get() = statusGroup == 3
    val isFinished: Boolean get() = statusGroup == 4
    val competitionLogoUrl: String
        get() = "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitions:default.png/v${competitionImageVersion.coerceAtLeast(1)}/Competitions/$competitionId"
}

data class LeagueMatchesGroup(
    val competitionId: Int,
    val competitionName: String,
    val countryId: Int,
    val imageVersion: Int,
    val color: String,
    val priorityRank: Int,
    val matches: List<MatchItem>
) {
    val logoUrl: String
        get() = "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitions:default.png/v${imageVersion.coerceAtLeast(1)}/Competitions/$competitionId"
}

data class MatchEventItem(
    val id: String,
    val order: Int,
    val gameTime: Int,
    val minuteDisplay: String,
    val competitorId: Int,
    val isHome: Boolean,
    val typeId: Int,
    val typeName: String,
    val subTypeName: String,
    val playerName: String,
    val secondaryPlayerName: String,
    val playerPhotoUrl: String
)

data class LineupPlayer(
    val id: Int,
    val athleteId: Int,
    val name: String,
    val shortName: String,
    val jerseyNumber: Int,
    val positionName: String,
    val fieldLine: Int,
    val fieldSide: Int,
    val yardLine: Int,
    val ranking: Double,
    val isStarting: Boolean,
    val imageVersion: Int
) {
    val photoUrl: String
        get() = if (athleteId > 0) {
            "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${imageVersion.coerceAtLeast(1)}/Athletes/$athleteId"
        } else {
            "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/Athletes/$id"
        }
}

data class TeamLineup(
    val competitorId: Int,
    val teamName: String,
    val teamColor: String,
    val formation: String,
    val statusText: String,
    val startingPlayers: List<LineupPlayer>,
    val substitutes: List<LineupPlayer>
)

data class MatchStatComparison(
    val statId: Int,
    val name: String,
    val homeValue: String,
    val awayValue: String,
    val homeRatio: Float,
    val awayRatio: Float,
    val isPossession: Boolean,
    val order: Int
)

data class StandingRowItem(
    val position: Int,
    val competitorId: Int,
    val teamName: String,
    val imageVersion: Int,
    val teamColor: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val points: Int,
    val isHomeMatchTeam: Boolean,
    val isAwayMatchTeam: Boolean
) {
    val logoUrl: String
        get() = "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v${imageVersion.coerceAtLeast(1)}/Competitors/$competitorId"
}

data class NewsArticleItem(
    val id: Long,
    val title: String,
    val publishDate: String,
    val formattedDate: String,
    val imageUrl: String,
    val url: String
)

data class MatchDetailData(
    val match: MatchItem,
    val events: List<MatchEventItem>,
    val homeLineup: TeamLineup?,
    val awayLineup: TeamLineup?,
    val stats: List<MatchStatComparison>,
    val standings: List<StandingRowItem>,
    val standingsLeagueName: String,
    val news: List<NewsArticleItem>,
    val venueName: String,
    val refereeName: String
)

// --- Secondary Firebase Database (foul-aeed9) Models: Channel Views/Likes, User Auth & Contact Us ---

data class ChannelEngagementStats(
    val channelId: String,
    val views: Int = 0,
    val likes: Int = 0,
    val isLikedByMe: Boolean = false
)

data class AppAvatarSpec(
    val id: String,
    val nameAr: String,
    val isBoy: Boolean,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val styleVariant: Int
)

data class SocialUserProfile(
    val uid: String,
    val email: String,
    val username: String,
    val avatarId: String
)

data class ContactMessageItem(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val senderEmail: String,
    val senderAvatarId: String,
    val text: String,
    val timestamp: Long,
    val isFromBotOrAdmin: Boolean = false,
    val targetUid: String = "",
    val replyToMessageId: String = "",
    val replyToSenderName: String = "",
    val replyToText: String = ""
)

// --- 365Scores Player Career, Full Statistics, Transfers & Team Search Models ---

data class PlayerTransferItem(
    val id: String,
    val playerName: String = "",
    val playerPhotoUrl: String = "",
    val fromTeamId: Int = 0,
    val fromTeamName: String,
    val fromTeamLogoUrl: String,
    val toTeamId: Int = 0,
    val toTeamName: String,
    val toTeamLogoUrl: String,
    val dateText: String,
    val transferTypeOrFee: String
)

data class PlayerLeagueCareerStat(
    val id: String,
    val competitionId: Int,
    val competitionName: String,
    val competitionLogoUrl: String,
    val seasonName: String,
    val teamName: String,
    val teamLogoUrl: String,
    val appearances: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val minutesPlayed: Int = 0,
    val rating: String = "-"
)

data class PlayerGeneralStatItem(
    val label: String,
    val value: String,
    val progressRatio: Float = 0.7f
)

data class PlayerFullProfile(
    val id: Int,
    val athleteId: Int,
    val name: String,
    val shortName: String,
    val photoUrl: String,
    val positionName: String,
    val jerseyNumber: Int,
    val nationality: String,
    val nationalityFlagUrl: String = "",
    val age: String = "",
    val birthDate: String = "",
    val height: String = "",
    val weight: String = "",
    val preferredFoot: String = "",
    val marketValue: String = "",
    val currentTeamId: Int = 0,
    val currentTeamName: String,
    val currentTeamLogoUrl: String,
    val transfers: List<PlayerTransferItem> = emptyList(),
    val leagueCareerStats: List<PlayerLeagueCareerStat> = emptyList(),
    val fullStatistics: List<PlayerGeneralStatItem> = emptyList(),
    val recentMatches: List<MatchItem> = emptyList()
)

data class TeamFullProfile(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val countryName: String,
    val venueName: String = "",
    val coachName: String = "",
    val primaryCompetitionName: String = "",
    val primaryCompetitionLogoUrl: String = "",
    val playedMatches: List<MatchItem> = emptyList(),
    val upcomingMatches: List<MatchItem> = emptyList(),
    val transfers: List<PlayerTransferItem> = emptyList(),
    val squadPlayers: List<PlayerFullProfile> = emptyList(),
    val standings: List<StandingRowItem> = emptyList()
)

data class MatchesSearchResult(
    val query: String,
    val players: List<PlayerFullProfile> = emptyList(),
    val teams: List<MatchCompetitor> = emptyList()
)



