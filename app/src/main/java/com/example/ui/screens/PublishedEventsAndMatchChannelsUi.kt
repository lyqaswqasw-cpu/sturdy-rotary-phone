package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ChannelServer
import com.example.data.model.LeagueMatchesGroup
import com.example.data.model.LiveChannel
import com.example.data.model.LiveEventMatch
import com.example.data.model.MatchItem
import com.example.data.model.MatchPublishedChannel
import com.example.data.model.PublishedEventItem
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.components.tvFocusHighlight
import com.example.ui.components.tvFocusableClickable
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulTextSecondary
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Resolves all published `LiveChannel`s linked to a specific `MatchItem` in the Matches Schedule.
 * Matches by:
 * 1. Exact `matchId` (`item.matchId == match.id`)
 * 2. Team names (`item.homeTeamName` & `item.awayTeamName` matching `match.homeTeam.name` & `match.awayTeam.name`)
 * 3. Legacy `LiveEventMatch` entries (`v4_live_matches` / `v4_events`) if present.
 */
fun resolveChannelsForMatch(
    match: MatchItem,
    matchPublishedChannels: List<MatchPublishedChannel>,
    allChannels: List<LiveChannel>,
    liveEvents: List<LiveEventMatch> = emptyList()
): List<LiveChannel> {
    val result = mutableListOf<LiveChannel>()
    val seenIds = HashSet<String>()

    fun addChannelIfValid(ch: LiveChannel) {
        if (ch.servers.isNotEmpty() && seenIds.add(ch.id.ifBlank { ch.name })) {
            result.add(ch)
        }
    }

    val homeNorm = match.homeTeam.name.trim().lowercase()
    val awayNorm = match.awayTeam.name.trim().lowercase()

    for (entry in matchPublishedChannels) {
        val idMatches = entry.matchId > 0 && entry.matchId == match.id
        val eHome = entry.homeTeamName.trim().lowercase()
        val eAway = entry.awayTeamName.trim().lowercase()
        val namesMatch = eHome.isNotEmpty() && eAway.isNotEmpty() && (
            (eHome == homeNorm && eAway == awayNorm) ||
                (eHome == awayNorm && eAway == homeNorm) ||
                (homeNorm.contains(eHome) && awayNorm.contains(eAway)) ||
                (eHome.contains(homeNorm) && eAway.contains(awayNorm))
            )

        if (idMatches || namesMatch) {
            // 1. Resolve channels by ID from the latest app channels list
            for (cid in entry.channelIds) {
                allChannels.find { it.id == cid }?.let { addChannelIfValid(it) }
            }
            // 2. Also include embedded/custom channels published directly for this match
            for (customCh in entry.customChannels) {
                val updatedFromApp = allChannels.find { it.id == customCh.id }
                addChannelIfValid(updatedFromApp ?: customCh)
            }
        }
    }

    // Also check legacy liveEvents if any match
    for (ev in liveEvents) {
        val evHome = ev.homeTeam.trim().lowercase()
        val evAway = ev.awayTeam.trim().lowercase()
        if (evHome.isNotEmpty() && evAway.isNotEmpty() &&
            ((evHome == homeNorm && evAway == awayNorm) || (evHome == awayNorm && evAway == homeNorm))
        ) {
            if (ev.targetChannelId.isNotBlank()) {
                allChannels.find { it.id == ev.targetChannelId || it.name.equals(ev.broadcaster, ignoreCase = true) }
                    ?.let { addChannelIfValid(it) }
            }
        }
    }

    return result
}

/**
 * Resolves all `LiveChannel`s attached to a `PublishedEventItem`.
 */
fun resolveChannelsForPublishedEvent(
    event: PublishedEventItem,
    allChannels: List<LiveChannel>
): List<LiveChannel> {
    val result = mutableListOf<LiveChannel>()
    val seenIds = HashSet<String>()

    fun addChannelIfValid(ch: LiveChannel) {
        if (ch.servers.isNotEmpty() && seenIds.add(ch.id.ifBlank { ch.name })) {
            result.add(ch)
        }
    }

    for (cid in event.channelIds) {
        allChannels.find { it.id == cid }?.let { addChannelIfValid(it) }
    }
    for (customCh in event.customChannels) {
        val updatedFromApp = allChannels.find { it.id == customCh.id }
        addChannelIfValid(updatedFromApp ?: customCh)
    }
    return result
}

