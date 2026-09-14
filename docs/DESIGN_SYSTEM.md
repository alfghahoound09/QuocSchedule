# QuocSchedule Design System

> **Philosophy:** "Học nhẹ nhàng" — Minimalist × Material Design 3 × Soft Pastel  
> Designed for engineering students who need glanceable information in under 3 seconds.

---

## 1. Design Tokens

### Color System

#### Light Mode
```kotlin
object LightColors {
    val background = Color(0xFFF8F9FC)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFF1F5F9)
    val border = Color(0xFFE2E8F0)
    val borderLight = Color(0xFFF1F5F9)
    
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val textTertiary = Color(0xFF94A3B8)
    
    val primary = Color(0xFF3B82F6)
    val primaryVariant = Color(0xFF2563EB)
    val onPrimary = Color(0xFFFFFFFF)
}
```

#### Dark Mode
```kotlin
object DarkColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)
    val border = Color(0xFF334155)
    val borderLight = Color(0xFF475569)
    
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFFCBD5E1)
    val textTertiary = Color(0xFF94A3B8)
    
    val primary = Color(0xFF60A5FA)
    val primaryVariant = Color(0xFF3B82F6)
    val onPrimary = Color(0xFF0F172A)
}
```

#### Subject Pastel Palette (7 colors)
```kotlin
object SubjectPalette {
    val mint = Color(0xFFE8F8F0)        // 薄荷绿
    val mintDark = Color(0xFF10B981)
    
    val sky = Color(0xFFEAF4FD)         // 天空蓝
    val skyDark = Color(0xFF3B82F6)
    
    val lavender = Color(0xFFF3EDFD)    // 薰衣草
    val lavenderDark = Color(0xFF8B5CF6)
    
    val peach = Color(0xFFFEF0EA)       // 蜜桃橙
    val peachDark = Color(0xFFF97316)
    
    val lemon = Color(0xFFFEF9E6)       // 柠檬黄
    val lemonDark = Color(0xFFF59E0B)
    
    val rose = Color(0xFFFEE7F0)        // 玫瑰粉
    val roseDark = Color(0xFFEC4899)
    
    val coral = Color(0xFFFFF1ED)       // 珊瑚红
    val coralDark = Color(0xFFEF4444)
}
```

#### Semantic Colors
```kotlin
object SemanticColors {
    // Live Session (Đang học)
    val liveGreen = Color(0xFF10B981)
    val liveGreenBg = Color(0xFFD1FAE5)
    
    // Warning (Low confidence OCR)
    val warningYellow = Color(0xFFF59E0B)
    val warningYellowBg = Color(0xFFFEF3C7)
    
    // Exam Countdown
    val urgentRed = Color(0xFFEF4444)
    val urgentRedBg = Color(0xFFFEE2E2)
    
    // Conflict
    val conflictOrange = Color(0xFFF97316)
    val conflictOrangeBg = Color(0xFFFFEDD5)
}
```

### Typography (Be Vietnam Pro)

```kotlin
object AppTypography {
    // Display (Hero countdown, screen titles)
    val displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )
    
    val displayMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
    
    // Title (Card headers, section titles)
    val titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )
    
    val titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )
    
    // Body (Subject names, descriptions)
    val bodyLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )
    
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    
    // Label (Time slots, room tags, badges)
    val labelLarge = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )
    
    val labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    
    val labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
}
```

### Spacing Scale

```kotlin
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
}
```

### Border Radius

```kotlin
object Radius {
    val xs = 4.dp      // Small badges
    val sm = 8.dp      // Tags, chips
    val md = 12.dp     // Cards default
    val lg = 16.dp     // Large cards
    val xl = 20.dp     // Hero cards
    val full = 999.dp  // Pills (day selector)
}
```

### Elevation & Shadows

```kotlin
object Elevation {
    val none = 0.dp
    val sm = 2.dp      // Hover states
    val md = 4.dp      // Cards
    val lg = 8.dp      // Floating action button
    val xl = 12.dp     // Modal, bottom sheet
}

// Custom shadows for light/dark mode
val cardShadowLight = listOf(
    Shadow(
        color = Color(0x0A1E293B),
        offset = Offset(0f, 1f),
        blurRadius = 2f
    ),
    Shadow(
        color = Color(0x0F1E293B),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )
)

val cardShadowDark = listOf(
    Shadow(
        color = Color(0x33000000),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )
)
```

---

## 2. Component Library

### 2.1 Smart Schedule Card

