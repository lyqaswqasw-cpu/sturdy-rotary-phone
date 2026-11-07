package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppVersionConfig
import com.example.data.model.SocialUserProfile
import com.example.data.repository.SocialFirebaseRepository
import com.example.ui.components.CompactTopBarIconButton
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
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextSecondary

private enum class SettingsSubPage {
    NONE,
    PRIVACY_POLICY,
    APP_DESCRIPTION
}

/**
 * Custom Bespoke Broadcast Icon Types for Settings Cards
 */
private enum class SettingsProIconType {
    SUPPORT_HEADSET,
    TELEGRAM_PLANE,
    APP_ARCHITECTURE,
    PRIVACY_SHIELD,
    DEVELOPER_CONSOLE
}

/**
 * Master visibility toggle for Developer Control Panel entry points across Settings,
 * Mandatory Update Screen, and Top Bar icons.
 * Set to `false` to hide all developer entry points while keeping all code intact.
 * Flip back to `true` when the user asks to show the Developer Control Panel ("أظهر لوحة تحكم المطور").
 */
const val SHOW_DEVELOPER_PANEL_ENTRY = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    telegramUrl: String = "https://t.me/jdj_q",
    currentUser: SocialUserProfile? = null,
    isDevLoginSheetOpen: Boolean = false,
    isLoggingInDeveloper: Boolean = false,
    devLoginError: String? = null,
    onOpenContactUsScreen: () -> Unit = {},
    onOpenDeveloperSheet: () -> Unit = {},
    onDismissDeveloperSheet: () -> Unit = {},
    onSubmitDeveloperLogin: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val targetUrl = telegramUrl.ifBlank { "https://t.me/jdj_q" }
    var activeSubPage by remember { mutableStateOf(SettingsSubPage.NONE) }

    // Independent Full-Screen Page 1: Privacy Policy
    if (activeSubPage == SettingsSubPage.PRIVACY_POLICY) {
        PrivacyPolicyIndependentScreen(
            onBack = { activeSubPage = SettingsSubPage.NONE }
        )
        return
    }

    // Independent Full-Screen Page 2: App Description
    if (activeSubPage == SettingsSubPage.APP_DESCRIPTION) {
        AppDescriptionIndependentScreen(
            onBack = { activeSubPage = SettingsSubPage.NONE }
        )
        return
    }

    // Half-Screen Developer Login Modal Bottom Sheet (only active when developer entry is enabled)
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
    ) {
        FoulSmartTopBar(
            telegramUrl = targetUrl,
            telegramTestTag = "settings_top_telegram_button"
        )

        FoulSectionSubBar(
            title = "الإعدادات والنظام",
            subtitle = "مركز التحكم، الدعم المباشر، القناة الرسمية والخصوصية",
            actions = {
                if (SHOW_DEVELOPER_PANEL_ENTRY) {
                    CompactTopBarIconButton(
                        icon = Icons.Default.AdminPanelSettings,
                        contentDescription = "لوحة تحكم المطور",
                        onClick = onOpenDeveloperSheet,
                        modifier = Modifier.testTag("settings_top_dev_icon_button")
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_list"),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 0. Compact FOUL TV Identity & Version Strip (Not oversized)
            item(key = "settings_hero") {
                SettingsIdentityHeroBanner(
                    currentUser = currentUser,
                    versionName = AppVersionConfig.APP_CURRENT_VERSION
                )
            }

            // Section 1 Header: التواصل والقناة الرسمية
            item(key = "header_communication") {
                SettingsGroupHeader(
                    title = "التواصل المباشر والقناة الرسمية",
                    accentColor = FoulTeamAwayCyan
                )
            }

            // 1. Contact Us ("تواصل معنا") Compact Luxury Card
            item(key = "card_contact_us") {
                ProSettingsOptionCard(
                    title = "تواصل معنا • الدعم المباشر",
                    subtitle = if (currentUser != null) {
                        "حسابك النشط: ${currentUser.username}"
                    } else {
                        "مراسلة فريق الدعم الفني واستلام الردود الفورية"
                    },
                    iconType = SettingsProIconType.SUPPORT_HEADSET,
                    iconBgColors = listOf(Color(0xFF00B4DB), FoulPurplePrimary),
                    accentColor = FoulTeamAwayCyan,
                    badgeText = "متصل 24/7",
                    badgeColor = FoulLiveGreen,
                    onClick = onOpenContactUsScreen,
                    testTag = "settings_contact_us_button"
                )
            }

            // 2. Official Telegram Channel Compact Card
            item(key = "card_telegram") {
                ProSettingsOptionCard(
                    title = "قناة التيلجرام الرسمية",
                    subtitle = "آخر التحديثات وتنبيهات المباريات (${targetUrl.removePrefix("https://").removePrefix("http://")})",
                    iconType = SettingsProIconType.TELEGRAM_PLANE,
                    iconBgColors = listOf(Color(0xFF29B6F6), Color(0xFF0277BD)),
                    accentColor = Color(0xFF29B6F6),
                    badgeText = "رسمي",
                    badgeColor = Color(0xFF29B6F6),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    },
                    testTag = "settings_telegram_button"
                )
            }

            // Section 2 Header: عن التطبيق والسياسات
            item(key = "header_info") {
                SettingsGroupHeader(
                    title = "معلومات المنظومة والأمان والخصوصية",
                    accentColor = FoulPurpleGlow
                )
            }

            // 3. App Description Card
            item(key = "card_description") {
                ProSettingsOptionCard(
                    title = "وصف عن التطبيق",
                    subtitle = "تعرّف على مميزات وأقسام ومنظومة Foul TV الكاملة",
                    iconType = SettingsProIconType.APP_ARCHITECTURE,
                    iconBgColors = listOf(FoulPurpleBright, FoulPurpleDeep),
                    accentColor = FoulPurpleGlow,
                    badgeText = "دليل شامل",
                    badgeColor = FoulPurpleBright,
                    onClick = { activeSubPage = SettingsSubPage.APP_DESCRIPTION },
                    testTag = "settings_app_description_button"
                )
            }

            // 4. Privacy Policy Card
            item(key = "card_privacy") {
                ProSettingsOptionCard(
                    title = "سياسة الخصوصية والأمان",
                    subtitle = "معايير التشفير، حماية البيانات وحقوق المستخدم",
                    iconType = SettingsProIconType.PRIVACY_SHIELD,
                    iconBgColors = listOf(Color(0xFF00897B), FoulPurpleDeep),
                    accentColor = FoulLiveGreen,
                    badgeText = "مشفّر وآمن",
                    badgeColor = FoulLiveGreen,
                    onClick = { activeSubPage = SettingsSubPage.PRIVACY_POLICY },
                    testTag = "settings_privacy_policy_button"
                )
            }

            if (SHOW_DEVELOPER_PANEL_ENTRY) {
                // Section 3 Header: الإدارة والنظام
                item(key = "header_developer") {
                    SettingsGroupHeader(
                        title = "الإدارة السحابية ولوحة المطور",
                        accentColor = FoulGoldStar
                    )
                }

                // 5. Developer Control Panel Card (Version 1.1)
                item(key = "card_developer") {
                    ProSettingsOptionCard(
                        title = "لوحة تحكم المطور",
                        subtitle = "إدارة الباقات، القنوات، حسابات IPTV والمكتبة والمباريات",
                        iconType = SettingsProIconType.DEVELOPER_CONSOLE,
                        iconBgColors = listOf(FoulPurpleAccent, FoulPurpleDeep),
                        accentColor = FoulGoldStar,
                        badgeText = "نسخة ${AppVersionConfig.APP_CURRENT_VERSION}",
                        badgeColor = FoulPurplePrimary,
                        onClick = onOpenDeveloperSheet,
                        testTag = "settings_developer_panel_button"
                    )
                }
            }
        }
    }
}

/**
 * Compact, sleek single-row Identity Banner at the top of SettingsScreen
 */
@Composable
private fun SettingsIdentityHeroBanner(
    currentUser: SocialUserProfile?,
    versionName: String
) {
    Card(
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    FoulPurpleBright.copy(alpha = 0.65f),
                    FoulPurplePrimary.copy(alpha = 0.40f),
                    FoulDivider
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            FoulPurpleDeep.copy(alpha = 0.78f),
                            FoulCardDark,
                            FoulBlackBg
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
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
                        .background(FoulBlackBg)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(FoulPurpleGlow, FoulPurplePrimary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_foul_tv_launcher),
                        contentDescription = "شعار FOUL TV",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FOUL TV",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                letterSpacing = 0.6.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Surface(
                            color = FoulPurplePrimary,
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(0.7.dp, FoulPurpleBright)
                        ) {
                            Text(
                                text = "PRO $versionName",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (currentUser != null) {
                            "متصل بحساب: ${currentUser.username} • مزامنة سحابية فورية"
                        } else {
                            "منصة البث المباشر، IPTV، السينما والمباريات العالمية"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = FoulTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = FoulBlackBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.9.dp, FoulDivider)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FoulLiveGreen)
                    )
                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupHeader(
    title: String,
    accentColor: Color = FoulPurpleBright
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Compact Custom Multi-Layered Canvas Vector Icon Badge for Settings Cards
 */
@Composable
private fun SettingsProIconBadge(
    iconType: SettingsProIconType,
    gradientColors: List<Color>,
    accentGlow: Color
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .border(
                width = 0.9.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.42f), accentGlow.copy(alpha = 0.35f))
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 1.9.dp.toPx()

            when (iconType) {
                SettingsProIconType.SUPPORT_HEADSET -> {
                    // Professional Broadcast Support Headset + Live Mic Wave
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.14f, h * 0.12f),
                        size = Size(w * 0.72f, h * 0.64f),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Left & Right ear pads
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.08f, h * 0.42f),
                        size = Size(w * 0.16f, h * 0.30f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.76f, h * 0.42f),
                        size = Size(w * 0.16f, h * 0.30f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    // Boom microphone arm to center-bottom
                    val micPath = Path().apply {
                        moveTo(w * 0.84f, h * 0.70f)
                        quadraticTo(w * 0.82f, h * 0.90f, w * 0.54f, h * 0.88f)
                    }
                    drawPath(
                        path = micPath,
                        color = Color.White,
                        style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round)
                    )
                    // Mic capsule dot
                    drawCircle(
                        color = FoulLiveGreen,
                        radius = w * 0.07f,
                        center = Offset(w * 0.48f, h * 0.88f)
                    )
                }

                SettingsProIconType.TELEGRAM_PLANE -> {
                    val path = Path().apply {
                        moveTo(w * 0.92f, h * 0.14f)
                        lineTo(w * 0.08f, h * 0.47f)
                        lineTo(w * 0.34f, h * 0.58f)
                        lineTo(w * 0.76f, h * 0.28f)
                        lineTo(w * 0.45f, h * 0.64f)
                        lineTo(w * 0.45f, h * 0.86f)
                        lineTo(w * 0.58f, h * 0.72f)
                        lineTo(w * 0.78f, h * 0.86f)
                        close()
                    }
                    drawPath(path = path, color = Color.White)
                }

                SettingsProIconType.APP_ARCHITECTURE -> {
                    // Isometric Stacked Broadcast Platform Layers + Play Core
                    val topDiamond = Path().apply {
                        moveTo(w * 0.50f, h * 0.12f)
                        lineTo(w * 0.90f, h * 0.32f)
                        lineTo(w * 0.50f, h * 0.52f)
                        lineTo(w * 0.10f, h * 0.32f)
                        close()
                    }
                    drawPath(
                        path = topDiamond,
                        color = Color.White.copy(alpha = 0.22f)
                    )
                    drawPath(
                        path = topDiamond,
                        color = Color.White,
                        style = Stroke(width = stroke, join = StrokeJoin.Round)
                    )
                    // Middle Layer Chevron
                    val midLayer = Path().apply {
                        moveTo(w * 0.12f, h * 0.52f)
                        lineTo(w * 0.50f, h * 0.71f)
                        lineTo(w * 0.88f, h * 0.52f)
                    }
                    drawPath(
                        path = midLayer,
                        color = Color.White,
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Bottom Layer Chevron
                    val botLayer = Path().apply {
                        moveTo(w * 0.12f, h * 0.70f)
                        lineTo(w * 0.50f, h * 0.89f)
                        lineTo(w * 0.88f, h * 0.70f)
                    }
                    drawPath(
                        path = botLayer,
                        color = accentGlow,
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                SettingsProIconType.PRIVACY_SHIELD -> {
                    // Cryptographic Security Shield + Verified Checkmark
                    val shield = Path().apply {
                        moveTo(w * 0.50f, h * 0.08f)
                        lineTo(w * 0.86f, h * 0.22f)
                        lineTo(w * 0.86f, h * 0.52f)
                        quadraticTo(w * 0.86f, h * 0.80f, w * 0.50f, h * 0.93f)
                        quadraticTo(w * 0.14f, h * 0.80f, w * 0.14f, h * 0.52f)
                        lineTo(w * 0.14f, h * 0.22f)
                        close()
                    }
                    drawPath(
                        path = shield,
                        color = Color.White.copy(alpha = 0.18f)
                    )
                    drawPath(
                        path = shield,
                        color = Color.White,
                        style = Stroke(width = stroke, join = StrokeJoin.Round)
                    )
                    // Precision checkmark inside shield
                    val check = Path().apply {
                        moveTo(w * 0.34f, h * 0.52f)
                        lineTo(w * 0.46f, h * 0.64f)
                        lineTo(w * 0.68f, h * 0.38f)
                    }
                    drawPath(
                        path = check,
                        color = Color.White,
                        style = Stroke(width = stroke * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                SettingsProIconType.DEVELOPER_CONSOLE -> {
                    // Command Console Frame + Terminal Prompt `>_` + Broadcast Pulse
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.16f),
                        topLeft = Offset(w * 0.08f, h * 0.14f),
                        size = Size(w * 0.84f, h * 0.72f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.08f, h * 0.14f),
                        size = Size(w * 0.84f, h * 0.72f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f),
                        style = Stroke(width = stroke)
                    )
                    // Prompt chevron `>`
                    val chevron = Path().apply {
                        moveTo(w * 0.26f, h * 0.37f)
                        lineTo(w * 0.44f, h * 0.50f)
                        lineTo(w * 0.26f, h * 0.63f)
                    }
                    drawPath(
                        path = chevron,
                        color = Color.White,
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Command line `_`
                    drawLine(
                        color = FoulGoldStar,
                        start = Offset(w * 0.52f, h * 0.63f),
                        end = Offset(w * 0.74f, h * 0.63f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun ProSettingsOptionCard(
    title: String,
    subtitle: String,
    iconType: SettingsProIconType,
    iconBgColors: List<Color>,
    accentColor: Color,
    badgeText: String? = null,
    badgeColor: Color = FoulPurpleDeep,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(12.dp),
                scaleOnFocus = 1.02f,
                onClick = onClick
            )
            .border(
                width = 0.9.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.48f), FoulDivider)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(FoulPurpleDeep.copy(alpha = 0.42f), FoulCardDark, FoulBlackBg)
                    )
                )
                .padding(horizontal = 11.dp, vertical = 8.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsProIconBadge(
                iconType = iconType,
                gradientColors = iconBgColors,
                accentGlow = accentColor
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.5.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = FoulTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!badgeText.isNullOrBlank()) {
                Surface(
                    color = badgeColor.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            CompactTrailingChevron(accentColor = FoulCardElevated)
        }
    }
}

@Composable
private fun CompactTrailingChevron(accentColor: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor)
            .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
    }
}

/**
 * Independent Full-Screen Page: App Description
 */
@Composable
fun AppDescriptionIndependentScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("app_description_screen")
    ) {
        FoulSmartTopBar(
            telegramTestTag = "app_description_telegram_button"
        )

        FoulSectionSubBar(
            title = "وصف عن التطبيق",
            subtitle = "تعرّف على منظومة Foul TV المتكاملة",
            onBackClick = onBack,
            backButtonTestTag = "app_description_back_button"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Identity Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                    border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(FoulPurpleDeep.copy(alpha = 0.7f), FoulCardDark)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FoulPurpleBright)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "تطبيق Foul TV الرسمي",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "الإصدار ${AppVersionConfig.APP_CURRENT_VERSION} • منصة البث والترفيه والرياضة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FoulGoldStar
                                )
                            }
                        }

                        Text(
                            text = "يُعد تطبيق Foul TV منصة عربية شاملة ومتطورة تجمع بين البث التلفزيوني المباشر عالي الدقة، مكتبة الأفلام والمسلسلات الضخمة، وتغطية حية لجدول المباريات وإحصائيات اللاعبين والأندية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.LiveTv,
                    accentColor = FoulPurpleBright,
                    title = "1. قسم القنوات المباشرة (الرئيسية)",
                    bulletPoints = listOf(
                        "تنظيم احترافي للأفرع والأقسام الفرعية مع عرض القنوات بشبكة منظمة 3×3.",
                        "دعم تعدد السيرفرات والجودات (4K / FHD / HD / SD) مع التبديل الفوري داخل المشغل.",
                        "عدادات تفاعلية للمشاهدات والإعجابات لكل قناة بشكل مباشر."
                    )
                )
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.Movie,
                    accentColor = FoulTeamAwayCyan,
                    title = "2. قسم المكتبة (الأفلام والمسلسلات)",
                    bulletPoints = listOf(
                        "مكتبة متجددة من الأفلام والمسلسلات مع محرك بحث شامل فائق السرعة.",
                        "عرض المواسم والحلقات والتفاصيل الكاملة مع تشغيل فوري وسلس."
                    )
                )
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.SportsSoccer,
                    accentColor = FoulLiveGreen,
                    title = "3. قسم جدول المباريات واللاعبين والأندية",
                    bulletPoints = listOf(
                        "متابعة نتائج ومواعيد مباريات أمس واليوم وغداً مع تقويم تفاعلي شامل.",
                        "عرض أحداث المباراة، التشكيلة الرسمية على الملعب، نسب الاستحواذ، جدول الترتيب والأخبار.",
                        "إمكانية الضغط على أي لاعب في التشكيلة أو البحث عن أي لاعب أو فريق لعرض مسيرته في الدوريات، إحصائياته الكاملة، وسجل انتقالاته."
                    )
                )
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.Security,
                    accentColor = FoulGoldStar,
                    title = "4. السرعة والأمان والدعم المستمر",
                    bulletPoints = listOf(
                        "مزامنة سحابية لحظية مع قاعدة البيانات لضمان تحديث الروابط والقنوات تلقائياً.",
                        "نظام دعم فني مدمج للتواصل المباشر مع إدارة التطبيق في أي وقت."
                    )
                )
            }
        }
    }
}

