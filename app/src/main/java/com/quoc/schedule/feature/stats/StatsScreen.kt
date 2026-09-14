package com.quoc.schedule.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.feature.timetable.ScheduleBottomBar
import com.quoc.schedule.ui.theme.subjectCardColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ScheduleBottomBar(
                selected = 2,
                onExams = onNavigateToExams,
                onStats = {},
                onSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Thống kê 📊", style = MaterialTheme.typography.headlineSmall)
            }

            // ── 3 thẻ tổng quan ──
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "⏱",
                        value = "%.1f".format(state.totalHoursPerWeek),
                        label = "giờ/tuần"
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "📚",
                        value = "${state.totalSubjects}",
                        label = "môn học"
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "📝",
                        value = "${state.upcomingExams}",
                        label = "kỳ thi sắp tới"
                    )
                }
            }

            item {
                Text(
                    "Giờ học theo môn",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.subjectLoads.isEmpty() && !state.isLoading) {
                item {
                    Card(shape = RoundedCornerShape(18.dp)) {
                        Text(
                            "Chưa có dữ liệu — thêm lịch học để xem thống kê ✨",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            val maxHours = state.subjectLoads.maxOfOrNull { it.hoursPerWeek } ?: 1f
            items(state.subjectLoads, key = { it.subject.id }) { load ->
                SubjectLoadBar(load = load, maxHours = maxHours, isDark = isDark)
            }

            item { Spacer(Modifier.height(70.dp)) }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, emoji: String, value: String, label: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SubjectLoadBar(load: SubjectLoad, maxHours: Float, isDark: Boolean) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(12.dp)
                        .background(
                            subjectCardColor(load.subject.colorKey, isDark),
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    load.subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    "%.1fh · ${load.sessionsPerWeek} buổi".format(load.hoursPerWeek),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            // thanh bar tỉ lệ
            LinearProgressIndicator(
                progress = { (load.hoursPerWeek / maxHours).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = subjectCardColor(load.subject.colorKey, isDark),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
