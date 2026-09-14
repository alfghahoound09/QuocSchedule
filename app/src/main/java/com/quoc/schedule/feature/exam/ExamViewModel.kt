package com.quoc.schedule.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quoc.schedule.core.data.ScheduleRepository
import com.quoc.schedule.core.database.Exam
import com.quoc.schedule.core.database.Subject
import com.quoc.schedule.domain.GetExamCountdownUseCase
import com.quoc.schedule.domain.NextExamInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExamUiState(
    val upcoming: List<Pair<Exam, Subject>> = emptyList(),
    val past: List<Pair<Exam, Subject>> = emptyList(),
    val nextExam: NextExamInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val getCountdown: GetExamCountdownUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeExams(), repository.observeSubjects()) { exams, subjects ->
                exams to subjects.associateBy { it.id }
            }.collect { (exams, subjectMap) ->
                val today = LocalDate.now()
                val withSubject = exams.mapNotNull { e -> subjectMap[e.subjectId]?.let { e to it } }
                val upcoming = withSubject.filter { LocalDate.parse(it.first.examDate) >= today }
                    .sortedWith(compareBy({ it.first.examDate }, { it.first.startMinutes }))
                val past = withSubject.filter { LocalDate.parse(it.first.examDate) < today }
                    .sortedByDescending { it.first.examDate }
                _uiState.update {
                    it.copy(
                        upcoming = upcoming,
                        past = past,
                        nextExam = getCountdown(exams, subjectMap, today),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch { repository.deleteExam(exam) }
    }
}