/**
 * Independent Full-Screen Page: Privacy Policy
 */
@Composable
fun PrivacyPolicyIndependentScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("privacy_policy_screen")
    ) {
        FoulSmartTopBar(
            telegramTestTag = "privacy_policy_telegram_button"
        )

        FoulSectionSubBar(
            title = "سياسة الخصوصية",
            subtitle = "حماية البيانات ومعايير الأمان والخصوصية في Foul TV",
            onBackClick = onBack,
            backButtonTestTag = "privacy_policy_back_button"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoSectionCard(
                    icon = Icons.Default.Security,
                    accentColor = FoulLiveGreen,
                    title = "1. التزامنا بحماية خصوصيتك",
                    bulletPoints = listOf(
                        "يحرص تطبيق Foul TV على حماية خصوصية جميع المستخدمين وتأمين بياناتهم وفق أعلى معايير التشفير.",
                        "لا يطلب التطبيق أي أذونات حساسة غير ضرورية لتشغيل البث أو التحميل."
                    )
                )
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.VerifiedUser,
                    accentColor = FoulTeamAwayCyan,
                    title = "2. البيانات التي يتم جمعها واستخدامها",
                    bulletPoints = listOf(
                        "عند إنشاء حساب في قسم (تواصل معنا)، يتم حفظ البريد الإلكتروني واسم المستخدم والصورة الرمزية المختارة فقط لتمكينك من مراسلة الدعم الفني واستلام الردود.",
                        "يتم تسجيل عدادات المشاهدة والإعجابات بشكل إحصائي لتحسين جودة القنوات والسيرفرات."
                    )
                )
            }

            item {
                InfoSectionCard(
                    icon = Icons.Default.GppGood,
                    accentColor = FoulPurpleBright,
                    title = "3. أمان الاتصالات وعدم مشاركة البيانات",
                    bulletPoints = listOf(
                        "لا نقوم ببيع أو مشاركة أي بيانات خاصة بالمستخدمين مع أي أطراف خارجية.",
                        "جميع الاتصالات بقواعد البيانات تتم عبر قنوات مشفرة وآمنة (HTTPS)."
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    bulletPoints: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(1.dp, FoulDivider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
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

            bulletPoints.forEach { point ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(14.dp)
                    )
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

/**
 * Reusable Professional Telegram Vector Emblem Badge
 */
@Composable
fun TelegramVectorBadge(sizeDp: Int = 50) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2AABEE), Color(0xFF1E88E5))
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.28f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp * 0.52f).dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.92f, h * 0.14f)
                lineTo(w * 0.08f, h * 0.47f)
                lineTo(w * 0.34f, h * 0.58f)
                lineTo(w * 0.76f, h * 0.28f)
                lineTo(w * 0.45f, h * 0.64f)
                lineTo(w * 0.45f, h * 0.86f)
                lineTo(w * 0.58f, h * 0.72f)
                lineTo(w * 0.78f, h * 0.86f)
                close()
            }
            drawPath(path = path, color = Color.White)
        }
    }
}

