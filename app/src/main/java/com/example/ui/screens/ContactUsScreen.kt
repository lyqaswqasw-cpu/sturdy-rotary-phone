package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.ui.components.tvFocusableClickable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.AppAvatarSpec
import com.example.data.model.ContactMessageItem
import com.example.data.model.SocialUserProfile
import com.example.data.repository.SocialFirebaseRepository
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.FoulCompactTopBar
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.FoulTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Animated Professional Bot Icon ("أيقونة بوت متحركة احترافية جدا")
 * Features smooth floating motion, glowing antenna signal pulse, animated cyber eyes, and royal purple/cyan aura.
 */
@Composable
fun AnimatedBotIcon(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bot_anim")

    val floatOffsetPx by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_float"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_pulse"
    )

    val eyeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_eye_glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .offset { IntOffset(0, floatOffsetPx.roundToInt()) },
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing aura ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.35f),
                            FoulPurpleBright.copy(alpha = 0.65f),
                            FoulPurpleDeep
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF38BDF8), Color.White, Color(0xFFA855F7))
                    ),
                    shape = CircleShape
                )
        )

        // Custom Vector Bot Drawing
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // Antenna stem & glowing orb
            drawLine(
                color = Color.White,
                start = Offset(w * 0.5f, h * 0.24f),
                end = Offset(w * 0.5f, h * 0.08f),
                strokeWidth = w * 0.06f
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = eyeGlowAlpha),
                radius = w * 0.09f * pulseScale,
                center = Offset(w * 0.5f, h * 0.07f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.045f,
                center = Offset(w * 0.5f, h * 0.07f)
            )

            // Side Ear Pods
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(w * 0.06f, h * 0.40f),
                size = Size(w * 0.10f, h * 0.26f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(w * 0.84f, h * 0.40f),
                size = Size(w * 0.10f, h * 0.26f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )

            // Main Bot Head Shell
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E1065), Color(0xFF17092B))
                ),
                topLeft = Offset(w * 0.15f, h * 0.24f),
                size = Size(w * 0.70f, h * 0.60f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.15f, h * 0.24f),
                size = Size(w * 0.70f, h * 0.60f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                style = Stroke(width = w * 0.045f)
            )

            // Inner Visor Screen
            drawRoundRect(
                color = Color(0xFF070712),
                topLeft = Offset(w * 0.23f, h * 0.34f),
                size = Size(w * 0.54f, h * 0.38f),
                cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
            )

            // Glowing Eyes
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = eyeGlowAlpha),
                radius = w * 0.075f,
                center = Offset(w * 0.37f, h * 0.49f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.032f,
                center = Offset(w * 0.37f, h * 0.48f)
            )

            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = eyeGlowAlpha),
                radius = w * 0.075f,
                center = Offset(w * 0.63f, h * 0.49f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.032f,
                center = Offset(w * 0.63f, h * 0.48f)
            )

            // Friendly Smile Curve
            val smilePath = Path().apply {
                moveTo(w * 0.39f, h * 0.62f)
                quadraticTo(w * 0.50f, h * 0.69f, w * 0.61f, h * 0.62f)
            }
            drawPath(
                path = smilePath,
                color = Color.White.copy(alpha = eyeGlowAlpha),
                style = Stroke(width = w * 0.04f)
            )
        }
    }
}

/**
 * Renders one of the 18 Built-in Boys & Girls Avatars ("صور الموجودة في تطبيق بشكل صور من تطبيق ولد وبنات").
 */
