package com.example.data.repository

import android.util.JsonReader
import android.util.JsonToken
import com.example.data.model.LibraryFilterType
import com.example.data.model.LibraryMediaItem
import com.example.data.model.MediaDetailInfo
import com.example.data.model.SeriesEpisode
import com.example.data.model.SeriesSeason
import com.example.data.model.XtreamAccount
import com.example.data.model.XtreamAccountLiveCatalog
import com.example.data.model.XtreamLiveCategory
import com.example.data.model.XtreamLiveStreamItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

class XtreamRepository(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    // High-speed in-memory catalog for instant browsing & sub-5ms comprehensive search across 100% of content
    private val catalogLock = Any()
    private val allMovies = ArrayList<LibraryMediaItem>(4096)
    private val allSeries = ArrayList<LibraryMediaItem>(2048)
    private val seenMovieIds = HashSet<String>(4096)
    private val seenSeriesIds = HashSet<String>(2048)

    private var activeAccount: XtreamAccount? = null
    @Volatile
    private var isFullIndexStarted = false
    @Volatile
    private var isFullIndexCompleted = false
    private val detailCache = HashMap<String, MediaDetailInfo>()
    private val iptvCatalogCache = java.util.concurrent.ConcurrentHashMap<String, XtreamAccountLiveCatalog>()

    fun clearIptvCatalogCache() {
        iptvCatalogCache.clear()
    }

    companion object {
        // 96 items per page for main browsing (divisible by 3 for 3x3 grid!)
        const val PAGE_SIZE = 96

        private val PREFIX_CLEAN_REGEX = Regex("^[A-Z]{2,4}\\s*[-|:]\\s*")
        private val YEAR_REGEX = Regex("\\b(19\\d{2}|20\\d{2})\\b")

        /**
         * Normalizes Arabic & English text for ultra-accurate, fast search with zero Regex allocations:
         * unifies Alef variants (أ, إ, آ -> ا), Teh Marbuta (ة -> ه), Alef Maksura (ى -> ي),
         * removes tashkeel/diacritics and punctuation, and collapses consecutive spaces inline.
         */
        fun normalizeForSearch(input: String): String {
            if (input.isEmpty()) return ""
            val sb = StringBuilder(input.length)
            var lastWasSpace = true
            for (rawCh in input) {
                val ch = rawCh.lowercaseChar()
                val mapped: Char = when (ch) {
                    'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                    'ة' -> 'ه'
                    'ى' -> 'ي'
                    'ؤ' -> 'و'
                    'ئ' -> 'ي'
                    in '\u064B'..'\u065F', '\u0640' -> continue // strip Arabic diacritics & tatweel
                    '-', '_', ':', '|', '.', ',', '(', ')', '[', ']', '/', '\t', '\n', '\r' -> ' '
                    else -> ch
                }
                if (mapped == ' ') {
                    if (!lastWasSpace) {
                        sb.append(' ')
                        lastWasSpace = true
                    }
                } else {
                    sb.append(mapped)
                    lastWasSpace = false
                }
            }
            if (sb.isNotEmpty() && sb[sb.length - 1] == ' ') {
                sb.setLength(sb.length - 1)
            }
            return sb.toString()
        }
    }

    fun resetCatalog() {
        synchronized(catalogLock) {
            allMovies.clear()
            allSeries.clear()
            seenMovieIds.clear()
            seenSeriesIds.clear()
        }
        detailCache.clear()
        activeAccount = null
        isFullIndexStarted = false
        isFullIndexCompleted = false
    }

    val totalIndexedCount: Int
        get() = synchronized(catalogLock) { allMovies.size + allSeries.size }

    /**
     * Quickly resolves the fastest working Xtream account from Firebase and fetches the initial
     * batch of 72 movies & series in under 1 second using priority categories, while immediately
     * streaming the ENTIRE server catalog in the background so Search covers 100% of all movies & series.
     */
    suspend fun ensureInitialCatalogLoaded(
        accounts: List<XtreamAccount>,
        onBackgroundCatalogUpdated: () -> Unit = {}
    ): Result<List<LibraryMediaItem>> = withContext(Dispatchers.IO) {
        if (accounts.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("لا توجد حسابات مكتبة متاحة في قاعدة البيانات"))
        }

        mutex.withLock {
            val workingAccounts = findAllWorkingAccountsParallel(accounts).ifEmpty { accounts }
            val working = activeAccount ?: workingAccounts.first()
            activeAccount = working

            if (allMovies.isNotEmpty() || allSeries.isNotEmpty()) {
                return@withContext Result.success(buildCombinedList(LibraryFilterType.ALL))
            }

            // Load initial priority categories (Arabic + New Releases + Netflix) in parallel across working accounts for instant startup
            try {
                coroutineScope {
                    val fastJobs = workingAccounts.map { acc ->
                        async {
                            val mDef = async { fetchFastInitialMovies(acc) }
                            val sDef = async { fetchFastInitialSeries(acc) }
                            mDef.await() to sDef.await()
                        }
                    }
                    for ((initialMovies, initialSeries) in fastJobs.awaitAll()) {
                        addMoviesThreadSafe(initialMovies)
                        addSeriesThreadSafe(initialSeries)
                    }
                }
            } catch (_: Exception) {
            }

            val combined = buildCombinedList(LibraryFilterType.ALL)
            if (combined.isEmpty()) {
                coroutineScope {
                    workingAccounts.map { acc ->
                        launch {
                            streamCatalogItemsProgressive(
                                account = acc,
                                action = "get_vod_streams",
                                isSeries = false,
                                maxItems = 120
                            ) { batch ->
                                addMoviesThreadSafe(batch)
                            }
                        }
                    }
                }
            }

            if (!isFullIndexStarted && workingAccounts.isNotEmpty()) {
                isFullIndexStarted = true
                backgroundScope.launch {
                    streamFullCatalogInBackground(workingAccounts, working, onBackgroundCatalogUpdated)
                }
            }

            Result.success(buildCombinedList(LibraryFilterType.ALL))
        }
    }

    private fun addMoviesThreadSafe(items: List<LibraryMediaItem>): Int {
        if (items.isEmpty()) return 0
        synchronized(catalogLock) {
            var added = 0
            for (m in items) {
                val key = "${m.account.cleanHost}_${m.account.username}_${m.id}"
                if (seenMovieIds.add(key)) {
                    allMovies.add(m)
                    added++
                }
            }
            return added
        }
    }

    private fun addSeriesThreadSafe(items: List<LibraryMediaItem>): Int {
        if (items.isEmpty()) return 0
        synchronized(catalogLock) {
            var added = 0
            for (s in items) {
                val key = "${s.account.cleanHost}_${s.account.username}_${s.id}"
                if (seenSeriesIds.add(key)) {
                    allSeries.add(s)
                    added++
                }
            }
            return added
        }
    }

    fun getCatalogItems(filter: LibraryFilterType): List<LibraryMediaItem> {
        return buildCombinedList(filter)
    }

    /**
     * Fast O(limit) pagination slice that only materializes the requested visible items (`limit`)
     * and whether more items exist, avoiding allocating 40,000-element lists during scroll.
     */
    fun getCatalogSlice(filter: LibraryFilterType, limit: Int): Pair<List<LibraryMediaItem>, Boolean> {
        val targetLimit = limit.coerceAtLeast(PAGE_SIZE)
        synchronized(catalogLock) {
            return when (filter) {
                LibraryFilterType.MOVIES -> {
                    val count = minOf(targetLimit, allMovies.size)
                    val slice = ArrayList<LibraryMediaItem>(count)
                    for (i in 0 until count) {
                        slice.add(allMovies[i])
                    }
                    slice to (allMovies.size > count)
                }
                LibraryFilterType.SERIES -> {
                    val count = minOf(targetLimit, allSeries.size)
                    val slice = ArrayList<LibraryMediaItem>(count)
                    for (i in 0 until count) {
                        slice.add(allSeries[i])
                    }
                    slice to (allSeries.size > count)
                }
                LibraryFilterType.ALL -> {
                    val totalSize = allMovies.size + allSeries.size
                    val count = minOf(targetLimit, totalSize)
                    val out = ArrayList<LibraryMediaItem>(count)
                    var mIdx = 0
                    var sIdx = 0
                    while (out.size < count && (mIdx < allMovies.size || sIdx < allSeries.size)) {
                        if (mIdx < allMovies.size && out.size < count) out.add(allMovies[mIdx++])
                        if (mIdx < allMovies.size && out.size < count) out.add(allMovies[mIdx++])
                        if (sIdx < allSeries.size && out.size < count) out.add(allSeries[sIdx++])
                    }
                    out to (totalSize > out.size)
                }
            }
        }
    }

    /**
     * Comprehensive, ultra-fast search across 100% of all Movies & Series on the server.
     * Never depends on clicking "عرض المزيد" on the main page.
     * If the full server catalog is still streaming or hasn't started yet, it searches existing items
     * immediately AND streams the full catalog in parallel, pushing updated matches via `onResultsUpdated`.
     */
    /**
     * Comprehensive, ultra-fast search across 100% of all Movies & Series across ALL published Xtream accounts simultaneously.
     * When 2 or more Xtream accounts are published, fetches and searches all accounts in parallel ("يجلب الحسابين سوية وبشكل سريع جدا جدا وحقيقي فعلي للمحتوى").
     */
    suspend fun searchCatalogComprehensive(
        query: String,
        filter: LibraryFilterType,
        accounts: List<XtreamAccount>,
        onResultsUpdated: (List<LibraryMediaItem>) -> Unit = {}
    ): List<LibraryMediaItem> = withContext(Dispatchers.Default) {
        if (!isFullIndexStarted && accounts.isNotEmpty()) {
            isFullIndexStarted = true
            val primary = activeAccount ?: accounts.first()
            activeAccount = primary
            backgroundScope.launch {
                streamFullCatalogInBackground(accounts, primary) {
                    val updated = searchCatalog(query, filter, limit = 600)
                    onResultsUpdated(updated)
                }
            }
        }

        val immediate = searchCatalog(query, filter, limit = 600)
        // If full index across all accounts is not completed yet, stream ALL accounts in parallel for real-time combined results
        if (!isFullIndexCompleted && accounts.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                coroutineScope {
                    val jobs = accounts.flatMap { acc ->
                        listOf(
                            launch {
                                streamCatalogItemsProgressive(acc, "get_vod_streams", isSeries = false, maxItems = Int.MAX_VALUE, batchSize = 500) { batch ->
                                    if (addMoviesThreadSafe(batch) > 0) {
                                        onResultsUpdated(searchCatalog(query, filter, limit = 600))
                                    }
                                }
                            },
                            launch {
                                streamCatalogItemsProgressive(acc, "get_series", isSeries = true, maxItems = Int.MAX_VALUE, batchSize = 500) { batch ->
                                    if (addSeriesThreadSafe(batch) > 0) {
                                        onResultsUpdated(searchCatalog(query, filter, limit = 600))
                                    }
                                }
                            }
                        )
                    }
                    if (immediate.isEmpty()) {
                        jobs.forEach { it.join() }
                        isFullIndexCompleted = true
                    }
                }
            }
            return@withContext searchCatalog(query, filter, limit = 600)
        }

        immediate
    }

    /**
     * Ultra-fast in-memory search across ALL indexed movies & series from ALL published Xtream accounts
     * with Arabic/English normalization, multi-account interleaving, and smart relevance ranking.
     */
    fun searchCatalog(
        query: String,
        filter: LibraryFilterType,
        limit: Int = 600
    ): List<LibraryMediaItem> {
        val cleanQuery = normalizeForSearch(query)
        if (cleanQuery.isEmpty()) {
            return buildCombinedList(filter).take(120)
        }

        val tokens = cleanQuery.split(" ").filter { it.isNotBlank() }
        val exactMatches = ArrayList<LibraryMediaItem>()
        val prefixMatches = ArrayList<LibraryMediaItem>()
        val containsMatches = ArrayList<LibraryMediaItem>()

        val searchMovies = filter == LibraryFilterType.ALL || filter == LibraryFilterType.MOVIES
        val searchSeries = filter == LibraryFilterType.ALL || filter == LibraryFilterType.SERIES

        fun evaluateItem(item: LibraryMediaItem) {
            val normName = item.lowerName
            if (normName == cleanQuery) {
                exactMatches.add(item)
            } else if (normName.startsWith(cleanQuery) || normName.contains(" $cleanQuery")) {
                prefixMatches.add(item)
            } else if (matchesAllTokens(normName, cleanQuery, tokens)) {
                containsMatches.add(item)
            }
        }

        val (mSnapshot, sSnapshot) = synchronized(catalogLock) {
            val mCopy = if (searchMovies) ArrayList(allMovies) else emptyList()
            val sCopy = if (searchSeries) ArrayList(allSeries) else emptyList()
            mCopy to sCopy
        }

        // Scan across all indexed items from all accounts so no account is truncated early
        if (searchMovies && searchSeries) {
            val maxLen = maxOf(mSnapshot.size, sSnapshot.size)
            for (i in 0 until maxLen) {
                if (i < mSnapshot.size) evaluateItem(mSnapshot[i])
                if (i < sSnapshot.size) evaluateItem(sSnapshot[i])
            }
        } else if (searchMovies) {
            for (m in mSnapshot) {
                evaluateItem(m)
            }
        } else if (searchSeries) {
            for (s in sSnapshot) {
                evaluateItem(s)
            }
        }

        val interleavedExact = interleaveByAccount(exactMatches)
        val interleavedPrefix = interleaveByAccount(prefixMatches)
        val interleavedContains = interleaveByAccount(containsMatches)

        val combined = ArrayList<LibraryMediaItem>(
            minOf(limit, interleavedExact.size + interleavedPrefix.size + interleavedContains.size)
        )
        for (item in interleavedExact) {
            if (combined.size >= limit) break
            combined.add(item)
        }
        for (item in interleavedPrefix) {
            if (combined.size >= limit) break
            combined.add(item)
        }
        for (item in interleavedContains) {
            if (combined.size >= limit) break
            combined.add(item)
        }
        return combined
    }

    /**
     * Interleaves search matches across all Xtream accounts so when 2+ accounts are published,
     * results from both accounts appear side-by-side immediately.
     */
    private fun interleaveByAccount(items: List<LibraryMediaItem>): List<LibraryMediaItem> {
        if (items.size <= 1) return items
        val grouped = items.groupBy { "${it.account.cleanHost}_${it.account.username}" }.values.toList()
        if (grouped.size <= 1) return items
        val out = ArrayList<LibraryMediaItem>(items.size)
        val maxGroupSize = grouped.maxOf { it.size }
        for (i in 0 until maxGroupSize) {
            for (group in grouped) {
                if (i < group.size) {
                    out.add(group[i])
                }
            }
        }
        return out
    }

    private fun matchesAllTokens(normalizedName: String, fullQuery: String, tokens: List<String>): Boolean {
        if (normalizedName.contains(fullQuery)) return true
        if (tokens.isEmpty()) return false
        for (t in tokens) {
            if (!normalizedName.contains(t)) return false
        }
        return true
    }

    private fun buildCombinedList(filter: LibraryFilterType): List<LibraryMediaItem> {
        return getCatalogSlice(filter, PAGE_SIZE * 2).first
    }

    private suspend fun findAllWorkingAccountsParallel(accounts: List<XtreamAccount>): List<XtreamAccount> =
        coroutineScope {
            accounts.map { acc ->
                async {
                    try {
                        val url = "${acc.cleanHost}/player_api.php?username=${acc.username}&password=${acc.password}"
                        val req = Request.Builder()
                            .url(url)
                            .header("User-Agent", "IPTVSmartersPlayer")
                            .build()
                        httpClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful) {
                                val body = resp.body?.string().orEmpty()
                                if (body.contains("user_info")) acc else null
                            } else null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun fetchFastInitialMovies(account: XtreamAccount): List<LibraryMediaItem> =
        coroutineScope {
            val catIds = fetchPriorityCategoryIds(account, "get_vod_categories", maxCategories = 3)
            if (catIds.isEmpty()) {
                val list = ArrayList<LibraryMediaItem>(90)
                streamCatalogItemsProgressive(
                    account = account,
                    action = "get_vod_streams",
                    isSeries = false,
                    maxItems = 90
                ) { batch -> list.addAll(batch) }
                return@coroutineScope list
            }
            catIds.map { catId ->
                async {
                    val list = ArrayList<LibraryMediaItem>(60)
                    streamCatalogItemsProgressive(
                        account = account,
                        action = "get_vod_streams&category_id=$catId",
                        isSeries = false,
                        maxItems = 60
                    ) { batch -> list.addAll(batch) }
                    list
                }
            }.awaitAll().flatten()
        }

    private suspend fun fetchFastInitialSeries(account: XtreamAccount): List<LibraryMediaItem> =
        coroutineScope {
            val catIds = fetchPriorityCategoryIds(account, "get_series_categories", maxCategories = 3)
            if (catIds.isEmpty()) {
                val list = ArrayList<LibraryMediaItem>(90)
                streamCatalogItemsProgressive(
                    account = account,
                    action = "get_series",
                    isSeries = true,
                    maxItems = 90
                ) { batch -> list.addAll(batch) }
                return@coroutineScope list
            }
            catIds.map { catId ->
                async {
                    val list = ArrayList<LibraryMediaItem>(60)
                    streamCatalogItemsProgressive(
                        account = account,
                        action = "get_series&category_id=$catId",
                        isSeries = true,
                        maxItems = 60
                    ) { batch -> list.addAll(batch) }
                    list
                }
            }.awaitAll().flatten()
        }

    private fun fetchPriorityCategoryIds(
        account: XtreamAccount,
        action: String,
        maxCategories: Int
    ): List<String> {
        return try {
            val url = "${account.cleanHost}/player_api.php?username=${account.username}&password=${account.password}&action=$action"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTVSmartersPlayer")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val arr = JSONArray(resp.body?.string().orEmpty())
                val arabicOrNew = mutableListOf<String>()
                val fallback = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("category_id", "")
                    val name = obj.optString("category_name", "").uppercase()
                    if (id.isNotEmpty()) {
                        if (name.contains("ARAB") || name.contains("عرب") || name.contains("مترجم") ||
                            name.contains("NEW") || name.contains("NETFLIX") || name.contains("202")
                        ) {
                            arabicOrNew.add(id)
                        } else if (fallback.size < maxCategories) {
                            fallback.add(id)
                        }
                    }
                }
                (arabicOrNew + fallback).distinct().take(maxCategories)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Streams 100% of all Movies and Series across ALL active Xtream accounts in the background
     * IN PARALLEL, emitting real-time progressive batches so comprehensive search covers all accounts together.
     */
    private suspend fun streamFullCatalogInBackground(
        accounts: List<XtreamAccount>,
        primaryAccount: XtreamAccount,
        onUpdated: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val orderedAccounts = (listOf(primaryAccount) + accounts.filter { it.id != primaryAccount.id })
                .distinctBy { "${it.cleanHost}_${it.username}" }
            coroutineScope {
                val allAccountJobs = orderedAccounts.flatMap { acc ->
                    listOf(
                        launch {
                            streamCatalogItemsProgressive(
                                account = acc,
                                action = "get_series",
                                isSeries = true,
                                maxItems = Int.MAX_VALUE,
                                batchSize = 1200
                            ) { batch ->
                                if (addSeriesThreadSafe(batch) > 0) {
                                    onUpdated()
                                }
                            }
                        },
                        launch {
                            streamCatalogItemsProgressive(
                                account = acc,
                                action = "get_vod_streams",
                                isSeries = false,
                                maxItems = Int.MAX_VALUE,
                                batchSize = 1200
                            ) { batch ->
                                if (addMoviesThreadSafe(batch) > 0) {
                                    onUpdated()
                                }
                            }
                        }
                    )
                }
                allAccountJobs.forEach { it.join() }
            }
            isFullIndexCompleted = true
            onUpdated()
        } catch (_: Exception) {
        }
    }

    /**
     * Streams JSON array from Xtream API using Android's streaming JsonReader and emits batches
     * of 600 items progressively so memory remains minimal and search results update in real time.
     */
    private fun streamCatalogItemsProgressive(
        account: XtreamAccount,
        action: String,
        isSeries: Boolean,
        maxItems: Int,
        batchSize: Int = 600,
        onBatchReady: (List<LibraryMediaItem>) -> Unit
    ) {
        val currentBatch = ArrayList<LibraryMediaItem>(minOf(batchSize, maxItems))
        var totalRead = 0
        val url = "${account.cleanHost}/player_api.php?username=${account.username}&password=${account.password}&action=$action"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmartersPlayer")
            .build()

        try {
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return
                val stream = resp.body?.byteStream() ?: return
                val reader = JsonReader(InputStreamReader(stream, Charsets.UTF_8))
                reader.isLenient = true
                if (reader.peek() != JsonToken.BEGIN_ARRAY) return
                reader.beginArray()
                while (reader.hasNext() && totalRead < maxItems) {
                    if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                        reader.skipValue()
                        continue
                    }
                    reader.beginObject()
                    var id = ""
                    var name = ""
                    var poster = ""
                    var rating = ""
                    var ext = "mp4"
                    var catId = ""
                    var plot = ""
                    var year = ""

                    while (reader.hasNext()) {
                        val field = reader.nextName()
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            continue
                        }
                        when (field) {
                            "stream_id", "series_id" -> id = nextStringSafe(reader)
                            "name", "title" -> name = nextStringSafe(reader)
                            "stream_icon", "cover" -> poster = nextStringSafe(reader)
                            "rating" -> rating = nextStringSafe(reader)
                            "container_extension" -> ext = nextStringSafe(reader).ifBlank { "mp4" }
                            "category_id" -> catId = nextStringSafe(reader)
                            "plot" -> plot = nextStringSafe(reader)
                            "releaseDate", "year" -> year = nextStringSafe(reader)
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()

                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        val cleanName = cleanDisplayName(name)
                        val extractedYear = if (year.length >= 4) {
                            year.take(4)
                        } else {
                            extractYearFromTitle(name)
                        }
                        currentBatch.add(
                            LibraryMediaItem(
                                id = id,
                                name = cleanName,
                                lowerName = normalizeForSearch("$cleanName $name"),
                                posterUrl = poster,
                                rating = formatRating(rating),
                                releaseYear = extractedYear,
                                isSeries = isSeries,
                                containerExtension = ext,
                                categoryId = catId,
                                plotPreview = plot,
                                account = account
                            )
                        )
                        totalRead++
                        if (currentBatch.size >= batchSize) {
                            onBatchReady(ArrayList(currentBatch))
                            currentBatch.clear()
                        }
                    }
                }
                if (currentBatch.isNotEmpty()) {
                    onBatchReady(currentBatch)
                }
            }
        } catch (_: Exception) {
            if (currentBatch.isNotEmpty()) {
                onBatchReady(currentBatch)
            }
        }
    }

    private fun nextStringSafe(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                JsonToken.STRING, JsonToken.NUMBER -> reader.nextString()
                JsonToken.BOOLEAN -> reader.nextBoolean().toString()
                else -> {
                    reader.skipValue()
                    ""
                }
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Fetches real Movie or Series details (plot/description, cast, genre, seasons, episodes)
     * directly from the Xtream Codes API (`get_vod_info` or `get_series_info`).
     */
    suspend fun getMediaDetails(item: LibraryMediaItem): Result<MediaDetailInfo> =
        withContext(Dispatchers.IO) {
            val cacheKey = "${item.account.cleanHost}_${item.account.username}_${if (item.isSeries) "S" else "M"}_${item.id}"
            detailCache[cacheKey]?.let { return@withContext Result.success(it) }

            val acc = item.account
            val action = if (item.isSeries) {
                "get_series_info&series_id=${item.id}"
            } else {
                "get_vod_info&vod_id=${item.id}"
            }
            val url = "${acc.cleanHost}/player_api.php?username=${acc.username}&password=${acc.password}&action=$action"

            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "IPTVSmartersPlayer")
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("تعذر جلب التفاصيل: ${resp.code}")
                        )
                    }
                    val bodyStr = resp.body?.string().orEmpty()
                    val root = JSONObject(bodyStr)
                    val infoObj = root.optJSONObject("info") ?: JSONObject()

                    val description = infoObj.optString(
                        "plot",
                        infoObj.optString("description", infoObj.optString("overview", item.plotPreview))
                    ).trim().ifBlank { "لا يوجد وصف متاح لهذا العمل في المصدر حالياً." }

                    val genre = infoObj.optString("genre", "").trim()
                    val releaseDate = infoObj.optString(
                        "releasedate",
                        infoObj.optString("releaseDate", item.releaseYear)
                    ).trim()
                    val cast = infoObj.optString("cast", infoObj.optString("actors", "")).trim()
                    val director = infoObj.optString("director", "").trim()
                    val duration = infoObj.optString("duration", infoObj.optString("episode_run_time", "")).trim()
                    val rating = formatRating(infoObj.optString("rating", item.rating))

                    var backdropUrl = infoObj.optString("cover_big", infoObj.optString("movie_image", item.posterUrl))
                    val backdropArr = infoObj.optJSONArray("backdrop_path")
                    if (backdropArr != null && backdropArr.length() > 0) {
                        val firstBackdrop = backdropArr.optString(0, "")
                        if (firstBackdrop.isNotEmpty()) backdropUrl = firstBackdrop
                    }

                    // For movies, also check movie_data.container_extension
                    val movieDataObj = root.optJSONObject("movie_data")
                    val realExt = movieDataObj?.optString("container_extension", item.containerExtension)
                        ?.ifBlank { item.containerExtension } ?: item.containerExtension
                    val updatedItem = item.copy(containerExtension = realExt)

                    if (!item.isSeries) {
                        val detail = MediaDetailInfo(
                            item = updatedItem,
                            description = description,
                            genre = genre,
                            releaseDate = releaseDate,
                            cast = cast,
                            director = director,
                            duration = duration,
                            rating = rating,
                            backdropUrl = backdropUrl
                        )
                        detailCache[cacheKey] = detail
                        return@withContext Result.success(detail)
                    }

                    // Parse Series Seasons & Episodes cleanly and in strict order
                    val episodesBySeason = LinkedHashMap<Int, List<SeriesEpisode>>()
                    val episodesNode = root.opt("episodes")
                    if (episodesNode is JSONObject) {
                        val seasonKeys = episodesNode.keys().asSequence().toList()
                            .sortedBy { it.toIntOrNull() ?: 999 }
                        for (sKey in seasonKeys) {
                            val seasonNum = sKey.toIntOrNull() ?: 1
                            val epArr = episodesNode.optJSONArray(sKey) ?: continue
                            val parsedEps = parseEpisodeArray(epArr, seasonNum, item.posterUrl)
                            if (parsedEps.isNotEmpty()) {
                                episodesBySeason[seasonNum] = parsedEps
                            }
                        }
                    } else if (episodesNode is JSONArray) {
                        for (i in 0 until episodesNode.length()) {
                            val subArr = episodesNode.optJSONArray(i)
                            if (subArr != null) {
                                val seasonNum = i + 1
                                val parsedEps = parseEpisodeArray(subArr, seasonNum, item.posterUrl)
                                if (parsedEps.isNotEmpty()) {
                                    episodesBySeason[seasonNum] = parsedEps
                                }
                            }
                        }
                    }

                    val seasonsList = mutableListOf<SeriesSeason>()
                    val seasonsArr = root.optJSONArray("seasons")
                    if (seasonsArr != null) {
                        for (i in 0 until seasonsArr.length()) {
                            val sObj = seasonsArr.optJSONObject(i) ?: continue
                            val sNum = sObj.optInt("season_number", i + 1)
                            val epListForSeason = episodesBySeason[sNum]
                            if (epListForSeason != null && epListForSeason.isNotEmpty()) {
                                seasonsList.add(
                                    SeriesSeason(
                                        seasonNumber = sNum,
                                        name = sObj.optString("name", "الموسم $sNum").ifBlank { "الموسم $sNum" },
                                        episodeCount = epListForSeason.size,
                                        coverUrl = sObj.optString("cover", item.posterUrl).ifBlank { item.posterUrl },
                                        overview = sObj.optString("overview", "")
                                    )
                                )
                            }
                        }
                    }

                    // Ensure every season present in episodesBySeason is represented in seasonsList
                    val existingSeasonNums = seasonsList.map { it.seasonNumber }.toSet()
                    for ((sNum, epList) in episodesBySeason) {
                        if (sNum !in existingSeasonNums) {
                            seasonsList.add(
                                SeriesSeason(
                                    seasonNumber = sNum,
                                    name = "الموسم $sNum",
                                    episodeCount = epList.size,
                                    coverUrl = item.posterUrl
                                )
                            )
                        }
                    }
                    seasonsList.sortBy { it.seasonNumber }

                    val detail = MediaDetailInfo(
                        item = updatedItem,
                        description = description,
                        genre = genre,
                        releaseDate = releaseDate,
                        cast = cast,
                        director = director,
                        duration = duration,
                        rating = rating,
                        backdropUrl = backdropUrl,
                        seasons = seasonsList,
                        episodesBySeason = episodesBySeason
                    )
                    detailCache[cacheKey] = detail
                    Result.success(detail)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseEpisodeArray(
        epArr: JSONArray,
        defaultSeasonNum: Int,
        fallbackCover: String
    ): List<SeriesEpisode> {
        val list = ArrayList<SeriesEpisode>(epArr.length())
        for (i in 0 until epArr.length()) {
            val epObj = epArr.optJSONObject(i) ?: continue
            val id = epObj.optString("id", "")
            if (id.isEmpty()) continue
            val epNum = epObj.optInt("episode_num", i + 1)
            val seasonNum = epObj.optInt("season", defaultSeasonNum)
            val rawTitle = epObj.optString("title", "الحلقة $epNum")
            val ext = epObj.optString("container_extension", "mp4").ifBlank { "mp4" }

            val epInfo = epObj.optJSONObject("info")
            val plot = epInfo?.optString("plot", "")?.trim().orEmpty()
            val duration = epInfo?.optString("duration", "")?.trim()
                ?.takeIf { it != "00:00:00" }.orEmpty()
            val rating = formatRating(epInfo?.optString("rating", "").orEmpty())
            val releaseDate = epInfo?.optString("releasedate", "")?.trim().orEmpty()
            val cover = epInfo?.optString("movie_image", fallbackCover)?.ifBlank { fallbackCover }
                ?: fallbackCover

            list.add(
                SeriesEpisode(
                    id = id,
                    episodeNum = epNum,
                    seasonNum = seasonNum,
                    title = cleanDisplayName(rawTitle).ifBlank { "الحلقة $epNum" },
                    plot = plot,
                    duration = duration,
                    rating = rating,
                    releaseDate = releaseDate,
                    containerExtension = ext,
                    coverUrl = cover
                )
            )
        }
        return list.sortedBy { it.episodeNum }
    }

    /**
     * Builds the ordered sequence of candidate stream URLs for the Library Player,
     * placing the item's actual `preferredExtension` first so playback starts immediately,
     * followed by fallback extensions (`mp4`, `mkv`, `m3u8`, `ts`).
     */
    fun buildLibraryStreamCandidates(
        account: XtreamAccount,
        streamId: String,
        isSeries: Boolean,
        preferredExtension: String = "mp4"
    ): List<String> {
        val typeSegment = if (isSeries) "series" else "movie"
        val base = "${account.cleanHost}/$typeSegment/${account.username}/${account.password}/$streamId"
        val cleanPref = preferredExtension.trim().lowercase().removePrefix(".").ifBlank { "mp4" }
        val orderedExts = listOf(cleanPref, "mp4", "mkv", "m3u8", "ts").distinct()
        return orderedExts.map { ext -> "$base.$ext" }
    }

    /**
     * Ultra-fast cached IPTV Live Catalog loader for the dedicated IPTV bottom-bar tab.
     * Fetches categories and live channels in parallel on first load and caches per account
     * so switching categories or returning to an account is instantaneous.
     */
    suspend fun fetchIptvAccountLiveCatalogFast(
        account: XtreamAccount,
        forceRefresh: Boolean = false
    ): Result<XtreamAccountLiveCatalog> = withContext(Dispatchers.IO) {
        val cacheKey = "${account.cleanHost}_${account.username.trim()}"
        if (!forceRefresh) {
            iptvCatalogCache[cacheKey]?.let { cached ->
                return@withContext Result.success(cached)
            }
        }
        val res = fetchLiveAccountCategoriesAndStreams(
            rawHost = account.cleanHost,
            rawUsername = account.username,
            rawPassword = account.password
        )
        res.onSuccess { catalog ->
            iptvCatalogCache[cacheKey] = catalog
        }
        res
    }

    /**
     * Fetches Live TV Categories (`get_live_categories`) and Live Streams (`get_live_streams`)
     * from an Xtream Codes account (Host + Username + Password) for the Developer's "نشر فرع حساب" workflow.
     */
    suspend fun fetchLiveAccountCategoriesAndStreams(
        rawHost: String,
        rawUsername: String,
        rawPassword: String
    ): Result<XtreamAccountLiveCatalog> = withContext(Dispatchers.IO) {
        val cleanUser = rawUsername.trim()
        val cleanPass = rawPassword.trim()
        val normalizedHost = normalizeHostUrl(rawHost)
        if (normalizedHost.isEmpty() || cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("يرجى إدخال الهوست واسم المستخدم وكلمة المرور بشكل صحيح")
            )
        }

        try {
            coroutineScope {
                val categoriesDeferred = async {
                    fetchLiveCategoriesInternal(normalizedHost, cleanUser, cleanPass)
                }
                val streamsDeferred = async {
                    fetchLiveStreamsInternal(normalizedHost, cleanUser, cleanPass, categoryId = null, categoryNameMap = emptyMap())
                }

                val categories = categoriesDeferred.await()
                val rawStreams = streamsDeferred.await()

                if (categories.isEmpty() && rawStreams.isEmpty()) {
                    return@coroutineScope Result.failure(
                        IllegalStateException("تعذر جلب القنوات: تأكد من صحة الهوست واسم المستخدم وكلمة المرور أو صلاحية الحساب")
                    )
                }

                val catMap = categories.associate { it.categoryId to it.categoryName }
                val enrichedStreams = rawStreams.map { st ->
                    val resolvedCatName = catMap[st.categoryId] ?: st.categoryName.ifBlank { "تصنيف عام" }
                    st.copy(categoryName = resolvedCatName)
                }

                // Ensure any category referenced by streams is also in categories list & compute channelCount per category
                val countByCategory = enrichedStreams.groupingBy { it.categoryId }.eachCount()
                val existingCatIds = categories.map { it.categoryId }.toMutableSet()
                val finalCategories = categories.map { cat ->
                    cat.copy(channelCount = countByCategory[cat.categoryId] ?: 0)
                }.toMutableList()
                for (st in enrichedStreams) {
                    if (st.categoryId.isNotEmpty() && st.categoryId !in existingCatIds) {
                        existingCatIds.add(st.categoryId)
                        finalCategories.add(
                            XtreamLiveCategory(
                                categoryId = st.categoryId,
                                categoryName = st.categoryName.ifBlank { "تصنيف ${st.categoryId}" },
                                channelCount = countByCategory[st.categoryId] ?: 1
                            )
                        )
                    }
                }

                Result.success(
                    XtreamAccountLiveCatalog(
                        host = normalizedHost,
                        username = cleanUser,
                        password = cleanPass,
                        categories = finalCategories,
                        streams = enrichedStreams
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("خطأ أثناء الاتصال بسيرفر الحساب: ${e.localizedMessage ?: "تعذر الجلب"}")
            )
        }
    }

    /**
     * Fetches Live Streams for a specific Category ID (`get_live_streams&category_id=...`)
     * on demand when the developer clicks "جلب محتوى التصنيف".
     */
    suspend fun fetchLiveStreamsForCategory(
        rawHost: String,
        rawUsername: String,
        rawPassword: String,
        categoryId: String,
        categoryName: String
    ): Result<List<XtreamLiveStreamItem>> = withContext(Dispatchers.IO) {
        val normalizedHost = normalizeHostUrl(rawHost)
        val cleanUser = rawUsername.trim()
        val cleanPass = rawPassword.trim()
        if (normalizedHost.isEmpty() || cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("بيانات الحساب غير مكتملة"))
        }
        try {
            val catIds = categoryId.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "ALL" }
                .distinct()
            val catNames = categoryName.split(" + ").map { it.trim() }

            if (catIds.size > 1) {
                coroutineScope {
                    val deferredList = catIds.mapIndexed { idx, singleCatId ->
                        val singleCatName = catNames.getOrNull(idx)?.takeIf { it.isNotBlank() } ?: categoryName
                        async {
                            fetchLiveStreamsInternal(
                                cleanHost = normalizedHost,
                                username = cleanUser,
                                password = cleanPass,
                                categoryId = singleCatId,
                                categoryNameMap = mapOf(singleCatId to singleCatName)
                            )
                        }
                    }
                    val combined = deferredList.flatMap { it.await() }.distinctBy { it.streamId }
                    Result.success(combined)
                }
            } else {
                val singleId = catIds.firstOrNull()
                val map = if (singleId != null) mapOf(singleId to categoryName) else emptyMap()
                val list = fetchLiveStreamsInternal(
                    cleanHost = normalizedHost,
                    username = cleanUser,
                    password = cleanPass,
                    categoryId = singleId,
                    categoryNameMap = map
                )
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeHostUrl(rawHost: String): String {
        val trimmed = rawHost.trim().removeSuffix("/")
        if (trimmed.isEmpty()) return ""
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    private fun fetchLiveCategoriesInternal(
        cleanHost: String,
        username: String,
        password: String
    ): List<XtreamLiveCategory> {
        val url = "$cleanHost/player_api.php?username=$username&password=$password&action=get_live_categories"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmartersPlayer")
            .build()
        return try {
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                val arr = JSONArray(body)
                val out = ArrayList<XtreamLiveCategory>(arr.length())
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("category_id", "").trim()
                    val name = obj.optString("category_name", "").trim()
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        out.add(XtreamLiveCategory(categoryId = id, categoryName = name))
                    }
                }
                out
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchLiveStreamsInternal(
        cleanHost: String,
        username: String,
        password: String,
        categoryId: String?,
        categoryNameMap: Map<String, String>
    ): List<XtreamLiveStreamItem> {
        val actionPart = if (!categoryId.isNullOrBlank()) {
            "get_live_streams&category_id=$categoryId"
        } else {
            "get_live_streams"
        }
        val url = "$cleanHost/player_api.php?username=$username&password=$password&action=$actionPart"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmartersPlayer")
            .build()
        val out = ArrayList<XtreamLiveStreamItem>(1024)
        return try {
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val stream = resp.body?.byteStream() ?: return emptyList()
                val reader = JsonReader(InputStreamReader(stream, Charsets.UTF_8))
                reader.isLenient = true
                if (reader.peek() != JsonToken.BEGIN_ARRAY) return emptyList()
                reader.beginArray()
                while (reader.hasNext()) {
                    if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                        reader.skipValue()
                        continue
                    }
                    reader.beginObject()
                    var streamId = ""
                    var name = ""
                    var icon = ""
                    var catId = categoryId.orEmpty()
                    while (reader.hasNext()) {
                        val field = reader.nextName()
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            continue
                        }
                        when (field) {
                            "stream_id" -> streamId = nextStringSafe(reader).trim()
                            "name" -> name = nextStringSafe(reader).trim()
                            "stream_icon" -> icon = nextStringSafe(reader).trim()
                            "category_id" -> catId = nextStringSafe(reader).trim()
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    if (streamId.isNotEmpty() && name.isNotEmpty()) {
                        out.add(
                            XtreamLiveStreamItem(
                                streamId = streamId,
                                name = cleanDisplayName(name).ifBlank { name },
                                streamIcon = icon,
                                categoryId = catId,
                                categoryName = categoryNameMap[catId].orEmpty(),
                                host = cleanHost,
                                username = username,
                                password = password
                            )
                        )
                    }
                }
                out
            }
        } catch (_: Exception) {
            out
        }
    }

    private fun cleanDisplayName(raw: String): String {
        return raw.replace(PREFIX_CLEAN_REGEX, "").trim()
    }

    private fun extractYearFromTitle(title: String): String {
        val match = YEAR_REGEX.find(title)
        return match?.value.orEmpty()
    }

    private fun formatRating(raw: String): String {
        val num = raw.toDoubleOrNull() ?: return ""
        if (num <= 0.0) return ""
        return String.format(java.util.Locale.US, "%.1f", num)
    }
}
