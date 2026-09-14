package com.quoc.schedule.core.database.di

import android.content.Context
import androidx.room.Room
import com.quoc.schedule.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "quoc_schedule.db")
            .fallbackToDestructiveMigration() // MVP: bản sau viết migration đúng chuẩn
            .build()

    @Provides fun provideSubjectDao(db: AppDatabase) = db.subjectDao()
    @Provides fun provideClassSessionDao(db: AppDatabase) = db.classSessionDao()
    @Provides fun provideSessionOverrideDao(db: AppDatabase) = db.sessionOverrideDao()
    @Provides fun provideExamDao(db: AppDatabase) = db.examDao()
    @Provides fun provideIngestionDraftDao(db: AppDatabase) = db.ingestionDraftDao()
}
