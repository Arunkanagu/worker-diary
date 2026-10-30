package dev.nura.app.thozhil.diary.data

import androidx.room.Entity
import androidx.room.Index

enum class AttendanceStatus { PRESENT, ABSENT }

/**
 * One record per employee per day; saving again for the same day replaces it. The employee and day
 * are the key (and unique everywhere, since employee ids are); [id] names the record for syncing and
 * stays the same however often the day is saved again.
 */
@Entity(
    tableName = "attendance",
    primaryKeys = ["employeeId", "date"],
    indices = [Index("date"), Index(value = ["id"], unique = true), Index("userId")]
)
data class Attendance(
    val employeeId: String,
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
    val remarks: String = "",
    val updatedAt: Long = nowMillis(),
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val createdAt: Long = nowMillis()
)
