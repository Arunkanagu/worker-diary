package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
import kotlinx.coroutines.flow.map
import dev.nura.app.thozhil.diary.data.AccountRepository
import dev.nura.app.thozhil.diary.data.BillAdjustment
import dev.nura.app.thozhil.diary.data.NoteItem
import dev.nura.app.thozhil.diary.data.NoteTable
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A note table with its rows, in the order they were added. */
data class TableWithItems(val table: NoteTable, val items: List<NoteItem>)

/** One site note, its tables and items, and the site it belongs to; the note id comes from the route. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val db: AppDatabase,
    accounts: AccountRepository
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    /** Whose name goes on the PDF as having prepared it. */
    val preparedBy: StateFlow<String> = accounts.currentUser.map { it?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val note: StateFlow<SiteNote?> = db.siteDao().observeNote(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val site: StateFlow<SiteWithContacts?> = note.filterNotNull()
        .flatMapLatest { db.siteDao().observe(it.siteId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tables: StateFlow<List<TableWithItems>> = combine(
        db.noteTableDao().observeForNote(noteId),
        db.noteItemDao().observeForNote(noteId)
    ) { tables, items ->
        val byTable = items.groupBy { it.tableId }
        tables.map { TableWithItems(it, byTable[it.id].orEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Advance, discount and tax entries on this bill, in the order they were added. */
    val adjustments: StateFlow<List<BillAdjustment>> = db.billAdjustmentDao().observeForNote(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTable(table: NoteTable) {
        viewModelScope.launch {
            val withNote = table.copy(noteId = noteId)
            db.noteTableDao().upsert(withNote)
        }
    }

    /** Removes the table and its rows. */
    fun deleteTable(table: NoteTable) {
        viewModelScope.launch {
            db.withTransaction {
                db.noteItemDao().deleteForTable(table.id)
                db.noteTableDao().delete(table.id)
            }
        }
    }

    fun saveItem(item: NoteItem) {
        viewModelScope.launch {
            val withNote = item.copy(noteId = noteId)
            db.noteItemDao().upsert(withNote)
        }
    }

    fun deleteItem(item: NoteItem) {
        viewModelScope.launch { db.noteItemDao().delete(item.id) }
    }

    fun saveAdjustment(adjustment: BillAdjustment) {
        viewModelScope.launch {
            val withNote = adjustment.copy(noteId = noteId)
            db.billAdjustmentDao().upsert(withNote)
        }
    }

    /** Whether this bill's PDF ends with a signature line. */
    fun setShowSignature(show: Boolean) {
        val current = note.value ?: return
        viewModelScope.launch { db.siteDao().updateNote(current.copy(showSignature = show)) }
    }

    fun deleteAdjustment(adjustment: BillAdjustment) {
        viewModelScope.launch { db.billAdjustmentDao().delete(adjustment.id) }
    }
}
