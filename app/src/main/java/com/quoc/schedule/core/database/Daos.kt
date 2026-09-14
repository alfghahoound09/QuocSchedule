package com.quoc.schedule.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quoc.schedule.core.model.DraftStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subject: Subject): Long

    @Query("SELECT * FROM Subject ORDER BY name")
    fun observeAll(): Flow<List<Subject>>

    @Query("SELECT * FROM Subject WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Delete
    suspend fun delete(subject: Subject)
}

@Dao
interface ClassSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: ClassSession): Long

    @Update
    suspend fun update(session: ClassSession)

    @Delete
    suspend fun delete(session: ClassSession)

    @Query("SELECT * FROM ClassSession")
    fun observeAll(): Flow<List<ClassSession>>

    @Query("DELETE FROM ClassSession WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: Long)
}

@Dao
interface SessionOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: SessionOverride): Long

    @Query("SELECT * FROM SessionOverride WHERE originalDate BETWEEN :weekStart AND :weekEnd")
    suspend fun getInRange(weekStart: String, weekEnd: String): List<SessionOverride>

    @Query("SELECT * FROM SessionOverride WHERE newDate BETWEEN :weekStart AND :weekEnd AND type = 'RESCHEDULED'")
    suspend fun getRescheduledInto(weekStart: String, weekEnd: String): List<SessionOverride>
}

@Dao
interface ExamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exam: Exam): Long

    @Delete
    suspend fun delete(exam: Exam)

    @Query("SELECT * FROM Exam ORDER BY examDate, startMinutes")
    fun observeAll(): Flow<List<Exam>>
}

@Dao
interface IngestionDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: IngestionDraft): Long

    @Query("SELECT * FROM IngestionDraft WHERE status = 'PENDING' ORDER BY createdAt DESC LIMIT 1")
    suspend fun latestPending(): IngestionDraft?

    @Query("UPDATE IngestionDraft SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: DraftStatus)
}
