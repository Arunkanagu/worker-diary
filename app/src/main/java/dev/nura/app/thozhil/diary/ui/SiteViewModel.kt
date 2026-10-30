package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.BillTotals
import dev.nura.app.thozhil.diary.data.NoteCounts
import dev.nura.app.thozhil.diary.data.Site
import dev.nura.app.thozhil.diary.data.SiteBillSummary
import dev.nura.app.thozhil.diary.data.SiteContact
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.data.newId
import dev.nura.app.thozhil.diary.data.amount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A bill, the name of its site and what it adds up to. */
data class BillWithSite(val bill: SiteNote, val siteName: String, val total: Double)

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val sites: StateFlow<List<SiteWithContacts>> = db.siteDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observe(id: String): Flow<SiteWithContacts?> = db.siteDao().observe(id)

    fun observeNotes(siteId: String): Flow<List<SiteNote>> = db.siteDao().observeNotes(siteId, isBill = false)

    fun observeBills(siteId: String): Flow<List<SiteNote>> = db.siteDao().observeNotes(siteId, isBill = true)

    /**
     * Every bill's total keyed by its id: item amounts ((W × L in) / 144 × qty × rate) less discounts
     * plus tax, counting everything saved, hidden or not.
     */
    val billTotals: Flow<Map<String, Double>> = combine(
        db.noteItemDao().observeBillItems(),
        db.billAdjustmentDao().observeAll()
    ) { items, adjustments ->
        val subtotals = items.groupBy { it.noteId }.mapValues { (_, rows) -> rows.sumOf { it.amount() ?: 0.0 } }
        val byNote = adjustments.groupBy { it.noteId }
        (subtotals.keys + byNote.keys).associateWith { BillTotals.of(subtotals[it] ?: 0.0, byNote[it].orEmpty()).billTotal }
    }

    /** Bill count and total per site id, for the Bills tab. */
    val billSummaries: StateFlow<Map<String, SiteBillSummary>> = combine(db.siteDao().observeAllBills(), billTotals) { bills, totals ->
        bills.groupBy { it.siteId }.mapValues { (siteId, list) -> SiteBillSummary(siteId, list.size, list.sumOf { totals[it.id] ?: 0.0 }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Every bill on every site with its site's name and total, newest first, for Home. */
    val recentBills: StateFlow<List<BillWithSite>> = combine(
        db.siteDao().observeAllBills(),
        billTotals,
        db.siteDao().observeAll()
    ) { bills, totals, sites ->
        val names = sites.associate { it.site.id to it.site.name }
        bills.sortedByDescending { it.createdAt }
            .map { BillWithSite(it, names[it.siteId].orEmpty(), totals[it.id] ?: 0.0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** How many tables and items each of a site's notes and bills holds, keyed by note id. */
    fun observeNoteCounts(siteId: String): Flow<Map<String, NoteCounts>> =
        db.siteDao().observeNoteCounts(siteId).map { list -> list.associateBy { it.noteId } }

    fun saveNote(note: SiteNote) {
        viewModelScope.launch {
            db.siteDao().upsertNote(note)
        }
    }

    /** Removes the note with its tables, items and bill adjustments. */
    fun deleteNote(note: SiteNote) {
        viewModelScope.launch {
            db.withTransaction {
                db.billAdjustmentDao().deleteForNote(note.id)
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
                db.siteDao().upsert(site)
                db.siteDao().deleteContactsForSite(site.id)
                db.siteDao().insertContacts(contacts.map { it.copy(id = newId(), siteId = site.id) })
            }
        }
    }

    /** Removes the site with its contacts, notes, note tables, items and bill adjustments. */
    fun delete(site: Site) {
        viewModelScope.launch {
            db.withTransaction {
                db.siteDao().deleteContactsForSite(site.id)
                db.billAdjustmentDao().deleteForSite(site.id)
                db.noteItemDao().deleteForSite(site.id)
                db.noteTableDao().deleteForSite(site.id)
                db.siteDao().deleteNotesForSite(site.id)
                db.siteDao().delete(site.id)
            }
        }
    }
}
