package com.quoc.schedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quoc.schedule.R

// Be Vietnam Pro font family
// TODO: Add font files to res/font/ directory:
// - be_vietnam_pro_regular.ttf (400)
// - be_vietnam_pro_medium.ttf (500)
// - be_vietnam_pro_semibold.ttf (600)
// - be_vietnam_pro_bold.ttf (700)

// Using default font family as fallback until fonts are added
val BeVietnamPro = FontFamily.Default
/* Uncomment when fonts are added:
val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold)
)
*/

val QuocTypography = Typography(
    // Display - Hero elements (countdown, screen titles)
    displayLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),

    // Headline - Section headers
    headlineLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),

    // Title - Card headers, dialog titles
    titleLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),

    // Body - Main content text
    bodyLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),

    // Label - Buttons, chips, tags, badges
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
