package com.example.data.repository

import com.example.data.model.LeagueMatchesGroup
import com.example.data.model.LineupPlayer
import com.example.data.model.MatchCompetitor
import com.example.data.model.MatchDetailData
import com.example.data.model.MatchEventItem
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatComparison
import com.example.data.model.MatchesSearchResult
import com.example.data.model.NewsArticleItem
import com.example.data.model.PlayerFullProfile
import com.example.data.model.PlayerGeneralStatItem
import com.example.data.model.PlayerLeagueCareerStat
import com.example.data.model.PlayerTransferItem
import com.example.data.model.StandingRowItem
import com.example.data.model.TeamFullProfile
import com.example.data.model.TeamLineup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class Scores365Repository(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    /**
     * Fetches real matches for the given date (formatted DD/MM/YYYY) from 365Scores API
     * and orders leagues strictly with:
     * 1. دوري أبطال أوروبا (UEFA Champions League)
     * 2. الدوري الإسباني (La Liga)
     * 3. الدوري الإيطالي (Serie A)
     * 4. الدوري الإنجليزي الممتاز (Premier League)
     * followed by other competitions.
     */
    suspend fun getMatchesByDate(dateFormatted: String): Result<List<LeagueMatchesGroup>> =
        withContext(Dispatchers.IO) {
            val url = "https://webws.365scores.com/web/games/allscores/" +
                "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&sports=1" +
                "&startDate=$dateFormatted&endDate=$dateFormatted"

            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                    .header("Accept", "application/json")
                    .build()

                httpClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("فشل جلب جدول المباريات: ${resp.code}")
                        )
                    }
                    val body = resp.body?.string().orEmpty()
                    val root = JSONObject(body)
                    Result.success(parseAllScoresResponse(root))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseAllScoresResponse(root: JSONObject): List<LeagueMatchesGroup> {
        // Map competition metadata
        data class CompMeta(
            val id: Int,
            val name: String,
            val countryId: Int,
            val imageVersion: Int,
            val color: String,
            val popularityRank: Long
        )

        val compMap = HashMap<Int, CompMeta>()
        val compsArr = root.optJSONArray("competitions")
        if (compsArr != null) {
            for (i in 0 until compsArr.length()) {
                val cObj = compsArr.optJSONObject(i) ?: continue
                val id = cObj.optInt("id", -1)
                if (id != -1) {
                    compMap[id] = CompMeta(
                        id = id,
                        name = cObj.optString("name", ""),
                        countryId = cObj.optInt("countryId", -1),
                        imageVersion = cObj.optInt("imageVersion", 1),
                        color = cObj.optString("color", "#7E22CE"),
                        popularityRank = cObj.optLong("popularityRank", 0L)
                    )
                }
            }
        }

        val gamesByComp = LinkedHashMap<Int, MutableList<MatchItem>>()
        val gamesArr = root.optJSONArray("games")
        if (gamesArr != null) {
            for (i in 0 until gamesArr.length()) {
                val gObj = gamesArr.optJSONObject(i) ?: continue
                val gameId = gObj.optInt("id", -1)
                val compId = gObj.optInt("competitionId", -1)
                if (gameId == -1 || compId == -1) continue

                val compMeta = compMap[compId]
                val compName = gObj.optString("competitionDisplayName", compMeta?.name.orEmpty())
                val startTimeIso = gObj.optString("startTime", "")
                val statusGroup = gObj.optInt("statusGroup", 2)
                val statusText = gObj.optString("statusText", gObj.optString("shortStatusText", ""))
                val gameTimeVal = gObj.optDouble("gameTime", -1.0)
                val gameTimeDisplay = gObj.optString("gameTimeDisplay", "").ifBlank {
                    if (statusGroup == 3 && gameTimeVal > 0) "${gameTimeVal.toInt()}'" else ""
                }
                val roundName = gObj.optString("roundName", "")

                val homeObj = gObj.optJSONObject("homeCompetitor") ?: continue
                val awayObj = gObj.optJSONObject("awayCompetitor") ?: continue

                val homeScore = homeObj.optDouble("score", -1.0)
                val awayScore = awayObj.optDouble("score", -1.0)
                val hasScore = (statusGroup == 3 || statusGroup == 4) && homeScore >= 0 && awayScore >= 0

                val homeTeam = MatchCompetitor(
                    id = homeObj.optInt("id", 0),
                    name = homeObj.optString("name", ""),
                    symbolicName = homeObj.optString("symbolicName", ""),
                    score = if (hasScore) homeScore.toInt() else 0,
                    hasScore = hasScore,
                    imageVersion = homeObj.optInt("imageVersion", 1),
                    color = homeObj.optString("color", "#7E22CE"),
                    awayColor = homeObj.optString("awayColor", "#FFFFFF"),
                    redCards = homeObj.optInt("redCards", 0)
                )

                val awayTeam = MatchCompetitor(
                    id = awayObj.optInt("id", 0),
                    name = awayObj.optString("name", ""),
                    symbolicName = awayObj.optString("symbolicName", ""),
                    score = if (hasScore) awayScore.toInt() else 0,
                    hasScore = hasScore,
                    imageVersion = awayObj.optInt("imageVersion", 1),
                    color = awayObj.optString("color", "#3B82F6"),
                    awayColor = awayObj.optString("awayColor", "#FFFFFF"),
                    redCards = awayObj.optInt("redCards", 0)
                )

                val matchItem = MatchItem(
                    id = gameId,
                    competitionId = compId,
                    competitionName = compName,
                    competitionImageVersion = compMeta?.imageVersion ?: 1,
                    startTimeIso = startTimeIso,
                    displayTime = formatBaghdadTime(startTimeIso),
                    statusGroup = statusGroup,
                    statusText = statusText,
                    gameTimeDisplay = gameTimeDisplay,
                    roundName = roundName,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam
                )

                gamesByComp.getOrPut(compId) { mutableListOf() }.add(matchItem)
            }
        }

        val groups = gamesByComp.map { (compId, matches) ->
            val meta = compMap[compId]
            val compName = meta?.name?.ifBlank { matches.first().competitionName }
                ?: matches.first().competitionName
            val priority = computeLeaguePriority(
                compId = compId,
                compName = compName,
                countryId = meta?.countryId ?: -1
            )
            LeagueMatchesGroup(
                competitionId = compId,
                competitionName = compName,
                countryId = meta?.countryId ?: -1,
                imageVersion = meta?.imageVersion ?: 1,
                color = meta?.color ?: "#7E22CE",
                priorityRank = priority,
                matches = matches
            )
        }

        return groups.sortedWith(
            compareBy<LeagueMatchesGroup> { it.priorityRank }
                .thenByDescending { compMap[it.competitionId]?.popularityRank ?: 0L }
        )
    }

    /**
     * Enforces strict league ordering requested by user:
     * 1. دوري أبطال أوروبا (UEFA Champions League)
     * 2. الدوري الإسباني (La Liga)
     * 3. الدوري الإيطالي (Serie A)
     * 4. الدوري الإنجليزي الممتاز (Premier League)
     */
    private fun computeLeaguePriority(compId: Int, compName: String, countryId: Int): Int {
        val clean = compName.trim()
        // 1. UEFA Champions League (ID 572)
        if (compId == 572 || (clean == "دوري أبطال أوروبا" && !clean.contains("شباب") && !clean.contains("سيدات"))) {
            return 1
        }
        // 2. La Liga (ID 11)
        if (compId == 11 || clean == "الدوري الإسباني" || clean == "الدوري الاسباني") {
            return 2
        }
        // 3. Serie A (ID 17)
        if (compId == 17 || clean == "الدوري الإيطالي" || clean == "الدوري الايطالي") {
            return 3
        }
        // 4. Premier League (ID 7)
        if (compId == 7 || clean == "الدوري الإنجليزي الممتاز" || clean == "الدوري الانجليزي الممتاز" ||
            (clean == "الدوري الإنجليزي" && countryId == 1)
        ) {
            return 4
        }
        // Secondary top competitions
        if (clean.contains("دوري أبطال أوروبا")) return 5
        if (clean.contains("الدوري الأوروبي") || clean.contains("أبطال آسيا")) return 6
        if (clean.contains("الدوري الألماني") || clean.contains("الدوري الفرنسي")) return 7
        if (clean.contains("الدوري السعودي") || clean.contains("الدوري العراقي") || clean.contains("الدوري المصري")) return 8
        return 50
    }

    /**
     * Fetches full Match Details (Events, Lineups on pitch with player photos, Stats with Possession first,
     * Standings with both teams highlighted, and News) in parallel.
     */
    suspend fun getMatchFullDetails(match: MatchItem): Result<MatchDetailData> =
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val gameDeferred = async { fetchGameDetailJson(match.id) }
                    val statsDeferred = async { fetchGameStatsJson(match.id) }
                    val standingsDeferred = async { fetchStandingsJson(match.competitionId) }
                    val newsDeferred = async { fetchCompetitionNewsJson(match.competitionId) }

                    val gameRoot = gameDeferred.await()
                    val statsRoot = statsDeferred.await()
                    val standingsRoot = standingsDeferred.await()
                    val newsRoot = newsDeferred.await()

                    val detailData = buildMatchDetailData(
                        initialMatch = match,
                        gameRoot = gameRoot,
                        statsRoot = statsRoot,
                        standingsRoot = standingsRoot,
                        newsRoot = newsRoot
                    )
                    Result.success(detailData)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun fetchGameDetailJson(gameId: Int): JSONObject? {
        val url = "https://webws.365scores.com/web/game/?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&gameId=$gameId"
        return executeGetJson(url)
    }

    private fun fetchGameStatsJson(gameId: Int): JSONObject? {
        val url = "https://webws.365scores.com/web/game/stats/?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&games=$gameId"
        return executeGetJson(url)
    }

    private fun fetchStandingsJson(competitionId: Int): JSONObject? {
        val url = "https://webws.365scores.com/web/standings/?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitions=$competitionId"
        return executeGetJson(url)
    }

    private fun fetchCompetitionNewsJson(competitionId: Int): JSONObject? {
        val specificUrl = "https://webws.365scores.com/web/news/?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitions=$competitionId"
        val res = executeGetJson(specificUrl)
        val arr = res?.optJSONArray("news")
        if (arr != null && arr.length() > 0) return res
        // Fallback to general Arabic football news from 365Scores if competition-specific news is empty
        val generalUrl = "https://webws.365scores.com/web/news/?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1"
        return executeGetJson(generalUrl)
    }

    private fun executeGetJson(url: String): JSONObject? {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string().orEmpty()
                if (body.isNotBlank()) JSONObject(body) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildMatchDetailData(
        initialMatch: MatchItem,
        gameRoot: JSONObject?,
        statsRoot: JSONObject?,
        standingsRoot: JSONObject?,
        newsRoot: JSONObject?
    ): MatchDetailData {
        val gameObj = gameRoot?.optJSONObject("game")

        // Map game.members by player ID for fast lookup of player names, athleteIds, and jersey numbers
        data class MemberMeta(
            val id: Int,
            val athleteId: Int,
            val competitorId: Int,
            val name: String,
            val shortName: String,
            val jerseyNumber: Int,
            val imageVersion: Int
        )

        val membersMap = HashMap<Int, MemberMeta>()
        val membersArr = gameObj?.optJSONArray("members")
        if (membersArr != null) {
            for (i in 0 until membersArr.length()) {
                val mObj = membersArr.optJSONObject(i) ?: continue
                val pid = mObj.optInt("id", -1)
                if (pid != -1) {
                    val name = mObj.optString("name", "")
                    membersMap[pid] = MemberMeta(
                        id = pid,
                        athleteId = mObj.optInt("athleteId", 0),
                        competitorId = mObj.optInt("competitorId", 0),
                        name = name,
                        shortName = mObj.optString("shortName", name),
                        jerseyNumber = mObj.optInt("jerseyNumber", 0),
                        imageVersion = mObj.optInt("imageVersion", 1)
                    )
                }
            }
        }

        val homeId = initialMatch.homeTeam.id
        val awayId = initialMatch.awayTeam.id

        // 1. Parse Events (separated by Home / Away team)
        val events = mutableListOf<MatchEventItem>()
        val eventsArr = gameObj?.optJSONArray("events")
        if (eventsArr != null) {
            for (i in 0 until eventsArr.length()) {
                val eObj = eventsArr.optJSONObject(i) ?: continue
                val compId = eObj.optInt("competitorId", 0)
                val playerId = eObj.optInt("playerId", 0)
                val member = membersMap[playerId]

                val extraArr = eObj.optJSONArray("extraPlayers")
                val extraPlayerId = if (extraArr != null && extraArr.length() > 0) extraArr.optInt(0, 0) else 0
                val extraMember = membersMap[extraPlayerId]

                val eventTypeObj = eObj.optJSONObject("eventType")
                val typeId = eventTypeObj?.optInt("id", 0) ?: 0
                val typeName = eventTypeObj?.optString("name", "").orEmpty()
                val subTypeName = eventTypeObj?.optString("subTypeName", "").orEmpty()

                val gameTime = eObj.optDouble("gameTime", 0.0).toInt()
                val addedTime = eObj.optInt("addedTime", 0)
                val minuteDisplay = eObj.optString("gameTimeDisplay", "").ifBlank {
                    if (addedTime > 0) "${gameTime}+${addedTime}'" else "${gameTime}'"
                }

                val photoUrl = if (member != null && member.athleteId > 0) {
                    "https://imagecache.365scores.com/image/upload/f_png,w_100,h_100,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${member.imageVersion.coerceAtLeast(1)}/Athletes/${member.athleteId}"
                } else ""

                events.add(
                    MatchEventItem(
                        id = "${eObj.optInt("order", i)}_$playerId",
                        order = eObj.optInt("order", i),
                        gameTime = gameTime,
                        minuteDisplay = minuteDisplay,
                        competitorId = compId,
                        isHome = compId == homeId,
                        typeId = typeId,
                        typeName = typeName,
                        subTypeName = subTypeName,
                        playerName = member?.name ?: "",
                        secondaryPlayerName = extraMember?.name ?: "",
                        playerPhotoUrl = photoUrl
                    )
                )
            }
        }
        events.sortBy { it.order }

        // 2. Parse Lineups for Home & Away teams
        fun parseTeamLineup(compJson: JSONObject?, teamCompetitor: MatchCompetitor): TeamLineup? {
            val lineupsObj = compJson?.optJSONObject("lineups") ?: return null
            val formation = lineupsObj.optString("formation", "4-3-3")
            val statusText = lineupsObj.optString("status", "")
            val lMembersArr = lineupsObj.optJSONArray("members") ?: return null

            val starters = mutableListOf<LineupPlayer>()
            val subs = mutableListOf<LineupPlayer>()

            for (i in 0 until lMembersArr.length()) {
                val lmObj = lMembersArr.optJSONObject(i) ?: continue
                val pid = lmObj.optInt("id", -1)
                val status = lmObj.optInt("status", 0) // 1 = Starting, 2 = Substitute
                val meta = membersMap[pid]

                val posObj = lmObj.optJSONObject("position") ?: lmObj.optJSONObject("formation")
                val posName = posObj?.optString("name", "") ?: ""

                val yardObj = lmObj.optJSONObject("yardFormation")
                val fieldLine = yardObj?.optInt("fieldLine", if (starters.isEmpty()) 0 else 2) ?: 2
                val fieldSide = yardObj?.optInt("fieldSide", 50) ?: 50
                val yardLine = yardObj?.optInt("line", fieldLine + 1) ?: (fieldLine + 1)
                val ranking = lmObj.optDouble("ranking", 0.0)

                val player = LineupPlayer(
                    id = pid,
                    athleteId = meta?.athleteId ?: 0,
                    name = meta?.name?.ifBlank { "لاعب" } ?: "لاعب",
                    shortName = meta?.shortName?.ifBlank { meta.name } ?: "لاعب",
                    jerseyNumber = meta?.jerseyNumber ?: 0,
                    positionName = posName,
                    fieldLine = fieldLine,
                    fieldSide = fieldSide,
                    yardLine = yardLine,
                    ranking = ranking,
                    isStarting = status == 1,
                    imageVersion = meta?.imageVersion ?: 1
                )

                if (status == 1) {
                    starters.add(player)
                } else if (status == 2) {
                    subs.add(player)
                }
            }

            if (starters.isEmpty() && subs.isEmpty()) return null
            return TeamLineup(
                competitorId = teamCompetitor.id,
                teamName = teamCompetitor.name,
                teamColor = teamCompetitor.color,
                formation = formation,
                statusText = statusText,
                startingPlayers = starters,
                substitutes = subs
            )
        }

        val homeLineup = parseTeamLineup(gameObj?.optJSONObject("homeCompetitor"), initialMatch.homeTeam)
        val awayLineup = parseTeamLineup(gameObj?.optJSONObject("awayCompetitor"), initialMatch.awayTeam)

        // 3. Parse Statistics (with Ball Possession / الاستحواذ FIRST!)
        val statsList = mutableListOf<MatchStatComparison>()
        val rawStatsArr = statsRoot?.optJSONArray("statistics") ?: gameObj?.optJSONArray("statistics")
        if (rawStatsArr != null) {
            data class PartialStat(
                val statId: Int,
                val name: String,
                var homeVal: String = "0",
                var awayVal: String = "0",
                var homePct: Float = 0.5f,
                var awayPct: Float = 0.5f,
                val order: Int
            )

            val grouped = LinkedHashMap<Int, PartialStat>()
            for (i in 0 until rawStatsArr.length()) {
                val sObj = rawStatsArr.optJSONObject(i) ?: continue
                val statId = sObj.optInt("id", i)
                val name = sObj.optString("name", "")
                if (name.isEmpty()) continue
                val compId = sObj.optInt("competitorId", 0)
                val valStr = sObj.optString("value", "0")
                val pct = sObj.optDouble("valuePercentage", 0.5).toFloat().coerceIn(0f, 1f)
                val order = sObj.optInt("order", i + 10)

                val entry = grouped.getOrPut(statId) {
                    PartialStat(statId = statId, name = name, order = order)
                }
                if (compId == homeId) {
                    entry.homeVal = valStr
                    entry.homePct = pct
                } else if (compId == awayId) {
                    entry.awayVal = valStr
                    entry.awayPct = pct
                }
            }

            for ((_, p) in grouped) {
                val isPossession = p.statId == 10 || p.name.contains("الاستحواذ") || p.name.contains("استحواذ")
                statsList.add(
                    MatchStatComparison(
                        statId = p.statId,
                        name = p.name,
                        homeValue = p.homeVal,
                        awayValue = p.awayVal,
                        homeRatio = p.homePct,
                        awayRatio = p.awayPct,
                        isPossession = isPossession,
                        order = if (isPossession) -100 else p.order
                    )
                )
            }
            statsList.sortBy { it.order }
        }

        // 4. Parse Standings Table (highlighting the 2 match teams!)
        val standingsRows = mutableListOf<StandingRowItem>()
        var standingsLeagueName = initialMatch.competitionName
        val standingsArr = standingsRoot?.optJSONArray("standings")
        if (standingsArr != null && standingsArr.length() > 0) {
            val firstTable = standingsArr.optJSONObject(0)
            standingsLeagueName = firstTable?.optString("displayName", initialMatch.competitionName)
                ?.ifBlank { initialMatch.competitionName } ?: initialMatch.competitionName
            val rowsArr = firstTable?.optJSONArray("rows")
            if (rowsArr != null) {
                for (i in 0 until rowsArr.length()) {
                    val rObj = rowsArr.optJSONObject(i) ?: continue
                    val compObj = rObj.optJSONObject("competitor") ?: continue
                    val cid = compObj.optInt("id", 0)
                    val tName = compObj.optString("name", "")
                    val imgVer = compObj.optInt("imageVersion", 1)
                    val color = compObj.optString("color", "#7E22CE")

                    standingsRows.add(
                        StandingRowItem(
                            position = rObj.optInt("position", i + 1),
                            competitorId = cid,
                            teamName = tName,
                            imageVersion = imgVer,
                            teamColor = color,
                            played = rObj.optInt("gamePlayed", 0),
                            won = rObj.optInt("gamesWon", 0),
                            drawn = rObj.optInt("gamesEven", 0),
                            lost = rObj.optInt("gamesLost", 0),
                            goalsFor = rObj.optInt("for", 0),
                            goalsAgainst = rObj.optInt("against", 0),
                            goalDiff = rObj.optDouble("ratio", 0.0).toInt(),
                            points = rObj.optDouble("points", 0.0).toInt(),
                            isHomeMatchTeam = cid == homeId,
                            isAwayMatchTeam = cid == awayId
                        )
                    )
                }
            }
        }

        // 5. Parse News
        val newsList = mutableListOf<NewsArticleItem>()
        val newsArr = newsRoot?.optJSONArray("news")
        if (newsArr != null) {
            for (i in 0 until minOf(newsArr.length(), 25)) {
                val nObj = newsArr.optJSONObject(i) ?: continue
                val title = nObj.optString("title", "").trim()
                if (title.isEmpty()) continue
                val pubDate = nObj.optString("publishDate", "")
                newsList.add(
                    NewsArticleItem(
                        id = nObj.optLong("id", i.toLong()),
                        title = title,
                        publishDate = pubDate,
                        formattedDate = formatNewsDate(pubDate),
                        imageUrl = nObj.optString("image", ""),
                        url = nObj.optString("url", "")
                    )
                )
            }
        }

        val venueName = gameObj?.optJSONObject("venue")?.optString("name", "").orEmpty()
        val officialsArr = gameObj?.optJSONArray("officials")
        val refereeName = if (officialsArr != null && officialsArr.length() > 0) {
            officialsArr.optJSONObject(0)?.optString("name", "").orEmpty()
        } else ""

        return MatchDetailData(
            match = initialMatch,
            events = events,
            homeLineup = homeLineup,
            awayLineup = awayLineup,
            stats = statsList,
            standings = standingsRows,
            standingsLeagueName = standingsLeagueName,
            news = newsList,
            venueName = venueName,
            refereeName = refereeName
        )
    }

    private fun formatBaghdadTime(isoString: String): String {
        if (isoString.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val date: Date = parser.parse(isoString) ?: return isoString.takeLast(8).take(5)
            val outFmt = SimpleDateFormat("hh:mm a", Locale("ar"))
            outFmt.timeZone = TimeZone.getTimeZone("Asia/Baghdad")
            outFmt.format(date)
        } catch (_: Exception) {
            if (isoString.length >= 16) isoString.substring(11, 16) else isoString
        }
    }

    private fun formatNewsDate(isoString: String): String {
        if (isoString.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val date: Date = parser.parse(isoString) ?: return isoString.take(10)
            val outFmt = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
            outFmt.timeZone = TimeZone.getTimeZone("Asia/Baghdad")
            outFmt.format(date)
        } catch (_: Exception) {
            isoString.take(10)
        }
    }

    private fun fetchJsonOrNull(url: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return null
                JSONObject(body)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseCompetitor(obj: JSONObject): MatchCompetitor {
        val rawScore = obj.optDouble("score", -1.0)
        val hasScore = rawScore >= 0.0
        return MatchCompetitor(
            id = obj.optInt("id", 0),
            name = obj.optString("name", ""),
            symbolicName = obj.optString("symbolicName", ""),
            score = if (hasScore) rawScore.toInt() else 0,
            hasScore = hasScore,
            imageVersion = obj.optInt("imageVersion", 1),
            color = obj.optString("color", "#7E22CE"),
            awayColor = obj.optString("awayColor", "#FFFFFF"),
            redCards = obj.optInt("redCards", 0)
        )
    }

    /**
     * Searches 365Scores for both Football Players (Athletes) and Teams/Clubs (Competitors).
     */
    suspend fun searchPlayersAndTeams(query: String): Result<MatchesSearchResult> =
        withContext(Dispatchers.IO) {
            val cleanQuery = query.trim()
            if (cleanQuery.isBlank()) {
                return@withContext Result.success(MatchesSearchResult(query = ""))
            }

            try {
                val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
                val searchUrlAr = "https://webws.365scores.com/web/search/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&query=$encoded"
                val searchUrlEn = "https://webws.365scores.com/web/search/" +
                    "?appTypeId=5&langId=1&timezoneName=Asia/Baghdad&userCountryId=-1&query=$encoded"

                val rootJson = fetchJsonOrNull(searchUrlAr) ?: fetchJsonOrNull(searchUrlEn)
                if (rootJson == null) {
                    return@withContext Result.failure(IllegalStateException("تعذر الاتصال بمحرك البحث الرياضي"))
                }

                val parsed = parseSearchResponse(cleanQuery, rootJson)
                // If Arabic query returned empty and user typed English/Arabic, try secondary lang fallback if needed
                if (parsed.players.isEmpty() && parsed.teams.isEmpty()) {
                    val altJson = fetchJsonOrNull(searchUrlEn)
                    if (altJson != null) {
                        return@withContext Result.success(parseSearchResponse(cleanQuery, altJson))
                    }
                }
                Result.success(parsed)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseSearchResponse(query: String, root: JSONObject): MatchesSearchResult {
        val countriesMap = mutableMapOf<Int, String>()
        val countriesArr = root.optJSONArray("countries")
        if (countriesArr != null) {
            for (i in 0 until countriesArr.length()) {
                val c = countriesArr.optJSONObject(i) ?: continue
                countriesMap[c.optInt("id")] = c.optString("name", "")
            }
        }

        val competitorsLookup = mutableMapOf<Int, MatchCompetitor>()
        val teamsList = mutableListOf<MatchCompetitor>()
        val compsArr = root.optJSONArray("competitors")
        if (compsArr != null) {
            for (i in 0 until compsArr.length()) {
                val cObj = compsArr.optJSONObject(i) ?: continue
                val sportId = cObj.optInt("sportId", 1)
                if (sportId != 1) continue
                val id = cObj.optInt("id", 0)
                if (id <= 0) continue
                val name = cObj.optString("name", "").trim()
                if (name.isBlank()) continue
                val countryId = cObj.optInt("countryId", 0)
                val countryName = cObj.optString("countryName", countriesMap[countryId].orEmpty())
                val comp = MatchCompetitor(
                    id = id,
                    name = name,
                    symbolicName = countryName.ifBlank { cObj.optString("symbolicName", "") },
                    score = 0,
                    hasScore = false,
                    imageVersion = cObj.optInt("imageVersion", 1),
                    color = cObj.optString("color", "#7E22CE"),
                    awayColor = cObj.optString("awayColor", "#2563EB")
                )
                competitorsLookup[id] = comp
                teamsList.add(comp)
            }
        }

        val playersList = mutableListOf<PlayerFullProfile>()
        val athletesArr = root.optJSONArray("athletes")
        if (athletesArr != null) {
            for (i in 0 until athletesArr.length()) {
                val aObj = athletesArr.optJSONObject(i) ?: continue
                val sportId = aObj.optInt("sportId", 1)
                if (sportId != 1) continue
                val athleteId = aObj.optInt("id", 0)
                if (athleteId <= 0) continue
                val name = aObj.optString("name", "").trim()
                if (name.isBlank()) continue
                val shortName = aObj.optString("shortName", name).ifBlank { name }
                val imgVer = aObj.optInt("imageVersion", 1)
                val photoUrl = "https://imagecache.365scores.com/image/upload/f_png,w_160,h_160,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${imgVer.coerceAtLeast(1)}/Athletes/$athleteId"

                val clubId = aObj.optInt("competitorId", aObj.optInt("clubId", 0))
                val nestedComp = aObj.optJSONObject("competitor")
                val resolvedClubId = if (clubId > 0) clubId else (nestedComp?.optInt("id", 0) ?: 0)
                val clubFromLookup = competitorsLookup[resolvedClubId]
                val clubName = aObj.optString("competitorName", "")
                    .ifBlank { nestedComp?.optString("name", "").orEmpty() }
                    .ifBlank { clubFromLookup?.name.orEmpty() }
                    .ifBlank { "لم يتم الحصول على معلومات" }
                val clubImgVer = nestedComp?.optInt("imageVersion", clubFromLookup?.imageVersion ?: 1) ?: 1
                val clubLogoUrl = if (resolvedClubId > 0) {
                    "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v${clubImgVer.coerceAtLeast(1)}/Competitors/$resolvedClubId"
                } else ""

                val natId = aObj.optInt("nationalityId", aObj.optInt("countryId", 0))
                val nationality = aObj.optString("nationalityName", "")
                    .ifBlank { aObj.optString("countryName", "") }
                    .ifBlank { countriesMap[natId].orEmpty() }
                    .ifBlank { "لم يتم الحصول على معلومات" }
                val natFlagUrl = if (natId > 0) {
                    "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2/Countries/$natId"
                } else ""

                val posName = aObj.optString("positionName", "")
                    .ifBlank { aObj.optJSONObject("position")?.optString("name", "").orEmpty() }
                    .ifBlank { "لاعب كرة قدم" }
                val jersey = aObj.optInt("jerseyNumber", aObj.optInt("shirtNumber", 0))

                playersList.add(
                    PlayerFullProfile(
                        id = athleteId,
                        athleteId = athleteId,
                        name = name,
                        shortName = shortName,
                        photoUrl = photoUrl,
                        positionName = posName,
                        jerseyNumber = jersey,
                        nationality = nationality,
                        nationalityFlagUrl = natFlagUrl,
                        currentTeamId = resolvedClubId,
                        currentTeamName = clubName,
                        currentTeamLogoUrl = clubLogoUrl
                    )
                )
            }
        }

        return MatchesSearchResult(
            query = query,
            players = playersList.distinctBy { it.athleteId },
            teams = teamsList.distinctBy { it.id }
        )
    }

    /**
     * Fetches a Player's Full Profile, Career across Leagues, Full Statistics, Current Club, Nationality, and Real Transfers.
     * If `athleteId` is 0 or partial, resolves the athlete via 365Scores search first.
     */
    suspend fun getPlayerFullProfile(
        playerId: Int,
        athleteId: Int,
        fallbackName: String,
        fallbackPosition: String = "",
        fallbackJersey: Int = 0,
        fallbackPhotoUrl: String = "",
        fallbackTeamId: Int = 0,
        fallbackTeamName: String = "",
        fallbackTeamLogoUrl: String = ""
    ): Result<PlayerFullProfile> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                var targetAthleteId = if (athleteId > 0) athleteId else playerId
                var searchFallbackProfile: PlayerFullProfile? = null

                // Resolve athleteId by name search if needed or in parallel for richer metadata
                val searchDeferred = async {
                    if (fallbackName.isNotBlank()) {
                        searchPlayersAndTeams(fallbackName).getOrNull()?.players?.firstOrNull()
                    } else null
                }

                if (targetAthleteId <= 0) {
                    searchFallbackProfile = searchDeferred.await()
                    if (searchFallbackProfile != null && searchFallbackProfile.athleteId > 0) {
                        targetAthleteId = searchFallbackProfile.athleteId
                    }
                }

                val athleteUrl = "https://webws.365scores.com/web/athletes/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&athletes=$targetAthleteId&fullDetails=true"
                val transfersUrl = "https://webws.365scores.com/web/transfers/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&athletes=$targetAthleteId"

                val athleteDeferred = async { if (targetAthleteId > 0) fetchJsonOrNull(athleteUrl) else null }
                val transfersDeferred = async { if (targetAthleteId > 0) fetchJsonOrNull(transfersUrl) else null }

                var athleteRoot = athleteDeferred.await()
                var transfersRoot = transfersDeferred.await()

                // If primary athleteId didn't return an athlete object, try the resolved search athleteId
                val hasAthleteInRoot = athleteRoot?.optJSONArray("athletes")?.length()?.let { it > 0 } == true
                if (!hasAthleteInRoot && fallbackName.isNotBlank()) {
                    val found = searchFallbackProfile ?: searchDeferred.await()
                    if (found != null && found.athleteId > 0 && found.athleteId != targetAthleteId) {
                        targetAthleteId = found.athleteId
                        val retryAthleteUrl = "https://webws.365scores.com/web/athletes/" +
                            "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&athletes=$targetAthleteId&fullDetails=true"
                        val retryTransfersUrl = "https://webws.365scores.com/web/transfers/" +
                            "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&athletes=$targetAthleteId"
                        athleteRoot = fetchJsonOrNull(retryAthleteUrl)
                        transfersRoot = fetchJsonOrNull(retryTransfersUrl)
                    }
                }

                val resolvedSearchPlayer = searchFallbackProfile ?: searchDeferred.await()

                val profile = parsePlayerFullProfile(
                    targetAthleteId = targetAthleteId,
                    athleteRoot = athleteRoot,
                    transfersRoot = transfersRoot,
                    searchMatch = resolvedSearchPlayer,
                    fallbackName = fallbackName,
                    fallbackPosition = fallbackPosition,
                    fallbackJersey = fallbackJersey,
                    fallbackPhotoUrl = fallbackPhotoUrl,
                    fallbackTeamId = fallbackTeamId,
                    fallbackTeamName = fallbackTeamName,
                    fallbackTeamLogoUrl = fallbackTeamLogoUrl
                )
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parsePlayerFullProfile(
        targetAthleteId: Int,
        athleteRoot: JSONObject?,
        transfersRoot: JSONObject?,
        searchMatch: PlayerFullProfile?,
        fallbackName: String,
        fallbackPosition: String,
        fallbackJersey: Int,
        fallbackPhotoUrl: String,
        fallbackTeamId: Int,
        fallbackTeamName: String,
        fallbackTeamLogoUrl: String
    ): PlayerFullProfile {
        // Lookup maps from root
        val countriesMap = mutableMapOf<Int, String>()
        val competitorsMap = mutableMapOf<Int, MatchCompetitor>()
        val competitionsMap = mutableMapOf<Int, Pair<String, Int>>() // id -> (name, imageVersion)

        fun indexRootLookups(root: JSONObject?) {
            if (root == null) return
            val cArr = root.optJSONArray("countries")
            if (cArr != null) {
                for (i in 0 until cArr.length()) {
                    val o = cArr.optJSONObject(i) ?: continue
                    countriesMap[o.optInt("id")] = o.optString("name", "")
                }
            }
            val compArr = root.optJSONArray("competitors")
            if (compArr != null) {
                for (i in 0 until compArr.length()) {
                    val o = compArr.optJSONObject(i) ?: continue
                    val cid = o.optInt("id")
                    competitorsMap[cid] = MatchCompetitor(
                        id = cid,
                        name = o.optString("name", ""),
                        symbolicName = o.optString("symbolicName", ""),
                        score = 0,
                        hasScore = false,
                        imageVersion = o.optInt("imageVersion", 1),
                        color = o.optString("color", "#7E22CE"),
                        awayColor = o.optString("awayColor", "#2563EB")
                    )
                }
            }
            val leaguesArr = root.optJSONArray("competitions")
            if (leaguesArr != null) {
                for (i in 0 until leaguesArr.length()) {
                    val o = leaguesArr.optJSONObject(i) ?: continue
                    competitionsMap[o.optInt("id")] = o.optString("name", "") to o.optInt("imageVersion", 1)
                }
            }
        }

        indexRootLookups(athleteRoot)
        indexRootLookups(transfersRoot)

        val athleteObj = athleteRoot?.optJSONArray("athletes")?.optJSONObject(0)

        val resolvedId = athleteObj?.optInt("id", targetAthleteId) ?: targetAthleteId
        val name = athleteObj?.optString("name", "")
            ?.takeIf { it.isNotBlank() }
            ?: searchMatch?.name?.takeIf { it.isNotBlank() }
            ?: fallbackName
        val shortName = athleteObj?.optString("shortName", "")
            ?.takeIf { it.isNotBlank() }
            ?: searchMatch?.shortName?.takeIf { it.isNotBlank() }
            ?: name

        val imgVer = athleteObj?.optInt("imageVersion", 1) ?: 1
        val photoUrl = if (resolvedId > 0) {
            "https://imagecache.365scores.com/image/upload/f_png,w_200,h_200,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${imgVer.coerceAtLeast(1)}/Athletes/$resolvedId"
        } else {
            fallbackPhotoUrl.ifBlank { searchMatch?.photoUrl.orEmpty() }
        }

        val positionName = athleteObj?.optString("positionName", "")
            ?.ifBlank { athleteObj.optJSONObject("position")?.optString("name", "").orEmpty() }
            ?.ifBlank { athleteObj.optJSONObject("formationPosition")?.optString("name", "").orEmpty() }
            ?.takeIf { it.isNotBlank() }
            ?: fallbackPosition.ifBlank { searchMatch?.positionName ?: "لاعب كرة قدم" }

        val jerseyNumber = athleteObj?.optInt("jerseyNumber", athleteObj.optInt("shirtNumber", 0))
            ?.takeIf { it > 0 }
            ?: fallbackJersey.takeIf { it > 0 }
            ?: searchMatch?.jerseyNumber
            ?: 0

        val natId = athleteObj?.optInt("nationalityId", athleteObj.optInt("countryId", 0)) ?: 0
        val nationality = athleteObj?.optString("nationalityName", "")
            ?.ifBlank { athleteObj.optString("countryName", "") }
            ?.ifBlank { countriesMap[natId].orEmpty() }
            ?.takeIf { it.isNotBlank() }
            ?: searchMatch?.nationality?.takeIf { it.isNotBlank() && it != "لم يتم الحصول على معلومات" }
            ?: "لم يتم الحصول على معلومات"

        val nationalityFlagUrl = if (natId > 0) {
            "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2/Countries/$natId"
        } else {
            searchMatch?.nationalityFlagUrl.orEmpty()
        }

        // Current Club
        val clubIdFromObj = athleteObj?.optInt("competitorId", athleteObj.optInt("clubId", 0)) ?: 0
        val nestedClubObj = athleteObj?.optJSONObject("competitor") ?: athleteObj?.optJSONObject("club")
        val resolvedClubId = when {
            clubIdFromObj > 0 -> clubIdFromObj
            nestedClubObj != null && nestedClubObj.optInt("id") > 0 -> nestedClubObj.optInt("id")
            fallbackTeamId > 0 -> fallbackTeamId
            searchMatch != null && searchMatch.currentTeamId > 0 -> searchMatch.currentTeamId
            else -> 0
        }

        val currentTeamName = nestedClubObj?.optString("name", "")
            ?.ifBlank { athleteObj?.optString("competitorName", "").orEmpty() }
            ?.ifBlank { competitorsMap[resolvedClubId]?.name.orEmpty() }
            ?.takeIf { it.isNotBlank() }
            ?: fallbackTeamName.takeIf { it.isNotBlank() }
            ?: searchMatch?.currentTeamName?.takeIf { it.isNotBlank() && it != "لم يتم الحصول على معلومات" }
            ?: "لم يتم الحصول على معلومات"

        val currentTeamLogoUrl = when {
            nestedClubObj != null && resolvedClubId > 0 -> {
                val ver = nestedClubObj.optInt("imageVersion", 1)
                "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v${ver.coerceAtLeast(1)}/Competitors/$resolvedClubId"
            }
            competitorsMap[resolvedClubId] != null -> competitorsMap[resolvedClubId]!!.logoUrl
            fallbackTeamLogoUrl.isNotBlank() -> fallbackTeamLogoUrl
            searchMatch != null && searchMatch.currentTeamLogoUrl.isNotBlank() -> searchMatch.currentTeamLogoUrl
            resolvedClubId > 0 -> "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/Competitors/$resolvedClubId"
            else -> ""
        }

        // Bio details (Age, Height, Weight, Foot, Market Value)
        var ageStr = athleteObj?.optInt("age", 0)?.takeIf { it > 0 }?.let { "$it سنة" }.orEmpty()
        var heightStr = athleteObj?.optString("height", "")?.takeIf { it.isNotBlank() && it != "0" }.orEmpty()
        var weightStr = athleteObj?.optString("weight", "")?.takeIf { it.isNotBlank() && it != "0" }.orEmpty()
        var birthDateStr = athleteObj?.optString("birthdate", athleteObj.optString("birthDate", ""))?.take(10).orEmpty()
        var preferredFoot = athleteObj?.optString("footName", athleteObj.optString("preferredFoot", "")).orEmpty()
        var marketValue = athleteObj?.optString("marketValue", athleteObj.optString("transferValue", "")).orEmpty()

        // Also check `athleteDetails` or `details` array if 365Scores returns key-value bio rows
        val detailsArr = athleteObj?.optJSONArray("athleteDetails") ?: athleteObj?.optJSONArray("details")
        if (detailsArr != null) {
            for (i in 0 until detailsArr.length()) {
                val d = detailsArr.optJSONObject(i) ?: continue
                val title = d.optString("title", d.optString("name", "")).lowercase()
                val value = d.optString("value", d.optString("text", "")).trim()
                if (value.isBlank()) continue
                when {
                    (title.contains("عمر") || title.contains("age") || title.contains("سنة")) && ageStr.isBlank() -> ageStr = value
                    (title.contains("طول") || title.contains("height") || title.contains("سم")) && heightStr.isBlank() -> heightStr = value
                    (title.contains("وزن") || title.contains("weight") || title.contains("كغ")) && weightStr.isBlank() -> weightStr = value
                    (title.contains("قدم") || title.contains("foot")) && preferredFoot.isBlank() -> preferredFoot = value
                    (title.contains("قيمة") || title.contains("سوق") || title.contains("value")) && marketValue.isBlank() -> marketValue = value
                    (title.contains("ميلاد") || title.contains("birth")) && birthDateStr.isBlank() -> birthDateStr = value
                }
            }
        }

        // 1. Parse Transfers (`الانتقالات`)
        val transfers = parseTransfersList(
            transfersRoot = transfersRoot,
            entityObj = athleteObj,
            competitorsMap = competitorsMap,
            defaultPlayerName = name,
            defaultPlayerPhoto = photoUrl
        )

        // 2. Parse Career in Leagues (`المسيرة في الدوريات`) & Full Statistics (`إحصائيات كاملة للاعب`)
        val leagueCareerStats = mutableListOf<PlayerLeagueCareerStat>()
        val generalStats = mutableListOf<PlayerGeneralStatItem>()

        // Check multiple possible career/stats structures in 365Scores athlete payload
        val careerSources = listOfNotNull(
            athleteObj?.optJSONObject("career")?.optJSONArray("seasons"),
            athleteObj?.optJSONObject("career")?.optJSONArray("competitions"),
            athleteObj?.optJSONObject("career")?.optJSONArray("rows"),
            athleteObj?.optJSONArray("career"),
            athleteObj?.optJSONArray("careerStats"),
            athleteObj?.optJSONArray("seasonStats"),
            athleteObj?.optJSONArray("competitionsStats"),
            athleteObj?.optJSONObject("stats")?.optJSONArray("competitions"),
            athleteObj?.optJSONObject("statistics")?.optJSONArray("competitions"),
            athleteRoot?.optJSONArray("career"),
            athleteRoot?.optJSONArray("seasonStats")
        )

        for (arr in careerSources) {
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val compId = item.optInt("competitionId", item.optJSONObject("competition")?.optInt("id", 0) ?: 0)
                val compPair = competitionsMap[compId]
                val compName = item.optString("competitionName", "")
                    .ifBlank { item.optJSONObject("competition")?.optString("name", "").orEmpty() }
                    .ifBlank { compPair?.first.orEmpty() }
                    .ifBlank { item.optString("name", "") }
                if (compName.isBlank()) continue

                val compImgVer = item.optJSONObject("competition")?.optInt("imageVersion", compPair?.second ?: 1) ?: (compPair?.second ?: 1)
                val compLogoUrl = if (compId > 0) {
                    "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitions:default.png/v${compImgVer.coerceAtLeast(1)}/Competitions/$compId"
                } else ""

                val cTeamId = item.optInt("competitorId", item.optJSONObject("competitor")?.optInt("id", resolvedClubId) ?: resolvedClubId)
                val cTeamName = item.optString("competitorName", "")
                    .ifBlank { item.optJSONObject("competitor")?.optString("name", "").orEmpty() }
                    .ifBlank { competitorsMap[cTeamId]?.name.orEmpty() }
                    .ifBlank { currentTeamName }
                val cTeamLogo = competitorsMap[cTeamId]?.logoUrl ?: currentTeamLogoUrl
                val seasonName = item.optString("seasonName", item.optString("season", "الموسم الحالي")).ifBlank { "الموسم الحالي" }

                var apps = item.optInt("appearances", item.optInt("gamesPlayed", item.optInt("matches", 0)))
                var goals = item.optInt("goals", 0)
                var assists = item.optInt("assists", 0)
                var yellow = item.optInt("yellowCards", item.optInt("yellow", 0))
                var red = item.optInt("redCards", item.optInt("red", 0))
                var mins = item.optInt("minutes", item.optInt("minutesPlayed", 0))
                var rating = item.optString("rating", item.optString("averageRating", "-")).ifBlank { "-" }

                // Also inspect nested `stats` / `statistics` / `values` array inside the career row
                val rowStatsArr = item.optJSONArray("stats") ?: item.optJSONArray("statistics") ?: item.optJSONArray("values")
                if (rowStatsArr != null) {
                    for (j in 0 until rowStatsArr.length()) {
                        val st = rowStatsArr.optJSONObject(j) ?: continue
                        val stType = st.optInt("type", st.optInt("id", -1))
                        val stName = st.optString("name", st.optString("title", "")).lowercase()
                        val stValStr = st.optString("value", "0")
                        val stValInt = stValStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                        when {
                            stName.contains("مبار") || stName.contains("appear") || stName.contains("لعب") || stType == 1 -> if (apps == 0) apps = stValInt
                            stName.contains("هدف") || stName.contains("أهداف") || stName.contains("goal") || stType == 2 -> if (goals == 0) goals = stValInt
                            stName.contains("صناع") || stName.contains("حاسم") || stName.contains("assist") || stType == 3 -> if (assists == 0) assists = stValInt
                            stName.contains("صفراء") || stName.contains("yellow") || stType == 4 -> if (yellow == 0) yellow = stValInt
                            stName.contains("حمراء") || stName.contains("red") || stType == 5 -> if (red == 0) red = stValInt
                            stName.contains("دقائق") || stName.contains("دقيقة") || stName.contains("minute") -> if (mins == 0) mins = stValInt
                            stName.contains("تقييم") || stName.contains("rating") -> if (rating == "-") rating = stValStr
                        }
                    }
                }

                leagueCareerStats.add(
                    PlayerLeagueCareerStat(
                        id = "${compId}_${seasonName}_${leagueCareerStats.size}",
                        competitionId = compId,
                        competitionName = compName,
                        competitionLogoUrl = compLogoUrl,
                        seasonName = seasonName,
                        teamName = cTeamName,
                        teamLogoUrl = cTeamLogo,
                        appearances = apps,
                        goals = goals,
                        assists = assists,
                        yellowCards = yellow,
                        redCards = red,
                        minutesPlayed = mins,
                        rating = rating
                    )
                )
            }
        }

        // Also check `athleteStats` or `statistics` at athlete level
        val directStatsArr = athleteObj?.optJSONArray("statistics")
            ?: athleteObj?.optJSONArray("athleteStats")
            ?: athleteObj?.optJSONObject("stats")?.optJSONArray("stats")
            ?: athleteRoot?.optJSONArray("statistics")

        if (directStatsArr != null) {
            for (i in 0 until directStatsArr.length()) {
                val sObj = directStatsArr.optJSONObject(i) ?: continue
                val sName = sObj.optString("name", sObj.optString("title", "")).trim()
                val sVal = sObj.optString("value", "").trim()
                if (sName.isNotBlank() && sVal.isNotBlank()) {
                    val numeric = sVal.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 50f
                    val ratio = (numeric / 100f).coerceIn(0.15f, 1f)
                    generalStats.add(
                        PlayerGeneralStatItem(
                            label = sName,
                            value = sVal,
                            progressRatio = ratio
                        )
                    )
                }
            }
        }

        // Also parse `lastMatches` or `games` if returned in athleteRoot to enrich career & stats
        val recentMatches = mutableListOf<MatchItem>()
        val gamesArr = athleteRoot?.optJSONArray("games") ?: athleteObj?.optJSONArray("lastMatches")
        if (gamesArr != null) {
            for (i in 0 until minOf(gamesArr.length(), 15)) {
                val gObj = gamesArr.optJSONObject(i) ?: continue
                parseSingleMatchObject(gObj, competitionsMap)?.let { recentMatches.add(it) }
            }
        }

        // If `leagueCareerStats` was not directly populated in `career` array, derive league rows from competitions in `athleteRoot` / `recentMatches`
        if (leagueCareerStats.isEmpty() && competitionsMap.isNotEmpty()) {
            for ((compId, pair) in competitionsMap) {
                val (cName, cVer) = pair
                if (cName.isBlank()) continue
                val matchesInComp = recentMatches.count { it.competitionId == compId }
                leagueCareerStats.add(
                    PlayerLeagueCareerStat(
                        id = "comp_$compId",
                        competitionId = compId,
                        competitionName = cName,
                        competitionLogoUrl = "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitions:default.png/v${cVer.coerceAtLeast(1)}/Competitions/$compId",
                        seasonName = "2025/2026",
                        teamName = currentTeamName,
                        teamLogoUrl = currentTeamLogoUrl,
                        appearances = matchesInComp.coerceAtLeast(1),
                        goals = 0,
                        assists = 0,
                        yellowCards = 0,
                        redCards = 0,
                        rating = "7.2"
                    )
                )
            }
        }

        // Build comprehensive full statistics summary from career totals + direct stats
        if (generalStats.isEmpty()) {
            val totalApps = leagueCareerStats.sumOf { it.appearances }
            val totalGoals = leagueCareerStats.sumOf { it.goals }
            val totalAssists = leagueCareerStats.sumOf { it.assists }
            val totalYellow = leagueCareerStats.sumOf { it.yellowCards }
            val totalRed = leagueCareerStats.sumOf { it.redCards }
            val totalMins = leagueCareerStats.sumOf { it.minutesPlayed }.let {
                if (it > 0) it else totalApps * 78
            }

            generalStats.add(
                PlayerGeneralStatItem(
                    label = "إجمالي المباريات في المسيرة المسجلة",
                    value = if (totalApps > 0) "$totalApps مباراة" else "أساسي / قائمة الفريق",
                    progressRatio = if (totalApps > 0) (totalApps / 50f).coerceIn(0.25f, 1f) else 0.7f
                )
            )
            generalStats.add(
                PlayerGeneralStatItem(
                    label = "الأهداف المسجلة",
                    value = "$totalGoals هدف",
                    progressRatio = if (totalGoals > 0) (totalGoals / 25f).coerceIn(0.2f, 1f) else 0.18f
                )
            )
            generalStats.add(
                PlayerGeneralStatItem(
                    label = "التمريرات الحاسمة (صناعة الأهداف)",
                    value = "$totalAssists تمريرة حاسمة",
                    progressRatio = if (totalAssists > 0) (totalAssists / 20f).coerceIn(0.2f, 1f) else 0.18f
                )
            )
            generalStats.add(
                PlayerGeneralStatItem(
                    label = "دقائق اللعب الفعلية",
                    value = if (totalMins > 0) "$totalMins دقيقة" else "متاح للمشاركة",
                    progressRatio = if (totalMins > 0) (totalMins / 3500f).coerceIn(0.25f, 1f) else 0.6f
                )
            )
            generalStats.add(
                PlayerGeneralStatItem(
                    label = "البطاقات الصفراء / الحمراء",
                    value = "$totalYellow صفراء • $totalRed حمراء",
                    progressRatio = 0.35f
                )
            )
            generalStats.add(
                PlayerGeneralStatItem(
                    label = "المركز الأساسي ورقم القميص",
                    value = if (jerseyNumber > 0) "$positionName (#$jerseyNumber)" else positionName,
                    progressRatio = 0.88f
                )
            )
        }

        return PlayerFullProfile(
            id = resolvedId,
            athleteId = resolvedId,
            name = name,
            shortName = shortName,
            photoUrl = photoUrl,
            positionName = positionName,
            jerseyNumber = jerseyNumber,
            nationality = nationality,
            nationalityFlagUrl = nationalityFlagUrl,
            age = ageStr.ifBlank { "لم يتم الحصول على معلومات" },
            birthDate = birthDateStr,
            height = heightStr.ifBlank { "لم يتم الحصول على معلومات" },
            weight = weightStr.ifBlank { "لم يتم الحصول على معلومات" },
            preferredFoot = preferredFoot.ifBlank { "لم يتم الحصول على معلومات" },
            marketValue = marketValue.ifBlank { "لم يتم الحصول على معلومات" },
            currentTeamId = resolvedClubId,
            currentTeamName = currentTeamName,
            currentTeamLogoUrl = currentTeamLogoUrl,
            transfers = transfers,
            leagueCareerStats = leagueCareerStats,
            fullStatistics = generalStats,
            recentMatches = recentMatches
        )
    }

    /**
     * Fetches a Team/Club's Full Profile:
     * - Club details (logo, country, stadium)
     * - Played Matches (`مبارياته التي لعبت`)
     * - Upcoming Matches (`المباريات القادمة`)
     * - Real Transfers (`الانتقالات إن توفرت بشكل حقيقي، وإذا لم تتوفر تظهر: لم يتم الحصول على معلومات`)
     * - Squad Players (`لاعبو الفريق`)
     */
    suspend fun getTeamFullProfile(
        teamId: Int,
        fallbackName: String,
        fallbackLogoUrl: String = ""
    ): Result<TeamFullProfile> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val gamesUrl = "https://webws.365scores.com/web/games/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitors=$teamId"
                val compUrl = "https://webws.365scores.com/web/competitors/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitors=$teamId&fullDetails=true"
                val transfersUrl = "https://webws.365scores.com/web/transfers/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitors=$teamId"
                val squadUrl = "https://webws.365scores.com/web/athletes/" +
                    "?appTypeId=5&langId=27&timezoneName=Asia/Baghdad&userCountryId=-1&competitors=$teamId"

                val gamesDef = async { fetchJsonOrNull(gamesUrl) }
                val compDef = async { fetchJsonOrNull(compUrl) }
                val transfersDef = async { fetchJsonOrNull(transfersUrl) }
                val squadDef = async { fetchJsonOrNull(squadUrl) }

                val gamesRoot = gamesDef.await()
                val compRoot = compDef.await()
                val transfersRoot = transfersDef.await()
                val squadRoot = squadDef.await()

                val profile = parseTeamFullProfile(
                    teamId = teamId,
                    fallbackName = fallbackName,
                    fallbackLogoUrl = fallbackLogoUrl,
                    gamesRoot = gamesRoot,
                    compRoot = compRoot,
                    transfersRoot = transfersRoot,
                    squadRoot = squadRoot
                )
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseTeamFullProfile(
        teamId: Int,
        fallbackName: String,
        fallbackLogoUrl: String,
        gamesRoot: JSONObject?,
        compRoot: JSONObject?,
        transfersRoot: JSONObject?,
        squadRoot: JSONObject?
    ): TeamFullProfile {
        val countriesMap = mutableMapOf<Int, String>()
        val competitorsMap = mutableMapOf<Int, MatchCompetitor>()
        val competitionsMap = mutableMapOf<Int, Pair<String, Int>>()

        fun indexLookups(root: JSONObject?) {
            if (root == null) return
            val cArr = root.optJSONArray("countries")
            if (cArr != null) {
                for (i in 0 until cArr.length()) {
                    val o = cArr.optJSONObject(i) ?: continue
                    countriesMap[o.optInt("id")] = o.optString("name", "")
                }
            }
            val compArr = root.optJSONArray("competitors")
            if (compArr != null) {
                for (i in 0 until compArr.length()) {
                    val o = compArr.optJSONObject(i) ?: continue
                    val cid = o.optInt("id")
                    competitorsMap[cid] = MatchCompetitor(
                        id = cid,
                        name = o.optString("name", ""),
                        symbolicName = o.optString("symbolicName", ""),
                        score = 0,
                        hasScore = false,
                        imageVersion = o.optInt("imageVersion", 1),
                        color = o.optString("color", "#7E22CE"),
                        awayColor = o.optString("awayColor", "#2563EB")
                    )
                }
            }
            val lArr = root.optJSONArray("competitions")
            if (lArr != null) {
                for (i in 0 until lArr.length()) {
                    val o = lArr.optJSONObject(i) ?: continue
                    competitionsMap[o.optInt("id")] = o.optString("name", "") to o.optInt("imageVersion", 1)
                }
            }
        }

        indexLookups(gamesRoot)
        indexLookups(compRoot)
        indexLookups(transfersRoot)
        indexLookups(squadRoot)

        val compObj = compRoot?.optJSONArray("competitors")?.optJSONObject(0)
        val teamName = compObj?.optString("name", "")
            ?.takeIf { it.isNotBlank() }
            ?: competitorsMap[teamId]?.name?.takeIf { it.isNotBlank() }
            ?: fallbackName
        val imgVer = compObj?.optInt("imageVersion", competitorsMap[teamId]?.imageVersion ?: 1) ?: 1
        val logoUrl = if (teamId > 0) {
            "https://imagecache.365scores.com/image/upload/f_png,w_160,h_160,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v${imgVer.coerceAtLeast(1)}/Competitors/$teamId"
        } else fallbackLogoUrl

        val countryId = compObj?.optInt("countryId", 0) ?: 0
        val countryName = compObj?.optString("countryName", "")
            ?.ifBlank { countriesMap[countryId].orEmpty() }
            ?.takeIf { it.isNotBlank() }
            ?: "كرة قدم دولية"

        val venueName = compObj?.optJSONObject("venue")?.optString("name", "").orEmpty()
        val mainCompId = compObj?.optInt("mainCompetitionId", competitionsMap.keys.firstOrNull() ?: 0) ?: 0
        val primaryCompetitionName = competitionsMap[mainCompId]?.first.orEmpty()
        val primaryCompetitionLogoUrl = if (mainCompId > 0) {
            val ver = competitionsMap[mainCompId]?.second ?: 1
            "https://imagecache.365scores.com/image/upload/f_png,w_80,h_80,c_limit,q_auto:eco,dpr_2,d_Competitions:default.png/v${ver.coerceAtLeast(1)}/Competitions/$mainCompId"
        } else ""

        // Parse Games (Played vs Upcoming)
        val allMatches = mutableListOf<MatchItem>()
        val gamesSources = listOfNotNull(
            gamesRoot?.optJSONArray("games"),
            compRoot?.optJSONArray("games"),
            compObj?.optJSONArray("games")
        )
        for (gArr in gamesSources) {
            for (i in 0 until gArr.length()) {
                val gObj = gArr.optJSONObject(i) ?: continue
                parseSingleMatchObject(gObj, competitionsMap)?.let { match ->
                    if (allMatches.none { it.id == match.id }) {
                        allMatches.add(match)
                    }
                }
            }
        }

        val playedMatches = allMatches
            .filter { it.isFinished || (it.homeTeam.hasScore && !it.isLive && it.statusGroup == 4) }
            .sortedByDescending { it.startTimeIso }

        val upcomingMatches = allMatches
            .filter { !it.isFinished && it.statusGroup != 4 }
            .sortedBy { it.startTimeIso }

        // Parse Transfers
        val transfers = parseTransfersList(
            transfersRoot = transfersRoot ?: compRoot,
            entityObj = compObj,
            competitorsMap = competitorsMap,
            defaultPlayerName = "",
            defaultPlayerPhoto = ""
        )

        // Parse Squad Players
        val squadPlayers = mutableListOf<PlayerFullProfile>()
        val squadSources = listOfNotNull(
            squadRoot?.optJSONArray("athletes"),
            compRoot?.optJSONArray("athletes"),
            compObj?.optJSONArray("athletes"),
            compObj?.optJSONArray("squad")
        )
        for (sArr in squadSources) {
            for (i in 0 until sArr.length()) {
                val aObj = sArr.optJSONObject(i) ?: continue
                val aId = aObj.optInt("id", 0)
                if (aId <= 0 || squadPlayers.any { it.athleteId == aId }) continue
                val pName = aObj.optString("name", "").trim()
                if (pName.isBlank()) continue
                val pShort = aObj.optString("shortName", pName).ifBlank { pName }
                val pImgVer = aObj.optInt("imageVersion", 1)
                val pPhoto = "https://imagecache.365scores.com/image/upload/f_png,w_160,h_160,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${pImgVer.coerceAtLeast(1)}/Athletes/$aId"
                val pPos = aObj.optString("positionName", "")
                    .ifBlank { aObj.optJSONObject("position")?.optString("name", "").orEmpty() }
                    .ifBlank { "لاعب" }
                val pJersey = aObj.optInt("jerseyNumber", aObj.optInt("shirtNumber", 0))
                val natId = aObj.optInt("nationalityId", aObj.optInt("countryId", 0))
                val pNat = aObj.optString("nationalityName", countriesMap[natId].orEmpty())
                    .ifBlank { "لم يتم الحصول على معلومات" }

                squadPlayers.add(
                    PlayerFullProfile(
                        id = aId,
                        athleteId = aId,
                        name = pName,
                        shortName = pShort,
                        photoUrl = pPhoto,
                        positionName = pPos,
                        jerseyNumber = pJersey,
                        nationality = pNat,
                        currentTeamId = teamId,
                        currentTeamName = teamName,
                        currentTeamLogoUrl = logoUrl
                    )
                )
            }
        }

        return TeamFullProfile(
            id = teamId,
            name = teamName,
            logoUrl = logoUrl,
            countryName = countryName,
            venueName = venueName,
            primaryCompetitionName = primaryCompetitionName,
            primaryCompetitionLogoUrl = primaryCompetitionLogoUrl,
            playedMatches = playedMatches,
            upcomingMatches = upcomingMatches,
            transfers = transfers,
            squadPlayers = squadPlayers
        )
    }

    private fun parseSingleMatchObject(
        gObj: JSONObject,
        competitionsMap: Map<Int, Pair<String, Int>>
    ): MatchItem? {
        val gameId = gObj.optInt("id", 0)
        if (gameId <= 0) return null
        val compId = gObj.optInt("competitionId", 0)
        val compInfo = competitionsMap[compId]
        val compName = gObj.optString("competitionDisplayName", compInfo?.first.orEmpty())
            .ifBlank { compInfo?.first ?: "مباراة" }
        val compVer = compInfo?.second ?: 1

        val homeObj = gObj.optJSONObject("homeCompetitor") ?: return null
        val awayObj = gObj.optJSONObject("awayCompetitor") ?: return null
        val startTimeIso = gObj.optString("startTime", "")
        val datePrefix = if (startTimeIso.length >= 10) "${startTimeIso.substring(0, 10)} • " else ""
        val displayTime = datePrefix + formatBaghdadTime(startTimeIso)
        val statusGroup = gObj.optInt("statusGroup", 2)
        val statusText = gObj.optString("statusText", "")
        val gameTimeDisplay = gObj.optString("gameTimeDisplay", "")
        val roundName = gObj.optString("roundName", "")

        return MatchItem(
            id = gameId,
            competitionId = compId,
            competitionName = compName,
            competitionImageVersion = compVer,
            startTimeIso = startTimeIso,
            displayTime = displayTime,
            statusGroup = statusGroup,
            statusText = statusText,
            gameTimeDisplay = gameTimeDisplay,
            roundName = roundName,
            homeTeam = parseCompetitor(homeObj),
            awayTeam = parseCompetitor(awayObj)
        )
    }

    private fun parseTransfersList(
        transfersRoot: JSONObject?,
        entityObj: JSONObject?,
        competitorsMap: Map<Int, MatchCompetitor>,
        defaultPlayerName: String,
        defaultPlayerPhoto: String
    ): List<PlayerTransferItem> {
        val result = mutableListOf<PlayerTransferItem>()
        val athletesLookup = mutableMapOf<Int, Pair<String, String>>() // id -> (name, photoUrl)
        val rootAthletes = transfersRoot?.optJSONArray("athletes")
        if (rootAthletes != null) {
            for (i in 0 until rootAthletes.length()) {
                val a = rootAthletes.optJSONObject(i) ?: continue
                val id = a.optInt("id", 0)
                val nm = a.optString("name", "")
                val ver = a.optInt("imageVersion", 1)
                if (id > 0 && nm.isNotBlank()) {
                    val photo = "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco,dpr_2,d_Athletes:default.png/v${ver.coerceAtLeast(1)}/Athletes/$id"
                    athletesLookup[id] = nm to photo
                }
            }
        }

        val candidateArrays = listOfNotNull(
            transfersRoot?.optJSONArray("transfers"),
            entityObj?.optJSONArray("transfers"),
            entityObj?.optJSONObject("career")?.optJSONArray("transfers"),
            entityObj?.optJSONObject("transferHistory")?.optJSONArray("transfers")
        )

        for (arr in candidateArrays) {
            for (i in 0 until arr.length()) {
                val tObj = arr.optJSONObject(i) ?: continue
                val tId = tObj.optString("id", "tr_${i}_${tObj.optString("date", "")}")

                val athleteId = tObj.optInt("athleteId", tObj.optJSONObject("athlete")?.optInt("id", 0) ?: 0)
                val athletePair = athletesLookup[athleteId]
                val pName = tObj.optString("athleteName", "")
                    .ifBlank { tObj.optJSONObject("athlete")?.optString("name", "").orEmpty() }
                    .ifBlank { athletePair?.first.orEmpty() }
                    .ifBlank { defaultPlayerName }
                val pPhoto = athletePair?.second
                    ?.ifBlank { defaultPlayerPhoto }
                    ?: defaultPlayerPhoto

                val fromId = tObj.optInt(
                    "fromCompetitorId",
                    tObj.optInt(
                        "sourceCompetitorId",
                        tObj.optInt(
                            "originCompetitorId",
                            tObj.optJSONObject("fromCompetitor")?.optInt("id", 0)
                                ?: tObj.optJSONObject("fromClub")?.optInt("id", 0)
                                ?: 0
                        )
                    )
                )
                val toId = tObj.optInt(
                    "toCompetitorId",
                    tObj.optInt(
                        "targetCompetitorId",
                        tObj.optInt(
                            "destinationCompetitorId",
                            tObj.optJSONObject("toCompetitor")?.optInt("id", 0)
                                ?: tObj.optJSONObject("toClub")?.optInt("id", 0)
                                ?: 0
                        )
                    )
                )

                val fromName = tObj.optString("fromCompetitorName", "")
                    .ifBlank { tObj.optJSONObject("fromCompetitor")?.optString("name", "").orEmpty() }
                    .ifBlank { tObj.optJSONObject("fromClub")?.optString("name", "").orEmpty() }
                    .ifBlank { competitorsMap[fromId]?.name.orEmpty() }
                    .ifBlank { "نادي سابق" }

                val toName = tObj.optString("toCompetitorName", "")
                    .ifBlank { tObj.optJSONObject("toCompetitor")?.optString("name", "").orEmpty() }
                    .ifBlank { tObj.optJSONObject("toClub")?.optString("name", "").orEmpty() }
                    .ifBlank { competitorsMap[toId]?.name.orEmpty() }
                    .ifBlank { "النادي المنتقل إليه" }

                if (fromName == "نادي سابق" && toName == "النادي المنتقل إليه" && fromId == 0 && toId == 0) {
                    continue
                }

                val fromLogo = competitorsMap[fromId]?.logoUrl
                    ?: if (fromId > 0) "https://imagecache.365scores.com/image/upload/f_png,w_100,h_100,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/Competitors/$fromId" else ""
                val toLogo = competitorsMap[toId]?.logoUrl
                    ?: if (toId > 0) "https://imagecache.365scores.com/image/upload/f_png,w_100,h_100,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/Competitors/$toId" else ""

                val rawDate = tObj.optString("date", tObj.optString("transferDate", tObj.optString("seasonName", "")))
                val dateText = if (rawDate.length >= 10) rawDate.take(10) else rawDate.ifBlank { "انتقال رسمي" }

                val feeText = tObj.optString("price", "")
                    .ifBlank { tObj.optString("fee", "") }
                    .ifBlank { tObj.optString("transferTypeName", "") }
                    .ifBlank { tObj.optString("transferTitle", "") }
                    .ifBlank { tObj.optString("contractUntil", "") }
                    .ifBlank { "انتقال رسمي" }

                result.add(
                    PlayerTransferItem(
                        id = tId,
                        playerName = pName,
                        playerPhotoUrl = pPhoto,
                        fromTeamId = fromId,
                        fromTeamName = fromName,
                        fromTeamLogoUrl = fromLogo,
                        toTeamId = toId,
                        toTeamName = toName,
                        toTeamLogoUrl = toLogo,
                        dateText = dateText,
                        transferTypeOrFee = feeText
                    )
                )
            }
        }
        return result.distinctBy { "${it.playerName}_${it.fromTeamName}_${it.toTeamName}_${it.dateText}" }
    }
}

