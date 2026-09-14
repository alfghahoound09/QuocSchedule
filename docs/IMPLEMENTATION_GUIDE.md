# QuocSchedule UI Implementation Guide

> Hướng dẫn triển khai Design System mới vào codebase Jetpack Compose hiện tại.

---

## Phase 1: Design Tokens Migration (Week 1)

### 1.1 Update Color Palette

**File:** `app/src/main/java/com/quoc/schedule/ui/theme/Color.kt`

```kotlin
package com.quoc.schedule.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
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

// Dark Mode Colors
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

// Subject Pastel Palette (existing + enhanced)
val SubjectPalette = listOf(
    // Mint
    Pair(Color(0xFFE8F8F0), Color(0xFF10B981)),
    // Sky
    Pair(Color(0xFFEAF4FD), Color(0xFF3B82F6)),
    // Lavender
    Pair(Color(0xFFF3EDFD), Color(0xFF8B5CF6)),
    // Peach
    Pair(Color(0xFFFEF0EA), Color(0xFFF97316)),
    // Lemon
    Pair(Color(0xFFFEF9E6), Color(0xFFF59E0B)),
    // Rose
    Pair(Color(0xFFFEE7F0), Color(0xFFEC4899)),
    // Coral
    Pair(Color(0xFFFFF1ED), Color(0xFFEF4444))
)

// Semantic Colors
object SemanticColors {
    val liveGreen = Color(0xFF10B981)
    val liveGreenBg = Color(0xFFD1FAE5)
    
    val warningYellow = Color(0xFFF59E0B)
    val warningYellowBg = Color(0xFFFEF3C7)
    
    val urgentRed = Color(0xFFEF4444)
    val urgentRedBg = Color(0xFFFEE2E2)
    
    val conflictOrange = Color(0xFFF97316)
    val conflictOrangeBg = Color(0xFFFFEDD5)
}

// Helper function: Get subject color by hash
fun getSubjectColor(subjectCode: String, isDark: Boolean = false): Pair<Color, Color> {
    val index = subjectCode.hashCode().absoluteValue % SubjectPalette.size
    return SubjectPalette[index]
}
```

### 1.2 Add Be Vietnam Pro Font

