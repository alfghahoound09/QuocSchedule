package com.quoc.schedule.feature.importflow

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.core.model.EntryKind
import com.quoc.schedule.core.model.ParsedEntry
import com.quoc.schedule.ui.theme.*

@Composable
fun ImportFlowScreen(
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Dùng wildcard để chắc chắn thấy .docx trên mọi hãng (Samsung hay báo MIME lạ)
    val docxGetContent = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(viewModel::importDocx) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::importImage) }

    when (val s = state) {
        ImportUiState.Choose -> ChooseSourceScreen(
            onPickDocx = { docxGetContent.launch("*/*") },
            onPickImage = { imagePicker.launch(PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )) },
            onOpenCamera = onOpenCamera,
            onCancel = onCancel
        )
        is ImportUiState.Scanning -> ScanningScreen(s.step)
        is ImportUiState.Preview -> PreviewScreen(
            entries = s.entries,
            onEdit = viewModel::editEntry,
            onRemove = viewModel::removeEntry,
            onConfirm = viewModel::confirm,
            onCancel = { viewModel.reset() }
        )
        ImportUiState.Empty -> ResultScreen(
            emoji = "🤔",
            title = "Không đọc được lịch",
            body = "Thử chụp ảnh nét hơn, cắt sát vùng bảng lịch, hoặc chuyển sang nhập thủ công.",
            primaryAction = { viewModel.reset() },
            primaryLabel = "Thử lại"
        )
        ImportUiState.Saved -> ResultScreen(
            emoji = "✅",
            title = "Đã thêm vào lịch!",
            body = "Thời khóa biểu của bạn đã được cập nhật.",
            primaryAction = onFinished,
            primaryLabel = "Xem lịch"
        )
        is ImportUiState.Error -> ResultScreen(
            emoji = "⚠️",
            title = "Có lỗi xảy ra",
            body = s.message,
            primaryAction = { viewModel.reset() },
            primaryLabel = "Thử lại"
        )
    }
}

@Composable
fun ChooseSourceScreen(
    onPickDocx: () -> Unit,
    onPickImage: () -> Unit,
    onOpenCamera: () -> Unit,
    onCancel: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Thêm lịch mới ✨", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Chọn nguồn dữ liệu — app sẽ tự đọc và bạn luôn được kiểm tra trước khi lưu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        SourceOption("📄", "Tải file Word (.docx)", "Bóc tách bảng lịch tự động", PastelBlue.copy(alpha = .25f), onPickDocx)
        SourceOption("🖼️", "Tải ảnh (JPG / PNG)", "Quét ảnh chụp màn hình bằng OCR", MintGreen.copy(alpha = .4f), onPickImage)
        SourceOption("📷", "Chụp ảnh trực tiếp", "Chụp bảng lịch giấy / màn hình", ColorPeach.copy(alpha = .5f), onOpenCamera)
        SourceOption("✏️", "Nhập thủ công", "Tự điền tên môn, giờ học, phòng", BeigeAccent.copy(alpha = .6f), onCancel)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("‹ Quay lại") }
    }
}