**Anatomy:**
```
┌────────────────────────────────────────┐
│ [Live Badge]              [Menu •••]   │  ← Header
│                                        │
│ ██████ 45%                             │  ← Progress bar (live session only)
│                                        │
│ 📘 THỰC TẬP DOANH NGHIỆP              │  ← Subject name (bodyLarge, bold)
│ IT4409                                 │  ← Code (labelMedium, secondary)
│                                        │
│ ⏰ 07:00 – 09:30                       │  ← Time (labelLarge)
│ 📍 TC-205                              │  ← Room tag (chip)
│                                        │
└────────────────────────────────────────┘
```

**States:**
- **Default:** Pastel background, border subtle
- **Live (Đang học):** Green accent border + progress bar + pulse animation
- **Past:** Reduced opacity 60%, strikethrough on time
- **Cancelled:** Red "X" badge, reduced opacity
- **Conflict:** Orange border, warning icon

**Specs:**
- Padding: 16dp
- Corner radius: 16dp
- Min height: 120dp
- Subject emoji size: 20sp
- Progress bar height: 4dp with 8dp radius

### 2.2 Day Strip Carousel

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  [T2]   [T3 •]   [T4]   [T5]   [T6]   [T7]   [CN]     │
│   3      5       2       4       3       0       0      │ ← Session count badges
└─────────────────────────────────────────────────────────┘
```

**Selected State:**
- Background: Primary color with 12% opacity
- Border: 2dp primary solid
- Text: Primary bold
- Badge: Solid primary background

**Unselected State:**
- Background: Surface
- Border: 1dp border color
- Text: Secondary
- Badge: Surface variant with secondary text

**Specs:**
- Pill height: 56dp
- Width: 48dp (portrait), auto on landscape
- Spacing: 8dp between pills
- Badge: 16dp circle, -4dp offset top-right

### 2.3 View Switcher Toggle

```
┌──────────────────────────────┐
│  [📅 Ngày]  │  [📊 Tuần]    │  ← Segmented button
└──────────────────────────────┘
```

**Specs:**
- Total width: 180dp
- Height: 40dp
- Corner radius: 20dp (full pill)
- Selected segment: Primary color fill
- Unselected: Transparent with border

### 2.4 Exam Countdown Hero Card

```
┌─────────────────────────────────────────┐
│                                         │
│         🔥 Sắp thi trong                │  ← displayMedium
│            5 NGÀY                       │  ← displayLarge, gradient text
│                                         │
│         Toán Cao Cấp 2                 │  ← titleLarge
│         MI1142 · 08:00 · TC-208        │  ← labelMedium
│                                         │
└─────────────────────────────────────────┘
```

**Gradient Background:**
- Light mode: Linear gradient from urgentRedBg to surface
- Dark mode: Linear gradient from urgentRed (20% opacity) to surface
- Height: 160dp
- Padding: 24dp

### 2.5 Room Tag Chip

```
┌─────────┐
│ 📍TC-205│  ← Monospace font for room code
└─────────┘
```

**Specs:**
- Height: 28dp
- Padding horizontal: 12dp
- Corner radius: 8dp
- Background: Surface variant
- Border: 1dp borderLight
- Font: labelMedium, medium weight

---

## 3. Screen-specific Layouts

### 3.1 Dashboard — Timeline View (Default Portrait)

**Hierarchy:**
1. **Header (56dp)**
   - Title: "Lịch học" (displayMedium)
   - Right: Settings icon button

2. **Day Strip Carousel (72dp)**
   - Horizontal scroll with snap behavior
   - Current day centered on mount

3. **View Switcher (56dp)**
   - Fixed below day strip
   - Sticky on scroll

4. **Session Timeline (Scrollable)**
   - Time ruler on left (60dp width)
   - Cards on right with time-proportional spacing
   - Empty state: "Không có tiết học hôm nay 🎉"

**Timeline Grid Logic:**
- Each hour = 80dp vertical space
- 07:00 start, 21:00 end (14 hours × 80dp = 1120dp)
- Cards positioned via `offset.y = (startMinutes - 420) * 80 / 60`
- Card height = `(endMinutes - startMinutes) * 80 / 60` (min 64dp)

### 3.2 Dashboard — Week Grid View (Landscape Optimized)

**Columns:** Mon–Sun (7 equal widths)  
**Rows:** 07:00–21:00 (hourly markers)

**Grid Cell:**
- Width: `(screenWidth - 60dp) / 7`
- Height: 80dp per hour
- Cards span multiple rows based on duration
- Overlap handling: Stack horizontally with 4dp offset + reduce width 85%

**Interaction:**
- Long-press card: Drag to new slot (haptic on drop)
- Tap card: Open detail bottom sheet
- Swipe week: HorizontalPager with snap

### 3.3 Exam Screen

**Sections:**
1. **Hero Countdown Card** (160dp height)
2. **Filter Chips** (48dp height, horizontal scroll)
   - "Tất cả" | "Sắp thi" | "Đã qua"
3. **Exam List** (LazyColumn)
   - Each card 104dp height
   - Divider 16dp spacing

**Exam Card Structure:**
```
┌────────────────────────────────────────┐
│ 📝 Toán Cao Cấp 2          [Edit ✏️]  │
│ MI1142                                 │
│                                        │
│ 📅 28/01/2026 · 08:00                 │
│ 📍 TC-208 · 🪪 SBD: 20241234          │
│ 📋 Tự luận                            │
└────────────────────────────────────────┘
```

### 3.4 Import Flow — 3-Step Wizard

**Step 1: Upload Source Selection**
```
┌─────────────────────────────────────┐
│         Nhập lịch học              │
│                                     │
│  ┌────────────────────────────┐   │
│  │  📄  Tải file Word (.docx) │   │  ← Card button
│  └────────────────────────────┘   │
│                                     │
│  ┌────────────────────────────┐   │
│  │  📸  Chụp ảnh lịch         │   │
│  └────────────────────────────┘   │
│                                     │
│  ┌────────────────────────────┐   │
│  │  🖼️  Chọn từ thư viện      │   │
│  └────────────────────────────┘   │
└─────────────────────────────────────┘
```

**Step 2: Processing Screen**
```
┌─────────────────────────────────────┐
│         Đang xử lý...              │
│                                     │
│  ✅ Đọc file thành công            │
│  ⏳ Nhận diện văn bản (OCR)...     │  ← Shimmer animation
│  ⬜ Chuẩn hóa dữ liệu              │
│  ⬜ Kiểm tra xung đột              │
│                                     │
│  [Cancel]                          │
└─────────────────────────────────────┘
```

**Step 3: Review & Edit Screen**
```
┌─────────────────────────────────────┐
│  ← Kiểm tra lịch nhập        [✓]   │
│                                     │
│  Phát hiện 12 môn, 48 tiết học     │
│                                     │
│  ┌────────────────────────────┐   │
│  │ ⚠️ IT4409 - Thực tập DN    │   │  ← Yellow border (low confidence)
│  │ T2 · 07:00-09:30 · TC-3O5  │   │  ← Tap to edit "TC-3O5" → "TC-305"
│  └────────────────────────────┘   │
│                                     │
│  ┌────────────────────────────┐   │
│  │ ✓ MI1142 - Toán Cao Cấp    │   │  ← Green checkmark (high confidence)
│  │ T3 · 13:00-15:30 · D7-305  │   │
│  └────────────────────────────┘   │
│                                     │
│  [Bỏ qua]           [Lưu lịch]     │
└─────────────────────────────────────┘
```

**Edit Dialog for Low-Confidence Fields:**
- Bottom sheet modal (400dp height)
- Show original OCR text + suggested correction
- Manual text input with validation
- Real-time room code format check (TC-\d{3}, D\d-\d{3})

---

## 4. Micro-interactions & Animations

### 4.1 Haptic Feedback Patterns

```kotlin
object HapticPatterns {
    // Day strip selection
    fun onDaySelected() = HapticFeedbackConstants.CLOCK_TICK
    