@Composable
fun InAppCharacterAvatar(
    avatarSpec: AppAvatarSpec,
    size: Dp = 64.dp,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(avatarSpec.primaryHex),
                        Color(avatarSpec.secondaryHex)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.5.dp else 1.2.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val accent = Color(avatarSpec.accentHex)
            val skinColor = when (avatarSpec.styleVariant % 3) {
                0 -> Color(0xFFFDE6D8)
                1 -> Color(0xFFF7D3BA)
                else -> Color(0xFFEAC09E)
            }
            val hairColor = when (avatarSpec.styleVariant % 4) {
                0 -> Color(0xFF18181B)
                1 -> Color(0xFF3F2314)
                2 -> Color(0xFF27173A)
                else -> Color(0xFF4A2511)
            }

            // Subtle background glow ring
            drawCircle(
                color = accent.copy(alpha = 0.22f),
                radius = w * 0.42f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Back hair for girls
            if (!avatarSpec.isBoy) {
                val backHair = Path().apply {
                    moveTo(w * 0.22f, h * 0.36f)
                    quadraticTo(w * 0.14f, h * 0.72f, w * 0.24f, h * 0.88f)
                    lineTo(w * 0.76f, h * 0.88f)
                    quadraticTo(w * 0.86f, h * 0.72f, w * 0.78f, h * 0.36f)
                    close()
                }
                drawPath(path = backHair, color = hairColor)
            }

            // Shoulders / Hoodie / Outfit
            val outfitPath = Path().apply {
                moveTo(w * 0.16f, h * 1.0f)
                quadraticTo(w * 0.22f, h * 0.70f, w * 0.50f, h * 0.70f)
                quadraticTo(w * 0.78f, h * 0.70f, w * 0.84f, h * 1.0f)
                close()
            }
            drawPath(path = outfitPath, color = accent)

            // Collar highlight
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.36f, h * 0.67f),
                size = Size(w * 0.28f, h * 0.14f),
                style = Stroke(width = w * 0.03f)
            )

            // Neck
            drawRoundRect(
                color = skinColor,
                topLeft = Offset(w * 0.43f, h * 0.58f),
                size = Size(w * 0.14f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )

            // Head / Face
            drawOval(
                color = skinColor,
                topLeft = Offset(w * 0.29f, h * 0.24f),
                size = Size(w * 0.42f, h * 0.42f)
            )

            // Eyes
            drawCircle(
                color = Color(0xFF18181B),
                radius = w * 0.032f,
                center = Offset(w * 0.41f, h * 0.45f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.012f,
                center = Offset(w * 0.40f, h * 0.44f)
            )

            drawCircle(
                color = Color(0xFF18181B),
                radius = w * 0.032f,
                center = Offset(w * 0.59f, h * 0.45f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.012f,
                center = Offset(w * 0.58f, h * 0.44f)
            )

            // Eyebrows
            drawLine(
                color = hairColor,
                start = Offset(w * 0.36f, h * 0.39f),
                end = Offset(w * 0.45f, h * 0.39f),
                strokeWidth = w * 0.024f
            )
            drawLine(
                color = hairColor,
                start = Offset(w * 0.55f, h * 0.39f),
                end = Offset(w * 0.64f, h * 0.39f),
                strokeWidth = w * 0.024f
            )

            // Rosy blush for girls
            if (!avatarSpec.isBoy) {
                drawCircle(
                    color = Color(0xFFF43F5E).copy(alpha = 0.35f),
                    radius = w * 0.042f,
                    center = Offset(w * 0.35f, h * 0.51f)
                )
                drawCircle(
                    color = Color(0xFFF43F5E).copy(alpha = 0.35f),
                    radius = w * 0.042f,
                    center = Offset(w * 0.65f, h * 0.51f)
                )
            }

            // Smile
            val mouthPath = Path().apply {
                moveTo(w * 0.43f, h * 0.55f)
                quadraticTo(w * 0.50f, h * 0.60f, w * 0.57f, h * 0.55f)
            }
            drawPath(
                path = mouthPath,
                color = if (avatarSpec.isBoy) Color(0xFF7C2D12) else Color(0xFFE11D48),
                style = Stroke(width = w * 0.028f)
            )

            // Top Hair / Cap / Accessories
            if (avatarSpec.isBoy) {
                val boyHairPath = Path().apply {
                    moveTo(w * 0.27f, h * 0.38f)
                    quadraticTo(w * 0.30f, h * 0.15f, w * 0.52f, h * 0.17f)
                    quadraticTo(w * 0.72f, h * 0.18f, w * 0.73f, h * 0.38f)
                    quadraticTo(w * 0.50f, h * 0.28f, w * 0.27f, h * 0.38f)
                    close()
                }
                drawPath(path = boyHairPath, color = hairColor)

                // Gamer / Sport headphones on odd variants
                if (avatarSpec.styleVariant % 2 == 1) {
                    drawArc(
                        color = accent,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.24f, h * 0.18f),
                        size = Size(w * 0.52f, h * 0.45f),
                        style = Stroke(width = w * 0.045f)
                    )
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(w * 0.21f, h * 0.38f),
                        size = Size(w * 0.07f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                    )
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(w * 0.72f, h * 0.38f),
                        size = Size(w * 0.07f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                    )
                }
            } else {
                // Girl bangs & hairband / crown
                val girlBangs = Path().apply {
                    moveTo(w * 0.26f, h * 0.40f)
                    quadraticTo(w * 0.28f, h * 0.16f, w * 0.50f, h * 0.17f)
                    quadraticTo(w * 0.72f, h * 0.16f, w * 0.74f, h * 0.40f)
                    quadraticTo(w * 0.58f, h * 0.27f, w * 0.50f, h * 0.32f)
                    quadraticTo(w * 0.42f, h * 0.27f, w * 0.26f, h * 0.40f)
                    close()
                }
                drawPath(path = girlBangs, color = hairColor)

                // Hair accessory / headband
                drawArc(
                    color = accent,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.29f, h * 0.19f),
                    size = Size(w * 0.42f, h * 0.30f),
                    style = Stroke(width = w * 0.038f)
                )
            }
        }
    }
}

