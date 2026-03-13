package com.chanmi.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ===== Material 3 Color Schemes =====

private val LightColorScheme = lightColorScheme(
    primary = AccentColorLight,
    onPrimary = OnAccentLight,
    primaryContainer = GoldAccentLight,
    onPrimaryContainer = Color(0xFF2C2610),

    secondary = GoldAccentLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE5D4),
    onSecondaryContainer = Color(0xFF3C3C2E),

    background = AppBackgroundLight,
    onBackground = Color(0xFF1C1B1A),

    surface = CardBackgroundLight,
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF4A4740),

    outline = OutlineLight,
    outlineVariant = Color(0xFFE0DAD0),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentColorDark,
    onPrimary = OnAccentDark,
    primaryContainer = Color(0xFF5C5040),
    onPrimaryContainer = Color(0xFFE8DCC8),

    secondary = GoldAccentDark,
    onSecondary = Color(0xFF1C1C1C),
    secondaryContainer = Color(0xFF4A4230),
    onSecondaryContainer = Color(0xFFE8DCC8),

    background = AppBackgroundDark,
    onBackground = Color(0xFFE6E1DB),

    surface = CardBackgroundDark,
    onSurface = Color(0xFFE6E1DB),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC8C3BA),

    outline = OutlineDark,
    outlineVariant = Color(0xFF3A3A3A),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

// ===== 커스텀 확장 컬러 (Material 3에 없는 앱 고유 색상) =====

@Immutable
data class ChanmiColors(
    val goldAccent: Color,
    val rosaryIndicator: Color,
    val goodDeedIndicator: Color,
    val sundayColor: Color,
    val saturdayColor: Color,
)

val LightChanmiColors = ChanmiColors(
    goldAccent = GoldAccentLight,
    rosaryIndicator = RosaryIndicator,
    goodDeedIndicator = GoodDeedIndicator,
    sundayColor = SundayColor,
    saturdayColor = SaturdayColor,
)

val DarkChanmiColors = ChanmiColors(
    goldAccent = GoldAccentDark,
    rosaryIndicator = RosaryIndicatorDark,
    goodDeedIndicator = GoodDeedIndicatorDark,
    sundayColor = SundayColorDark,
    saturdayColor = SaturdayColorDark,
)

val LocalChanmiColors = staticCompositionLocalOf { LightChanmiColors }

// ===== 테마 Composable =====

@Composable
fun ChanmiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val chanmiColors = if (darkTheme) DarkChanmiColors else LightChanmiColors

    // 상태바/네비게이션 바 색상 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalChanmiColors provides chanmiColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChanmiTypography,
            content = content
        )
    }
}

// ===== 편의 접근자 =====

/**
 * MaterialTheme.chanmiColors 로 커스텀 컬러에 접근
 *
 * 사용 예:
 * ```
 * val goldAccent = MaterialTheme.chanmiColors.goldAccent
 * val rosaryDot = MaterialTheme.chanmiColors.rosaryIndicator
 * ```
 */
val MaterialTheme.chanmiColors: ChanmiColors
    @Composable
    get() = LocalChanmiColors.current
