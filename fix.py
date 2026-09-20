import re

with open(r'd:\QuocSchedule\app\src\main\java\com\quoc\schedule\feature\timetable\TimetableScreenNew.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add state to TimetableScreenNew
content = content.replace(
    '''    var conflictDialog by remember { mutableStateOf<Triple<TimetableEntry, List<TimetableEntry>, DropTarget>?>(null) }''',
    '''    var conflictDialog by remember { mutableStateOf<Triple<TimetableEntry, List<TimetableEntry>, DropTarget>?>(null) }
    var editingEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }'''
)

# 2. Update Scaffold FAB
old_fab = '''        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToImport,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Thêm lịch") }
        },'''
new_fab = '''        floatingActionButton = {
            Box {
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(text = { Text("Nhập từ ảnh/web") }, onClick = { showFabMenu = false; onNavigateToImport() })
                    DropdownMenuItem(text = { Text("Thêm thủ công") }, onClick = { 
                        showFabMenu = false
                        editingEntry = TimetableEntry(
                            sessionId = -1,
                            subject = com.quoc.schedule.core.database.Subject(id = -1, code = "", name = "", lecturer = "", colorKey = 0),
                            date = state.weekStart,
                            startMinutes = 7 * 60,
                            endMinutes = 9 * 60,
                            room = "",
                            isMakeup = false,
                            isCancelled = false
                        )
                    })
                }
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Add, contentDescription = "Thêm lịch") }
            }
        },'''
content = content.replace(old_fab, new_fab)

# 3. Add EditEntrySheet usage
old_dialog = '''            dismissButton = { TextButton(onClick = { conflictDialog = null }) { Text("Hủy") } }
        )
    }
}'''
new_dialog = '''            dismissButton = { TextButton(onClick = { conflictDialog = null }) { Text("Hủy") } }
        )
    }

    editingEntry?.let { entry ->
        EditEntrySheet(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { name, room, day, start, end ->
                if (entry.sessionId == -1L) {
                    // Placeholder for create
                } else {
                    viewModel.updateEntry(entry.sessionId, name, room, day, start, end)
                }
                editingEntry = null
            },
            onDelete = {
                if (entry.sessionId != -1L) viewModel.deleteEntry(entry.sessionId)
                editingEntry = null
            }
        )
    }
}'''
content = content.replace(old_dialog, new_dialog)

# 4. WeekGridNew signature
content = content.replace(
    '''    onDropConfirm: (TimetableEntry, DropTarget) -> Unit,
    onDragCancel: () -> Unit
) {''',
    '''    onDropConfirm: (TimetableEntry, DropTarget) -> Unit,
    onDragCancel: () -> Unit,
    onEdit: (TimetableEntry) -> Unit = {}
) {'''
)

# 5. Overlap resolution in WeekGridNew
old_layout = '''                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        dayEntries.sortedBy { it.startMinutes }.forEach { entry ->
                            val startOffset = (entry.startMinutes - DAY_START_MINUTES).coerceAtLeast(0)
                            val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                            val top = (startOffset * SLOT_HEIGHT_DP / 60f).dp
                            val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                            val isDragged = entry.sessionId == drag.entry?.sessionId

                            Box(Modifier.fillMaxWidth().absoluteOffset(y = top).height(cardHeight).padding(end = 4.dp, bottom = 2.dp)) {
                                ClassCardNew(
                                    entry = entry,
                                    height = cardHeight,
                                    isDark = isDark,
                                    draggable = true,
                                    isDragged = isDragged,
                                    onClick = { /* TODO: Show edit sheet */ },'''
new_layout = '''                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        val overlaps = mutableListOf<MutableList<TimetableEntry>>()
                        dayEntries.sortedBy { it.startMinutes }.forEach { entry ->
                            val overlappingGroup = overlaps.firstOrNull { group ->
                                group.any { maxOf(it.startMinutes, entry.startMinutes) < minOf(it.endMinutes, entry.endMinutes) }
                            }
                            if (overlappingGroup != null) {
                                overlappingGroup.add(entry)
                            } else {
                                overlaps.add(mutableListOf(entry))
                            }
                        }

                        overlaps.forEach { group ->
                            val cols = group.size
                            group.forEachIndexed { index, entry ->
                                val startOffset = (entry.startMinutes - DAY_START_MINUTES).coerceAtLeast(0)
                                val duration = (entry.endMinutes - entry.startMinutes).coerceAtLeast(45)
                                val top = (startOffset * SLOT_HEIGHT_DP / 60f).dp
                                val cardHeight = (duration * SLOT_HEIGHT_DP / 60f).coerceAtLeast(40f).dp
                                val isDragged = entry.sessionId == drag.entry?.sessionId

                                BoxWithConstraints(
                                    Modifier
                                        .fillMaxWidth(1f / cols)
                                        .absoluteOffset(y = top)
                                        .height(cardHeight)
                                ) {
                                    val w = maxWidth
                                    Box(Modifier.absoluteOffset(x = w * index).padding(end = 4.dp, bottom = 2.dp)) {
                                        ClassCardNew(
                                            entry = entry,
                                            height = cardHeight,
                                            isDark = isDark,
                                            draggable = true,
                                            isDragged = isDragged,
                                            onClick = { onEdit(entry) },'''
content = content.replace(old_layout, new_layout)
content = content.replace(
'''                                        drag.hoveredTarget = null
                                    }
                                )
                            }
                        }
                    }
                }''',
'''                                        drag.hoveredTarget = null
                                    }
                                )
                            }
                        }
                        }
                    }
                }'''
)

# 6. ClassCardNew signature
content = content.replace(
    '''    isDark: Boolean,
    draggable: Boolean = false,
    isDragged: Boolean = false,
    onClick: () -> Unit = {},''',
    '''    isDark: Boolean,
    draggable: Boolean = false,
    isDragged: Boolean = false,
    onClick: () -> Unit = {},''' # Already there?
)

# 7. ClassCardNew alpha
old_alpha = '''    val color = subjectCardColor(entry.subject.colorKey, isDark)
    val baseAlpha = if (entry.isCancelled) 0.45f else 1f
    Surface('''
new_alpha = '''    val color = subjectCardColor(entry.subject.colorKey, isDark)
    val baseAlpha = if (entry.isCancelled) 0.45f else 1f
    val finalAlpha = if (isDragged) 0f else baseAlpha
    Surface('''
content = content.replace(old_alpha, new_alpha)

old_mod = '''        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(height)
            .alpha(baseAlpha)
            .clickable { onClick() }'''
new_mod = '''        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(height)
            .alpha(finalAlpha)
            .clickable { onClick() }'''
content = content.replace(old_mod, new_mod)

with open(r'd:\QuocSchedule\app\src\main\java\com\quoc\schedule\feature\timetable\TimetableScreenNew.kt', 'w', encoding='utf-8') as f:
    f.write(content)
