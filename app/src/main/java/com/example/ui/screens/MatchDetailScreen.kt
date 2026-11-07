package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LineupPlayer
import com.example.data.model.LiveChannel
import com.example.data.model.MatchDetailData
import com.example.data.model.MatchEventItem
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatComparison
import com.example.data.model.NewsArticleItem
import com.example.data.model.StandingRowItem
import com.example.data.model.TeamLineup
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.mutableStateOf
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.components.tvFocusableClickable
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPitchGreenDark
import com.example.ui.theme.FoulPitchGreenLight
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTeamHomeIndigo
import com.example.ui.theme.FoulTextMuted
import com.example.ui.theme.FoulTextSecondary

private val MATCH_DETAIL_TABS = listOf(
    "الأحداث",     // 0: Events FIRST
    "التشكيلة",    // 1: Real Pitch + Lineup + Player Photos
    "الإحصائيات",  // 2: Possession FIRST + Other Stats
    "الترتيب",     // 3: Full Standings with both teams & league colored
    "الأخبار"      // 4: Real News
)

@Composable
fun MatchDetailScreen(
    match: MatchItem,
    isLoading: Boolean,
    error: String?,
    detail: MatchDetailData?,
    publishedChannels: List<LiveChannel> = emptyList(),
    telegramUrl: String = "https://t.me/jdj_q",
    onWatchChannel: (LiveChannel) -> Unit = {},
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPlayerClick: (LineupPlayer, Int, String, String) -> Unit = { _, _, _, _ -> }
) {
    BackHandler { onBack() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isChannelPickerOpen by remember { mutableStateOf(false) }

    if (isChannelPickerOpen && publishedChannels.isNotEmpty()) {
        MatchAndEventChannelPickerDialog(
            title = "${match.homeTeam.name} ضد ${match.awayTeam.name}",
            subtitle = "اختر القناة الناقلة للمباراة لبدء المشاهدة المباشرة",
            channels = publishedChannels,
            onSelectChannel = { ch ->
                isChannelPickerOpen = false
                onWatchChannel(ch)
            },
            onDismiss = { isChannelPickerOpen = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
    ) {
        // 1. Smart Top Bar: Strictly Telegram Icon + FOUL TV Name + App Logo Image
        com.example.ui.components.FoulSmartTopBar(
            telegramUrl = telegramUrl,
            telegramTestTag = "match_detail_telegram_button"
        )

        // 2. Contextual Sub-Header Bar Directly Underneath Top Bar (Back Button + Competition Title + Refresh)
        com.example.ui.components.FoulSectionSubBar(
            title = match.competitionName,
            subtitle = if (match.roundName.isNotBlank()) match.roundName else "${match.homeTeam.name} ضد ${match.awayTeam.name}",
            onBackClick = onBack,
            backButtonTestTag = "match_detail_back_button",
            actions = {
                CompactTopBarIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "تحديث تفاصيل المباراة",
                    onClick = onRetry
                )
            }
        )

        // Ultra-Clean Broadcast Scoreboard Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FoulBottomBarBlack,
                            FoulPurpleDeep.copy(alpha = 0.55f),
                            FoulCardDark
                        )
                    )
                )
        ) {
            // Stadium Scoreboard Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team (Right in RTL)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(FoulCardElevated)
                            .border(1.5.dp, FoulTeamHomeIndigo, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = match.homeTeam.logoUrl,
                            contentDescription = match.homeTeam.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Center Score & Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    if (match.homeTeam.hasScore && match.awayTeam.hasScore) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${match.homeTeam.score}",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.headlineMedium,
                                color = FoulTextMuted
                            )
                            Text(
                                text = "${match.awayTeam.score}",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = match.displayTime,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Surface(
                        color = if (match.isLive) FoulLiveGreen.copy(alpha = 0.2f) else FoulCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(
                            1.dp,
                            if (match.isLive) FoulLiveGreen else FoulDivider,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            text = if (match.isLive && match.gameTimeDisplay.isNotEmpty()) {
                                "مباشر • ${match.gameTimeDisplay}"
                            } else {
                                match.statusText
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (match.isLive) FoulLiveGreen else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    if (!detail?.venueName.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stadium,
                                contentDescription = null,
                                tint = FoulTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = detail?.venueName.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    if (publishedChannels.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FoulPurpleBright,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(10.dp),
                                    scaleOnFocus = 1.05f,
                                    onClick = {
                                        if (publishedChannels.size == 1) {
                                            onWatchChannel(publishedChannels.first())
                                        } else {
                                            isChannelPickerOpen = true
                                        }
                                    }
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                .testTag("match_detail_watch_now_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "شاهد الآن",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (publishedChannels.size > 1) {
                                        "شاهد الآن (${publishedChannels.size} قنوات)"
                                    } else {
                                        "شاهد الآن"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Away Team (Left in RTL)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(FoulCardElevated)
                            .border(1.5.dp, FoulTeamAwayCyan, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = match.awayTeam.logoUrl,
                            contentDescription = match.awayTeam.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            // Sleek Segmented Tab Bar (All White Text + Crisp Active Underline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FoulBottomBarBlack)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MATCH_DETAIL_TABS.forEachIndexed { idx, title ->
                    val selected = selectedTabIndex == idx
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .tvFocusableClickable(
                                shape = RoundedCornerShape(8.dp),
                                scaleOnFocus = 1.03f,
                                onClick = { selectedTabIndex = idx }
                            )
                            .padding(top = 10.dp)
                            .testTag("match_detail_tab_$idx"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else FoulTextMuted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (selected) Color.White else Color.Transparent)
                        )
                    }
                }
            }
            OrderedColorfulDivider(thickness = 1.2.dp)
        }

        // Tab Body
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp)
                        Text(
                            text = "جاري جلب تفاصيل المباراة...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FoulTextSecondary
                        )
                    }
                }
            }

            error != null && detail == null -> {
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
                        Text(text = error, color = Color.White)
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated)
                        ) {
                            Text("إعادة المحاولة", color = Color.White)
                        }
                    }
                }
            }

            detail != null -> {
                when (selectedTabIndex) {
                    0 -> MatchEventsSection(
                        homeTeamName = match.homeTeam.name,
                        awayTeamName = match.awayTeam.name,
                        events = detail.events
                    )
                    1 -> MatchLineupsPitchSection(
                        match = match,
                        homeLineup = detail.homeLineup,
                        awayLineup = detail.awayLineup,
                        onPlayerClick = onPlayerClick
                    )
                    2 -> MatchStatisticsSection(
                        homeTeamName = match.homeTeam.name,
                        awayTeamName = match.awayTeam.name,
                        stats = detail.stats
                    )
                    3 -> MatchStandingsFullSection(
                        leagueName = detail.standingsLeagueName,
                        leagueLogoUrl = match.competitionLogoUrl,
                        homeTeamName = match.homeTeam.name,
                        awayTeamName = match.awayTeam.name,
                        rows = detail.standings
                    )
                    4 -> MatchNewsSection(news = detail.news)
                }
            }
        }
    }
}

