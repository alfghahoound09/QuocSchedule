package com.quoc.schedule.core.data.ingestion

import com.quoc.schedule.core.model.EntryKind
import com.quoc.schedule.core.model.ParsedEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapping dữ liệu thô → ParsedEntry theo chiến lược trong ARCHITECTURE.md:
 * - Mã HP: regex dạng PREFIX.SUFFIX (UET.CS1058) hoặc [A-Z]{2,4}\d{3,4}
 * - Giờ: "HH:mm-HH:mm" / "Tiết 4 - 6" / "4 - 6"
 * - Thứ: T2..CN / Thứ Hai / Thứ 2 / đa ngày "T2,T4,T5"
 * - Phòng: 308-B / B1-203 / P.305 / TC-305
 * - Loại: keyword thi → EntryKind.EXAM
 */
@Singleton
class ScheduleNormalizer @Inject constructor() {

    private val subjectCodeRegex = Regex("""\b[A-Z]{2,4}[.\s]?\d{3,4}\b""")
    private val prefixedCodeRegex = Regex("""\b[A-Z]{2,4}\.[A-Z]{2}\d{4}\b""")
    private val timeRangeRegex = Regex("""(\d{1,2})[:.h](\d{2})\s*[-–]\s*(\d{1,2})[:.h](\d{2})""")
    private val singleTimeRegex = Regex("""\b(\d{1,2})[:.h](\d{2})\b""")
    private val dateRegex = Regex("""\b(\d{1,2})[/.\-](\d{1,2})[/.\-](\d{4})\b""")
    private val roomRegex = Regex("""\b(\d{2,4}-[A-Z]\d*|[A-Z]{1,2}\d?-\d{3,4}[A-Z]?|[Pp]\.?\s?\d{3,4}[A-Z]?)\b""")
    private val candidateRegex = Regex("""(?i)(?:s(?:ố|o)\s*b(?:áo|ao)\s*d(?:anh)?|SBD)\s*[:.]?\s*(\d+)""")
    private val lecturerRegex = Regex("""(?i)(?:gv|cbgd|giảng viên|giáo viên)\s*[:.]?\s*(.+)""")
    private val examKeywordRegex = Regex("""(?i)\b(thi|kiểm tra|giữa kỳ|cuối kỳ|giữa kì|cuối kì|midterm|final)\b""")
    private val tietRegex = Regex("""\b(\d{1,2})\s*[-–]\s*(\d{1,2})\b""")

    private val dayOfWeekMap = mapOf(
        "thứ 2" to 2, "thứ hai" to 2, "mon" to 2,
        "thứ 3" to 3, "thứ ba" to 3, "tue" to 3,
        "thứ 4" to 4, "thứ tư" to 4, "wed" to 4,
        "thứ 5" to 5, "thứ năm" to 5, "thu" to 5,
        "thứ 6" to 6, "thứ sáu" to 6, "fri" to 6,
        "thứ 7" to 7, "thứ bảy" to 7, "sat" to 7,
        "chủ nhật" to 8, "cn" to 8, "sun" to 8
    )

    fun normalize(raw: RawExtraction): List<ParsedEntry> {
        val entries = mutableListOf<ParsedEntry>()
        if (raw.tableRows.isNotEmpty()) {
            entries += fromTable(raw.tableRows)
        }
        if (entries.isEmpty()) {
            entries += fromLines(raw.rawText)
        }
        return entries
    }

    /** Đường đi tốt nhất: docx/html có bảng rõ ràng → chỉ giữ bảng môn học, map theo cột có header. */
    private fun fromTable(rows: List<List<String>>): List<ParsedEntry> {
        var headerIdx = -1
        var cols = emptyMap<String, Int>()
        rows.forEachIndexed { idx, row ->
            val joined = row.joinToString(" ").lowercase()
            // Header phải chứa ít nhất 2 keyword đặc trưng của bảng lịch
            val score = listOf("môn học", "học phần", "mã môn", "thứ", "tiết", "giảng đường")
                .count { joined.contains(it) }
            if (headerIdx < 0 && score >= 2) {
                headerIdx = idx
                cols = detectColumns(row)
            }
        }
        if (headerIdx < 0) return emptyList()

        val out = mutableListOf<ParsedEntry>()
        for (row in rows.drop(headerIdx + 1)) {
            if (!isSubjectRow(row, cols)) continue
            val entries = mapRow(row, cols) ?: listOfNotNull(mapRowHeuristic(row))
            out += entries.filter { it.subjectName.isNotBlank() }
        }
        return out
    }

