# QuocSchedule — Android (CLAUDE.md)

> File hướng dẫn cho Claude (Desktop & CLI) khi làm việc trong repo này.
> Kiến trúc đầy đủ: `docs/ARCHITECTURE.md` · Mockup UI: `docs/ui-mockup.html`

## Lệnh build

```bash
./gradlew assembleDebug      # build APK
./gradlew test               # unit tests (normalizer)
./gradlew installDebug       # cài vào thiết bị/emulator
```

Yêu cầu: JDK 17, Android SDK 35. Nếu chưa có wrapper: `gradle wrapper --gradle-version 8.7`.

## Stack

Kotlin 2.0 · Jetpack Compose (Material 3, BOM) · MVVM + Hilt · Room + Flow · ML Kit OCR (on-device) · CameraX · DataStore Preferences · Navigation Compose.

## Cấu trúc package

```
com.quoc.schedule/
├── MainActivity.kt         — NavHost: timetable / exams / stats / settings / import / camera
├── core/model/             — enums + ParsedEntry (kết quả trích xuất, có confidence)
├── core/database/          — Room: Subject, ClassSession, SessionOverride, Exam, IngestionDraft
├── core/data/              — ScheduleRepository (SoT), prefs/UserPrefsRepository (DataStore)
├── core/data/ingestion/    — OcrEngine (ML Kit), DocxParser (zip+regex), ScheduleNormalizer
├── domain/                 — GetWeekTimetableUseCase, DetectConflictUseCase, GetExamCountdownUseCase
├── feature/timetable/      — lưới tuần, kéo–thả + haptic + conflict, báo nghỉ
├── feature/exam/           — exam board + countdown + xóa
├── feature/importflow/     — chọn nguồn → quét → preview → sửa → xác nhận
├── feature/camera/         — CameraX chụp trực tiếp
├── feature/stats/          — giờ học/tuần theo môn
├── feature/settings/       — học kỳ, theme mode
└── ui/theme/               — palette pastel M3, typography, subjectPalette (7 màu)
```

## Quy ước kiến trúc

- **Luồng dữ liệu:** Room emit Flow → Repository → UseCase → ViewModel (StateFlow) → Compose. Không bao giờ gọi DAO trực tiếp từ ViewModel.
- **Ingestion:** ảnh/docx → RawExtraction → ScheduleNormalizer → `List<ParsedEntry>` → IngestionDraft (PENDING) → Preview UI → commitParsedEntries() → Room. Không bao giờ lưu thẳng dữ liệu trích xuất.
- **Lịch gốc vs ngoại lệ:** `ClassSession` lưu lịch lặp theo `weekPattern` ("1-15", "1,3,5"). Nghỉ/học bù lưu ở `SessionOverride` theo ngày cụ thể. Hủy buổi không xóa lịch gốc.
- **Màu card môn:** `colorKey = hash(mã HP) % 7` → index vào `SubjectPalette` (7 màu pastel). Cùng mã luôn cùng màu.
- **Confidence:** ParsedEntry.confidence < 0.7 → UI tô vàng cảnh báo trong Preview.
- **Thời gian trong DB:** lưu dạng `startMinutes`/`endMinutes` (Int, phút từ 00:00) — không dùng java.time trong entity.
- **Ngày:** ISO yyyy-MM-dd dạng String trong DB.

## Quy ước code

- Package theo feature: `feature/<name>/` chứa Screen + ViewModel của feature đó.
- ViewModel dùng `@HiltViewModel` + `inject constructor`. UI state là data class + `MutableStateFlow` + `asStateFlow()`.
- Compose screen nhận callback navigation (`onNavigateToX`) qua params, không tự navigate.
- Unit tests cho logic thuần (normalizer, use cases) — đặt ở `app/src/test/`.
- Không dùng Apache POI — docx parse bằng ZipInputStream + regex trên `word/document.xml`.
- Tiếng Việt cho UI strings, tiếng Anh cho code/comments.

## Trạng thái (v0.2 — 2026-09-14)

Đã xong: toàn bộ MVP + kéo–thả, CameraX, conflict detection, DataStore settings, thống kê.
Còn thiếu: swipe ngang chuyển tuần (HorizontalPager), font Be Vietnam Pro trong res/font, Room migration chuẩn, form nhập thủ công đầy đủ.
