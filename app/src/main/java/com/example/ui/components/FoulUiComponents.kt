package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BottomNavTab
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulOrderedColorLineBrush
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulPurpleGlow
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulSurfaceDark
import com.example.ui.theme.FoulTextSecondary

/**
 * Subtle, ultra-clean dark purple & obsidian divider line.
 */
@Composable
fun OrderedColorfulDivider(
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(FoulOrderedColorLineBrush)
    )
}

/**
 * ============================================================================
 * 1. SMART MAIN TOP BAR ("شريط علوي للأعلى قليلاً وبشكل مرتب وذكي جداً")
 * ============================================================================
 * Contains strictly:
 * - App Logo image (`R.drawable.ic_foul_tv_launcher`) + `FOUL TV` brand title
 * - Official Telegram Channel icon button (`TelegramTopBarIconButton`)
 * Nothing else is placed in the top bar. Positioned high and flush with compact height.
 */
@Composable
fun FoulSmartTopBar(
    telegramUrl: String = "https://t.me/jdj_q",
    telegramTestTag: String = "top_bar_telegram_button",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FoulBottomBarBlack)
            .statusBarsPadding()
            .testTag("foul_smart_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Side (in RTL): App Logo Image + "FOUL TV" Brand Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(FoulCardDark)
                        .border(
                            width = 1.1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(FoulPurpleBright, FoulPurplePrimary)
                            ),
                            shape = RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_foul_tv_launcher),
                        contentDescription = "شعار تطبيق FOUL TV",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FOUL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Surface(
                        color = FoulPurpleDeep,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.9.dp, FoulPurpleBright.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "TV",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }

            // Left Side (in RTL): Telegram Official Channel Icon Button ONLY
            TelegramTopBarIconButton(
                telegramUrl = telegramUrl,
                modifier = Modifier.testTag(telegramTestTag)
            )
        }

        OrderedColorfulDivider(thickness = 1.dp)
    }
}

/**
 * ============================================================================
 * 2. CONTEXTUAL SUB-HEADER BAR UNDER TOP BAR ("زر البحث وكذلك سهم رجوع يكون تحت شريط العلوي حسب الأقسام وبشكل مرتب وظاهر")
 * ============================================================================
 * Placed directly underneath [FoulSmartTopBar] to house:
 * - Prominent Back Arrow button (when navigating inside a branch, search, or detail page)
 * - Current Section Title & optional subtitle
 * - Prominent Search button (when the section supports searching) + optional section tools
 */
