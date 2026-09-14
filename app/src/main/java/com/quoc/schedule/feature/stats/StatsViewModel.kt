package com.quoc.schedule.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quoc.schedule.core.data.ScheduleRepository
import com.quoc.schedule.core.database.Exam
import com.quoc.schedule.core.database.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Giờ học/tuần của một môn (tính từ ClassSession lặp). */
data class SubjectLoad(
    val subject: Subject,
    val hoursPerWeek: Float,
    val sessionsPerWeek: Int
)

data class StatsUiState(
    val totalHoursPerWeek: Float = 0f,
    val totalSubjects: Int = 0,
    val subjectLoads: List<SubjectLoad> = emptyList(),
    val upcomingExams: Int = 0,
    val totalExams: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeSessions(),
                repository.observeSubjects(),
                repository.observeExams()
            ) { sessions, subjects, exams ->
                Triple(sessions, subjects.associateBy { it.id }, exams)
            }.collect { (sessions, subjectMap, exams) ->
                val loads = sessions
                    .groupBy { it.subjectId }
                    .mapNotNull { (subjectId, list) ->
                        subjectMap[subjectId]?.let { subject ->
                            SubjectLoad(
                                subject = subject,
                                hoursPerWeek = list.sumOf { (it.endMinutes - it.startMinutes) } / 60f,
                                sessionsPerWeek = list.size
                            )
                        }
                    }
                    .sortedByDescending { it.hoursPerWeek }

                val today = LocalDate.now()
                _uiState.update {
                    it.copy(
                        totalHoursPerWeek = loads.sumOf { l -> l.hoursPerWeek.toDouble() }.toFloat(),
                        totalSubjects = loads.size,
                        subjectLoads = loads,
                        upcomingExams = exams.count { LocalDate.parse(it.examDate) >= today },
                        totalExams = exams.size,
                        isLoading = false
                    )
                }
            }
        }
    }
}
