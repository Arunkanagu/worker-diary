package dev.nura.app.thozhil.diary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemAmountTest {
    private val delta = 1e-9

    @Test
    fun parsesWholeDecimalAndFractionSizes() {
        assertEquals(2.0, parseSize("2")!!, delta)
        assertEquals(2.5, parseSize("2.5")!!, delta)
        assertEquals(0.125, parseSize("1/8")!!, delta)
        assertEquals(2.125, parseSize("2.1/8")!!, delta)
        assertEquals(2.125, parseSize("2 1/8")!!, delta)
        assertNull(parseSize(""))
        assertNull(parseSize("abc"))
    }

    @Test
    fun areaIsWidthTimesLengthInInchesOver144() {
        // 36 in × 84 in = 3,024 sq in = 21 sq ft
        assertEquals(21.0, areaSqFt("36", SizeUnit.INCH, "84", SizeUnit.INCH)!!, delta)
        // Feet count as 12 inches: 3 ft × 7 ft is the same 21 sq ft
        assertEquals(21.0, areaSqFt("3", SizeUnit.FEET, "7", SizeUnit.FEET)!!, delta)
        // Mixed units: 42 in × 7 ft = 42 × 84 / 144 = 24.5 sq ft
        assertEquals(24.5, areaSqFt("42", SizeUnit.INCH, "7", SizeUnit.FEET)!!, delta)
        assertNull(areaSqFt("", SizeUnit.INCH, "7", SizeUnit.FEET))
    }

    @Test
    fun amountIsAreaTimesQuantityTimesRate() {
        // 3½ ft × 7 ft = 24.5 sq ft; × 2 pcs × ₹100 = ₹4,900
        assertEquals(4900.0, itemAmount("3.1/2", SizeUnit.FEET, "7", SizeUnit.FEET, 2, 100.0)!!, delta)
    }

    @Test
    fun thicknessDoesNotChangeTheAmount() {
        val thin = NoteItem(noteId = "1", item = "Door", width = "3", widthUnit = SizeUnit.FEET, length = "7",
            lengthUnit = SizeUnit.FEET, thickness = "1", quantity = 1, rate = 100.0)
        val thick = thin.copy(thickness = "2.1/2")
        assertEquals(2100.0, thin.amount()!!, delta)
        assertEquals(thin.amount()!!, thick.amount()!!, delta)
    }

    @Test
    fun rowWithoutBothSizesIsPricedPerPiece() {
        assertEquals(300.0, itemAmount("", SizeUnit.INCH, "", SizeUnit.FEET, 3, 100.0)!!, delta)
        assertEquals(100.0, itemAmount("3", SizeUnit.FEET, "", SizeUnit.FEET, null, 100.0)!!, delta)
        assertNull(itemAmount("3", SizeUnit.FEET, "7", SizeUnit.FEET, 1, null))
    }
}