**Download fonts:**
1. Go to [Google Fonts - Be Vietnam Pro](https://fonts.google.com/specimen/Be+Vietnam+Pro)
2. Download Regular (400), Medium (500), SemiBold (600), Bold (700)
3. Place in `app/src/main/res/font/`

**File structure:**
```
app/src/main/res/font/
├── be_vietnam_pro_regular.ttf
├── be_vietnam_pro_medium.ttf
├── be_vietnam_pro_semibold.ttf
└── be_vietnam_pro_bold.ttf
```

**Update Type.kt:**

```kotlin
package com.quoc.schedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quoc.schedule.R

val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    // Display
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
        lineHeight = 32.sp
    ),
    
    // Title
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
    
    // Body
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
    
    // Label
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
```

### 1.3 Define Spacing & Radius

**Create:** `app/src/main/java/com/quoc/schedule/ui/theme/Dimensions.kt`

```kotlin
package com.quoc.schedule.ui.theme

import androidx.compose.ui.unit.dp

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
}

object Radius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val full = 999.dp
}

object Elevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
}
```

---

## Phase 2: Component Library (Week 2-3)

### 2.1 SmartScheduleCard

**Create:** `app/src/main/java/com/quoc/schedule/ui/components/SmartScheduleCard.kt`

```kotlin
package com.quoc.schedule.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quoc.schedule.core.model.*
import com.quoc.schedule.ui.theme.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun SmartScheduleCard(
    session: ClassSession,
    subject: Subject,
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) {
                bgColor.copy(alpha = 0.4f)
            } else {
                bgColor
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLive) Elevation.md else Elevation.sm
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isLive) 2.dp else if (hasConflict) 2.dp else 1.dp,
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
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (isLive) {
                        LiveBadge(alpha = pulseAlpha)
                    } else if (isCancelled) {
                        CancelledBadge()
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
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
                if (isLive && liveProgress > 0) {
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
                
                // Subject info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getSubjectEmoji(subject.code),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LightColors.textPrimary
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏰", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${formatMinutes(session.startMinutes)} – ${formatMinutes(session.endMinutes)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightColors.textPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                // Room tag
                RoomTag(room = session.room)
            }
        }
    }
}

@Composable
fun LiveBadge(alpha: Float = 1f) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = SemanticColors.liveGreenBg,
        modifier = Modifier.padding(0.dp)
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
fun RoomTag(room: String) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = LightColors.surfaceVariant,
        border = BorderStroke(1.dp, LightColors.borderLight)
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

// Helpers
fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}

fun getSubjectEmoji(code: String): String {
    return when {
        code.startsWith("IT") || code.startsWith("INT") -> "💻"
        code.startsWith("MI") || code.startsWith("MA") -> "🧮"
        code.startsWith("PH") -> "⚛️"
        code.startsWith("CH") -> "🧪"
        code.startsWith("FL") || code.startsWith("ENG") -> "🗣️"
        else -> "📘"
    }
}
```

### 2.2 DayStripCarousel

**Create:** `app/src/main/java/com/quoc/schedule/ui/components/DayStripCarousel.kt`

```kotlin
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

data class DayInfo(
    val dayOfWeek: DayOfWeek,
    val date: LocalDate,
    val sessionCount: Int,
    val isToday: Boolean
)

@Composable
fun DayStripCarousel(
    days: List<DayInfo>,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    
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
        contentPadding = PaddingValues(horizontal = Spacing.lg)
    ) {
        items(days) { day ->
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

@Composable
fun DayPill(
    day: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(56.dp)
            .background(
                color = if (isSelected) {
                    LightColors.primary.copy(alpha = 0.12f)
                } else {
                    Color.White
                },
                shape = RoundedCornerShape(Radius.full)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
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
    }
}

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
```

### 2.3 ViewSwitcherToggle

**Create:** `app/src/main/java/com/quoc/schedule/ui/components/ViewSwitcher.kt`

```kotlin
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

enum class ViewMode {
    TIMELINE, WEEK_GRID
}

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
            .height(40.dp)
            .background(Color.White, shape = RoundedCornerShape(Radius.xl))
            .border(1.dp, LightColors.border, shape = RoundedCornerShape(Radius.xl))
            .padding(4.dp)
    ) {
        ViewButton(
            text = "📅 Ngày",
            isSelected = selectedMode == ViewMode.TIMELINE,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onModeChanged(ViewMode.TIMELINE)
            },
            modifier = Modifier.weight(1f)
        )
        
        ViewButton(
            text = "📊 Tuần",
            isSelected = selectedMode == ViewMode.WEEK_GRID,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onModeChanged(ViewMode.WEEK_GRID)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ViewButton(
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
                (slideInHorizontally { it } + fadeIn()).togetherWith(
                    slideOutHorizontally { -it } + fadeOut()
                )
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(
                    slideOutHorizontally { it } + fadeOut()
                )
            }.using(tween(300))
        },
        label = "viewModeTransition"
    ) { mode ->
        when (mode) {
            ViewMode.TIMELINE -> timelineContent()
            ViewMode.WEEK_GRID -> weekGridContent()
        }
    }
}
```

---

## Phase 3: Screen Refactoring (Week 4-5)

### 3.1 Update TimetableScreen with Timeline/Grid Switcher

**File:** `app/src/main/java/com/quoc/schedule/feature/timetable/TimetableScreen.kt`

```kotlin
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var viewMode by remember { mutableStateOf(ViewMode.TIMELINE) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Lịch học",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightColors.background)
        ) {
            // Day Strip
            DayStripCarousel(
                days = generateDayInfoList(uiState.sessions),
                selectedDay = selectedDate,
                onDaySelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // View Switcher
            ViewSwitcher(
                selectedMode = viewMode,
                onModeChanged = { viewMode = it },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Animated content
            AnimatedViewContent(
                viewMode = viewMode,
                timelineContent = {
                    TimelineView(
                        sessions = uiState.getSessionsForDate(selectedDate),
                        onSessionClick = { /* TODO */ },
                        onSessionMenu = { /* TODO */ }
                    )
                },
                weekGridContent = {
                    WeekGridView(
                        sessions = uiState.sessions,
                        onSessionClick = { /* TODO */ }
                    )
                }
            )
        }
    }
}
```

### 3.2 TimelineView Implementation

```kotlin
@Composable
fun TimelineView(
    sessions: List<Pair<ClassSession, Subject>>,
    onSessionClick: (ClassSession) -> Unit,
    onSessionMenu: (ClassSession) -> Unit
) {
    if (sessions.isEmpty()) {
        EmptyState(
            emoji = "🎉",
            title = "Không có tiết học hôm nay",
            subtitle = "Nghỉ ngơi và tận hưởng cuối tuần!"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(sessions) { (session, subject) ->
            val now = LocalDateTime.now()
            val isLive = /* check if now is between session start/end */
            val progress = /* calculate progress */
            
            SmartScheduleCard(
                session = session,
                subject = subject,
                isLive = isLive,
                liveProgress = progress,
                onClick = { onSessionClick(session) },
                onMenuClick = { onSessionMenu(session) }
            )
        }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
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
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = LightColors.textSecondary
        )
    }
}
```

---

## Phase 4: Micro-interactions & Polish (Week 6)

### 4.1 Haptic Feedback Integration

**Update ViewModel to expose haptic events:**

```kotlin
// In TimetableViewModel
sealed class HapticEvent {
    object DaySelected : HapticEvent()
    object ViewSwitched : HapticEvent()
    object DragStarted : HapticEvent()
    data class DropResult(val success: Boolean) : HapticEvent()
}

private val _hapticEvents = MutableSharedFlow<HapticEvent>()
val hapticEvents = _hapticEvents.asSharedFlow()
```

### 4.2 Drag-and-Drop with Conflict Detection

```kotlin
@Composable
fun DraggableSessionCard(
    session: ClassSession,
    subject: Subject,
    onDragEnd: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = dragOffset.x
                translationY = dragOffset.y
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, dragAmount ->
                        dragOffset += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd(dragOffset)
                        dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        SmartScheduleCard(
            session = session,
            subject = subject,
            onClick = {},
            onMenuClick = {}
        )
    }
}
```

---

## Testing Checklist

### Visual Testing
- [ ] All colors meet WCAG AA contrast ratio (4.5:1 for normal text)
- [ ] Dark mode renders correctly without color bleeding
- [ ] Font sizes ≥ 11sp on all text elements
- [ ] Touch targets ≥ 48×48dp

### Interaction Testing
- [ ] Haptic feedback fires on day selection
- [ ] View switcher animates smoothly (no jank)
- [ ] Timeline scroll is smooth with 60fps
- [ ] Live session progress updates every minute
- [ ] Drag-and-drop detects conflicts correctly

### Accessibility Testing
- [ ] TalkBack reads all UI elements correctly
- [ ] Focus order is logical (top to bottom, left to right)
- [ ] Screen reader announces live session status
- [ ] Color is not the only means of conveying information

---

## Performance Optimization

### LazyColumn Optimization
```kotlin
LazyColumn {
    items(
        items = sessions,
        key = { it.id } // Stable keys for recomposition
    ) { session ->
        SmartScheduleCard(...)
    }
}
```

### Image Loading (if adding subject icons later)
```kotlin
// Use Coil for async image loading
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(subject.iconUrl)
        .crossfade(true)
        .build(),
    contentDescription = subject.name
)
```

---

## Migration Path

**Week 1:** Design tokens (colors, fonts, spacing)  
**Week 2:** Core components (SmartScheduleCard, DayStrip)  
**Week 3:** Layout components (ViewSwitcher, Timeline)  
**Week 4:** Refactor TimetableScreen  
**Week 5:** Refactor ExamScreen + ImportFlow  
**Week 6:** Micro-interactions + Polish  
**Week 7:** Testing + Bug fixes  
**Week 8:** Release v0.3

---

**Next Steps:**
1. Add Be Vietnam Pro fonts to `res/font/`
2. Run `./gradlew test` to ensure normalizer tests still pass
3. Update `CLAUDE.md` with new design system reference
4. Create Figma prototype for stakeholder approval
