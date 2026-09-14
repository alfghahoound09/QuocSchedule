package com.quoc.schedule.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.ui.components.*
import com.quoc.schedule.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime

/**
 * Main Timetable Screen with Timeline/Week Grid view switcher.
 * Implements the new design system with day strip carousel and smart schedule cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onNavigateToImport: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf(ViewMode.TIMELINE) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        containerColor = LightColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch học",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightColors.background,
                    titleContentColor = LightColors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = LightColors.textSecondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToImport,
                containerColor = LightColors.primary,
                contentColor = LightColors.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nhập lịch")
            }
        },
        bottomBar = {
            ScheduleBottomBar(
                selected = 0,
                onTimetable = {},
                onExams = onNavigateToExams,
                onStats = onNavigateToStats,
                onSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightColors.background)
        ) {
            // Day Strip Carousel
            val days = generateWeekDays(
                startDate = state.weekStart,
                sessionCountMap = state.sessionCountByDate
            )

            DayStripCarousel(
                days = days,
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

            // Animated content based on view mode
            AnimatedViewContent(
                viewMode = viewMode,
                timelineContent = {
                    TimelineView(
                        entries = state.getEntriesForDate(selectedDate),
                        onSessionClick = { /* TODO: Open detail */ },
                        onSessionMenu = { /* TODO: Show menu */ }
                    )
                },
                weekGridContent = {
                    WeekGridViewPlaceholder(state.weekStart)
                }
            )
        }
    }
}

/**
 * Timeline view - single day vertical schedule.
 */
@Composable
private fun TimelineView(
    entries: List<com.quoc.schedule.domain.TimetableEntry>,
    onSessionClick: (com.quoc.schedule.domain.TimetableEntry) -> Unit,
    onSessionMenu: (com.quoc.schedule.domain.TimetableEntry) -> Unit
) {
    if (entries.isEmpty()) {
        EmptyState(
            emoji = "🎉",
            title = "Không có tiết học hôm nay",
            subtitle = "Nghỉ ngơi và tận hưởng thời gian rảnh!"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(entries, key = { it.sessionId }) { entry ->
            val currentTime = LocalTime.now()
            val (isLive, progress) = calculateLiveStatus(
                session = entry.toClassSession(),
                currentTime = currentTime
            )

            SmartScheduleCard(
                session = entry.toClassSession(),
                subject = entry.subject,
                isLive = isLive,
                liveProgress = progress,
                hasConflict = entry.hasConflict,
                isCancelled = entry.isCancelled,
                onClick = { onSessionClick(entry) },
                onMenuClick = { onSessionMenu(entry) }
            )
        }
    }
}

/**
 * Week Grid view placeholder - will be implemented with full drag-drop support.
 */
@Composable
private fun WeekGridViewPlaceholder(weekStart: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Chế độ xem tuần",
                style = MaterialTheme.typography.titleLarge,
                color = LightColors.textPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "Đang phát triển - kéo thả lịch học",
                style = MaterialTheme.typography.bodyMedium,
                color = LightColors.textSecondary
            )
        }
    }
}

/**
 * Bottom navigation bar.
 */
@Composable
private fun ScheduleBottomBar(
    selected: Int,
    onTimetable: () -> Unit,
    onExams: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(
        containerColor = LightColors.surface,
        contentColor = LightColors.textPrimary
    ) {
        NavigationBarItem(
            icon = { Text("📅", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Lịch học", style = MaterialTheme.typography.labelSmall) },
            selected = selected == 0,
            onClick = onTimetable
        )
        NavigationBarItem(
            icon = { Text("📝", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Lịch thi", style = MaterialTheme.typography.labelSmall) },
            selected = selected == 1,
            onClick = onExams
        )
        NavigationBarItem(
            icon = { Text("📊", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Thống kê", style = MaterialTheme.typography.labelSmall) },
            selected = selected == 2,
            onClick = onStats
        )
        NavigationBarItem(
            icon = { Text("⚙️", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Cài đặt", style = MaterialTheme.typography.labelSmall) },
            selected = selected == 3,
            onClick = onSettings
        )
    }
}

// Extension to convert TimetableEntry to ClassSession for SmartScheduleCard
private fun com.quoc.schedule.domain.TimetableEntry.toClassSession(): com.quoc.schedule.core.database.ClassSession {
    return com.quoc.schedule.core.database.ClassSession(
        id = sessionId,
        subjectId = subject.id,
        dayOfWeek = dayOfWeek,
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        room = room,
        weekPattern = weekPattern
    )
}

// Extension for UI state to get entries by date
private fun TimetableUiState.getEntriesForDate(date: LocalDate): List<com.quoc.schedule.domain.TimetableEntry> {
    return entries.filter { entry ->
        val entryDate = weekStart.plusDays(entry.dayOfWeek - 1L)
        entryDate == date
    }.sortedBy { it.startMinutes }
}

// Extension to calculate session counts by date
private val TimetableUiState.sessionCountByDate: Map<LocalDate, Int>
    get() {
        return entries.groupBy { entry ->
            weekStart.plusDays(entry.dayOfWeek - 1L)
        }.mapValues { it.value.size }
    }
