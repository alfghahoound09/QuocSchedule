package com.quoc.schedule.domain

import com.quoc.schedule.core.data.ScheduleRepository
import com.quoc.schedule.core.database.ClassSession
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.core.model.ExamType
import com.quoc.schedule.core.model.OverrideType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** Một buổi học đã resolve (lịch gốc + override) để render lên lưới tuần. */
data class TimetableEntry(
    val sessionId: Long,
    val subject: Subject,
    val date: LocalDate,
    val startMinutes: Int,
    val endMinutes: Int,
    val room: String?,
    val isCancelled: Boolean = false,
    val isMakeup: Boolean = false    // buổi học bù được chuyển sang ngày này
)

/**
 * Kiểm tra một buổi học (mới/di chuyển) có trùng giờ với buổi khác cùng ngày không.
 * Trả về danh sách entry xung đột (rỗng = hợp lệ).
 */
class DetectConflictUseCase @Inject constructor() {
    operator fun invoke(
        entries: List<TimetableEntry>,
        candidate: TimetableEntry
    ): List<TimetableEntry> = entries.filter { other ->
        other.sessionId != candidate.sessionId &&
            other.date == candidate.date &&
            !other.isCancelled &&
            other.startMinutes < candidate.endMinutes &&
            candidate.startMinutes < other.endMinutes
    }
}

/**
 * Render một tuần cụ thể: lịch lặp theo weekPattern + merge SessionOverride
 * (buổi bị hủy vẫn hiện mờ, buổi học bù dời đến hiện kèm nhãn "Học bù").
 */
class GetWeekTimetableUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        weekStart: LocalDate,
        sessions: List<ClassSession>,
        subjects: Map<Long, Subject>,
        currentWeek: Int
    ): List<TimetableEntry> {
        val out = mutableListOf<TimetableEntry>()
        val weekEnd = weekStart.plusDays(6)
        val (overridesOut, overridesIn) =
            repository.overridesInWeek(weekStart.toString(), weekEnd.toString())

        val cancelledKeys = mutableSetOf<Pair<Long, String>>()
        val rescheduledOutIds = mutableSetOf<Long>()
        overridesOut.forEach { o ->
            when (o.type) {
                OverrideType.CANCELLED -> cancelledKeys += o.sessionId to o.originalDate
                OverrideType.RESCHEDULED -> rescheduledOutIds += o.id
            }
        }

        for (session in sessions) {
            val subject = subjects[session.subjectId] ?: continue
            if (!weekMatches(session.weekPattern, currentWeek)) continue
            val date = weekStart.plusDays((session.dayOfWeek - 2).toLong())
            val cancelled = (session.id to date.toString()) in cancelledKeys
            out += TimetableEntry(
                sessionId = session.id, subject = subject, date = date,
                startMinutes = session.startMinutes, endMinutes = session.endMinutes,
                room = session.room, isCancelled = cancelled
            )
        }
        for (o in overridesIn) {
            val original = sessions.find { it.id == o.sessionId } ?: continue
            val subject = subjects[original.subjectId] ?: continue
            val newDate = o.newDate ?: continue
            out += TimetableEntry(
                sessionId = original.id, subject = subject,
                date = LocalDate.parse(newDate),
                startMinutes = o.newStartMinutes ?: original.startMinutes,
                endMinutes = o.newEndMinutes ?: original.endMinutes,
                room = o.newRoom ?: original.room,
                isMakeup = true
            )
        }
        return out.sortedWith(compareBy({ it.date }, { it.startMinutes }))
    }

    /** "1-15" / "1,3,5,7" / "" (all) — week 1-based theo học kỳ. */
    fun weekMatches(pattern: String, week: Int): Boolean {
        if (pattern.isBlank() || pattern.equals("all", true)) return true
        if ("-" in pattern) {
            val parts = pattern.split("-").mapNotNull { it.trim().toIntOrNull() }
            if (parts.size == 2) return week in parts[0]..parts[1]
            return true
        }
        return pattern.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(week)
    }
}

/** Kỳ thi sắp tới + số ngày đếm ngược. */
data class NextExamInfo(
    val examId: Long,
    val subject: Subject,
    val date: LocalDate,
    val startMinutes: Int,
    val room: String?,
    val candidateId: String?,
    val daysLeft: Long,
    val examType: ExamType
)

class GetExamCountdownUseCase @Inject constructor() {
    operator fun invoke(
        exams: List<com.quoc.schedule.core.database.Exam>,
        subjects: Map<Long, Subject>,
        today: LocalDate = LocalDate.now()
    ): NextExamInfo? {
        val next = exams
            .filter { LocalDate.parse(it.examDate) >= today }
            .minByOrNull { it.examDate + "%05d".format(it.startMinutes) }
            ?: return null
        val subject = subjects[next.subjectId] ?: return null
        return NextExamInfo(
            examId = next.id,
            subject = subject,
            date = LocalDate.parse(next.examDate),
            startMinutes = next.startMinutes,
            room = next.room,
            candidateId = next.candidateId,
            daysLeft = ChronoUnit.DAYS.between(today, LocalDate.parse(next.examDate)),
            examType = next.examType
        )
    }
}
