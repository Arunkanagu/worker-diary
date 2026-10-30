package dev.nura.app.thozhil.diary.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Someone who keeps a diary on this phone: how they log in and what their profile shows. The
 * password is kept only as a salted hash. Every other table's userId points here.
 */
@Entity(tableName = "users", indices = [Index(value = ["mobile"], unique = true)])
data class User(
    @PrimaryKey
    val id: String = newId(),
    val name: String,
    /** Ten digits, no spaces or +91; logs in, so no two users share one. */
    val mobile: String,
    /** Lower case, or blank. */
    val email: String = "",
    val passwordSalt: String = "",
    val passwordHash: String = "",
    val businessName: String = "",
    val trade: String = "",
    val businessAddress: String = "",
    val gstNumber: String = "",
    /** A file in the app's own storage, or blank for initials. */
    val photoPath: String = "",
    /** Also the day the diary was started. */
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
)

/**
 * Who is logged in on this phone: one row while someone is, none after logging out. It stays on the
 * phone and is never synced. New records take their userId from it.
 */
@Entity(tableName = "session")
data class Session(
    @PrimaryKey
    val slot: Int = 0,
    val userId: String,
    val loggedInAt: Long = nowMillis()
)
