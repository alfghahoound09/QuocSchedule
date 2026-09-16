package com.quoc.schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

// ── NEW Design System Colors (Material 3 + Minimalism) ──

// Light Mode
object LightColors {
    val background = Color(0xFFF8F9FC)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFF1F5F9)
    val border = Color(0xFFE2E8F0)
    val borderLight = Color(0xFFF1F5F9)

    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val textTertiary = Color(0xFF94A3B8)

    val primary = Color(0xFF3B82F6)
    val primaryVariant = Color(0xFF2563EB)
    val onPrimary = Color(0xFFFFFFFF)
}

// Dark Mode
object DarkColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)
    val border = Color(0xFF334155)
    val borderLight = Color(0xFF475569)

    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFFCBD5E1)
    val textTertiary = Color(0xFF94A3B8)

    val primary = Color(0xFF60A5FA)
    val primaryVariant = Color(0xFF3B82F6)
    val onPrimary = Color(0xFF0F172A)
}

// Subject Pastel Palette (7 colors) - Light bg / Dark accent
data class SubjectColorPair(val lightBg: Color, val darkAccent: Color)

val ExamRose = Color(0xFFFFD4D4)
val ExamRoseDark = Color(0xFFEC4899)
val PastelBlue = Color(0xFFDBEAFE)
val MintGreen = Color(0xFFD1FAE5)
val ColorPeach = Color(0xFFFFEDD5)
val BeigeAccent = Color(0xFFF5E6D3)

val SubjectPalette = listOf(
    SubjectColorPair(Color(0xFFE8F8F0), Color(0xFF10B981)), // mint
    SubjectColorPair(Color(0xFFEAF4FD), Color(0xFF3B82F6)), // sky
    SubjectColorPair(Color(0xFFF3EDFD), Color(0xFF8B5CF6)), // lavender
    SubjectColorPair(Color(0xFFFEF0EA), Color(0xFFF97316)), // peach
    SubjectColorPair(Color(0xFFFEF9E6), Color(0xFFF59E0B)), // lemon
    SubjectColorPair(Color(0xFFFEE7F0), Color(0xFFEC4899)), // rose
    SubjectColorPair(Color(0xFFFFF1ED), Color(0xFFEF4444))  // coral
)

// Semantic Colors
object SemanticColors {
    // Live Session
    val liveGreen = Color(0xFF10B981)
    val liveGreenBg = Color(0xFFD1FAE5)

    // Warning (Low confidence OCR)
    val warningYellow = Color(0xFFF59E0B)
    val warningYellowBg = Color(0xFFFEF3C7)

    // Exam Countdown / Error
    val urgentRed = Color(0xFFEF4444)
    val urgentRedBg = Color(0xFFFEE2E2)

    // Conflict
    val conflictOrange = Color(0xFFF97316)
    val conflictOrangeBg = Color(0xFFFFEDD5)
}

// Material 3 ColorSchemes
private val LightColorScheme = lightColorScheme(
    primary = LightColors.primary,
    onPrimary = LightColors.onPrimary,
    primaryContainer = Color(0xFFDCE9FF),
    onPrimaryContainer = LightColors.textPrimary,

    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = LightColors.textPrimary,

    tertiary = Color(0xFFEF4444),
    onTertiary = Color.White,
    tertiaryContainer = SemanticColors.urgentRedBg,
    onTertiaryContainer = LightColors.textPrimary,

    background = LightColors.background,
    onBackground = LightColors.textPrimary,

    surface = LightColors.surface,
    onSurface = LightColors.textPrimary,
    surfaceVariant = LightColors.surfaceVariant,
    onSurfaceVariant = LightColors.textSecondary,

    outline = LightColors.border,
    outlineVariant = LightColors.borderLight,

    error = SemanticColors.urgentRed,
    onError = Color.White,
    errorContainer = SemanticColors.urgentRedBg,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.primary,
    onPrimary = DarkColors.onPrimary,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = DarkColors.textPrimary,

    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = DarkColors.textPrimary,

    tertiary = Color(0xFFF87171),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF7F1D1D),
    onTertiaryContainer = DarkColors.textPrimary,

    background = DarkColors.background,
    onBackground = DarkColors.textPrimary,

    surface = DarkColors.surface,
    onSurface = DarkColors.textPrimary,
    surfaceVariant = DarkColors.surfaceVariant,
    onSurfaceVariant = DarkColors.textSecondary,

    outline = DarkColors.border,
    outlineVariant = DarkColors.borderLight,

    error = SemanticColors.urgentRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun QuocScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = QuocTypography,
        content = content
    )
}

/** Get subject card colors (background + accent) by subject code hash */
fun getSubjectColor(subjectCode: String, isDarkTheme: Boolean = false): Pair<Color, Color> {
    val index = subjectCode.hashCode().absoluteValue % SubjectPalette.size
    val pair = SubjectPalette[index]
    return if (isDarkTheme) {
        // Dark mode: dimmed background, brighter accent
        Pair(pair.lightBg.copy(alpha = 0.15f), pair.darkAccent)
    } else {
        // Light mode: pastel background, darker accent
        Pair(pair.lightBg, pair.darkAccent)
    }
}

/** Legacy support - convert colorKey to color */
@Deprecated("Use getSubjectColor(subjectCode) instead")
fun subjectCardColor(colorKey: Int, darkTheme: Boolean): Color {
    val pair = SubjectPalette[colorKey % SubjectPalette.size]
    return if (darkTheme) pair.lightBg.copy(alpha = 0.28f) else pair.lightBg
}
