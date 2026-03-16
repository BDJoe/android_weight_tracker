package com.josephlimbert.weighttracker.data.utils

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.util.Date
import java.util.Locale

fun formatDateToMonthYearString(date: Date): String {
    val formatter = SimpleDateFormat("MMM y", Locale.getDefault())
    return formatter.format(date)
}

fun formatDateToDayOfWeekString(date: Date): String {
    val formatter = SimpleDateFormat("E", Locale.getDefault())
    return formatter.format(date)
}

fun formatDateToDayOfMonthString(date: Date): String {
    val formatter = SimpleDateFormat("d", Locale.getDefault())
    return formatter.format(date)
}

fun formatDateToMediumPatternString(date: Date): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun formatStringToTimestamp(date: String): Timestamp {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return Timestamp(formatter.parse(date))
}

fun formatMillisToDateString(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(millis))
}