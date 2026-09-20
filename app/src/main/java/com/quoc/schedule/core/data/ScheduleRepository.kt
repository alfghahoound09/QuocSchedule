package com.quoc.schedule.core.data

import com.quoc.schedule.core.database.ClassSession
import com.quoc.schedule.core.database.ClassSessionDao
import com.quoc.schedule.core.database.Exam
import com.quoc.schedule.core.database.ExamDao
import com.quoc.schedule.core.database.IngestionDraft
import com.quoc.schedule.core.database.IngestionDraftDao
import com.quoc.schedule.core.database.SessionOverride
import com.quoc.schedule.core.database.SessionOverrideDao
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.core.database.SubjectDao
import com.quoc.schedule.core.model.DraftStatus
import com.quoc.schedule.core.model.EntryKind
import com.quoc.schedule.core.model.ExamType
import com.quoc.schedule.core.model.ParsedEntry
import com.quoc.schedule.core.model.SourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class ScheduleRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val sessionDao: ClassSessionDao,
    private val overrideDao: SessionOverrideDao,
    private val examDao: ExamDao,
    private val draftDao: IngestionDraftDao
) {
    fun observeSubjects(): Flow<List<Subject>> = subjectDao.observeAll()
    fun observeSessions(): Flow<List<ClassSession>> = sessionDao.observeAll()
    fun observeExams(): Flow<List<Exam>> = examDao.observeAll()

    /** Màu ổn định theo mã HP — cùng mã luôn ra cùng màu pastel. */
    fun colorKeyFor(code: String): Int = (code.hashCode().absoluteValue) % 7

    suspend fun addSubject(subject: Subject): Long = subjectDao.upsert(subject)
    suspend fun addSession(session: ClassSession): Long = sessionDao.upsert(session)
    suspend fun updateSession(session: ClassSession) = sessionDao.update(session)
    suspend fun getSession(id: Long): ClassSession? =
        sessionDao.observeAll().first().find { it.id == id }
    suspend fun deleteSession(session: ClassSession) = sessionDao.delete(session)
    suspend fun addExam(exam: Exam): Long = examDao.upsert(exam)
    suspend fun deleteExam(exam: Exam) = examDao.delete(exam)
    suspend fun deleteSubject(subject: Subject) = subjectDao.delete(subject)

    suspend fun addOverride(override: SessionOverride): Long = overrideDao.upsert(override)
    suspend fun overridesInWeek(weekStart: String, weekEnd: String): Pair<List<SessionOverride>, List<SessionOverride>> =
        overrideDao.getInRange(weekStart, weekEnd) to overrideDao.getRescheduledInto(weekStart, weekEnd)

    suspend fun saveDraft(source: SourceType, rawPayload: String): Long =
        draftDao.upsert(IngestionDraft(sourceType = source, rawPayload = rawPayload,
            createdAt = System.currentTimeMillis(), status = DraftStatus.PENDING))

    suspend fun markDraft(id: Long, status: DraftStatus) = draftDao.setStatus(id, status)

    /**
     * Chốt dữ liệu từ màn Preview vào DB trong một lần.
     * Gộp Subject theo (code, name) để tránh tạo trùng khi OCR ra nhiều buổi cùng môn.
     */
    suspend fun commitParsedEntries(entries: List<ParsedEntry>) {
        val subjectIdCache = mutableMapOf<String, Long>()
        val existing = subjectDao.observeAll().first()
            .associateBy { it.code.ifBlank { it.name } }

        suspend fun subjectIdFor(e: ParsedEntry): Long {
            val key = e.subjectCode?.takeIf { it.isNotBlank() } ?: e.subjectName
            subjectIdCache[key]?.let { return it }
            existing[key]?.let { subjectIdCache[key] = it.id; return it.id }
            val id = subjectDao.upsert(
                Subject(
                    code = e.subjectCode ?: "",
                    name = e.subjectName,
                    lecturer = e.lecturer,
                    colorKey = colorKeyFor(e.subjectCode ?: e.subjectName)
                )
            )
            subjectIdCache[key] = id
            return id
        }

        for (e in entries) {
            val subjectId = subjectIdFor(e)
            when (e.kind) {
                EntryKind.CLASS -> sessionDao.upsert(
                    ClassSession(
                        subjectId = subjectId,
                        dayOfWeek = e.dayOfWeek ?: continue,
                        startMinutes = e.startTime.toMinutesOrNull() ?: continue,
                        endMinutes = e.endTime.toMinutesOrNull() ?: (e.startTime.toMinutesOrNull() ?: 0) + 90,
                        room = e.room,
                        weekPattern = e.weekPattern
                    )
                )
                EntryKind.EXAM -> examDao.upsert(
                    Exam(
                        subjectId = subjectId,
                        examDate = e.date ?: continue,
                        startMinutes = e.startTime.toMinutesOrNull() ?: 7 * 60 + 30,
                        room = e.room,
                        candidateId = e.candidateId,
                        examType = ExamType.FINAL
                    )
                )
            }
        }
    }

    private fun String?.toMinutesOrNull(): Int? {
        if (this == null) return null
        val parts = split(":")
        if (parts.size != 2) return null
        return parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: return null)
    }
}