/**
 * Half-Screen Modal Bottom Sheet for Developer Email & Password Verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperLoginBottomSheet(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FoulCardDark,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OrderedColorfulDivider(thickness = 1.5.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsProIconBadge(
                    iconType = SettingsProIconType.DEVELOPER_CONSOLE,
                    gradientColors = listOf(FoulPurpleBright, FoulPurpleDeep),
                    accentGlow = FoulGoldStar
                )

                Column {
                    Text(
                        text = "تحقق هوية المطور",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أدخل البريد الإلكتروني وكلمة المرور لفتح لوحة التحكم",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني للمطور", color = FoulTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FoulCardElevated,
                    unfocusedContainerColor = FoulCardElevated,
                    focusedBorderColor = FoulPurpleBright,
                    unfocusedBorderColor = FoulDivider,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dev_login_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور", color = FoulTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "إظهار كلمة المرور",
                            tint = FoulTextSecondary
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FoulCardElevated,
                    unfocusedContainerColor = FoulCardElevated,
                    focusedBorderColor = FoulPurpleBright,
                    unfocusedBorderColor = FoulDivider,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dev_login_password_input")
            )

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = FoulLiveRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FoulLiveRed, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = { onLogin(email, password) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FoulPurplePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, FoulPurpleBright.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .testTag("dev_login_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جاري التحقق...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = "تسجيل الدخول وفتح لوحة التحكم",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
