package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.Employee
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val employees: StateFlow<List<Employee>> = db.employeeDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(employee: Employee) {
        viewModelScope.launch {
            if (employee.id == 0L) db.employeeDao().insert(employee) else db.employeeDao().update(employee)
        }
    }

    /** Removes the employee and all of their attendance and advance records. */
    fun delete(employee: Employee) {
        viewModelScope.launch {
            db.withTransaction {
                db.attendanceDao().deleteForEmployee(employee.id)
                db.advanceDao().deleteForEmployee(employee.id)
                db.employeeDao().delete(employee)
            }
        }
    }
}
