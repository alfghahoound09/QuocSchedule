package com.quoc.schedule.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quoc.schedule.domain.TimetableEntry
import com.quoc.schedule.ui.theme.subjectCardColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val DAY_START_MINUTES = 7 * 60
private const val DAY_END_MINUTES = 18 * 60 + 30
private const val SLOT_HEIGHT_DP = 64
private val SNAP_MINUTES = 30

@Composable
fun TimetableScreenNew(
    onNavigateToImport: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (state.themeMode) {
        com.quoc.schedule.core.data.prefs.ThemeMode.LIGHT -> false
        com.quoc.schedule.core.data.prefs.ThemeMode.DARK -> true
        com.quoc.schedule.core.data.prefs.ThemeMode.SYSTEM -> systemDark
    }
    val haptics = LocalHapticFeedback.current
    val drag = remember { DragState() }
    var conflictDialog by remember { mutableStateOf<Triple<TimetableEntry, List<TimetableEntry>, DropTarget>?>(null) }

    Scaffold(
        containerColor = Color(0xFFFAFAF7),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToImport,
                shape = RoundedCornerShape(16.dp),
                containerColor = Color(0xFF7FB3D9)
            ) { Icon(Icons.Default.Add, contentDescription = "Thêm lịch") }
        },
        bottomBar = {
            ScheduleBottomBarNew(selected = 0, onExams = onNavigateToExams, onStats = onNavigateToStats, onSettings = onNavigateToSettings)
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Color(0xFFFAFAF7))) {
            TopTabsNew(selected = 0, onSelect = { if (it == 1) onNavigateToExams() })
            WeekHeaderNew(
                weekStart = state.weekStart,
                weekNumber = state.weekNumber,
                weekKnown = state.weekNumberKnown,
                onPrev = { viewModel.goToWeek(-1) },
                onNext = { viewModel.goToWeek(1) },
                onToday = { viewModel.goToday() },
                isDark = isDark,
                onToggleTheme = { viewModel.toggleThemeMode(systemDark) }
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                WeekGridNew(
                    entries = state.entries,
                    isDark = isDark,
                    drag = drag,
                    onDropConfirm = { entry, target ->
                        val targetDate = state.weekStart.plusDays(target.dayIdx.toLong())
                        val conflicts = viewModel.findConflictsForMove(entry.sessionId, targetDate, target.startMinutes)
                        if (conflicts.isEmpty()) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.applyMove(entry.sessionId, targetDate, target.startMinutes)
                        } else {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            conflictDialog = Triple(entry, conflicts, target)
                        }
                    },
                    onDragCancel = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                )
            }
        }
    }

    conflictDialog?.let { (entry, conflicts, target) ->
        AlertDialog(
            onDismissRequest = { conflictDialog = null },
            title = { Text("Trùng lịch học ⚠️") },
            text = {
                Column {
                    Text("Buổi này sẽ trùng giờ với ${conflicts.size} môn khác:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    conflicts.forEach { c ->
                        Text(
                            "• ${c.subject.name} (%02d:%02d–%02d:%02d)".format(c.startMinutes / 60, c.startMinutes % 60, c.endMinutes / 60, c.endMinutes % 60),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.applyMove(entry.sessionId, state.weekStart.plusDays(target.dayIdx.toLong()), target.startMinutes)
                    conflictDialog = null
                }) { Text("Vẫn chuyển") }
            },
            dismissButton = { TextButton(onClick = { conflictDialog = null }) { Text("Hủy") } }
        )
    }
}

