package `in`.hridayan.driftly.calender.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.driftly.core.domain.model.ClassSchedule
import `in`.hridayan.driftly.core.domain.model.toDomain
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val classScheduleRepository: ClassScheduleRepository
) : ViewModel() {

    fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassSchedule>> {
        return classScheduleRepository.getSchedulesForSubject(subjectId)
            .map { schedules -> schedules.map { it.toDomain() } }
    }

    fun saveSchedulesForSubject(subjectId: Int, schedules: List<ClassSchedule>) {
        viewModelScope.launch {
            // Delete existing schedules
            classScheduleRepository.deleteSchedulesForSubject(subjectId)

            // Insert new schedules
            if (schedules.isNotEmpty()) {
                val entities = schedules.map { it.copy(subjectId = subjectId).toEntity() }
                classScheduleRepository.insertSchedules(entities)
            }
        }
    }

    fun deleteSchedule(schedule: ClassSchedule) {
        viewModelScope.launch {
            classScheduleRepository.deleteSchedule(schedule.toEntity())
        }
    }

    fun updateSchedule(schedule: ClassSchedule) {
        viewModelScope.launch {
            classScheduleRepository.updateSchedule(schedule.toEntity())
        }
    }

    fun deleteAllSchedulesForSubject(subjectId: Int) {
        viewModelScope.launch {
            classScheduleRepository.deleteSchedulesForSubject(subjectId)
        }
    }
}
