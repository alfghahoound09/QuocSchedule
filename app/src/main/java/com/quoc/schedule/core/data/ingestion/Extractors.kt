package com.quoc.schedule.core.data.ingestion

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Kết quả thô sau khi đọc nguồn — chỉ là text, chưa mapping. */
data class RawExtraction(val rawText: String, val tableRows: List<List<String>>)

@Singleton
class OcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** Quét text từ ảnh (gallery/camera). Chạy on-device, không cần mạng. */
    suspend fun extractFromImage(uri: Uri): RawExtraction = withContext(Dispatchers.Default) {
        val image = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(image).await()
        RawExtraction(rawText = result.text, tableRows = emptyList())
    }
}

/**
 * Đọc file Word — tự nhận diện 3 dạng theo byte đầu:
 * 1. `<html...`  → Word "Save as HTML" (.doc của các hệ thống đào tạo VN hay xuất dạng này)
 * 2. `PK`        → .docx (nén zip) — bóc word/document.xml
 * 3. `0xD0CF`    → .doc binary Word 97-2003 thật — không hỗ trợ (cần POI scratchpad nặng),
 *                   báo lỗi hướng dẫn user lưu lại thành .docx.
 */
@Singleton
class DocxParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun extract(uri: Uri): RawExtraction = withContext(Dispatchers.Default) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Không mở được file")
        val sniff = String(bytes, 0, minOf(1024, bytes.size), Charsets.ISO_8859_1)
            .trimStart('﻿', ' ', '\n', '\r', '\t', '﻿')

        when {
            sniff.startsWith("<html", ignoreCase = true) ->
                return@withContext parseHtml(String(bytes, Charsets.UTF_8))
            sniff.startsWith("PK") ->
                return@withContext parseDocxZip(bytes)
            bytes.size > 8 && bytes[0] == 0xD0.toByte() && bytes[1] == 0xCF.toByte() ->
                error("File .doc dạng Word 97-2003 (binary) chưa được hỗ trợ.\nHãy mở file trong Word → Save As → chọn Word Document (.docx) rồi thử lại.")
            else ->
                error("File không đúng định dạng Word")
        }
    }

    // ── Dạng 1: Word HTML (.doc xuất từ hệ thống đào tạo) ──

    private fun parseHtml(html: String): RawExtraction {
        val rows = mutableListOf<List<String>>()
        val rowRegex = Regex("""<tr[^>]*>(.*?)</tr>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val cellRegex = Regex("""<t[hd][^>]*>(.*?)</t[hd]>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

        rowRegex.findAll(html).forEach { tr ->
            val cells = cellRegex.findAll(tr.groupValues[1]).map { stripTags(it.groupValues[1]) }.toList()
            if (cells.any { it.isNotBlank() }) rows += cells
        }

        val paraRegex = Regex("""<p[^>]*>(.*?)</p>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val text = paraRegex.findAll(html).map { stripTags(it.groupValues[1]) }
            .filter { it.isNotBlank() }.joinToString("\n")

        return RawExtraction(rawText = text, tableRows = rows)
    }

    private fun stripTags(s: String): String = s
        .replace(Regex("""<[^>]+>"""), "")
        .replace("&nbsp;", " ").replace("&amp;", "&")
        .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
        .replace(Regex("""&#(\d+);""")) { m ->
            m.groupValues[1].toIntOrNull()
                ?.let { String(Character.toChars(it)) }
                ?: ""
        }
        .replace(Regex("""&#x([0-9a-fA-F]+);""")) { m ->
            m.groupValues[1].toIntOrNull(16)
                ?.let { String(Character.toChars(it)) }
                ?: ""
        }
        .trim()

    // ── Dạng 2: .docx chuẩn (zip + document.xml) ──

    private fun parseDocxZip(bytes: ByteArray): RawExtraction {
        var xml: String? = null
        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    xml = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val documentXml = xml ?: error("File không đúng định dạng .docx (thiếu word/document.xml)")

        val rows = mutableListOf<List<String>>()
        val tableRegex = Regex("<w:tbl[ >].*?</w:tbl>", RegexOption.DOT_MATCHES_ALL)
        val rowRegex = Regex("<w:tr[ >].*?</w:tr>", RegexOption.DOT_MATCHES_ALL)
        val cellRegex = Regex("<w:tc>.*?</w:tc>", RegexOption.DOT_MATCHES_ALL)
        val textRegex = Regex("<w:t[^>]*>([^<]*)</w:t>")

        tableRegex.findAll(documentXml).forEach { table ->
            rowRegex.findAll(table.value).forEach { row ->
                val cells = cellRegex.findAll(row.value).map { cell ->
                    textRegex.findAll(cell.value).joinToString("") { it.groupValues[1] }
                        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                        .trim()
                }.toList()
                if (cells.any { it.isNotBlank() }) rows += cells
            }
        }

        val paraRegex = Regex("<w:p[ >].*?</w:p>", RegexOption.DOT_MATCHES_ALL)
        val text = paraRegex.findAll(documentXml)
            .map { p -> textRegex.findAll(p.value).joinToString("") { it.groupValues[1] } }
            .filter { it.isNotBlank() }.joinToString("\n")

        return RawExtraction(rawText = text, tableRows = rows)
    }
}
