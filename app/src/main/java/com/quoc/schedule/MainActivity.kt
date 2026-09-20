package com.quoc.schedule

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quoc.schedule.core.data.prefs.AppLanguage
import com.quoc.schedule.core.data.prefs.ThemeMode
import com.quoc.schedule.core.data.prefs.UserPrefsRepository
import com.quoc.schedule.feature.camera.CameraScreen
import com.quoc.schedule.feature.exam.ExamScreen
import com.quoc.schedule.feature.importflow.ImportFlowScreen
import com.quoc.schedule.feature.settings.SettingsScreen
import com.quoc.schedule.feature.stats.StatsScreen
import com.quoc.schedule.feature.timetable.TimetableScreen
import com.quoc.schedule.feature.timetable.TimetableScreenNew
import com.quoc.schedule.ui.theme.QuocScheduleTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

object Routes {
    const val TIMETABLE = "timetable"
    const val EXAMS = "exams"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val IMPORT = "import"
    const val CAMERA = "camera"
}

@HiltViewModel
class AppViewModel @Inject constructor(
    prefsRepository: UserPrefsRepository
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = prefsRepository.prefs
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val language: StateFlow<AppLanguage> = prefsRepository.prefs
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.SYSTEM)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
            val language by appViewModel.language.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(language) {
                if (language != AppLanguage.SYSTEM) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            }

            QuocScheduleTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()

                val navigateToTopLevel: (String) -> Unit = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                NavHost(navController = navController, startDestination = Routes.TIMETABLE) {
                    composable(Routes.TIMETABLE) {
                        TimetableScreenNew(
                            onNavigateToImport = { navController.navigate(Routes.IMPORT) },
                            onNavigateToExams = { navigateToTopLevel(Routes.EXAMS) },
                            onNavigateToStats = { navigateToTopLevel(Routes.STATS) },
                            onNavigateToSettings = { navigateToTopLevel(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.EXAMS) {
                        ExamScreen(
                            onNavigateToTimetable = { navigateToTopLevel(Routes.TIMETABLE) },
                            onNavigateToStats = { navigateToTopLevel(Routes.STATS) },
                            onNavigateToSettings = { navigateToTopLevel(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.STATS) {
                        StatsScreen(
                            onNavigateToTimetable = { navigateToTopLevel(Routes.TIMETABLE) },
                            onNavigateToExams = { navigateToTopLevel(Routes.EXAMS) },
                            onNavigateToSettings = { navigateToTopLevel(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.IMPORT) { backStackEntry ->
                        val importViewModel: com.quoc.schedule.feature.importflow.ImportViewModel =
                            hiltViewModel()
                        // Nhận ảnh từ CameraScreen quay lại
                        androidx.compose.runtime.LaunchedEffect(backStackEntry) {
                            backStackEntry.savedStateHandle
                                .getStateFlow<String?>("captured_uri", null)
                                .collect { uriStr ->
                                    if (uriStr != null) {
                                        importViewModel.importImage(Uri.parse(uriStr))
                                        backStackEntry.savedStateHandle["captured_uri"] = null
                                    }
                                }
                        }
                        ImportFlowScreen(
                            onFinished = { navController.popBackStack(Routes.TIMETABLE, false) },
                            onCancel = { navController.popBackStack() },
                            onOpenCamera = { navController.navigate(Routes.CAMERA, null) },
                            viewModel = importViewModel
                        )
                    }
                    composable(Routes.CAMERA) {
                        CameraScreen(
                            onCapture = { uri ->
                                // Quay về import và gửi ảnh vào flow OCR
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("captured_uri", uri.toString())
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
