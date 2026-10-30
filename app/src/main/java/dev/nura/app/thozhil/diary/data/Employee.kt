package dev.nura.app.thozhil.diary.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "employees", indices = [Index("userId")])
data class Employee(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val name: String,
    val mobile: String,
    val designation: String = "",
    val department: String = "",
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
)
