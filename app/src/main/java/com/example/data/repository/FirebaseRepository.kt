package com.example.data.repository

import com.example.data.model.AppVersionConfig
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import com.example.data.model.EventBanner
import com.example.data.model.FirebaseHomePayload
import com.example.data.model.LiveChannel
import com.example.data.model.LiveEventMatch
import com.example.data.model.MatchPublishedChannel
import com.example.data.model.PublishedEventItem
import com.example.data.model.XtreamAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseRepository(
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    private val mutex = Mutex()
    private var cachedPayload: FirebaseHomePayload? = null
    private var lastFetchTimeMs: Long = 0L

    private var developerIdToken: String? = null
    private var developerRefreshToken: String? = null
    private var developerEmail: String? = null

    private val cacheTtlMs = 10 * 60 * 1000L
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val isDeveloperLoggedIn: Boolean
        get() = !developerIdToken.isNullOrBlank()

    val currentDeveloperEmail: String
        get() = developerEmail.orEmpty()

    /**
     * Authenticates the developer using Email & Password against Firebase Authentication REST API
     * for project `foul-ddbb8`.
     */
    suspend fun loginDeveloper(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            val cleanEmail = email.trim()
            val cleanPass = password.trim()
            if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("يرجى إدخال البريد الإلكتروني وكلمة المرور")
                )
            }

            try {
                val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY"
                val payload = JSONObject().apply {
                    put("email", cleanEmail)
                    put("password", cleanPass)
                    put("returnSecureToken", true)
                }
                val req = Request.Builder()
                    .url(url)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .header("Accept", "application/json")
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        val errMsg = try {
                            val errJson = JSONObject(bodyStr).optJSONObject("error")
                            when (errJson?.optString("message", "")) {
                                "INVALID_PASSWORD", "INVALID_LOGIN_CREDENTIALS" -> "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                                "EMAIL_NOT_FOUND" -> "هذا البريد الإلكتروني غير مسجل كمطور"
                                "USER_DISABLED" -> "تم تعطيل هذا الحساب"
                                "TOO_MANY_ATTEMPTS_TRY_LATER" -> "محاولات كثيرة، يرجى المحاولة لاحقاً"
                                else -> "تعذر تسجيل الدخول: تأكد من صحة البريد وكلمة المرور"
                            }
                        } catch (_: Exception) {
                            "تعذر تسجيل الدخول إلى لوحة المطور"
                        }
                        return@withContext Result.failure(IllegalStateException(errMsg))
                    }

                    val json = JSONObject(bodyStr)
                    val idToken = json.optString("idToken", "")
                    val refreshToken = json.optString("refreshToken", "")
                    if (idToken.isBlank()) {
                        return@withContext Result.failure(IllegalStateException("لم يتم استلام رمز المصادقة"))
                    }
                    developerIdToken = idToken
                    developerRefreshToken = refreshToken
                    developerEmail = json.optString("email", cleanEmail)
                    Result.success(idToken)
                }
            } catch (e: Exception) {
                Result.failure(IllegalStateException("خطأ في الاتصال بخادم المصادقة: ${e.localizedMessage}"))
            }
        }

    fun logoutDeveloper() {
        developerIdToken = null
        developerRefreshToken = null
        developerEmail = null
    }

    private fun refreshDeveloperTokenIfNeeded(): String? {
        val refresh = developerRefreshToken ?: return developerIdToken
        return try {
            val url = "https://securetoken.googleapis.com/v1/token?key=$FIREBASE_API_KEY"
            val body = JSONObject().apply {
                put("grant_type", "refresh_token")
                put("refresh_token", refresh)
            }
            val req = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val json = JSONObject(resp.body?.string().orEmpty())
                    val newIdToken = json.optString("id_token", "")
                    if (newIdToken.isNotBlank()) {
                        developerIdToken = newIdToken
                    }
                }
            }
            developerIdToken
        } catch (_: Exception) {
            developerIdToken
        }
    }

    private fun executeAuthenticatedWrite(
        path: String,
        method: String,
        jsonBody: String?
    ): Result<Unit> {
        val token = developerIdToken
            ?: return Result.failure(IllegalStateException("انتهت جلسة المطور، يرجى تسجيل الدخول مجدداً"))

        fun buildRequest(activeToken: String): Request {
            val url = "$FIREBASE_DB_URL/$path.json?auth=$activeToken"
            val builder = Request.Builder().url(url)
            val body = jsonBody?.toRequestBody(jsonMediaType)
            when (method) {
                "PUT" -> builder.put(body ?: "{}".toRequestBody(jsonMediaType))
                "PATCH" -> builder.patch(body ?: "{}".toRequestBody(jsonMediaType))
                "DELETE" -> builder.delete()
                else -> builder.put(body ?: "{}".toRequestBody(jsonMediaType))
            }
            return builder.build()
        }

        httpClient.newCall(buildRequest(token)).execute().use { resp ->
            if (resp.isSuccessful) return Result.success(Unit)
            if (resp.code == 401) {
                val refreshed = refreshDeveloperTokenIfNeeded()
                if (!refreshed.isNullOrBlank()) {
                    httpClient.newCall(buildRequest(refreshed)).execute().use { retryResp ->
                        if (retryResp.isSuccessful) return Result.success(Unit)
                    }
                }
            }
            return Result.failure(IllegalStateException("فشل حفظ البيانات في قاعدة البيانات (${resp.code})"))
        }
    }

    // --- Developer Panel CRUD Operations ---

    suspend fun saveBranch(branch: ChannelCategory): Result<Unit> = withContext(Dispatchers.IO) {
        val obj = JSONObject().apply {
            put("id", branch.id)
            put("name", branch.name.trim())
            put("imageUrl", branch.imageUrl.trim())
            put("parentId", branch.parentId.trim())
            put("order", branch.order)
            put("createdAt", System.currentTimeMillis())
        }
        val res = executeAuthenticatedWrite(
            path = "v4_channel_categories/${branch.id}",
            method = "PUT",
            jsonBody = obj.toString()
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun saveBranchesOrder(orderedBranches: List<ChannelCategory>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val patchObj = JSONObject()
            orderedBranches.forEachIndexed { index, branch ->
                val itemObj = JSONObject().apply {
                    put("id", branch.id)
                    put("name", branch.name.trim())
                    put("imageUrl", branch.imageUrl.trim())
                    put("parentId", branch.parentId.trim())
                    put("order", index + 1)
                    put("createdAt", System.currentTimeMillis())
                }
                patchObj.put(branch.id, itemObj)
            }
            val res = executeAuthenticatedWrite(
                path = "v4_channel_categories",
                method = "PATCH",
                jsonBody = patchObj.toString()
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    suspend fun deleteBranch(branchId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val res = executeAuthenticatedWrite(
            path = "v4_channel_categories/$branchId",
            method = "DELETE",
            jsonBody = null
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun saveChannel(channel: LiveChannel): Result<Unit> = withContext(Dispatchers.IO) {
        val serversArr = JSONArray()
        channel.servers.forEach { srv ->
            val cleanUrl = srv.streamUrl.trim()
            val detectedType = when {
                srv.streamType.equals("ts", ignoreCase = true) || cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                else -> "m3u8"
            }
            serversArr.put(
                JSONObject().apply {
                    put("id", srv.id)
                    put("name", srv.name.trim().ifBlank { "HD" })
                    put("streamType", detectedType)
                    put("streamUrl", cleanUrl)
                }
            )
        }

        val obj = JSONObject().apply {
            put("id", channel.id)
            put("name", channel.name.trim())
            put("category", channel.category.trim())
            put("branchId", channel.branchId.trim())
            put("imageUrl", channel.imageUrl.trim())
            put("servers", serversArr)
            put("appVersion", AppVersionConfig.APP_CURRENT_VERSION)
            put("createdAt", System.currentTimeMillis())
            put("views", channel.views)
            put("order", channel.order)
            put("strictAdBlock", true)
        }

        val res = executeAuthenticatedWrite(
            path = "v4_channels/${channel.id}",
            method = "PUT",
            jsonBody = obj.toString()
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    /**
     * Publishes an Account Branch ("نشر فرع حساب") together with its selected and ordered
     * live channels in a fast atomic batch write to `v4_channel_categories` and `v4_channels`.
     */
    suspend fun saveAccountBranchWithChannels(
        branch: ChannelCategory,
        selectedChannels: List<LiveChannel>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val branchRes = saveBranch(branch)
        if (branchRes.isFailure) return@withContext branchRes

        if (selectedChannels.isNotEmpty()) {
            val channelsPatch = JSONObject()
            val baseTimestamp = System.currentTimeMillis()
            selectedChannels.forEachIndexed { idx, channel ->
                val serversArr = JSONArray()
                channel.servers.forEach { srv ->
                    val cleanUrl = srv.streamUrl.trim()
                    if (cleanUrl.isNotEmpty()) {
                        val detectedType = when {
                            srv.streamType.equals("ts", ignoreCase = true) || cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                            else -> "m3u8"
                        }
                        serversArr.put(
                            JSONObject().apply {
                                put("id", srv.id)
                                put("name", srv.name.trim().ifBlank { "HD" })
                                put("streamType", detectedType)
                                put("streamUrl", cleanUrl)
                            }
                        )
                    }
                }
                val chObj = JSONObject().apply {
                    put("id", channel.id)
                    put("name", channel.name.trim())
                    put("category", branch.name.trim())
                    put("branchId", branch.id.trim())
                    put("imageUrl", channel.imageUrl.trim())
                    put("servers", serversArr)
                    put("appVersion", AppVersionConfig.APP_CURRENT_VERSION)
                    put("createdAt", baseTimestamp + idx)
                    put("views", channel.views)
                    put("order", idx + 1)
                    put("strictAdBlock", true)
                }
                channelsPatch.put(channel.id, chObj)
            }
            val chRes = executeAuthenticatedWrite(
                path = "v4_channels",
                method = "PATCH",
                jsonBody = channelsPatch.toString()
            )
            if (chRes.isFailure) return@withContext chRes
        }

        invalidateCache()
        Result.success(Unit)
    }

    suspend fun deleteChannel(channelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val res = executeAuthenticatedWrite(
            path = "v4_channels/$channelId",
            method = "DELETE",
            jsonBody = null
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun saveXtreamAccount(account: XtreamAccount): Result<Unit> = withContext(Dispatchers.IO) {
        val obj = JSONObject().apply {
            put("id", account.id)
            put("name", account.name.trim().ifBlank { "Xtream" })
            put("host", account.cleanHost)
            put("username", account.username.trim())
            put("password", account.password.trim())
            put("streamExtension", account.streamExtension.ifBlank { "m3u8" })
            put("createdAt", System.currentTimeMillis())
        }
        val res = executeAuthenticatedWrite(
            path = "v4_xtreme_accounts/${account.id}",
            method = "PUT",
            jsonBody = obj.toString()
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun deleteXtreamAccount(accountId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val res1 = executeAuthenticatedWrite(
            path = "v4_xtreme_accounts/$accountId",
            method = "DELETE",
            jsonBody = null
        )
        // Also try removing from legacy xtream_accounts node if it existed there
        executeAuthenticatedWrite(
            path = "xtream_accounts/$accountId",
            method = "DELETE",
            jsonBody = null
        )
        if (res1.isSuccess) invalidateCache()
        res1
    }

    suspend fun saveIptvAccount(account: XtreamAccount): Result<Unit> = withContext(Dispatchers.IO) {
        val obj = JSONObject().apply {
            put("id", account.id)
            put("name", account.name.trim().ifBlank { "حساب IPTV" })
            put("host", account.cleanHost)
            put("username", account.username.trim())
            put("password", account.password.trim())
            put("streamExtension", account.streamExtension.ifBlank { "m3u8" })
            put("createdAt", System.currentTimeMillis())
        }
        val res = executeAuthenticatedWrite(
            path = "v4_iptv_accounts/${account.id}",
            method = "PUT",
            jsonBody = obj.toString()
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun deleteIptvAccount(accountId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val res = executeAuthenticatedWrite(
            path = "v4_iptv_accounts/$accountId",
            method = "DELETE",
            jsonBody = null
        )
        if (res.isSuccess) invalidateCache()
        res
    }

    suspend fun saveVersionConfig(config: AppVersionConfig): Result<Unit> =
        withContext(Dispatchers.IO) {
            val cleanVer = config.requiredVersion.trim().ifBlank { AppVersionConfig.APP_CURRENT_VERSION }
            val patchObj = JSONObject().apply {
                put("required_version", cleanVer)
                put("latest_version", cleanVer)
                put("update_text", config.updateText.trim())
                put("update_url", config.updateUrl.trim())
                put("telegram_url", config.telegramUrl.trim().ifBlank { DEFAULT_TELEGRAM_URL })
                put("lock_old_versions", config.lockOldVersions)
                put("update_type", "forced")
            }
            val res = executeAuthenticatedWrite(
                path = "v4_app_config",
                method = "PATCH",
                jsonBody = patchObj.toString()
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    /**
     * Saves the global online App Control settings (`showChannelViewsAndLikes` & `showAdsInApp`)
     * directly to the primary Firebase Realtime Database (`foul-ddbb8`) using the authenticated developer token
     * so all users receive the exact online state in real time.
     */
    suspend fun saveAppControlConfig(
        showChannelViewsAndLikes: Boolean,
        showAdsInApp: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val controlObj = JSONObject().apply {
            put("showChannelViewsAndLikes", showChannelViewsAndLikes)
            put("show_channel_views_likes", showChannelViewsAndLikes)
            put("showAdsInApp", showAdsInApp)
            put("show_ads_in_app", showAdsInApp)
            put("updatedAt", updatedAt)
        }
        val resAppControl = executeAuthenticatedWrite(
            path = "app_control",
            method = "PUT",
            jsonBody = controlObj.toString()
        )
        val resV4Config = executeAuthenticatedWrite(
            path = "v4_app_config",
            method = "PATCH",
            jsonBody = controlObj.toString()
        )
        if (resAppControl.isSuccess || resV4Config.isSuccess) {
            invalidateCache()
            Result.success(Unit)
        } else {
            resAppControl
        }
    }

    private fun serializeChannelToJson(channel: LiveChannel): JSONObject {
        val serversArr = JSONArray()
        channel.servers.forEach { srv ->
            val cleanUrl = srv.streamUrl.trim()
            if (cleanUrl.isNotEmpty()) {
                val detectedType = when {
                    srv.streamType.equals("ts", ignoreCase = true) || cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                    srv.streamType.equals("mp4", ignoreCase = true) || cleanUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                    srv.streamType.equals("mkv", ignoreCase = true) || cleanUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                    else -> "m3u8"
                }
                serversArr.put(
                    JSONObject().apply {
                        put("id", srv.id.ifBlank { "s_${serversArr.length() + 1}" })
                        put("name", srv.name.trim().ifBlank { "HD" })
                        put("streamType", detectedType)
                        put("streamUrl", cleanUrl)
                    }
                )
            }
        }
        return JSONObject().apply {
            put("id", channel.id)
            put("name", channel.name.trim())
            put("category", channel.category.trim())
            put("branchId", channel.branchId.trim())
            put("imageUrl", channel.imageUrl.trim())
            put("servers", serversArr)
            put("views", channel.views)
        }
    }

    private fun parseChannelFromJson(item: JSONObject, fallbackId: String): LiveChannel? {
        val id = item.optString("id", fallbackId).ifBlank { fallbackId }
        val name = item.optString("name", "").trim()
        val category = item.optString("category", "").trim()
        val branchId = item.optString("branchId", "").trim()
        val imageUrl = item.optString("imageUrl", "").trim()
        val views = item.optInt("views", 0)

        val servers = mutableListOf<ChannelServer>()
        val serversArr = item.optJSONArray("servers")
        if (serversArr != null) {
            for (i in 0 until serversArr.length()) {
                val sObj = serversArr.optJSONObject(i) ?: continue
                val sUrl = sObj.optString("streamUrl", "").trim()
                if (sUrl.isNotEmpty()) {
                    val rawType = sObj.optString("streamType", "").trim()
                    val resolvedType = when {
                        rawType.equals("ts", ignoreCase = true) || sUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                        rawType.equals("mp4", ignoreCase = true) || sUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                        rawType.equals("mkv", ignoreCase = true) || sUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                        else -> "m3u8"
                    }
                    servers.add(
                        ChannelServer(
                            id = sObj.optString("id", "$i"),
                            name = sObj.optString("name", "Server ${i + 1}").ifBlank { "HD" },
                            streamType = resolvedType,
                            streamUrl = sUrl
                        )
                    )
                }
            }
        }
        if (servers.isEmpty()) {
            val directUrl = item.optString("streamUrl", item.optString("url", "")).trim()
            if (directUrl.isNotEmpty()) {
                servers.add(
                    ChannelServer(
                        id = "default",
                        name = "بث مباشر",
                        streamType = when {
                            directUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                            directUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                            directUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                            else -> "m3u8"
                        },
                        streamUrl = directUrl
                    )
                )
            }
        }
        if (name.isEmpty() && servers.isEmpty()) return null
        return LiveChannel(
            id = id,
            name = name.ifBlank { "قناة مباشرة" },
            category = category,
            branchId = branchId,
            imageUrl = imageUrl,
            servers = servers,
            views = views
        )
    }

    /**
     * Saves/Publishes channel(s) linked to a Match in the Matches Schedule (`v4_match_channels`)
     * on the primary Firebase database (`foul-ddbb8`).
     */
    suspend fun saveMatchPublishedChannel(item: MatchPublishedChannel): Result<Unit> =
        withContext(Dispatchers.IO) {
            val channelIdsArr = JSONArray()
            item.channelIds.forEach { cid ->
                if (cid.isNotBlank()) channelIdsArr.put(cid.trim())
            }
            val customChannelsArr = JSONArray()
            item.customChannels.forEach { ch ->
                customChannelsArr.put(serializeChannelToJson(ch))
            }
            val obj = JSONObject().apply {
                put("id", item.id)
                put("matchId", item.matchId)
                put("homeTeamName", item.homeTeamName.trim())
                put("awayTeamName", item.awayTeamName.trim())
                put("competitionName", item.competitionName.trim())
                put("matchDate", item.matchDate.trim())
                put("channelIds", channelIdsArr)
                put("customChannels", customChannelsArr)
                put("createdAt", item.createdAt)
            }
            val res = executeAuthenticatedWrite(
                path = "v4_match_channels/${item.id}",
                method = "PUT",
                jsonBody = obj.toString()
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    suspend fun deleteMatchPublishedChannel(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val res = executeAuthenticatedWrite(
                path = "v4_match_channels/$id",
                method = "DELETE",
                jsonBody = null
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    /**
     * Publishes an Event or Video Announcement (`v4_published_events`) on the primary Firebase database (`foul-ddbb8`).
     * Supports:
     * - Single or multiple channels (`eventType == "CHANNELS"`)
     * - Direct MP4 / MKV / Video playback (`eventType == "VIDEO"`)
     * - Optional title/description text and optional image URL.
     */
    suspend fun savePublishedEvent(event: PublishedEventItem): Result<Unit> =
        withContext(Dispatchers.IO) {
            val channelIdsArr = JSONArray()
            event.channelIds.forEach { cid ->
                if (cid.isNotBlank()) channelIdsArr.put(cid.trim())
            }
            val customChannelsArr = JSONArray()
            event.customChannels.forEach { ch ->
                customChannelsArr.put(serializeChannelToJson(ch))
            }
            val obj = JSONObject().apply {
                put("id", event.id)
                put("eventType", event.eventType)
                put("title", event.title.trim())
                put("description", event.description.trim())
                put("imageUrl", event.imageUrl.trim())
                put("showWatchNow", event.showWatchNow)
                put("channelIds", channelIdsArr)
                put("customChannels", customChannelsArr)
                put("videoUrl", event.videoUrl.trim())
                put("createdAt", event.createdAt)
            }
            val res = executeAuthenticatedWrite(
                path = "v4_published_events/${event.id}",
                method = "PUT",
                jsonBody = obj.toString()
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    suspend fun deletePublishedEvent(eventId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val res = executeAuthenticatedWrite(
                path = "v4_published_events/$eventId",
                method = "DELETE",
                jsonBody = null
            )
            if (res.isSuccess) invalidateCache()
            res
        }

    private fun invalidateCache() {
        cachedPayload = null
        lastFetchTimeMs = 0L
    }

    // --- Reading Firebase Data ---

    suspend fun getFirebaseData(forceRefresh: Boolean = false): Result<FirebaseHomePayload> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val now = System.currentTimeMillis()
                val current = cachedPayload
                if (!forceRefresh && current != null && (now - lastFetchTimeMs) < cacheTtlMs) {
                    return@withContext Result.success(current)
                }

                try {
                    val request = Request.Builder()
                        .url("$FIREBASE_DB_URL/.json")
                        .header("Accept", "application/json")
                        .header("User-Agent", "FoulTV-Android/1.0")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            if (current != null) return@withContext Result.success(current)
                            return@withContext Result.failure(
                                IllegalStateException("خطأ في الاتصال بقاعدة البيانات: ${response.code}")
                            )
                        }
                        val bodyStr = response.body?.string().orEmpty()
                        val root = if (bodyStr.isNotBlank() && bodyStr != "null") {
                            JSONObject(bodyStr)
                        } else {
                            JSONObject()
                        }

                        val payload = parseFirebaseRoot(root)
                        cachedPayload = payload
                        lastFetchTimeMs = now
                        Result.success(payload)
                    }
                } catch (e: Exception) {
                    if (current != null) {
                        Result.success(current)
                    } else {
                        Result.failure(e)
                    }
                }
            }
        }

    private fun parseFirebaseRoot(root: JSONObject): FirebaseHomePayload {
        // 1. Parse Branches / Categories (v4_channel_categories)
        val categories = mutableListOf<ChannelCategory>()
        val catObj = root.optJSONObject("v4_channel_categories")
        if (catObj != null) {
            val keys = catObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = catObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                val name = item.optString("name", "").trim()
                val imageUrl = item.optString("imageUrl", item.optString("image", "")).trim()
                val parentId = item.optString("parentId", "").trim()
                val order = item.optInt("order", 999)
                if (name.isNotEmpty()) {
                    categories.add(
                        ChannelCategory(
                            id = id,
                            name = name,
                            imageUrl = imageUrl,
                            parentId = parentId,
                            order = order
                        )
                    )
                }
            }
        }
        categories.sortBy { it.order }

        // 2. Parse Channels (v4_channels)
        val channels = mutableListOf<LiveChannel>()
        val chanObj = root.optJSONObject("v4_channels")
        if (chanObj != null) {
            val keys = chanObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = chanObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                val name = item.optString("name", "").trim()
                val category = item.optString("category", "").trim()
                val branchId = item.optString("branchId", "").trim()
                val imageUrl = item.optString("imageUrl", "").trim()
                val views = item.optInt("views", 0)
                val order = item.optInt("order", 9999)

                val servers = mutableListOf<ChannelServer>()
                val serversArr = item.optJSONArray("servers")
                if (serversArr != null) {
                    for (i in 0 until serversArr.length()) {
                        val sObj = serversArr.optJSONObject(i) ?: continue
                        val sUrl = sObj.optString("streamUrl", "").trim()
                        if (sUrl.isNotEmpty()) {
                            val rawType = sObj.optString("streamType", "").trim()
                            val resolvedType = when {
                                rawType.equals("ts", ignoreCase = true) || sUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                                else -> "m3u8"
                            }
                            servers.add(
                                ChannelServer(
                                    id = sObj.optString("id", "$i"),
                                    name = sObj.optString("name", "Server ${i + 1}").ifBlank { "HD" },
                                    streamType = resolvedType,
                                    streamUrl = sUrl
                                )
                            )
                        }
                    }
                }
                if (servers.isEmpty()) {
                    val directUrl = item.optString("streamUrl", item.optString("url", "")).trim()
                    if (directUrl.isNotEmpty()) {
                        servers.add(
                            ChannelServer(
                                id = "default",
                                name = "بث مباشر",
                                streamType = if (directUrl.endsWith(".ts", ignoreCase = true)) "ts" else "m3u8",
                                streamUrl = directUrl
                            )
                        )
                    }
                }

                if (name.isNotEmpty()) {
                    channels.add(
                        LiveChannel(
                            id = id,
                            name = name,
                            category = category,
                            branchId = branchId,
                            imageUrl = imageUrl,
                            servers = servers,
                            views = views,
                            order = order
                        )
                    )
                }
            }
        }
        channels.sortBy { it.order }

        // Ensure any category referenced by a channel exists as a branch
        val existingCatNames = categories.map { it.name.lowercase() }.toMutableSet()
        val existingCatIds = categories.map { it.id }.toMutableSet()
        for (ch in channels) {
            if (ch.branchId.isEmpty() && ch.category.isNotEmpty() && !existingCatNames.contains(ch.category.lowercase())) {
                existingCatNames.add(ch.category.lowercase())
                categories.add(
                    ChannelCategory(
                        id = ch.category,
                        name = ch.category,
                        imageUrl = ch.imageUrl,
                        parentId = "",
                        order = categories.size + 10
                    )
                )
            } else if (ch.branchId.isNotEmpty() && ch.branchId !in existingCatIds && ch.category.isNotEmpty() && !existingCatNames.contains(ch.category.lowercase())) {
                existingCatIds.add(ch.branchId)
                existingCatNames.add(ch.category.lowercase())
                categories.add(
                    ChannelCategory(
                        id = ch.branchId,
                        name = ch.category,
                        imageUrl = ch.imageUrl,
                        parentId = "",
                        order = categories.size + 10
                    )
                )
            }
        }
        categories.sortBy { it.order }

        // 3. Parse Event Banners (v4_event_banners)
        val banners = mutableListOf<EventBanner>()
        val bannersObj = root.optJSONObject("v4_event_banners")
        if (bannersObj != null) {
            val keys = bannersObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = bannersObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                val title = item.optString("title", "").trim()
                val imageUrl = item.optString("imageUrl", "").trim()
                val channelId = item.optString("channelId", "")
                val channelName = item.optString("channelName", "")
                val showWatchNow = item.optBoolean("showWatchNow", true)
                val watchNowUrl = item.optString("watchNowUrl", "")

                val channelIds = mutableListOf<String>()
                val cIdsArr = item.optJSONArray("channelIds")
                if (cIdsArr != null) {
                    for (i in 0 until cIdsArr.length()) {
                        val cid = cIdsArr.optString(i, "")
                        if (cid.isNotEmpty()) channelIds.add(cid)
                    }
                }
                val channelNames = mutableListOf<String>()
                val cNamesArr = item.optJSONArray("channelNames")
                if (cNamesArr != null) {
                    for (i in 0 until cNamesArr.length()) {
                        val cname = cNamesArr.optString(i, "")
                        if (cname.isNotEmpty()) channelNames.add(cname)
                    }
                }

                if (title.isNotEmpty() || imageUrl.isNotEmpty()) {
                    banners.add(
                        EventBanner(
                            id = id,
                            title = title,
                            imageUrl = imageUrl,
                            channelId = channelId,
                            channelName = channelName,
                            channelIds = channelIds,
                            channelNames = channelNames,
                            showWatchNow = showWatchNow,
                            watchNowUrl = watchNowUrl
                        )
                    )
                }
            }
        }

        // 4. Parse Live Matches / Events
        val liveMatches = mutableListOf<LiveEventMatch>()
        val seenMatchIds = mutableSetOf<String>()
        for (nodeName in listOf("v4_live_matches", "v4_events")) {
            val matchesObj = root.optJSONObject(nodeName) ?: continue
            val keys = matchesObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = matchesObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                if (!seenMatchIds.add(id)) continue
                liveMatches.add(
                    LiveEventMatch(
                        id = id,
                        name = item.optString("name", ""),
                        homeTeam = item.optString("homeTeam", ""),
                        awayTeam = item.optString("awayTeam", ""),
                        homeLogo = item.optString("homeLogo", ""),
                        imageUrl = item.optString("imageUrl", ""),
                        broadcaster = item.optString("broadcaster", ""),
                        leagueName = item.optString("leagueName", item.optString("tournament", "")),
                        isLive = item.optBoolean("isLive", false),
                        targetChannelId = item.optString("targetChannelId", "")
                    )
                )
            }
        }

        // 5. Parse Xtream Accounts (v4_xtreme_accounts & xtream_accounts)
        val xtreamAccounts = mutableListOf<XtreamAccount>()
        val v4XtreamObj = root.optJSONObject("v4_xtreme_accounts")
        if (v4XtreamObj != null) {
            val keys = v4XtreamObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = v4XtreamObj.optJSONObject(key) ?: continue
                val host = item.optString("host", item.optString("url", "")).trim()
                val username = item.optString("username", "").trim()
                val password = item.optString("password", "").trim()
                if (host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    xtreamAccounts.add(
                        XtreamAccount(
                            id = item.optString("id", key),
                            name = item.optString("name", "Xtream"),
                            host = host,
                            username = username,
                            password = password,
                            streamExtension = item.optString("streamExtension", "mp4")
                        )
                    )
                }
            }
        }
        val legacyXtreamObj = root.optJSONObject("xtream_accounts")
        if (legacyXtreamObj != null) {
            val keys = legacyXtreamObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = legacyXtreamObj.optJSONObject(key) ?: continue
                val host = item.optString("url", item.optString("host", "")).trim()
                val username = item.optString("username", "").trim()
                val password = item.optString("password", "").trim()
                if (host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    xtreamAccounts.add(
                        XtreamAccount(
                            id = key,
                            name = item.optString("name", "Xtream"),
                            host = host,
                            username = username,
                            password = password,
                            streamExtension = "mp4"
                        )
                    )
                }
            }
        }

        // 5.B Parse IPTV Accounts (v4_iptv_accounts)
        val iptvAccounts = mutableListOf<XtreamAccount>()
        val v4IptvObj = root.optJSONObject("v4_iptv_accounts")
        if (v4IptvObj != null) {
            val keys = v4IptvObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = v4IptvObj.optJSONObject(key) ?: continue
                val host = item.optString("host", item.optString("url", "")).trim()
                val username = item.optString("username", "").trim()
                val password = item.optString("password", "").trim()
                if (host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    iptvAccounts.add(
                        XtreamAccount(
                            id = item.optString("id", key),
                            name = item.optString("name", "حساب IPTV"),
                            host = host,
                            username = username,
                            password = password,
                            streamExtension = item.optString("streamExtension", "m3u8")
                        )
                    )
                }
            }
        }

        // 6. Parse App Config & Forced Update Version (v4_app_config)
        val configObj = root.optJSONObject("v4_app_config")
        val telegramUrl = configObj?.optString("telegram_url", DEFAULT_TELEGRAM_URL)
            ?.ifBlank { DEFAULT_TELEGRAM_URL } ?: DEFAULT_TELEGRAM_URL

        // Use required_version if set by Developer Panel; otherwise default to 1.0 (this app's version)
        // unless latest_version was explicitly set to something other than the legacy 4.0.0 placeholder
        val rawRequiredVer = configObj?.optString("required_version", "")?.trim().orEmpty()
        val rawLatestVer = configObj?.optString("latest_version", "")?.trim().orEmpty()
        val effectiveRequiredVersion = when {
            rawRequiredVer.isNotEmpty() -> rawRequiredVer
            rawLatestVer.isNotEmpty() && rawLatestVer != "4.0.0" -> rawLatestVer
            else -> AppVersionConfig.APP_CURRENT_VERSION
        }

        val updateText = configObj?.optString(
            "update_text",
            "يتوفر إصدار جديد من تطبيق Foul TV، يرجى التحديث الآن للاستمرار."
        )?.ifBlank { "يتوفر إصدار جديد من تطبيق Foul TV، يرجى التحديث الآن للاستمرار." }
            ?: "يتوفر إصدار جديد من تطبيق Foul TV، يرجى التحديث الآن للاستمرار."

        val updateUrl = configObj?.optString("update_url", "")?.trim().orEmpty()
        val lockOld = configObj?.optBoolean("lock_old_versions", true) ?: true

        val versionConfig = AppVersionConfig(
            requiredVersion = effectiveRequiredVersion,
            updateText = updateText,
            updateUrl = updateUrl,
            telegramUrl = telegramUrl,
            lockOldVersions = lockOld
        )

        // 7. Parse Published Match Channels (v4_match_channels)
        val matchPublishedChannels = mutableListOf<MatchPublishedChannel>()
        val matchChObj = root.optJSONObject("v4_match_channels")
        if (matchChObj != null) {
            val keys = matchChObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = matchChObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                val matchId = item.optInt("matchId", 0)
                val homeTeamName = item.optString("homeTeamName", "").trim()
                val awayTeamName = item.optString("awayTeamName", "").trim()
                val competitionName = item.optString("competitionName", "").trim()
                val matchDate = item.optString("matchDate", "").trim()
                val createdAt = item.optLong("createdAt", 0L)

                val cIds = mutableListOf<String>()
                val cIdsArr = item.optJSONArray("channelIds")
                if (cIdsArr != null) {
                    for (i in 0 until cIdsArr.length()) {
                        val cid = cIdsArr.optString(i, "").trim()
                        if (cid.isNotEmpty()) cIds.add(cid)
                    }
                }

                val customChs = mutableListOf<LiveChannel>()
                val customArr = item.optJSONArray("customChannels")
                if (customArr != null) {
                    for (i in 0 until customArr.length()) {
                        val chObj = customArr.optJSONObject(i) ?: continue
                        parseChannelFromJson(chObj, "${id}_ch_$i")?.let { customChs.add(it) }
                    }
                }

                if (homeTeamName.isNotEmpty() || awayTeamName.isNotEmpty() || matchId > 0) {
                    matchPublishedChannels.add(
                        MatchPublishedChannel(
                            id = id,
                            matchId = matchId,
                            homeTeamName = homeTeamName,
                            awayTeamName = awayTeamName,
                            competitionName = competitionName,
                            matchDate = matchDate,
                            channelIds = cIds,
                            customChannels = customChs,
                            createdAt = createdAt
                        )
                    )
                }
            }
        }
        matchPublishedChannels.sortByDescending { it.createdAt }

        // 8. Parse Published Events / Video Announcements (v4_published_events)
        val publishedEvents = mutableListOf<PublishedEventItem>()
        val pubEventsObj = root.optJSONObject("v4_published_events")
        if (pubEventsObj != null) {
            val keys = pubEventsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = pubEventsObj.optJSONObject(key) ?: continue
                val id = item.optString("id", key)
                val eventType = item.optString("eventType", "CHANNELS").ifBlank { "CHANNELS" }
                val title = item.optString("title", "").trim()
                val description = item.optString("description", "").trim()
                val imageUrl = item.optString("imageUrl", "").trim()
                val showWatchNow = item.optBoolean("showWatchNow", true)
                val videoUrl = item.optString("videoUrl", "").trim()
                val createdAt = item.optLong("createdAt", 0L)

                val cIds = mutableListOf<String>()
                val cIdsArr = item.optJSONArray("channelIds")
                if (cIdsArr != null) {
                    for (i in 0 until cIdsArr.length()) {
                        val cid = cIdsArr.optString(i, "").trim()
                        if (cid.isNotEmpty()) cIds.add(cid)
                    }
                }

                val customChs = mutableListOf<LiveChannel>()
                val customArr = item.optJSONArray("customChannels")
                if (customArr != null) {
                    for (i in 0 until customArr.length()) {
                        val chObj = customArr.optJSONObject(i) ?: continue
                        parseChannelFromJson(chObj, "${id}_evch_$i")?.let { customChs.add(it) }
                    }
                }

                publishedEvents.add(
                    PublishedEventItem(
                        id = id,
                        eventType = eventType,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        showWatchNow = showWatchNow,
                        channelIds = cIds,
                        customChannels = customChs,
                        videoUrl = videoUrl,
                        createdAt = createdAt
                    )
                )
            }
        }
        publishedEvents.sortByDescending { it.createdAt }

        // 9. Parse Online App Control Settings (showChannelViewsAndLikes & showAdsInApp) from `app_control` and `v4_app_config`
        var parsedShowViews: Boolean? = null
        var parsedShowAds: Boolean? = null
        var parsedControlUpdatedAt = 0L

        fun inspectControlNode(node: JSONObject?) {
            if (node == null) return
            val ts = node.optLong("updatedAt", node.optLong("app_control_updated_at", 0L))
            val v = when {
                node.has("showChannelViewsAndLikes") -> node.optBoolean("showChannelViewsAndLikes", true)
                node.has("show_channel_views_likes") -> node.optBoolean("show_channel_views_likes", true)
                else -> null
            }
            val a = when {
                node.has("showAdsInApp") -> node.optBoolean("showAdsInApp", true)
                node.has("show_ads_in_app") -> node.optBoolean("show_ads_in_app", true)
                else -> null
            }
            if (v != null || a != null) {
                if (ts >= parsedControlUpdatedAt) {
                    if (ts > parsedControlUpdatedAt) {
                        parsedControlUpdatedAt = ts
                        if (v != null) parsedShowViews = v
                        if (a != null) parsedShowAds = a
                    } else {
                        if (parsedShowViews == null && v != null) parsedShowViews = v
                        if (parsedShowAds == null && a != null) parsedShowAds = a
                    }
                }
            }
        }

        inspectControlNode(root.optJSONObject("app_control"))
        inspectControlNode(configObj)

        return FirebaseHomePayload(
            categories = categories,
            channels = channels,
            banners = banners,
            liveMatches = liveMatches,
            xtreamAccounts = xtreamAccounts,
            iptvAccounts = iptvAccounts,
            telegramUrl = telegramUrl,
            versionConfig = versionConfig,
            matchPublishedChannels = matchPublishedChannels,
            publishedEvents = publishedEvents,
            showChannelViewsAndLikes = parsedShowViews,
            showAdsInApp = parsedShowAds,
            appControlUpdatedAt = parsedControlUpdatedAt
        )
    }

    companion object {
        const val FIREBASE_DB_URL = "https://foul-ddbb8-default-rtdb.firebaseio.com"
        const val FIREBASE_API_KEY = "AIzaSyBKd4aGpfLcYolAZqO2cPWxseAbplJCwTg"
        const val DEFAULT_TELEGRAM_URL = "https://t.me/jdj_q"

        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
