package com.quoc.schedule.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quoc.schedule.core.model.DraftStatus
import com.quoc.schedule.core.model.ExamType
import com.quoc.schedule.core.model.OverrideType
import com.quoc.schedule.core.model.SourceType

@Entity
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val lecturer: String?,
    val colorKey: Int,          // index vào palette pastel, hash ổn định từ code
    val semesterId: Long = 1
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val dayOfWeek: Int,         // 2..8 (T2..CN)
    val startMinutes: Int,      // phút kể từ 00:00, tránh phụ thuộc java.time trong Room
    val endMinutes: Int,
    val room: String?,
    val weekPattern: String     // "1-15" hoặc "1,3,5"
)

@Entity(
    indices = [Index("sessionId")]
)
data class SessionOverride(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val originalDate: String,   // ISO yyyy-MM-dd
    val type: OverrideType,
    val newDate: String? = null,
    val newStartMinutes: Int? = null,
    val newEndMinutes: Int? = null,
    val newRoom: String? = null
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("subjectId")]
)
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val examDate: String,       // ISO yyyy-MM-dd
    val startMinutes: Int,
    val durationMinutes: Int = 90,
    val room: String?,
    val candidateId: String?,
    val examType: ExamType
)

@Entity
data class IngestionDraft(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceType: SourceType,
    val rawPayload: String,     // text thô để audit / khôi phục
    val createdAt: Long,        // epoch millis
    val status: DraftStatus
)
