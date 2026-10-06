package com.microsaving.app.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

fun formatMoney(currency: String, amount: Double): String {
    val sign = if (amount < 0) "-" else ""
    return "$sign$currency${String.format(Locale.US, "%,.2f", abs(amount))}"
}

/** Accepts both "12.50" and "12,50". Returns null for anything that is not a positive number. */
fun parseAmount(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)

fun formatDay(date: LocalDate): String = date.format(dayFormatter)
