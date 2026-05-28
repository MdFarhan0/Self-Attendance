package `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.model.WrappedSubjectCard
import `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.model.WrappedSummary
import `in`.hridayan.driftly.settings.presentation.page.attendancewrapped.model.WrappedUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AttendanceWrappedViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    attendanceRepository: AttendanceRepository
) : ViewModel() {

    val uiState = subjectRepository.getAllSubjects()
        .flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(WrappedUiState())
            } else {
                combine(
                    subjects.map { subject ->
                        attendanceRepository.getAttendanceForSubject(subject.id)
                            .map { attendances ->
                                val attended = subject.attendedCount +
                                    attendances.count { it.status == AttendanceStatus.PRESENT }
                                val skipped = subject.missedCount +
                                    attendances.count { it.status == AttendanceStatus.ABSENT }

                                WrappedSubjectCard(
                                    subjectId = subject.id,
                                    subjectName = subject.subject,
                                    attended = attended,
                                    skipped = skipped,
                                    total = attended + skipped,
                                    targetPercentage = subject.targetPercentage
                                )
                            }
                    }
                ) { cards ->
                    val cardList = cards.toList()
                    val totalAttended = cardList.sumOf { it.attended }
                    val totalSkipped = cardList.sumOf { it.skipped }
                    val totalClasses = totalAttended + totalSkipped

                    WrappedUiState(
                        subjects = cardList,
                        summary = WrappedSummary(
                            overallPercentage = if (totalClasses == 0) 0f
                            else (totalAttended.toFloat() / totalClasses.toFloat()) * 100f,
                            totalAttended = totalAttended,
                            totalSkipped = totalSkipped,
                            totalClasses = totalClasses
                        )
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WrappedUiState()
        )
}
