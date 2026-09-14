package com.quoc.schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Bảng màu pastel theo ARCHITECTURE.md §5.2 ──
val PastelBlue = Color(0xFF7FB3D9)
val PastelBlueDark = Color(0xFF5B8FB0)
val MintGreen = Color(0xFFA8D8C8)
val MintDark = Color(0xFF6FA892)
val CreamBg = Color(0xFFFAFAF7)
val BeigeAccent = Color(0xFFF2E8D5)
val ExamRose = Color(0xFFF5B8B1)
val ExamRoseDark = Color(0xFF8C5652)
val InkLight = Color(0xFF2E3A45)
val InkDark = Color(0xFFE8E6E1)
val DarkBg = Color(0xFF1C1E20)
val DarkSurface = Color(0xFF26292C)
val DarkBeige = Color(0xFF4A4238)
val ColorPeach = Color(0xFFF7D9C4)

/** 7 màu card môn học — index theo Subject.colorKey (hash ổn định từ mã HP). */
val SubjectPalette = listOf(
    Color(0xFFC9EBDD), // mint
    Color(0xFFC7E0F2), // sky
    Color(0xFFF2E8D5), // beige
    Color(0xFFF6D3D0), // rose
    Color(0xFFDCD3EE), // lavender
    Color(0xFFF6EDC8), // lemon
    Color(0xFFF7D9C4)  // peach
)

private val LightColors = lightColorScheme(
    primary = PastelBlueDark,
    onPrimary = Color.White,
    primaryContainer = PastelBlue,
    onPrimaryContainer = InkLight,
    secondary = MintDark,
    secondaryContainer = MintGreen,
    onSecondaryContainer = InkLight,
    tertiary = ExamRoseDark,
    tertiaryContainer = ExamRose,
    background = CreamBg,
    onBackground = InkLight,
    surface = Color.White,
    onSurface = InkLight,
    surfaceVariant = BeigeAccent,
    onSurfaceVariant = InkLight.copy(alpha = 0.72f),
    outline = Color(0xFFD8D4CB)
)

private val DarkColors = darkColorScheme(
    primary = PastelBlue,
    onPrimary = Color(0xFF10222E),
    primaryContainer = PastelBlueDark,
    onPrimaryContainer = InkDark,
    secondary = MintGreen,
    secondaryContainer = MintDark,
    onSecondaryContainer = InkDark,
    tertiary = ExamRose,
    tertiaryContainer = ExamRoseDark,
    background = DarkBg,
    onBackground = InkDark,
    surface = DarkSurface,
    onSurface = InkDark,
    surfaceVariant = DarkBeige,
    onSurfaceVariant = InkDark.copy(alpha = 0.72f),
    outline = Color(0xFF454A4E)
)

@Composable
fun QuocScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = QuocTypography,
        content = content
    )
}

/** Màu nền card cho một môn học (ổn định theo colorKey). */
fun subjectCardColor(colorKey: Int, darkTheme: Boolean): Color =
    if (darkTheme) SubjectPalette[colorKey % SubjectPalette.size].copy(alpha = 0.28f)
    else SubjectPalette[colorKey % SubjectPalette.size]
