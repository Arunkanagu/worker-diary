package dev.nura.app.thozhil.diary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.app.thozhil.diary.data.AppDatabase
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
    private val db: AppDatabase
) : ViewModel() {

    private val noteId: Long = checkNotNull(savedStateHandle["noteId"])

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

    fun saveTable(table: NoteTable) {
        viewModelScope.launch {
            val withNote = table.copy(noteId = noteId)
            if (withNote.id == 0L) db.noteTableDao().insert(withNote) else db.noteTableDao().update(withNote)
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
            if (withNote.id == 0L) db.noteItemDao().insert(withNote) else db.noteItemDao().update(withNote)
        }
    }

    fun deleteItem(item: NoteItem) {
        viewModelScope.launch { db.noteItemDao().delete(item.id) }
    }
}
