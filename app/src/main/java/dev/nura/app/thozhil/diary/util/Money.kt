package dev.nura.app.thozhil.diary.util

import java.text.NumberFormat
import java.util.Locale

private val rupees = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

/** Indian grouping with the rupee sign, e.g. ₹1,24,500 or ₹350.5. */
fun formatRupees(amount: Double): String = "₹" + rupees.format(amount)

/** A number for an input field: "350", not "350.0". */
fun plainNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
