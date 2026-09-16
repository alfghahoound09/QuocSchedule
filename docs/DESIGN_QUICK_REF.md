# QuocSchedule Design System — Quick Reference

**v0.2.1 | September 2026 | Material Design 3 + Deep Dark Mode**

---

## Color Quick Reference

### Dark Palette
```
Background:  #0F172A    Surfaces:    #1E293B    Variant:     #334155
Text:        #F1F5F9    Secondary:   #CBD5E1    Tertiary:    #94A3B8
Primary:     #60A5FA    Accent:      #34D399    Error:       #EF4444
```

### Subject Colors (7 palette)
| # | Name | Accent | Light Bg |
|---|------|--------|----------|
| 1 | Mint | #10B981 | #E8F8F0 |
| 2 | Sky | #3B82F6 | #EAF4FD |
| 3 | Lavender | #8B5CF6 | #F3EDFD |
| 4 | Peach | #F97316 | #FEF0EA |
| 5 | Lemon | #F59E0B | #FEF9E6 |
| 6 | Rose | #EC4899 | #FEE7F0 |
| 7 | Coral | #EF4444 | #FFF1ED |

### Semantic Badges
- **Room:** Amber Yellow (#F59E0B) badge with 📍 icon
- **SBD:** Rose Pink (#EC4899) badge with 🏷 icon
- **Live:** Emerald (#10B981) with pulse animation
- **Time:** Red (#EF4444) line + dot
- **Today:** Emerald (#34D399) background
- **Conflict:** Orange (#F97316) border

---

## Component Patterns

### ClassCard (Timetable)
```
┌─────────────────────┐
│ ■ IT101         [bù]│  ← Subject code, bold. Left stripe = subject color
│ Lập trình           │
│ (auto-wrap)         │
│                     │
│ 📍 A101 ✨          │  ← Room in Amber Yellow, bold, at bottom
└─────────────────────┘
```

### ExamCard (Exam)
```
┌──────────────────────────────────┐
│ Giải tích 2          [Cuối kỳ]  │  ← Subject bold, type chip
│ 📆 19/09/2026 · ⏰ 14:00        │
│                                  │
│ 📍 [A205]  🏷 [SBD: 001234] 🗑  │  ← Badges + delete button
└──────────────────────────────────┘
```

### SubjectLoadBar (Stats)
```
┌────────────────────────────────┐
│ ■ Cấu trúc dữ liệu    4.5h     │  ← Color dot + name (wraps)
│   và giải thuật      3 buổi     │     Hours + sessions on right
│ ▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░   │  ← Responsive progress bar
└────────────────────────────────┘
```

---

## Typography Hierarchy

| Level | Size | Weight | Use Case |
|-------|------|--------|----------|
| Display | 2.5em | Bold | Screen title (rare) |
| Headline | 1.8em | Bold | Section headers |
| Title | 1.2em | Bold | Card titles, subject names |
| Body | 0.95em | Regular | Description text |
| Label | 0.9em | Medium | Badges, secondary info |
| Caption | 0.85em | Regular | Smallest text (room, time) |

---

## Key Functions

### Get Subject Color
```kotlin
// Returns (background, accent) pair based on subject code hash
val (bgColor, accentColor) = getSubjectColor(subjectCode, isDarkTheme = true)

// Use in card background + border stripe
Box(
    Modifier.background(bgColor)
        .border(1.dp, accentColor)
)
```

### Format Time
```kotlin
// Convert minutes (0-1440) to HH:MM
fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}
```

### Check Live Status
```kotlin
// Returns (isLive, progress) where progress is 0.0-1.0
val (isLive, progress) = calculateLiveStatus(session, currentTime)

// Use for live badge + progress bar
if (isLive) {
    LiveBadge(alpha = pulseAlpha)
    LinearProgressIndicator(progress = { progress })
}
```

---

## Do's & Don'ts

### ✅ DO
- Use `getSubjectColor()` for all subject-related backgrounds
- Place room in Amber Yellow (#F59E0B) badge with 📍
- Highlight today with Emerald Green (#34D399)
- Use red (#EF4444) only for current time line + errors
- Keep text white (#F1F5F9) on dark backgrounds
- Wrap long names (e.g., subject names) to 3 lines max

### ❌ DON'T
- Use gray text for room (must be Amber Yellow)
- Mix subject colors (same code = same color always)
- Truncate room numbers (too important)
- Use pure black or pure white
- Add decorative colors (only semantic use)
- Forget haptic feedback on important actions

---

## Accessibility Checklist

- [ ] Text contrast ≥ 4.5:1 (WCAG AA)
- [ ] Color + icon/text (not color alone)
- [ ] Touch targets ≥ 48dp
- [ ] Text size ≥ 12sp
- [ ] No pure red-on-green (colorblind safe)
- [ ] Semantic HTML structure (via Compose)

---

## State Indicators

| State | Icon | Color | Badge |
|-------|------|-------|-------|
| Live | 🔴 | Green | "Đang học" with pulse |
| Cancelled | ✗ | Red | "Đã hủy" |
| Conflict | ⚠️ | Orange | "Trùng lịch" |
| Past Exam | - | Gray | "Đã qua" |
| Makeup | bù | Gray | Small label |

---

## Layout Spacing (Material Design 3)

```
xs = 4dp      sm = 8dp      md = 12dp
lg = 16dp     xl = 24dp     xxl = 32dp

Card padding: 16dp
Row spacing: 8-12dp
Column spacing: 12dp
```

---

## Animation Timing

| Component | Duration | Easing |
|-----------|----------|--------|
| Live badge pulse | 1000ms | Linear |
| Card entrance | 300ms | EaseInOutCubic |
| Time line update | 1000ms (once per min) | None |
| Theme switch | 200ms | EaseInOut |

---

## Quick Wins for Future

1. **Light Mode:** Use light backgrounds, dark text
2. **Animations:** Fade-in + slide for cards
3. **Widgets:** Home screen schedule preview
4. **Shortcuts:** "Today" + "Next Exam" quick actions
5. **Google Sync:** Two-way Calendar integration

---

## Common Questions

**Q: Can I change the subject color palette?**  
A: No, same code = same color always (deterministic hash). Change via `SubjectPalette` in `Color.kt`.

**Q: Why is room in Amber, not red?**  
A: Red is reserved for errors/current time. Amber provides high contrast without confusion.

**Q: How many colors in the subject palette?**  
A: Exactly 7. Cycles after: `hash % 7`. Ensures even distribution.

**Q: Can I use green for something other than "today"?**  
A: Only Emerald (#34D399) for today + live sessions. Use `SemanticColors` for other status.

**Q: Why deep dark (#0F172A) instead of pure black?**  
A: Reduces eye strain and OLED burn-in. Pure black (#000000) is harsher on eyes.

---

## Resources

- **Mockups:** `docs/ui-design-system.html` (open in browser)
- **Full Guide:** `docs/DESIGN_IMPLEMENTATION.md`
- **Architecture:** `docs/ARCHITECTURE.md`
- **Build:** `CLAUDE.md`

---

**Last Updated:** September 16, 2026  
**Status:** Ready for production 🚀
