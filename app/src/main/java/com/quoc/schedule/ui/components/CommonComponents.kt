package com.quoc.schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quoc.schedule.ui.theme.*

/**
 * Hero card displaying exam countdown with gradient background.
 * Designed to grab attention for upcoming exams.
 */
@Composable
fun ExamCountdownHero(
    daysRemaining: Int,
    examName: String,
    examCode: String,
    examTime: String,
    examRoom: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SemanticColors.urgentRedBg,
                        LightColors.background
                    )
                ),
                shape = RoundedCornerShape(Radius.xl)
            )
            .padding(Spacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔥 Sắp thi trong",
                style = MaterialTheme.typography.bodyLarge,
                color = SemanticColors.urgentRed,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Countdown value with gradient
            Text(
                text = "$daysRemaining NGÀY",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 40.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SemanticColors.urgentRed,
                            SemanticColors.conflictOrange
                        )
                    )
                ),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = examName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = LightColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "$examCode · $examTime · $examRoom",
                style = MaterialTheme.typography.labelMedium,
                color = LightColors.textSecondary
            )
        }
    }
}

/**
 * Empty state component with emoji and message.
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = LightColors.textSecondary
        )
    }
}

/**
 * Filter chips for exam list filtering.
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(ComponentHeight.chipSmall)
            .background(
                color = if (isSelected) LightColors.primary else Color.White,
                shape = RoundedCornerShape(Radius.full)
            )
            .border(
                width = BorderWidth.thin,
                color = if (isSelected) LightColors.primary else LightColors.border,
                shape = RoundedCornerShape(Radius.full)
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else LightColors.textSecondary
        )
    }
}

/**
 * Horizontal row of filter chips.
 */
@Composable
fun FilterChipRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        filters.forEach { filter ->
            FilterChip(
                label = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

/**
 * Section header with title.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.textPrimary
        )

        action?.invoke()
    }
}

/**
 * Loading shimmer effect for skeleton screens.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = LightColors.surfaceVariant,
                shape = RoundedCornerShape(Radius.sm)
            )
    )
}

/**
 * Info badge for additional metadata.
 */
@Composable
fun InfoBadge(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = LightColors.surfaceVariant,
                shape = RoundedCornerShape(Radius.sm)
            )
            .border(
                width = BorderWidth.thin,
                color = LightColors.borderLight,
                shape = RoundedCornerShape(Radius.sm)
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.textPrimary
        )
    }
}