    /** Chỉ giữ hàng thực sự là môn học (có mã HP), bỏ header/footer/label rác. */
    private fun isSubjectRow(row: List<String>, cols: Map<String, Int>): Boolean {
        // Bỏ hàng quá ngắn so với header
        val headerColCount = cols.size
        if (headerColCount > 0 && row.size < headerColCount / 2) return false
        // Bắt buộc phải có mã HP ở cột code hoặc bất kỳ ô nào
        val codeCell = cols["code"]?.let { row.getOrNull(it) }
        val code = codeCell?.let { extractCode(it) } ?: row.joinToString(" ").let { extractCode(it) }
        return code != null
    }

    private fun detectColumns(header: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        header.forEachIndexed { i, cell ->
            val c = cell.lowercase().trim()
            when {
                "mã môn" in c || ("mã" in c && ("hp" in c || "học phần" in c)) -> map["code"] = i
                c == "môn học" || c == "môn thi" || c == "môn" || "tên môn" in c -> map["name"] = i
                "học phần" in c && "mã" !in c -> map["name"] = i
                c == "thứ" -> map["day"] = i
                c == "tiết" || "giờ" in c || "thời gian" in c -> map["time"] = i
                "phòng" in c || "giảng đường" in c -> map["room"] = i
                "giảng viên" in c || "cbgd" in c || c == "gv" -> map["lecturer"] = i
                "ngày" in c && "ngày" !in map.values.joinToString("") -> map["date"] = i
                "sbd" in c || "báo danh" in c -> map["candidate"] = i
                "lớp" in c && "môn" in c -> map.putIfAbsent("classGroup", i)
            }
        }
        return map
    }

    /**
     * Map một hàng bảng → có thể ra NHIỀU entry nếu đa ngày / đa tiết.
     * vd: T2,T4,T5 + "4 - 6,10 - 12,7 - 9" → 3 buổi học riêng.
     */
    private fun mapRow(row: List<String>, cols: Map<String, Int>): List<ParsedEntry>? {
        if (cols.isEmpty()) return null
        fun col(key: String): String? = cols[key]?.let { row.getOrNull(it) }?.takeIf { it.isNotBlank() }

        val isExam = row.joinToString(" ").let(examKeywordRegex::containsMatchIn)
        val warnings = mutableListOf<String>()
        var confidence = 0.9f

        val name = col("name")
        if (name == null) confidence -= 0.3f
        val code = col("code") ?: row.joinToString(" ").let { extractCode(it) }
        val room = col("room")
        val lecturer = col("lecturer")
        val candidate = col("candidate")
        val date = col("date")?.let { parseDate(it) }
        val timeCol = col("time")

        // Parse thứ (có thể đa: "T2,T4,T5")
        val days = parseDayList(timeCol ?: "") + (col("day")?.let { parseDayList(it) } ?: emptyList())

        // Parse tiết hoặc giờ
        val timeText = timeCol ?: ""
        val timeRanges = if (isTietValue(timeText)) {
            parseTietRanges(timeText)
        } else {
            val (s, e) = parseTimeRange(timeText)
            if (s != null) listOf(s to e) else emptyList()
        }

        if (timeRanges.isEmpty()) {
            confidence -= 0.2f
            warnings += "Chưa rõ giờ bắt đầu"
        }

        // Tạo entry cho mỗi (ngày × khung giờ)
        // Quy ước pairing: "T2,T4,T5" + "4 - 6,10 - 12,7 - 9" → T2↔(4-6), T4↔(10-12), T5↔(7-9)
        val result = mutableListOf<ParsedEntry>()
        val pairs: List<Pair<Int?, Pair<String?, String?>>> = when {
            days.size == timeRanges.size && days.isNotEmpty() ->
                days.zip(timeRanges) { d, r -> d to r }
            days.isEmpty() ->
                timeRanges.map { null to it }
            timeRanges.isEmpty() ->
                days.map { it to (null to null) }
            else ->
                days.flatMap { d -> timeRanges.map { d to it } }  // fallback cross product
        }

        for ((day, range) in pairs) {
            result += ParsedEntry(
                subjectName = name ?: "",
                subjectCode = code,
                lecturer = lecturer,
                dayOfWeek = day,
                date = date,
                startTime = range.first,
                endTime = range.second,
                room = room,
                candidateId = candidate,
                kind = if (isExam) EntryKind.EXAM else EntryKind.CLASS,
                confidence = confidence.coerceIn(0.1f, 1f),
                warnings = warnings
            )
        }
        return result.ifEmpty { null }
    }

