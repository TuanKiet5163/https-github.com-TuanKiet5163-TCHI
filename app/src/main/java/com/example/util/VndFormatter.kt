package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object VndFormatter {

    private val symbols = DecimalFormatSymbols(Locale.GERMANY).apply {
        groupingSeparator = '.'
    }
    private val decimalFormat = DecimalFormat("#,###", symbols)

    /**
     * Formats an integer/long amount to standard VNĐ representation.
     * Example: 150000 -> "150.000 VNĐ"
     */
    fun formatVnd(amount: Long): String {
        val safeAmount = if (amount < 0) 0L else amount
        return "${decimalFormat.format(safeAmount)} VNĐ"
    }

    /**
     * Formats numeric digits with dot thousands separators without the "VNĐ" suffix.
     * Useful for live input fields.
     */
    fun formatInput(digitsOnly: String): String {
        if (digitsOnly.isEmpty()) return ""
        val value = digitsOnly.toLongOrNull() ?: return ""
        return decimalFormat.format(value)
    }

    /**
     * Extracts digits and returns clean Long.
     */
    fun parseToLong(rawText: String): Long {
        val digits = rawText.filter { it.isDigit() }
        return digits.toLongOrNull() ?: 0L
    }
}
