package com.quoc.schedule.feature.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quoc.schedule.core.data.ScheduleRepository
import com.quoc.schedule.core.database.ClassSession
import com.quoc.schedule.core.database.SessionOverride
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.core.model.OverrideType
import com.quoc.schedule.domain.GetWeekTimetableUseCase
import com.quoc.schedule.domain.TimetableEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class TimetableUiState(
    val weekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val weekNumber: Int = 1,          // tự tính nếu đã đặt ngày bắt đầu học kỳ trong Settings
    val weekNumberKnown: Boolean = false,
    val entries: List<TimetableEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val getWeekTimetable: GetWeekTimetableUseCase,
    private val detectConflict: com.quoc.schedule.domain.DetectConflictUseCase,
    private val prefsRepository: com.quoc.schedule.core.data.prefs.UserPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    // Cache dữ liệu mới nhất từ Room để render lại ngay khi đổi tuần
    private var cachedSessions: List<ClassSession> = emptyList()
    private var cachedSubjects: Map<Long, Subject> = emptyMap()
    private var cachedPrefs: com.quoc.schedule.core.data.prefs.UserPrefs =
        com.quoc.schedule.core.data.prefs.UserPrefs()

    init {
        viewModelScope.launch {
            prefsRepository.prefs.collect { prefs ->
                cachedPrefs = prefs
                val week = prefsRepository.weekNumberFor(prefs, _uiState.value.weekStart)
                _uiState.update { it.copy(weekNumber = week ?: it.weekNumber, weekNumberKnown = week != null) }
                renderWeek()
            }
        }
        viewModelScope.launch {
            combine(repository.observeSessions(), repository.observeSubjects()) { sessions, subjects ->
                sessions to subjects.associateBy { it.id }
            }.collect { (sessions, subjects) ->
                cachedSessions = sessions
                cachedSubjects = subjects
                renderWeek()
            }
        }
    }

    private suspend fun renderWeek() {
        val state = _uiState.value
        // weekNumber tự tính lại nếu đã cấu hình học kỳ (ưu tiên DataStore hơn state)
        val week = prefsRepository.weekNumberFor(cachedPrefs, state.weekStart) ?: state.weekNumber
        val entries = getWeekTimetable(state.weekStart, cachedSessions, cachedSubjects, week)
        _uiState.update { it.copy(entries = entries, isLoading = false) }
    }

    fun goToWeek(offsetWeeks: Long) {
        val newWeekStart = _uiState.value.weekStart.plusWeeks(offsetWeeks)
        val week = prefsRepository.weekNumberFor(cachedPrefs, newWeekStart)
        _uiState.update {
            it.copy(
                weekStart = newWeekStart,
                weekNumber = week ?: (it.weekNumber + offsetWeeks.toInt()).coerceAtLeast(1),
                weekNumberKnown = week != null
            )
        }
        viewModelScope.launch { renderWeek() }
    }

    fun goToday() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val week = prefsRepository.weekNumberFor(cachedPrefs, monday)
        _uiState.update {
            it.copy(weekStart = monday, weekNumber = week ?: it.weekNumber, weekNumberKnown = week != null)
        }
        viewModelScope.launch { renderWeek() }
    }

    /** Báo nghỉ một buổi cụ thể theo ngày — không xóa lịch gốc. */
    fun cancelSession(sessionId: Long, date: LocalDate) {
        viewModelScope.launch {
            repository.addOverride(
                SessionOverride(
                    sessionId = sessionId,
                    originalDate = date.toString(),
                    type = OverrideType.CANCELLED
                )
            )
        }
    }

    // ── Kéo–thả: di chuyển buổi học sang ô giờ/ngày khác ──

    /** Xét di chuyển khi user thả card. Trả về danh sách conflict (rỗng = hợp lệ). */
    fun findConflictsForMove(sessionId: Long, targetDate: LocalDate, targetStartMinutes: Int): List<TimetableEntry> {
        val session = cachedSessions.find { it.id == sessionId } ?: return emptyList()
        val subject = cachedSubjects[session.subjectId] ?: return emptyList()
        val candidate = TimetableEntry(
            sessionId = session.id, subject = subject, date = targetDate,
            startMinutes = targetStartMinutes,
            endMinutes = targetStartMinutes + (session.endMinutes - session.startMinutes),
            room = session.room
        )
        return detectConflict(_uiState.value.entries, candidate)
    }

    /** Áp dụng di chuyển (gọi sau khi không conflict, hoặc user chọn "Vẫn chuyển"). */
    fun applyMove(sessionId: Long, targetDate: LocalDate, targetStartMinutes: Int) {
        viewModelScope.launch {
            val session = cachedSessions.find { it.id == sessionId } ?: return@launch
            val duration = session.endMinutes - session.startMinutes
            val targetDayOfWeek = targetDate.dayOfWeek.value + 1   // DayOfWeek 1..7 → 2..8
            val movingSameWeek = targetDate >= _uiState.value.weekStart &&
                    targetDate < _uiState.value.weekStart.plusDays(7)
            val originalDate = _uiState.value.weekStart.plusDays((session.dayOfWeek - 2).toLong())

            if (movingSameWeek) {
                // Di chuyển trong tuần hiện tại → sửa lịch lặp gốc (giữ giờ nếu chỉ đổi ngày…)
                repository.updateSession(
                    session.copy(
                        dayOfWeek = targetDayOfWeek,
                        startMinutes = targetStartMinutes,
                        endMinutes = targetStartMinutes + duration
                    )
                )
            } else {
                // Di chuyển sang tuần khác → tạo override dạng học bù
                repository.addOverride(
                    SessionOverride(
                        sessionId = sessionId,
                        originalDate = originalDate.toString(),
                        type = OverrideType.RESCHEDULED,
                        newDate = targetDate.toString(),
                        newStartMinutes = targetStartMinutes,
                        newEndMinutes = targetStartMinutes + duration
                    )
                )
            }
        }
    }
}
