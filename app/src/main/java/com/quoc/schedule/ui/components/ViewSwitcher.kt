package com.quoc.schedule.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quoc.schedule.ui.theme.*

/**
 * View mode for timetable display.
 */
enum class ViewMode {
    TIMELINE,    // Single day timeline view (default portrait)
    WEEK_GRID    // Week grid view (default landscape)
}

/**
 * Segmented control-style view switcher for Timeline/Week Grid modes.
 * Provides smooth animations and haptic feedback.
 */
@Composable
fun ViewSwitcher(
    selectedMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .width(200.dp)
            .height(ComponentHeight.button)
            .background(Color.White, shape = RoundedCornerShape(Radius.xl))
            .border(BorderWidth.thin, LightColors.border, shape = RoundedCornerShape(Radius.xl))
            .padding(4.dp)
    ) {
        ViewButton(
            text = "📅 Ngày",
            isSelected = selectedMode == ViewMode.TIMELINE,
            onClick = {
                if (selectedMode != ViewMode.TIMELINE) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onModeChanged(ViewMode.TIMELINE)
                }
            },
            modifier = Modifier.weight(1f)
        )

        ViewButton(
            text = "📊 Tuần",
            isSelected = selectedMode == ViewMode.WEEK_GRID,
            onClick = {
                if (selectedMode != ViewMode.WEEK_GRID) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onModeChanged(ViewMode.WEEK_GRID)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ViewButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(
                if (isSelected) LightColors.primary else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else LightColors.textSecondary
        )
    }
}

/**
 * Animated content wrapper for view mode transitions.
 * Provides horizontal slide + fade animations.
 */
@Composable
fun AnimatedViewContent(
    viewMode: ViewMode,
    timelineContent: @Composable () -> Unit,
    weekGridContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = viewMode,
        transitionSpec = {
            if (targetState == ViewMode.WEEK_GRID) {
                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it }
                    ) + fadeOut(animationSpec = tween(300))
                )
            } else {
                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it }
                    ) + fadeOut(animationSpec = tween(300))
                )
            }
        },
        label = "viewModeTransition"
    ) { mode ->
        when (mode) {
            ViewMode.TIMELINE -> timelineContent()
            ViewMode.WEEK_GRID -> weekGridContent()
        }
    }
}
