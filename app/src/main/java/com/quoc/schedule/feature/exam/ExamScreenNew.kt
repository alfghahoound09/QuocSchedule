package com.quoc.schedule.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.core.database.Exam
import com.quoc.schedule.ui.components.*
import com.quoc.schedule.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Exam Screen with countdown hero and filter chips.
 * New design system implementation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    val filters = listOf("Tất cả", "Sắp thi", "Đã qua")

    Scaffold(
        containerColor = LightColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch thi",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightColors.background,
                    titleContentColor = LightColors.textPrimary
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Add exam */ }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Thêm lịch thi",
                            tint = LightColors.textSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LightColors.surface,
                contentColor = LightColors.textPrimary
            ) {
                NavigationBarItem(
                    icon = { Text("📅", style = MaterialTheme.typography.titleMedium) },
                    label = { Text("Lịch học", style = MaterialTheme.typography.labelSmall) },
                    selected = false,
                    onClick = onNavigateToTimetable
                )
                NavigationBarItem(
                    icon = { Text("📝", style = MaterialTheme.typography.titleMedium) },
                    label = { Text("Lịch thi", style = MaterialTheme.typography.labelSmall) },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Text("📊", style = MaterialTheme.typography.titleMedium) },
                    label = { Text("Thống kê", style = MaterialTheme.typography.labelSmall) },
                    selected = false,
                    onClick = onNavigateToStats
                )
                NavigationBarItem(
                    icon = { Text("⚙️", style = MaterialTheme.typography.titleMedium) },
                    label = { Text("Cài đặt", style = MaterialTheme.typography.labelSmall) },
                    selected = false,
                    onClick = onNavigateToSettings
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightColors.background)
        ) {
            // Hero countdown for nearest exam
            val upcomingExam = state.upcoming.firstOrNull()

            if (upcomingExam != null) {
                val examDate = LocalDate.parse(upcomingExam.first.examDate)
                val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), examDate).toInt()

                if (daysRemaining >= 0 && daysRemaining <= 30) {
                    val startTime = String.format("%02d:%02d", upcomingExam.first.startMinutes / 60, upcomingExam.first.startMinutes % 60)
                    ExamCountdownHero(
                        daysRemaining = daysRemaining,
                        examName = upcomingExam.second.name,
                        examCode = upcomingExam.second.code,
                        examTime = startTime,
                        examRoom = upcomingExam.first.room ?: "Chưa rõ",
                        modifier = Modifier.padding(Spacing.lg)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Filter chips
            FilterChipRow(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            // Exam list
            val filteredExams = when (selectedFilter) {
                "Sắp thi" -> state.upcoming
                "Đã qua" -> state.past
                else -> state.upcoming + state.past
            }

            if (filteredExams.isEmpty()) {
                EmptyState(
                    emoji = "📚",
                    title = "Chưa có lịch thi",
                    subtitle = "Thêm lịch thi để theo dõi kỳ thi sắp tới"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(filteredExams, key = { it.first.id }) { examPair ->
                        ExamCard(
                            exam = examPair.first,
                            subject = examPair.second,
                            onEdit = { /* TODO */ },
                            onDelete = { viewModel.deleteExam(examPair.first) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exam card with subject info, date, room, and student ID.
 */
@Composable
private fun ExamCard(
    exam: Exam,
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (bgColor, accentColor) = getSubjectColor(subject.code)
    val examDate = LocalDate.parse(exam.examDate)
    val isPast = examDate.isBefore(LocalDate.now())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.7f else 1f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.sm
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // Header with subject name and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getSubjectEmoji(subject.code),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LightColors.textPrimary,
                            maxLines = 2
                        )
                    }
                    Text(
                        text = subject.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = LightColors.textSecondary
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = LightColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = SemanticColors.urgentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Date and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📅", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(Spacing.xs))
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val startTime = String.format("%02d:%02d", exam.startMinutes / 60, exam.startMinutes % 60)
                Text(
                    text = "${examDate.format(formatter)} · $startTime · ${exam.durationMinutes}p",
                    style = MaterialTheme.typography.labelLarge,
                    color = LightColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Room and candidate ID badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (exam.room != null) {
                    InfoBadge(icon = "📍", text = exam.room)
                }
                if (exam.candidateId != null) {
                    InfoBadge(icon = "🪪", text = "SBD: ${exam.candidateId}")
                }
            }

            // Exam type
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = exam.examType.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = LightColors.textSecondary
                )
            }
        }
    }
}
