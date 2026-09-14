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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
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

private const val DAY_START_MINUTES = 7 * 60        // 07:00
private const val DAY_END_MINUTES = 18 * 60 + 30    // 18:30
private const val SLOT_HEIGHT_DP = 64               // 60 phút = 64dp
private val SNAP_MINUTES = 30

/** Vị trí thả: ngày trong tuần (0..6) + phút bắt đầu trong ngày. */
private data class DropTarget(val dayIdx: Int, val startMinutes: Int)

/** Trạng thái kéo–thả trong lưới tuần. */
private class DragState {
    var entry by mutableStateOf<TimetableEntry?>(null)
    var offset by mutableStateOf(Offset.Zero)
    var gridWidth by mutableStateOf(0)
    var columnWidthPx by mutableStateOf(0f)
    var slotHeightPx by mutableStateOf(0f)
    var hoveredTarget by mutableStateOf<DropTarget?>(null)
}

@Composable
fun TimetableScreen(
    onNavigateToImport: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val haptics = LocalHapticFeedback.current

    // Trạng thái drag + dialog conflict (giữ target vì drag state reset khi thả)
    val drag = remember { DragState() }
    var conflictDialog by remember {
        mutableStateOf<Triple<TimetableEntry, List<TimetableEntry>, DropTarget>?>(null)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToImport,
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Thêm lịch") }
        },
        bottomBar = {
            ScheduleBottomBar(
                selected = 0,
                onExams = onNavigateToExams,
                onStats = onNavigateToStats,
                onSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TopTabs(selected = 0, onSelect = { if (it == 1) onNavigateToExams() })
            WeekHeader(
                weekStart = state.weekStart,
                weekNumber = state.weekNumber,
                weekKnown = state.weekNumberKnown,
                onPrev = { viewModel.goToWeek(-1) },
                onNext = { viewModel.goToWeek(1) },
                onToday = { viewModel.goToday() }
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                WeekGrid(
                    entries = state.entries,
                    isDark = isDark,
                    drag = drag,
                    onDropConfirm = { entry, target ->
                        val targetDate = state.weekStart.plusDays(target.dayIdx.toLong())
                        val conflicts = viewModel.findConflictsForMove(
                            entry.sessionId, targetDate, target.startMinutes
                        )
                        if (conflicts.isEmpty()) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.applyMove(entry.sessionId, targetDate, target.startMinutes)
                        } else {
                            // haptic "từ chối" kép
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            conflictDialog = Triple(entry, conflicts, target)
                        }
                    },
                    onDragCancel = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            }
        }
    }

    // Dialog conflict khi thả vào ô đã có môn
    conflictDialog?.let { (entry, conflicts, target) ->
        AlertDialog(
            onDismissRequest = { conflictDialog = null },
            title = { Text("Trùng lịch học ⚠️") },
            text = {
                Column {
                    Text(
                        "Buổi này sẽ trùng giờ với ${conflicts.size} môn khác:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    conflicts.forEach { c ->
                        Text(
                            "• ${c.subject.name} (" +
                                    "%02d:%02d–%02d:%02d".format(
                                        c.startMinutes / 60, c.startMinutes % 60,
                                        c.endMinutes / 60, c.endMinutes % 60
                                    ) + ")",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.applyMove(
                        entry.sessionId,
                        state.weekStart.plusDays(target.dayIdx.toLong()),
                        target.startMinutes
                    )
                    conflictDialog = null
                }) { Text("Vẫn chuyển") }
            },
            dismissButton = {
                TextButton(onClick = { conflictDialog = null }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun TopTabs(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(4.dp)) {
            listOf("📅 Lịch học", "📝 Lịch thi").forEachIndexed { i, label ->
                val active = i == selected
                Surface(
                    modifier = Modifier.weight(1f).clickable { onSelect(i) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 9.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WeekHeader(
    weekStart: LocalDate,
    weekNumber: Int,
    weekKnown: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, "Tuần trước") }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onToday)) {
            Text(
                "${weekStart.format(formatter)} – ${weekStart.plusDays(6).format(formatter)} / ${weekStart.year}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (weekKnown) "Tuần $weekNumber · chạm để về hôm nay" else "chạm để về hôm nay · đặt tuần 1 trong Cài đặt",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, "Tuần sau") }
    }
}

@Composable
private fun WeekGrid(
    entries: List<TimetableEntry>,
    isDark: Boolean,
    drag: DragState,
    onDropConfirm: (TimetableEntry, DropTarget) -> Unit,
    onDragCancel: () -> Unit
) {
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val scrollState = rememberScrollState()
    val totalSlots = (DAY_END_MINUTES - DAY_START_MINUTES) / 60
    val totalHeight = (SLOT_HEIGHT_DP * (totalSlots + 1)).dp
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 8.dp)) {
        // Header thứ
        Row {
            Spacer(Modifier.width(38.dp))
            dayLabels.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        Box(
            Modifier.fillMaxWidth().height(totalHeight)
                .onGloballyPositioned { coords ->
                    drag.gridWidth = coords.size.width
                    drag.columnWidthPx = (coords.size.width - with(density) { 38.dp.toPx() }) / 7f
                    drag.slotHeightPx = with(density) { SLOT_HEIGHT_DP.dp.toPx() }
                }
        ) {
            // lưới nền + cột giờ
            Row {
                Column(Modifier.width(38.dp)) {
                    for (slot in 0..totalSlots) {
                        Text(
                            "%02d:00".format((DAY_START_MINUTES + slot * 60) / 60),
                            modifier = Modifier.height(SLOT_HEIGHT_DP.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(Modifier.weight(1f)) {
                    repeat(7) { dayIdx ->
                        Column(Modifier.weight(1f)) {
                            repeat(totalSlots + 1) { slot ->
                                val isHovered = drag.hoveredTarget?.let {
                                    it.dayIdx == dayIdx &&
                                        slot == (it.startMinutes - DAY_START_MINUTES) / 60
                                } == true
                                Box(
                                    Modifier.height(SLOT_HEIGHT_DP.dp).fillMaxWidth()
                                        .padding(1.dp)
                                        .background(
                                            if (isHovered)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // các card môn học (không tính card đang kéo)
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(38.dp))
                for (dayIdx in 0..6) {
                    val dayEntries = entries.filter {
                        it.date.dayOfWeek.value == dayIdx + 1 && it.sessionId != drag.entry?.sessionId
                    }
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        dayEntries.forEach { entry ->
                            val startOffset = (entry.startMinutes - DAY_START_MINUTES).coerceAtLeast(0)
                            val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                            val top = (startOffset * SLOT_HEIGHT_DP / 60f).dp
                            val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                            Box(Modifier.fillMaxWidth().offset(y = top).height(cardHeight)) {
                                ClassCard(
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
                                        // tính ô đích (snap 30')
                                        val dayW = drag.columnWidthPx
                                        if (dayW > 0) {
                                            val x = dayIdx * dayW + drag.offset.x + dayW / 2
                                            val y = (entry.startMinutes - DAY_START_MINUTES) / 60f *
                                                    drag.slotHeightPx + drag.offset.y
                                            val newDay = (x / dayW).roundToInt().coerceIn(0, 6)
                                            val rawMin = DAY_START_MINUTES +
                                                    (y / drag.slotHeightPx * 60).roundToInt()
                                            val snapped = (rawMin / SNAP_MINUTES) * SNAP_MINUTES
                                            drag.hoveredTarget = DropTarget(
                                                newDay, snapped.coerceIn(
                                                    DAY_START_MINUTES, DAY_END_MINUTES - 30
                                                )
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        val target = drag.hoveredTarget
                                        val current = drag.entry
                                        if (current != null && target != null) {
                                            onDropConfirm(current, target)
                                        } else if (current != null) {
                                            onDragCancel()
                                        }
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

            // card đang kéo — nổi lên trên, scale nhẹ
            drag.entry?.let { entry ->
                val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                val baseX = 38.dp.value * density.density +
                        (entry.date.dayOfWeek.value - 1) * drag.columnWidthPx
                val baseY = (entry.startMinutes - DAY_START_MINUTES) / 60f * drag.slotHeightPx
                Box(
                    Modifier
                        .zIndex(10f)
                        .offset { IntOffset((baseX + drag.offset.x).roundToInt(), (baseY + drag.offset.y).roundToInt()) }
                        .width((drag.columnWidthPx / density.density).dp)
                        .height(cardHeight)
                        .graphicsLayer { scaleX = 1.02f; scaleY = 1.02f; shadowElevation = 12f }
                        .alpha(0.92f)
                ) {
                    ClassCard(entry = entry, height = cardHeight, isDark = isDark, draggable = false)
                }
            }
        }
        Spacer(Modifier.height(80.dp)) // chừa chỗ cho FAB + bottom bar
    }
}

@Composable
fun ClassCard(
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
                        onDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
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
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.isMakeup) {
                    Spacer(Modifier.width(4.dp))
                    Text("bù", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (height > 56.dp) {
                Text(
                    entry.subject.name,
                    fontSize = 9.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 11.sp
                )
            }
            if (entry.isCancelled) {
                Text("Đã hủy", fontSize = 8.sp, color = MaterialTheme.colorScheme.error)
            } else {
                entry.room?.let {
                    Text(it, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ScheduleBottomBar(
    selected: Int,
    onExams: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = selected == 0, onClick = {},
            icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("Lịch") }
        )
        NavigationBarItem(
            selected = selected == 1, onClick = onExams,
            icon = { Icon(Icons.Default.EventNote, null) }, label = { Text("Thi") }
        )
        NavigationBarItem(
            selected = selected == 2, onClick = onStats,
            icon = { Icon(Icons.Outlined.BarChart, null) }, label = { Text("Thống kê") }
        )
        NavigationBarItem(
            selected = selected == 3, onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Cài đặt") }
        )
    }
}