@Composable
fun SourceOption(emoji: String, title: String, subtitle: String, bg: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).background(bg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ScanningScreen(step: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Đang đọc lịch của bạn…", style = MaterialTheme.typography.headlineSmall)
        Text("Giữ yên một chút nhé ☕", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
        Spacer(Modifier.height(12.dp))
        Text(step, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreviewScreen(
    entries: List<ParsedEntry>,
    onEdit: (Int, ParsedEntry) -> Unit,
    onRemove: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Kiểm tra dữ liệu 🧐", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Đọc được ${entries.size} mục. Các trường tô vàng cần bạn xác nhận.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(entries) { index, entry ->
                EntryCard(
                    entry = entry,
                    expanded = editingIndex == index,
                    onExpand = { editingIndex = if (editingIndex == index) null else index },
                    onSaveEdit = { updated -> onEdit(index, updated); editingIndex = null },
                    onRemove = { onRemove(index) }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        Surface(shadowElevation = 8.dp) {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = onConfirm,
                    enabled = entries.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("✓ Xác nhận & lưu ${entries.size} mục", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EntryCard(
    entry: ParsedEntry,
    expanded: Boolean,
    onExpand: () -> Unit,
    onSaveEdit: (ParsedEntry) -> Unit,
    onRemove: () -> Unit
) {
    val kindEmoji = if (entry.kind == EntryKind.EXAM) "🟥" else "🟦"
    val lowConfidence = entry.confidence < 0.7f

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onExpand),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$kindEmoji ${entry.subjectName}", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
                if (lowConfidence) {
                    Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFFCF3E3)) {
                        Text("⚠ cần kiểm tra", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp, color = Color(0xFFC77E2C), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            FieldRow("Mã HP", entry.subjectCode ?: "—", entry.subjectCode == null)
            FieldRow("Thời gian", formatTime(entry), entry.startTime == null)
            FieldRow("Phòng", entry.room ?: "—", entry.room == null)
            entry.lecturer?.let { FieldRow("Giảng viên", it, false) }
            entry.candidateId?.let { FieldRow("SBD", it, false) }
            entry.warnings.forEach { w ->
                Text("⚠ $w", fontSize = 11.sp, color = Color(0xFFC77E2C))
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                EditForm(entry = entry, onSave = onSaveEdit, onRemove = onRemove)
            }
        }
    }
}

@Composable
fun FieldRow(label: String, value: String, suspicious: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (suspicious) Modifier.background(Color(0x33FCF3E3), RoundedCornerShape(8.dp))
                  else Modifier)
            .padding(horizontal = if (suspicious) 6.dp else 0.dp, vertical = 4.dp)
    ) {
        Text(label, modifier = Modifier.width(90.dp), fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value, fontSize = 12.sp,
            fontWeight = if (suspicious) FontWeight.Bold else FontWeight.Normal,
            color = if (suspicious) Color(0xFFC77E2C) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EditForm(entry: ParsedEntry, onSave: (ParsedEntry) -> Unit, onRemove: () -> Unit) {
    var name by remember { mutableStateOf(entry.subjectName) }
    var code by remember { mutableStateOf(entry.subjectCode ?: "") }
    var start by remember { mutableStateOf(entry.startTime ?: "") }
    var end by remember { mutableStateOf(entry.endTime ?: "") }
    var room by remember { mutableStateOf(entry.room ?: "") }
    var lecturer by remember { mutableStateOf(entry.lecturer ?: "") }

    OutlinedTextField(name, { name = it }, label = { Text("Tên môn") },
        modifier = Modifier.fillMaxWidth(), singleLine = true)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(code, { code = it }, label = { Text("Mã HP") },
            modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(room, { room = it }, label = { Text("Phòng") },
            modifier = Modifier.weight(1f), singleLine = true)
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(start, { start = it }, label = { Text("Bắt đầu (7:30)") },
            modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(end, { end = it }, label = { Text("Kết thúc (9:30)") },
            modifier = Modifier.weight(1f), singleLine = true)
    }
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(lecturer, { lecturer = it }, label = { Text("Giảng viên") },
        modifier = Modifier.fillMaxWidth(), singleLine = true)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                onSave(entry.copy(
                    subjectName = name.trim(),
                    subjectCode = code.trim().ifBlank { null },
                    startTime = start.trim().ifBlank { null },
                    endTime = end.trim().ifBlank { null },
                    room = room.trim().ifBlank { null },
                    lecturer = lecturer.trim().ifBlank { null },
                    confidence = 1f,          // đã được người dùng xác nhận
                    warnings = emptyList()
                ))
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Lưu thay đổi") }
        OutlinedButton(onClick = onRemove, shape = RoundedCornerShape(14.dp)) {
            Text("Xóa", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ResultScreen(
    emoji: String,
    title: String,
    body: String,
    primaryAction: () -> Unit,
    primaryLabel: String
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 52.sp)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = primaryAction, shape = RoundedCornerShape(16.dp)) { Text(primaryLabel) }
    }
}

private fun formatTime(entry: ParsedEntry): String {
    val day = entry.dayOfWeek?.let {
        when (it) {
            2 -> "T2"; 3 -> "T3"; 4 -> "T4"; 5 -> "T5"
            6 -> "T6"; 7 -> "T7"; else -> "CN"
        }
    }
    val time = listOfNotNull(entry.startTime, entry.endTime).joinToString(" – ")
    val date = entry.date
    return listOfNotNull(day, time.ifBlank { null }, date).joinToString(" · ").ifBlank { "—" }
}
