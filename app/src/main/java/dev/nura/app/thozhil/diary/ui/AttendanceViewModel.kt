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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
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
    fun toggle(employeeId: String, current: AttendanceStatus?, chosen: AttendanceStatus) {
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

    /** Batch updates status for a set of employee IDs on the selected date. */
    fun markBatchStatus(employeeIds: Set<String>, status: AttendanceStatus) {
        if (employeeIds.isEmpty()) return
        val date = _selectedDate.value.toString()
        viewModelScope.launch {
            val records = employeeIds.map { id ->
                Attendance(employeeId = id, date = date, status = status)
            }
            db.attendanceDao().upsertAll(records)
        }
    }

    fun saveIndividualPresent(
        employeeId: String,
        date: LocalDate,
        inTime: String,
        outTime: String?,
        overtimeHours: Double = 0.0,
        remarks: String
    ) {
        viewModelScope.launch {
            val record = Attendance(
                employeeId = employeeId,
                date = date.toString(),
                status = AttendanceStatus.PRESENT,
                inTime = inTime,
                outTime = outTime?.ifBlank { null },
                overtimeHours = if (outTime.isNullOrBlank()) 0.0 else overtimeHours,
                absentReason = "",
                remarks = remarks.trim(),
                updatedAt = System.currentTimeMillis()
            )
            db.attendanceDao().upsertAll(listOf(record))
        }
    }

    fun saveIndividualAbsent(
        employeeId: String,
        date: LocalDate,
        reason: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val record = Attendance(
                employeeId = employeeId,
                date = date.toString(),
                status = AttendanceStatus.ABSENT,
                inTime = null,
                outTime = null,
                absentReason = reason.trim(),
                remarks = remarks.trim(),
                updatedAt = System.currentTimeMillis()
            )
            db.attendanceDao().upsertAll(listOf(record))
        }
    }

    fun saveBulkPresent(
        employeeIds: Set<String>,
        date: LocalDate,
        inTime: String,
        outTime: String?,
        remarks: String
    ) {
        if (employeeIds.isEmpty()) return
        viewModelScope.launch {
            val records = employeeIds.map { id ->
                Attendance(
                    employeeId = id,
                    date = date.toString(),
                    status = AttendanceStatus.PRESENT,
                    inTime = inTime,
                    outTime = outTime?.ifBlank { null },
                    absentReason = "",
                    remarks = remarks.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            db.attendanceDao().upsertAll(records)
        }
    }

    fun saveBulkAbsent(
        employeeIds: Set<String>,
        date: LocalDate,
        reason: String,
        remarks: String
    ) {
        if (employeeIds.isEmpty()) return
        viewModelScope.launch {
            val records = employeeIds.map { id ->
                Attendance(
                    employeeId = id,
                    date = date.toString(),
                    status = AttendanceStatus.ABSENT,
                    inTime = null,
                    outTime = null,
                    absentReason = reason.trim(),
                    remarks = remarks.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            db.attendanceDao().upsertAll(records)
        }
    }
    fun saveCheckOut(
        employeeId: String,
        date: LocalDate,
        outTime: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val existing = db.attendanceDao().observeForDate(dateStr)
                .firstOrNull()?.firstOrNull { it.employeeId == employeeId }

            val updated = existing?.copy(
                status = AttendanceStatus.PRESENT,
                outTime = outTime,
                remarks = if (remarks.isNotBlank()) remarks.trim() else existing.remarks,
                updatedAt = System.currentTimeMillis()
            ) ?: Attendance(
                employeeId = employeeId,
                date = dateStr,
                status = AttendanceStatus.PRESENT,
                inTime = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                outTime = outTime,
                remarks = remarks.trim(),
                updatedAt = System.currentTimeMillis()
            )

            db.attendanceDao().upsertAll(listOf(updated))
        }
    }

    fun saveBulkSmartPresent(
        employeeIds: Set<String>,
        date: LocalDate,
        commonInTime: String,
        commonOutTime: String?,
        remarks: String
    ) {
        if (employeeIds.isEmpty()) return
        val dateStr = date.toString()
        viewModelScope.launch {
            val currentRecords = db.attendanceDao().observeForDate(dateStr)
                .firstOrNull() ?: emptyList()
            val recordMap = currentRecords.associateBy { it.employeeId }

            val updatedRecords = employeeIds.map { id ->
                val existing = recordMap[id]
                when {
                    existing == null || existing.status != AttendanceStatus.PRESENT -> {
                        Attendance(
                            employeeId = id,
                            date = dateStr,
                            status = AttendanceStatus.PRESENT,
                            inTime = commonInTime,
                            outTime = commonOutTime?.ifBlank { null },
                            absentReason = "",
                            remarks = remarks.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    existing.outTime.isNullOrBlank() -> {
                        existing.copy(
                            status = AttendanceStatus.PRESENT,
                            outTime = commonOutTime?.ifBlank { commonInTime },
                            remarks = if (remarks.isNotBlank()) remarks.trim() else existing.remarks,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    else -> existing
                }
            }
            db.attendanceDao().upsertAll(updatedRecords)
        }
    }

    /**
     * Saves a fresh check-in for each of [employeeIds]: anything they had for the day is cleared and
     * replaced. The screen asks before including anyone who was already marked.
     */
    fun saveBulkCheckIn(employeeIds: Set<String>, date: LocalDate, inTime: String, remarks: String) {
        if (employeeIds.isEmpty()) return
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            db.attendanceDao().upsertAll(employeeIds.map { id ->
                Attendance(
                    employeeId = id,
                    date = date.toString(),
                    status = AttendanceStatus.PRESENT,
                    inTime = inTime,
                    remarks = remarks.trim(),
                    updatedAt = now
                )
            })
        }
    }

    /**
     * Sets the check-out for each of [employeeIds], keeping the check-in they already have. Those in
     * [replaceIds] (already checked out, or marked absent) have the rest of their record cleared and
     * replaced; everyone else keeps their remarks unless new ones are given.
     */
    fun saveBulkCheckOut(
        employeeIds: Set<String>,
        date: LocalDate,
        outTime: String,
        remarks: String,
        replaceIds: Set<String> = emptySet()
    ) {
        if (employeeIds.isEmpty()) return
        val dateStr = date.toString()
        viewModelScope.launch {
            val existing = db.attendanceDao().observeForDate(dateStr).firstOrNull().orEmpty().associateBy { it.employeeId }
            val now = System.currentTimeMillis()
            db.attendanceDao().upsertAll(employeeIds.map { id ->
                val before = existing[id]?.takeIf { it.status == AttendanceStatus.PRESENT }
                Attendance(
                    employeeId = id,
                    date = dateStr,
                    status = AttendanceStatus.PRESENT,
                    inTime = before?.inTime,
                    outTime = outTime,
                    remarks = remarks.trim().ifEmpty { if (id in replaceIds) "" else before?.remarks.orEmpty() },
                    updatedAt = now
                )
            })
        }
    }
}
