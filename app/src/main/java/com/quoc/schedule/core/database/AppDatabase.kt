package com.quoc.schedule.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.quoc.schedule.core.model.DraftStatus
import com.quoc.schedule.core.model.ExamType
import com.quoc.schedule.core.model.OverrideType
import com.quoc.schedule.core.model.SourceType

class EnumConverters {
    @TypeConverter fun overrideTypeToString(v: OverrideType) = v.name
    @TypeConverter fun stringToOverrideType(v: String) = OverrideType.valueOf(v)
    @TypeConverter fun examTypeToString(v: ExamType) = v.name
    @TypeConverter fun stringToExamType(v: String) = ExamType.valueOf(v)
    @TypeConverter fun sourceTypeToString(v: SourceType) = v.name
    @TypeConverter fun stringToSourceType(v: String) = SourceType.valueOf(v)
    @TypeConverter fun draftStatusToString(v: DraftStatus) = v.name
    @TypeConverter fun stringToDraftStatus(v: String) = DraftStatus.valueOf(v)
}

@Database(
    entities = [Subject::class, ClassSession::class, SessionOverride::class, Exam::class, IngestionDraft::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun sessionOverrideDao(): SessionOverrideDao
    abstract fun examDao(): ExamDao
    abstract fun ingestionDraftDao(): IngestionDraftDao
}
