package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.Site
import dev.nura.app.thozhil.diary.data.SiteBillSummary
import dev.nura.app.thozhil.diary.data.SiteContact
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val sites: StateFlow<List<SiteWithContacts>> = db.siteDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observe(id: Long): Flow<SiteWithContacts?> = db.siteDao().observe(id)

    fun observeNotes(siteId: Long): Flow<List<SiteNote>> = db.siteDao().observeNotes(siteId, isBill = false)

    fun observeBills(siteId: Long): Flow<List<SiteNote>> = db.siteDao().observeNotes(siteId, isBill = true)

    /** Each bill's total, keyed by its id. */
    fun observeBillTotals(siteId: Long): Flow<Map<Long, Double?>> =
        db.siteDao().observeNoteTotals(siteId).map { totals -> totals.associate { it.noteId to it.total } }

    /** Bill count and total per site id, for the Bills tab. */
    val billSummaries: StateFlow<Map<Long, SiteBillSummary>> = db.siteDao().observeBillSummaries()
        .map { list -> list.associateBy { it.siteId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun saveNote(note: SiteNote) {
        viewModelScope.launch {
            if (note.id == 0L) db.siteDao().insertNote(note) else db.siteDao().updateNote(note)
        }
    }

    /** Removes the note with its tables and items. */
    fun deleteNote(note: SiteNote) {
        viewModelScope.launch {
            db.withTransaction {
                db.noteItemDao().deleteForNote(note.id)
                db.noteTableDao().deleteForNote(note.id)
                db.siteDao().deleteNote(note.id)
            }
        }
    }

    /** Saves the site and replaces its contact list. */
    fun save(site: Site, contacts: List<SiteContact>) {
        viewModelScope.launch {
            db.withTransaction {
                val siteId = if (site.id == 0L) {
                    db.siteDao().insert(site)
                } else {
                    db.siteDao().update(site)
                    db.siteDao().deleteContactsForSite(site.id)
                    site.id
                }
                db.siteDao().insertContacts(contacts.map { it.copy(id = 0, siteId = siteId) })
            }
        }
    }

    /** Removes the site with its contacts, notes, note tables and items. */
    fun delete(site: Site) {
        viewModelScope.launch {
            db.withTransaction {
                db.siteDao().deleteContactsForSite(site.id)
                db.noteItemDao().deleteForSite(site.id)
                db.noteTableDao().deleteForSite(site.id)
                db.siteDao().deleteNotesForSite(site.id)
                db.siteDao().delete(site.id)
            }
        }
    }
}
