# 📚 QuocSchedule — Phác thảo Kiến trúc, Luồng Tính năng & Thiết kế UI/UX

> Ứng dụng Android quản lý lịch học & lịch thi cho sinh viên đại học
> Phiên bản tài liệu: 1.0 — 2026-09-14

---

## Mục lục

1. [Tổng quan sản phẩm](#1-tổng-quan-sản-phẩm)
2. [Kiến trúc ứng dụng](#2-kiến-trúc-ứng-dụng)
3. [Luồng tính năng chi tiết](#3-luồng-tính-năng-chi-tiết)
4. [Mô hình dữ liệu (Room Database)](#4-mô-hình-dữ-liệu-room-database)
5. [Design System & UI/UX](#5-design-system--uiux)
6. [Luồng người dùng (User Flow)](#6-luồng-người-dùng-user-flow)
7. [Technical Stack & Thư viện](#7-technical-stack--thư-viện)
8. [Roadmap triển khai (gợi ý)](#8-roadmap-triển-khai-gợi-ý)

---

## 1. Tổng quan sản phẩm

**Vấn đề:** Sinh viên phải tự nhập tay lịch học/lịch thi từ ảnh chụp hoặc file Word của khoa — mất thời gian, dễ sai, dễ quên.

**Giải pháp:** App nhận đầu vào là ảnh (JPG/PNG) hoặc file `.docx`, tự động trích xuất lịch bằng OCR + table parsing, cho sinh viên kiểm tra trước khi lưu, sau đó hiển thị thời khóa biểu và bảng lịch thi với countdown.

**Nguyên tắc thiết kế xuyên suốt:**

- *Nhẹ nhàng* — gam pastel, nhiều whitespace, giảm áp lực học tập.
- *Mượt mà* — mọi tương tác có phản hồi ngay (Room + Flow), micro-interactions tinh tế.
- *Tin cậy* — dữ liệu OCR luôn đi qua bước Preview/Confirm, không bao giờ lưu thẳng.

---

## 2. Kiến trúc ứng dụng

### 2.1. Kiến trúc tổng thể: MVVM + Clean Architecture (3 tầng)

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  Jetpack Compose UI  ·  ViewModel  ·  UI State (StateFlow)  │
│                                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │Timetable │ │ExamBoard │ │  Import  │ │    Settings   │  │
│  │  Screen  │ │  Screen  │ │  Flow    │ │    Screen     │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────┬────────┘  │
│       └─────────────┴────────────┴───────────────┘          │
│                        ViewModels                            │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                            │
│  Use Cases (tính toán tuần, countdown, mapping OCR, validate)│
│                                                              │
│  · ParseScheduleUseCase      · DetectConflictUseCase        │
│  · ExtractFromImageUseCase   · ExtractFromDocxUseCase       │
│  · GetWeekTimetableUseCase   · GetExamCountdownUseCase      │
│  · AddMakeupClassUseCase     · CancelClassUseCase           │
├─────────────────────────────────────────────────────────────┤
│                       DATA LAYER                             │
│                                                              │
│  ┌──────────────┐  ┌───────────────────┐  ┌──────────────┐ │
│  │ Room Database│  │  Ingestion Engine │  │ Preferences  │ │
│  │ (SQLite)     │  │ ML Kit OCR + POI  │  │ DataStore    │ │
│  └──────────────┘  └───────────────────┘  └──────────────┘ │
│       Repositories (Offline-first, Single Source of Truth)   │
└─────────────────────────────────────────────────────────────┘
```

**Vì sao MVVM thay vì MVI:** MVVM quen thuộc với Compose (`viewModel()` + `collectAsStateWithLifecycle()`), ít boilerplate hơn MVI, đủ sức quản lý state cho app quy mô này. Nếu Import Flow trở nên phức tạp (nhiều bước, nhiều trạng thái), có thể áp dụng MVI *cục bộ* cho riêng feature đó bằng một `Intent → Reducer → State` đơn giản.

### 2.2. Phân chia module Gradle (đa module nhẹ)

```
:app                  — điểm vào, navigation, DI graph
:core:ui              — design system, theme, composable dùng chung
:core:database        — Room entities, DAOs, migrations
:core:model           — domain models, enums
:feature:timetable    — thời khóa biểu tuần/tháng
:feature:exam         — bảng lịch thi + countdown
:feature:ingestion    — OCR / docx parsing / preview & confirm
:feature:settings     — theme, học kỳ, giờ tiết học cấu hình
```

Phân module theo feature giúp build nhanh hơn và giữ `:feature:ingestion` (nặng, chứa ML Kit + Apache POI) tách biệt khỏi UI.

### 2.3. Dependency Injection & luồng dữ liệu

- **DI:** Hilt.
- **Luồng dữ liệu:** Room phát `Flow` → Repository → UseCase → ViewModel expose `StateFlow<UiState>` → Compose recompose tự động. Người dùng sửa lịch → ghi vào Room → UI cập nhật *ngay lập tức* mà không cần refresh thủ công.
- **Background work:** WorkManager cho các tác vụ nặng (OCR ảnh lớn, parse docx) để không chặn UI và sống sót qua việc app bị đóng.

---

## 3. Luồng tính năng chi tiết

### 3.1. Smart Data Ingestion (Trích xuất thông minh)

```
[Ảnh JPG/PNG] ──► ML Kit Text Recognition ──► Raw text blocks
                                                    │
[File .docx]  ──► Apache POI (XWPF) ──► Table/Paragraph extract
                                                    │
                                                    ▼
                                        ┌───────────────────────┐
                                        │  Normalization Layer  │
                                        │  · Regex tiếng Việt   │
                                        │    cho ngày/giờ       │
                                        │  · Fuzzy match tên    │
                                        │    thứ (T2..CN)       │
                                        │  · Tách "7h30-9h30"   │
                                        └───────────┬───────────┘
                                                    ▼
                                        ┌───────────────────────┐
                                        │   Structured Draft    │
                                        │  Subject, Code, Time, │
                                        │  Room, Lecturer, Type │
                                        └───────────┬───────────┘
                                                    ▼
                                        ┌───────────────────────┐
                                        │  Preview & Confirm    │
                                        │  (user chỉnh sửa lỗi) │
                                        └───────────┬───────────┘
                                                    ▼
                                              Room Database
```

**Các quy tắc mapping dữ liệu thô → trường có cấu trúc:**

| Trường đích | Chiến lược nhận diện |
|---|---|
| Mã học phần | Regex dạng `[A-Z]{2,4}\d{3,4}` (vd: `IT4409`), ưu tiên cột "Mã HP" nếu docx có header |
| Tên môn | Chuỗi dài nhất trong ô/cùng block với mã HP; loại bỏ tiền tố thừa |
| Thời gian | Regex giờ `HH:mm`, dải tiết (Tiết 1-3), ngày `dd/MM/yyyy`; quy ước cấu hình tiết→giờ trong Settings |
| Phòng học | Pattern `P.\d+`, `TC-\d+`, `B1-\d+`, hoặc chuỗi sau nhãn "Phòng:" |
| Giảng viên | Chuỗi sau nhãn "GV:", "CBGD:", hoặc dòng có dấu `(` dưới tên môn |
| Loại lịch | Keyword "thi", "kiểm tra", "giữa kỳ", "cuối kỳ" → đẩy sang Exam; còn lại → Class |

**Xử lý trường hợp xấu:** mỗi trường trích xuất kèm `confidence` (0..1). Trường có confidence < 0.7 được tô màu cảnh báo trong màn Preview để người dùng ưu tiên kiểm tra.

### 3.2. Interactive Timetable (Thời khóa biểu)

- **Dạng tuần (mặc định):** lưới 7 cột (T2–CN) × dải giờ cấu hình; mỗi môn là một card pastel có màu riêng theo mã HP (hash màu ổn định).
- **Dạng tháng:** lịch tháng với dot indicator số môn/ngày; chạm vào ngày → bottom sheet liệt kê các buổi học.
- **Modify toàn quyền:**
  - Thêm thủ công (form có gợi ý tên môn đã có).
  - Sửa chi tiết: đổi phòng, đổi giờ, đổi giảng viên.
  - Xóa một buổi hoặc cả môn (có xác nhận).
  - **Lịch học bù / nghỉ đột xuất:** chọn buổi học → "Học bù" tạo bản ghi override gắn với buổi gốc (giữ lịch sử), "Báo nghỉ" ẩn buổi đó khỏi tuần tương ứng nhưng không xóa dữ liệu gốc.
- **Kéo–thả (stretch goal):** kéo card môn sang ô giờ khác để đổi lịch, kèm haptic feedback rung nhẹ; nếu trùng giờ môn khác → hiện cảnh báo conflict (DetectConflictUseCase).

### 3.3. Exam Board (Bảng lịch thi)

- Module độc lập với Timetable (tab riêng, entity riêng) để không trộn lẫn.
- Mỗi kỳ thi là một **card nổi bật**: màu đậm hơn pastel thường, icon riêng, hiển thị ngày thi, giờ thi, phòng thi, **số báo danh**.
- **Countdown:** header của tab Exam hiển thị "Còn **X ngày** đến môn thi tiếp theo: *Tên môn*"; mỗi card cũng có chip đếm ngược riêng. Countdown tính realtime qua `GetExamCountdownUseCase` (tick theo ngày, không cần giây).
- Sắp xếp theo thời gian gần nhất; môn đã thi tự chuyển xuống nhóm "Đã qua" mờ đi.
- Kỳ thi sắp đến (≤ 3 ngày) cũng được gợi ý hiển thị dạng banner nhỏ ngay trên Timetable để nhắc nhẹ.

---

## 4. Mô hình dữ liệu (Room Database)

```kotlin
@Entity
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,            // Mã học phần, vd "IT4409"
    val name: String,            // Tên môn
    val lecturer: String?,       // Giảng viên
    val colorKey: Int,           // index màu pastel ổn định
    val semesterId: Long         // thuộc học kỳ nào
)

@Entity(foreignKeys = [ForeignKey(Subject::class, ["id"], ["subjectId"], onDelete = CASCADE)])
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val dayOfWeek: Int,          // 2..8 (T2..CN)
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String?,
    val weekPattern: String      // "1-15" hoặc "1,3,5" (tuần học)
)

@Entity
data class SessionOverride(      // học bù / báo nghỉ
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val originalDate: LocalDate,
    val type: OverrideType,      // CANCELLED | RESCHEDULED
    val newDate: LocalDate?,     // chỉ dùng khi RESCHEDULED
    val newStartTime: LocalTime?,
    val newRoom: String?
)

@Entity
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val examDate: LocalDate,
    val startTime: LocalTime,
    val room: String?,
    val candidateId: String?,    // số báo danh
    val examType: ExamType       // MIDTERM | FINAL | RETAKE
)

@Entity
data class IngestionDraft(       // dữ liệu OCR chờ xác nhận
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceType: SourceType,  // IMAGE | DOCX | MANUAL
    val rawPayload: String,      // text thô để audit
    val createdAt: Instant,
    val status: DraftStatus      // PENDING | CONFIRMED | DISCARDED
)
```

**Nguyên tắc:** `ClassSession` lưu lịch *lặp theo tuần* (weekPattern), `SessionOverride` lưu *ngoại lệ theo ngày cụ thể* — khi render một tuần, UseCase kết hợp cả hai. Như vậy việc nghỉ/học bù không phá vỡ lịch gốc.

---

## 5. Design System & UI/UX

### 5.1. Concept

**"Học nhẹ nhàng"** — Minimalism + Material Design 3 (Material You). Bố cục thoáng, thẻ bo tròn lớn (16–20dp), không đường viền nặng, dùng elevation nhẹ và khoảng trắng để phân tách.

### 5.2. Bảng màu Pastel

| Token | Light | Dark (giảm sáng, giữ hue) | Dùng cho |
|---|---|---|---|
| Primary | `#7FB3D9` (xanh dương nhạt) | `#5B8FB0` | Nút chính, FAB, tab active |
| Secondary | `#A8D8C8` (xanh mint) | `#6FA892` | Tag, chip, accent phụ |
| Background | `#FAFAF7` (trắng kem) | `#1C1E20` | Nền màn hình |
| Surface | `#FFFFFF` | `#26292C` | Card |
| Beige accent | `#F2E8D5` | `#4A4238` | Card nền phụ, highlight |
| Exam highlight | `#F5B8B1` (hồng đào nhạt) | `#8C5652` | Card thi, countdown |
| On-colors | `#2E3A45` chữ chính | `#E8E6E1` | Text |

Màu card môn học: palette 8 màu pastel bổ sung (mint, xanh, be, hồng, tím nhạt...) gán theo `colorKey` hash từ mã HP để mỗi môn một màu ổn định, dễ quét mắt.

### 5.3. Typography

- Font: **Be Vietnam Pro** (hỗ trợ tiếng Việt tốt, hiện đại) — fallback Roboto.
- Thang chữ: Display cho countdown (28–32sp), Title cho tên môn trên card (15–16sp, medium), Body cho chi tiết (13sp), Label cho chip/room (11–12sp).

### 5.4. Micro-interactions & Motion

- **Import/OCR:** loading dạng shimmer kết hợp progress steps ("Đang đọc ảnh → Đang nhận diện bảng → Sắp xong"), không dùng spinner trần.
- **Kéo–thả môn học:** card nâng elevation + scale 1.02, rung nhẹ (haptic `LongPressHandle`) khi nhấc, rung ngắn khi thả hợp lệ; rung "từ chối" kép nhẹ khi thả vào ô conflict.
- **Swipe:** `HorizontalPager` chuyển tuần và chuyển tab Lịch học ↔ Lịch thi với hiệu ứng parallax nhẹ; indicator tuần hiện tại co giãn mượt.
- **Lưu thành công:** snackbar bo tròn + icon check animate (Lottie nhỏ, ~600ms).
- Tất cả animation tuân thủ `spring()` với damping vừa phải — không bounce quá mức, giữ cảm giác "nhẹ".

### 5.5. Accessibility & Dark mode

- Light/Dark tự động theo hệ thống + toggle thủ công trong Settings (DataStore).
- Độ tương phản chữ/nền ≥ WCAG AA (4.5:1) trên cả hai theme; màu pastel chỉ dùng làm nền card, chữ luôn đậm đủ.
- Hỗ trợ TalkBack: mỗi card môn có contentDescription đầy đủ ("Toán cao cấp, thứ 2, 7h30 đến 9h30, phòng B1-203").

---

## 6. Luồng người dùng (User Flow)

```
┌─────────────┐
│  Màn chính  │  (Timetable tuần hiện tại, tab Lịch học / Lịch thi)
│  Timetable  │
└──────┬──────┘
       │ bấm FAB "+"
       ▼
┌─────────────────────────┐
│ Chọn phương thức nhập   │  Bottom sheet:
│ · Tải file .docx        │  3 lựa chọn dạng card lớn
│ · Tải ảnh JPG/PNG       │  + "Nhập thủ công"
│ · Chụp ảnh trực tiếp    │
└──────────┬──────────────┘
           ▼
┌─────────────────────────┐
│ Hệ thống quét           │  OCR (ML Kit) / Parse docx (POI)
│ Loading + progress      │  chạy trên WorkManager/IO
└──────────┬──────────────┘
           ▼
┌─────────────────────────┐
│ Preview dữ liệu trích   │  Danh sách môn đã mapping;
│ xuất (bản nháp)         │  trường confidence thấp tô
│ ⚠ đánh dấu lỗi nghi ngờ │  màu cảnh báo
└──────────┬──────────────┘
           │ người dùng sửa lỗi nhận diện (nếu có)
           ▼
┌─────────────────────────┐
│ Xác nhận lưu            │  Ghi Room (Subject, ClassSession
└──────────┬──────────────┘  hoặc Exam) trong 1 transaction
           ▼
┌─────────────────────────┐
│ Timetable tự cập nhật   │  Flow emit → recompose;
│ + snackbar "Đã thêm X   │  không cần refresh
│   môn học"              │
└─────────────────────────┘
```

**Nhánh phụ:**

- OCR trả về 0 môn → màn "Không đọc được" gợi ý: chụp lại ảnh nét hơn, cắt vùng bảng, hoặc nhập thủ công.
- Phát hiện trùng lịch với môn đã có → dialog conflict cho chọn: giữ cái cũ / thay thế / giữ cả hai.
- Nhập tay: form tối giản (tên môn, thứ, giờ bắt đầu/kết thúc, phòng) với cùng một đường Preview trước khi lưu.

---

## 7. Technical Stack & Thư viện

| Lĩnh vực | Lựa chọn | Lý do |
|---|---|---|
| Ngôn ngữ / UI | Kotlin + **Jetpack Compose** (Material 3) | UI mượt, animation `animateDpAsState`/`spring` gọn, dễ scale |
| Kiến trúc | MVVM, Repository, UseCase; Hilt (DI) | Chuẩn Android hiện nay, test tốt |
| Local DB | **Room** + Coroutines/Flow | Phản hồi UI tức thì, migration an toàn |
| OCR | **Google ML Kit Text Recognition v2** (on-device) | Miễn phí, chạy offline, nhận diện tiếng Việt tốt |
| Đọc .docx | **Apache POI (poi-ooxml-lite)** | Parse bảng XWPF đầy đủ trên Android |
| Background | WorkManager | Quét file/ảnh nặng không chặn UI |
| Ảnh/Camera | CameraX + Photo Picker (`ActivityResultContracts`) | Chụp trực tiếp & chọn ảnh chuẩn Android 13+ |
| Settings | Preferences DataStore | Theme, giờ tiết học, học kỳ hiện tại |
| Async | Kotlin Coroutines + Flow | Toàn bộ pipeline reactive |
| Test | JUnit, Turbine (Flow), Room test, Compose UI Test | |

**Lưu ý kỹ thuật:**

- ML Kit chạy on-device → không cần mạng, bảo mật dữ liệu sinh viên; vẫn nên có fallback "nhập thủ công" luôn hiển thị.
- Apache POI bản full nặng và đụng hạn mức method count — dùng `poi-ooxml-lite` (POI 5.2.3+) để tránh multidex phức tạp.
- Parse docx và OCR đều chạy trong `Dispatchers.Default` qua WorkManager; kết quả ghi vào bảng `IngestionDraft` trước, UI Preview đọc từ draft — mất app giữa chừng vẫn khôi phục được.

---

## 8. Roadmap triển khai (gợi ý)

**Sprint 1 — Nền tảng:** setup project đa module, design system (theme pastel, typography, dark mode), Room schema + migration, navigation + 2 tab chính.

**Sprint 2 — Timetable:** render tuần từ Room, nhập thủ công, sửa/xóa, chuyển tuần bằng swipe.

**Sprint 3 — Ingestion:** tích hợp ML Kit OCR + POI, normalization/mapping, màn Preview & Confirm với confidence highlight.

**Sprint 4 — Exam Board:** entity Exam, UI card nổi bật, countdown, banner nhắc thi trên Timetable.

**Sprint 5 — Polish:** học bù/báo nghỉ (SessionOverride), kéo–thả + haptics, conflict detection, accessibility pass, dark mode review.

---

*Tài liệu này là bản phác thảo khởi đầu — các quyết định chi tiết (tên package, API surface) sẽ được chốt khi bắt đầu code.*
