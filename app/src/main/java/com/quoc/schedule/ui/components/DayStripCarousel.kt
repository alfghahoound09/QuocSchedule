package com.quoc.schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quoc.schedule.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Data class representing a day in the week strip.
 */
data class DayInfo(
    val dayOfWeek: DayOfWeek,
    val date: LocalDate,
    val sessionCount: Int,
    val isToday: Boolean = false
)

/**
 * Horizontal scrolling carousel of days with session count badges.
 * Auto-scrolls to selected day and provides haptic feedback.
 */
@Composable
fun DayStripCarousel(
    days: List<DayInfo>,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // Auto-scroll to selected day
    LaunchedEffect(selectedDay) {
        val index = days.indexOfFirst { it.date == selectedDay }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        items(days, key = { it.date.toString() }) { day ->
            DayPill(
                day = day,
                isSelected = day.date == selectedDay,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDaySelected(day.date)
                }
            )
        }
    }
}

/**
 * Individual day pill with session count badge.
 */
@Composable
fun DayPill(
    day: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(ComponentHeight.dayPill)
            .background(
                color = if (isSelected) {
                    LightColors.primary.copy(alpha = 0.12f)
                } else {
                    Color.White
                },
                shape = RoundedCornerShape(Radius.full)
            )
            .border(
                width = if (isSelected) BorderWidth.medium else BorderWidth.thin,
                color = if (isSelected) LightColors.primary else LightColors.border,
                shape = RoundedCornerShape(Radius.full)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getDayName(day.dayOfWeek),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) LightColors.primary else LightColors.textSecondary
            )
        }

        // Session count badge
        if (day.sessionCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .background(
                        color = if (isSelected) LightColors.primary else LightColors.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.sessionCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else LightColors.textSecondary
                )
            }
        }

        // Today indicator dot
        if (day.isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
                    .background(
                        color = LightColors.primary,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

/**
 * Get Vietnamese day name abbreviation.
 */
fun getDayName(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "T2"
        DayOfWeek.TUESDAY -> "T3"
        DayOfWeek.WEDNESDAY -> "T4"
        DayOfWeek.THURSDAY -> "T5"
        DayOfWeek.FRIDAY -> "T6"
        DayOfWeek.SATURDAY -> "T7"
        DayOfWeek.SUNDAY -> "CN"
    }
}

/**
 * Generate a week's worth of DayInfo from a start date.
 */
fun generateWeekDays(
    startDate: LocalDate,
    sessionCountMap: Map<LocalDate, Int> = emptyMap()
): List<DayInfo> {
    val today = LocalDate.now()
    return (0..6).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        DayInfo(
            dayOfWeek = date.dayOfWeek,
            date = date,
            sessionCount = sessionCountMap[date] ?: 0,
            isToday = date == today
        )
    }
}
