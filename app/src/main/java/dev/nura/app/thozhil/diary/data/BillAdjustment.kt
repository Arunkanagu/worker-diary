package dev.nura.app.thozhil.diary.data

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.nura.app.thozhil.diary.R

enum class AdjustmentType(val label: String, @param:StringRes val labelRes: Int) {
    ADVANCE("Advance", R.string.advance),
    DISCOUNT("Discount", R.string.discount),
    TAX("Tax", R.string.tax)
}

/**
 * An amount added to or taken off a bill: a discount lowers the bill total, tax raises it, and an
 * advance already paid lowers the balance due. Hiding one from the PDF keeps it in every total.
 */
@Entity(tableName = "bill_adjustments", indices = [Index("noteId"), Index("userId")])
data class BillAdjustment(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val noteId: String,
    val type: AdjustmentType,
    val amount: Double,
    @ColumnInfo(defaultValue = "0")
    val hideInPdf: Boolean = false,
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
)

/** A bill's money: items, then discount and tax for the bill total, then advances for the balance. */
data class BillTotals(
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val advance: Double
) {
    val billTotal: Double get() = subtotal - discount + tax
    val balanceDue: Double get() = billTotal - advance

    companion object {
        fun of(subtotal: Double, adjustments: List<BillAdjustment>): BillTotals {
            fun sum(type: AdjustmentType) = adjustments.filter { it.type == type }.sumOf { it.amount }
            return BillTotals(subtotal, sum(AdjustmentType.DISCOUNT), sum(AdjustmentType.TAX), sum(AdjustmentType.ADVANCE))
        }
    }
}
