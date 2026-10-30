package dev.nura.app.thozhil.diary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SizeUnit(val short: String) { INCH("in"), FEET("ft") }

/** A named table inside a site note, e.g. "Ground floor windows". */
@Entity(tableName = "note_tables", indices = [Index("noteId"), Index("userId")])
data class NoteTable(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val noteId: String,
    val name: String,
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis(),
    /** The PDF shows only the table's name and total, not its rows; the app still shows everything. */
    @ColumnInfo(defaultValue = "0")
    val hideInPdf: Boolean = false
)

/**
 * One measured row in a note table: an item's size and how many pieces.
 * Sizes are kept as typed so fractions like "2.1/8" (2⅛) read back exactly.
 */
@Entity(tableName = "note_items", indices = [Index("noteId"), Index("tableId"), Index("userId")])
data class NoteItem(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val noteId: String,
    val tableId: String = "",
    val item: String,
    val thickness: String = "",
    val thicknessUnit: SizeUnit = SizeUnit.INCH,
    val width: String = "",
    val widthUnit: SizeUnit = SizeUnit.INCH,
    val length: String = "",
    val lengthUnit: SizeUnit = SizeUnit.INCH,
    val quantity: Int? = null,
    /** Price per piece, used on bills. */
    val rate: Double? = null,
    val remarks: String = "",
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
)

/** A size with its unit for display, e.g. "2.1/8 in"; a dash when empty. */
fun formatSize(value: String, unit: SizeUnit) = if (value.isEmpty()) "–" else "$value ${unit.short}"

private val fractionPattern = Regex("""^(?:(\d+)[. ])?(\d+)/([1-9]\d*)$""")

/** A typed size as a number: "2.5" → 2.5, "1/8" → 0.125, "2.1/8" or "2 1/8" → 2.125; null if empty or unreadable. */
fun parseSize(value: String): Double? {
    val text = value.trim()
    fractionPattern.matchEntire(text)?.let { match ->
        val whole = match.groupValues[1].toDoubleOrNull() ?: 0.0
        return whole + match.groupValues[2].toDouble() / match.groupValues[3].toDouble()
    }
    return text.toDoubleOrNull()
}

/** A size in inches, feet counting as 12 inches. */
fun sizeInInches(value: String, unit: SizeUnit): Double? =
    parseSize(value)?.let { if (unit == SizeUnit.FEET) it * 12 else it }

/** Square feet from width and length: (W in × L in) / 144. Thickness doesn't count. Null without both sizes. */
fun areaSqFt(width: String, widthUnit: SizeUnit, length: String, lengthUnit: SizeUnit): Double? {
    val w = sizeInInches(width, widthUnit) ?: return null
    val l = sizeInInches(length, lengthUnit) ?: return null
    return w * l / 144
}

/**
 * A bill row's amount: (W in × L in) / 144 × quantity × rate per sq ft. A row without both width and
 * length is priced per piece, rate × quantity. A missing quantity counts as 1; null without a rate.
 */
fun itemAmount(width: String, widthUnit: SizeUnit, length: String, lengthUnit: SizeUnit, quantity: Int?, rate: Double?): Double? {
    rate ?: return null
    val pieces = quantity ?: 1
    val area = areaSqFt(width, widthUnit, length, lengthUnit)
    return if (area != null) area * pieces * rate else rate * pieces
}

fun NoteItem.areaSqFt(): Double? = areaSqFt(width, widthUnit, length, lengthUnit)

fun NoteItem.amount(): Double? = itemAmount(width, widthUnit, length, lengthUnit, quantity, rate)
