package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.AppVersionConfig
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import com.example.data.model.LeagueMatchesGroup
import com.example.data.model.LiveChannel
import com.example.data.model.MatchPublishedChannel
import com.example.data.model.PublishedEventItem
import com.example.data.model.XtreamAccount
import com.example.data.model.XtreamAccountLiveCatalog
import com.example.data.model.XtreamLiveStreamItem
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.SportsSoccer
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.FoulCompactTopBar
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Forum
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.example.data.model.ContactMessageItem
import com.example.data.repository.SocialFirebaseRepository
import com.example.ui.theme.FoulTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

private data class DevTabSpec(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

private val DEV_TABS = listOf(
    DevTabSpec(0, "الأفرع وترتيبها", Icons.Default.FolderSpecial, "dev_tab_branches"),
    DevTabSpec(9, "نشر حساب IPTV (مستقل)", Icons.Default.LiveTv, "dev_tab_iptv_accounts"),
    DevTabSpec(8, "نشر فرع حساب", Icons.Default.AccountTree, "dev_tab_publish_account_branch"),
    DevTabSpec(1, "القنوات والسيرفرات", Icons.Default.LiveTv, "dev_tab_channels"),
    DevTabSpec(2, "حسابات المكتبة (أفلام ومسلسلات)", Icons.Default.CloudQueue, "dev_tab_xtream"),
    DevTabSpec(3, "الإصدارات والتحديث", Icons.Default.SystemUpdate, "dev_tab_versions"),
    DevTabSpec(4, "تواصل المستخدمين", Icons.Default.Forum, "dev_tab_user_messages"),
    DevTabSpec(5, "تحكم في التطبيق", Icons.Default.Tune, "dev_tab_app_control"),
    DevTabSpec(6, "نشر قناة لجدول المباريات", Icons.Default.SportsSoccer, "dev_tab_match_channels"),
    DevTabSpec(7, "نشر حدث", Icons.Default.Campaign, "dev_tab_publish_event")
)


/**
 * Completely isolated full-screen Developer Control Panel ("صفحة منعزلة كلياً وإمكانية التحكم في التطبيق").
 */
@Composable
fun DeveloperControlPanelScreen(
    developerEmail: String,
    isActionLoading: Boolean,
    statusMessage: String?,
    branches: List<ChannelCategory>,
    channels: List<LiveChannel>,
    xtreamAccounts: List<XtreamAccount>,
    iptvAccounts: List<XtreamAccount> = emptyList(),
    versionConfig: AppVersionConfig,
    userMessages: List<ContactMessageItem> = emptyList(),
    leagueGroups: List<LeagueMatchesGroup> = emptyList(),
    matchPublishedChannels: List<MatchPublishedChannel> = emptyList(),
    publishedEvents: List<PublishedEventItem> = emptyList(),
    showChannelViewsAndLikes: Boolean = true,
    showAdsInApp: Boolean = true,
    onlineUsersCount: Int = 1,
    isFetchingAccountBranchCatalog: Boolean = false,
    isFetchingAccountCategoryStreams: Boolean = false,
    accountBranchFetchError: String? = null,
    fetchedAccountLiveCatalog: XtreamAccountLiveCatalog? = null,
    onDismissStatusMessage: () -> Unit,
    onSaveBranch: (existingId: String?, name: String, imageUrl: String, parentId: String) -> Unit,
    onMoveBranchOrder: (branchId: String, moveUp: Boolean) -> Unit,
    onSaveBranchesOrder: () -> Unit,
    onDeleteBranch: (branchId: String) -> Unit,
    onFetchAccountLiveCatalog: (host: String, username: String, password: String) -> Unit = { _, _, _ -> },
    onFetchAccountCategoryStreams: (host: String, username: String, password: String, categoryId: String, categoryName: String) -> Unit = { _, _, _, _, _ -> },
    onClearAccountLiveCatalog: () -> Unit = {},
    onPublishAccountBranch: (existingBranchId: String?, branchName: String, branchImageUrl: String, parentBranchId: String, orderedSelectedStreams: List<XtreamLiveStreamItem>) -> Unit = { _, _, _, _, _ -> },
    onSaveChannel: (existingId: String?, name: String, imageUrl: String, targetBranch: ChannelCategory?, servers: List<ChannelServer>) -> Unit,
    onDeleteChannel: (channelId: String) -> Unit,
    onSaveXtreamAccount: (existingId: String?, name: String, host: String, username: String, password: String) -> Unit,
    onDeleteXtreamAccount: (accountId: String) -> Unit,
    onSaveIptvAccount: (existingId: String?, name: String, host: String, username: String, password: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteIptvAccount: (accountId: String) -> Unit = {},
    onSaveVersionConfig: (requiredVersion: String, updateText: String, updateUrl: String, telegramUrl: String, lockOldVersions: Boolean) -> Unit,
    onSaveMatchPublishedChannel: (item: MatchPublishedChannel, extraMainChannels: List<LiveChannel>) -> Unit = { _, _ -> },
    onDeleteMatchPublishedChannel: (id: String) -> Unit = {},
    onSavePublishedEvent: (event: PublishedEventItem) -> Unit = {},
    onDeletePublishedEvent: (eventId: String) -> Unit = {},
    onSendDeveloperReply: (targetMessage: ContactMessageItem, replyText: String) -> Unit = { _, _ -> },
    onRefreshUserMessages: () -> Unit = {},
    onToggleShowChannelViewsAndLikes: (Boolean) -> Unit = {},
    onToggleShowAdsInApp: (Boolean) -> Unit = {},
    onRefreshOnlineUsers: () -> Unit = {},
    onRefreshData: () -> Unit,
    onLogoutDeveloper: () -> Unit,
    onClosePanel: () -> Unit
) {

    BackHandler {
        onClosePanel()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("developer_control_panel_screen")
    ) {
        // Isolated Developer Top Bar
        FoulCompactTopBar(
            title = "لوحة تحكم المطور • نسخة ${AppVersionConfig.APP_CURRENT_VERSION}",
            subtitle = developerEmail.ifBlank { "تحكم كامل في القنوات والأفرع و IPTV والمكتبة (إصدار ${AppVersionConfig.APP_CURRENT_VERSION})" },
            leadingIcon = Icons.Default.AdminPanelSettings,
            actions = {
                CompactTopBarIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "تحديث البيانات",
                    onClick = onRefreshData,
                    modifier = Modifier.testTag("dev_panel_refresh_button")
                )
                CompactTopBarIconButton(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "تسجيل خروج المطور",
                    onClick = onLogoutDeveloper,
                    modifier = Modifier.testTag("dev_panel_logout_button")
                )
                CompactTopBarIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "إغلاق لوحة المطور",
                    onClick = onClosePanel,
                    modifier = Modifier.testTag("dev_panel_back_button")
                )
            }
        )

        // Scrollable Horizontal Tabs Bar ("اجعل أقسام لوحة تحكم بشكل شريط تمرير بينهما")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF06060C))
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DEV_TABS, key = { it.index }) { tab ->
                val selected = selectedTabIndex == tab.index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) FoulPurpleBright else FoulCardDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedTabIndex = tab.index }
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.White.copy(alpha = 0.65f) else FoulDivider,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag(tab.testTag)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                        if (tab.index == 4 && userMessages.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = if (selected) FoulBlackBg else FoulPurpleBright
                            ) {
                                Text(
                                    text = userMessages.count { !it.isFromBotOrAdmin }.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }


        OrderedColorfulDivider(thickness = 1.dp)

        // Status / Loading Banner
        if (isActionLoading || !statusMessage.isNullOrBlank()) {
            Surface(
                color = FoulPurpleDeep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FoulLiveGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isActionLoading) "جاري تنفيذ العملية في قاعدة البيانات..." else statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isActionLoading && !statusMessage.isNullOrBlank()) {
                        IconButton(
                            onClick = onDismissStatusMessage,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق التنبيه",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> BranchesManagementTab(
                    branches = branches,
                    channels = channels,
                    isActionLoading = isActionLoading,
                    onSaveBranch = onSaveBranch,
                    onMoveBranchOrder = onMoveBranchOrder,
                    onSaveBranchesOrder = onSaveBranchesOrder,
                    onDeleteBranch = onDeleteBranch,
                    onOpenPublishAccountBranchTab = { selectedTabIndex = 8 }
                )

                8 -> DeveloperPublishAccountBranchTab(
                    branches = branches,
                    channels = channels,
                    xtreamAccounts = xtreamAccounts,
                    isActionLoading = isActionLoading,
                    isFetchingCatalog = isFetchingAccountBranchCatalog,
                    isFetchingCategoryStreams = isFetchingAccountCategoryStreams,
                    fetchError = accountBranchFetchError,
                    fetchedCatalog = fetchedAccountLiveCatalog,
                    onFetchAccountCatalog = onFetchAccountLiveCatalog,
                    onFetchCategoryStreams = onFetchAccountCategoryStreams,
                    onClearCatalog = onClearAccountLiveCatalog,
                    onPublishAccountBranch = onPublishAccountBranch
                )

                1 -> ChannelsManagementTab(
                    branches = branches,
                    channels = channels,
                    isActionLoading = isActionLoading,
                    onSaveChannel = onSaveChannel,
                    onDeleteChannel = onDeleteChannel
                )

                2 -> XtreamAccountsManagementTab(
                    xtreamAccounts = xtreamAccounts,
                    isActionLoading = isActionLoading,
                    onSaveXtreamAccount = onSaveXtreamAccount,
                    onDeleteXtreamAccount = onDeleteXtreamAccount
                )

                9 -> IptvAccountsManagementTab(
                    iptvAccounts = iptvAccounts,
                    isActionLoading = isActionLoading,
                    onSaveIptvAccount = onSaveIptvAccount,
                    onDeleteIptvAccount = onDeleteIptvAccount
                )

                3 -> AppVersionsManagementTab(
                    versionConfig = versionConfig,
                    isActionLoading = isActionLoading,
                    onSaveVersionConfig = onSaveVersionConfig
                )

                4 -> DeveloperUserMessagesTab(
                    messages = userMessages,
                    isActionLoading = isActionLoading,
                    onSendDeveloperReply = onSendDeveloperReply,
                    onRefreshMessages = onRefreshUserMessages
                )

                5 -> DeveloperAppControlTab(
                    showChannelViewsAndLikes = showChannelViewsAndLikes,
                    showAdsInApp = showAdsInApp,
                    onlineUsersCount = onlineUsersCount,
                    isActionLoading = isActionLoading,
                    channelsCount = channels.size,
                    branchesCount = branches.size,
                    onToggleShowChannelViewsAndLikes = onToggleShowChannelViewsAndLikes,
                    onToggleShowAdsInApp = onToggleShowAdsInApp,
                    onRefreshOnlineUsers = onRefreshOnlineUsers
                )

                6 -> DeveloperMatchChannelsTab(
                    leagueGroups = leagueGroups,
                    allChannels = channels,
                    matchPublishedChannels = matchPublishedChannels,
                    isActionLoading = isActionLoading,
                    onSaveMatchPublishedChannel = onSaveMatchPublishedChannel,
                    onDeleteMatchPublishedChannel = onDeleteMatchPublishedChannel
                )

                7 -> DeveloperPublishEventTab(
                    allChannels = channels,
                    publishedEvents = publishedEvents,
                    isActionLoading = isActionLoading,
                    onSavePublishedEvent = onSavePublishedEvent,
                    onDeletePublishedEvent = onDeletePublishedEvent
                )
            }
        }
    }
}


