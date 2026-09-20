package com.quoc.schedule.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.R
import com.quoc.schedule.core.data.prefs.ThemeMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.settings_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Học kỳ ──
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_semester), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.settings_semester_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.semesterStartDate?.format(formatter) ?: stringResource(R.string.settings_not_set),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { showDatePicker = true }, shape = RoundedCornerShape(14.dp)) {
                            Text(stringResource(R.string.settings_pick_date))
                        }
                        if (state.semesterStartDate != null) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.setSemesterStart(null) }) { Text(stringResource(R.string.settings_clear)) }
                        }
                    }
                    state.currentWeek?.let {
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(stringResource(R.string.settings_current_week, it),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ── Giao diện ──
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { i, mode ->
                            SegmentedButton(
                                selected = state.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size)
                            ) {
                                Text(when (mode) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                                })
                            }
                        }
                    }
                }
            }

            // ── Ngôn ngữ ──
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        com.quoc.schedule.core.data.prefs.AppLanguage.entries.forEachIndexed { i, lang ->
                            SegmentedButton(
                                selected = state.language == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                shape = SegmentedButtonDefaults.itemShape(i, com.quoc.schedule.core.data.prefs.AppLanguage.entries.size)
                            ) {
                                Text(when (lang) {
                                    com.quoc.schedule.core.data.prefs.AppLanguage.SYSTEM -> stringResource(R.string.settings_theme_system)
                                    com.quoc.schedule.core.data.prefs.AppLanguage.VI -> stringResource(R.string.settings_lang_vi)
                                    com.quoc.schedule.core.data.prefs.AppLanguage.EN -> stringResource(R.string.settings_lang_en)
                                })
                            }
                        }
                    }
                }
            }

            val quickJumpOnOpen = remember { mutableStateOf(true) }
            Card(shape = RoundedCornerShape(18.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_jump_today), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.settings_jump_today_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = quickJumpOnOpen.value,
                        onCheckedChange = { quickJumpOnOpen.value = it }
                    )
                }
            }

            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(stringResource(R.string.settings_export_sync), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { /* placeholder */ }, modifier = Modifier.weight(1f)) {
                            Text("📅 Google Cal")
                        }
                        OutlinedButton(onClick = { /* placeholder */ }, modifier = Modifier.weight(1f)) {
                            Text("🎨 Widget")
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (state.semesterStartDate ?: LocalDate.now())
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.setSemesterStart(
                            java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.settings_select)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.settings_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
