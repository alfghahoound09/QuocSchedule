package com.quoc.schedule.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quoc.schedule.core.database.ClassSession
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.ui.theme.*
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Smart Schedule Card - Adaptive session card with live status, progress, and conflict detection.
 *
 * Features:
 * - Live badge with pulse animation when session is active
 * - Progress bar showing elapsed time
 * - Pastel background based on subject code hash
 * - Conflict and cancelled state indicators
 * - Haptic feedback on interactions
 */
@Composable
fun SmartScheduleCard(
    session: ClassSession,
    subject: Subject,
    modifier: Modifier = Modifier,
    isLive: Boolean = false,
    liveProgress: Float = 0f,
    hasConflict: Boolean = false,
    isCancelled: Boolean = false,
    onClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val (bgColor, accentColor) = getSubjectColor(subject.code)

    // Pulse animation for live badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val cardAlpha = if (isCancelled) 0.6f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLive) Elevation.md else Elevation.sm
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = when {
                        isLive -> BorderWidth.medium
                        hasConflict -> BorderWidth.medium
                        else -> BorderWidth.thin
                    },
                    color = when {
                        isLive -> SemanticColors.liveGreen
                        hasConflict -> SemanticColors.conflictOrange
                        else -> LightColors.border
                    },
                    shape = RoundedCornerShape(Radius.lg)
                )
                .padding(Spacing.lg)
        ) {
            Column {
                // Header row with badges and menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    when {
                        isLive -> LiveBadge(alpha = pulseAlpha)
                        isCancelled -> CancelledBadge()
                        hasConflict -> ConflictBadge()
                        else -> Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMenuClick()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = LightColors.textSecondary
                        )
                    }
                }

                // Live progress bar
                if (isLive && liveProgress > 0f) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    LinearProgressIndicator(
                        progress = { liveProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(Radius.sm)),
                        color = SemanticColors.liveGreen,
                        trackColor = LightColors.border,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Subject info with emoji
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = getSubjectEmoji(subject.code),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LightColors.textPrimary,
                            maxLines = 2
                        )
                        Text(
                            text = subject.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = LightColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Time slot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⏰", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${formatMinutes(session.startMinutes)} – ${formatMinutes(session.endMinutes)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Room tag
                RoomTag(room = session.room ?: "Chưa rõ")
            }
        }
    }
}

@Composable
fun LiveBadge(alpha: Float = 1f) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = SemanticColors.liveGreenBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        SemanticColors.liveGreen.copy(alpha = alpha),
                        shape = RoundedCornerShape(50)
                    )
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Đang học",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SemanticColors.liveGreen
            )
        }
    }
}

@Composable
fun CancelledBadge() {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = SemanticColors.urgentRedBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✗ Nghỉ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SemanticColors.urgentRed
            )
        }
    }
}

@Composable
fun ConflictBadge() {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = SemanticColors.conflictOrangeBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠ Trùng",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SemanticColors.conflictOrange
            )
        }
    }
}

@Composable
fun RoomTag(room: String) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = LightColors.surfaceVariant,
        border = BorderStroke(BorderWidth.thin, LightColors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = room,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LightColors.textPrimary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

// Helper functions
fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}

fun getSubjectEmoji(code: String): String {
    return when {
        code.startsWith("IT", ignoreCase = true) ||
        code.startsWith("INT", ignoreCase = true) -> "💻"
        code.startsWith("MI", ignoreCase = true) ||
        code.startsWith("MA", ignoreCase = true) -> "🧮"
        code.startsWith("PH", ignoreCase = true) -> "⚛️"
        code.startsWith("CH", ignoreCase = true) -> "🧪"
        code.startsWith("FL", ignoreCase = true) ||
        code.startsWith("ENG", ignoreCase = true) -> "🗣️"
        code.startsWith("PE", ignoreCase = true) ||
        code.startsWith("TC", ignoreCase = true) -> "⚽"
        else -> "📘"
    }
}

/**
 * Calculate if a session is currently live and its progress.
 * Returns Pair<isLive, progress> where progress is 0.0-1.0
 */
fun calculateLiveStatus(session: ClassSession, currentTime: LocalTime = LocalTime.now()): Pair<Boolean, Float> {
    val currentMinutes = currentTime.hour * 60 + currentTime.minute

    if (currentMinutes < session.startMinutes || currentMinutes > session.endMinutes) {
        return Pair(false, 0f)
    }

    val duration = session.endMinutes - session.startMinutes
    val elapsed = currentMinutes - session.startMinutes
    val progress = if (duration > 0) elapsed.toFloat() / duration.toFloat() else 0f

    return Pair(true, progress.coerceIn(0f, 1f))
}