/**
 * Modal dialog displayed when a Match or Published Event has MORE THAN ONE channel,
 * allowing the user to pick which channel to watch immediately.
 */
@Composable
fun MatchAndEventChannelPickerDialog(
    title: String,
    subtitle: String = "اختر القناة الناقلة لبدء المشاهدة المباشرة",
    channels: List<LiveChannel>,
    onSelectChannel: (LiveChannel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(FoulPurpleBright, FoulDivider)),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("multi_channel_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(FoulPurpleDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = title.ifBlank { "القنوات الناقلة المتاحة" },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = FoulTextSecondary
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channels, key = { it.id.ifBlank { it.name } }) { channel ->
                        Surface(
                            color = FoulCardElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    scaleOnFocus = 1.02f,
                                    onClick = {
                                        onDismiss()
                                        onSelectChannel(channel)
                                    }
                                )
                                .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
                                .testTag("picker_channel_${channel.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(FoulPurpleDeep)
                                            .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (channel.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = channel.imageUrl,
                                                contentDescription = channel.name,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = channel.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (channel.servers.size > 1) {
                                                "${channel.servers.size} سيرفرات بث مباشر جاهزة"
                                            } else {
                                                "بث مباشر سريع • ${channel.servers.firstOrNull()?.name ?: "HD"}"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FoulTextSecondary
                                        )
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
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "شاهد الآن",
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

/**
 * Unified Single-Slot Auto-Advancing Carousel for Published Events ("تكون الأحداث بشكل في مكان واحد وبشكل تنقل كل ثلاث ثواني وبشكل مرتب جدا").
 * Displays all published events in a single sleek container on HomeScreen and automatically transitions every 3 seconds.
 */
@Composable
fun PublishedEventsAutoPagerCarousel(
    events: List<PublishedEventItem>,
    allChannels: List<LiveChannel>,
    onWatchEvent: (PublishedEventItem, List<LiveChannel>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { events.size }
    )

    val coroutineScope = rememberCoroutineScope()

    // Auto-advance every 3 seconds when there is more than 1 published event
    androidx.compose.runtime.LaunchedEffect(events.size, pagerState.settledPage) {
        if (events.size > 1) {
            kotlinx.coroutines.delay(3000L)
            val nextPage = (pagerState.currentPage + 1) % events.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("published_events_carousel_container"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                pageSpacing = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val eventItem = events[pageIndex]
                val resolvedChannels = remember(eventItem, allChannels) {
                    resolveChannelsForPublishedEvent(eventItem, allChannels)
                }
                PublishedEventShowcaseCard(
                    event = eventItem,
                    resolvedChannelsCount = resolvedChannels.size,
                    pageIndicatorText = if (events.size > 1) "${pageIndex + 1} / ${events.size}" else null,
                    onWatchNowClick = { onWatchEvent(eventItem, resolvedChannels) }
                )
            }
        }

        // Sleek Carousel Indicator Dots when multiple events exist
        if (events.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                events.forEachIndexed { idx, _ ->
                    val isSelected = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        colors = listOf(FoulPurpleBright, Color(0xFF38BDF8))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.28f),
                                            Color.White.copy(alpha = 0.28f)
                                        )
                                    )
                                }
                            )
                            .tvFocusableClickable(
                                shape = CircleShape,
                                scaleOnFocus = 1.2f,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(idx)
                                    }
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Showcase Card for a `PublishedEventItem` (Event / Match Announcement with Channels OR MP4/MKV Video).
 * Supports optional title, optional description, and optional image URL.
 */
@Composable
fun PublishedEventShowcaseCard(
    event: PublishedEventItem,
    resolvedChannelsCount: Int,
    pageIndicatorText: String? = null,
    onWatchNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVideoEvent = event.eventType.equals("VIDEO", ignoreCase = true) || event.videoUrl.isNotBlank()
    val hasImage = event.imageUrl.isNotBlank()
    val hasText = event.title.isNotBlank() || event.description.isNotBlank()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(18.dp),
                scaleOnFocus = 1.015f,
                onClick = onWatchNowClick
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(FoulPurpleBright, Color(0xFF38BDF8).copy(alpha = 0.65f), FoulDivider)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("published_event_card_${event.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (hasImage) 168.dp else 96.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FoulPurpleDeep,
                                Color(0xFF130B2E),
                                FoulBlackBg
                            )
                        )
                    )
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.title.ifBlank { "حدث مباشر" },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                }

                // Top Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isVideoEvent) FoulPurpleBright else FoulLiveRed,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVideoEvent) Icons.Default.Movie else Icons.Default.Sensors,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isVideoEvent) "فيديو مباشر" else "حدث مباشر",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isVideoEvent && resolvedChannelsCount > 0) {
                            Surface(
                                color = FoulBlackBg.copy(alpha = 0.78f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = if (resolvedChannelsCount == 1) "قناة ناقلة متاحة" else "$resolvedChannelsCount قنوات ناقلة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!pageIndicatorText.isNullOrBlank()) {
                            Surface(
                                color = FoulPurpleDeep.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(0.8.dp, FoulPurpleBright, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = pageIndicatorText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Overlay Row with Optional Text + Watch Now Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (hasText) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (event.title.isNotBlank()) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (event.description.isNotBlank()) {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (event.showWatchNow) {
                        Button(
                            onClick = onWatchNowClick,
                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .tvFocusHighlight(shape = RoundedCornerShape(10.dp))
                                .testTag("published_event_watch_now_${event.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "شاهد الآن",
                                style = MaterialTheme.typography.labelLarge,
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

/**
 * Developer Control Panel Tab: "نشر قناة لجدول المباريات"
 * Allows the developer to:
 * 1. Pick a match from the current Matches Schedule (`leagueGroups`) or enter Home/Away team names.
 * 2. Attach one or more channels from existing app channels (`allChannels`) OR create new custom channel(s)
 *    with servers (`m3u8` / `ts` / `mp4` / `mkv`) directly for the match.
 * 3. Publish online to the primary Firebase database (`foul-ddbb8`) so a "شاهد الآن" button appears
 *    on the match row in `MatchesScreen` and inside `MatchDetailScreen`.
 */
@Composable
fun DeveloperMatchChannelsTab(
    leagueGroups: List<LeagueMatchesGroup>,
    allChannels: List<LiveChannel>,
    matchPublishedChannels: List<MatchPublishedChannel>,
    isActionLoading: Boolean,
    onSaveMatchPublishedChannel: (MatchPublishedChannel, List<LiveChannel>) -> Unit,
    onDeleteMatchPublishedChannel: (String) -> Unit
) {
    val allAvailableMatches = remember(leagueGroups) {
        leagueGroups.flatMap { it.matches }
    }

    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var selectedMatchId by remember { mutableStateOf(0) }
    var homeTeamName by remember { mutableStateOf("") }
    var awayTeamName by remember { mutableStateOf("") }
    var competitionName by remember { mutableStateOf("") }
    var matchDate by remember { mutableStateOf("") }

    var matchSearchQuery by remember { mutableStateOf("") }
    var channelSearchQuery by remember { mutableStateOf("") }

    val selectedExistingChannelIds = remember { mutableStateListOf<String>() }
    val customMatchChannels = remember { mutableStateListOf<LiveChannel>() }

    // New custom channel form fields
    var newChannelName by remember { mutableStateOf("") }
    var newChannelLogoUrl by remember { mutableStateOf("") }
    var newServerName by remember { mutableStateOf("FHD") }
    var newServerUrl by remember { mutableStateOf("") }
    var alsoPublishToMainChannels by remember { mutableStateOf(true) }

    val filteredScheduleMatches = remember(allAvailableMatches, matchSearchQuery) {
        val q = matchSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            allAvailableMatches
        } else {
            allAvailableMatches.filter { m ->
                m.homeTeam.name.lowercase().contains(q) ||
                    m.awayTeam.name.lowercase().contains(q) ||
                    m.competitionName.lowercase().contains(q)
            }
        }
    }

    val filteredAppChannels = remember(allChannels, channelSearchQuery) {
        val q = channelSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            allChannels
        } else {
            allChannels.filter { ch ->
                ch.name.lowercase().contains(q) || ch.category.lowercase().contains(q)
            }
        }
    }

    fun resetForm() {
        editingEntryId = null
        selectedMatchId = 0
        homeTeamName = ""
        awayTeamName = ""
        competitionName = ""
        matchDate = ""
        selectedExistingChannelIds.clear()
        customMatchChannels.clear()
        newChannelName = ""
        newChannelLogoUrl = ""
        newServerName = "FHD"
        newServerUrl = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dev_match_channels_tab_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Match Picker Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = FoulPurpleBright
                        )
                        Column {
                            Text(
                                text = if (editingEntryId != null) {
                                    "تعديل القنوات الناقلة للمباراة"
                                } else {
                                    "نشر قناة لجدول المباريات (Online)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "اختر مباراة من الجدول أو اكتب اسم الفريقين وأضف قناة أو أكثر ليظهر زر (شاهد الآن) في جدول المباريات وداخل تفاصيل المباراة",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    // Quick Match Picker from Schedule
                    if (allAvailableMatches.isNotEmpty()) {
                        Text(
                            text = "1. اختر المباراة مباشرة من جدول المباريات الحالي (${allAvailableMatches.size} مباراة):",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = matchSearchQuery,
                            onValueChange = { matchSearchQuery = it },
                            label = { Text("بحث في مباريات الجدول (اسم الفريق أو البطولة)") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = FoulTextSecondary)
                            },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredScheduleMatches, key = { it.id }) { match ->
                                val isSelected = selectedMatchId == match.id ||
                                    (homeTeamName == match.homeTeam.name && awayTeamName == match.awayTeam.name)
                                Surface(
                                    color = if (isSelected) FoulPurpleDeep else FoulCardElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedMatchId = match.id
                                            homeTeamName = match.homeTeam.name
                                            awayTeamName = match.awayTeam.name
                                            competitionName = match.competitionName
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) FoulPurpleBright else FoulDivider,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${match.homeTeam.name} × ${match.awayTeam.name}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${match.competitionName} • ${match.displayTime.ifBlank { match.statusText }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FoulTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Manual / Editable Team Names
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = homeTeamName,
                            onValueChange = { homeTeamName = it },
                            label = { Text("الفريق الأول (المستضيف)") },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_match_home_team_input")
                        )
                        OutlinedTextField(
                            value = awayTeamName,
                            onValueChange = { awayTeamName = it },
                            label = { Text("الفريق الثاني (الضيف)") },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_match_away_team_input")
                        )
                    }

                    OutlinedTextField(
                        value = competitionName,
                        onValueChange = { competitionName = it },
                        label = { Text("اسم البطولة (اختياري)") },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. Select from Existing Channels in App
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. اختيار من القنوات الموجودة في التطبيق (${selectedExistingChannelIds.size} محددة):",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    OutlinedTextField(
                        value = channelSearchQuery,
                        onValueChange = { channelSearchQuery = it },
                        label = { Text("ابحث في القنوات الموجودة في التطبيق...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = FoulTextSecondary)
                        },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (filteredAppChannels.isEmpty()) {
                        Text(
                            text = "لا توجد قنوات مطابقة في التطبيق حالياً، يمكنك إضافة قناة جديدة بالأسفل.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredAppChannels, key = { it.id }) { ch ->
                                val isChecked = selectedExistingChannelIds.contains(ch.id)
                                Surface(
                                    color = if (isChecked) FoulPurpleDeep else FoulCardElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (isChecked) {
                                                selectedExistingChannelIds.remove(ch.id)
                                            } else {
                                                selectedExistingChannelIds.add(ch.id)
                                            }
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = if (isChecked) FoulLiveGreen else FoulDivider,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                            contentDescription = null,
                                            tint = if (isChecked) FoulLiveGreen else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = ch.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (ch.category.isNotBlank()) {
                                                Text(
                                                    text = ch.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = FoulTextSecondary
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

        // 3. Or Add a Brand-New Channel Directly for the Match
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3. أو إضافة قناة جديدة مباشرة للمباراة:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newChannelName,
                            onValueChange = { newChannelName = it },
                            label = { Text("اسم القناة الناقلة (مثال: beIN 1)") },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newServerName,
                            onValueChange = { newServerName = it },
                            label = { Text("اسم السيرفر/الجودة") },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    OutlinedTextField(
                        value = newServerUrl,
                        onValueChange = { newServerUrl = it },
                        label = { Text("رابط البث المباشر (m3u8 / ts / mp4 / mkv)") },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newChannelLogoUrl,
                        onValueChange = { newChannelLogoUrl = it },
                        label = { Text("رابط شعار القناة (اختياري)") },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = alsoPublishToMainChannels,
                                onCheckedChange = { alsoPublishToMainChannels = it },
                                colors = CheckboxDefaults.colors(checkedColor = FoulPurpleBright)
                            )
                            Text(
                                text = "حفظ القناة أيضاً ضمن قنوات التطبيق",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                val cleanUrl = newServerUrl.trim()
                                if (cleanUrl.isNotEmpty()) {
                                    val chName = newChannelName.trim().ifBlank { "قناة المباراة ${customMatchChannels.size + 1}" }
                                    val streamType = when {
                                        cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                                        cleanUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                                        cleanUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                                        else -> "m3u8"
                                    }
                                    val newCh = LiveChannel(
                                        id = "match_custom_${UUID.randomUUID().toString().take(8)}",
                                        name = chName,
                                        category = competitionName.ifBlank { "قنوات المباريات" },
                                        branchId = if (alsoPublishToMainChannels) "match_channels" else "",
                                        imageUrl = newChannelLogoUrl.trim(),
                                        servers = listOf(
                                            ChannelServer(
                                                id = "srv_1",
                                                name = newServerName.trim().ifBlank { "HD" },
                                                streamType = streamType,
                                                streamUrl = cleanUrl
                                            )
                                        )
                                    )
                                    customMatchChannels.add(newCh)
                                    newChannelName = ""
                                    newServerUrl = ""
                                    newChannelLogoUrl = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إضافة للقائمة", color = Color.White)
                        }
                    }

                    // Show currently attached custom channels
                    if (customMatchChannels.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "القنوات المخصصة المضافة لهذه المباراة (${customMatchChannels.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                color = FoulLiveGreen
                            )
                            customMatchChannels.forEachIndexed { idx, ch ->
                                Surface(
                                    color = FoulCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${ch.name} (${ch.servers.firstOrNull()?.streamUrl.orEmpty()})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { customMatchChannels.removeAt(idx) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = FoulLiveRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Publish Match Channels Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // If developer filled the custom channel fields without clicking "إضافة للقائمة", include it automatically
                                val finalCustomList = customMatchChannels.toMutableList()
                                if (newServerUrl.trim().isNotEmpty()) {
                                    val cleanUrl = newServerUrl.trim()
                                    val chName = newChannelName.trim().ifBlank { "قناة المباراة" }
                                    val streamType = when {
                                        cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                                        cleanUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                                        cleanUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                                        else -> "m3u8"
                                    }
                                    finalCustomList.add(
                                        LiveChannel(
                                            id = "match_custom_${UUID.randomUUID().toString().take(8)}",
                                            name = chName,
                                            category = competitionName.ifBlank { "قنوات المباريات" },
                                            branchId = if (alsoPublishToMainChannels) "match_channels" else "",
                                            imageUrl = newChannelLogoUrl.trim(),
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "srv_1",
                                                    name = newServerName.trim().ifBlank { "HD" },
                                                    streamType = streamType,
                                                    streamUrl = cleanUrl
                                                )
                                            )
                                        )
                                    )
                                }

                                // Also embed snapshots of selected existing channels so they always resolve online
                                val selectedExistingObjects = allChannels.filter { selectedExistingChannelIds.contains(it.id) }
                                val combinedCustom = (selectedExistingObjects + finalCustomList).distinctBy { it.id }

                                val entryId = editingEntryId
                                    ?: if (selectedMatchId > 0) "match_$selectedMatchId" else "match_${UUID.randomUUID().toString().take(8)}"

                                val item = MatchPublishedChannel(
                                    id = entryId,
                                    matchId = selectedMatchId,
                                    homeTeamName = homeTeamName.trim(),
                                    awayTeamName = awayTeamName.trim(),
                                    competitionName = competitionName.trim(),
                                    matchDate = matchDate.trim(),
                                    channelIds = selectedExistingChannelIds.toList(),
                                    customChannels = combinedCustom,
                                    createdAt = System.currentTimeMillis()
                                )

                                val channelsToSaveInMainApp = if (alsoPublishToMainChannels) {
                                    finalCustomList
                                } else {
                                    emptyList()
                                }

                                onSaveMatchPublishedChannel(item, channelsToSaveInMainApp)
                                resetForm()
                            },
                            enabled = !isActionLoading &&
                                (homeTeamName.isNotBlank() || awayTeamName.isNotBlank() || selectedMatchId > 0) &&
                                (selectedExistingChannelIds.isNotEmpty() || customMatchChannels.isNotEmpty() || newServerUrl.isNotBlank()),
                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_publish_match_channel_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (editingEntryId != null) "حفظ تعديل قنوات المباراة (Online)" else "نشر القناة لجدول المباريات (Online)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (editingEntryId != null) {
                            OutlinedButton(
                                onClick = { resetForm() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إلغاء", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 4. List of Currently Published Match Channels
        item {
            Text(
                text = "القنوات المنشورة لجدول المباريات حالياً (${matchPublishedChannels.size}):",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(matchPublishedChannels, key = { it.id }) { item ->
            val resolvedCount = (item.channelIds + item.customChannels.map { it.id }).distinct().size
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${item.homeTeamName} × ${item.awayTeamName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = buildString {
                                if (item.competitionName.isNotBlank()) append("${item.competitionName} • ")
                                append("$resolvedCount قناة ناقلة منشورة")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulLiveGreen
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                editingEntryId = item.id
                                selectedMatchId = item.matchId
                                homeTeamName = item.homeTeamName
                                awayTeamName = item.awayTeamName
                                competitionName = item.competitionName
                                matchDate = item.matchDate
                                selectedExistingChannelIds.clear()
                                selectedExistingChannelIds.addAll(item.channelIds)
                                customMatchChannels.clear()
                                customMatchChannels.addAll(
                                    item.customChannels.filter { ch -> !item.channelIds.contains(ch.id) }
                                )
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White)
                        }
                        IconButton(
                            onClick = { onDeleteMatchPublishedChannel(item.id) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = FoulLiveRed)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Developer Control Panel Tab: "نشر حدث"
 * Allows the developer to publish an Event or Announcement online (`v4_published_events` in `foul-ddbb8`):
 * - Mode 1 ("CHANNELS"): Publish an event (e.g., "مباراة برشلونة ضد ريال مدريد") with optional title/text,
 *   optional image URL, "شاهد الآن" button, and 1 or multiple channels (from existing channels or custom channels).
 *   When the user clicks "شاهد الآن": if 1 channel -> opens and plays directly; if 2+ channels -> shows the channel picker!
 * - Mode 2 ("VIDEO"): Publish a direct video event (`mp4` or `mkv`) with optional text and optional image URL
 *   that plays smoothly in the app's video player.
 */
@Composable
fun DeveloperPublishEventTab(
    allChannels: List<LiveChannel>,
    publishedEvents: List<PublishedEventItem>,
    isActionLoading: Boolean,
    onSavePublishedEvent: (PublishedEventItem) -> Unit,
    onDeletePublishedEvent: (String) -> Unit
) {
    var editingEventId by remember { mutableStateOf<String?>(null) }
    var eventType by remember { mutableStateOf("CHANNELS") } // "CHANNELS" or "VIDEO"
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var showWatchNow by remember { mutableStateOf(true) }

    var channelSearchQuery by remember { mutableStateOf("") }
    val selectedChannelIds = remember { mutableStateListOf<String>() }
    val customEventChannels = remember { mutableStateListOf<LiveChannel>() }

    var customChName by remember { mutableStateOf("") }
    var customChStreamUrl by remember { mutableStateOf("") }

    val filteredChannels = remember(allChannels, channelSearchQuery) {
        val q = channelSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            allChannels
        } else {
            allChannels.filter {
                it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
            }
        }
    }

    fun resetForm() {
        editingEventId = null
        title = ""
        description = ""
        imageUrl = ""
        videoUrl = ""
        showWatchNow = true
        selectedChannelIds.clear()
        customEventChannels.clear()
        customChName = ""
        customChStreamUrl = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dev_publish_event_tab_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = FoulPurpleBright
                        )
                        Column {
                            Text(
                                text = if (editingEventId != null) "تعديل الحدث المنشور" else "نشر حدث أو إعلان مباشر (Online)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "انشر حدثاً بقناة أو عدة قنوات ناقلة، أو انشر فيديو مباشر (MP4 / MKV) مع نص وصورة اختياريين",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    // Mode Selector: CHANNELS vs VIDEO (MP4 / MKV)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isChannelsMode = eventType == "CHANNELS"
                        Button(
                            onClick = { eventType = "CHANNELS" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isChannelsMode) FoulPurpleBright else FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_event_mode_channels_button")
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("حدث بقنوات مباشرة", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { eventType = "VIDEO" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isChannelsMode) FoulPurpleBright else FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_event_mode_video_button")
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("فيديو MP4 / MKV", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Optional Title / Event Name
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = {
                            Text(
                                if (eventType == "VIDEO") {
                                    "عنوان / نص الفيديو (اختياري)"
                                } else {
                                    "اسم الحدث (اختياري - مثال: مباراة برشلونة ضد ريال مدريد)"
                                }
                            )
                        },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dev_event_title_input")
                    )

                    // Optional Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("نص إضافي أو تفاصيل الحدث (اختياري)") },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional Image URL
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("رابط الصورة (اختياري)") },
                        singleLine = true,
                        colors = devTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dev_event_image_input")
                    )

                    if (eventType == "VIDEO") {
                        // Video URL (MP4 / MKV)
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("رابط الفيديو المباشر (MP4 أو MKV)") },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dev_event_video_url_input")
                        )
                    } else {
                        // Channels Selection for the Event (1 or multiple channels)
                        Text(
                            text = "اختر القنوات الناقلة للحدث (${selectedChannelIds.size + customEventChannels.size} قناة مضافة):",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• إذا أضفت قناة واحدة: تفتح وتعمل مباشرة عند ضغط (شاهد الآن).\n• إذا أضفت أكثر من قناة: تظهر قائمة القنوات للمستخدم ليختار منها عند ضغط (شاهد الآن).",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )

                        OutlinedTextField(
                            value = channelSearchQuery,
                            onValueChange = { channelSearchQuery = it },
                            label = { Text("بحث في القنوات المنشورة في التطبيق...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = FoulTextSecondary)
                            },
                            singleLine = true,
                            colors = devTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (filteredChannels.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(filteredChannels, key = { it.id }) { ch ->
                                    val isSelected = selectedChannelIds.contains(ch.id)
                                    Surface(
                                        color = if (isSelected) FoulPurpleDeep else FoulCardElevated,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isSelected) {
                                                    selectedChannelIds.remove(ch.id)
                                                } else {
                                                    selectedChannelIds.add(ch.id)
                                                }
                                            }
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) FoulLiveGreen else FoulDivider,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = if (isSelected) FoulLiveGreen else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = ch.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Optional direct custom channel for the event
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customChName,
                                onValueChange = { customChName = it },
                                label = { Text("اسم قناة إضافية") },
                                singleLine = true,
                                colors = devTextFieldColors(),
                                modifier = Modifier.weight(0.4f)
                            )
                            OutlinedTextField(
                                value = customChStreamUrl,
                                onValueChange = { customChStreamUrl = it },
                                label = { Text("رابط بث القناة (m3u8/mp4/mkv)") },
                                singleLine = true,
                                colors = devTextFieldColors(),
                                modifier = Modifier.weight(0.6f)
                            )
                        }

                        Button(
                            onClick = {
                                val cleanUrl = customChStreamUrl.trim()
                                if (cleanUrl.isNotEmpty()) {
                                    val sType = when {
                                        cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                                        cleanUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                                        cleanUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                                        else -> "m3u8"
                                    }
                                    customEventChannels.add(
                                        LiveChannel(
                                            id = "ev_ch_${UUID.randomUUID().toString().take(8)}",
                                            name = customChName.trim().ifBlank { "قناة الحدث ${customEventChannels.size + 1}" },
                                            category = "أحداث مباشرة",
                                            imageUrl = imageUrl.trim(),
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s1",
                                                    name = "HD",
                                                    streamType = sType,
                                                    streamUrl = cleanUrl
                                                )
                                            )
                                        )
                                    )
                                    customChName = ""
                                    customChStreamUrl = ""
                                }
                            },
                            enabled = customChStreamUrl.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إضافة قناة مخصصة للحدث", color = Color.White)
                        }

                        if (customEventChannels.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                customEventChannels.forEachIndexed { idx, ch ->
                                    Surface(
                                        color = FoulCardElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = ch.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                            IconButton(
                                                onClick = { customEventChannels.removeAt(idx) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = FoulLiveRed, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Show Watch Now Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "إظهار زر (شاهد الآن) على بطاقة الحدث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = showWatchNow,
                            onCheckedChange = { showWatchNow = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FoulPurpleBright
                            )
                        )
                    }

                    // Publish Event Button
                    val canPublish = if (eventType == "VIDEO") {
                        videoUrl.trim().isNotEmpty()
                    } else {
                        selectedChannelIds.isNotEmpty() ||
                            customEventChannels.isNotEmpty() ||
                            customChStreamUrl.trim().isNotEmpty() ||
                            imageUrl.trim().isNotEmpty() ||
                            title.trim().isNotEmpty()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val finalCustom = customEventChannels.toMutableList()
                                if (eventType == "CHANNELS" && customChStreamUrl.trim().isNotEmpty()) {
                                    val cleanUrl = customChStreamUrl.trim()
                                    val sType = when {
                                        cleanUrl.endsWith(".ts", ignoreCase = true) -> "ts"
                                        cleanUrl.endsWith(".mp4", ignoreCase = true) -> "mp4"
                                        cleanUrl.endsWith(".mkv", ignoreCase = true) -> "mkv"
                                        else -> "m3u8"
                                    }
                                    finalCustom.add(
                                        LiveChannel(
                                            id = "ev_ch_${UUID.randomUUID().toString().take(8)}",
                                            name = customChName.trim().ifBlank { "قناة الحدث" },
                                            category = "أحداث مباشرة",
                                            imageUrl = imageUrl.trim(),
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s1",
                                                    name = "HD",
                                                    streamType = sType,
                                                    streamUrl = cleanUrl
                                                )
                                            )
                                        )
                                    )
                                }

                                val selectedObjs = allChannels.filter { selectedChannelIds.contains(it.id) }
                                val combinedChannels = (selectedObjs + finalCustom).distinctBy { it.id }

                                val eventItem = PublishedEventItem(
                                    id = editingEventId ?: "event_${UUID.randomUUID().toString().take(8)}",
                                    eventType = eventType,
                                    title = title.trim(),
                                    description = description.trim(),
                                    imageUrl = imageUrl.trim(),
                                    showWatchNow = showWatchNow,
                                    channelIds = selectedChannelIds.toList(),
                                    customChannels = combinedChannels,
                                    videoUrl = if (eventType == "VIDEO") videoUrl.trim() else "",
                                    createdAt = System.currentTimeMillis()
                                )
                                onSavePublishedEvent(eventItem)
                                resetForm()
                            },
                            enabled = !isActionLoading && canPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_publish_event_submit_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (editingEventId != null) "حفظ تعديل الحدث (Online)" else "نشر الحدث الآن (Online)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (editingEventId != null) {
                            OutlinedButton(
                                onClick = { resetForm() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إلغاء", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // List of Published Events
        item {
            Text(
                text = "الأحداث المنشورة حالياً (${publishedEvents.size}):",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(publishedEvents, key = { it.id }) { ev ->
            val isVid = ev.eventType.equals("VIDEO", ignoreCase = true) || ev.videoUrl.isNotBlank()
            val chCount = (ev.channelIds + ev.customChannels.map { it.id }).distinct().size
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = ev.title.ifBlank {
                                if (isVid) "حدث فيديو مباشر (بدون نص)" else "حدث مباشر منشور (بدون نص)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isVid) {
                                "فيديو مباشر (MP4/MKV) • ${ev.videoUrl}"
                            } else {
                                "حدث بقنوات مباشرة • $chCount قناة ناقلة"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulLiveGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                editingEventId = ev.id
                                eventType = ev.eventType
                                title = ev.title
                                description = ev.description
                                imageUrl = ev.imageUrl
                                videoUrl = ev.videoUrl
                                showWatchNow = ev.showWatchNow
                                selectedChannelIds.clear()
                                selectedChannelIds.addAll(ev.channelIds)
                                customEventChannels.clear()
                                customEventChannels.addAll(
                                    ev.customChannels.filter { ch -> !ev.channelIds.contains(ch.id) }
                                )
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White)
                        }
                        IconButton(
                            onClick = { onDeletePublishedEvent(ev.id) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = FoulLiveRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun devTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FoulPurpleBright,
    unfocusedBorderColor = FoulDivider,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = FoulTextSecondary,
    cursorColor = FoulPurpleBright
)
