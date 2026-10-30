package dev.nura.app.thozhil.diary.util

import java.text.NumberFormat
import java.util.Locale

private val rupees = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

/** Indian grouping with the rupee sign, e.g. ₹1,24,500, ₹350.5 or −₹200. */
fun formatRupees(amount: Double): String =
    if (amount < 0) "−₹" + rupees.format(-amount) else "₹" + rupees.format(amount)

/** A number for an input field: "350", not "350.0". */
fun plainNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

/** Square feet to two places, e.g. "24.5" or "17.53". */
fun formatSqFt(area: Double): String = plainNumber(Math.round(area * 100) / 100.0)

private val ones = listOf(
    "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
    "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
)
private val tens = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

private fun belowHundred(n: Int): String =
    if (n < 20) ones[n] else listOf(tens[n / 10], ones[n % 10]).filter { it.isNotEmpty() }.joinToString(" ")

private fun belowThousand(n: Int): String = listOf(
    if (n >= 100) "${ones[n / 100]} Hundred" else "",
    belowHundred(n % 100)
).filter { it.isNotEmpty() }.joinToString(" ")

/** A whole number in words with Indian grouping: 1,23,45,678 → "One Crore Twenty Three Lakh Forty Five Thousand Six Hundred Seventy Eight". */
private fun numberInWords(n: Long): String {
    if (n == 0L) return "Zero"
    val crore = n / 10_000_000
    val rest = (n % 10_000_000).toInt()
    return listOf(
        if (crore > 0) "${numberInWords(crore)} Crore" else "",
        if (rest / 100_000 > 0) "${belowHundred(rest / 100_000)} Lakh" else "",
        if (rest / 1000 % 100 > 0) "${belowHundred(rest / 1000 % 100)} Thousand" else "",
        belowThousand(rest % 1000)
    ).filter { it.isNotEmpty() }.joinToString(" ")
}

/** An amount as written on a bill, e.g. ₹2,21,940.50 → "Rupees Two Lakh Twenty One Thousand Nine Hundred Forty and Fifty Paise Only". */
fun rupeesInWords(amount: Double): String {
    val totalPaise = Math.round(kotlin.math.abs(amount) * 100)
    val words = numberInWords(totalPaise / 100)
    val paise = (totalPaise % 100).toInt()
    val sign = if (amount < 0 && totalPaise > 0) "Minus " else ""
    return if (paise == 0) "${sign}Rupees $words Only" else "${sign}Rupees $words and ${belowHundred(paise)} Paise Only"
}
