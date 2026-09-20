package com.quoc.schedule.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quoc.schedule.core.data.prefs.AppLanguage
import com.quoc.schedule.core.data.prefs.ThemeMode
import com.quoc.schedule.core.data.prefs.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val semesterStartDate: LocalDate? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val currentWeek: Int? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepository.prefs.collect { prefs ->
                val today = LocalDate.now()
                _uiState.update {
                    it.copy(
                        semesterStartDate = prefs.semesterStartDate,
                        themeMode = prefs.themeMode,
                        language = prefs.language,
                        currentWeek = prefsRepository.weekNumberFor(prefs, today.with(DayOfWeek.MONDAY))
                    )
                }
            }
        }
    }

    fun setSemesterStart(date: LocalDate?) {
        viewModelScope.launch { prefsRepository.setSemesterStartDate(date) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepository.setLanguage(language) }
    }
}
