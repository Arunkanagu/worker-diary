package dev.nura.app.thozhil.diary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Employee>>

    @Insert
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)

    @Delete
    suspend fun delete(employee: Employee)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun observeForDate(date: String): Flow<List<Attendance>>

    /** Dates are ISO strings, so string comparison orders them correctly. */
    @Query("SELECT * FROM attendance WHERE date BETWEEN :from AND :to")
    fun observeBetween(from: String, to: String): Flow<List<Attendance>>

    @Upsert
    suspend fun upsertAll(records: List<Attendance>)

    @Query("DELETE FROM attendance WHERE employeeId = :employeeId AND date = :date")
    suspend fun delete(employeeId: Long, date: String)

    @Query("DELETE FROM attendance WHERE employeeId = :employeeId")
    suspend fun deleteForEmployee(employeeId: Long)
}

@Dao
interface AdvanceDao {
    @Query("SELECT * FROM advances WHERE date = :date")
    fun observeForDate(date: String): Flow<List<Advance>>

    @Query("SELECT * FROM advances WHERE date BETWEEN :from AND :to ORDER BY date DESC, createdAt DESC")
    fun observeBetween(from: String, to: String): Flow<List<Advance>>

    @Insert
    suspend fun insert(advance: Advance): Long

    @Update
    suspend fun update(advance: Advance)

    @Query("DELETE FROM advances WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM advances WHERE employeeId = :employeeId")
    suspend fun deleteForEmployee(employeeId: Long)
}

@Dao
interface SiteDao {
    @Transaction
    @Query("SELECT * FROM sites ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<SiteWithContacts>>

    @Transaction
    @Query("SELECT * FROM sites WHERE id = :id")
    fun observe(id: Long): Flow<SiteWithContacts?>

    @Insert
    suspend fun insert(site: Site): Long

    @Update
    suspend fun update(site: Site)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert
    suspend fun insertContacts(contacts: List<SiteContact>)

    @Query("DELETE FROM site_contacts WHERE siteId = :siteId")
    suspend fun deleteContactsForSite(siteId: Long)

    @Query("SELECT * FROM site_notes WHERE id = :id")
    fun observeNote(id: Long): Flow<SiteNote?>

    @Query("SELECT * FROM site_notes WHERE siteId = :siteId AND isBill = :isBill ORDER BY createdAt DESC")
    fun observeNotes(siteId: Long, isBill: Boolean): Flow<List<SiteNote>>

    @Query(
        "SELECT noteId, SUM(rate * COALESCE(quantity, 1)) AS total FROM note_items " +
            "WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId) GROUP BY noteId"
    )
    fun observeNoteTotals(siteId: Long): Flow<List<NoteTotal>>

    @Query(
        "SELECT n.siteId AS siteId, COUNT(DISTINCT n.id) AS bills, " +
            "SUM(i.rate * COALESCE(i.quantity, 1)) AS total " +
            "FROM site_notes n LEFT JOIN note_items i ON i.noteId = n.id " +
            "WHERE n.isBill = 1 GROUP BY n.siteId"
    )
    fun observeBillSummaries(): Flow<List<SiteBillSummary>>

    @Insert
    suspend fun insertNote(note: SiteNote): Long

    @Update
    suspend fun updateNote(note: SiteNote)

    @Query("DELETE FROM site_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("DELETE FROM site_notes WHERE siteId = :siteId")
    suspend fun deleteNotesForSite(siteId: Long)
}

@Dao
interface NoteTableDao {
    @Query("SELECT * FROM note_tables WHERE noteId = :noteId ORDER BY createdAt")
    fun observeForNote(noteId: Long): Flow<List<NoteTable>>

    @Insert
    suspend fun insert(table: NoteTable): Long

    @Update
    suspend fun update(table: NoteTable)

    @Query("DELETE FROM note_tables WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM note_tables WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: Long)

    @Query("DELETE FROM note_tables WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId)")
    suspend fun deleteForSite(siteId: Long)
}

@Dao
interface NoteItemDao {
    @Query("SELECT * FROM note_items WHERE noteId = :noteId ORDER BY createdAt")
    fun observeForNote(noteId: Long): Flow<List<NoteItem>>

    @Insert
    suspend fun insert(item: NoteItem): Long

    @Update
    suspend fun update(item: NoteItem)

    @Query("DELETE FROM note_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM note_items WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: Long)

    @Query("DELETE FROM note_items WHERE tableId = :tableId")
    suspend fun deleteForTable(tableId: Long)

    @Query("DELETE FROM note_items WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId)")
    suspend fun deleteForSite(siteId: Long)
}
