package com.quoc.schedule.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserPrefs(
    /** Ngày thứ Hai của tuần 1 học kỳ — dùng để tính weekNumber. Null = chưa cấu hình. */
    val semesterStartDate: LocalDate? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@Singleton
class UserPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val semesterStartKey = stringPreferencesKey("semester_start_date")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            semesterStartDate = p[semesterStartKey]?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            themeMode = p[themeModeKey]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM)
            } ?: ThemeMode.SYSTEM
        )
    }

    suspend fun setSemesterStartDate(date: LocalDate?) {
        context.dataStore.edit { p ->
            if (date == null) p.remove(semesterStartKey)
            else p[semesterStartKey] = date.toString()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    /** Tuần hiện tại (1-based) theo ngày bắt đầu học kỳ; null nếu chưa cấu hình. */
    fun weekNumberFor(prefs: UserPrefs, date: LocalDate): Int? {
        val start = prefs.semesterStartDate ?: return null
        if (date < start) return null
        return java.time.temporal.ChronoUnit.DAYS.between(start, date).toInt() / 7 + 1
    }
}
