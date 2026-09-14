package com.quoc.schedule.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale for consistent padding/margins throughout the app.
 * Based on 4dp grid system.
 */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
    val gigantic = 48.dp
}

/**
 * Border radius values for cards, buttons, chips, etc.
 */
object Radius {
    val xs = 4.dp      // Small badges, tight corners
    val sm = 8.dp      // Tags, chips, small buttons
    val md = 12.dp     // Standard cards
    val lg = 16.dp     // Large cards, bottom sheets
    val xl = 20.dp     // Hero cards, modals
    val xxl = 24.dp    // Extra large surfaces
    val full = 999.dp  // Pills, circular elements
}

/**
 * Elevation values for Material 3 components.
 */
object Elevation {
    val none = 0.dp
    val xs = 1.dp      // Subtle lift
    val sm = 2.dp      // Hover states
    val md = 4.dp      // Cards, raised buttons
    val lg = 8.dp      // Floating action buttons
    val xl = 12.dp     // Dialogs, bottom sheets
    val xxl = 16.dp    // Modal overlays
}

/**
 * Icon sizes for consistent sizing.
 */
object IconSize {
    val xs = 12.dp
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

/**
 * Common component heights.
 */
object ComponentHeight {
    val button = 40.dp
    val buttonLarge = 48.dp
    val textField = 48.dp
    val chip = 32.dp
    val chipSmall = 28.dp
    val topBar = 64.dp
    val bottomNav = 80.dp
    val dayPill = 56.dp
}

/**
 * Border widths.
 */
object BorderWidth {
    val thin = 1.dp
    val medium = 2.dp
    val thick = 3.dp
}
