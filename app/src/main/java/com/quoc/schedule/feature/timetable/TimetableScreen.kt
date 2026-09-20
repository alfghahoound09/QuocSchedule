package com.quoc.schedule.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.quoc.schedule.ui.theme.getSubjectColor
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val DAY_START_MINUTES = 7 * 60        // 07:00
private const val DAY_END_MINUTES = 18 * 60 + 30    // 18:30
private const val SLOT_HEIGHT_DP = 56
private const val TIME_COLUMN_WIDTH_DP = 38
private const val DAY_COLUMN_WIDTH_DP = 84
private val SNAP_MINUTES = 30

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

    var viewMode by remember { mutableStateOf("Hôm nay") }

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
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFAFAF7))
        ) {
            WeekHeader(
                weekStart = state.weekStart,
                weekNumber = state.weekNumber,
                weekKnown = state.weekNumberKnown,
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onPrev = { viewModel.goToWeek(-1) },
                onNext = { viewModel.goToWeek(1) },
                onToday = { viewModel.goToday() }
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (viewMode == "Hôm nay") {
                    TimelineView(
                        entries = state.entries,
                        weekStart = state.weekStart,
                        isDark = isDark
                    )
                } else {
                    WeekGrid(
                        entries = state.entries,
                        weekStart = state.weekStart,
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
fun WeekHeader(
    weekStart: LocalDate,
    weekNumber: Int,
    weekKnown: Boolean,
    viewMode: String,
    onViewModeChange: (String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onPrev, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "‹ Tuần ${if (weekKnown) (weekNumber - 1).coerceAtLeast(1) else "trước"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onToday)
            ) {
                Text(
                    "${weekStart.format(formatter)} – ${weekStart.plusDays(6).format(formatter)} / " +
                            "%02d / %d".format(weekStart.monthValue, weekStart.year),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (weekKnown) "Tuần $weekNumber · chạm để về hôm nay"
                    else "chạm để về hôm nay",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onNext, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "Tuần ${if (weekKnown) weekNumber + 1 else "sau"} ›",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            val modes = listOf("Hôm nay", "Cả tuần")
            modes.forEach { mode ->
                val active = viewMode == mode
                Surface(
                    modifier = Modifier.weight(1f).clickable { onViewModeChange(mode) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ) {
                    Text(
                        text = mode,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekGrid(
    entries: List<TimetableEntry>,
    weekStart: LocalDate,
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
    val today = LocalDate.now()
    val horizontalScrollState = rememberScrollState()
    val gridWidth = TIME_COLUMN_WIDTH_DP + DAY_COLUMN_WIDTH_DP * 7

    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 8.dp)) {
        // ── Day Strip Header (FIXED) — Shows date + today highlight ──
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .width(gridWidth.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(TIME_COLUMN_WIDTH_DP.dp))
            dayLabels.forEachIndexed { index, day ->
                val date = weekStart.plusDays(index.toLong())
                val isToday = date == today
                Box(
                    modifier = Modifier
                        .width(DAY_COLUMN_WIDTH_DP.dp)
                        .padding(2.dp)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$day\n${date.dayOfMonth}",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        Box(
            Modifier
                .horizontalScroll(horizontalScrollState)
                .width(gridWidth.dp)
                .height(totalHeight)
                .onGloballyPositioned { coords ->
                    coords.size.width.also { drag.gridWidth = it }
                    drag.columnWidthPx = with(density) { DAY_COLUMN_WIDTH_DP.dp.toPx() }
                    drag.slotHeightPx = with(density) { SLOT_HEIGHT_DP.dp.toPx() }
                }
        ) {
            val now = LocalTime.now()
            val currentMinutes = now.hour * 60 + now.minute
            val currentLineY = if (
                today in weekStart..weekStart.plusDays(6) &&
                currentMinutes in DAY_START_MINUTES..DAY_END_MINUTES
            ) {
                ((currentMinutes - DAY_START_MINUTES).toFloat() / 60f) * SLOT_HEIGHT_DP
            } else 0f

            // lưới nền + cột giờ
            Row {
                Column(Modifier.width(TIME_COLUMN_WIDTH_DP.dp)) {
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
                Row(Modifier.width((DAY_COLUMN_WIDTH_DP * 7).dp)) {
                    repeat(7) { dayIdx ->
                        Column(Modifier.width(DAY_COLUMN_WIDTH_DP.dp)) {
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
                                            else if (isDark)
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            else
                                                Color.White,
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            if (currentLineY > 0f) {
                Box(
                    Modifier
                        .offset(y = currentLineY.dp)
                        .fillMaxWidth()
                        .padding(start = TIME_COLUMN_WIDTH_DP.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.width(0.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
                        )
                    }
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50))
                            .align(Alignment.TopCenter)
                    )
                }
            }

            // các card môn học (không tính card đang kéo)
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(TIME_COLUMN_WIDTH_DP.dp))
                for (dayIdx in 0..6) {
                    val dayEntries = entries.filter {
                        it.date.dayOfWeek.value == dayIdx + 1 && it.sessionId != drag.entry?.sessionId
                    }
                    Box(Modifier.width(DAY_COLUMN_WIDTH_DP.dp).fillMaxHeight()) {
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
                        .width(DAY_COLUMN_WIDTH_DP.dp)
                        .height(cardHeight)
                        .graphicsLayer { scaleX = 1.02f; scaleY = 1.02f; shadowElevation = 12f }
                        .alpha(0.92f)
                ) {
                    ClassCard(entry = entry, height = cardHeight, isDark = isDark, draggable = false)
                }
            }
        }
        Spacer(Modifier.height(100.dp)) // chừa chỗ cho FAB + bottom bar
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
    val (bgColor, accentColor) = getSubjectColor(entry.subject.code, isDark)
    val alpha = if (entry.isCancelled) 0.45f else 1f

    Surface(
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .then(
                if (draggable) Modifier.pointerInput(entry.sessionId) {
                    detectDragGesturesAfterLongPress(
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
        color = bgColor,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize().border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))) {
            // Left accent stripe
            Box(Modifier.width(3.dp).fillMaxHeight().background(accentColor))

            Column(Modifier.padding(start = 6.dp, end = 5.dp, top = 5.dp, bottom = 5.dp).fillMaxSize()) {
                // Subject code/name
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        entry.subject.code.ifBlank { entry.subject.name },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.isMakeup) {
                        Text("bù", fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Full subject name (if space)
                if (height > 56.dp) {
                    Text(
                        entry.subject.name,
                        fontSize = 9.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 10.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.weight(1f))

                // Room or status (PROMINENT at bottom)
                if (entry.isCancelled) {
                    Text("Đã hủy", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                } else {
                    entry.room?.let {
                        Text(
                            it,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color =                             Color(0xFFC77E2C), // HTML mockup's warm accent
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineView(
    entries: List<TimetableEntry>,
    weekStart: LocalDate,
    isDark: Boolean
) {
    val scrollState = rememberScrollState()
    val today = LocalDate.now()
    val totalSlots = (DAY_END_MINUTES - DAY_START_MINUTES) / 60
    val totalHeight = (SLOT_HEIGHT_DP * (totalSlots + 1)).dp
    val density = LocalDensity.current

    val dayIdx = (today.dayOfWeek.value - 1).coerceIn(0, 6)
    val todayEntries = entries.filter { it.date == today }

    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Thứ ${dayIdx + 2} - Hôm nay, ${today.dayOfMonth}/${today.monthValue}",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(Modifier.height(4.dp))

        Box(Modifier.fillMaxWidth().height(totalHeight)) {
            val now = LocalTime.now()
            val currentMinutes = now.hour * 60 + now.minute
            val currentLineY = if (
                today in weekStart..weekStart.plusDays(6) &&
                currentMinutes in DAY_START_MINUTES..DAY_END_MINUTES
            ) {
                ((currentMinutes - DAY_START_MINUTES).toFloat() / 60f) * SLOT_HEIGHT_DP
            } else 0f

            // lưới nền + cột giờ
            Row {
                Column(Modifier.width(TIME_COLUMN_WIDTH_DP.dp)) {
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
                Column(Modifier.fillMaxWidth()) {
                    repeat(totalSlots + 1) {
                        Box(
                            Modifier.height(SLOT_HEIGHT_DP.dp).fillMaxWidth()
                                .padding(1.dp)
                                .background(
                                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    else Color.White,
                                    RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }

            if (currentLineY > 0f) {
                Box(
                    Modifier
                        .offset(y = currentLineY.dp)
                        .fillMaxWidth()
                        .padding(start = TIME_COLUMN_WIDTH_DP.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.width(0.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
                        )
                    }
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50))
                            .align(Alignment.TopCenter)
                    )
                }
            }

            // Timeline cards (full width)
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(TIME_COLUMN_WIDTH_DP.dp))
                Box(Modifier.fillMaxWidth().fillMaxHeight()) {
                    todayEntries.forEach { entry ->
                        val startOffset = (entry.startMinutes - DAY_START_MINUTES).coerceAtLeast(0)
                        val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                        val top = (startOffset * SLOT_HEIGHT_DP / 60f).dp
                        val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                        Box(Modifier.fillMaxWidth().offset(y = top).height(cardHeight).padding(end = 8.dp)) {
                            ClassCard(
                                entry = entry,
                                height = cardHeight,
                                isDark = isDark,
                                draggable = false
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
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
