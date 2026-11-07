package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.model.LibraryFilterType
import com.example.data.model.LibraryMediaItem
import com.example.data.model.MediaDetailInfo
import com.example.data.model.SeriesEpisode
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.FoulCompactTopBar
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.components.TelegramTopBarIconButton
import com.example.ui.components.tvFocusableClickable
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulSurfaceDark
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextMuted
import com.example.ui.theme.FoulTextPrimary
import com.example.ui.theme.FoulTextSecondary

@Composable
fun LibraryScreen(
    isLoading: Boolean,
    errorMessage: String?,
    mainFilter: LibraryFilterType,
    displayedItems: List<LibraryMediaItem>,
    hasMore: Boolean,
    isSearchOpen: Boolean,
    searchQuery: String,
    searchFilter: LibraryFilterType,
    searchResults: List<LibraryMediaItem>,
    isSearching: Boolean,
    selectedMediaItem: LibraryMediaItem?,
    isLoadingMediaDetail: Boolean,
    mediaDetailError: String?,
    mediaDetail: MediaDetailInfo?,
    selectedSeasonNumber: Int,
    telegramUrl: String = "https://t.me/jdj_q",
    onSelectMainFilter: (LibraryFilterType) -> Unit,
    onLoadMore: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchFilterChange: (LibraryFilterType) -> Unit,
    onMediaItemClick: (LibraryMediaItem) -> Unit,
    onCloseMediaDetail: () -> Unit,
    onSelectSeason: (Int) -> Unit,
    onPlayMovie: (LibraryMediaItem) -> Unit,
    onPlayEpisode: (LibraryMediaItem, SeriesEpisode) -> Unit,
    onRefresh: () -> Unit
) {
    // 1. Movie / Series Detail Fullscreen View
    if (selectedMediaItem != null) {
        MediaDetailScreen(
            item = selectedMediaItem,
            isLoading = isLoadingMediaDetail,
            errorMessage = mediaDetailError,
            detail = mediaDetail,
            selectedSeasonNumber = selectedSeasonNumber,
            onSelectSeason = onSelectSeason,
            onPlayMovie = { onPlayMovie(selectedMediaItem) },
            onPlayEpisode = { ep -> onPlayEpisode(selectedMediaItem, ep) },
            onRetry = { onMediaItemClick(selectedMediaItem) },
            onBack = onCloseMediaDetail
        )
        return
    }

    // 2. Dedicated Fast Search Screen across all Xtreme accounts
    if (isSearchOpen) {
        LibraryDedicatedSearchScreen(
            searchQuery = searchQuery,
            searchFilter = searchFilter,
            searchResults = searchResults,
            isSearching = isSearching,
            onSearchQueryChange = onSearchQueryChange,
            onSearchFilterChange = onSearchFilterChange,
            onMediaItemClick = onMediaItemClick,
            onBack = onCloseSearch
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("library_screen")
    ) {
        com.example.ui.components.FoulSmartTopBar(
            telegramUrl = telegramUrl,
            telegramTestTag = "library_telegram_button"
        )

        com.example.ui.components.FoulSectionSubBar(
            title = "المكتبة • الأفلام والمسلسلات",
            subtitle = "مكتبة الأفلام والمسلسلات المحدثة تلقائياً",
            onSearchClick = onOpenSearch,
            searchButtonLabel = "بحث في المكتبة",
            searchButtonTestTag = "library_open_search_button",
            actions = {
                CompactTopBarIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "تحديث المكتبة",
                    onClick = onRefresh,
                    modifier = Modifier.testTag("library_refresh_button")
                )
            }
        )

        // Luxury Filter Strip (الكل / الأفلام / المسلسلات) + Loaded Count Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF06060D))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LibraryFilterType.entries.forEach { filter ->
                    val selected = filter == mainFilter
                    Surface(
                        color = if (selected) FoulPurplePrimary else FoulCardDark,
                        shape = RoundedCornerShape(11.dp),
                        border = BorderStroke(
                            width = if (selected) 1.2.dp else 0.9.dp,
                            color = if (selected) FoulPurpleBright else FoulDivider
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .tvFocusableClickable(
                                shape = RoundedCornerShape(11.dp),
                                scaleOnFocus = 1.04f,
                                onClick = { onSelectMainFilter(filter) }
                            )
                            .testTag("library_filter_${filter.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FoulLiveGreen)
                                )
                            }
                            Text(
                                text = filter.labelAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else FoulTextSecondary,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (displayedItems.isNotEmpty()) {
                Surface(
                    color = FoulCardDark,
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(0.8.dp, FoulDivider)
                ) {
                    Text(
                        text = "${displayedItems.size}+ عنصر",
                        style = MaterialTheme.typography.labelSmall,
                        color = FoulGoldStar,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.dp)

        // Content Grid
        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading && displayedItems.isEmpty() -> {
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
                                text = "جاري جلب الأفلام والمسلسلات...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FoulTextSecondary
                            )
                        }
                    }
                }

                errorMessage != null && displayedItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FoulTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                                    modifier = Modifier.testTag("retry_library_button")
                                ) {
                                    Text("تحديث المكتبة", color = Color.White)
                                }
                            }
                        }
                    }
                }

                displayedItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = FoulPurpleBright,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "لا توجد أفلام أو مسلسلات متاحة حالياً",
                                style = MaterialTheme.typography.titleMedium,
                                color = FoulTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val gridState = rememberLazyGridState()
                    val shouldAutoLoadMore by remember(gridState, displayedItems.size, hasMore) {
                        derivedStateOf {
                            if (!hasMore || displayedItems.isEmpty()) {
                                false
                            } else {
                                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                lastVisibleIndex >= (displayedItems.size - 12).coerceAtLeast(0)
                            }
                        }
                    }

                    LaunchedEffect(shouldAutoLoadMore) {
                        if (shouldAutoLoadMore) {
                            onLoadMore()
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("library_grid"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = displayedItems,
                            key = { "${it.account.id}_${it.id}_${it.isSeries}" },
                            contentType = { "library_poster_card" }
                        ) { media ->
                            LibraryPosterCard(
                                item = media,
                                onClick = { onMediaItemClick(media) }
                            )
                        }

                        if (hasMore) {
                            item(
                                key = "library_load_more_footer",
                                span = { GridItemSpan(3) },
                                contentType = "library_footer"
                            ) {
                                OutlinedButton(
                                    onClick = onLoadMore,
                                    border = BorderStroke(1.dp, FoulPurpleBright),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .testTag("library_load_more_button")
                                ) {
                                    Text(
                                        text = "عرض المزيد من الأفلام والمسلسلات",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun LibraryDedicatedSearchScreen(
    searchQuery: String,
    searchFilter: LibraryFilterType,
    searchResults: List<LibraryMediaItem>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchFilterChange: (LibraryFilterType) -> Unit,
    onMediaItemClick: (LibraryMediaItem) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("library_search_screen")
    ) {
        com.example.ui.components.FoulSmartTopBar(
            telegramTestTag = "library_search_telegram_button"
        )

        com.example.ui.components.FoulSectionSubBar(
            title = "البحث الشامل في المكتبة",
            subtitle = "يبحث في جميع حسابات الأفلام والمسلسلات معاً بسرعة فائقة",
            onBackClick = onBack,
            backButtonTestTag = "library_search_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulSurfaceDark)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "ابحث عن فيلم أو مسلسل بدون تقطيع...",
                        color = FoulTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = FoulPurpleBright
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("library_clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح البحث",
                                tint = FoulTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
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
                    .testTag("library_search_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibraryFilterType.entries.forEach { filter ->
                    val selected = filter == searchFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onSearchFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.labelAr,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoulPurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = FoulCardDark,
                            labelColor = FoulTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = if (selected) FoulPurpleBright else FoulDivider
                        ),
                        modifier = Modifier.testTag("library_search_filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.2.dp)

        Box(modifier = Modifier.weight(1f)) {
            when {
                isSearching && searchResults.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FoulPurpleBright)
                    }
                }

                searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "لا توجد نتائج مطابقة لبحثك \"$searchQuery\""
                            } else {
                                "اكتب اسم الفيلم أو المسلسل للبحث الفوري"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = FoulTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    val searchGridState = rememberLazyGridState()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = searchGridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("library_search_results_grid"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = searchResults,
                            key = { "search_${it.account.id}_${it.id}_${it.isSeries}" },
                            contentType = { "library_poster_card" }
                        ) { media ->
                            LibraryPosterCard(
                                item = media,
                                onClick = { onMediaItemClick(media) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaDetailScreen(
    item: LibraryMediaItem,
    isLoading: Boolean,
    errorMessage: String?,
    detail: MediaDetailInfo?,
    selectedSeasonNumber: Int,
    onSelectSeason: (Int) -> Unit,
    onPlayMovie: () -> Unit,
    onPlayEpisode: (SeriesEpisode) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("media_detail_screen")
    ) {
        com.example.ui.components.FoulSmartTopBar(
            telegramTestTag = "media_detail_telegram_button"
        )

        com.example.ui.components.FoulSectionSubBar(
            title = item.name,
            subtitle = if (item.isSeries) "تفاصيل المسلسل والمواسم والحلقات" else "تفاصيل الفيلم والمشاهدة المباشرة",
            onBackClick = onBack,
            backButtonTestTag = "media_detail_back_button"
        )

        when {
            isLoading && detail == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = FoulPurpleBright)
                        Text(
                            text = "جاري تحميل التفاصيل...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FoulTextSecondary
                        )
                    }
                }
            }

            errorMessage != null && detail == null -> {
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
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FoulTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary)
                            ) {
                                Text("إعادة المحاولة", color = Color.White)
                            }
                            if (!item.isSeries) {
                                Button(
                                    onClick = onPlayMovie,
                                    colors = ButtonDefaults.buttonColors(containerColor = FoulLiveGreen)
                                ) {
                                    Text("تشغيل الفيلم مباشرة", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                val activeEpisodes = detail?.episodesBySeason?.get(selectedSeasonNumber).orEmpty()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Card with Poster & Metadata
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(FoulCardElevated)
                                            .border(1.dp, FoulDivider, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.posterUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = item.posterUrl,
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (item.isSeries) Icons.Default.Tv else Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = FoulPurpleBright,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )

                                        val genreText = detail?.genre.orEmpty()
                                        if (genreText.isNotBlank()) {
                                            Text(
                                                text = "التصنيف: $genreText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FoulTeamAwayCyan
                                            )
                                        }

                                        val releaseText = detail?.releaseDate?.ifBlank { item.releaseYear }.orEmpty()
                                        if (releaseText.isNotBlank()) {
                                            Text(
                                                text = "الإصدار: $releaseText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FoulTextSecondary
                                            )
                                        }

                                        val ratingText = detail?.rating?.ifBlank { item.rating }.orEmpty()
                                        if (ratingText.isNotBlank() && ratingText != "0") {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = FoulGoldStar,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = ratingText,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = FoulGoldStar,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        if (!item.isSeries) {
                                            Spacer(Modifier.height(4.dp))
                                            Button(
                                                onClick = onPlayMovie,
                                                colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("play_movie_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "مشاهدة الفيلم الآن",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }

                                val plot = detail?.description?.ifBlank { item.plotPreview }.orEmpty()
                                if (plot.isNotBlank()) {
                                    OrderedColorfulDivider(thickness = 1.dp)
                                    Text(
                                        text = plot,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FoulTextSecondary,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }

                    // Seasons & Episodes for Series
                    if (item.isSeries && detail != null) {
                        val seasons = detail.seasons
                        if (seasons.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "المواسم المتوفرة (${seasons.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(seasons, key = { it.seasonNumber }) { season ->
                                            val selected = season.seasonNumber == selectedSeasonNumber
                                            Surface(
                                                color = if (selected) FoulPurplePrimary else FoulCardDark,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (selected) FoulPurpleBright else FoulDivider
                                                ),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .tvFocusableClickable(
                                                        shape = RoundedCornerShape(10.dp),
                                                        onClick = { onSelectSeason(season.seasonNumber) }
                                                    )
                                                    .testTag("season_tab_${season.seasonNumber}")
                                            ) {
                                                Text(
                                                    text = season.name.ifBlank { "الموسم ${season.seasonNumber}" },
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White,
                                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (activeEpisodes.isNotEmpty()) {
                            items(activeEpisodes, key = { it.id }) { ep ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(12.dp),
                                            scaleOnFocus = 1.02f,
                                            onClick = { onPlayEpisode(ep) }
                                        )
                                        .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
                                        .testTag("episode_card_${ep.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(FoulPurpleDeep)
                                                    .border(1.dp, FoulPurpleBright, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${ep.episodeNum}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ep.title.ifBlank { "الحلقة ${ep.episodeNum}" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (ep.duration.isNotBlank()) {
                                                    Text(
                                                        text = "المدة: ${ep.duration}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = FoulTextSecondary
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            color = FoulPurpleBright,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "تشغيل الحلقة",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "تشغيل",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
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
    }
}

private val LibraryPosterShape = RoundedCornerShape(14.dp)

private val LibraryPosterFallbackBrush = Brush.verticalGradient(
    colors = listOf(FoulPurpleDeep, FoulCardDark, FoulBlackBg)
)

private val LibraryPosterOverlayBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.25f),
        Color.Transparent,
        Color.Black.copy(alpha = 0.55f),
        Color.Black.copy(alpha = 0.95f)
    )
)

private val LibraryPosterBorderBrush = Brush.verticalGradient(
    colors = listOf(
        FoulPurpleBright.copy(alpha = 0.45f),
        FoulDivider,
        FoulPurplePrimary.copy(alpha = 0.65f)
    )
)

@Composable
private fun LibraryPosterCard(
    item: LibraryMediaItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageRequest = remember(item.posterUrl) {
        if (item.posterUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(item.posterUrl)
                .size(240, 340)
                .allowHardware(true)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Card(
        shape = LibraryPosterShape,
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(206.dp)
            .clip(LibraryPosterShape)
            .tvFocusableClickable(
                shape = LibraryPosterShape,
                scaleOnFocus = 1.04f,
                onClick = onClick
            )
            .border(1.dp, LibraryPosterBorderBrush, LibraryPosterShape)
            .testTag("media_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LibraryPosterFallbackBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isSeries) Icons.Default.Tv else Icons.Default.Movie,
                        contentDescription = item.name,
                        tint = FoulPurpleBright,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Pre-allocated Gradient Overlay for crisp readability and zero scroll allocation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LibraryPosterOverlayBrush)
            )

            // Top Badges (Type & Rating)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (item.isSeries) FoulPurplePrimary.copy(alpha = 0.92f) else FoulBlackBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        0.7.dp,
                        if (item.isSeries) FoulPurpleBright else FoulDivider
                    )
                ) {
                    Text(
                        text = if (item.isSeries) "مسلسل" else "فيلم",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (item.rating.isNotBlank() && item.rating != "0" && item.rating != "0.0") {
                    Surface(
                        color = Color.Black.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, FoulGoldStar.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = FoulGoldStar,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = item.rating.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulGoldStar,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            // Bottom Title & Year Strip
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(FoulPurplePrimary.copy(alpha = 0.9f))
                            .border(0.8.dp, FoulPurpleBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (item.releaseYear.isNotBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = item.releaseYear.take(4),
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.5.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

