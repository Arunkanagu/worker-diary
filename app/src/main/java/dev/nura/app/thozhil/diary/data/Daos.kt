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
    suspend fun insert(employee: Employee)

    @Update
    suspend fun update(employee: Employee)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsert(employee: Employee)

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
    suspend fun delete(employeeId: String, date: String)

    @Query("DELETE FROM attendance WHERE employeeId = :employeeId")
    suspend fun deleteForEmployee(employeeId: String)
}

@Dao
interface SiteDao {
    @Transaction
    @Query("SELECT * FROM sites ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<SiteWithContacts>>

    @Transaction
    @Query("SELECT * FROM sites WHERE id = :id")
    fun observe(id: String): Flow<SiteWithContacts?>

    @Insert
    suspend fun insert(site: Site)

    @Update
    suspend fun update(site: Site)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsert(site: Site)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun delete(id: String)

    @Insert
    suspend fun insertContacts(contacts: List<SiteContact>)

    @Query("DELETE FROM site_contacts WHERE siteId = :siteId")
    suspend fun deleteContactsForSite(siteId: String)

    @Query("SELECT * FROM site_notes WHERE id = :id")
    fun observeNote(id: String): Flow<SiteNote?>

    @Query("SELECT * FROM site_notes WHERE siteId = :siteId AND isBill = :isBill ORDER BY createdAt DESC")
    fun observeNotes(siteId: String, isBill: Boolean): Flow<List<SiteNote>>

    @Query(
        "SELECT n.id AS noteId, " +
            "(SELECT COUNT(*) FROM note_tables t WHERE t.noteId = n.id) AS tables, " +
            "(SELECT COUNT(*) FROM note_items i WHERE i.noteId = n.id) AS items " +
            "FROM site_notes n WHERE n.siteId = :siteId"
    )
    fun observeNoteCounts(siteId: String): Flow<List<NoteCounts>>

    /** Every bill on every site; totals are worked out in Kotlin since sizes like "2.1/8" are text. */
    @Query("SELECT * FROM site_notes WHERE isBill = 1")
    fun observeAllBills(): Flow<List<SiteNote>>

    @Insert
    suspend fun insertNote(note: SiteNote)

    @Update
    suspend fun updateNote(note: SiteNote)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsertNote(note: SiteNote)

    @Query("DELETE FROM site_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("DELETE FROM site_notes WHERE siteId = :siteId")
    suspend fun deleteNotesForSite(siteId: String)
}

@Dao
interface NoteTableDao {
    @Query("SELECT * FROM note_tables WHERE noteId = :noteId ORDER BY createdAt")
    fun observeForNote(noteId: String): Flow<List<NoteTable>>

    @Insert
    suspend fun insert(table: NoteTable)

    @Update
    suspend fun update(table: NoteTable)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsert(table: NoteTable)

    @Query("DELETE FROM note_tables WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM note_tables WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: String)

    @Query("DELETE FROM note_tables WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId)")
    suspend fun deleteForSite(siteId: String)
}

@Dao
interface NoteItemDao {
    @Query("SELECT * FROM note_items WHERE noteId = :noteId ORDER BY createdAt")
    fun observeForNote(noteId: String): Flow<List<NoteItem>>

    @Query("SELECT * FROM note_items WHERE noteId IN (SELECT id FROM site_notes WHERE isBill = 1)")
    fun observeBillItems(): Flow<List<NoteItem>>

    @Insert
    suspend fun insert(item: NoteItem)

    @Update
    suspend fun update(item: NoteItem)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsert(item: NoteItem)

    @Query("DELETE FROM note_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM note_items WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: String)

    @Query("DELETE FROM note_items WHERE tableId = :tableId")
    suspend fun deleteForTable(tableId: String)

    @Query("DELETE FROM note_items WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId)")
    suspend fun deleteForSite(siteId: String)
}

@Dao
interface BillAdjustmentDao {
    @Query("SELECT * FROM bill_adjustments WHERE noteId = :noteId ORDER BY createdAt")
    fun observeForNote(noteId: String): Flow<List<BillAdjustment>>

    @Query("SELECT * FROM bill_adjustments")
    fun observeAll(): Flow<List<BillAdjustment>>

    @Insert
    suspend fun insert(adjustment: BillAdjustment)

    @Update
    suspend fun update(adjustment: BillAdjustment)

    /** Adds it, or saves over the one with the same id. */
    @Upsert
    suspend fun upsert(adjustment: BillAdjustment)

    @Query("DELETE FROM bill_adjustments WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM bill_adjustments WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: String)

    @Query("DELETE FROM bill_adjustments WHERE noteId IN (SELECT id FROM site_notes WHERE siteId = :siteId)")
    suspend fun deleteForSite(siteId: String)
}

@Dao
interface UserDao {
    /** The logged-in user, or null while nobody is. */
    @Query("SELECT users.* FROM users JOIN session ON session.userId = users.id WHERE session.slot = 0")
    fun observeCurrent(): Flow<User?>

    @Query("SELECT users.* FROM users JOIN session ON session.userId = users.id WHERE session.slot = 0")
    suspend fun current(): User?

    @Query("SELECT * FROM users WHERE passwordHash != '' LIMIT 1")
    suspend fun findAnyWithPassword(): User?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun findByMobile(mobile: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND email != '' LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Upsert
    suspend fun setSession(session: Session)

    @Query("DELETE FROM session")
    suspend fun clearSession()

    // Records whose owner isn't on this phone (made before accounts, or by a removed user) go to [userId]
    @Query("UPDATE employees SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimEmployees(userId: String)

    @Query("UPDATE attendance SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimAttendance(userId: String)

    @Query("UPDATE sites SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimSites(userId: String)

    @Query("UPDATE site_contacts SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimSiteContacts(userId: String)

    @Query("UPDATE site_notes SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimSiteNotes(userId: String)

    @Query("UPDATE note_tables SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimNoteTables(userId: String)

    @Query("UPDATE note_items SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimNoteItems(userId: String)

    @Query("UPDATE bill_adjustments SET userId = :userId WHERE userId NOT IN (SELECT id FROM users)")
    suspend fun claimBillAdjustments(userId: String)
}
