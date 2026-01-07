package `in`.hridayan.driftly.home.presentation.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.core.domain.model.SubjectError
import `in`.hridayan.driftly.core.domain.model.TotalAttendance
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.domain.model.toDomain
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import `in`.hridayan.driftly.notification.createAppNotificationSettingsIntent
import `in`.hridayan.driftly.notification.ClassNotificationScheduler
import `in`.hridayan.driftly.notification.TimetableAlarmScheduler
import `in`.hridayan.driftly.settings.presentation.event.SettingsUiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val subjectRepository: SubjectRepository,
    private val attendanceRepository: AttendanceRepository,
    private val classScheduleRepository: ClassScheduleRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _subject = MutableStateFlow("")
    val subject: StateFlow<String> = _subject

    private val _subjectCode = MutableStateFlow("")
    val subjectCode: StateFlow<String> = _subjectCode

    private val _histogramLabel = MutableStateFlow("")
    val histogramLabel: StateFlow<String> = _histogramLabel

    private val _subjectError = MutableStateFlow<SubjectError>(SubjectError.None)
    val subjectError: StateFlow<SubjectError> = _subjectError

    fun setSubjectNamePlaceholder(value: String) {
        if (_subject.value.isBlank()) {
            _subject.value = value
        }
    }

    fun onSubjectChange(newValue: String) {
        _subject.value = newValue
        _subjectError.value = SubjectError.None
    }

    fun onSubjectCodeChange(newValue: String) {
        _subjectCode.value = newValue
    }

    fun onHistogramLabelChange(newValue: String) {
        // Limit to 5 characters
        if (newValue.length <= 5) {
            _histogramLabel.value = newValue
        }
    }

    val subjectList: Flow<List<SubjectEntity>> = subjectRepository.getAllSubjects().stateIn(
        viewModelScope,
        SharingStarted.Eagerly, emptyList()
    )

    fun addSubject(onSuccess: () -> Unit) {
        val isSubjectInvalid = _subject.value.trim().isBlank()
        if (isSubjectInvalid) {
            _subjectError.value = SubjectError.Empty
            return
        }

        viewModelScope.launch {
            val isSubjectExists = subjectRepository.isSubjectExists(_subject.value.trim()).first()
            if (isSubjectExists) {
                _subjectError.value = SubjectError.AlreadyExists
            } else {
                subjectRepository.insertSubject(
                    SubjectEntity(
                        subject = _subject.value.trim(),
                        subjectCode = _subjectCode.value.trim().takeIf { it.isNotBlank() },
                        histogramLabel = _histogramLabel.value.trim().takeIf { it.isNotBlank() }
                    )
                )
                _subject.value = ""
                _subjectCode.value = ""
                _histogramLabel.value = ""
                onSuccess()
            }
        }
    }

    fun resetInputFields() {
        _subject.value = ""
        _subjectCode.value = ""
        _histogramLabel.value = ""
        _subjectError.value = SubjectError.None
    }

    fun updateSubject(subjectId: Int, onSuccess: () -> Unit) {
        val isSubjectInvalid = _subject.value.trim().isBlank()
        if (isSubjectInvalid) {
            _subjectError.value = SubjectError.Empty
            return
        }

        viewModelScope.launch {
            val isSubjectExists = subjectRepository.isSubjectExists(_subject.value.trim()).first()
            if (isSubjectExists) {
                _subjectError.value = SubjectError.AlreadyExists
            } else {
                subjectRepository.updateSubject(
                    subjectId = subjectId,
                    newName = _subject.value.trim(),
                    newCode = _subjectCode.value.trim().takeIf { it.isNotBlank() },
                    histogramLabel = _histogramLabel.value.trim().takeIf { it.isNotBlank() }
                )
                _subject.value = ""
                _subjectCode.value = ""
                _histogramLabel.value = ""
                onSuccess()
            }
        }
    }

    fun deleteSubject(subjectId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Cancel notifications first
            ClassNotificationScheduler.cancelNotifications(context, subjectId)
            subjectRepository.deleteSubject(subjectId)
            onSuccess()
        }
    }

    val subjectCount: Flow<Int> = subjectRepository.getSubjectCount()

    fun getSubjectById(subjectId: Int): Flow<SubjectEntity> {
        return subjectRepository.getSubjectById(subjectId)
    }

    fun deleteAllAttendanceForSubject(subjectId: Int) {
        viewModelScope.launch {
            attendanceRepository.deleteAllAttendanceForSubject(subjectId)
        }
    }

    fun getSubjectAttendanceCounts(subjectId: Int): Flow<SubjectAttendance> {
        val presentFlow = attendanceRepository.getCountBySubjectAndStatus(
            subjectId,
            AttendanceStatus.PRESENT
        )
        val absentFlow =
            attendanceRepository.getCountBySubjectAndStatus(subjectId, AttendanceStatus.ABSENT)

        return combine(presentFlow, absentFlow) { present, absent ->
            SubjectAttendance(
                presentCount = present,
                absentCount = absent,
                totalCount = present + absent
            )
        }
    }

    fun getTotalAttendanceCounts(): Flow<TotalAttendance> {
        val presentFlow = attendanceRepository.getTotalCountByStatus(AttendanceStatus.PRESENT)
        val absentFlow = attendanceRepository.getTotalCountByStatus(AttendanceStatus.ABSENT)

        return combine(presentFlow, absentFlow) { present, absent ->
            TotalAttendance(
                totalPresent = present,
                totalAbsent = absent,
                totalCount = present + absent
            )
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _uiEvent.emit(SettingsUiEvent.RequestPermission(android.Manifest.permission.POST_NOTIFICATIONS))
            } else {
                val intent = createAppNotificationSettingsIntent(context)
                _uiEvent.emit(SettingsUiEvent.LaunchIntent(intent))
            }
        }
    }

    fun updateSubjectTarget(subjectId: Int, targetPercentage: Float) {
        viewModelScope.launch {
            subjectRepository.updateTargetPercentage(subjectId, targetPercentage)
        }
    }

    // Timetable Management
    fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassSchedule>> {
        return classScheduleRepository.getSchedulesForSubject(subjectId)
            .map { schedules -> schedules.map { it.toDomain() } }
    }

    fun saveSchedulesForSubject(subjectId: Int, schedules: List<ClassSchedule>) {
        viewModelScope.launch {
            // Get current schedules from DB to identify deletions
            val currentSchedules = classScheduleRepository.getSchedulesForSubject(subjectId).first().map { it.toDomain() }
            
            // Identify schedules to delete (present in DB but not in new list)
            val newScheduleIds = schedules.map { it.id }.toSet()
            val schedulesToDelete = currentSchedules.filter { it.id != 0 && it.id !in newScheduleIds }

            // Delete removed schedules and cancel their alarms
            if (schedulesToDelete.isNotEmpty()) {
                val entitiesToDelete = schedulesToDelete.map { it.toEntity() }
                entitiesToDelete.forEach { entity ->
                    classScheduleRepository.deleteSchedule(entity)
                }
                
                schedulesToDelete.forEach { schedule ->
                    ClassNotificationScheduler.cancelScheduleNotification(context, schedule.id)
                }
            }

            // Upsert new/updated schedules
            if (schedules.isNotEmpty()) {
                val entitiesToUpsert = schedules.map { it.copy(subjectId = subjectId).toEntity() }
                classScheduleRepository.insertSchedules(entitiesToUpsert)
                
                // Check Exact Alarm Permission
                if (!TimetableAlarmScheduler.canScheduleExactAlarms(context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        _uiEvent.emit(SettingsUiEvent.LaunchIntent(intent))
                    }
                }
                
                // Fetch the updated list from DB (to get generated IDs for new items)
                // Actually, insertSchedules might not return IDs immediately if it's void.
                // But for Alarm scheduling, we ideally need the ID. 
                // However, 'schedules' has 0 for new ones. 
                // If we schedule with ID 0, it won't be unique?
                // PROBLEM: We need the new IDs.
                // Assuming inserts happens fast, we can re-fetch.
                val updatedSchedules = classScheduleRepository.getSchedulesForSubject(subjectId).first().map { it.toDomain() }
                
                // Schedule exact alarms using AlarmManager for precise timing
                updatedSchedules.forEach { schedule ->
                    TimetableAlarmScheduler.scheduleClassAlarms(
                        context = context,
                        scheduleId = schedule.id,
                        subjectId = subjectId,
                        subjectName = getSubjectById(subjectId).first().subject,
                        dayOfWeek = schedule.dayOfWeek,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        location = schedule.location
                    )
                }
            }
        }
    }



    fun deleteSchedulesForSubject(subjectId: Int) {
        viewModelScope.launch {
            val schedules = classScheduleRepository.getSchedulesForSubject(subjectId).first()
            TimetableAlarmScheduler.cancelAllAlarmsForSubject(
                context,
                schedules.map { it.id }
            )
            classScheduleRepository.deleteSchedulesForSubject(subjectId)
        }
    }
}

