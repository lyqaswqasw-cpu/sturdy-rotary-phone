package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelEngagementStats
import com.example.data.model.EventBanner
import com.example.data.model.LiveChannel
import com.example.data.model.PublishedEventItem
import com.example.ui.components.AdMobBanner
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.FoulCustomPackageIcon
import com.example.ui.components.FoulSectionSubBar
import com.example.ui.components.FoulSmartTopBar
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.components.tvFocusableClickable
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleAccent
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulPurpleGlow
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulSurfaceDark
import com.example.ui.theme.FoulTextMuted
import com.example.ui.theme.FoulTextPrimary
import com.example.ui.theme.FoulTextSecondary

@Composable
fun HomeScreen(
    isLoading: Boolean,
    errorMessage: String?,
    currentBranch: ChannelCategory?,
    visibleBranches: List<ChannelCategory>,
    visibleChannels: List<LiveChannel>,
    allCategories: List<ChannelCategory>,
    allChannels: List<LiveChannel>,
    banners: List<EventBanner> = emptyList(),
    publishedEvents: List<PublishedEventItem> = emptyList(),
    channelStatsMap: Map<String, ChannelEngagementStats> = emptyMap(),
    showChannelViewsAndLikes: Boolean = true,
    showAdsInApp: Boolean = true,
    telegramUrl: String = "https://t.me/jdj_q",
    onOpenBranch: (ChannelCategory) -> Unit,
    onNavigateBackBranch: () -> Unit,
    onChannelClick: (LiveChannel) -> Unit,
    onBannerClick: (EventBanner) -> Unit = {},
    onPlayPublishedEventVideo: (PublishedEventItem) -> Unit = {},
    onRefresh: () -> Unit
) {
    var isHomeSearchOpen by remember { mutableStateOf(false) }
    var homeSearchQuery by remember { mutableStateOf("") }

    // Intercept system back button when search is open or when inside a branch
    if (isHomeSearchOpen) {
        BackHandler {
            isHomeSearchOpen = false
            homeSearchQuery = ""
        }
    } else if (currentBranch != null) {
        BackHandler {
            onNavigateBackBranch()
        }
    }

    var pickerTitle by remember { mutableStateOf("") }
    var pickerSubtitle by remember { mutableStateOf("") }
    var pickerChannels by remember { mutableStateOf<List<LiveChannel>>(emptyList()) }

    if (pickerChannels.isNotEmpty()) {
        MatchAndEventChannelPickerDialog(
            title = pickerTitle,
            subtitle = pickerSubtitle,
            channels = pickerChannels,
            onSelectChannel = { ch ->
                pickerChannels = emptyList()
                onChannelClick(ch)
            },
            onDismiss = { pickerChannels = emptyList() }
        )
    }

    val filteredBranches = remember(visibleBranches, allCategories, isHomeSearchOpen, homeSearchQuery) {
        val q = homeSearchQuery.trim()
        if (!isHomeSearchOpen || q.isEmpty()) {
            visibleBranches
        } else {
            allCategories.filter { it.name.contains(q, ignoreCase = true) }
        }
    }

    val filteredChannels = remember(visibleChannels, allChannels, isHomeSearchOpen, homeSearchQuery) {
        val q = homeSearchQuery.trim()
        if (!isHomeSearchOpen || q.isEmpty()) {
            visibleChannels
        } else {
            allChannels.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("home_screen")
    ) {
        // 1. Smart Top Bar: Strictly Telegram Icon + FOUL TV Name + App Logo Image
        FoulSmartTopBar(
            telegramUrl = telegramUrl,
            telegramTestTag = "home_telegram_button"
        )

        // 2. Contextual Sub-Header Bar Directly Underneath Top Bar (Back Arrow + Title + Search + Refresh)
        FoulSectionSubBar(
            title = when {
                isHomeSearchOpen -> "البحث في القنوات والباقات"
                currentBranch != null -> currentBranch.name
                else -> "البث المباشر • القنوات والباقات"
            },
            subtitle = when {
                isHomeSearchOpen -> "ابحث عن أي قناة أو باقة مباشرة"
                currentBranch != null -> "تصفح محتويات (${currentBranch.name})"
                else -> "الباقات التلفزيونية والأحداث المباشرة"
            },
            onBackClick = when {
                currentBranch != null -> onNavigateBackBranch
                isHomeSearchOpen -> {
                    {
                        isHomeSearchOpen = false
                        homeSearchQuery = ""
                    }
                }
                else -> null
            },
            backButtonTestTag = "home_branch_back_button",
            onSearchClick = {
                isHomeSearchOpen = !isHomeSearchOpen
                if (!isHomeSearchOpen) homeSearchQuery = ""
            },
            searchButtonLabel = if (isHomeSearchOpen) "إغلاق البحث" else "بحث",
            searchButtonTestTag = "home_search_button",
            actions = {
                CompactTopBarIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "تحديث القنوات والباقات",
                    onClick = onRefresh,
                    modifier = Modifier.testTag("home_refresh_button")
                )
            }
        )

        // Expandable Search Bar under Sub-Header when activated
        AnimatedVisibility(visible = isHomeSearchOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FoulSurfaceDark)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = homeSearchQuery,
                    onValueChange = { homeSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "اكتب اسم القناة أو الباقة للبحث الفوري...",
                            color = FoulTextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        if (homeSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { homeSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = FoulTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FoulCardDark,
                        unfocusedContainerColor = FoulCardDark,
                        focusedBorderColor = FoulPurpleBright,
                        unfocusedBorderColor = FoulDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val gridColumns = when {
                maxWidth >= 960.dp -> 5
                maxWidth >= 660.dp -> 4
                else -> 3
            }

            when {
                isLoading && allChannels.isEmpty() && allCategories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = FoulPurpleBright)
                            Text(
                                text = "جاري تحميل الباقات والقنوات المباشرة...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FoulTextSecondary
                            )
                        }
                    }
                }

                errorMessage != null && allChannels.isEmpty() && allCategories.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, FoulDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "تعذر الاتصال بقاعدة البيانات",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FoulLiveRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                                    modifier = Modifier.testTag("retry_home_button")
                                ) {
                                    Text("إعادة المحاولة", color = Color.White)
                                }
                            }
                        }
                    }
                }

                filteredBranches.isEmpty() && filteredChannels.isEmpty() && publishedEvents.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FoulCustomPackageIcon(
                                tint = FoulPurpleGlow,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (currentBranch != null) {
                                    "هذا القسم (${currentBranch.name}) فارغ حالياً"
                                } else {
                                    "لا توجد أقسام أو قنوات مطابقة حالياً"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = FoulTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            if (currentBranch != null) {
                                OutlinedButton(
                                    onClick = onNavigateBackBranch,
                                    border = BorderStroke(1.dp, FoulPurpleBright)
                                ) {
                                    Text("العودة للقسم السابق", color = Color.White)
                                }
                            }
                        }
                    }
                }

                else -> {
                    val gridState = rememberLazyGridState()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("channels_grid"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Unified Single-Slot Auto-Advancing Carousel for Published Events (Root level only, transitions every 3 seconds)
                        if (currentBranch == null && !isHomeSearchOpen && publishedEvents.isNotEmpty()) {
                            item(
                                key = "published_events_carousel",
                                span = { GridItemSpan(gridColumns) },
                                contentType = "events_carousel"
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PublishedEventsAutoPagerCarousel(
                                        events = publishedEvents,
                                        allChannels = allChannels,
                                        onWatchEvent = { eventItem, resolvedChannels ->
                                            val isVideo = eventItem.eventType.equals("VIDEO", ignoreCase = true) ||
                                                eventItem.videoUrl.isNotBlank()
                                            if (isVideo && eventItem.videoUrl.isNotBlank()) {
                                                onPlayPublishedEventVideo(eventItem)
                                            } else if (resolvedChannels.size == 1) {
                                                onChannelClick(resolvedChannels.first())
                                            } else if (resolvedChannels.size > 1) {
                                                pickerTitle = eventItem.title.ifBlank { "القنوات الناقلة للحدث" }
                                                pickerSubtitle = "اختر القناة الناقلة لمشاهدة الحدث مباشرة"
                                                pickerChannels = resolvedChannels
                                            }
                                        }
                                    )
                                    OrderedColorfulDivider(thickness = 1.dp)
                                }
                            }
                        }

                        // Sub-branches / Folders: Each folder spans full width in a sleek pure-black & dark-purple card
                        if (filteredBranches.isNotEmpty()) {
                            items(
                                items = filteredBranches,
                                key = { "branch_${it.id}" },
                                span = { GridItemSpan(gridColumns) },
                                contentType = { "branch_folder" }
                            ) { branch ->
                                val subBranchesCount = remember(allCategories, branch.id) {
                                    allCategories.count { it.parentId == branch.id }
                                }
                                val directChannelsCount = remember(allChannels, branch) {
                                    allChannels.count {
                                        it.branchId == branch.id ||
                                            (it.branchId.isBlank() && it.category.equals(branch.name, ignoreCase = true))
                                    }
                                }

                                BranchFolderCard(
                                    branch = branch,
                                    subBranchesCount = subBranchesCount,
                                    directChannelsCount = directChannelsCount,
                                    onClick = {
                                        if (isHomeSearchOpen) {
                                            isHomeSearchOpen = false
                                            homeSearchQuery = ""
                                        }
                                        onOpenBranch(branch)
                                    }
                                )
                            }
                        }

                        // Section header between sub-branches and channels if both exist
                        if (filteredBranches.isNotEmpty() && filteredChannels.isNotEmpty()) {
                            item(
                                key = "channels_section_header",
                                span = { GridItemSpan(gridColumns) },
                                contentType = "section_header"
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(FoulPurpleBright)
                                    )
                                    Text(
                                        text = "القنوات المباشرة المتوفرة (${filteredChannels.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Channels Grid (3 columns on phones, 4-5 columns on Android TV / wide screens)
                        items(
                            items = filteredChannels,
                            key = { "channel_${it.id}" },
                            span = { GridItemSpan(1) },
                            contentType = { "live_channel_card" }
                        ) { channel ->
                            val socialStats = channelStatsMap[channel.id]
                            val totalViews = maxOf(channel.views, socialStats?.views ?: 0)
                            val totalLikes = socialStats?.likes ?: 0
                            LiveChannelGridCard(
                                channel = channel,
                                totalViews = totalViews,
                                totalLikes = totalLikes,
                                showViewsAndLikes = showChannelViewsAndLikes,
                                onClick = { onChannelClick(channel) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom AdMob Banner inside Home section (controlled online via Firebase database)
        if (showAdsInApp) {
            AdMobBanner()
        }
    }
}

private val BranchCardShape = RoundedCornerShape(14.dp)

private val BranchCardBorderBrush = Brush.horizontalGradient(
    colors = listOf(
        FoulPurpleBright.copy(alpha = 0.65f),
        FoulDivider,
        FoulDivider
    )
)

private val BranchCardBgBrush = Brush.horizontalGradient(
    colors = listOf(
        FoulPurpleDeep.copy(alpha = 0.48f),
        FoulCardDark,
        FoulBlackBg.copy(alpha = 0.92f)
    )
)

private val ChannelCardShape = RoundedCornerShape(14.dp)

private val ChannelCardBorderBrush = Brush.verticalGradient(
    colors = listOf(
        FoulDivider,
        FoulPurplePrimary.copy(alpha = 0.65f)
    )
)

private val ChannelCardBgBrush = Brush.verticalGradient(
    colors = listOf(
        FoulCardDark,
        FoulBlackBg
    )
)

/**
 * Full-width sleek pure-obsidian & dark-purple card for a Branch or Sub-branch.
 */
@Composable
private fun BranchFolderCard(
    branch: ChannelCategory,
    subBranchesCount: Int,
    directChannelsCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageRequest = remember(branch.imageUrl) {
        if (branch.imageUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(branch.imageUrl)
                .size(160)
                .allowHardware(true)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Card(
        shape = BranchCardShape,
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(BranchCardShape)
            .tvFocusableClickable(
                shape = BranchCardShape,
                scaleOnFocus = 1.02f,
                onClick = onClick
            )
            .border(
                width = 1.dp,
                brush = BranchCardBorderBrush,
                shape = BranchCardShape
            )
            .testTag("branch_folder_${branch.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BranchCardBgBrush)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Side (in RTL): Branch Logo/Icon + Title & Subtitle
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FoulBlackBg)
                        .border(1.dp, FoulPurpleBright.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageRequest != null) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = branch.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        FoulCustomPackageIcon(
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when {
                            subBranchesCount > 0 -> "يحتوي على $subBranchesCount قسم فرعي • اضغط للفتح"
                            directChannelsCount > 0 -> "باقة تضم $directChannelsCount قناة مباشرة • اضغط للفتح"
                            else -> "اضغط لتصفح محتويات هذه الباقة"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = FoulTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Left Side (in RTL): Counter Pill + Sleek Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val badgeText = when {
                    subBranchesCount > 0 && directChannelsCount > 0 -> "$subBranchesCount قسم • $directChannelsCount قناة"
                    subBranchesCount > 0 -> "$subBranchesCount أقسام"
                    directChannelsCount > 0 -> "$directChannelsCount قناة"
                    else -> "فتح"
                }

                Surface(
                    color = FoulBlackBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, FoulDivider)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(FoulPurplePrimary)
                        .border(1.dp, FoulPurpleBright.copy(alpha = 0.65f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "فتح القسم",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Pure Black & Obsidian Grid Card for a Live Channel with optional live Views & Likes counter.
 */
@Composable
private fun LiveChannelGridCard(
    channel: LiveChannel,
    totalViews: Int,
    totalLikes: Int,
    showViewsAndLikes: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isLogoLoaded by remember(channel.imageUrl) { mutableStateOf(false) }
    val imageRequest = remember(channel.imageUrl) {
        if (channel.imageUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(channel.imageUrl)
                .size(160)
                .allowHardware(true)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Card(
        shape = ChannelCardShape,
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (showViewsAndLikes) 156.dp else 138.dp)
            .clip(ChannelCardShape)
            .tvFocusableClickable(
                shape = ChannelCardShape,
                scaleOnFocus = 1.04f,
                onClick = onClick
            )
            .border(
                width = 1.dp,
                brush = ChannelCardBorderBrush,
                shape = ChannelCardShape
            )
            .testTag("channel_card_${channel.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChannelCardBgBrush)
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top-right LIVE green dot + server count badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.5.dp)
                            .clip(CircleShape)
                            .background(FoulLiveGreen)
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = FoulLiveGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (channel.servers.size > 1) {
                    Surface(
                        color = FoulBlackBg,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.6.dp, FoulDivider)
                    ) {
                        Text(
                            text = "${channel.servers.size} سيرفر",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = FoulTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Channel Logo in a sleek pure-black circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(FoulBlackBg)
                    .border(1.1.dp, FoulPurpleBright.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!isLogoLoaded) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = channel.name,
                        tint = FoulPurpleBright.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = channel.name,
                        onSuccess = { isLogoLoaded = true },
                        onError = { isLogoLoaded = false },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Full Channel Name Pill (No `...` truncation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FoulBlackBg.copy(alpha = 0.75f))
                    .border(0.6.dp, FoulDivider.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = if (channel.name.length > 22) 9.5.sp else 10.5.sp,
                        lineHeight = 12.5.sp
                    ),
                    color = FoulTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    softWrap = true,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Views & Likes Counter Strip (Controlled by Developer Setting)
            if (showViewsAndLikes) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(FoulBlackBg)
                        .border(0.7.dp, FoulDivider, RoundedCornerShape(7.dp))
                        .padding(horizontal = 5.dp, vertical = 2.5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "المشاهدات",
                            tint = FoulTextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = formatCompactCount(totalViews),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = FoulTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "الإعجابات",
                            tint = FoulGoldStar,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = formatCompactCount(totalLikes),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = FoulGoldStar,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatCompactCount(value: Int): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format("%.1fK", value / 1_000f)
        else -> value.toString()
    }
}
