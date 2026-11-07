package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulTeamAwayCyan

private val TvFocusHighlightBrush = Brush.horizontalGradient(
    colors = listOf(FoulTeamAwayCyan, Color.White, FoulPurpleBright)
)

/**
 * Universal zero-lag modifier for Android TV, TV Box (D-Pad / Remote Control), PC (Mouse Hover + Keyboard Enter/Space),
 * and Mobile Touch.
 *
 * - Provides full D-Pad & Keyboard focusability without spawning per-item animation coroutines or render layers
 * - Highlights focused or hovered elements with a high-contrast neon Cyan-Purple border
 * - Triggers [onClick] on Remote OK / DPAD_CENTER / Enter / NumPadEnter / Spacebar as well as touch/mouse click
 */
fun Modifier.tvFocusableClickable(
    shape: Shape = RoundedCornerShape(12.dp),
    scaleOnFocus: Float = 1.025f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var hasDirectFocus by remember { mutableStateOf(false) }

    val highlighted = enabled && (isFocused || isHovered || hasDirectFocus)

    this
        .onFocusChanged { state ->
            val next = state.isFocused || state.hasFocus
            if (hasDirectFocus != next) {
                hasDirectFocus = next
            }
        }
        .onKeyEvent { event ->
            if (enabled && event.type == KeyEventType.KeyDown &&
                (event.key == Key.NumPadEnter || event.key == Key.ButtonA || event.key == Key.ButtonSelect)
            ) {
                onClick()
                true
            } else {
                false
            }
        }
        .hoverable(interactionSource = interactionSource, enabled = enabled)
        .then(
            if (highlighted) {
                Modifier
                    .scale(scaleOnFocus.coerceIn(1f, 1.035f))
                    .border(
                        width = 2.3.dp,
                        brush = TvFocusHighlightBrush,
                        shape = shape
                    )
            } else {
                Modifier
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Adds a high-visibility TV Remote D-Pad & PC Hover highlight ring to standard Compose Buttons / IconButtons
 * without per-frame animation overhead.
 */
fun Modifier.tvFocusHighlight(
    shape: Shape = RoundedCornerShape(12.dp),
    scaleOnFocus: Float = 1.03f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }

    val highlighted = isFocused || isHovered

    this
        .onFocusChanged { state ->
            val next = state.isFocused || state.hasFocus
            if (isFocused != next) {
                isFocused = next
            }
        }
        .hoverable(interactionSource = interactionSource)
        .then(
            if (highlighted) {
                Modifier
                    .scale(scaleOnFocus.coerceIn(1f, 1.035f))
                    .border(
                        width = 2.3.dp,
                        brush = TvFocusHighlightBrush,
                        shape = shape
                    )
            } else {
                Modifier
            }
        )
}

/**
 * Checks whether a hardware key corresponds to TV Remote OK / Center / Gamepad A / PC Enter / Space.
 */
fun Key.isConfirmKey(): Boolean {
    return this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter ||
        this == Key.Spacebar ||
        this == Key.ButtonA ||
        this == Key.ButtonSelect
}

@Composable
fun rememberIsWideTvOrDesktopScreen(maxWidthDp: Float): Boolean {
    return maxWidthDp >= 680f
}
