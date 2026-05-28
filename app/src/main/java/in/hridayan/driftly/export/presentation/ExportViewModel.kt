package `in`.hridayan.driftly.export.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import `in`.hridayan.driftly.export.pdf.AttendancePdfGenerator
import `in`.hridayan.driftly.export.pdf.model.AttendancePdfItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // Use Eagerly so data is loaded immediately when ViewModel is created
    val subjects: StateFlow<List<SubjectEntity>> = subjectRepository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedSubjectIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedSubjectIds = _selectedSubjectIds.asStateFlow()

    private val _studentName = MutableStateFlow("")
    val studentName = _studentName.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    fun updateStudentName(name: String) {
        _studentName.value = name
    }

    fun toggleSubjectSelection(subjectId: Int) {
        val current = _selectedSubjectIds.value.toMutableSet()
        if (current.contains(subjectId)) {
            current.remove(subjectId)
        } else {
            current.add(subjectId)
        }
        _selectedSubjectIds.value = current
    }

    fun selectAll() {
        _selectedSubjectIds.value = subjects.value.map { it.id }.toSet()
    }

    fun clearAll() {
        _selectedSubjectIds.value = emptySet()
    }

    fun setSelectedSubjectIds(ids: Set<Int>) {
        _selectedSubjectIds.value = ids
    }

    fun generatePdf(context: Context, onGenerated: (File?) -> Unit) {
        val name = _studentName.value.trim().ifEmpty { "Report" }
        val selectedIds = _selectedSubjectIds.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                // Always fetch fresh subject list from DB at generation time
                val freshSubjects = subjectRepository.getAllSubjectsOnce()
                val selectedSubjects = freshSubjects.filter { selectedIds.contains(it.id) }

                val pdfItems = selectedSubjects.map { subject ->
                    // Read real attendance counts from the attendance table (not legacy fields)
                    val attended = attendanceRepository.getPresentCountForSubject(subject.id)
                    val missed   = attendanceRepository.getAbsentCountForSubject(subject.id)
                    val total    = attended + missed
                    val percentage = if (total > 0) {
                        (attended.toFloat() / total) * 100f
                    } else {
                        0.0f
                    }

                    AttendancePdfItem(
                        subjectName     = subject.subject,
                        subjectCode     = subject.subjectCode ?: "",
                        attendedClasses = attended,
                        missedClasses   = missed,
                        threshold       = subject.targetPercentage.toInt(),
                        percentage      = percentage
                    )
                }

                val file = AttendancePdfGenerator.generatePdf(context, name, pdfItems)
                onGenerated(file)
            } catch (e: Exception) {
                e.printStackTrace()
                onGenerated(null)
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