@Composable
fun TopTabsNew(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFEDEAE3)
    ) {
        Row(Modifier.padding(4.dp)) {
            listOf("📅 Lịch học", "📝 Lịch thi").forEachIndexed { i, label ->
                val active = i == selected
                Surface(
                    modifier = Modifier.weight(1f).clickable { onSelect(i) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) Color(0xFFFFFFFF) else Color(0xFFEDEAE3)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 9.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Color(0xFF2E3A45) else Color(0xFF6B7B88),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WeekHeaderNew(
    weekStart: LocalDate,
    weekNumber: Int,
    weekKnown: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onPrev) {
            Text("‹ Tuần ${weekNumber-1}", color = Color(0xFF6B7B88), fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onToday)) {
            Text(
                weekStart.format(formatter) + " – " + weekStart.plusDays(6).format(formatter),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E3A45),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (weekKnown) {
                Text("Tuần $weekNumber", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7B88), fontSize = 11.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onNext) {
                Text("Tuần ${weekNumber+1} ›", color = Color(0xFF6B7B88), fontSize = 11.sp)
            }
            IconButton(onClick = onToggleTheme, modifier = Modifier.size(32.dp)) {
                Text(text = if (isDark) "☀️" else "🌙", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun WeekGridNew(
    entries: List<TimetableEntry>,
    isDark: Boolean,
    drag: DragState,
    onDropConfirm: (TimetableEntry, DropTarget) -> Unit,
    onDragCancel: () -> Unit
) {
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6")
    val scrollState = rememberScrollState()
    val totalSlots = (DAY_END_MINUTES - DAY_START_MINUTES) / 60
    val totalHeight = (SLOT_HEIGHT_DP * (totalSlots + 1)).dp
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 8.dp).background(Color(0xFFFAFAF7))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(34.dp))
            dayLabels.forEachIndexed { _, day ->
                Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                    Text(day, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF6B7B88), fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        Box(
            Modifier.fillMaxWidth().height(totalHeight)
                .onGloballyPositioned { coords ->
                    drag.columnWidthPx = (coords.size.width - with(density) { 34.dp.toPx() }) / 5f
                    drag.slotHeightPx = with(density) { SLOT_HEIGHT_DP.dp.toPx() }
                }
        ) {
            Row {
                Column(Modifier.width(34.dp)) {
                    for (slot in 0..totalSlots) {
                        Text(
                            "%02d:00".format((DAY_START_MINUTES + slot * 60) / 60),
                            modifier = Modifier.height(SLOT_HEIGHT_DP.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color(0xFF6B7B88)
                        )
                    }
                }
                Row(Modifier.weight(1f)) {
                    repeat(5) { dayIdx ->
                        Column(Modifier.weight(1f)) {
                            repeat(totalSlots + 1) { slot ->
                                val isHovered = drag.hoveredTarget?.let { it.dayIdx == dayIdx && slot == (it.startMinutes - DAY_START_MINUTES) / 60 } == true
                                Box(
                                    Modifier.height(SLOT_HEIGHT_DP.dp).fillMaxWidth()
                                        .padding(1.dp)
                                        .background(if (isHovered) Color(0xFF7FB3D9).copy(alpha = 0.15f) else Color(0xFFEDEAE3), RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(34.dp))
                for (dayIdx in 0..4) {
                    val dayEntries = entries.filter { it.date.dayOfWeek.value == dayIdx + 1 && it.sessionId != drag.entry?.sessionId }
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        dayEntries.forEach { entry ->
                            val startOffset = (entry.startMinutes - DAY_START_MINUTES).coerceAtLeast(0)
                            val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                            val top = (startOffset * SLOT_HEIGHT_DP / 60f).dp
                            val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                            Box(Modifier.fillMaxWidth().offset(y = top).height(cardHeight)) {
                                ClassCardNew(
                                    entry = entry,
                                    height = cardHeight,
                                    isDark = isDark,
                                    draggable = true,
                                    onDragStart = {
                                        drag.entry = entry
                                        drag.offset = Offset.Zero
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDrag = { delta ->
                                        drag.offset += delta
                                        val dayW = drag.columnWidthPx
                                        if (dayW > 0) {
                                            val x = dayIdx * dayW + drag.offset.x + dayW / 2
                                            val y = (entry.startMinutes - DAY_START_MINUTES) / 60f * drag.slotHeightPx + drag.offset.y
                                            val newDay = (x / dayW).roundToInt().coerceIn(0, 4)
                                            val rawMin = DAY_START_MINUTES + (y / drag.slotHeightPx * 60).roundToInt()
                                            val snapped = (rawMin / SNAP_MINUTES) * SNAP_MINUTES
                                            drag.hoveredTarget = DropTarget(newDay, snapped.coerceIn(DAY_START_MINUTES, DAY_END_MINUTES - 30))
                                        }
                                    },
                                    onDragEnd = {
                                        val target = drag.hoveredTarget
                                        val current = drag.entry
                                        if (current != null && target != null) onDropConfirm(current, target) else if (current != null) onDragCancel()
                                        drag.entry = null
                                        drag.offset = Offset.Zero
                                        drag.hoveredTarget = null
                                    },
                                    onDragCancelLocal = {
                                        onDragCancel()
                                        drag.entry = null
                                        drag.offset = Offset.Zero
                                        drag.hoveredTarget = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            drag.entry?.let { entry ->
                val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                val baseX = 34.dp.value * density.density + (entry.date.dayOfWeek.value - 1) * drag.columnWidthPx
                val baseY = (entry.startMinutes - DAY_START_MINUTES) / 60f * drag.slotHeightPx
                Box(
                    Modifier
                        .zIndex(10f)
                        .offset { IntOffset((baseX + drag.offset.x).roundToInt(), (baseY + drag.offset.y).roundToInt()) }
                        .width((drag.columnWidthPx / density.density).dp)
                        .height(cardHeight)
                        .graphicsLayer { scaleX = 1.02f; scaleY = 1.02f; shadowElevation = 8f }
                        .alpha(0.92f)
                ) {
                    ClassCardNew(entry = entry, height = cardHeight, isDark = isDark, draggable = false)
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun ClassCardNew(
    entry: TimetableEntry,
    height: androidx.compose.ui.unit.Dp,
    isDark: Boolean,
    draggable: Boolean = false,
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancelLocal: () -> Unit = {}
) {
    val color = subjectCardColor(entry.subject.colorKey, isDark)
    val alpha = if (entry.isCancelled) 0.45f else 1f
    Surface(
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .then(
                if (draggable) Modifier.pointerInput(entry.sessionId) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDrag = { change, amount -> change.consume(); onDrag(amount) },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragCancelLocal() }
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(10.dp),
        color = color,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.subject.code.ifBlank { entry.subject.name },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.isMakeup) {
                    Spacer(Modifier.width(4.dp))
                    Text("bù", fontSize = 8.sp, color = Color(0xFF6B7B88))
                }
            }
            if (height > 56.dp) {
                Text(entry.subject.name, fontSize = 8.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 10.sp)
            }
            if (entry.isCancelled) {
                Text("Đã hủy", fontSize = 8.sp, color = Color(0xFFF5B8B1))
            } else {
                entry.room?.let { Text(it, fontSize = 8.sp, color = Color(0xFF6B7B88)) }
            }
        }
    }
}

@Composable
fun ScheduleBottomBarNew(
    selected: Int,
    onExams: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(containerColor = Color(0xFFFFFFFF), contentColor = Color(0xFF2E3A45)) {
        NavigationBarItem(selected = selected == 0, onClick = {}, icon = { Text("🏠", fontSize = 16.sp) }, label = { Text("Lịch", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B8FB0), selectedTextColor = Color(0xFF5B8FB0), unselectedIconColor = Color(0xFF6B7B88), unselectedTextColor = Color(0xFF6B7B88)))
        NavigationBarItem(selected = selected == 1, onClick = onExams, icon = { Text("📝", fontSize = 16.sp) }, label = { Text("Thi", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B8FB0), selectedTextColor = Color(0xFF5B8FB0), unselectedIconColor = Color(0xFF6B7B88), unselectedTextColor = Color(0xFF6B7B88)))
        NavigationBarItem(selected = selected == 2, onClick = onStats, icon = { Text("📊", fontSize = 16.sp) }, label = { Text("Thống kê", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B8FB0), selectedTextColor = Color(0xFF5B8FB0), unselectedIconColor = Color(0xFF6B7B88), unselectedTextColor = Color(0xFF6B7B88)))
        NavigationBarItem(selected = selected == 3, onClick = onSettings, icon = { Text("⚙️", fontSize = 16.sp) }, label = { Text("Cài đặt", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF5B8FB0), selectedTextColor = Color(0xFF5B8FB0), unselectedIconColor = Color(0xFF6B7B88), unselectedTextColor = Color(0xFF6B7B88)))
    }
}
