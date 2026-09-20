package com.quoc.schedule.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val selectedRange = remember { mutableStateOf("Tuần này") }
    val rangeOptions = listOf("Tuần này", "Cả học kỳ")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ScheduleBottomBar(
                selected = 2,
                onTimetable = onNavigateToTimetable,
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
                        icon = Icons.Rounded.Timer,
                        iconColor = Color(0xFF38BDF8), // Blue accent
                        value = "%.1f".format(if (selectedRange.value == "Tuần này") state.totalHoursPerWeek else state.totalHoursSemester),
                        label = if (selectedRange.value == "Tuần này") "giờ/tuần" else "giờ/kỳ"
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.MenuBook,
                        iconColor = Color(0xFFA855F7), // Purple accent
                        value = "${state.totalSubjects}",
                        label = "môn học"
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Event,
                        iconColor = Color(0xFFFB923C), // Orange accent
                        value = "${state.upcomingExams}",
                        label = "kỳ thi tới"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rangeOptions.forEach { range ->
                        val selected = selectedRange.value == range
                        FilterChip(
                            selected = selected,
                            onClick = { selectedRange.value = range },
                            label = { Text(range) }
                        )
                    }
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
fun SummaryCard(modifier: Modifier, icon: ImageVector, iconColor: Color, value: String, label: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier.size(12.dp)
                        .background(
                            subjectCardColor(load.subject.colorKey, isDark),
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        load.subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "%.1fh · %d buổi".format(load.hoursPerWeek, load.sessionsPerWeek),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val ratio = (load.hoursPerWeek / maxHours).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f).height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
                    Box(
                        Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        subjectCardColor(load.subject.colorKey, isDark).copy(alpha = 0.6f),
                                        subjectCardColor(load.subject.colorKey, isDark)
                                    )
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${(ratio * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = subjectCardColor(load.subject.colorKey, isDark)
                )
            }
        }
    }
}
