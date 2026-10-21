package dev.nura.app.thozhil.diary.data

import androidx.room.Entity
import androidx.room.Index

enum class AttendanceStatus { PRESENT, ABSENT }

/** One record per employee per day; saving again for the same day replaces it. */
@Entity(
    tableName = "attendance",
    primaryKeys = ["employeeId", "date"],
    indices = [Index("date")]
)
data class Attendance(
    val employeeId: Long,
    /** ISO date, yyyy-MM-dd */
    val date: String,
    val status: AttendanceStatus,
    /** HH:mm, only when present */
    val inTime: String? = null,
    /** HH:mm, only when present */
    val outTime: String? = null,
    val overtimeHours: Double = 0.0,
    /** Optional, only when absent */
    val absentReason: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
