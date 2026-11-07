package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Foul TV — True OLED Black & Deep Dark Royal Purple Luxury Palette
// ============================================================================

// Pure Black & Deep Obsidian Surfaces ("لون الأسود الصافي والداكن الفخم")
val FoulBlackBg = Color(0xFF000000)         // Pure OLED Black primary background
val FoulBottomBarBlack = Color(0xFF030305)  // Pitch-black top/bottom bar background
val FoulSurfaceDark = Color(0xFF08080C)     // Deep obsidian sub-bar / section surface
val FoulCardDark = Color(0xFF0D0D13)        // Pure obsidian dark card background
val FoulCardElevated = Color(0xFF15151E)    // Elevated matte charcoal-obsidian surface

// Deep Dark Royal Purple Accents ("أرجواني غامق ملكي غير مشبع")
val FoulPurpleDeep = Color(0xFF1A092E)      // Deepest dark royal purple
val FoulPurplePrimary = Color(0xFF2B114D)   // Dark purple primary container
val FoulPurpleAccent = Color(0xFF3E196E)    // Refined dark purple border/button accent
val FoulPurpleBright = Color(0xFF562496)    // Rich dark royal amethyst (replaces bright neon purple)
val FoulPurpleGlow = Color(0xFF6E32B8)      // Subtle royal purple highlight for focus/active states

// Sports & Broadcast Semantic Accents
val FoulGoldStar = Color(0xFFEAB308)        // Refined broadcast gold
val FoulLiveRed = Color(0xFFEF4444)         // Crisp live red badge
val FoulLiveGreen = Color(0xFF10B981)       // Crisp emerald broadcast live indicator
val FoulPitchGreenDark = Color(0xFF062E1E)  // Deep stadium pitch green
val FoulPitchGreenLight = Color(0xFF0A422B) // Stadium turf stripe green
val FoulTeamHomeIndigo = Color(0xFF4C1D95)  // Deep dark violet for home team stats
val FoulTeamAwayCyan = Color(0xFF7C3AED)    // Refined royal violet accent (replaces garish light cyan)

// Typography & Borders
val FoulTextPrimary = Color(0xFFF8FAFC)     // Crisp pure white text
val FoulTextSecondary = Color(0xFF94A3B8)   // Balanced cool slate-silver secondary text
val FoulTextMuted = Color(0xFF64748B)       // Muted slate metadata text
val FoulDivider = Color(0xFF1C1C27)         // Subtle dark obsidian border/divider

// Sleek Dark Royal Purple & Obsidian Divider Line (replaces noisy rainbow line)
val FoulOrderedColorLineBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF050508),
        Color(0xFF2B114D),
        Color(0xFF562496),
        Color(0xFF2B114D),
        Color(0xFF050508)
    )
)

// Pure Black & Subtle Dark Purple Header Gradient
val FoulBroadcastHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF07030D),
        Color(0xFF020204),
        Color(0xFF000000)
    )
)
