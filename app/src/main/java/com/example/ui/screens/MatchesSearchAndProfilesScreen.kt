package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.MatchCompetitor
import com.example.data.model.MatchItem
import com.example.data.model.MatchesSearchResult
import com.example.data.model.PlayerFullProfile
import com.example.data.model.PlayerGeneralStatItem
import com.example.data.model.PlayerLeagueCareerStat
import com.example.data.model.PlayerTransferItem
import com.example.data.model.TeamFullProfile
import com.example.ui.components.OrderedColorfulDivider
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
import com.example.ui.theme.FoulPurpleGlow
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulSurfaceDark
import com.example.ui.theme.FoulTeamAwayCyan

@Composable
fun MatchesSearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchResult: MatchesSearchResult?,
    telegramUrl: String = "https://t.me/jdj_q",
    onPlayerSelect: (PlayerFullProfile) -> Unit,
    onTeamSelect: (MatchCompetitor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("matches_search_screen")
    ) {
        // 1. Smart Top Bar: Strictly Telegram Icon + FOUL TV Name + App Logo Image
        com.example.ui.components.FoulSmartTopBar(
            telegramUrl = telegramUrl,
            telegramTestTag = "matches_search_telegram_button"
        )

        // 2. Contextual Sub-Header Bar Directly Underneath Top Bar (Back Button + Title)
        com.example.ui.components.FoulSectionSubBar(
            title = "البحث عن لاعب أو فريق",
            subtitle = "استعرض مسيرة اللاعبين والأندية وإحصائياتهم الكاملة",
            onBackClick = onBack,
            backButtonTestTag = "matches_search_back_button"
        )

        // 3. Dedicated Search Input Bar Underneath Sub-Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulSurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "ابحث عن لاعب أو فريق...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("matches_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FoulPurpleBright,
                        unfocusedBorderColor = FoulDivider,
                        focusedContainerColor = FoulCardDark,
                        unfocusedContainerColor = FoulCardDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("matches_search_input")
                )
            }
            OrderedColorfulDivider(thickness = 1.dp)
        }

        // Body: Show NOTHING except the prompt when searchQuery is blank ("تفتح صفحة لا تعرض أي شيء فقط عند البحث وأيضا بشكل أيقونة ونص تقول أبحث عن لاعب أو فريق")
        if (searchQuery.trim().isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .testTag("matches_search_empty_prompt"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FoulPurplePrimary.copy(alpha = 0.55f),
                                        FoulPurpleDeep.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.5.dp, FoulPurpleBright.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = "ابحث عن لاعب أو فريق",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ابحث عن لاعب أو فريق",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اكتب اسم اللاعب أو النادي في شريط البحث بالأعلى لعرض المسيرة الكاملة، الإحصائيات، المباريات والانتقالات",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري البحث عن \"$searchQuery\"...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            val players = searchResult?.players.orEmpty()
            val teams = searchResult?.teams.orEmpty()

            if (players.isEmpty() && teams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "جرّب كتابة اسم اللاعب أو الفريق بشكل آخر",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("matches_search_results_list"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (players.isNotEmpty()) {
                        item {
                            SearchSectionHeader(
                                title = "اللاعبون (${players.size})",
                                icon = Icons.Default.Person,
                                accentColor = FoulGoldStar
                            )
                        }
                        items(players, key = { "player_${it.id}_${it.name}" }) { player ->
                            SearchedPlayerCard(
                                player = player,
                                onClick = { onPlayerSelect(player) }
                            )
                        }
                    }

                    if (teams.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SearchSectionHeader(
                                title = "الفرق والأندية (${teams.size})",
                                icon = Icons.Default.Shield,
                                accentColor = FoulTeamAwayCyan
                            )
                        }
                        items(teams, key = { "team_${it.id}_${it.name}" }) { team ->
                            SearchedTeamCard(
                                team = team,
                                onClick = { onTeamSelect(team) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SearchedPlayerCard(
    player: PlayerFullProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(14.dp),
                scaleOnFocus = 1.02f,
                onClick = onClick
            )
            .testTag("search_player_card_${player.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(1.dp, FoulDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Player Photo
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(FoulPurpleDeep.copy(alpha = 0.6f))
                    .border(1.5.dp, FoulPurpleBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = player.photoUrl,
                    contentDescription = player.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    error = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.jerseyNumber > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FoulPurpleDeep,
                            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "#${player.jerseyNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Current Club
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (player.currentTeamLogoUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = player.currentTeamLogoUrl,
                            contentDescription = player.currentTeamName,
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "النادي: ${player.currentTeamName.ifBlank { "غير محدد" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Nationality & Position badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = FoulTeamAwayCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "الجنسية: ${player.nationality.ifBlank { "غير محدد" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    if (player.positionName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FoulPurplePrimary.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = player.positionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = FoulPurpleDeep.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "التفاصيل",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchedTeamCard(
    team: MatchCompetitor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(14.dp),
                scaleOnFocus = 1.02f,
                onClick = onClick
            )
            .testTag("search_team_card_${team.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(1.dp, FoulDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FoulCardElevated)
                    .border(1.dp, FoulPurpleBright.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = team.logoUrl,
                    contentDescription = team.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "اضغط لعرض المباريات السابقة والقادمة والانتقالات والتشكيلة",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = FoulTeamAwayCyan.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, FoulTeamAwayCyan.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "عرض النادي",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerFullProfileScreen(
    playerProfile: PlayerFullProfile?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "المسيرة في الدوريات",
        "الإحصائيات الكاملة",
        "الانتقالات"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("player_full_profile_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulSurfaceDark)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = FoulPurpleDeep.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = playerProfile?.name ?: "ملف اللاعب الكامل",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "المسيرة في الدوريات • الإحصائيات • الانتقالات",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.5.dp)

        if (isLoading && playerProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري جلب المسيرة والإحصائيات الكاملة للاعب...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            return@Column
        }

        if (playerProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "لم يتم الحصول على معلومات اللاعب",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            return@Column
        }

        // Player Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FoulPurpleDeep.copy(alpha = 0.65f),
                                FoulCardDark
                            )
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Player Avatar with Jersey Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(FoulCardElevated)
                                .border(2.dp, FoulGoldStar, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = playerProfile.photoUrl,
                                contentDescription = playerProfile.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            )
                        }

                        if (playerProfile.jerseyNumber > 0) {
                            Surface(
                                shape = CircleShape,
                                color = FoulPurplePrimary,
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = playerProfile.jerseyNumber.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Player Main Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = playerProfile.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Current Club Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (playerProfile.currentTeamLogoUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = playerProfile.currentTeamLogoUrl,
                                    contentDescription = playerProfile.currentTeamName,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = FoulGoldStar,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "النادي الحالي: ${playerProfile.currentTeamName.ifBlank { "غير محدد" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Nationality Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (playerProfile.nationalityFlagUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = playerProfile.nationalityFlagUrl,
                                    contentDescription = playerProfile.nationality,
                                    modifier = Modifier.size(16.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = FoulTeamAwayCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "الجنسية: ${playerProfile.nationality.ifBlank { "غير محدد" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bio Pills Row (Position, Age, Height, Weight)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerInfoPill(
                        label = "المركز",
                        value = playerProfile.positionName.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerInfoPill(
                        label = "العمر",
                        value = playerProfile.age.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerInfoPill(
                        label = "الطول",
                        value = playerProfile.height.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerInfoPill(
                        label = "الوزن",
                        value = playerProfile.weight.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (playerProfile.marketValue.isNotBlank() || playerProfile.preferredFoot.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (playerProfile.preferredFoot.isNotBlank()) {
                            PlayerInfoPill(
                                label = "القدم المفضلة",
                                value = playerProfile.preferredFoot,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (playerProfile.marketValue.isNotBlank()) {
                            PlayerInfoPill(
                                label = "القيمة السوقية",
                                value = playerProfile.marketValue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FoulPurplePrimary else FoulCardDark,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) FoulGoldStar else FoulDivider
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(12.dp),
                            scaleOnFocus = 1.03f,
                            onClick = { selectedTabIndex = index }
                        )
                        .testTag("player_profile_tab_$index")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = FoulGoldStar,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "جاري تحديث البيانات التفصيلية...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> PlayerLeagueCareerTab(playerProfile.leagueCareerStats)
                1 -> PlayerFullStatisticsTab(
                    generalStats = playerProfile.fullStatistics,
                    careerStats = playerProfile.leagueCareerStats
                )
                2 -> PlayerTransfersSectionTab(
                    transfers = playerProfile.transfers,
                    entityName = playerProfile.name
                )
            }
        }
    }
}

@Composable
private fun PlayerInfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = FoulCardElevated,
        border = BorderStroke(1.dp, FoulDivider)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 10.sp,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayerLeagueCareerTab(
    careerStats: List<PlayerLeagueCareerStat>
) {
    if (careerStats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = FoulGoldStar,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "لم يتم الحصول على معلومات تفصيلية لمسيرة الدوريات",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val totalMatches = careerStats.sumOf { it.appearances }
    val totalGoals = careerStats.sumOf { it.goals }
    val totalAssists = careerStats.sumOf { it.assists }
    val totalYellow = careerStats.sumOf { it.yellowCards }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player_league_career_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulPurpleDeep.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, FoulGoldStar.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = FoulGoldStar,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ملخص مسيرة اللاعب في البطولات والدوريات",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CareerSummaryStatBox("البطولات", "${careerStats.size}", FoulTeamAwayCyan)
                        CareerSummaryStatBox("المباريات", "$totalMatches", Color.White)
                        CareerSummaryStatBox("الأهداف", "$totalGoals", FoulLiveGreen)
                        CareerSummaryStatBox("الصناعة", "$totalAssists", FoulGoldStar)
                        CareerSummaryStatBox("إنذارات", "$totalYellow", Color(0xFFFFD54F))
                    }
                }
            }
        }

        items(
            careerStats,
            key = { "${it.id}_${it.competitionName}_${it.seasonName}_${it.teamName}" }
        ) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                border = BorderStroke(1.dp, FoulDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // League & Season Header
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
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FoulCardElevated)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = item.competitionLogoUrl,
                                    contentDescription = item.competitionName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = FoulGoldStar,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                            Column {
                                Text(
                                    text = item.competitionName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.teamName.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (item.teamLogoUrl.isNotBlank()) {
                                            SubcomposeAsyncImage(
                                                model = item.teamLogoUrl,
                                                contentDescription = item.teamName,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = item.teamName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (item.seasonName.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FoulPurpleDeep.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = item.seasonName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (item.rating.isNotBlank() && item.rating != "-") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FoulLiveGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, FoulLiveGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = FoulGoldStar,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = item.rating,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stats Grid Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FoulCardElevated)
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LeagueStatMiniCol("مباريات", "${item.appearances}", Color.White)
                        LeagueStatMiniCol("أهداف", "${item.goals}", FoulLiveGreen)
                        LeagueStatMiniCol("صناعة", "${item.assists}", FoulGoldStar)
                        if (item.minutesPlayed > 0) {
                            LeagueStatMiniCol("دقائق", "${item.minutesPlayed}'", FoulTeamAwayCyan)
                        }
                        LeagueStatMiniCol("صفراء", "${item.yellowCards}", Color(0xFFFFD54F))
                        LeagueStatMiniCol("حمراء", "${item.redCards}", FoulLiveRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerSummaryStatBox(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun LeagueStatMiniCol(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun PlayerFullStatisticsTab(
    generalStats: List<PlayerGeneralStatItem>,
    careerStats: List<PlayerLeagueCareerStat>
) {
    val combinedStats = remember(generalStats, careerStats) {
        if (generalStats.isNotEmpty()) {
            generalStats
        } else if (careerStats.isNotEmpty()) {
            val totalMatches = careerStats.sumOf { it.appearances }
            val totalGoals = careerStats.sumOf { it.goals }
            val totalAssists = careerStats.sumOf { it.assists }
            val totalMinutes = careerStats.sumOf { it.minutesPlayed }
            val totalYellow = careerStats.sumOf { it.yellowCards }
            val totalRed = careerStats.sumOf { it.redCards }
            listOf(
                PlayerGeneralStatItem("إجمالي المباريات الملعوبة", "$totalMatches", (totalMatches / 100f).coerceIn(0.1f, 1f)),
                PlayerGeneralStatItem("إجمالي الأهداف المسجلة", "$totalGoals", (totalGoals / 50f).coerceIn(0.05f, 1f)),
                PlayerGeneralStatItem("إجمالي التمريرات الحاسمة (الأسيست)", "$totalAssists", (totalAssists / 40f).coerceIn(0.05f, 1f)),
                PlayerGeneralStatItem("المساهمات التهديفية المباشرة", "${totalGoals + totalAssists}", ((totalGoals + totalAssists) / 70f).coerceIn(0.05f, 1f)),
                PlayerGeneralStatItem("إجمالي دقائق اللعب", "$totalMinutes دقيقة", (totalMinutes / 3000f).coerceIn(0.1f, 1f)),
                PlayerGeneralStatItem("البطاقات الصفراء", "$totalYellow", (totalYellow / 20f).coerceIn(0.05f, 1f)),
                PlayerGeneralStatItem("البطاقات الحمراء", "$totalRed", (totalRed / 5f).coerceIn(0.02f, 1f))
            )
        } else {
            emptyList()
        }
    }

    if (combinedStats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = FoulTeamAwayCyan,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "لم يتم الحصول على معلومات الإحصائيات التفصيلية",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player_full_statistics_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(combinedStats, key = { "${it.label}_${it.value}" }) { stat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                border = BorderStroke(1.dp, FoulDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.titleSmall,
                            color = FoulGoldStar,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Progress bar indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(FoulCardElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stat.progressRatio.coerceIn(0.06f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FoulPurpleBright, FoulTeamAwayCyan)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerTransfersSectionTab(
    transfers: List<PlayerTransferItem>,
    entityName: String
) {
    if (transfers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("transfers_empty_message"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                border = BorderStroke(1.dp, FoulDivider)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = FoulPurpleGlow,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "لم يتم الحصول على معلومات",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "لا تتوفر سجل انتقالات مسجلة حالياً لـ $entityName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transfers_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transfers, key = { "${it.id}_${it.playerName}_${it.fromTeamName}_${it.toTeamName}_${it.dateText}" }) { transfer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                border = BorderStroke(1.dp, FoulDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Row: Player Name + Transfer Type / Date / Fee
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (transfer.playerPhotoUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = transfer.playerPhotoUrl,
                                    contentDescription = transfer.playerName,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = transfer.playerName.ifBlank { entityName },
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (transfer.dateText.isNotBlank()) {
                                Text(
                                    text = transfer.dateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                            if (transfer.transferTypeOrFee.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FoulGoldStar.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, FoulGoldStar.copy(alpha = 0.55f))
                                ) {
                                    Text(
                                        text = transfer.transferTypeOrFee,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // From Club -> To Club Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FoulCardElevated)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // From Team
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = transfer.fromTeamLogoUrl,
                                contentDescription = transfer.fromTeamName,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit,
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            )
                            Column {
                                Text(
                                    text = "من نادي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = transfer.fromTeamName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Arrow badge
                        Surface(
                            shape = CircleShape,
                            color = FoulPurplePrimary.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, FoulPurpleBright),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = "انتقال إلى",
                                    tint = FoulGoldStar,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // To Team
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = "إلى نادي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulLiveGreen,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = transfer.toTeamName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            SubcomposeAsyncImage(
                                model = transfer.toTeamLogoUrl,
                                contentDescription = transfer.toTeamName,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit,
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = FoulLiveGreen,
                                        modifier = Modifier.size(22.dp)
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
fun TeamFullProfileScreen(
    teamProfile: TeamFullProfile?,
    isLoading: Boolean,
    errorMessage: String?,
    onMatchClick: (MatchItem) -> Unit,
    onPlayerClick: (PlayerFullProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "المباريات التي لُعبت",
        "المباريات القادمة",
        "الانتقالات",
        "اللاعبون"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("team_full_profile_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulSurfaceDark)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = FoulPurpleDeep.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f)),
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("team_profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column {
                Text(
                    text = teamProfile?.name ?: "ملف النادي الكامل",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "المباريات السابقة والقادمة • الانتقالات • التشكيلة",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }
        }

        OrderedColorfulDivider(thickness = 1.5.dp)

        if (isLoading && teamProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري جلب تفاصيل النادي والمباريات والانتقالات...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            return@Column
        }

        if (teamProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "لم يتم الحصول على معلومات النادي",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            return@Column
        }

        // Club Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                FoulPurpleDeep.copy(alpha = 0.7f),
                                FoulCardDark
                            )
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(FoulCardElevated)
                        .border(1.5.dp, FoulGoldStar.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = teamProfile.logoUrl,
                        contentDescription = teamProfile.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = teamProfile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (teamProfile.countryName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = FoulTeamAwayCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "الدولة: ${teamProfile.countryName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    if (teamProfile.venueName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stadium,
                                contentDescription = null,
                                tint = FoulGoldStar,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "الملعب: ${teamProfile.venueName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FoulPurplePrimary else FoulCardDark,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) FoulGoldStar else FoulDivider
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(12.dp),
                            scaleOnFocus = 1.03f,
                            onClick = { selectedTabIndex = index }
                        )
                        .testTag("team_profile_tab_$index")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> TeamMatchesListTab(
                    matches = teamProfile.playedMatches,
                    emptyMessage = "لا توجد مباريات سابقة مسجلة حالياً لهذا الفريق",
                    onMatchClick = onMatchClick
                )
                1 -> TeamMatchesListTab(
                    matches = teamProfile.upcomingMatches,
                    emptyMessage = "لا توجد مباريات قادمة مجدولة حالياً لهذا الفريق",
                    onMatchClick = onMatchClick
                )
                2 -> PlayerTransfersSectionTab(
                    transfers = teamProfile.transfers,
                    entityName = teamProfile.name
                )
                3 -> TeamSquadPlayersTab(
                    players = teamProfile.squadPlayers,
                    onPlayerClick = onPlayerClick
                )
            }
        }
    }
}

@Composable
private fun TeamMatchesListTab(
    matches: List<MatchItem>,
    emptyMessage: String,
    onMatchClick: (MatchItem) -> Unit
) {
    if (matches.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = FoulPurpleGlow,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(matches, key = { it.id }) { match ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .tvFocusableClickable(
                        shape = RoundedCornerShape(14.dp),
                        scaleOnFocus = 1.02f,
                        onClick = { onMatchClick(match) }
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                border = BorderStroke(1.dp, FoulDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // League & Date Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = match.competitionLogoUrl,
                                contentDescription = match.competitionName,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = match.competitionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulGoldStar,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (match.displayTime.isNotBlank()) {
                            Text(
                                text = match.displayTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // Teams & Score Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Home Team
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = match.homeTeam.logoUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = match.homeTeam.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Score / Status Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FoulCardElevated,
                            border = BorderStroke(
                                1.dp,
                                if (match.isLive) FoulLiveGreen else FoulPurpleBright.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            val centerText = if (match.isLive || match.isFinished || match.homeTeam.hasScore || match.awayTeam.hasScore) {
                                "${match.homeTeam.score.coerceAtLeast(0)} - ${match.awayTeam.score.coerceAtLeast(0)}"
                            } else {
                                match.displayTime.ifBlank { match.statusText.ifBlank { "قادمة" } }
                            }
                            Text(
                                text = centerText,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        // Away Team
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = match.awayTeam.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SubcomposeAsyncImage(
                                model = match.awayTeam.logoUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamSquadPlayersTab(
    players: List<PlayerFullProfile>,
    onPlayerClick: (PlayerFullProfile) -> Unit
) {
    if (players.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لم يتم الحصول على معلومات قائمة اللاعبين حالياً",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players, key = { "${it.id}_${it.name}" }) { player ->
            SearchedPlayerCard(
                player = player,
                onClick = { onPlayerClick(player) }
            )
        }
    }
}
