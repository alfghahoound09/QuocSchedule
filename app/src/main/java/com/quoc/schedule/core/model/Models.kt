package com.quoc.schedule.core.model

enum class OverrideType { CANCELLED, RESCHEDULED }

enum class ExamType { MIDTERM, FINAL, RETAKE }

enum class SourceType { IMAGE, DOCX, MANUAL }

enum class DraftStatus { PENDING, CONFIRMED, DISCARDED }

enum class EntryKind { CLASS, EXAM }

/**
 * Kết quả sau bước trích xuất & mapping, chưa lưu DB.
 * confidence < 0.7 sẽ được UI tô cảnh báo.
 */
data class ParsedEntry(
    val subjectName: String,
    val subjectCode: String? = null,
    val lecturer: String? = null,
    val dayOfWeek: Int? = null,        // 2..8 (T2..CN), null nếu là thi theo ngày
    val date: String? = null,          // ISO yyyy-MM-dd (dùng cho thi)
    val startTime: String? = null,     // "HH:mm"
    val endTime: String? = null,
    val room: String? = null,
    val candidateId: String? = null,   // số báo danh
    val weekPattern: String = "1-15",
    val kind: EntryKind = EntryKind.CLASS,
    val confidence: Float = 1f,
    val warnings: List<String> = emptyList()
)