    // View switcher toggle
    fun onViewSwitch() = HapticFeedbackConstants.CONTEXT_CLICK
    
    // Card drag start
    fun onDragStart() = HapticFeedbackConstants.LONG_PRESS
    
    // Card drop (valid slot)
    fun onDropSuccess() = HapticFeedbackConstants.CONFIRM
    
    // Card drop (conflict detected)
    fun onDropConflict() = HapticFeedbackConstants.REJECT
    
    // Import success
    fun onImportComplete() = HapticFeedbackConstants.GESTURE_END
}
```

### 4.2 Animations Spec

**Card Entrance (LazyColumn items):**
```kotlin
AnimationSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)
// Slide up + fade in, stagger 50ms per item
```

**Live Session Progress Bar:**
```kotlin
// Indeterminate shimmer + determinate fill
animateFloatAsState(
    targetValue = (currentTime - startTime) / (endTime - startTime),
    animationSpec = tween(durationMillis = 500, easing = EaseInOut)
)
```

**View Switcher Toggle:**
```kotlin
// Slide indicator + crossfade content
AnimatedContent(
    targetState = viewMode,
    transitionSpec = {
        slideInHorizontally { it } + fadeIn() with
        slideOutHorizontally { -it } + fadeOut()
    }
)
```

**Conflict Shake Animation:**
```kotlin
// When drag-drop detects overlap
val offsetX by animateFloatAsState(
    targetValue = if (hasConflict) 8f else 0f,
    animationSpec = keyframes {
        durationMillis = 400
        0f at 0
        8f at 100
        -8f at 200
        4f at 300
        0f at 400
    }
)
```

### 4.3 Gesture Interactions

**Week Grid HorizontalPager:**
```kotlin
HorizontalPager(
    state = pagerState,
    beyondBoundsPageCount = 1, // Preload adjacent weeks
    flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
)
```

**Card Drag & Drop (Timetable Edit):**
```kotlin
Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { hapticFeedback.performHapticFeedback(LONG_PRESS) },
        onDrag = { change, dragAmount ->
            // Visual lift: elevation 8dp, scale 1.05
            // Shadow follows cursor
        },
        onDragEnd = {
            // Calculate target slot
            if (hasConflict) {
                hapticFeedback.performHapticFeedback(REJECT)
                // Snap back to origin with spring
            } else {
                hapticFeedback.performHapticFeedback(CONFIRM)
                // Commit position change to ViewModel
            }
        }
    )
}
```

**Pull-to-Refresh (Dashboard):**
```kotlin
// Material 3 PullRefreshIndicator with custom color
val refreshing by viewModel.isRefreshing.collectAsState()
SwipeRefresh(
    state = rememberSwipeRefreshState(refreshing),
    onRefresh = { viewModel.syncSchedule() },
    indicator = { state, trigger ->
        PullRefreshIndicator(
            state = state,
            refreshTriggerDistance = trigger,
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
)
```

---

## 5. Responsive Breakpoints

### Portrait Phone (Width < 600dp)
- **Default:** Timeline view (single day focus)
- Week grid: Horizontal scroll with snap (show 2 days at a time)
- Navigation: Bottom nav bar (4 tabs)

### Landscape Phone / Small Tablet (600dp–839dp)
- **Default:** Week grid (full week visible)
- Timetable: Side-by-side day strip + grid
- Navigation: Rail navigation (left side)

### Tablet (840dp+)
- **Default:** Dual-pane layout (day list left, detail right)
- Week grid: Show 2 weeks simultaneously
- Navigation: Permanent drawer

---

## 6. Accessibility (WCAG AA)

### Color Contrast
- Text primary on background: 4.5:1 minimum (AA)
- Text secondary: 3:1 minimum (AA for large text)
- Pastel card backgrounds: Ensure text contrast by using dark mode overlays

### Touch Targets
- Minimum 48×48dp for all interactive elements
- Day strip pills: 48×56dp
- Card actions (menu): 48×48dp tap area

### Screen Reader Support
```kotlin
Modifier.semantics {
    contentDescription = "Lịch học thứ Ba, 5 tiết: Toán Cao Cấp 07:00, ..."
    role = Role.Button
}
```

### Focus Order
- Logical tab order: Top to bottom, left to right
- Day strip: Horizontal navigation with arrow keys
- Cards: Up/Down navigation in timeline

---

## 7. Implementation Checklist

### Phase 1: Design Tokens Migration
- [ ] Update Color.kt with new palette
- [ ] Add Be Vietnam Pro font to res/font/
- [ ] Define typography scale in Type.kt
- [ ] Create spacing/radius dimension resources

### Phase 2: Component Library
- [ ] SmartScheduleCard composable
- [ ] DayStripCarousel with badge overlay
- [ ] ViewSwitcherToggle segmented button
- [ ] ExamCountdownHero gradient card
- [ ] RoomTagChip with validation

### Phase 3: Screen Refactoring
- [ ] TimetableScreen: Add Timeline/Grid switcher
- [ ] TimetableScreen: Implement HorizontalPager for weeks
- [ ] ExamScreen: Add hero countdown + filter chips
- [ ] ImportFlowScreen: 3-step wizard with review UX
- [ ] Add haptic feedback to all interactions

### Phase 4: Polish
- [ ] Implement all micro-animations
- [ ] Add drag-and-drop with conflict detection
- [ ] Accessibility audit with TalkBack
- [ ] Dark mode validation (all colors tested)
- [ ] Performance: LazyColumn key stability check

---

**Version:** 1.0.0  
**Last Updated:** 2026-09-14  
**Figma Link:** [Insert Figma prototype URL]  
**Design Review:** Pending stakeholder approval