/**
 * Tab 0: Events ("عرض الأحداث أول شيء بين الفريقين بشكل منفصل ليكون مرتب")
 */
@Composable
private fun MatchEventsSection(
    homeTeamName: String,
    awayTeamName: String,
    events: List<MatchEventItem>
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد أحداث مسجلة لهذه المباراة حتى الآن",
                style = MaterialTheme.typography.titleMedium,
                color = FoulTextSecondary
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_events_list")
    ) {
        // Separated Team Headers
        item {
            Surface(
                color = FoulCardElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FoulTeamHomeIndigo)
                        )
                        Text(
                            text = homeTeamName,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "الوقت",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextMuted
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = awayTeamName,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FoulTeamAwayCyan)
                        )
                    }
                }
            }
        }

        items(events, key = { it.id }) { ev ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team Column (Right in RTL)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (ev.isHome) {
                        TeamEventBubble(event = ev, isHome = true)
                    }
                }

                // Center Timeline Minute Pill
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FoulCardElevated)
                        .border(1.dp, FoulDivider, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ev.minuteDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Away Team Column (Left in RTL)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (!ev.isHome) {
                        TeamEventBubble(event = ev, isHome = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamEventBubble(
    event: MatchEventItem,
    isHome: Boolean
) {
    val badgeColor = when {
        event.typeName.contains("هدف") || event.typeId == 1 -> FoulLiveGreen
        event.typeName.contains("حمراء") || event.typeId == 3 -> FoulLiveRed
        event.typeName.contains("صفراء") || event.typeId == 2 -> FoulGoldStar
        else -> if (isHome) FoulTeamHomeIndigo else FoulTeamAwayCyan
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FoulDivider, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Text(
                    text = event.typeName.ifBlank { "حدث" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (event.playerName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (event.playerPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = event.playerPhotoUrl,
                            contentDescription = event.playerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(FoulCardElevated)
                        )
                    }
                    Text(
                        text = event.playerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (event.secondaryPlayerName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = FoulTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = event.secondaryPlayerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Tab 1: Lineups ("قسم التشكيلة بشكل ملعب مباراة حقيقي وفوقه التشكيلة وأسماء اللاعبين وصورة الاعبين")
 */
@Composable
private fun MatchLineupsPitchSection(
    match: MatchItem,
    homeLineup: TeamLineup?,
    awayLineup: TeamLineup?,
    onPlayerClick: (LineupPlayer, Int, String, String) -> Unit
) {
    if (homeLineup == null && awayLineup == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "التشكيلة الرسمية غير متوفرة لهذه المباراة بعد",
                style = MaterialTheme.typography.titleMedium,
                color = FoulTextSecondary
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_lineups_pitch")
    ) {
        // Formation Header Banner directly above Pitch
        item {
            Surface(
                color = FoulCardElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = homeLineup?.teamName.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الخطة: ${homeLineup?.formation ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }

                    Surface(
                        color = FoulCardDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, FoulDivider, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "اضغط على أي لاعب لعرض المسيرة والإحصائيات",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulGoldStar,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = awayLineup?.teamName.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الخطة: ${awayLineup?.formation ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                }
            }
        }

        // Authentic Football Pitch Graphic (Split cleanly into two halves)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(720.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val stripeCount = 12
                        val stripeHeight = h / stripeCount
                        for (i in 0 until stripeCount) {
                            drawRect(
                                color = if (i % 2 == 0) FoulPitchGreenDark else FoulPitchGreenLight,
                                topLeft = Offset(0f, i * stripeHeight),
                                size = Size(w, stripeHeight)
                            )
                        }

                        val lineStroke = Stroke(width = 1.8.dp.toPx())
                        val lineColor = Color.White.copy(alpha = 0.55f)
                        val pad = 10.dp.toPx()

                        // Outer Pitch Boundary
                        drawRect(
                            color = lineColor,
                            topLeft = Offset(pad, pad),
                            size = Size(w - pad * 2, h - pad * 2),
                            style = lineStroke
                        )

                        // Halfway Line
                        drawLine(
                            color = lineColor,
                            start = Offset(pad, h / 2f),
                            end = Offset(w - pad, h / 2f),
                            strokeWidth = 1.8.dp.toPx()
                        )

                        // Center Circle & Spot
                        drawCircle(
                            color = lineColor,
                            radius = 44.dp.toPx(),
                            center = Offset(w / 2f, h / 2f),
                            style = lineStroke
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(w / 2f, h / 2f)
                        )

                        // Top Penalty Area & Goal Box
                        val boxW = w * 0.54f
                        val boxH = h * 0.13f
                        drawRect(
                            color = lineColor,
                            topLeft = Offset((w - boxW) / 2f, pad),
                            size = Size(boxW, boxH),
                            style = lineStroke
                        )
                        val smallBoxW = w * 0.26f
                        val smallBoxH = h * 0.05f
                        drawRect(
                            color = lineColor,
                            topLeft = Offset((w - smallBoxW) / 2f, pad),
                            size = Size(smallBoxW, smallBoxH),
                            style = lineStroke
                        )

                        // Bottom Penalty Area & Goal Box
                        drawRect(
                            color = lineColor,
                            topLeft = Offset((w - boxW) / 2f, h - pad - boxH),
                            size = Size(boxW, boxH),
                            style = lineStroke
                        )
                        drawRect(
                            color = lineColor,
                            topLeft = Offset((w - smallBoxW) / 2f, h - pad - smallBoxH),
                            size = Size(smallBoxW, smallBoxH),
                            style = lineStroke
                        )
                    }

                    // Top Half = Home Team, Bottom Half = Away Team
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            TeamHalfPitchPlayers(
                                players = homeLineup?.startingPlayers.orEmpty(),
                                isTopHalf = true,
                                badgeColor = FoulTeamHomeIndigo,
                                onPlayerClick = { player ->
                                    onPlayerClick(
                                        player,
                                        match.homeTeam.id,
                                        match.homeTeam.name,
                                        match.homeTeam.logoUrl
                                    )
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            TeamHalfPitchPlayers(
                                players = awayLineup?.startingPlayers.orEmpty(),
                                isTopHalf = false,
                                badgeColor = FoulTeamAwayCyan,
                                onPlayerClick = { player ->
                                    onPlayerClick(
                                        player,
                                        match.awayTeam.id,
                                        match.awayTeam.name,
                                        match.awayTeam.logoUrl
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Substitutes Table
        if (!homeLineup?.substitutes.isNullOrEmpty() || !awayLineup?.substitutes.isNullOrEmpty()) {
            item {
                Text(
                    text = "مقاعد البدلاء (اضغط على اللاعب لعرض مسيرته وإحصائياته)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = homeLineup?.teamName.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        homeLineup?.substitutes.orEmpty().forEach { sub ->
                            SubPlayerCard(
                                player = sub,
                                accentColor = FoulTeamHomeIndigo,
                                onClick = {
                                    onPlayerClick(
                                        sub,
                                        match.homeTeam.id,
                                        match.homeTeam.name,
                                        match.homeTeam.logoUrl
                                    )
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = awayLineup?.teamName.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        awayLineup?.substitutes.orEmpty().forEach { sub ->
                            SubPlayerCard(
                                player = sub,
                                accentColor = FoulTeamAwayCyan,
                                onClick = {
                                    onPlayerClick(
                                        sub,
                                        match.awayTeam.id,
                                        match.awayTeam.name,
                                        match.awayTeam.logoUrl
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamHalfPitchPlayers(
    players: List<LineupPlayer>,
    isTopHalf: Boolean,
    badgeColor: Color,
    onPlayerClick: (LineupPlayer) -> Unit
) {
    if (players.isEmpty()) return

    val groupedByLine = players.groupBy { it.yardLine }
        .toSortedMap(if (isTopHalf) naturalOrder() else reverseOrder())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        groupedByLine.values.forEach { linePlayers ->
            val orderedLine = linePlayers.sortedBy { it.fieldSide }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderedLine.forEach { player ->
                    PitchPlayerNode(
                        player = player,
                        badgeColor = badgeColor,
                        onClick = { onPlayerClick(player) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PitchPlayerNode(
    player: LineupPlayer,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(8.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(8.dp),
                scaleOnFocus = 1.06f,
                onClick = onClick
            )
            .testTag("pitch_player_${player.id}")
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FoulCardElevated)
                    .border(1.5.dp, Color.White, CircleShape)
            )

            if (player.jerseyNumber > 0) {
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${player.jerseyNumber}",
                        fontSize = 8.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        Surface(
            color = FoulBlackBg.copy(alpha = 0.82f),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                text = player.shortName,
                fontSize = 9.5.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun SubPlayerCard(
    player: LineupPlayer,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = FoulCardDark,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(10.dp),
                scaleOnFocus = 1.03f,
                onClick = onClick
            )
            .border(1.dp, FoulDivider, RoundedCornerShape(10.dp))
            .testTag("sub_player_${player.id}")
    ) {
        Row(
            modifier = Modifier.padding(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(FoulCardElevated)
                    .border(1.dp, accentColor, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.shortName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (player.jerseyNumber > 0) "#${player.jerseyNumber} • ${player.positionName}" else player.positionName,
                    style = MaterialTheme.typography.bodySmall,
                    color = FoulTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Tab 2: Statistics ("قسم الإحصائيات وجلب الاستواذ أول شيء وبقية الاحصائيات")
 */
@Composable
private fun MatchStatisticsSection(
    homeTeamName: String,
    awayTeamName: String,
    stats: List<MatchStatComparison>
) {
    if (stats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا تتوفر إحصائيات مسجلة لهذه المباراة حالياً",
                style = MaterialTheme.typography.titleMedium,
                color = FoulTextSecondary
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_stats_list")
    ) {
        items(stats, key = { it.statId }) { stat ->
            if (stat.isPossession) {
                // Ball Possession Hero Card FIRST
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulDivider, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "نسبة الاستحواذ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = homeTeamName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary
                                )
                                Text(
                                    text = stat.homeValue,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = awayTeamName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary
                                )
                                Text(
                                    text = stat.awayValue,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(stat.homeRatio.coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(FoulTeamHomeIndigo)
                            )
                            Spacer(Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .weight(stat.awayRatio.coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(FoulTeamAwayCyan)
                            )
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.homeValue,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = FoulTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stat.awayValue,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(stat.homeRatio.coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(FoulTeamHomeIndigo)
                            )
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(stat.awayRatio.coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(FoulTeamAwayCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Standings ("قسم الترتيب حسب المباراة الداخل عليها وبشكل لون للفريقين إلي داخل عليهم والدوري
 * وأيضا جعل تصنيف بشكل صفحة كاملة وأنيقة جدا")
 */
@Composable
private fun MatchStandingsFullSection(
    leagueName: String,
    leagueLogoUrl: String,
    homeTeamName: String,
    awayTeamName: String,
    rows: List<StandingRowItem>
) {
    if (rows.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "جدول الترتيب غير متاح لهذه البطولة حالياً",
                style = MaterialTheme.typography.titleMedium,
                color = FoulTextSecondary
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_standings_table")
    ) {
        // League Banner & Color Key for Both Match Teams
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = leagueLogoUrl,
                            contentDescription = leagueName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(3.dp)
                        )
                        Column {
                            Text(
                                text = "جدول ترتيب $leagueName",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تمييز لوني خاص لفريقي المباراة في التصنيف",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(FoulTeamHomeIndigo)
                            )
                            Text(
                                text = homeTeamName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(FoulTeamAwayCyan)
                            )
                            Text(
                                text = awayTeamName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Table Column Headers
        item {
            Surface(
                color = FoulCardElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", Modifier.width(26.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    Text("الفريق", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    Text("لعب", Modifier.width(32.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("ف", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("ت", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("خ", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("+/-", Modifier.width(36.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("نقاط", Modifier.width(40.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }

        // Standings Rows
        items(rows, key = { it.competitorId }) { row ->
            val bgColor = when {
                row.isHomeMatchTeam -> FoulTeamHomeIndigo.copy(alpha = 0.22f)
                row.isAwayMatchTeam -> FoulTeamAwayCyan.copy(alpha = 0.22f)
                else -> FoulCardDark
            }
            val borderColor = when {
                row.isHomeMatchTeam -> FoulTeamHomeIndigo
                row.isAwayMatchTeam -> FoulTeamAwayCyan
                else -> FoulDivider
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (row.isHomeMatchTeam || row.isAwayMatchTeam) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${row.position}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (row.position <= 4) FoulGoldStar else Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(26.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = row.logoUrl,
                            contentDescription = row.teamName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = row.teamName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = if (row.isHomeMatchTeam || row.isAwayMatchTeam) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text("${row.played}", Modifier.width(32.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("${row.won}", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("${row.drawn}", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text("${row.lost}", Modifier.width(26.dp), color = FoulTextSecondary, textAlign = TextAlign.Center)
                    Text(
                        text = if (row.goalDiff > 0) "+${row.goalDiff}" else "${row.goalDiff}",
                        modifier = Modifier.width(36.dp),
                        color = FoulTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = when {
                            row.isHomeMatchTeam -> FoulTeamHomeIndigo
                            row.isAwayMatchTeam -> FoulTeamAwayCyan
                            else -> FoulCardElevated
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = "${row.points}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 4: News ("وقسم الأخبار أيضا بشكل جلب الحقيقي")
 */
@Composable
private fun MatchNewsSection(news: List<NewsArticleItem>) {
    val context = LocalContext.current

    if (news.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد أخبار متاحة حالياً",
                style = MaterialTheme.typography.titleMedium,
                color = FoulTextSecondary
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_news_list")
    ) {
        items(news, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .tvFocusableClickable(
                        shape = RoundedCornerShape(12.dp),
                        scaleOnFocus = 1.02f,
                        onClick = {
                            if (item.url.isNotBlank()) {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        }
                    )
                    .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FoulCardElevated)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FoulCardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "قراءة الخبر",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
