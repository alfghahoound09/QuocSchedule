package com.quoc.schedule.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.core.database.Exam
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.core.model.ExamType
import com.quoc.schedule.feature.timetable.ScheduleBottomBar
import com.quoc.schedule.ui.theme.ExamRose
import com.quoc.schedule.ui.theme.ExamRoseDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun ExamScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDeleteExam: (Exam) -> Unit = {},
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var examToDelete by remember { mutableStateOf<Pair<Exam, Subject>?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ScheduleBottomBar(selected = 1, onTimetable = onNavigateToTimetable, onExams = {}, onStats = onNavigateToStats, onSettings = onNavigateToSettings)
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Countdown header — điểm nhấn của tab Thi
                item {
                    state.nextExam?.let { next ->
                        CountdownCard(
                            daysLeft = next.daysLeft,
                            subjectName = next.subject.name,
                            subjectCode = next.subject.code,
                            examType = next.examType,
                            dateText = next.date.format(dateFormatter),
                            timeText = "%02d:%02d".format(next.startMinutes / 60, next.startMinutes % 60)
                        )
                    } ?: EmptyExamHint(onNavigateToTimetable)
                }

                // ── Upcoming exams sorted by date (soonest first) ──
                val sortedUpcoming = state.upcoming.sortedBy { it.first.examDate }
                items(sortedUpcoming, key = { it.first.id }) { (exam, subject) ->
                    ExamCard(exam = exam, subject = subject, past = false, onDelete = { examToDelete = exam to subject })
                }

                if (state.past.isNotEmpty()) {
                    item {
                        Text(
                            "Đã qua",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    val sortedPast = state.past.sortedByDescending { it.first.examDate }
                    items(sortedPast, key = { it.first.id }) { (exam, subject) ->
                        ExamCard(exam = exam, subject = subject, past = true, onDelete = { examToDelete = exam to subject })
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Xác nhận xóa lịch thi
    examToDelete?.let { (exam, subject) ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Xóa lịch thi?") },
            text = { Text("Môn ${subject.name} ngày ${LocalDate.parse(exam.examDate).format(dateFormatter)} sẽ bị xóa khỏi danh sách.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExam(exam)
                    examToDelete = null
                }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { examToDelete = null }) { Text("Giữ lại") } }
        )
    }
}

@Composable
fun CountdownCard(
    daysLeft: Long,
    subjectName: String,
    subjectCode: String,
    examType: ExamType,
    dateText: String,
    timeText: String
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(ExamRose, ExamRoseDark)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text("Môn thi tiếp theo", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            Text(
                if (daysLeft == 0L) "Hôm nay!" else "$daysLeft ngày",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$subjectName${if (subjectCode.isNotBlank()) " ($subjectCode)" else ""} — ${examTypeLabel(examType)}",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                "📆 $dateText · ⏰ $timeText",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EmptyExamHint(onNavigateToTimetable: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EventNote, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Text("Chưa có lịch thi nào", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Bạn có thể tải lịch thi từ ảnh hoặc file Word bằng cách thêm lịch mới.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNavigateToTimetable, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Thêm lịch thi ngay")
            }
        }
    }
}

@Composable
fun ExamCard(exam: Exam, subject: Subject, past: Boolean, onDelete: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (past) 0.5f else 1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header: subject name + exam type chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ExamTypeChip(exam.examType, past)
            }
            Spacer(Modifier.height(8.dp))

            // Date + Time
            Text(
                "📆 ${LocalDate.parse(exam.examDate).format(dateFormatter)} · ⏰ %02d:%02d".format(exam.startMinutes / 60, exam.startMinutes % 60),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // ── ROOM (PROMINENT) + SBD (BOLD) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                exam.room?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = androidx.compose.ui.graphics.Color(0xFFF59E0B).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📍", fontSize = 12.sp)
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color(0xFFF59E0B)
                            )
                        }
                    }
                }

                exam.candidateId?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExamRoseDark.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🏷", fontSize = 12.sp)
                            Text(
                                "SBD: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ExamRoseDark
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Text("🗑", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExamTypeChip(type: ExamType, past: Boolean) {
    val (label, color) = when (type) {
        ExamType.MIDTERM -> "Giữa kỳ" to MaterialTheme.colorScheme.secondaryContainer
        ExamType.FINAL -> "Cuối kỳ" to ExamRose
        ExamType.RETAKE -> "Thi lại" to MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (past) MaterialTheme.colorScheme.surfaceVariant else color
    ) {
        Text(
            if (past) "Đã qua" else label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun examTypeLabel(type: ExamType) = when (type) {
    ExamType.MIDTERM -> "Giữa kỳ"
    ExamType.FINAL -> "Cuối kỳ"
    ExamType.RETAKE -> "Thi lại"
}