private enum class RegistrationStep {
    CREDENTIALS_FORM,
    AVATAR_SELECTION_PAGE
}

/**
 * Full-Screen "Contact Us" (`تواصل معنا`) & User Account Registration / Login / Avatar Picker Screen
 * backed by the secondary Firebase database (`foul-aeed9`).
 */
@Composable
fun ContactUsScreen(
    currentUser: SocialUserProfile?,
    isAuthLoading: Boolean,
    authError: String?,
    messages: List<ContactMessageItem>,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, username: String, avatarId: String) -> Unit,
    onLogout: () -> Unit,
    onSendMessage: (text: String, replyTo: ContactMessageItem?) -> Unit,
    onRefreshMessages: () -> Unit = {},
    onClearAuthError: () -> Unit,
    onBack: () -> Unit
) {
    // Fast real-time sync while ContactUsScreen is open so developer replies arrive immediately
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            onRefreshMessages()
            while (true) {
                delay(2500L)
                onRefreshMessages()
            }
        }
    }

    // Registration sub-navigation state:
    // Step 1: CREDENTIALS_FORM -> Step 2: AVATAR_SELECTION_PAGE ("وأيضا اجعل صفحة صور بشكل تنقل إلى صفحة إختيار وبعدها ضغط على زر إنشاء حساب")
    var isLoginTab by remember { mutableStateOf(false) } // Default to Create Account or toggle freely
    var registrationStep by remember { mutableStateOf(RegistrationStep.CREDENTIALS_FORM) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Avatar Picker state: shows 6 Boys & Girls initially, with "عرض المزيد" button
    var visibleAvatarCount by remember { mutableIntStateOf(SocialFirebaseRepository.INITIAL_AVATAR_BATCH_SIZE) }
    var selectedAvatarId by remember { mutableStateOf(SocialFirebaseRepository.APP_AVATARS.first().id) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (currentUser == null && !isLoginTab && registrationStep == RegistrationStep.AVATAR_SELECTION_PAGE) {
            registrationStep = RegistrationStep.CREDENTIALS_FORM
        } else {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("contact_us_full_screen")
    ) {
        // Sleek Compact Top Bar
        FoulCompactTopBar(
            title = when {
                currentUser != null -> "تواصل معنا"
                !isLoginTab && registrationStep == RegistrationStep.AVATAR_SELECTION_PAGE -> "اختيار الصورة الشخصية"
                isLoginTab -> "تسجيل الدخول • تواصل معنا"
                else -> "إنشاء حساب • تواصل معنا"
            },
            subtitle = when {
                currentUser != null -> "متصل بحساب: ${currentUser.username} • اسحب أي رسالة للرد عليها"
                !isLoginTab && registrationStep == RegistrationStep.AVATAR_SELECTION_PAGE -> "اختر صورتك المفضلة من صور التطبيق ثم اضغط إنشاء حساب"
                else -> "سجل دخولك أو أنشئ حسابك للتواصل المباشر مع الدعم"
            },
            actions = {
                if (currentUser != null) {
                    CompactTopBarIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "تحديث الرسائل",
                        onClick = onRefreshMessages,
                        modifier = Modifier.testTag("contact_refresh_button")
                    )
                    CompactTopBarIconButton(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "تسجيل الخروج",
                        onClick = onLogout,
                        modifier = Modifier.testTag("contact_logout_button")
                    )
                }
                CompactTopBarIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع للخلف",
                    onClick = {
                        if (currentUser == null && !isLoginTab && registrationStep == RegistrationStep.AVATAR_SELECTION_PAGE) {
                            registrationStep = RegistrationStep.CREDENTIALS_FORM
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.testTag("contact_back_button")
                )
            }
        )

        if (currentUser != null) {
            // Logged-in Contact Us & Support Chat Experience with Swipe-to-Reply
            LoggedInContactChatContent(
                user = currentUser,
                messages = messages,
                onSendMessage = onSendMessage,
                onLogout = onLogout
            )
        } else if (!isLoginTab && registrationStep == RegistrationStep.AVATAR_SELECTION_PAGE) {

            // Dedicated Step 2 Page: In-App Boys & Girls Avatar Selection Page ("صفحة صور بشكل تنقل إلى صفحة إختيار وبعدها ضغط على زر إنشاء حساب")
            DedicatedAvatarPickerPage(
                username = usernameInput,
                email = emailInput,
                selectedAvatarId = selectedAvatarId,
                visibleCount = visibleAvatarCount,
                isLoading = isAuthLoading,
                errorMessage = authError ?: localValidationError,
                onSelectAvatar = { selectedAvatarId = it },
                onShowMoreAvatars = {
                    visibleAvatarCount = (visibleAvatarCount + 6).coerceAtMost(SocialFirebaseRepository.APP_AVATARS.size)
                },
                onBackToForm = {
                    localValidationError = null
                    onClearAuthError()
                    registrationStep = RegistrationStep.CREDENTIALS_FORM
                },
                onSubmitCreateAccount = {
                    localValidationError = null
                    onRegister(emailInput, passwordInput, usernameInput, selectedAvatarId)
                }
            )
        } else {
            // Step 1: Login / Create Account Credentials Screen
            AuthCredentialsStepContent(
                isLoginTab = isLoginTab,
                email = emailInput,
                password = passwordInput,
                username = usernameInput,
                isPasswordVisible = isPasswordVisible,
                isLoading = isAuthLoading,
                errorMessage = authError ?: localValidationError,
                onToggleTab = { loginMode ->
                    isLoginTab = loginMode
                    localValidationError = null
                    onClearAuthError()
                },
                onEmailChange = {
                    emailInput = it
                    localValidationError = null
                },
                onPasswordChange = {
                    passwordInput = it
                    localValidationError = null
                },
                onUsernameChange = {
                    usernameInput = it
                    localValidationError = null
                },
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                onProceedToAvatarPage = {
                    val cleanU = usernameInput.trim()
                    val cleanE = emailInput.trim()
                    val cleanP = passwordInput.trim()
                    when {
                        cleanU.isBlank() -> localValidationError = "يرجى إدخال اسم المستخدم أولاً"
                        cleanE.isBlank() || !cleanE.contains("@") -> localValidationError = "يرجى إدخال بريد إلكتروني صحيح"
                        cleanP.length < 6 -> localValidationError = "كلمة المرور يجب أن تكون 6 أحرف أو أرقام على الأقل"
                        else -> {
                            localValidationError = null
                            onClearAuthError()
                            registrationStep = RegistrationStep.AVATAR_SELECTION_PAGE
                        }
                    }
                },
                onSubmitLogin = {
                    localValidationError = null
                    onLogin(emailInput, passwordInput)
                }
            )
        }
    }
}

/**
 * Step 1: Professional Login & Create Account Form with Animated Bot Header
 */
@Composable
private fun AuthCredentialsStepContent(
    isLoginTab: Boolean,
    email: String,
    password: String,
    username: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleTab: (Boolean) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onProceedToAvatarPage: () -> Unit,
    onSubmitLogin: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Animated Bot Welcome Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF38BDF8), FoulPurpleBright, Color.White.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(FoulPurpleDeep, FoulCardDark)
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedBotIcon(size = 60.dp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرحباً بك في بوابة تواصل معنا",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "أنشئ حسابك الخاص مع اسم المستخدم وصورتك المفضلة من داخل التطبيق للتواصل الفوري مع فريق Foul TV.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FoulTextSecondary
                        )
                    }
                }
            }
        }

        // Mode Switcher: إنشاء حساب جديد | تسجيل الدخول
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FoulCardDark)
                    .border(1.dp, FoulDivider, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isLoginTab) FoulPurpleBright else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(10.dp),
                            scaleOnFocus = 1.03f,
                            onClick = { onToggleTab(false) }
                        )
                        .testTag("auth_tab_signup")
                ) {
                    Text(
                        text = "إنشاء حساب جديد",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = if (!isLoginTab) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLoginTab) FoulPurpleBright else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(10.dp),
                            scaleOnFocus = 1.03f,
                            onClick = { onToggleTab(true) }
                        )
                        .testTag("auth_tab_login")
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = if (isLoginTab) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Credentials Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FoulDivider, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isLoginTab) "تسجيل الدخول إلى حسابك" else "الخطوة 1: بيانات الحساب الأساسية",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (!isLoginTab) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = onUsernameChange,
                            label = { Text("اسم المستخدم (يظهر في ملفك والرسائل)", color = FoulTextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("social_username_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("البريد الإلكتروني", color = FoulTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = authTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("social_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("كلمة المرور", color = FoulTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
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
                        colors = authTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("social_password_input")
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
                        onClick = {
                            if (isLoginTab) onSubmitLogin() else onProceedToAvatarPage()
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .testTag(if (isLoginTab) "social_login_submit_button" else "social_next_avatar_page_button")
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
                                text = if (isLoginTab) "تسجيل الدخول الآن" else "التالي: اختيار الصورة الشخصية (ولد / بنات)",
                                style = MaterialTheme.typography.titleSmall,
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
 * Step 2: Dedicated In-App Avatar Selection Page ("وبعد الانشاء إضافة صورة حسب صور الموجودة في تطبيق
 * بشكل صور من تطبيق بشكل ضهور 6 صور ولد وبنات وإمكانية عرض صور آخر بشكل عرض المزيد
 * وأيضا اجعل صفحة صور بشكل تنقل إلى صفحة إختيار وبعدها ضغط على زر إنشاء حساب")
 */
@Composable
private fun DedicatedAvatarPickerPage(
    username: String,
    email: String,
    selectedAvatarId: String,
    visibleCount: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectAvatar: (String) -> Unit,
    onShowMoreAvatars: () -> Unit,
    onBackToForm: () -> Unit,
    onSubmitCreateAccount: () -> Unit
) {
    val allAvatars = SocialFirebaseRepository.APP_AVATARS
    val displayedAvatars = remember(visibleCount) { allAvatars.take(visibleCount) }
    val selectedAvatarSpec = remember(selectedAvatarId) {
        SocialFirebaseRepository.getAvatarById(selectedAvatarId)
    }
    val hasMoreAvatars = visibleCount < allAvatars.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("avatar_picker_page")
    ) {
        // Selected Avatar & User Preview Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FoulPurpleDeep, FoulCardDark)
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InAppCharacterAvatar(
                    avatarSpec = selectedAvatarSpec,
                    size = 66.dp,
                    isSelected = true
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username.ifBlank { "مستخدم جديد" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FoulPurpleBright
                    ) {
                        Text(
                            text = "الصورة المختارة: ${selectedAvatarSpec.nameAr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackToForm,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تعديل البيانات",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }

        // 3x2 Grid of Boys & Girls Avatars (6 initially) + "عرض المزيد" button
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "اختر صورتك الرمزية من صور التطبيق (أولاد وبنات):",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            items(displayedAvatars, key = { it.id }) { avatar ->
                val isSelected = avatar.id == selectedAvatarId
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) FoulPurpleDeep else FoulCardDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(16.dp),
                            scaleOnFocus = 1.04f,
                            onClick = { onSelectAvatar(avatar.id) }
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else FoulDivider,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("avatar_choice_${avatar.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            InAppCharacterAvatar(
                                avatarSpec = avatar,
                                size = 70.dp,
                                isSelected = isSelected
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(FoulLiveGreen)
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = avatar.nameAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(3.dp))

                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = FoulCardElevated
                        ) {
                            Text(
                                text = if (avatar.isBoy) "ولد" else "بنت",
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // "عرض المزيد" button to reveal more Boys & Girls avatars
            if (hasMoreAvatars) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedButton(
                        onClick = onShowMoreAvatars,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(44.dp)
                            .testTag("avatar_show_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "عرض المزيد من الصور (${allAvatars.size - visibleCount} صورة إضافية)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.dp)

        // Bottom Bar with Final "إنشاء حساب" Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulCardDark)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                onClick = onSubmitCreateAccount,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .testTag("final_create_account_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جاري إنشاء الحساب وحفظ الصورة...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "إنشاء حساب الآن",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Logged-In Contact Us Chat & User Profile Screen with Logout Button & Swipe-to-Reply.
 */
@Composable
private fun LoggedInContactChatContent(
    user: SocialUserProfile,
    messages: List<ContactMessageItem>,
    onSendMessage: (text: String, replyTo: ContactMessageItem?) -> Unit,
    onLogout: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ContactMessageItem?>(null) }
    val listState = rememberLazyListState()

    val userAvatarSpec = remember(user.avatarId) {
        SocialFirebaseRepository.getAvatarById(user.avatarId)
    }
    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale("ar"))
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // User Profile & Bot Status Header Card with Logout Button
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
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
                    InAppCharacterAvatar(
                        avatarSpec = userAvatarSpec,
                        size = 52.dp,
                        isSelected = true
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = FoulLiveGreen.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "متصل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Prominent Logout Button ("وأيضا إمكانية تسجيل الخروج")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FoulCardElevated,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .tvFocusableClickable(
                            shape = RoundedCornerShape(10.dp),
                            scaleOnFocus = 1.05f,
                            onClick = onLogout
                        )
                        .border(1.dp, FoulLiveRed.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                        .testTag("profile_logout_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = FoulLiveRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "تسجيل الخروج",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        OrderedColorfulDivider(thickness = 1.dp)

        // Messages List with Swipe-to-Reply ("عند سحب والرد على رسالة تظهر ذلك عند السحب ليكون بشكل رد منفصل")
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                SwipeableContactMessageBubble(
                    msg = msg,
                    timeFormatted = runCatching { timeFormatter.format(Date(msg.timestamp)) }.getOrDefault(""),
                    onSwipeReply = { replyingToMessage = msg }
                )
            }
        }

        OrderedColorfulDivider(thickness = 1.dp)

        // Quoted Reply Banner when a message is swiped for reply
        val activeReply = replyingToMessage
        if (activeReply != null) {
            Surface(
                color = FoulPurpleDeep,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = FoulPurpleBright, shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الرد على: ${activeReply.senderName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeReply.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = { replyingToMessage = null },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء الرد",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Message Composer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoulCardDark)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = {
                    Text(
                        text = if (activeReply != null) "اكتب ردك على الرسالة..." else "اكتب رسالتك للمطور (تصل فوراً للوحة المطور)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = FoulTextSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = authTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("contact_message_input")
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText, replyingToMessage)
                        messageText = ""
                        replyingToMessage = null
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FoulPurpleBright)
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                    .testTag("contact_send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun SwipeableContactMessageBubble(
    msg: ContactMessageItem,
    timeFormatted: String,
    onSwipeReply: () -> Unit
) {
    val isBotOrAdmin = msg.isFromBotOrAdmin
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = dragOffsetX, label = "swipe_reply_offset")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(msg.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffsetX) > 65f) {
                            onSwipeReply()
                        }
                        dragOffsetX = 0f
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-130f, 130f)
                    }
                )
            }
    ) {
        // Visual Reply Icon revealed behind bubble during horizontal swipe
        if (abs(animatedOffsetX) > 12f) {
            Box(
                modifier = Modifier
                    .align(if (animatedOffsetX > 0f) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FoulPurpleBright)
                    .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "رد",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) },
            horizontalArrangement = if (isBotOrAdmin) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            if (isBotOrAdmin) {
                AnimatedBotIcon(size = 38.dp)
                Spacer(Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isBotOrAdmin) 4.dp else 16.dp,
                    bottomEnd = if (isBotOrAdmin) 16.dp else 4.dp
                ),
                color = if (isBotOrAdmin) FoulCardDark else FoulPurpleBright,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .border(
                        1.dp,
                        if (isBotOrAdmin) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.25f),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isBotOrAdmin) 4.dp else 16.dp,
                            bottomEnd = if (isBotOrAdmin) 16.dp else 4.dp
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isBotOrAdmin) Color(0xFF38BDF8) else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isBotOrAdmin && msg.senderUid == "foul_tv_developer") {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = FoulLiveGreen.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = "رد المطور",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = FoulTextSecondary
                            )
                            IconButton(
                                onClick = onSwipeReply,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "رد على الرسالة",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Quoted Reply Box if this message is a reply to a swiped message
                    if (msg.replyToText.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FoulBlackBg.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "رداً على: ${msg.replyToSenderName.ifBlank { "رسالة سابقة" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = msg.replyToText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.82f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            if (!isBotOrAdmin) {
                Spacer(Modifier.width(8.dp))
                InAppCharacterAvatar(
                    avatarSpec = SocialFirebaseRepository.getAvatarById(msg.senderAvatarId),
                    size = 38.dp,
                    isSelected = false
                )
            }
        }
    }
}


@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FoulCardElevated,
    unfocusedContainerColor = FoulCardElevated,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = FoulDivider,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White
)
