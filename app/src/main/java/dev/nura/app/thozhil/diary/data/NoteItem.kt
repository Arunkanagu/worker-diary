package dev.nura.app.thozhil.diary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SizeUnit(val short: String) { INCH("in"), FEET("ft") }

/** A named table inside a site note, e.g. "Ground floor windows". */
@Entity(tableName = "note_tables", indices = [Index("noteId")])
data class NoteTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * One measured row in a note table: an item's size and how many pieces.
 * Sizes are kept as typed so fractions like "2.1/8" (2⅛) read back exactly.
 */
@Entity(tableName = "note_items", indices = [Index("noteId"), Index("tableId")])
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    @ColumnInfo(defaultValue = "0")
    val tableId: Long = 0,
    val item: String,
    val thickness: String = "",
    val thicknessUnit: SizeUnit = SizeUnit.INCH,
    val width: String = "",
    val widthUnit: SizeUnit = SizeUnit.INCH,
    val length: String = "",
    val lengthUnit: SizeUnit = SizeUnit.FEET,
    val quantity: Int? = null,
    /** Price per piece, used on bills. */
    val rate: Double? = null,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** A size with its unit for display, e.g. "2.1/8 in"; a dash when empty. */
fun formatSize(value: String, unit: SizeUnit) = if (value.isEmpty()) "–" else "$value ${unit.short}"

/** Rate × quantity (a missing quantity counts as 1); null without a rate. */
fun NoteItem.amount(): Double? = rate?.let { it * (quantity ?: 1) }
