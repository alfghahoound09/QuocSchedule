package com.quoc.schedule.feature.importflow

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quoc.schedule.core.data.ScheduleRepository
import com.quoc.schedule.core.data.ingestion.DocxParser
import com.quoc.schedule.core.data.ingestion.OcrEngine
import com.quoc.schedule.core.data.ingestion.ScheduleNormalizer
import com.quoc.schedule.core.model.DraftStatus
import com.quoc.schedule.core.model.ParsedEntry
import com.quoc.schedule.core.model.SourceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImportUiState {
    data object Choose : ImportUiState
    data class Scanning(val step: String) : ImportUiState
    data class Preview(val entries: List<ParsedEntry>, val draftId: Long?) : ImportUiState
    data object Empty : ImportUiState
    data object Saved : ImportUiState
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val docxParser: DocxParser,
    private val normalizer: ScheduleNormalizer,
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Choose)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun reset() { _uiState.value = ImportUiState.Choose }

    fun importImage(uri: Uri) = runIngestion(SourceType.IMAGE) {
        ocrEngine.extractFromImage(uri)
    }

    fun importDocx(uri: Uri) = runIngestion(SourceType.DOCX) {
        docxParser.extract(uri)
    }

    private fun runIngestion(
        sourceType: SourceType,
        extract: suspend () -> com.quoc.schedule.core.data.ingestion.RawExtraction
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ImportUiState.Scanning("Đang đọc tệp…")
                val raw = extract()
                _uiState.value = ImportUiState.Scanning("Đang nhận diện lịch học…")
                val entries = normalizer.normalize(raw)
                if (entries.isEmpty()) {
                    _uiState.value = ImportUiState.Empty
                    return@launch
                }
                val draftId = repository.saveDraft(sourceType, raw.rawText)
                _uiState.value = ImportUiState.Preview(entries, draftId)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Có lỗi xảy ra")
            }
        }
    }

    /** Người dùng sửa một entry trong màn Preview. */
    fun editEntry(index: Int, updated: ParsedEntry) {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        _uiState.update {
            ImportUiState.Preview(
                entries = state.entries.mapIndexed { i, e -> if (i == index) updated else e },
                draftId = state.draftId
            )
        }
    }

    fun removeEntry(index: Int) {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        _uiState.update {
            ImportUiState.Preview(
                entries = state.entries.filterIndexed { i, _ -> i != index },
                draftId = state.draftId
            )
        }
    }

    fun confirm() {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        viewModelScope.launch {
            try {
                repository.commitParsedEntries(state.entries)
                state.draftId?.let { repository.markDraft(it, DraftStatus.CONFIRMED) }
                _uiState.value = ImportUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Không lưu được dữ liệu")
            }
        }
    }
}