@Composable
fun FoulSectionSubBar(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    onBackClick: (() -> Unit)? = null,
    backButtonTestTag: String = "sub_bar_back_button",
    onSearchClick: (() -> Unit)? = null,
    searchButtonLabel: String = "بحث",
    searchButtonTestTag: String = "sub_bar_search_button",
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FoulSurfaceDark)
            .testTag("foul_section_sub_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Side (in RTL): Back Button (if active) + Section Indicator & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onBackClick != null) {
                    Surface(
                        color = FoulPurpleDeep,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .height(33.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .tvFocusableClickable(
                                shape = RoundedCornerShape(10.dp),
                                scaleOnFocus = 1.05f,
                                onClick = onBackClick
                            )
                            .testTag(backButtonTestTag)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "رجوع",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(FoulCardDark)
                            .border(
                                width = 1.dp,
                                color = FoulPurpleBright.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(9.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Subtle vertical dark-purple indicator bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(FoulPurpleGlow, FoulPurplePrimary)
                                )
                            )
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Left Side (in RTL): Search Button + Contextual Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (onSearchClick != null) {
                    Surface(
                        color = FoulCardDark,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .height(33.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .tvFocusableClickable(
                                shape = RoundedCornerShape(10.dp),
                                scaleOnFocus = 1.05f,
                                onClick = onSearchClick
                            )
                            .testTag(searchButtonTestTag)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = searchButtonLabel,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = searchButtonLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                actions()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FoulDivider)
        )
    }
}

/**
 * Combined wrapper that renders [FoulSmartTopBar] on top and [FoulSectionSubBar] directly underneath it.
 * Keeps full compatibility with existing screens while enforcing the new two-tier header design.
 */
@Composable
fun FoulCompactTopBar(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    telegramUrl: String = "https://t.me/jdj_q",
    telegramTestTag: String = "top_bar_telegram_button",
    onBackClick: (() -> Unit)? = null,
    backButtonTestTag: String = "sub_bar_back_button",
    onSearchClick: (() -> Unit)? = null,
    searchButtonLabel: String = "بحث",
    searchButtonTestTag: String = "sub_bar_search_button",
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FoulSmartTopBar(
            telegramUrl = telegramUrl,
            telegramTestTag = telegramTestTag
        )
        FoulSectionSubBar(
            title = title,
            subtitle = subtitle,
            leadingIcon = leadingIcon,
            onBackClick = onBackClick,
            backButtonTestTag = backButtonTestTag,
            onSearchClick = onSearchClick,
            searchButtonLabel = searchButtonLabel,
            searchButtonTestTag = searchButtonTestTag,
            actions = actions
        )
    }
}

@Composable
fun CompactTopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(FoulCardDark)
            .border(
                width = 1.dp,
                color = FoulDivider,
                shape = RoundedCornerShape(9.dp)
            )
            .tvFocusableClickable(
                shape = RoundedCornerShape(9.dp),
                scaleOnFocus = 1.08f,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Sleek, neat Telegram Channel Top Bar Icon Button in the main Top Bar.
 */
@Composable
fun TelegramTopBarIconButton(
    telegramUrl: String = "https://t.me/jdj_q",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val normalizedUrl = run {
        val raw = telegramUrl.trim().ifBlank { "https://t.me/jdj_q" }
        when {
            raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true) -> raw
            raw.startsWith("@") -> "https://t.me/${raw.removePrefix("@")}"
            raw.startsWith("t.me/", ignoreCase = true) -> "https://$raw"
            else -> "https://$raw"
        }
    }

    Box(
        modifier = modifier
            .size(33.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF29B6F6), Color(0xFF0277BD))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(9.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(9.dp),
                scaleOnFocus = 1.08f,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
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
 * ============================================================================
 * 3. BESPOKE MODERN VECTOR ICONS FOR NAVIGATION & SECTIONS
 *    Multi-layered, sharp broadcast-grade vector geometry.
 * ============================================================================
 */
@Composable
fun FoulCustomNavIcon(
    tab: BottomNavTab,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (isSelected) Color.White else FoulTextSecondary
    val accentColor = if (isSelected) FoulPurpleGlow else FoulTextSecondary.copy(alpha = 0.65f)

    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = if (isSelected) 2.0.dp.toPx() else 1.65.dp.toPx()

        when (tab) {
            BottomNavTab.HOME -> {
                // Sleek Widescreen Broadcast Monitor + Signal Glow + Play Core
                if (isSelected) {
                    drawRoundRect(
                        color = FoulPurpleBright.copy(alpha = 0.25f),
                        topLeft = Offset(w * 0.08f, h * 0.20f),
                        size = Size(w * 0.84f, h * 0.58f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                    )
                }
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(w * 0.08f, h * 0.20f),
                    size = Size(w * 0.84f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.15f, w * 0.15f),
                    style = Stroke(width = strokeWidth)
                )
                // Pedestal neck & base
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.50f, h * 0.78f),
                    end = Offset(w * 0.50f, h * 0.90f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(w * 0.28f, h * 0.90f),
                    end = Offset(w * 0.72f, h * 0.90f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                // Center play emblem
                val playPath = Path().apply {
                    moveTo(w * 0.41f, h * 0.36f)
                    lineTo(w * 0.64f, h * 0.49f)
                    lineTo(w * 0.41f, h * 0.62f)
                    close()
                }
                drawPath(path = playPath, color = primaryColor)
            }

            BottomNavTab.IPTV -> {
                // Live IPTV Satellite / Multi-Channel Receiver Hub Icon
                if (isSelected) {
                    drawRoundRect(
                        color = FoulPurpleBright.copy(alpha = 0.25f),
                        topLeft = Offset(w * 0.10f, h * 0.28f),
                        size = Size(w * 0.80f, h * 0.56f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                    )
                }
                // Antenna dual V-arms on top
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.50f, h * 0.28f),
                    end = Offset(w * 0.28f, h * 0.10f),
                    strokeWidth = strokeWidth * 0.9f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.50f, h * 0.28f),
                    end = Offset(w * 0.72f, h * 0.10f),
                    strokeWidth = strokeWidth * 0.9f,
                    cap = StrokeCap.Round
                )
                // Outer IPTV Screen Frame
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(w * 0.10f, h * 0.28f),
                    size = Size(w * 0.80f, h * 0.56f),
                    cornerRadius = CornerRadius(w * 0.15f, w * 0.15f),
                    style = Stroke(width = strokeWidth)
                )
                // 3x3 Live Grid Dots inside screen representing IPTV channels
                val cols = listOf(w * 0.32f, w * 0.50f, w * 0.68f)
                val rows = listOf(h * 0.45f, h * 0.66f)
                for (ry in rows) {
                    for (cx in cols) {
                        drawCircle(
                            color = if (isSelected && cx == w * 0.50f && ry == h * 0.45f) accentColor else primaryColor,
                            radius = w * 0.048f,
                            center = Offset(cx, ry)
                        )
                    }
                }
            }

            BottomNavTab.LIBRARY -> {
                // Cinema Pro Clapperboard & Reel Frame
                if (isSelected) {
                    drawRoundRect(
                        color = FoulPurpleBright.copy(alpha = 0.25f),
                        topLeft = Offset(w * 0.10f, h * 0.16f),
                        size = Size(w * 0.80f, h * 0.70f),
                        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                    )
                }
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(w * 0.10f, h * 0.16f),
                    size = Size(w * 0.80f, h * 0.70f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                    style = Stroke(width = strokeWidth)
                )
                // Top clapper bar line
                drawLine(
                    color = primaryColor,
                    start = Offset(w * 0.10f, h * 0.35f),
                    end = Offset(w * 0.90f, h * 0.35f),
                    strokeWidth = strokeWidth * 0.9f
                )
                // Clapper diagonal stripes
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.34f, h * 0.16f),
                    end = Offset(w * 0.24f, h * 0.35f),
                    strokeWidth = strokeWidth * 0.85f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.62f, h * 0.16f),
                    end = Offset(w * 0.52f, h * 0.35f),
                    strokeWidth = strokeWidth * 0.85f,
                    cap = StrokeCap.Round
                )
                // Play triangle inside body
                val playPath = Path().apply {
                    moveTo(w * 0.41f, h * 0.47f)
                    lineTo(w * 0.63f, h * 0.60f)
                    lineTo(w * 0.41f, h * 0.73f)
                    close()
                }
                drawPath(path = playPath, color = primaryColor)
            }

            BottomNavTab.MATCHES -> {
                // Tactical Stadium Arena Pitch & Center Kickoff Circle
                if (isSelected) {
                    drawRoundRect(
                        color = FoulPurpleBright.copy(alpha = 0.25f),
                        topLeft = Offset(w * 0.08f, h * 0.18f),
                        size = Size(w * 0.84f, h * 0.64f),
                        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                    )
                }
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(w * 0.08f, h * 0.18f),
                    size = Size(w * 0.84f, h * 0.64f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                    style = Stroke(width = strokeWidth)
                )
                // Halfway line
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.50f, h * 0.18f),
                    end = Offset(w * 0.50f, h * 0.82f),
                    strokeWidth = strokeWidth * 0.85f
                )
                // Center circle + kickoff dot
                drawCircle(
                    color = primaryColor,
                    radius = w * 0.15f,
                    center = Offset(w * 0.50f, h * 0.50f),
                    style = Stroke(width = strokeWidth * 0.9f)
                )
                drawCircle(
                    color = accentColor,
                    radius = w * 0.035f,
                    center = Offset(w * 0.50f, h * 0.50f)
                )
            }

            BottomNavTab.SETTINGS -> {
                // Precision Broadcast Control Equalizer Sliders
                val yPositions = listOf(h * 0.25f, h * 0.50f, h * 0.75f)
                val knobX = listOf(w * 0.35f, w * 0.67f, w * 0.42f)
                yPositions.forEachIndexed { idx, y ->
                    drawLine(
                        color = primaryColor.copy(alpha = 0.80f),
                        start = Offset(w * 0.12f, y),
                        end = Offset(w * 0.88f, y),
                        strokeWidth = strokeWidth * 0.9f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = FoulBlackBg,
                        radius = w * 0.095f,
                        center = Offset(knobX[idx], y)
                    )
                    drawCircle(
                        color = if (isSelected && idx == 1) accentColor else primaryColor,
                        radius = w * 0.095f,
                        center = Offset(knobX[idx], y),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }
    }
}

/**
 * Custom sleek Package / Branch Vault icon replacing the generic flat Folder icon.
 */
@Composable
fun FoulCustomPackageIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 1.85.dp.toPx()

        val folderPath = Path().apply {
            moveTo(w * 0.10f, h * 0.24f)
            lineTo(w * 0.38f, h * 0.24f)
            lineTo(w * 0.48f, h * 0.35f)
            lineTo(w * 0.90f, h * 0.35f)
            lineTo(w * 0.90f, h * 0.82f)
            lineTo(w * 0.10f, h * 0.82f)
            close()
        }
        drawPath(
            path = folderPath,
            color = tint.copy(alpha = 0.16f)
        )
        drawPath(
            path = folderPath,
            color = tint,
            style = Stroke(width = stroke, join = StrokeJoin.Round)
        )
        // Play / Broadcast symbol inside package
        val playPath = Path().apply {
            moveTo(w * 0.43f, h * 0.47f)
            lineTo(w * 0.63f, h * 0.59f)
            lineTo(w * 0.43f, h * 0.71f)
            close()
        }
        drawPath(path = playPath, color = tint)
    }
}