    /** Không có header rõ → đoán từ nội dung từng ô. */
    private fun mapRowHeuristic(row: List<String>): ParsedEntry? {
        val joined = row.joinToString(" ")
        if (joined.isBlank()) return null
        val warnings = mutableListOf<String>()
        var confidence = 0.6f

        val code = extractCode(joined)
        val day = parseDayOfWeek(joined)
        val (start, end) = parseTimeRange(joined)
        val room = parseRoom(joined)
        val date = parseDate(joined)
        val isExam = examKeywordRegex.containsMatchIn(joined)

        val labelPrefix = Regex("""(?i)^(phòng|p\.|gv|cbgd|giảng viên|thứ|tiết|ngày|sbd|mã)\b""")
        val name = row
            .filter {
                it.length > 3 && singleTimeRegex.find(it) == null &&
                    !subjectCodeRegex.matches(it.trim()) &&
                    !prefixedCodeRegex.matches(it.trim()) &&
                    !labelPrefix.containsMatchIn(it.trim())
            }
            .maxByOrNull { it.length }

        if (name == null) return null
        if (start == null) { confidence -= 0.2f; warnings += "Chưa rõ giờ bắt đầu" }
        if (!isExam && day == null && date == null) { confidence -= 0.15f; warnings += "Chưa rõ thứ/ngày" }

        return ParsedEntry(
            subjectName = name.trim(),
            subjectCode = code,
            lecturer = lecturerRegex.find(joined)?.groupValues?.get(1)?.trim(),
            dayOfWeek = day,
            date = date,
            startTime = start,
            endTime = end,
            room = room,
            candidateId = candidateRegex.find(joined)?.groupValues?.get(1),
            kind = if (isExam) EntryKind.EXAM else EntryKind.CLASS,
            confidence = confidence.coerceIn(0.1f, 1f),
            warnings = warnings
        )
    }

    /** OCR ảnh không có bảng → ghép theo block dòng. */
    private fun fromLines(text: String): List<ParsedEntry> {
        val out = mutableListOf<ParsedEntry>()
        val blocks = mutableListOf<MutableList<String>>()
        var current = mutableListOf<String>()
        text.lineSequence().forEach { line ->
            val l = line.trim()
            if (l.isEmpty()) {
                if (current.isNotEmpty()) { blocks += current; current = mutableListOf() }
            } else current += l
        }
        if (current.isNotEmpty()) blocks += current

        val grouped = mutableListOf<MutableList<String>>()
        for (b in blocks) {
            val joined = b.joinToString(" ")
            if (extractCode(joined) != null || grouped.isEmpty()) {
                grouped += b.toMutableList()
            } else {
                grouped.last().addAll(b)
            }
        }

        for (g in grouped) {
            val entry = mapRowHeuristic(g) ?: continue
            out += entry
        }
        return out
    }

    // ── Helpers ──

