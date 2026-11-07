package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import com.example.data.model.LiveChannel
import com.example.data.model.XtreamAccount
import com.example.data.model.XtreamAccountLiveCatalog
import com.example.data.model.XtreamLiveStreamItem
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.theme.FoulBlackBg
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
import java.util.Locale

/**
 * Detected Quality Tier for Smart Multi-Quality Channel Grouping into Ordered Servers
 * (e.g. beIN Sports 1 [4K, FHD, HD, SD] -> 1 unified channel "beIN Sports 1" with ordered servers).
 */
private data class DetectedStreamQuality(
    val canonicalChannelKey: String,
    val cleanChannelTitle: String,
    val qualityServerLabel: String,
    val qualityRankHighToLow: Int
)

private object SmartChannelQualityMerger {

    /**
     * Parses raw IPTV channel name & category name (e.g., "AR | beIN Sports 1 FHD", "beIN Sports 1 HD", "beIN Sports 1 SD")
     * to extract:
     * 1. Clean canonical channel title without quality tags (e.g. "beIN Sports 1")
     * 2. Normalized grouping key (e.g. "bein sports 1")
     * 3. Concise quality server name (e.g. "4K", "FHD", "HD", "HEVC", "SD", "LOW")
     * 4. Sorting rank so servers are ordered neatly (e.g. FHD -> HD -> SD or SD -> HD -> FHD)
     */
    fun analyzeStream(rawStreamName: String, rawCategoryName: String): DetectedStreamQuality {
        val combinedText = "$rawStreamName $rawCategoryName"

        // Detect quality label & rank (check FHD / FULL HD / 4K / UHD before HD!)
        val (qualityLabel, qualityRank) = when {
            Regex("\\b(4K|UHD|2160P|ULTRA\\s*HD)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "4K" to 1
            Regex("\\b(FHD|FULL\\s*HD|FULLHD|1080P|1080I)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "FHD" to 2
            Regex("\\b(HEVC|H265|H\\.265|VIP)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "HEVC" to 3
            Regex("\\b(HD|720P)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "HD" to 4
            Regex("\\b(SD|576P|480P)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "SD" to 5
            Regex("\\b(LOW|LQ|360P|240P|AUTO|MOBILE)\\b", RegexOption.IGNORE_CASE).containsMatchIn(combinedText) ->
                "LOW" to 6
            else -> "HD" to 4
        }

        // Clean channel name by stripping country prefixes, brackets with quality tags, and standalone quality words
        var workingTitle = rawStreamName.trim()

        // Strip common IPTV country/package prefixes like "AR:", "AR |", "[AR]", "|AR|", "VIP :", "•••"
        workingTitle = workingTitle
            .replace(Regex("^\\s*(\\[[A-Za-z0-9_\\-\\s]{1,8}\\]|\\|[A-Za-z0-9_\\-\\s]{1,8}\\||[A-Za-z]{2,4}\\s*[:|\\-]\\s*)"), "")
            .replace(Regex("[★☆•●◆▪]+"), " ")

        // Strip quality tokens inside brackets/parentheses like [FHD], (HD), [4K], (HEVC), [SD]
        workingTitle = workingTitle.replace(
            Regex("[\\[\\(]\\s*(4K|UHD|2160P|FHD|FULL\\s*HD|FULLHD|1080P|1080I|HEVC|H265|H\\.265|VIP|HD|720P|SD|576P|480P|LOW|LQ|360P|240P|AUTO|BACKUP|BK)\\s*[\\]\\)]", RegexOption.IGNORE_CASE),
            " "
        )

        // Strip standalone quality tokens at start, middle, or end of name
        workingTitle = workingTitle.replace(
            Regex("(?<=^|[\\s|\\-_:\\[\\]()])(4K|UHD|2160P|ULTRA\\s*HD|FHD|FULL\\s*HD|FULLHD|1080P|1080I|HEVC|H265|H\\.265|HD|720P|SD|576P|480P|LOW|LQ|360P|240P)(?=$|[\\s|\\-_:\\[\\]()])", RegexOption.IGNORE_CASE),
            " "
        )

        // Clean leftover empty brackets, leading/trailing separators, and duplicate whitespace
        workingTitle = workingTitle
            .replace(Regex("\\[\\s*\\]|\\(\\s*\\)"), " ")
            .replace(Regex("^[\\s|\\-_:•/]+|[\\s|\\-_:•/]+$"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val finalCleanTitle = workingTitle.ifBlank { rawStreamName.trim() }

        // Normalize canonical grouping key (case-insensitive, unify "bein sport" / "bein sports" spacing)
        val canonicalKey = finalCleanTitle
            .lowercase(Locale.ROOT)
            .replace(Regex("[|\\-_:•\\[\\]()]"), " ")
            .replace(Regex("\\bbein\\s+sport\\b"), "bein sports")
            .replace(Regex("\\s+"), " ")
            .trim()

        return DetectedStreamQuality(
            canonicalChannelKey = canonicalKey.ifBlank { rawStreamName.lowercase(Locale.ROOT).trim() },
            cleanChannelTitle = finalCleanTitle,
            qualityServerLabel = qualityLabel,
            qualityRankHighToLow = qualityRank
        )
    }

    /**
     * Groups a list of raw [XtreamLiveStreamItem]s across one or multiple combined categories
     * into unified [XtreamLiveStreamItem]s where matching base channels (e.g. "beIN Sports 1" across SD, HD, FHD, 4K)
     * become a SINGLE channel with neatly ordered quality servers ([ChannelServer] named "4K", "FHD", "HD", "SD").
     */
    fun mergeStreamsByChannelAndQuality(
        rawStreams: List<XtreamLiveStreamItem>,
        highToLowOrder: Boolean = true
    ): List<XtreamLiveStreamItem> {
        if (rawStreams.isEmpty()) return emptyList()

        // If any item already has mergedServers, preserve/flatten appropriately
        val analyzedList = rawStreams.map { item ->
            item to analyzeStream(item.name, item.categoryName)
        }

        val grouped = analyzedList.groupBy { (_, info) -> info.canonicalChannelKey }

        return grouped.values.map { groupEntries ->
            if (groupEntries.size == 1 && groupEntries.first().first.mergedServers.isNotEmpty()) {
                // Already merged item: just re-sort its servers if needed
                val existingItem = groupEntries.first().first
                existingItem
            } else {
                // Sort the variants by quality rank (4K -> FHD -> HEVC -> HD -> SD -> LOW, or reverse SD -> HD -> FHD -> 4K)
                val sortedVariants = if (highToLowOrder) {
                    groupEntries.sortedBy { it.second.qualityRankHighToLow }
                } else {
                    groupEntries.sortedByDescending { it.second.qualityRankHighToLow }
                }

                val primaryEntry = groupEntries.minByOrNull { it.second.qualityRankHighToLow } ?: sortedVariants.first()
                val primaryStream = primaryEntry.first
                val bestCleanTitle = primaryEntry.second.cleanChannelTitle
                val bestIcon = groupEntries.firstOrNull { it.first.streamIcon.isNotBlank() }?.first?.streamIcon.orEmpty()
                val combinedCategoryNames = groupEntries
                    .map { it.first.categoryName.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" + ")

                val usedServerLabels = mutableMapOf<String, Int>()
                val orderedQualityServers = mutableListOf<ChannelServer>()

                sortedVariants.forEach { (variantStream, qualityInfo) ->
                    if (variantStream.mergedServers.isNotEmpty()) {
                        orderedQualityServers.addAll(variantStream.mergedServers)
                    } else {
                        val baseLabel = qualityInfo.qualityServerLabel
                        val count = (usedServerLabels[baseLabel] ?: 0) + 1
                        usedServerLabels[baseLabel] = count
                        val finalServerName = if (count == 1) baseLabel else "$baseLabel $count"

                        val baseHost = variantStream.cleanHost
                        val cleanUser = variantStream.username.trim()
                        val cleanPass = variantStream.password.trim()

                        orderedQualityServers.add(
                            ChannelServer(
                                id = "xt_q_${variantStream.streamId}_m3u8",
                                name = finalServerName,
                                streamType = "m3u8",
                                streamUrl = "$baseHost/live/$cleanUser/$cleanPass/${variantStream.streamId}.m3u8"
                            )
                        )
                    }
                }

                // If only a single stream existed in this group, also provide a clean secondary server ("سيرفر 2") for instant failover
                if (orderedQualityServers.size == 1 && sortedVariants.size == 1 && primaryStream.mergedServers.isEmpty()) {
                    val baseHost = primaryStream.cleanHost
                    val cleanUser = primaryStream.username.trim()
                    val cleanPass = primaryStream.password.trim()
                    orderedQualityServers.add(
                        ChannelServer(
                            id = "xt_q_${primaryStream.streamId}_backup",
                            name = "سيرفر 2",
                            streamType = "ts",
                            streamUrl = "$baseHost/live/$cleanUser/$cleanPass/${primaryStream.streamId}.ts"
                        )
                    )
                }

                primaryStream.copy(
                    streamId = "merged_${primaryStream.streamId}",
                    name = bestCleanTitle,
                    streamIcon = bestIcon,
                    categoryName = combinedCategoryNames.ifBlank { primaryStream.categoryName },
                    mergedServers = orderedQualityServers
                )
            }
        }
    }
}

/**
 * Developer Tab: "نشر فرع حساب" (Publish Account Branch)
 *
 * Supports:
 * 1. Entering Host, Username, Password (or picking from saved Xtream accounts) and fetching categories & channels.
 * 2. Multi-Category Selection & Combining ("إمكانية جمع تصنيفات"): Select one or multiple categories together (e.g. beIN FHD + beIN HD + beIN SD) and fetch/combine their channels.
 * 3. Smart Multi-Quality Channel Merging into Ordered Servers ("وجعل مثلا التصنيفات تحتوي على قناة مثلا bein sports بجودات مختلفة مثلا SD و HD و FHD... إمكانية جعل فقط قناة bein sports وجودات تكون بشكل سيرفرات مرتبة جدا"):
 *    Unifies channels like `beIN Sports 1 SD`, `beIN Sports 1 HD`, `beIN Sports 1 FHD`, `beIN Sports 1 4K` into a SINGLE channel `beIN Sports 1` with neatly ordered servers (`4K`, `FHD`, `HD`, `SD`).
 * 4. Reordering selected channels & their quality servers before publishing.
 * 5. Publishing the branch with all ordered multi-server channels to Firebase.
 */
@Composable
fun DeveloperPublishAccountBranchTab(
    branches: List<ChannelCategory>,
    channels: List<LiveChannel>,
    xtreamAccounts: List<XtreamAccount>,
    isActionLoading: Boolean,
    isFetchingCatalog: Boolean,
    isFetchingCategoryStreams: Boolean,
    fetchError: String?,
    fetchedCatalog: XtreamAccountLiveCatalog?,
    onFetchAccountCatalog: (host: String, username: String, password: String) -> Unit,
    onFetchCategoryStreams: (host: String, username: String, password: String, categoryId: String, categoryName: String) -> Unit,
    onClearCatalog: () -> Unit,
    onPublishAccountBranch: (
        existingBranchId: String?,
        branchName: String,
        branchImageUrl: String,
        parentBranchId: String,
        orderedSelectedStreams: List<XtreamLiveStreamItem>
    ) -> Unit
) {
    // 1. Account Credentials State
    var hostInput by remember { mutableStateOf(fetchedCatalog?.host.orEmpty()) }
    var usernameInput by remember { mutableStateOf(fetchedCatalog?.username.orEmpty()) }
    var passwordInput by remember { mutableStateOf(fetchedCatalog?.password.orEmpty()) }

    // 2. Multi-Category Combining & Channel Search State ("إمكانية جمع تصنيفات")
    val selectedCategoryIds = remember { mutableStateListOf<String>() }
    var isAllCategoriesSelected by remember { mutableStateOf(true) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var channelSearchQuery by remember { mutableStateOf("") }

    // 3. Smart Quality-to-Servers Merge State ("إمكانية جعل فقط قناة bein sports وجودات تكون بشكل سيرفرات مرتبة جدا")
    var isSmartQualityMergeEnabled by remember { mutableStateOf(true) }
    var isQualityOrderHighToLow by remember { mutableStateOf(true) } // true: 4K -> FHD -> HD -> SD, false: SD -> HD -> FHD -> 4K

    // 4. Ordered Selected Channels State
    val selectedOrderedStreams = remember { mutableStateListOf<XtreamLiveStreamItem>() }

    // 5. Branch Metadata State
    var branchNameInput by remember { mutableStateOf("") }
    var branchImageUrlInput by remember { mutableStateOf("") }
    var selectedParentBranchId by remember { mutableStateOf("") }
    var targetExistingBranchId by remember { mutableStateOf<String?>(null) }

    // Sync inputs if catalog was already fetched
    LaunchedEffect(fetchedCatalog?.host, fetchedCatalog?.username) {
        if (fetchedCatalog != null) {
            if (hostInput.isBlank()) hostInput = fetchedCatalog.host
            if (usernameInput.isBlank()) usernameInput = fetchedCatalog.username
            if (passwordInput.isBlank()) passwordInput = fetchedCatalog.password
        }
    }

    val allCategories = fetchedCatalog?.categories.orEmpty()
    val allStreams = fetchedCatalog?.streams.orEmpty()

    val filteredCategories = remember(allCategories, categorySearchQuery) {
        val q = categorySearchQuery.trim()
        if (q.isEmpty()) {
            allCategories
        } else {
            allCategories.filter { it.categoryName.contains(q, ignoreCase = true) }
        }
    }

    // Raw streams belonging to the selected/combined categories
    val rawStreamsInSelectedCategories = remember(
        allStreams,
        isAllCategoriesSelected,
        selectedCategoryIds.toList()
    ) {
        if (isAllCategoriesSelected || selectedCategoryIds.isEmpty()) {
            allStreams
        } else {
            val idSet = selectedCategoryIds.toHashSet()
            allStreams.filter { it.categoryId in idSet }
        }
    }

    // Apply Smart Multi-Quality Merging if enabled (unifies SD / HD / FHD / 4K into 1 channel with ordered quality servers)
    val processedCategoryStreams = remember(
        rawStreamsInSelectedCategories,
        isSmartQualityMergeEnabled,
        isQualityOrderHighToLow
    ) {
        if (isSmartQualityMergeEnabled) {
            SmartChannelQualityMerger.mergeStreamsByChannelAndQuality(
                rawStreams = rawStreamsInSelectedCategories,
                highToLowOrder = isQualityOrderHighToLow
            )
        } else {
            rawStreamsInSelectedCategories
        }
    }

    // Apply channel search filter
    val displayedStreams = remember(processedCategoryStreams, channelSearchQuery) {
        val q = channelSearchQuery.trim()
        if (q.isEmpty()) {
            processedCategoryStreams
        } else {
            processedCategoryStreams.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.categoryName.contains(q, ignoreCase = true)
            }
        }
    }

    val selectedStreamIdsSet = remember(selectedOrderedStreams.size) {
        selectedOrderedStreams.map { it.streamId }.toSet()
    }

    val selectedCategoriesObjects = remember(allCategories, selectedCategoryIds.toList()) {
        val idSet = selectedCategoryIds.toHashSet()
        allCategories.filter { it.categoryId in idSet }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dev_publish_account_branch_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==================== STEP 1: ACCOUNT CREDENTIALS & FETCH ====================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulPurpleBright, FoulTeamAwayCyan)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FoulPurpleBright.copy(alpha = 0.22f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = FoulTeamAwayCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "نشر فرع حساب (جمع التصنيفات ودمج الجودات في سيرفرات)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "أضف الهوست واسم المستخدم وكلمة المرور لجلب التصنيفات، جمع عدة تصنيفات معاً، ودمج الجودات (SD / HD / FHD / 4K) في قناة واحدة بسيرفرات مرتبة",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    OrderedColorfulDivider(thickness = 1.dp)
                    Spacer(Modifier.height(10.dp))

                    // Quick Fill from Existing Xtream Accounts if available
                    if (xtreamAccounts.isNotEmpty()) {
                        Text(
                            text = "اختيار سريع من حسابات Xtream المحفوظة (${xtreamAccounts.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulGoldStar,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(xtreamAccounts, key = { it.id }) { acc ->
                                val isCurrentAcc = hostInput.trim() == acc.host.trim() &&
                                    usernameInput.trim() == acc.username.trim()
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCurrentAcc) FoulPurpleBright else FoulCardElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrentAcc) Color.White else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            hostInput = acc.host
                                            usernameInput = acc.username
                                            passwordInput = acc.password
                                            onFetchAccountCatalog(acc.host, acc.username, acc.password)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${acc.name.ifBlank { "حساب" }} (${acc.username})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    AccountBranchTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = "الهوست (Host / Server URL) مثال: http://line.example.com:8080",
                        testTag = "dev_acc_branch_host_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AccountBranchTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                label = "اسم المستخدم (Username)",
                                testTag = "dev_acc_branch_user_input"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AccountBranchTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = "كلمة المرور (Password)",
                                testTag = "dev_acc_branch_pass_input"
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onFetchAccountCatalog(hostInput, usernameInput, passwordInput)
                            },
                            enabled = !isFetchingCatalog && !isActionLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("dev_acc_branch_fetch_button")
                        ) {
                            if (isFetchingCatalog) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "جاري جلب تصنيفات وقنوات الحساب...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "جلب التصنيفات والقنوات من الحساب",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (fetchedCatalog != null) {
                            OutlinedButton(
                                onClick = {
                                    selectedOrderedStreams.clear()
                                    selectedCategoryIds.clear()
                                    isAllCategoriesSelected = true
                                    onClearCatalog()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.7f)),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "مسح",
                                    tint = FoulLiveRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (!fetchError.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = FoulLiveRed.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fetchError,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== STEP 2: MULTI-CATEGORY COMBINING & SMART QUALITY MERGING ====================
        if (fetchedCatalog != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = FoulGoldStar,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "1. تصنيفات الحساب وجمع التصنيفات (${allCategories.size} تصنيف)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "اضغط على أكثر من تصنيف لجمعها معاً (مثال: تصنيف beIN FHD + تصنيف beIN HD + تصنيف beIN SD)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FoulTextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = FoulPurpleDeep,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = if (isAllCategoriesSelected || selectedCategoryIds.isEmpty()) {
                                        "الكل (${allStreams.size})"
                                    } else {
                                        "مدمج: ${selectedCategoryIds.size} تصنيف"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulTeamAwayCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        AccountBranchTextField(
                            value = categorySearchQuery,
                            onValueChange = { categorySearchQuery = it },
                            label = "بحث في تصنيفات الحساب (مثال: beIN أو Sports أو العربية)...",
                            testTag = "dev_acc_branch_category_search_input"
                        )

                        Spacer(Modifier.height(8.dp))

                        // Quick Multi-Category Combine Buttons when searching or browsing categories
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isAllCategoriesSelected = false
                                    val existing = selectedCategoryIds.toHashSet()
                                    filteredCategories.forEach { cat ->
                                        if (existing.add(cat.categoryId)) {
                                            selectedCategoryIds.add(cat.categoryId)
                                        }
                                    }
                                    if (branchNameInput.isBlank() && categorySearchQuery.isNotBlank()) {
                                        branchNameInput = categorySearchQuery.trim()
                                    }
                                },
                                enabled = filteredCategories.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dev_acc_combine_filtered_categories_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = FoulTeamAwayCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (categorySearchQuery.isNotBlank()) {
                                        "جمع كل التصنيفات المطابقة (${filteredCategories.size})"
                                    } else {
                                        "تحديد وجمع عدة تصنيفات (${filteredCategories.size})"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!isAllCategoriesSelected && selectedCategoryIds.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        selectedCategoryIds.clear()
                                        isAllCategoriesSelected = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulDivider)
                                ) {
                                    Text(
                                        text = "إلغاء تحديد التصنيفات",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Horizontal Multi-Select Category Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item(key = "cat_ALL") {
                                val isAll = isAllCategoriesSelected || selectedCategoryIds.isEmpty()
                                Surface(
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (isAll) FoulPurpleBright else FoulCardElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isAll) Color.White else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable {
                                            selectedCategoryIds.clear()
                                            isAllCategoriesSelected = true
                                        }
                                        .testTag("dev_acc_cat_chip_ALL")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "كل القنوات (${allStreams.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = if (isAll) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            items(filteredCategories, key = { "cat_${it.categoryId}" }) { cat ->
                                val isSelected = !isAllCategoriesSelected && cat.categoryId in selectedCategoryIds
                                Surface(
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (isSelected) FoulPurpleBright else FoulCardElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color.White else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable {
                                            if (isAllCategoriesSelected) {
                                                isAllCategoriesSelected = false
                                                selectedCategoryIds.clear()
                                                selectedCategoryIds.add(cat.categoryId)
                                            } else if (cat.categoryId in selectedCategoryIds) {
                                                selectedCategoryIds.remove(cat.categoryId)
                                                if (selectedCategoryIds.isEmpty()) {
                                                    isAllCategoriesSelected = true
                                                }
                                            } else {
                                                selectedCategoryIds.add(cat.categoryId)
                                            }

                                            if (branchNameInput.isBlank()) {
                                                branchNameInput = cat.categoryName
                                            }
                                        }
                                        .testTag("dev_acc_cat_chip_${cat.categoryId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = if (isSelected) FoulLiveGreen else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.categoryName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.32f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${cat.channelCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FoulGoldStar,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Action Bar for Selected/Combined Categories: Fetch Streams from Server & Set Branch Name
                        if (!isAllCategoriesSelected && selectedCategoriesObjects.isNotEmpty()) {
                            val combinedNamesText = selectedCategoriesObjects.joinToString(" + ") { it.categoryName }
                            val combinedIdsParam = selectedCategoriesObjects.joinToString(",") { it.categoryId }

                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = FoulPurpleDeep.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (selectedCategoriesObjects.size == 1) {
                                                    "التصنيف المختار: ${selectedCategoriesObjects.first().categoryName}"
                                                } else {
                                                    "التصنيفات المجمعة (${selectedCategoriesObjects.size}): $combinedNamesText"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "إجمالي القنوات الخام في التصنيفات المحددة: ${rawStreamsInSelectedCategories.size} قناة • بعد دمج الجودات: ${processedCategoryStreams.size} قناة موحدة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FoulTeamAwayCyan
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onFetchCategoryStreams(
                                                    hostInput,
                                                    usernameInput,
                                                    passwordInput,
                                                    combinedIdsParam,
                                                    combinedNamesText
                                                )
                                            },
                                            enabled = !isFetchingCategoryStreams,
                                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("dev_acc_fetch_category_content_button")
                                        ) {
                                            if (isFetchingCategoryStreams) {
                                                CircularProgressIndicator(
                                                    color = Color.White,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "جاري الجلب...",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(5.dp))
                                                Text(
                                                    text = if (selectedCategoriesObjects.size > 1) {
                                                        "جلب وجمع قنوات الـ ${selectedCategoriesObjects.size} تصنيفات"
                                                    } else {
                                                        "جلب محتوى التصنيف"
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                branchNameInput = if (selectedCategoriesObjects.size == 1) {
                                                    selectedCategoriesObjects.first().categoryName
                                                } else {
                                                    selectedCategoriesObjects.first().categoryName
                                                        .replace(Regex("\\b(FHD|HD|SD|4K|UHD|HEVC)\\b", RegexOption.IGNORE_CASE), "")
                                                        .trim()
                                                        .ifBlank { selectedCategoriesObjects.first().categoryName }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, FoulGoldStar.copy(alpha = 0.7f))
                                        ) {
                                            Text(
                                                text = "تسمية الفرع تلقائياً",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FoulGoldStar,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // ==================== SMART MULTI-QUALITY MERGE CONTROL CARD ====================
                        Surface(
                            color = if (isSmartQualityMergeEnabled) {
                                FoulPurplePrimary.copy(alpha = 0.38f)
                            } else {
                                FoulCardElevated
                            },
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSmartQualityMergeEnabled) FoulLiveGreen.copy(alpha = 0.75f) else FoulDivider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                            color = if (isSmartQualityMergeEnabled) FoulLiveGreen.copy(alpha = 0.22f) else FoulCardDark,
                                            shape = RoundedCornerShape(9.dp),
                                            border = BorderStroke(1.dp, if (isSmartQualityMergeEnabled) FoulLiveGreen else FoulDivider),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.MergeType,
                                                    contentDescription = null,
                                                    tint = if (isSmartQualityMergeEnabled) FoulLiveGreen else Color.White,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "توحيد القنوات المتشابهة وجعل الجودات (SD / HD / FHD / 4K) سيرفرات مرتبة",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "مثال: beIN Sports 1 بجودات SD و HD و FHD تصبح قناة واحدة باسم (beIN Sports 1) وداخلها سيرفرات مرتبة لكل جودة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FoulTextSecondary
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isSmartQualityMergeEnabled,
                                        onCheckedChange = { isSmartQualityMergeEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = FoulLiveGreen,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = FoulCardDark
                                        ),
                                        modifier = Modifier.testTag("dev_acc_smart_quality_merge_switch")
                                    )
                                }

                                if (isSmartQualityMergeEnabled) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ترتيب سيرفرات الجودات داخل القناة الموحدة:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FoulGoldStar,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Surface(
                                            color = FoulBlackBg,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, FoulPurpleBright),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isQualityOrderHighToLow = !isQualityOrderHighToLow }
                                                .testTag("dev_acc_quality_order_toggle_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SwapVert,
                                                    contentDescription = null,
                                                    tint = FoulTeamAwayCyan,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = if (isQualityOrderHighToLow) {
                                                        "الأعلى جودة أولاً (4K ⬅ FHD ⬅ HD ⬅ SD)"
                                                    } else {
                                                        "الأقل استهلاكاً أولاً (SD ⬅ HD ⬅ FHD ⬅ 4K)"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
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

            // ==================== STEP 3: CHANNEL SELECTION FROM CATEGORY ====================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = FoulLiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isSmartQualityMergeEnabled) {
                                            "2. القنوات الموحدة وسيرفرات الجودات (${displayedStreams.size} قناة موحدة)"
                                        } else {
                                            "2. اختيار القنوات من التصنيف (${displayedStreams.size} قناة معروضة)"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (isSmartQualityMergeEnabled) {
                                            "تم دمج الجودات المتشابهة تلقائياً في قناة واحدة مع سيرفرات مرتبة لكل جودة"
                                        } else {
                                            "اضغط على أي قناة لإضافتها أو إزالتها، أو حدد كل القنوات دفعة واحدة"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FoulTextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = if (selectedOrderedStreams.isNotEmpty()) FoulLiveGreen.copy(alpha = 0.2f) else FoulCardElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedOrderedStreams.isNotEmpty()) FoulLiveGreen else FoulDivider
                                )
                            ) {
                                Text(
                                    text = "المختار: ${selectedOrderedStreams.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedOrderedStreams.isNotEmpty()) FoulLiveGreen else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        AccountBranchTextField(
                            value = channelSearchQuery,
                            onValueChange = { channelSearchQuery = it },
                            label = "بحث داخل القنوات بالاسم (مثال: beIN Sports أو SSC أو الكأس)...",
                            testTag = "dev_acc_branch_channel_search_input"
                        )

                        Spacer(Modifier.height(10.dp))

                        // Bulk Selection Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val existingIds = selectedOrderedStreams.map { it.streamId }.toHashSet()
                                    for (stream in displayedStreams) {
                                        if (existingIds.add(stream.streamId)) {
                                            selectedOrderedStreams.add(stream)
                                        }
                                    }
                                    if (branchNameInput.isBlank() && selectedCategoriesObjects.isNotEmpty()) {
                                        branchNameInput = selectedCategoriesObjects.first().categoryName
                                            .replace(Regex("\\b(FHD|HD|SD|4K|UHD|HEVC)\\b", RegexOption.IGNORE_CASE), "")
                                            .trim()
                                            .ifBlank { selectedCategoriesObjects.first().categoryName }
                                    }
                                    if (branchImageUrlInput.isBlank()) {
                                        branchImageUrlInput = selectedOrderedStreams.firstOrNull { it.streamIcon.isNotBlank() }?.streamIcon.orEmpty()
                                    }
                                },
                                enabled = displayedStreams.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleDeep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dev_acc_select_all_category_channels_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = FoulLiveGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "تحديد كل المعروض (${displayedStreams.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val displayedIds = displayedStreams.map { it.streamId }.toHashSet()
                                    selectedOrderedStreams.removeAll { it.streamId in displayedIds }
                                },
                                enabled = selectedOrderedStreams.isNotEmpty(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.7f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = FoulLiveRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "إلغاء تحديد المعروض",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Scrollable List of Channels (Merged by Quality or Raw)
                        if (displayedStreams.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد قنوات مطابقة في هذا التصنيف حالياً",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FoulTextSecondary
                                )
                            }
                        } else {
                            val visibleSlice = remember(displayedStreams) {
                                displayedStreams.take(250)
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(7.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                visibleSlice.forEach { stream ->
                                    val isSelected = selectedStreamIdsSet.contains(stream.streamId)
                                    val orderIndex = if (isSelected) {
                                        selectedOrderedStreams.indexOfFirst { it.streamId == stream.streamId } + 1
                                    } else 0

                                    Surface(
                                        color = if (isSelected) FoulPurpleDeep.copy(alpha = 0.65f) else FoulCardElevated,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) FoulPurpleBright else FoulDivider
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isSelected) {
                                                    selectedOrderedStreams.removeAll { it.streamId == stream.streamId }
                                                } else {
                                                    selectedOrderedStreams.add(stream)
                                                    if (branchImageUrlInput.isBlank() && stream.streamIcon.isNotBlank()) {
                                                        branchImageUrlInput = stream.streamIcon
                                                    }
                                                }
                                            }
                                            .testTag("dev_acc_stream_item_${stream.streamId}")
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
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = null,
                                                    tint = if (isSelected) FoulLiveGreen else Color.White.copy(alpha = 0.55f),
                                                    modifier = Modifier.size(22.dp)
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = FoulBlackBg,
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    if (stream.streamIcon.isNotBlank()) {
                                                        AsyncImage(
                                                            model = stream.streamIcon,
                                                            contentDescription = stream.name,
                                                            contentScale = ContentScale.Fit,
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(3.dp)
                                                        )
                                                    } else {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.LiveTv,
                                                                contentDescription = null,
                                                                tint = FoulPurpleBright,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text(
                                                        text = stream.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    // Show ordered quality servers pills if merged
                                                    if (stream.mergedServers.isNotEmpty()) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Text(
                                                                text = "السيرفرات:",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                color = FoulTextSecondary
                                                            )
                                                            stream.mergedServers.take(6).forEach { srv ->
                                                                Surface(
                                                                    color = FoulBlackBg,
                                                                    shape = RoundedCornerShape(5.dp),
                                                                    border = BorderStroke(0.8.dp, FoulPurpleBright.copy(alpha = 0.7f))
                                                                ) {
                                                                    Text(
                                                                        text = srv.name,
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                                                        color = FoulTeamAwayCyan,
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Text(
                                                            text = stream.categoryName,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = FoulTextSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            if (isSelected && orderIndex > 0) {
                                                Surface(
                                                    color = FoulLiveGreen,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "#$orderIndex",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (displayedStreams.size > visibleSlice.size) {
                                    Text(
                                        text = "يتم عرض أول ${visibleSlice.size} قناة من أصل ${displayedStreams.size} — استخدم مربع البحث أعلاه للوصول الفوري لأي قناة محددة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FoulGoldStar,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== STEP 4: REORDER & MERGE SELECTED CHANNELS ("ترتيب القنوات وسيرفرات الجودات") ====================
        if (selectedOrderedStreams.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulPurpleBright.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3. القنوات المختارة وسيرفرات الجودات قبل النشر (${selectedOrderedStreams.size} قناة)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "رتّب القنوات أو اضغط زر توحيد الجودات لدمج القنوات المتشابهة في قناة واحدة بسيرفرات مرتبة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary
                                )
                            }

                            Surface(
                                color = FoulLiveRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedOrderedStreams.clear() }
                            ) {
                                Text(
                                    text = "مسح الكل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // One-Click Action to Merge Any Selected Multi-Quality Channels into Single Channel with Ordered Quality Servers
                        Button(
                            onClick = {
                                val merged = SmartChannelQualityMerger.mergeStreamsByChannelAndQuality(
                                    rawStreams = selectedOrderedStreams.toList(),
                                    highToLowOrder = isQualityOrderHighToLow
                                )
                                selectedOrderedStreams.clear()
                                selectedOrderedStreams.addAll(merged)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dev_acc_merge_selected_qualities_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FoulGoldStar,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "دمج القنوات المتشابهة وجعل الجودات (SD / HD / FHD / 4K) سيرفرات مرتبة",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedOrderedStreams.forEachIndexed { idx, item ->
                                Surface(
                                    color = FoulCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FoulDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = FoulPurpleBright,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "#${idx + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (item.mergedServers.isNotEmpty()) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Dns,
                                                            contentDescription = null,
                                                            tint = FoulLiveGreen,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        item.mergedServers.forEach { srv ->
                                                            Surface(
                                                                color = FoulBlackBg,
                                                                shape = RoundedCornerShape(4.dp),
                                                                border = BorderStroke(0.7.dp, FoulLiveGreen.copy(alpha = 0.6f))
                                                            ) {
                                                                Text(
                                                                    text = srv.name,
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                    color = FoulLiveGreen,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (idx > 0) {
                                                        val removed = selectedOrderedStreams.removeAt(idx)
                                                        selectedOrderedStreams.add(idx - 1, removed)
                                                    }
                                                },
                                                enabled = idx > 0,
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = "تحريك للأعلى",
                                                    tint = if (idx > 0) Color.White else Color.White.copy(alpha = 0.25f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (idx < selectedOrderedStreams.lastIndex) {
                                                        val removed = selectedOrderedStreams.removeAt(idx)
                                                        selectedOrderedStreams.add(idx + 1, removed)
                                                    }
                                                },
                                                enabled = idx < selectedOrderedStreams.lastIndex,
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "تحريك للأسفل",
                                                    tint = if (idx < selectedOrderedStreams.lastIndex) Color.White else Color.White.copy(alpha = 0.25f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    selectedOrderedStreams.removeAt(idx)
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "إزالة القناة",
                                                    tint = FoulLiveRed,
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

        // ==================== STEP 5: BRANCH DETAILS & FINAL PUBLISH ====================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulLiveGreen, FoulPurpleBright)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = FoulGoldStar,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "4. بيانات الفرع والنشر في الرئيسية",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "أضف اسم الفرع ورابط صورة الفرع ثم اضغط نشر الفرع مع القنوات وسيرفرات الجودات المرتبة",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    AccountBranchTextField(
                        value = branchNameInput,
                        onValueChange = { branchNameInput = it },
                        label = "اسم الفرع (مثال: باقة قنوات بي إن سبورت)",
                        testTag = "dev_acc_branch_name_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    AccountBranchTextField(
                        value = branchImageUrlInput,
                        onValueChange = { branchImageUrlInput = it },
                        label = "رابط صورة الفرع (http://... أو https://...)",
                        testTag = "dev_acc_branch_image_input"
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "مكان النشر (فرع رئيسي جديد أو داخل فرع موجود):",
                        style = MaterialTheme.typography.labelMedium,
                        color = FoulTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isRoot = selectedParentBranchId.isBlank() && targetExistingBranchId == null
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isRoot) FoulPurpleBright else FoulCardElevated,
                                border = BorderStroke(1.dp, if (isRoot) Color.White else FoulDivider),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedParentBranchId = ""
                                        targetExistingBranchId = null
                                    }
                            ) {
                                Text(
                                    text = "فرع رئيسي جديد (في الرئيسية)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isRoot) FontWeight.ExtraBold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(branches.sortedBy { it.order }, key = { "parent_${it.id}" }) { br ->
                            val isSelectedParent = selectedParentBranchId == br.id && targetExistingBranchId == null
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelectedParent) FoulPurpleBright else FoulCardElevated,
                                border = BorderStroke(1.dp, if (isSelectedParent) Color.White else FoulDivider),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedParentBranchId = br.id
                                        targetExistingBranchId = null
                                    }
                            ) {
                                Text(
                                    text = "فرع داخل: ${br.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isSelectedParent) FontWeight.ExtraBold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Optional: Update/Append to an existing branch directly
                    if (branches.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "أو تحديث فرع حالي موجود مباشرة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTeamAwayCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(branches.sortedBy { it.order }, key = { "existing_${it.id}" }) { br ->
                                val isTarget = targetExistingBranchId == br.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isTarget) FoulLiveGreen.copy(alpha = 0.25f) else FoulCardElevated,
                                    border = BorderStroke(1.dp, if (isTarget) FoulLiveGreen else FoulDivider),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isTarget) {
                                                targetExistingBranchId = null
                                            } else {
                                                targetExistingBranchId = br.id
                                                branchNameInput = br.name
                                                branchImageUrlInput = br.imageUrl
                                                selectedParentBranchId = br.parentId
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "تحديث: ${br.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = if (isTarget) FontWeight.ExtraBold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val finalStreamsToPublish = if (isSmartQualityMergeEnabled) {
                                SmartChannelQualityMerger.mergeStreamsByChannelAndQuality(
                                    rawStreams = selectedOrderedStreams.toList(),
                                    highToLowOrder = isQualityOrderHighToLow
                                )
                            } else {
                                selectedOrderedStreams.toList()
                            }
                            onPublishAccountBranch(
                                targetExistingBranchId,
                                branchNameInput,
                                branchImageUrlInput,
                                selectedParentBranchId,
                                finalStreamsToPublish
                            )
                        },
                        enabled = !isActionLoading && selectedOrderedStreams.isNotEmpty() && branchNameInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = FoulLiveGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dev_acc_branch_publish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Publish,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "نشر فرع الحساب مع (${selectedOrderedStreams.size} قناة مرتبة) الآن",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountBranchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FoulPurpleBright,
            unfocusedBorderColor = FoulDivider,
            focusedContainerColor = FoulCardElevated,
            unfocusedContainerColor = FoulCardElevated,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = FoulPurpleBright,
            unfocusedLabelColor = FoulTextSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
