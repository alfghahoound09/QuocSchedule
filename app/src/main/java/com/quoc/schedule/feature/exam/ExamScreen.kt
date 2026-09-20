package com.quoc.schedule.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.quoc.schedule.R
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
                ) { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.str_th__m_l___ch_thi)) }
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
                    } ?: EmptyExamHint(startAddingNew)
                }

                // ── Upcoming exams sorted by date (soonest first) ──
                val sortedUpcoming = state.upcoming.sortedBy { it.first.examDate }
                items(sortedUpcoming, key = { it.first.id }) { (exam, subject) ->
                    ExamCard(exam = exam, subject = subject, past = false, onClick = { editingExam = exam to subject }, onDelete = { examToDelete = exam to subject })
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

    // Xác nhận xóa lịch thi
    examToDelete?.let { (exam, subject) ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text(stringResource(R.string.str_x__a_l___ch_thi)) },
            text = { Text("Môn ${subject.name} ngày ${LocalDate.parse(exam.examDate).format(dateFormatter)} sẽ bị xóa khỏi danh sách.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExam(exam)
                    examToDelete = null
                }) { Text(stringResource(R.string.str_x__a), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { examToDelete = null }) { Text(stringResource(R.string.str_gi____l___i)) } }
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
            Text(stringResource(R.string.str_m__n_thi_ti___p_theo), color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
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
            Text(stringResource(R.string.str_ch__a_c___l___ch_thi_), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text(stringResource(R.string.str_th__m_l___ch_thi_ngay))
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
                            Text(stringResource(R.string.str_unknown), fontSize = 12.sp)
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
                            Text(stringResource(R.string.str_unknown), fontSize = 12.sp)
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
                    Text(stringResource(R.string.str_unknown), fontSize = 14.sp)
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
                label = { Text(stringResource(R.string.str_t__n_m__n_h___c)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.str_ph__ng_thi)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text(stringResource(R.string.str_ng__y__yyyy_mm_dd)) },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text(stringResource(R.string.str_b___t______u__hh_mm)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(stringResource(R.string.str_th___i_l_____ng__ph__)) },
                    modifier = Modifier.weight(1f)
                )
                
                Box(Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    androidx.compose.material3.OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(if (examType == ExamType.MIDTERM) "Giữa kỳ" else "Cuối kỳ")
                    }
                    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(stringResource(R.string.str_gi___a_k)) }, onClick = { examType = ExamType.MIDTERM; expanded = false })
                        androidx.compose.material3.DropdownMenuItem(text = { Text(stringResource(R.string.str_cu___i_k)) }, onClick = { examType = ExamType.FINAL; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (exam.id != -1L) {
                    TextButton(onClick = onDelete, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text(stringResource(R.string.str_x__a))
                    }
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.str_h___y))
                }
                androidx.compose.material3.Button(onClick = {
                    val startM = startTime.split(":").let { it.getOrNull(0)?.toIntOrNull()?.times(60)?.plus(it.getOrNull(1)?.toIntOrNull() ?: 0) } ?: exam.startMinutes
                    val durM = duration.toIntOrNull() ?: exam.durationMinutes
                    onSave(subjectName, room, dateStr, startM, durM, examType)
                }) {
                    Text(stringResource(R.string.str_l__u))
                }
            }
        }
    }
}