    private fun minutesToTimeStr(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

    fun extractCode(text: String): String? {
        prefixedCodeRegex.find(text)?.let { return it.value }
        subjectCodeRegex.find(text)?.let { return it.value }
        return null
    }

    fun parseTimeRange(text: String): Pair<String?, String?> {
        timeRangeRegex.find(text)?.let {
            val s = it.groupValues
            return "%02d:%s".format(s[1].toInt(), s[2]) to "%02d:%s".format(s[3].toInt(), s[4])
        }
        singleTimeRegex.find(text)?.let {
            val h = it.groupValues[1].toInt()
            if (h in 6..21) return "%02d:%s".format(h, it.groupValues[2]) to null
        }
        return null to null
    }

    /** Kiểm tra có phải giá trị tiết không ("4 - 6", "10-12", "4 - 6,10 - 12,7 - 9") */
    private fun isTietValue(text: String): Boolean {
        if (text.isBlank()) return false
        // Nếu có dấu ":" hoặc "h" hoặc "." giữa số → là giờ, không phải tiết
        if (singleTimeRegex.containsMatchIn(text)) return false
        return tietRegex.containsMatchIn(text)
    }

    /**
     * Parse "4 - 6,10 - 12,7 - 9" → list of (startMin, endMin).
     * Dùng bảng giờ chính thức của UET:
     *   Tiết 1-3 (Ca 1): 07:00–09:40, nghỉ 5' giữa tiết
     *   Tiết 4-6 (Ca 2): 09:50–12:30, nghỉ 5' giữa tiết
     *   Tiết 7-9 (Ca 3): 13:30–16:10, nghỉ 5' giữa tiết
     *   Tiết 10-12 (Ca 4): 16:20–19:00, nghỉ 5' giữa tiết
     */
    private fun parseTietRanges(text: String): List<Pair<String?, String?>> {
        val results = mutableListOf<Pair<String?, String?>>()
        val parts = text.split(",").map { it.trim() }
        for (part in parts) {
            val m = tietRegex.find(part) ?: continue
            val from = m.groupValues[1].toInt()
            val to = m.groupValues[2].toInt()
            if (from !in 1..12 || to !in 1..12) continue
            val startMin = TIET_START_MINUTES[from - 1]
            val endMin = TIET_END_MINUTES[to - 1]
            results += minutesToTimeStr(startMin) to minutesToTimeStr(endMin)
        }
        return results
    }

    /** Tiết N → phút bắt đầu (theo bảng UET chính thức). */
    private fun tietToMinutes(tiet: Int): Int = TIET_START_MINUTES[(tiet - 1).coerceIn(0, 11)]

    /**
     * Bảng giờ UET (phút từ 00:00):
     * Tiết 1:  07:00–07:50   (420–470)
     * Tiết 2:  07:55–08:45   (475–525)
     * Tiết 3:  08:50–09:40   (530–580)
     * Tiết 4:  09:50–10:40   (590–640)
     * Tiết 5:  10:45–11:35   (645–695)
     * Tiết 6:  11:40–12:30   (700–750)
     * Tiết 7:  13:30–14:20   (810–860)
     * Tiết 8:  14:25–15:15   (865–915)
     * Tiết 9:  15:20–16:10   (920–970)
     * Tiết 10: 16:20–17:10   (980–1030)
     * Tiết 11: 17:15–18:05   (1035–1085)
     * Tiết 12: 18:10–19:00   (1090–1140)
     */
    companion object {
        private val TIET_START_MINUTES = intArrayOf(420, 475, 530, 590, 645, 700, 810, 865, 920, 980, 1035, 1090)
        private val TIET_END_MINUTES = intArrayOf(470, 525, 580, 640, 695, 750, 860, 915, 970, 1030, 1085, 1140)
    }

    /** Parse danh sách thứ: "T2,T4,T5" / "T2" / "Thứ 2" → [2,4,5] */
    fun parseDayList(text: String): List<Int> {
        val result = mutableListOf<Int>()
        val lower = text.lowercase().trim()

        // Cách 1: split theo phẩy → parse từng phần
        val parts = lower.split(",", "，").map { it.trim() }
        for (part in parts) {
            // "t2" / "t2 "
            val tMatch = Regex("""^t\s?(\d)$""").find(part)
            if (tMatch != null) {
                tMatch.groupValues[1].toIntOrNull()?.let { if (it in 2..8) result += it }
                continue
            }
            // "chủ nhật" / "cn" / "sun"
            if (part in listOf("chủ nhật", "cn", "sun")) { result += 8; continue }
            // "thứ 2" / "thứ ba"...
            val thuMatch = Regex("""^thứ\s?([2-8])$""").find(part)
            if (thuMatch != null) {
                thuMatch.groupValues[1].toIntOrNull()?.let { result += it }
                continue
            }
            dayOfWeekMap[part]?.let { result += it }
        }

        // Cách 2: fallback — text không có phẩy, dùng regex quét
        if (result.isEmpty()) {
            Regex("""(?i)(?<![a-z])t(\d)(?=\D|$)""").findAll(lower).forEach { m ->
                m.groupValues[1].toIntOrNull()?.let { if (it in 2..8) result += it }
            }
            if (Regex("""\bchủ nhật\b|\bcn\b|\bsun\b""").containsMatchIn(lower) && !result.contains(8)) {
                result += 8
            }
            dayOfWeekMap.forEach { (k, v) ->
                if (lower.contains(k) && v !in result) result += v
            }
        }

        return result.distinct().sorted()
    }

    fun parseDayOfWeek(text: String): Int? {
        val trimmed = text.trim()
        trimmed.toIntOrNull()?.let { if (it in 2..8) return it }
        return parseDayList(text).firstOrNull()
    }

    fun parseDate(text: String): String? {
        dateRegex.find(text)?.let {
            val (d, m, y) = it.destructured
            return try {
                LocalDate.of(y.toInt(), m.toInt(), d.toInt()).toString()
            } catch (_: Exception) { null }
        }
        return null
    }

    fun parseRoom(text: String): String? {
        val patterns = listOf(
            """\b[A-Z]{1,2}\d?-\d{3,4}[A-Z]?\b""",  // B1-203, TC-305
            """\b\d{2,4}-[A-Z]\d*\b""",               // 308-B, 405-B
            """\b[Pp]\.?\s?\d{3,4}[A-Z]?\b""",        // P.305
            """\b\d{3,4}[A-Z]\b"""                     // 305A
        )
        for (p in patterns) {
            Regex(p).find(text)?.let { return it.value.replace("P.", "P").trim() }
        }
        // "Học Online"
        if (text.contains("online", ignoreCase = true)) return "Online"
        return null
    }
}
