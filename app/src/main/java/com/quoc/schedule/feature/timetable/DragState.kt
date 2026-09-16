package com.quoc.schedule.feature.timetable

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import com.quoc.schedule.domain.TimetableEntry

/** Vị trí thả: ngày trong tuần (0..4 hoặc 0..6) + phút bắt đầu trong ngày. */
data class DropTarget(val dayIdx: Int, val startMinutes: Int)

/** Trạng thái kéo–thả trong lưới tuần. */
class DragState {
    var entry by mutableStateOf<TimetableEntry?>(null)
    var offset by mutableStateOf(Offset.Zero)
    var columnWidthPx by mutableStateOf(0f)
    var slotHeightPx by mutableStateOf(0f)
    var hoveredTarget by mutableStateOf<DropTarget?>(null)
}
