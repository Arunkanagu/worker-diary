package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.Attendance
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.data.Employee
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** An employee with their record for the selected day, or null when not marked. */
data class DayAttendance(val employee: Employee, val record: Attendance?) {
    val status: AttendanceStatus? get() = record?.status
}

/**
 * Present marks as a share of all marks in [month], up to the selected day (or today);
 * null when nothing is marked.
 */
data class MonthAttendance(val month: YearMonth, val presentPercent: Int?)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val dayAttendance: StateFlow<List<DayAttendance>> = _selectedDate.flatMapLatest { date ->
        combine(
            db.employeeDao().observeAll(),
            db.attendanceDao().observeForDate(date.toString())
        ) { employees, records ->
            val byEmployee = records.associateBy { it.employeeId }
            employees.map { DayAttendance(it, byEmployee[it.id]) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthAttendance: StateFlow<MonthAttendance> = _selectedDate.flatMapLatest { date ->
        val month = YearMonth.from(date)
        db.attendanceDao()
            .observeBetween(month.atDay(1).toString(), date.toString())
            .map { records ->
                val present = records.count { it.status == AttendanceStatus.PRESENT }
                MonthAttendance(month, if (records.isEmpty()) null else present * 100 / records.size)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthAttendance(YearMonth.now(), null))

    /** Future days can't be marked, so the date is kept at or before today. */
    fun selectDate(date: LocalDate) {
        val today = LocalDate.now()
        _selectedDate.value = if (date.isAfter(today)) today else date
    }

    /** Marks the status; choosing the current status again clears it. */
    fun toggle(employeeId: Long, current: AttendanceStatus?, chosen: AttendanceStatus) {
        val date = _selectedDate.value.toString()
        viewModelScope.launch {
            if (current == chosen) {
                db.attendanceDao().delete(employeeId, date)
            } else {
                db.attendanceDao().upsertAll(listOf(Attendance(employeeId = employeeId, date = date, status = chosen)))
            }
        }
    }

    fun setAbsentReason(record: Attendance, reason: String) {
        viewModelScope.launch {
            db.attendanceDao().upsertAll(
                listOf(record.copy(absentReason = reason.trim(), updatedAt = System.currentTimeMillis()))
            )
        }
    }

    /** Marks everyone not yet marked on the selected day as present. */
    fun markRestPresent() {
        val date = _selectedDate.value.toString()
        val unmarked = dayAttendance.value.filter { it.status == null }
        if (unmarked.isEmpty()) return
        viewModelScope.launch {
            db.attendanceDao().upsertAll(
                unmarked.map { Attendance(employeeId = it.employee.id, date = date, status = AttendanceStatus.PRESENT) }
            )
        }
    }
}
