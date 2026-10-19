package com.example.calculation

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

object HijriCalendarHelper {

    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val ARABIC_DAYS = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    data class HijriDate(
        val day: Int,
        val month: Int,
        val monthNameArabic: String,
        val year: Int,
        val dayOfWeekArabic: String
    ) {
        fun formatArabic(): String = "$day $monthNameArabic $year هـ"
    }

    /**
     * Converts a Gregorian calendar date to Hijri date using Umm Al-Qura / Tabular algorithm.
     */
    fun getHijriDate(calendar: Calendar, dayAdjustment: Int = 0): HijriDate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val localDate = java.time.LocalDate.of(year, month, day)
                var hijrahDate = java.time.chrono.HijrahDate.from(localDate)
                if (dayAdjustment != 0) {
                    hijrahDate = hijrahDate.plus(dayAdjustment.toLong(), java.time.temporal.ChronoUnit.DAYS)
                }

                val hYear = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
                val hMonth = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                val hDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

                val safeMonth = (hMonth - 1).coerceIn(0, 11)
                val monthName = HIJRI_MONTHS_AR[safeMonth]
                val dayOfWeekIdx = (calendar.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
                val dayOfWeek = ARABIC_DAYS[dayOfWeekIdx]

                return HijriDate(
                    day = hDay,
                    month = hMonth,
                    monthNameArabic = monthName,
                    year = hYear,
                    dayOfWeekArabic = dayOfWeek
                )
            } catch (_: Exception) {
            }
        }

        // Algorithmic fallback
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        var year = calendar.get(Calendar.YEAR)

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5 + dayAdjustment

        val z = jd - 1948439.5
        val cyc = floor(z / 10631.0)
        val rem = z - cyc * 10631.0
        val jYear = floor((rem - 0.5) / 354.366)
        val hYear = (cyc * 30 + jYear + 1).toInt()
        val remDays = rem - (jYear * 354.366)
        val hMonth = (floor(remDays / 29.5) + 1).toInt().coerceIn(1, 12)
        val hDay = (remDays - (hMonth - 1) * 29.5 + 1).toInt().coerceIn(1, 30)

        val safeMonth = (hMonth - 1).coerceIn(0, 11)
        val monthName = HIJRI_MONTHS_AR[safeMonth]
        val dayOfWeekIdx = (calendar.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
        val dayOfWeek = ARABIC_DAYS[dayOfWeekIdx]

        return HijriDate(
            day = hDay,
            month = hMonth,
            monthNameArabic = monthName,
            year = hYear,
            dayOfWeekArabic = dayOfWeek
        )
    }

    fun formatGregorianArabic(calendar: Calendar): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
        return sdf.format(calendar.time)
    }

    fun formatTimeArabic(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hours24 = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        val isPm = hours24 >= 12
        val hours12 = if (hours24 % 12 == 0) 12 else hours24 % 12
        val period = if (isPm) "م" else "ص"
        return String.format(Locale.getDefault(), "%02d:%02d %s", hours12, minutes, period)
    }
}
