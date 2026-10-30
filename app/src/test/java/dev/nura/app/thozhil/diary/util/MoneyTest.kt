package dev.nura.app.thozhil.diary.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun wordsUseIndianGrouping() {
        assertEquals("Rupees Zero Only", rupeesInWords(0.0))
        assertEquals("Rupees Five Only", rupeesInWords(5.0))
        assertEquals("Rupees Nineteen Only", rupeesInWords(19.0))
        assertEquals("Rupees One Hundred Five Only", rupeesInWords(105.0))
        assertEquals("Rupees Two Lakh Twenty One Thousand Nine Hundred Forty Only", rupeesInWords(221_940.0))
        assertEquals(
            "Rupees One Crore Twenty Three Lakh Forty Five Thousand Six Hundred Seventy Eight Only",
            rupeesInWords(12_345_678.0)
        )
        assertEquals("Rupees One Hundred Twenty Crore Only", rupeesInWords(1_200_000_000.0))
    }

    @Test
    fun wordsIncludePaiseAndSign() {
        assertEquals("Rupees Three Hundred Fifty and Fifty Paise Only", rupeesInWords(350.5))
        assertEquals("Rupees Ten and Five Paise Only", rupeesInWords(10.05))
        assertEquals("Minus Rupees Two Hundred Only", rupeesInWords(-200.0))
    }

    @Test
    fun squareFeetToTwoPlaces() {
        assertEquals("24.5", formatSqFt(24.5))
        assertEquals("21", formatSqFt(21.0))
        assertEquals("17.53", formatSqFt(17.5312))
    }
}
