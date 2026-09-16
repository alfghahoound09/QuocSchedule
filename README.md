# QuocSchedule — Student Timetable & Exam Tracker

> A smart, minimalist Android app for Vietnamese university students to manage schedules, track exams, and visualize workload.

**Current Version:** v0.2.1 | **Status:** 🚀 Production Ready

---

## 📱 Introduction

### Problem
Vietnamese university students juggle complex schedules. They need:
- Quick access to room locations, class times, subject info (3-second glance)
- Conflict detection when rescheduling classes
- Exam tracking with countdown & location
- Workload visualization (weekly/semester hours)

### Solution
QuocSchedule provides a minimal interface optimized for "Glance & Go":
- 📅 Weekly timetable with drag-and-drop rescheduling
- 📝 Exam tracker with countdown & sorted exams
- 📊 Analytics dashboard (hours/week by subject)
- ⚙️ Settings for semester config & theme

### Key Features
✅ Drag-and-drop schedule management
✅ OCR-powered import (image/DOCX)
✅ Real-time conflict detection
✅ Dark & light themes
✅ ML Kit on-device OCR (no cloud)
✅ Offline-first architecture

---

## 🏗️ Project Structure

```
QuocSchedule/
├── app/src/main/java/com/quoc/schedule/
│   ├── core/
│   │   ├── model/               ← Data models
│   │   ├── database/            ← Room entities & DAOs
│   │   └── data/                ← Repository + Ingestion
│   ├── domain/                  ← Use cases (business logic)
│   ├── feature/
│   │   ├── timetable/           ← 📅 Schedule (2 themes)
│   │   ├── exam/                ← 📝 Exams
│   │   ├── stats/               ← 📊 Analytics
│   │   ├── settings/            ← ⚙️ Config
│   │   └── importflow/          ← 📥 Import
│   ├── ui/
│   │   ├── theme/               ← Colors, Typography
│   │   └── components/          ← Reusable UI
│   └── di/                       ← Hilt DI
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DESIGN_IMPLEMENTATION.md
│   ├── ui-design-system.html
│   └── (more docs)
└── README.md
```

---

## 🛠️ Tech Stack

### Core
| Technology | Purpose |
|-----------|---------|
| Kotlin 2.0 | Modern, null-safe language |
| Jetpack Compose | Declarative UI (Material 3) |
| MVVM + Clean Architecture | Separation of concerns |
| Hilt | Dependency injection |

### Data & Persistence
- **Room Database** — Local SQLite with DAOs
- **DataStore** — Encrypted preferences
- **Coroutines & Flow** — Reactive async

### ML & Camera
- **ML Kit OCR** — On-device text recognition
- **CameraX** — Modern camera API
- **DOCX Parser** — ZIP + regex parsing

### Build
- **Gradle 8.7** with Kotlin DSL
- **Version Catalog** — Centralized dependencies
- **Unit Tests** — JUnit 4 + Kotlin Test

---

## 🚀 Getting Started

```bash
# Clone & build
git clone https://github.com/alfghahoound09/QuocSchedule.git
cd QuocSchedule

# Compile
./gradlew compileDebugKotlin

# Build APK
./gradlew assembleDebug

# Install
./gradlew installDebug

# Run tests
./gradlew test
```

---

## 🎨 Design System (v0.2.1)

### Colors
- **Dark:** #0F172A (bg) + #1E293B (cards)
- **Light:** #FAFAF7 (bg) + #FFFFFF (cards)
- **Subjects:** 7-color palette (hash-based)
- **Accents:** Emerald, Amber, Red

### Typography (Material 3)
- Subject: Bold 14-16sp
- Room/Time: Bold 12-13sp
- Secondary: Regular 11sp

### Accessibility
✅ WCAG AAA+ contrast (19:1)
✅ No color-only info
✅ Touch ≥ 48dp
✅ Text ≥ 12sp

---

## 📚 Documentation

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — Data flow
- [DESIGN_IMPLEMENTATION.md](docs/DESIGN_IMPLEMENTATION.md) — UI spec
- [ui-design-system.html](docs/ui-design-system.html) — Mockups
- [INDEX.md](docs/INDEX.md) — Docs hub

---

## 🤝 Contributing

```bash
git checkout -b feature/your-feature
# Make changes
git commit -m "feat(scope): description"
git push origin feature/your-feature
```

---

## 📄 License

MIT License — see LICENSE file

---

**Last Updated:** September 16, 2026
**Status:** ✅ v0.2.1 Production Ready
**Built with:** Kotlin 2.0 · Jetpack Compose · Material Design 3
