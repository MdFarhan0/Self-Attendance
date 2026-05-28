package `in`.hridayan.driftly.calender.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.driftly.calender.domain.usecase.GetWeekDayLabelsUseCase
import `in`.hridayan.driftly.core.data.model.AttendanceEntity
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import `in`.hridayan.driftly.core.domain.model.AttendanceStatus
import `in`.hridayan.driftly.core.domain.model.StreakType
import `in`.hridayan.driftly.core.domain.model.SubjectAttendance
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

// Represents the aggregate status of all attendance entries for a single calendar day
enum class DayAttendanceStatus { ALL_PRESENT, ALL_ABSENT, MIXED, UNMARKED }

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val subjectRepository: SubjectRepository,
    private val getWeekDayLabelsUseCase: GetWeekDayLabelsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _subjectId = MutableStateFlow<Int?>(null)

    private val _selectedMonthYear = mutableStateOf(YearMonth.now())
    val selectedMonthYear: State<YearMonth> = _selectedMonthYear

    // Legacy single-status map for backward compat with streak & color logic
    private val _markedDates = MutableStateFlow<Map<LocalDate, AttendanceStatus>>(emptyMap())
    val markedDatesFlow: StateFlow<Map<LocalDate, AttendanceStatus>> = _markedDates

    // NEW: aggregate day status map (for multi-attendance display)
    private val _dayStatusMap = MutableStateFlow<Map<LocalDate, DayAttendanceStatus>>(emptyMap())
    val dayStatusMapFlow: StateFlow<Map<LocalDate, DayAttendanceStatus>> = _dayStatusMap

    private val _streakMap = MutableStateFlow<Map<LocalDate, StreakType>>(emptyMap())
    val streakMapFlow: StateFlow<Map<LocalDate, StreakType>> = _streakMap

    init {
        savedStateHandle.get<Int>("subjectId")?.also { id ->
            _subjectId.value = id
            loadAttendanceData(id)
        }
    }

    fun updateMonthYear(year: Int, month: Int) {
        _selectedMonthYear.value = YearMonth.of(year, month)
    }

    fun upsert(att: AttendanceEntity, newStatus: AttendanceStatus) {
        viewModelScope.launch {
            attendanceRepository.insertAttendance(att.copy(status = newStatus))
        }
    }

    fun clear(subjectId: Int, date: String) {
        viewModelScope.launch { attendanceRepository.deleteAttendance(subjectId, date) }
    }

    fun getMonthlyAttendanceCounts(
        subjectId: Int, year: Int, month: Int
    ): Flow<SubjectAttendance> {
        val presentFlow = attendanceRepository.getPresentCountForMonth(subjectId, year, month)
        val absentFlow = attendanceRepository.getAbsentCountForMonth(subjectId, year, month)
        return combine(presentFlow, absentFlow) { p, a ->
            SubjectAttendance(p, a, p + a)
        }
    }

    fun getSubjectEntityById(subjectId: Int): Flow<SubjectEntity> {
        return subjectRepository.getSubjectById(subjectId)
    }

    fun saveMonthYearForSubject(subjectId: Int) {
        viewModelScope.launch {
            subjectRepository.updateSavedMonthYear(
                subjectId = subjectId,
                month = _selectedMonthYear.value.monthValue,
                year = _selectedMonthYear.value.year
            )
        }
    }

    fun resetYearMonthToCurrent(){
        _selectedMonthYear.value = YearMonth.now()
    }

    private fun loadAttendanceData(subjectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            attendanceRepository.getAttendanceForSubject(subjectId)
                .collect { list ->
                    // Group by date
                    val grouped = list.groupBy { it.date }

                    // Build legacy single-status map (for streak calculation)
                    // Priority: if any PRESENT exists, mark PRESENT; if only ABSENT, ABSENT
                    val newMap = grouped.mapNotNull { (dateStr, entries) ->
                        val nonUnmarked = entries.filter { it.status != AttendanceStatus.UNMARKED }
                        if (nonUnmarked.isEmpty()) null
                        else {
                            val representativeStatus = when {
                                nonUnmarked.all { it.status == AttendanceStatus.PRESENT } -> AttendanceStatus.PRESENT
                                nonUnmarked.all { it.status == AttendanceStatus.ABSENT } -> AttendanceStatus.ABSENT
                                else -> AttendanceStatus.PRESENT // mixed -> show as present for streak
                            }
                            LocalDate.parse(dateStr) to representativeStatus
                        }
                    }.toMap()
                    _markedDates.value = newMap

                    // Build aggregate day-status map
                    val dayStatus = grouped.mapNotNull { (dateStr, entries) ->
                        val nonUnmarked = entries.filter { it.status != AttendanceStatus.UNMARKED }
                        if (nonUnmarked.isEmpty()) null
                        else {
                            val aggStatus = when {
                                nonUnmarked.all { it.status == AttendanceStatus.PRESENT } -> DayAttendanceStatus.ALL_PRESENT
                                nonUnmarked.all { it.status == AttendanceStatus.ABSENT } -> DayAttendanceStatus.ALL_ABSENT
                                else -> DayAttendanceStatus.MIXED
                            }
                            LocalDate.parse(dateStr) to aggStatus
                        }
                    }.toMap()
                    _dayStatusMap.value = dayStatus

                    _streakMap.value = calculateStreaks(newMap)
                }
        }
    }

    // Add a NEW class entry for a date (returns new entry id)
    fun addClassEntry(subjectId: Int, date: String, status: AttendanceStatus) {
        viewModelScope.launch {
            attendanceRepository.insertAttendance(
                AttendanceEntity(subjectId = subjectId, date = date, status = status)
            )
        }
    }

    // Delete a single class entry by its DB id
    fun deleteClassEntry(id: Int) {
        viewModelScope.launch { attendanceRepository.deleteAttendanceById(id) }
    }

    // Update a single class entry's status
    fun updateClassEntry(entity: AttendanceEntity, newStatus: AttendanceStatus) {
        viewModelScope.launch {
            attendanceRepository.insertAttendance(entity.copy(status = newStatus))
        }
    }

    // Get all attendance entries for a specific date (for the daily bottom sheet)
    fun getEntriesForDate(subjectId: Int, date: String): Flow<List<AttendanceEntity>> {
        return attendanceRepository.getAttendanceForSubjectAndDate(subjectId, date)
    }

    // Update the note on a specific attendance entry
    fun updateNote(id: Int, note: String?) {
        viewModelScope.launch { attendanceRepository.updateNote(id, note) }
    }

    fun onStatusChange(subjectId: Int, date: String, newStatus: AttendanceStatus?) {
        when (newStatus) {
            AttendanceStatus.PRESENT, AttendanceStatus.ABSENT -> {
                upsert(
                    AttendanceEntity(subjectId = subjectId, date = date),
                    newStatus
                )
            }

            AttendanceStatus.UNMARKED, null -> {
                clear(subjectId, date)
            }
        }
    }


    private fun calculateStreaks(
        marked: Map<LocalDate, AttendanceStatus>
    ): Map<LocalDate, StreakType> {
        val streaks = mutableMapOf<LocalDate, StreakType>()

        marked.entries.groupBy({ it.value }, { it.key }).forEach { (status, dates) ->
            if (status == AttendanceStatus.UNMARKED) return@forEach

            val sorted = dates.sorted()
            var i = 0
            while (i < sorted.size) {
                var end = i
                while (end + 1 < sorted.size && sorted[end + 1] == sorted[end].plusDays(1)) {
                    end++
                }
                val len = end - i + 1
                for (j in i..end) {
                    val d = sorted[j]
                    streaks[d] = when {
                        len < 3 -> StreakType.NONE
                        j == i -> StreakType.START
                        j == end -> StreakType.END
                        else -> StreakType.MIDDLE
                    }
                }
                i = end + 1
            }
        }
        return streaks
    }

    private val _weekdayLabels = mutableStateOf<List<String>>(emptyList())
    val weekdayLabels: State<List<String>> = _weekdayLabels

    fun loadWeekdayLabels(isMondayFirstDay: Boolean) {
        _weekdayLabels.value = getWeekDayLabelsUseCase(isMondayFirstDay)
    }
}
