package dev.nura.app.thozhil.diary.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Money given to an employee ahead of pay; several can be given on the same day. */
@Entity(
    tableName = "advances",
    indices = [Index("employeeId"), Index("date")]
)
data class Advance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    /** ISO date, yyyy-MM-dd */
    val date: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