/**
 * Tab 0: Branches & Sub-Branches Management ("إضافة اسم الفرع ورابط صورة الفرع وإمكانية فرع داخل الفرع
 * وتعديل وحذف وتعديل الظهور في الأعلى أو تحت وإمكانية الحفظ")
 */
@Composable
private fun BranchesManagementTab(
    branches: List<ChannelCategory>,
    channels: List<LiveChannel>,
    isActionLoading: Boolean,
    onSaveBranch: (existingId: String?, name: String, imageUrl: String, parentId: String) -> Unit,
    onMoveBranchOrder: (branchId: String, moveUp: Boolean) -> Unit,
    onSaveBranchesOrder: () -> Unit,
    onDeleteBranch: (branchId: String) -> Unit,
    onOpenPublishAccountBranchTab: () -> Unit = {}
) {
    var editingBranchId by remember { mutableStateOf<String?>(null) }
    var branchName by remember { mutableStateOf("") }
    var branchImageUrl by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf("") }

    val sortedBranches = remember(branches) { branches.sortedBy { it.order } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Banner Shortcut to "نشر فرع حساب" (Publish Account Branch from Xtream Host/User/Pass)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FoulPurpleDeep.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenPublishAccountBranchTab() }
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulPurpleBright, FoulLiveGreen)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("dev_open_publish_account_branch_shortcut")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = FoulLiveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "نشر فرع حساب (جلب القنوات والتصنيفات من هوست وحساب)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "أضف الهوست واسم المستخدم وكلمة المرور واجلب تصنيفات الحساب واختر القنوات ورتبها وانشرها كفرع",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                    }
                    Surface(
                        color = FoulPurpleBright,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "فتح الآن",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Form Card: Add or Edit Branch / Sub-Branch
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingBranchId != null) "تعديل الفرع المحدد" else "نشر فرع جديد في الرئيسية (أو فرع داخل فرع)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (editingBranchId != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FoulCardElevated,
                                modifier = Modifier.clickable {
                                    editingBranchId = null
                                    branchName = ""
                                    branchImageUrl = ""
                                    selectedParentId = ""
                                }
                            ) {
                                Text(
                                    text = "إلغاء التعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = "اسم الفرع (مثال: قنوات بي إن سبورت)",
                        testTag = "dev_branch_name_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = branchImageUrl,
                        onValueChange = { branchImageUrl = it },
                        label = "رابط صورة الفرع (http://... أو https://...)",
                        testTag = "dev_branch_image_input"
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "مكان ظهور الفرع (فرع رئيسي أو فرع داخل فرع):",
                        style = MaterialTheme.typography.labelMedium,
                        color = FoulTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    // Parent branch selector chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isRootSelected = selectedParentId.isBlank()
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isRootSelected) FoulPurpleBright else FoulCardElevated,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedParentId = "" }
                                    .border(
                                        1.dp,
                                        if (isRootSelected) Color.White else FoulDivider,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = "فرع رئيسي (في الرئيسية)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isRootSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(
                            sortedBranches.filter { it.id != editingBranchId },
                            key = { "parent_opt_${it.id}" }
                        ) { candidateParent ->
                            val isSelected = selectedParentId == candidateParent.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FoulPurpleBright else FoulCardElevated,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedParentId = candidateParent.id }
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else FoulDivider,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = "داخل: ${candidateParent.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveBranch(
                                editingBranchId,
                                branchName,
                                branchImageUrl,
                                selectedParentId
                            )
                            if (editingBranchId != null) {
                                editingBranchId = null
                            }
                            branchName = ""
                            branchImageUrl = ""
                            selectedParentId = ""
                        },
                        enabled = !isActionLoading && branchName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoulPurpleBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dev_save_branch_button")
                    ) {
                        Icon(
                            imageVector = if (editingBranchId != null) Icons.Default.Save else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (editingBranchId != null) "حفظ تعديلات الفرع" else "نشر الفرع الآن",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Branches Reorder & Save Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الأفرع المنشورة (${sortedBranches.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "رتب ظهور الأفرع للأعلى أو الأسفل ثم اضغط حفظ الترتيب",
                        style = MaterialTheme.typography.labelSmall,
                        color = FoulTextSecondary
                    )
                }

                Button(
                    onClick = onSaveBranchesOrder,
                    enabled = !isActionLoading && sortedBranches.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FoulPurpleDeep,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("dev_save_branches_order_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "حفظ الترتيب",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Branches List with Up / Down / Edit / Delete
        itemsIndexed(sortedBranches, key = { _, b -> b.id }) { index, branch ->
            val parentBranch = sortedBranches.find { it.id == branch.parentId }
            val branchChannelsCount = channels.count { ch ->
                ch.branchId == branch.id ||
                    (ch.branchId.isBlank() && ch.category.equals(branch.name, ignoreCase = true))
            }
            val subBranchesCount = sortedBranches.count { it.parentId == branch.id }

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
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FoulPurpleDeep)
                            .border(1.dp, FoulDivider, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (branch.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = branch.imageUrl,
                                contentDescription = branch.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FoulPurpleDeep
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = branch.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = if (parentBranch != null) {
                                "فرع داخل: ${parentBranch.name} • $branchChannelsCount قناة"
                            } else {
                                "فرع رئيسي • $subBranchesCount فرع فرعي • $branchChannelsCount قناة"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Order Up / Down & Edit / Delete actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onMoveBranchOrder(branch.id, true) },
                            enabled = index > 0,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "للأعلى",
                                tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onMoveBranchOrder(branch.id, false) },
                            enabled = index < sortedBranches.lastIndex,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "للأسفل",
                                tint = if (index < sortedBranches.lastIndex) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                editingBranchId = branch.id
                                branchName = branch.name
                                branchImageUrl = branch.imageUrl
                                selectedParentId = branch.parentId
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الفرع",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteBranch(branch.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الفرع",
                                tint = FoulLiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Channels inside Branches & Multi-Server Builder ("نشر القنوات داخل الفرع بشكل إضافة اسم القناة
 * وإضافة رابط صورة القناة وإضافة رابط البث بشكل أما امتداد m3u8 أو ts وبشكل سيرفرات يعني إضافة اسم السيرفر ورابط البث")
 */
@Composable
private fun ChannelsManagementTab(
    branches: List<ChannelCategory>,
    channels: List<LiveChannel>,
    isActionLoading: Boolean,
    onSaveChannel: (existingId: String?, name: String, imageUrl: String, targetBranch: ChannelCategory?, servers: List<ChannelServer>) -> Unit,
    onDeleteChannel: (channelId: String) -> Unit
) {
    val sortedBranches = remember(branches) { branches.sortedBy { it.order } }

    var editingChannelId by remember { mutableStateOf<String?>(null) }
    var channelName by remember { mutableStateOf("") }
    var channelImageUrl by remember { mutableStateOf("") }
    var selectedBranchId by remember(sortedBranches) {
        mutableStateOf(sortedBranches.firstOrNull()?.id.orEmpty())
    }
    var filterBranchId by remember { mutableStateOf("") }

    // Dynamic list of servers (id + name + streamType m3u8/ts + streamUrl)
    val serverDrafts = remember {
        mutableStateListOf(
            ChannelServer(
                id = UUID.randomUUID().toString(),
                name = "سيرفر أساسي 1",
                streamType = "m3u8",
                streamUrl = ""
            )
        )
    }

    val filteredChannels = remember(channels, filterBranchId, sortedBranches) {
        if (filterBranchId.isBlank()) {
            channels
        } else {
            val branchObj = sortedBranches.find { it.id == filterBranchId }
            channels.filter { ch ->
                ch.branchId == filterBranchId ||
                    (branchObj != null && ch.branchId.isBlank() && ch.category.equals(branchObj.name, ignoreCase = true))
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Channel Publish / Edit Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingChannelId != null) "تعديل القناة وسيرفراتها" else "نشر قناة جديدة داخل فرع",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (editingChannelId != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FoulCardElevated,
                                modifier = Modifier.clickable {
                                    editingChannelId = null
                                    channelName = ""
                                    channelImageUrl = ""
                                    serverDrafts.clear()
                                    serverDrafts.add(
                                        ChannelServer(
                                            id = UUID.randomUUID().toString(),
                                            name = "سيرفر أساسي 1",
                                            streamType = "m3u8",
                                            streamUrl = ""
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = "إلغاء التعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "اختر الفرع لنشر القناة داخله:",
                        style = MaterialTheme.typography.labelMedium,
                        color = FoulTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    if (sortedBranches.isEmpty()) {
                        Text(
                            text = "يرجى نشر فرع أولاً من قسم (الأفرع وترتيبها) ليتم نشر القنوات داخله.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulLiveRed
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sortedBranches, key = { "target_branch_${it.id}" }) { branch ->
                                val isSelected = selectedBranchId == branch.id
                                val parent = sortedBranches.find { it.id == branch.parentId }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) FoulPurpleBright else FoulCardElevated,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedBranchId = branch.id }
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.White else FoulDivider,
                                            RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Text(
                                        text = if (parent != null) "${parent.name} ⬅ ${branch.name}" else branch.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = "اسم القناة (مثال: beIN Sports 1 HD)",
                        testTag = "dev_channel_name_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = channelImageUrl,
                        onValueChange = { channelImageUrl = it },
                        label = "رابط صورة شعار القناة (http://... أو https://...)",
                        testTag = "dev_channel_image_input"
                    )

                    Spacer(Modifier.height(12.dp))

                    // Multi-Server Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سيرفرات البث المباشر (m3u8 أو ts):",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                serverDrafts.add(
                                    ChannelServer(
                                        id = UUID.randomUUID().toString(),
                                        name = "سيرفر ${serverDrafts.size + 1}",
                                        streamType = "m3u8",
                                        streamUrl = ""
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "إضافة سيرفر آخر",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Dynamic Server Rows
                    serverDrafts.forEachIndexed { idx, server ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FoulCardElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, FoulDivider, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "سيرفر #${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Stream extension selector: m3u8 or ts
                                        listOf("m3u8", "ts").forEach { ext ->
                                            val isExtSelected = server.streamType.equals(ext, ignoreCase = true)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isExtSelected) FoulPurpleBright else FoulCardDark,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        serverDrafts[idx] = server.copy(streamType = ext)
                                                    }
                                                    .border(
                                                        1.dp,
                                                        if (isExtSelected) Color.White else FoulDivider,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                            ) {
                                                Text(
                                                    text = ext.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        if (serverDrafts.size > 1) {
                                            IconButton(
                                                onClick = { serverDrafts.removeAt(idx) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف السيرفر",
                                                    tint = FoulLiveRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                DevOutlinedTextField(
                                    value = server.name,
                                    onValueChange = { newName ->
                                        serverDrafts[idx] = server.copy(name = newName)
                                    },
                                    label = "اسم السيرفر (مثال: FHD 1080p أو سيرفر 1)",
                                    testTag = "dev_server_name_$idx"
                                )

                                Spacer(Modifier.height(6.dp))

                                DevOutlinedTextField(
                                    value = server.streamUrl,
                                    onValueChange = { newUrl ->
                                        val autoType = when {
                                            newUrl.lowercase().contains(".ts") -> "ts"
                                            newUrl.lowercase().contains(".m3u8") -> "m3u8"
                                            else -> server.streamType
                                        }
                                        serverDrafts[idx] = server.copy(streamUrl = newUrl, streamType = autoType)
                                    },
                                    label = "رابط البث المباشر (.m3u8 أو .ts)",
                                    testTag = "dev_server_url_$idx"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val targetBranchObj = sortedBranches.find { it.id == selectedBranchId }
                        ?: sortedBranches.firstOrNull()

                    Button(
                        onClick = {
                            onSaveChannel(
                                editingChannelId,
                                channelName,
                                channelImageUrl,
                                targetBranchObj,
                                serverDrafts.toList()
                            )
                            editingChannelId = null
                            channelName = ""
                            channelImageUrl = ""
                            serverDrafts.clear()
                            serverDrafts.add(
                                ChannelServer(
                                    id = UUID.randomUUID().toString(),
                                    name = "سيرفر أساسي 1",
                                    streamType = "m3u8",
                                    streamUrl = ""
                                )
                            )
                        },
                        enabled = !isActionLoading && channelName.isNotBlank() && targetBranchObj != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoulPurpleBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dev_save_channel_button")
                    ) {
                        Icon(
                            imageVector = if (editingChannelId != null) Icons.Default.Save else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (editingChannelId != null) "حفظ تعديلات القناة والسيرفرات" else "نشر القناة داخل الفرع",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Filter Published Channels by Branch
        item {
            Column {
                Text(
                    text = "القنوات المنشورة (${filteredChannels.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAll = filterBranchId.isBlank()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAll) FoulPurpleBright else FoulCardDark,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { filterBranchId = "" }
                                .border(1.dp, if (isAll) Color.White else FoulDivider, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "كل الأفرع (${channels.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }

                    items(sortedBranches, key = { "filter_br_${it.id}" }) { branch ->
                        val selected = filterBranchId == branch.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) FoulPurpleBright else FoulCardDark,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { filterBranchId = branch.id }
                                .border(1.dp, if (selected) Color.White else FoulDivider, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = branch.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Existing Channels List with Edit & Delete
        items(filteredChannels, key = { it.id }) { channel ->
            val branchNameLabel = sortedBranches.find { it.id == channel.branchId }?.name
                ?: channel.category

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
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, FoulDivider, RoundedCornerShape(10.dp)),
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
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "الفرع: $branchNameLabel • ${channel.servers.size} سيرفر (${channel.servers.joinToString("/") { it.streamType.uppercase() }})",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            editingChannelId = channel.id
                            channelName = channel.name
                            channelImageUrl = channel.imageUrl
                            val matchedBranch = sortedBranches.find {
                                it.id == channel.branchId || it.name.equals(channel.category, ignoreCase = true)
                            }
                            if (matchedBranch != null) {
                                selectedBranchId = matchedBranch.id
                            }
                            serverDrafts.clear()
                            if (channel.servers.isNotEmpty()) {
                                serverDrafts.addAll(channel.servers)
                            } else {
                                serverDrafts.add(
                                    ChannelServer(
                                        id = UUID.randomUUID().toString(),
                                        name = "سيرفر 1",
                                        streamType = "m3u8",
                                        streamUrl = ""
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل القناة",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onDeleteChannel(channel.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف القناة",
                            tint = FoulLiveRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Xtream Codes Accounts Management ("قسم نشر أكواد حساب Xtreme إضافة الهوست واسم مستخدم وكلمة المرور
 * ونشر ويظهر الحساب وتعمل مكتبة بشكل حقيقي فعلي سريع وإمكانية نشر أكثر من حساب وتعديل وحذف")
 */
@Composable
private fun XtreamAccountsManagementTab(
    xtreamAccounts: List<XtreamAccount>,
    isActionLoading: Boolean,
    onSaveXtreamAccount: (existingId: String?, name: String, host: String, username: String, password: String) -> Unit,
    onDeleteXtreamAccount: (accountId: String) -> Unit
) {
    var editingAccountId by remember { mutableStateOf<String?>(null) }
    var accountName by remember { mutableStateOf("") }
    var hostUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingAccountId != null) "تعديل حساب المكتبة المحدد" else "نشر حساب جديد لقسم المكتبة فقط (أفلام ومسلسلات)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "مخصص لقسم المكتبة (الأفلام والمسلسلات) فقط • منعزل تماماً عن قسم IPTV المباشر",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                        if (editingAccountId != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FoulCardElevated,
                                modifier = Modifier.clickable {
                                    editingAccountId = null
                                    accountName = ""
                                    hostUrl = ""
                                    username = ""
                                    password = ""
                                }
                            ) {
                                Text(
                                    text = "إلغاء التعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = "اسم السيرفر / الحساب (مثال: سيرفر مكتبة الأفلام 1)",
                        testTag = "dev_xtream_name_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = hostUrl,
                        onValueChange = { hostUrl = it },
                        label = "الهوست (Host) مثال: http://example.com:8080",
                        testTag = "dev_xtream_host_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "اسم المستخدم (Username)",
                        testTag = "dev_xtream_username_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "كلمة المرور (Password)",
                        testTag = "dev_xtream_password_input"
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveXtreamAccount(
                                editingAccountId,
                                accountName,
                                hostUrl,
                                username,
                                password
                            )
                            editingAccountId = null
                            accountName = ""
                            hostUrl = ""
                            username = ""
                            password = ""
                        },
                        enabled = !isActionLoading &&
                            hostUrl.isNotBlank() &&
                            username.isNotBlank() &&
                            password.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoulPurpleBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dev_save_xtream_button")
                    ) {
                        Icon(
                            imageVector = if (editingAccountId != null) Icons.Default.Save else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (editingAccountId != null) "حفظ تعديل حساب Xtreme" else "نشر الحساب وتفعيل المكتبة فوراً",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "حسابات Xtreme المنشورة والفعالة (${xtreamAccounts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(xtreamAccounts, key = { it.id }) { account ->
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FoulPurpleDeep)
                            .border(1.dp, FoulDivider, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FoulLiveGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "نشط",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Host: ${account.host}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "User: ${account.username} • Pass: ${account.password}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            editingAccountId = account.id
                            accountName = account.name
                            hostUrl = account.host
                            username = account.username
                            password = account.password
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الحساب",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onDeleteXtreamAccount(account.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الحساب",
                            tint = FoulLiveRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 9: IPTV Accounts Management ("قسم في إعدادات المطور نشر حساب IPTV ونشر هوست واسم مستخدم وكلمة مرور
 * وإضافة اسم الحساب ونشر وتظهر القنوات حسب الحساب وإمكانية التنقل بينهما")
 */
@Composable
private fun IptvAccountsManagementTab(
    iptvAccounts: List<XtreamAccount>,
    isActionLoading: Boolean,
    onSaveIptvAccount: (existingId: String?, name: String, host: String, username: String, password: String) -> Unit,
    onDeleteIptvAccount: (accountId: String) -> Unit
) {
    var editingAccountId by remember { mutableStateOf<String?>(null) }
    var accountName by remember { mutableStateOf("") }
    var hostUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulPurpleBright, FoulLiveGreen)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (editingAccountId != null) "تعديل حساب IPTV المحدد" else "نشر حساب IPTV مستقل (خاص بقسم IPTV فقط)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Surface(
                                    color = FoulLiveGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "منعزل عن المكتبة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FoulLiveGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "هذا القسم منعزل تماماً عن قسم المكتبة • الحسابات المنشورة هنا تظهر فقط في قسم IPTV بالشريط السفلي مع تصنيفاتها وقنواتها 3×3",
                                style = MaterialTheme.typography.bodySmall,
                                color = FoulTextSecondary
                            )
                        }
                        if (editingAccountId != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FoulCardElevated,
                                modifier = Modifier.clickable {
                                    editingAccountId = null
                                    accountName = ""
                                    hostUrl = ""
                                    username = ""
                                    password = ""
                                }
                            ) {
                                Text(
                                    text = "إلغاء التعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    DevOutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = "اسم الحساب (مثال: سيرفر IPTV الذهبي 1)",
                        testTag = "dev_iptv_name_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = hostUrl,
                        onValueChange = { hostUrl = it },
                        label = "الهوست (Host) مثال: http://example.com:8080",
                        testTag = "dev_iptv_host_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "اسم المستخدم (Username)",
                        testTag = "dev_iptv_username_input"
                    )

                    Spacer(Modifier.height(8.dp))

                    DevOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "كلمة المرور (Password)",
                        testTag = "dev_iptv_password_input"
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveIptvAccount(
                                editingAccountId,
                                accountName,
                                hostUrl,
                                username,
                                password
                            )
                            editingAccountId = null
                            accountName = ""
                            hostUrl = ""
                            username = ""
                            password = ""
                        },
                        enabled = !isActionLoading &&
                            hostUrl.isNotBlank() &&
                            username.isNotBlank() &&
                            password.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoulPurpleBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dev_save_iptv_button")
                    ) {
                        Icon(
                            imageVector = if (editingAccountId != null) Icons.Default.Save else Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (editingAccountId != null) "حفظ تعديل حساب IPTV" else "نشر حساب IPTV وتفعيل القنوات فوراً",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "حسابات IPTV المنشورة في قسم IPTV (${iptvAccounts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(iptvAccounts, key = { "dev_iptv_${it.id}" }) { account ->
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FoulPurpleDeep)
                            .border(1.dp, FoulLiveGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = FoulLiveGreen
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FoulLiveGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "IPTV مباشر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulLiveGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Host: ${account.host}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "User: ${account.username} • Pass: ${account.password}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            editingAccountId = account.id
                            accountName = account.name
                            hostUrl = account.host
                            username = account.username
                            password = account.password
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل حساب IPTV",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onDeleteIptvAccount(account.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف حساب IPTV",
                            tint = FoulLiveRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: App Version & Forced Update Management ("قسم إصدارات ويقبل فقط هاذهي نسخة تطبيق 1.0
 * وإمكانية نشر تحديث جديد إجباري بشكل إضافة نص التحديث وإضافة الإصدار إلي فقط تقبلة قاعدة بيانات
 * وإضافة رابط تحميل التطبيق بشكل اختياري وإضافة رابط قناة تيلجرام")
 */
@Composable
private fun AppVersionsManagementTab(
    versionConfig: AppVersionConfig,
    isActionLoading: Boolean,
    onSaveVersionConfig: (requiredVersion: String, updateText: String, updateUrl: String, telegramUrl: String, lockOldVersions: Boolean) -> Unit
) {
    var requiredVersion by remember(versionConfig.requiredVersion) {
        mutableStateOf(versionConfig.requiredVersion)
    }
    var updateText by remember(versionConfig.updateText) {
        mutableStateOf(versionConfig.updateText)
    }
    var updateUrl by remember(versionConfig.updateUrl) {
        mutableStateOf(versionConfig.updateUrl)
    }
    var telegramUrl by remember(versionConfig.telegramUrl) {
        mutableStateOf(versionConfig.telegramUrl)
    }
    var lockOldVersions by remember(versionConfig.lockOldVersions) {
        mutableStateOf(versionConfig.lockOldVersions)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current App Build Info Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulPurpleDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نسخة التطبيق الحالية المثبتة: ${AppVersionConfig.APP_CURRENT_VERSION}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "الإصدار المسموح به حالياً في القاعدة: ${versionConfig.requiredVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (versionConfig.isForcedUpdateLocked) FoulLiveRed else FoulLiveGreen.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (versionConfig.isForcedUpdateLocked) "مقفل للتحديث" else "مفتوح (${AppVersionConfig.APP_CURRENT_VERSION})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Version & Forced Update Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "إدارة الإصدار المسموح والتحديث الإجباري",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "عند كتابة رقم إصدار مختلف عن (${AppVersionConfig.APP_CURRENT_VERSION}) ونشره، لن يفتح التطبيق إطلاقاً للمستخدمين على النسخ القديمة وستظهر لهم شاشة التحديث الإجباري مع زر قناة التيلجرام.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )

                    Spacer(Modifier.height(12.dp))

                    // Quick Version Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { requiredVersion = AppVersionConfig.APP_CURRENT_VERSION },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "قبول نسخة ${AppVersionConfig.APP_CURRENT_VERSION} الحالية",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { requiredVersion = "2.0" },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "طلب تحديث لإصدار 2.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = requiredVersion,
                        onValueChange = { requiredVersion = it },
                        label = "الإصدار الوحيد الذي تقبله قاعدة البيانات (مثال: 1.2.1 أو 2.0)",
                        testTag = "dev_version_required_input"
                    )

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = updateText,
                        onValueChange = { updateText = it },
                        label = "نص رسالة التحديث للمستخدمين",
                        singleLine = false,
                        testTag = "dev_version_message_input"
                    )

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = updateUrl,
                        onValueChange = { updateUrl = it },
                        label = "رابط تحميل التحديث المباشر (اختياري - اتركه فارغاً إن لم يوجد)",
                        testTag = "dev_version_download_url_input"
                    )

                    Spacer(Modifier.height(10.dp))

                    DevOutlinedTextField(
                        value = telegramUrl,
                        onValueChange = { telegramUrl = it },
                        label = "رابط قناة التيلجرام الرسمية (يظهر كزر شعار التيلجرام)",
                        testTag = "dev_version_telegram_url_input"
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FoulCardElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل الإغلاق الإجباري للنسخ القديمة",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "لا يفتح التطبيق إطلاقاً إلا للإصدار المحدد أعلاه",
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTextSecondary
                            )
                        }
                        Switch(
                            checked = lockOldVersions,
                            onCheckedChange = { lockOldVersions = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FoulPurpleBright,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = FoulCardDark
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveVersionConfig(
                                requiredVersion,
                                updateText,
                                updateUrl,
                                telegramUrl,
                                lockOldVersions
                            )
                        },
                        enabled = !isActionLoading && requiredVersion.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoulPurpleBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dev_save_version_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "نشر الإصدار والتحديث الإجباري الآن",
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

/**
 * Full-screen Blocking Mandatory Update Screen shown to users when the published version in Firebase
 * does not match `AppVersionConfig.APP_CURRENT_VERSION` ("1.0").
 * ("ولا يفتح تطبيق إطلاقاً فقط للنسخة إلي نشرت وعند نشر تظهر لجميع المستخدمين القديمين بشكل يرجى تحديث تطبيق")
 */
@Composable
fun ForcedUpdateLockScreen(
    versionConfig: AppVersionConfig,
    isDevLoginSheetOpen: Boolean,
    isLoggingInDeveloper: Boolean,
    devLoginError: String?,
    onOpenDeveloperSheet: () -> Unit,
    onDismissDeveloperSheet: () -> Unit,
    onSubmitDeveloperLogin: (String, String) -> Unit
) {
    val context = LocalContext.current

    if (SHOW_DEVELOPER_PANEL_ENTRY && isDevLoginSheetOpen) {
        DeveloperLoginBottomSheet(
            isLoading = isLoggingInDeveloper,
            errorMessage = devLoginError,
            onDismiss = onDismissDeveloperSheet,
            onLogin = onSubmitDeveloperLogin
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("forced_update_lock_screen")
    ) {
        // Top Bar (Developer Access icon hidden unless SHOW_DEVELOPER_PANEL_ENTRY is true)
        FoulCompactTopBar(
            title = "تحديث إجباري مطلوب",
            subtitle = "Foul TV",
            leadingIcon = Icons.Default.SystemUpdate,
            actions = {
                if (SHOW_DEVELOPER_PANEL_ENTRY) {
                    CompactTopBarIconButton(
                        icon = Icons.Default.AdminPanelSettings,
                        contentDescription = "دخول المطور",
                        onClick = onOpenDeveloperSheet,
                        modifier = Modifier.testTag("forced_update_dev_button")
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(FoulPurpleBright, Color.White.copy(alpha = 0.45f), FoulPurpleDeep)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Update Icon Badge
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(FoulPurpleBright, FoulPurpleDeep)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "يرجى تحديث التطبيق",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    // Version comparison pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FoulPurpleDeep,
                        modifier = Modifier.border(1.dp, FoulDivider, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FoulGoldStar,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "نسختك الحالية: ${AppVersionConfig.APP_CURRENT_VERSION} • المطلوب: ${versionConfig.requiredVersion}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OrderedColorfulDivider(thickness = 1.dp)

                    Spacer(Modifier.height(16.dp))

                    // Developer's custom update message
                    Text(
                        text = versionConfig.updateText.ifBlank {
                            "تم إصدار نسخة جديدة من تطبيق Foul TV. يرجى تحديث التطبيق إلى الإصدار ${versionConfig.requiredVersion} للاستمرار في المشاهدة."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(22.dp))

                    // Optional Direct Download Button (shown if developer provided an updateUrl)
                    if (versionConfig.updateUrl.isNotBlank()) {
                        Button(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionConfig.updateUrl.trim()))
                                    context.startActivity(intent)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoulPurpleBright,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("forced_update_download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "تحميل التحديث الجديد الآن",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                    }

                    // Prominent Official Telegram Channel Button ("وإضافة رابط قناة تيلجرام للضهور للمستخدمين بشكل زر شعار قناة تيلجرام بشكل احترافي جدا")
                    val tgUrl = versionConfig.telegramUrl.ifBlank { "https://t.me/jdj_q" }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FoulPurpleDeep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tgUrl))
                                    context.startActivity(intent)
                                }
                            }
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("forced_update_telegram_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TelegramVectorBadge(sizeDp = 32)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "قناة التيلجرام الرسمية لتحميل التحديث",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (SHOW_DEVELOPER_PANEL_ENTRY) {
                        Spacer(Modifier.height(14.dp))

                        // Prominent Developer Control Panel Access Button on Forced Update Screen
                        // ("اجعل عند اجراءات تحديث إجباري إمكانية الضغط ودخول للوحة تحكم المطور بعد ما يكتب المعلومات البريد وكلمة المرور")
                        OutlinedButton(
                            onClick = onOpenDeveloperSheet,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(
                                    width = 1.dp,
                                    color = FoulPurpleBright,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("forced_update_dev_login_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "دخول لوحة تحكم المطور",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "دخول لوحة تحكم المطور",
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
 * Tab 4: Developer User Contact Section ("قسم جديد تواصل المستخدمين وإمكانية الرد على كل شخص
 * والوصول له رسالة عند سحب والرد على رسالة تظهر ذلك عند السحب ليكون بشكل رد منفصل
 * يعني تظهر جميع الرسائل في مكان واحد ولاكن لا تظهر ردود المطور للكل فقط للشخص إلي يسحب بشكل سحب ويرد عليه")
 */
@Composable
private fun DeveloperUserMessagesTab(
    messages: List<ContactMessageItem>,
    isActionLoading: Boolean,
    onSendDeveloperReply: (targetMessage: ContactMessageItem, replyText: String) -> Unit,
    onRefreshMessages: () -> Unit
) {
    var selectedReplyTarget by remember { mutableStateOf<ContactMessageItem?>(null) }
    var replyText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val timeFormatter = remember {
        SimpleDateFormat("yyyy/MM/dd • hh:mm a", Locale("ar"))
    }

    // Fast real-time sync every 2.5 seconds while Developer User Messages tab is open
    LaunchedEffect(Unit) {
        onRefreshMessages()
        while (true) {
            delay(2500L)
            onRefreshMessages()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("dev_user_messages_tab_content")
    ) {
        // Info Header Banner explaining Unified Stream + Private Swipe-to-Reply
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .border(1.dp, FoulDivider, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FoulPurpleDeep, FoulCardDark)
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedBotIcon(size = 42.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "صندوق رسائل تواصل المستخدمين الموحد",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "تظهر هنا جميع رسائل المستخدمين في مكان واحد • اسحب أي رسالة للرد عليها بشكل خاص ومنفصل لصاحبها فقط",
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshMessages,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FoulCardElevated)
                        .border(1.dp, FoulDivider, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث رسائل المستخدمين",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = FoulTextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "لا توجد رسائل واردة من المستخدمين حتى الآن",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "عندما يرسل أي مستخدم رسالة من قسم (تواصل معنا)، ستظهر هنا فوراً ويمكنك سحبها للرد عليه بشكل خاص.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Unified Feed of All User Messages & Developer Replies
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isSelectedForReply = selectedReplyTarget?.id == msg.id
                    DeveloperSwipeableMessageCard(
                        msg = msg,
                        isSelectedForReply = isSelectedForReply,
                        formattedTime = runCatching { timeFormatter.format(Date(msg.timestamp)) }.getOrDefault(""),
                        onSwipeToReply = {
                            selectedReplyTarget = msg
                        }
                    )
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.dp)

        // Bottom Developer Reply Bar (Activates when a message is swiped!)
        val target = selectedReplyTarget
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulCardDark)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (target != null) {
                // Active Swiped Message Preview Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FoulPurpleDeep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "رد خاص ومنفصل إلى: ${target.senderName} (${target.senderEmail})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = target.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "لا يظهر هذا الرد إلا لهذا المستخدم فقط",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulLiveGreen
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedReplyTarget = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إلغاء تحديد الرسالة",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Hint when no message has been swiped yet
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FoulCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulDivider, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "اسحب أي رسالة مستخدم لليمين أو اليسار للرد عليه بشكل خاص ومنفصل",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    enabled = target != null && !isActionLoading,
                    placeholder = {
                        Text(
                            text = if (target != null) {
                                "اكتب ردك الخاص لـ ${target.senderName}..."
                            } else {
                                "اسحب رسالة من الأعلى أولاً لتفعيل الرد الخاص..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = FoulTextSecondary,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = FoulDivider,
                        disabledBorderColor = FoulDivider,
                        focusedContainerColor = FoulCardElevated,
                        unfocusedContainerColor = FoulCardElevated,
                        disabledContainerColor = FoulCardElevated.copy(alpha = 0.6f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dev_reply_message_input")
                )

                IconButton(
                    onClick = {
                        val activeTarget = selectedReplyTarget
                        if (activeTarget != null && replyText.isNotBlank()) {
                            onSendDeveloperReply(activeTarget, replyText)
                            replyText = ""
                            selectedReplyTarget = null
                        }
                    },
                    enabled = target != null && replyText.isNotBlank() && !isActionLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (target != null && replyText.isNotBlank()) FoulPurpleBright else FoulCardElevated
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .testTag("dev_send_reply_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال الرد الخاص",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperSwipeableMessageCard(
    msg: ContactMessageItem,
    isSelectedForReply: Boolean,
    formattedTime: String,
    onSwipeToReply: () -> Unit
) {
    val isDeveloperReply = msg.isFromBotOrAdmin
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = dragOffsetX, label = "dev_swipe_offset")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(msg.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffsetX) > 60f) {
                            onSwipeToReply()
                        }
                        dragOffsetX = 0f
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-140f, 140f)
                    }
                )
            }
    ) {
        // Reply Badge revealed behind the card when swiping
        if (abs(animatedOffsetX) > 12f) {
            Row(
                modifier = Modifier
                    .align(if (animatedOffsetX > 0f) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FoulPurpleBright)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "رد خاص",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "رد خاص",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSelectedForReply -> FoulPurpleDeep
                    isDeveloperReply -> Color(0xFF121829)
                    else -> FoulCardDark
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .border(
                    width = if (isSelectedForReply) 1.6.dp else 1.dp,
                    color = when {
                        isSelectedForReply -> Color(0xFF38BDF8)
                        isDeveloperReply -> Color(0xFF38BDF8).copy(alpha = 0.4f)
                        else -> FoulDivider
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("dev_msg_card_${msg.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                        if (isDeveloperReply) {
                            AnimatedBotIcon(size = 40.dp)
                        } else {
                            InAppCharacterAvatar(
                                avatarSpec = SocialFirebaseRepository.getAvatarById(msg.senderAvatarId),
                                size = 40.dp,
                                isSelected = isSelectedForReply
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = msg.senderName.ifBlank { "مستخدم" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDeveloperReply) Color(0xFF38BDF8) else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (isDeveloperReply) FoulLiveGreen.copy(alpha = 0.22f) else FoulPurpleBright.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = if (isDeveloperReply) "رد المطور الخاص" else "رسالة مستخدم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (msg.senderEmail.isNotBlank()) {
                                Text(
                                    text = msg.senderEmail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Swipe / Tap Reply Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelectedForReply) Color(0xFF38BDF8) else FoulCardElevated,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onSwipeToReply)
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                            .testTag("dev_reply_btn_${msg.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "رد على هذا المستخدم",
                                tint = if (isSelectedForReply) FoulBlackBg else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "رد",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelectedForReply) FoulBlackBg else Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Quoted message box when this item is a reply to a swiped message
                if (msg.replyToText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FoulBlackBg.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "رد خاص ومباشر على رسالة: ${msg.replyToSenderName.ifBlank { "المستخدم" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = msg.replyToText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اسحب الرسالة للرد الخاص ⇄",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = FoulTextSecondary
                    )
                }
            }
        }
    }
}


@Composable
private fun DevOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = FoulTextSecondary) },
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = FoulDivider,
            focusedContainerColor = FoulCardElevated,
            unfocusedContainerColor = FoulCardElevated,
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

/**
 * Tab 5: App Control ("قسم تحكم في التطبيق وإمكانية إخفاء زر أيقونة المشاهدة والايكات من القنوات وبشكل online حسب قاعدة بيانات")
 */
@Composable
private fun DeveloperAppControlTab(
    showChannelViewsAndLikes: Boolean,
    showAdsInApp: Boolean,
    onlineUsersCount: Int,
    isActionLoading: Boolean,
    channelsCount: Int,
    branchesCount: Int,
    onToggleShowChannelViewsAndLikes: (Boolean) -> Unit,
    onToggleShowAdsInApp: (Boolean) -> Unit,
    onRefreshOnlineUsers: () -> Unit
) {
    // Continuously sync live online users count from `foul-aeed9` while this tab is open
    LaunchedEffect(Unit) {
        onRefreshOnlineUsers()
        while (true) {
            delay(8_000L)
            onRefreshOnlineUsers()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dev_app_control_tab"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF38BDF8), FoulPurpleBright, FoulDivider)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(FoulPurpleDeep.copy(alpha = 0.85f), FoulCardDark)
                            )
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(FoulPurpleBright)
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "قسم تحكم في التطبيق (Online)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "تحكم مباشر عبر قاعدة البيانات السحابية في خصائص التطبيق وعدّاد المتواجدين الآن في الوقت الفعلي",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                }
            }
        }

        // Real-Time Online Users Numeric Counter Card ("خيار بشكل فقط عداد بشكل أرقام ويظهر عدد المتواجدين الآن بشكل فعلي حقيقي حسب قاعدة بيانات الأخرى إلي مربوطة بها قسم تواصل معنا")
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulLiveGreen, FoulPurpleBright, Color(0xFF38BDF8))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("dev_online_users_counter_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    FoulPurpleDeep.copy(alpha = 0.65f),
                                    FoulCardDark
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(FoulLiveGreen)
                            )
                            Text(
                                text = "المتواجدون الآن في التطبيق",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "عداد حي ومباشر متصل بقاعدة بيانات تواصل معنا (foul-aeed9)",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pure Numeric Counter Badge ("فقط عداد بشكل أرقام")
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF07080F),
                            modifier = Modifier
                                .border(
                                    width = 1.5.dp,
                                    color = FoulLiveGreen,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Text(
                                text = "$onlineUsersCount",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .testTag("dev_online_users_count_value")
                            )
                        }

                        IconButton(
                            onClick = onRefreshOnlineUsers,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FoulCardElevated)
                                .border(1.dp, FoulDivider, CircleShape)
                                .testTag("dev_refresh_online_users_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث عداد المتواجدين الآن",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Control Card: Hide / Show Channel Views & Likes Online
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        color = if (showChannelViewsAndLikes) FoulLiveGreen.copy(alpha = 0.6f) else FoulLiveRed.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (showChannelViewsAndLikes) {
                                            FoulLiveGreen.copy(alpha = 0.2f)
                                        } else {
                                            FoulLiveRed.copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (showChannelViewsAndLikes) FoulLiveGreen else FoulLiveRed,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showChannelViewsAndLikes) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (showChannelViewsAndLikes) FoulLiveGreen else FoulLiveRed,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "عداد المشاهدات وزر اللايكات في القنوات",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (showChannelViewsAndLikes) {
                                        "الحالة الحالية: ظاهر ومفعل في جميع القنوات ومشغل البث (Online)"
                                    } else {
                                        "الحالة الحالية: مخفي تماماً من جميع القنوات ومشغل البث (Online)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (showChannelViewsAndLikes) FoulLiveGreen else FoulGoldStar,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = showChannelViewsAndLikes,
                            onCheckedChange = { enabled ->
                                onToggleShowChannelViewsAndLikes(enabled)
                            },
                            enabled = !isActionLoading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FoulLiveGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = FoulLiveRed.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("dev_toggle_channel_views_likes_switch")
                        )
                    }

                    OrderedColorfulDivider(thickness = 1.dp)

                    Text(
                        text = "يمكنك إخفاء أو إظهار أيقونة عداد المشاهدات وزر الإعجاب (لايك) من بطاقات القنوات في قسم الرئيسية ومن مشغل البث المباشر فوراً لجميع مستخدمي التطبيق عبر قاعدة البيانات السحابية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onToggleShowChannelViewsAndLikes(true) },
                            enabled = !isActionLoading && !showChannelViewsAndLikes,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoulLiveGreen,
                                disabledContainerColor = FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_show_views_likes_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "إظهار المشاهدات واللايكات",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onToggleShowChannelViewsAndLikes(false) },
                            enabled = !isActionLoading && showChannelViewsAndLikes,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoulLiveRed,
                                disabledContainerColor = FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_hide_views_likes_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "إخفاء من جميع القنوات",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Ads Control Card: Enable or Completely Remove/Hide All Ads Across the App
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        color = if (showAdsInApp) FoulLiveGreen.copy(alpha = 0.6f) else FoulLiveRed.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("dev_ads_control_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (showAdsInApp) {
                                            FoulLiveGreen.copy(alpha = 0.2f)
                                        } else {
                                            FoulLiveRed.copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (showAdsInApp) FoulLiveGreen else FoulLiveRed,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showAdsInApp) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (showAdsInApp) FoulLiveGreen else FoulLiveRed,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إعلانات التطبيق (تفعيل / إزالة نهائية)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (showAdsInApp) {
                                        "الحالة الحالية: الإعلانات مفعلة وتظهر في التطبيق (Banner + Native)"
                                    } else {
                                        "الحالة الحالية: تم إزالة وإخفاء الإعلانات تماماً ولن تظهر إطلاقاً"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (showAdsInApp) FoulLiveGreen else FoulGoldStar,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = showAdsInApp,
                            onCheckedChange = { enabled ->
                                onToggleShowAdsInApp(enabled)
                            },
                            enabled = !isActionLoading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FoulLiveGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = FoulLiveRed.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("dev_toggle_ads_switch")
                        )
                    }

                    OrderedColorfulDivider(thickness = 1.dp)

                    Text(
                        text = "تحكم شامل وفوري في إعلانات التطبيق (شريط إعلان البانر السفلي + شاشة إعلان Native الانتقالية قبل تشغيل القنوات والأفلام والمسلسلات). عند الإخفاء لن يظهر أي إعلان إطلاقاً في التطبيق.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onToggleShowAdsInApp(true) },
                            enabled = !isActionLoading && !showAdsInApp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoulLiveGreen,
                                disabledContainerColor = FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_enable_ads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تفعيل الإعلانات",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onToggleShowAdsInApp(false) },
                            enabled = !isActionLoading && showAdsInApp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoulLiveRed,
                                disabledContainerColor = FoulCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dev_disable_ads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "إزالة وإخفاء الإعلانات",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Database Overview Card
        item {
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
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$branchesCount",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "الأفرع المنشورة",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(FoulDivider)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$channelsCount",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "القنوات المباشرة",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(FoulDivider)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (showChannelViewsAndLikes) "مفعل" else "مخفي",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (showChannelViewsAndLikes) FoulLiveGreen else FoulLiveRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "عداد القنوات",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(FoulDivider)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (showAdsInApp) "مفعلة" else "مخفية",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (showAdsInApp) FoulLiveGreen else FoulLiveRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "الإعلانات",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                }
            }
        }
    }
}
