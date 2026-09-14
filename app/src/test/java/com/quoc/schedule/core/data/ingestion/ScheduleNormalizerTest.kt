package com.quoc.schedule.core.data.ingestion

import com.quoc.schedule.core.model.EntryKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleNormalizerTest {

    private val normalizer = ScheduleNormalizer()

    // ── parseTimeRange ──

    @Test
    fun `parseTimeRange - dang HHmm-HHmm`() {
        val (s, e) = normalizer.parseTimeRange("Học 7:30-9:30 thứ 2")
        assertEquals("07:30", s)
        assertEquals("09:30", e)
    }

    @Test
    fun `parseTimeRange - dang 7h30-9h30`() {
        val (s, e) = normalizer.parseTimeRange("7h30-9h30")
        assertEquals("07:30", s)
        assertEquals("09:30", e)
    }

    // ── parseDayOfWeek / parseDayList ──

    @Test
    fun `parseDayOfWeek - cac bien the`() {
        assertEquals(2, normalizer.parseDayOfWeek("Thứ 2"))
        assertEquals(2, normalizer.parseDayOfWeek("Thứ Hai"))
        assertEquals(4, normalizer.parseDayOfWeek("T4"))
        assertEquals(8, normalizer.parseDayOfWeek("Chủ nhật"))
        assertNull(normalizer.parseDayOfWeek("không rõ"))
    }

    @Test
    fun `parseDayList - da ngay T2T4T5`() {
        assertEquals(listOf(2, 4, 5), normalizer.parseDayList("T2,T4,T5"))
        assertEquals(listOf(8), normalizer.parseDayList("CN"))
        assertEquals(listOf(3), normalizer.parseDayList("T3"))
    }

    // ── parseDate / parseRoom ──

    @Test
    fun `parseDate - dinh dang dd MM yyyy`() {
        assertEquals("2026-09-26", normalizer.parseDate("Ngày thi 26/09/2026"))
        assertNull(normalizer.parseDate("99/99/2026"))
    }

    @Test
    fun `parseRoom - pattern phong VN`() {
        assertEquals("B1-203", normalizer.parseRoom("Học tại B1-203"))
        assertEquals("TC-305", normalizer.parseRoom("Phòng TC-305 nhé"))
        assertEquals("P305", normalizer.parseRoom("Phòng P.305"))
        assertEquals("308-B", normalizer.parseRoom("308-B"))
        assertEquals("Online", normalizer.parseRoom("Học Online"))
    }

    // ── extractCode ──

    @Test
    fun `extractCode - ma co prefix UET`() {
        assertEquals("UET.CS1058", normalizer.extractCode("UET.CS1058 19"))
        assertEquals("UET.MAT1051", normalizer.extractCode("môn UET.MAT1051"))
        assertEquals("IT4409", normalizer.extractCode("IT4409"))
        assertNull(normalizer.extractCode("không có mã"))
    }

    // ── normalize bảng chuẩn ──

    @Test
    fun `normalize bang docx - co header`() {
        val raw = RawExtraction(
            rawText = "",
            tableRows = listOf(
                listOf("Thứ", "Tiết", "Môn học", "Mã HP", "Phòng", "CBGD"),
                listOf("2", "1-2", "Công nghệ Web", "IT4409", "B1-203", "TS. Nguyen Van A"),
                listOf("4", "3-4", "Giải tích 1", "MA1115", "TC-305", "PGS. Tran Thi B")
            )
        )
        val entries = normalizer.normalize(raw)
        assertEquals(2, entries.size)
        val web = entries[0]
        assertEquals("Công nghệ Web", web.subjectName)
        assertEquals("IT4409", web.subjectCode)
        assertEquals("B1-203", web.room)
        assertEquals(2, web.dayOfWeek)
        assertEquals(EntryKind.CLASS, web.kind)
    }

    @Test
    fun `normalize - dong chua keyword thi duoc day sang EXAM`() {
        val raw = RawExtraction(
            rawText = "",
            tableRows = listOf(
                listOf("Ngày thi", "Môn", "Giờ", "Phòng", "SBD"),
                listOf("26/09/2026", "Giải tích 1 - Thi cuối kỳ", "07:30-09:00", "TC-305", "20261234")
            )
        )
        val entries = normalizer.normalize(raw)
        assertEquals(1, entries.size)
        assertEquals(EntryKind.EXAM, entries[0].kind)
        assertEquals("2026-09-26", entries[0].date)
        assertEquals("20261234", entries[0].candidateId)
    }

    // ── Dạng bảng UET (file .doc HTML thật) ──

    @Test
    fun `normalize - bang UET da ngay da tiet bung thanh nhieu buoi`() {
        val raw = RawExtraction(
            rawText = "",
            tableRows = listOf(
                listOf("STT", "Mã môn học", "Môn học", "Số tín chỉ", "Trạng thái", "Học phí",
                    "Lớp môn học", "Thứ", "Tiết", "Giảng đường"),
                // Giải tích 2: học T2,T4,T5 tiết "4 - 6,10 - 12,7 - 9" → 3 buổi
                listOf("3", "UET.MAT1051", "Giải tích 2", "5", "Đăng ký học lại", "00",
                    "UET.MAT1051 16", "T2,T4,T5", "4 - 6,10 - 12,7 - 9", "405-B508-B408-B")
            )
        )
        val entries = normalizer.normalize(raw)
        assertEquals(3, entries.size)
        assertTrue(entries.all { it.subjectName == "Giải tích 2" })
        assertTrue(entries.all { it.subjectCode == "UET.MAT1051" })
        assertEquals(listOf(2, 4, 5), entries.map { it.dayOfWeek })
        // Buổi 1: T2 tiết 4-6 → tiết 4 bắt đầu = 7:00 + 3*(50) + 1 nghỉ(10') = 09:40... kiểm tra format
        assertNotNull(entries[0].startTime)
        assertTrue(entries[0].startTime!!.startsWith("0"))
    }

    @Test
    fun `normalize - bang UET mot ngay mot tiet`() {
        val raw = RawExtraction(
            rawText = "",
            tableRows = listOf(
                listOf("STT", "Mã môn học", "Môn học", "Thứ", "Tiết", "Giảng đường"),
                listOf("1", "UET.CS1058", "Cấu trúc dữ liệu và giải thuật", "T3", "4 - 6", "308-B")
            )
        )
        val entries = normalizer.normalize(raw)
        assertEquals(1, entries.size)
        val e = entries[0]
        assertEquals("UET.CS1058", e.subjectCode)
        assertEquals("Cấu trúc dữ liệu và giải thuật", e.subjectName)
        assertEquals(3, e.dayOfWeek)
        assertEquals("308-B", e.room)
        // Bảng giờ UET: Tiết 4-6 → 09:50–12:30
        assertEquals("09:50", e.startTime)
        assertEquals("12:30", e.endTime)
    }

    // ── OCR text block ──

    @Test
    fun `normalize OCR text block - ghep theo ma HP`() {
        val raw = RawExtraction(
            rawText = """
                IT4409
                Công nghệ Web
                Thứ 2, tiết 1-2, 7:30-9:30
                Phòng B1-203
                GV: TS. Nguyen Van A
            """.trimIndent(),
            tableRows = emptyList()
        )
        val entries = normalizer.normalize(raw)
        assertTrue(entries.isNotEmpty())
        val e = entries[0]
        assertEquals("IT4409", e.subjectCode)
        assertNotNull(e.startTime)
        assertTrue(e.confidence <= 1f)
    }

    @Test
    fun `normalize - text rac khong crash`() {
        val raw = RawExtraction(rawText = "hello world\nkhong co lich", tableRows = emptyList())
        val entries = normalizer.normalize(raw)
        assertTrue(entries.all { it.subjectName.isNotBlank() })
    }
}
