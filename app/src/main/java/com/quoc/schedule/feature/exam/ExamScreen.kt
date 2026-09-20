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
import androidx.compose.foundation.clickable
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
    var editingExam by remember { mutableStateOf<Pair<Exam, Subject>?>(null) }

    val startAddingNew = {
        editingExam = Exam(
            id = -1,
            subjectId = -1,
            examDate = LocalDate.now().format(dateFormatter),
            startMinutes = 7 * 60 + 30,
            durationMinutes = 90,
            room = "",
            candidateId = null,
            examType = ExamType.FINAL
        ) to Subject(id = -1, code = "", name = "", lecturer = "", colorKey = 0)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (state.upcoming.isNotEmpty() || state.past.isNotEmpty()) {
                FloatingActionButton(
                    onClick = startAddingNew,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Rounded.Add, contentDescription = "ThÃªm lá»‹ch thi") }
            }
        },
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
                // Countdown header â€” Ä‘iá»ƒm nháº¥n cá»§a tab Thi
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
                    } ?: EmptyExamHint(startAddingNew)
                }

                // â”€â”€ Upcoming exams sorted by date (soonest first) â”€â”€
                val sortedUpcoming = state.upcoming.sortedBy { it.first.examDate }
                items(sortedUpcoming, key = { it.first.id }) { (exam, subject) ->
                    ExamCard(exam = exam, subject = subject, past = false, onClick = { editingExam = exam to subject }, onDelete = { examToDelete = exam to subject })
                }

                if (state.past.isNotEmpty()) {
                    item {
                        Text(
                            "ÄÃ£ qua",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    val sortedPast = state.past.sortedByDescending { it.first.examDate }
                    items(sortedPast, key = { it.first.id }) { (exam, subject) ->
                        ExamCard(exam = exam, subject = subject, past = true, onClick = { editingExam = exam to subject }, onDelete = { examToDelete = exam to subject })
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    editingExam?.let { (exam, subject) ->
        EditExamSheet(
            exam = exam,
            subject = subject,
            onDismiss = { editingExam = null },
            onSave = { subjectName, room, dateStr, startMinutes, durationMinutes, type ->
                viewModel.saveExam(exam.id, subjectName, room, dateStr, startMinutes, durationMinutes, type)
                editingExam = null
            },
            onDelete = {
                examToDelete = exam to subject
                editingExam = null
            }
        )
    }

    // XÃ¡c nháº­n xÃ³a lá»‹ch thi
    examToDelete?.let { (exam, subject) ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("XÃ³a lá»‹ch thi?") },
            text = { Text("MÃ´n ${subject.name} ngÃ y ${LocalDate.parse(exam.examDate).format(dateFormatter)} sáº½ bá»‹ xÃ³a khá»i danh sÃ¡ch.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExam(exam)
                    examToDelete = null
                }) { Text("XÃ³a", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { examToDelete = null }) { Text("Giá»¯ láº¡i") } }
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
            Text("MÃ´n thi tiáº¿p theo", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            Text(
                if (daysLeft == 0L) "HÃ´m nay!" else "$daysLeft ngÃ y",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$subjectName${if (subjectCode.isNotBlank()) " ($subjectCode)" else ""} â€” ${examTypeLabel(examType)}",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                "ðŸ“† $dateText Â· â° $timeText",
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
            Text("ChÆ°a cÃ³ lá»‹ch thi nÃ o", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Báº¡n cÃ³ thá»ƒ táº£i lá»‹ch thi tá»« áº£nh hoáº·c file Word báº±ng cÃ¡ch thÃªm lá»‹ch má»›i.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNavigateToTimetable, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ThÃªm lá»‹ch thi ngay")
            }
        }
    }
}

@Composable
fun ExamCard(exam: Exam, subject: Subject, past: Boolean, onClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (past) 0.5f else 1f).clickable { onClick() },
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
                "ðŸ“† ${LocalDate.parse(exam.examDate).format(dateFormatter)} Â· â° %02d:%02d".format(exam.startMinutes / 60, exam.startMinutes % 60),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // â”€â”€ ROOM (PROMINENT) + SBD (BOLD) â”€â”€
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
                            Text("ðŸ“", fontSize = 12.sp)
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
                            Text("ðŸ·", fontSize = 12.sp)
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
                    Text("ðŸ—‘", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExamTypeChip(type: ExamType, past: Boolean) {
    val (label, color) = when (type) {
        ExamType.MIDTERM -> "Giá»¯a ká»³" to MaterialTheme.colorScheme.secondaryContainer
        ExamType.FINAL -> "Cuá»‘i ká»³" to ExamRose
        ExamType.RETAKE -> "Thi láº¡i" to MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (past) MaterialTheme.colorScheme.surfaceVariant else color
    ) {
        Text(
            if (past) "ÄÃ£ qua" else label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun examTypeLabel(type: ExamType) = when (type) {
    ExamType.MIDTERM -> "Giá»¯a ká»³"
    ExamType.FINAL -> "Cuá»‘i ká»³"
    ExamType.RETAKE -> "Thi láº¡i"
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditExamSheet(
    exam: Exam,
    subject: Subject,
    onDismiss: () -> Unit,
    onSave: (subjectName: String, room: String, dateStr: String, startMinutes: Int, durationMinutes: Int, type: ExamType) -> Unit,
    onDelete: () -> Unit
) {
    var subjectName by remember { mutableStateOf(subject.name) }
    var room by remember { mutableStateOf(exam.room ?: "") }
    var dateStr by remember { mutableStateOf(exam.examDate) }
    var startTime by remember { mutableStateOf("%02d:%02d".format(exam.startMinutes / 60, exam.startMinutes % 60)) }
    var duration by remember { mutableStateOf(exam.durationMinutes.toString()) }
    var examType by remember { mutableStateOf(exam.examType) }

    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        ) {
            Text(if (exam.id == -1L) "Thêm lịch thi" else "Chỉnh sửa lịch thi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            androidx.compose.material3.OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("Tên môn học") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Phòng thi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Ngày (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Bắt đầu (HH:mm)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Thời lượng (phút)") },
                    modifier = Modifier.weight(1f)
                )
                
                Box(Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    androidx.compose.material3.OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(if (examType == ExamType.MIDTERM) "Giữa kỳ" else "Cuối kỳ")
                    }
                    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        androidx.compose.material3.DropdownMenuItem(text = { Text("Giữa kỳ") }, onClick = { examType = ExamType.MIDTERM; expanded = false })
                        androidx.compose.material3.DropdownMenuItem(text = { Text("Cuối kỳ") }, onClick = { examType = ExamType.FINAL; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (exam.id != -1L) {
                    TextButton(onClick = onDelete, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Xóa")
                    }
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
                androidx.compose.material3.Button(onClick = {
                    val startM = startTime.split(":").let { it.getOrNull(0)?.toIntOrNull()?.times(60)?.plus(it.getOrNull(1)?.toIntOrNull() ?: 0) } ?: exam.startMinutes
                    val durM = duration.toIntOrNull() ?: exam.durationMinutes
                    onSave(subjectName, room, dateStr, startM, durM, examType)
                }) {
                    Text("Lưu")
                }
            }
        }
    }
}
