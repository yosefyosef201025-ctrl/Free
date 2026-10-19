package com.example.calculation

import com.example.model.CalculationMethod
import com.example.model.Coordinates
import com.example.model.JuristicMethod
import com.example.model.PrayerType
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object PrayerTimeCalculator {

    data class CalculatedTimes(
        val fajrMillis: Long,
        val sunriseMillis: Long,
        val dhuhrMillis: Long,
        val asrMillis: Long,
        val maghribMillis: Long,
        val ishaMillis: Long
    ) {
        fun getMillisFor(prayerType: PrayerType): Long = when (prayerType) {
            PrayerType.FAJR -> fajrMillis
            PrayerType.SUNRISE -> sunriseMillis
            PrayerType.DHUHR -> dhuhrMillis
            PrayerType.ASR -> asrMillis
            PrayerType.MAGHRIB -> maghribMillis
            PrayerType.ISHA -> ishaMillis
        }
    }

    /**
     * Calculates prayer times for a given day, location coordinates, calculation method,
     * juristic method, and optional minute adjustments.
     */
    fun calculate(
        calendar: Calendar,
        coordinates: Coordinates,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        juristic: JuristicMethod = JuristicMethod.STANDARD,
        minuteOffsets: Map<PrayerType, Int> = emptyMap()
    ): CalculatedTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tzOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis).toDouble() / 3600000.0

        val julianDay = getJulianDay(year, month, day) - (coordinates.longitude / (15.0 * 24.0))

        val solarCoordinates = getSunPosition(julianDay)
        val declination = solarCoordinates.declination
        val equationOfTime = solarCoordinates.equationOfTime

        // Solar noon (Dhuhr) in fractional hours
        val noon = fixHour(12.0 + tzOffsetHours - (coordinates.longitude / 15.0) - (equationOfTime / 60.0))

        // Sunrise & Sunset angle (accounting for refraction and solar radius)
        val sunriseAngle = 0.8333
        val sunriseDiff = getHourAngle(sunriseAngle, coordinates.latitude, declination)
        val sunrise = if (!sunriseDiff.isNaN()) noon - sunriseDiff else noon - 6.0
        val sunset = if (!sunriseDiff.isNaN()) noon + sunriseDiff else noon + 6.0

        // Fajr angle
        val fajrDiff = getHourAngle(method.fajrAngle, coordinates.latitude, declination)
        val fajr = if (!fajrDiff.isNaN()) noon - fajrDiff else noon - 7.5

        // Asr angle based on shadow length factor
        val asrAngle = getAsrAngle(juristic.shadowFactor, coordinates.latitude, declination)
        val asrDiff = getHourAngle(asrAngle, coordinates.latitude, declination)
        val asr = if (!asrDiff.isNaN()) noon + asrDiff else noon + 3.0

        // Maghrib = Sunset
        val maghrib = sunset

        // Isha
        val isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaDiff = getHourAngle(method.ishaAngle, coordinates.latitude, declination)
            if (!ishaDiff.isNaN()) noon + ishaDiff else noon + 7.5
        }

        // Convert fractional hours to epoch millis for the given day
        val fajrMillis = hourToMillis(calendar, fajr, minuteOffsets[PrayerType.FAJR] ?: 0)
        val sunriseMillis = hourToMillis(calendar, sunrise, minuteOffsets[PrayerType.SUNRISE] ?: 0)
        val dhuhrMillis = hourToMillis(calendar, noon, minuteOffsets[PrayerType.DHUHR] ?: 0)
        val asrMillis = hourToMillis(calendar, asr, minuteOffsets[PrayerType.ASR] ?: 0)
        val maghribMillis = hourToMillis(calendar, maghrib, minuteOffsets[PrayerType.MAGHRIB] ?: 0)
        val ishaMillis = hourToMillis(calendar, isha, minuteOffsets[PrayerType.ISHA] ?: 0)

        return CalculatedTimes(
            fajrMillis = fajrMillis,
            sunriseMillis = sunriseMillis,
            dhuhrMillis = dhuhrMillis,
            asrMillis = asrMillis,
            maghribMillis = maghribMillis,
            ishaMillis = ishaMillis
        )
    }

    private fun hourToMillis(baseCal: Calendar, hourFraction: Double, offsetMinutes: Int): Long {
        val cal = baseCal.clone() as Calendar
        val normalizedHour = fixHour(hourFraction)
        val hours = normalizedHour.toInt()
        val minutesTotal = (normalizedHour - hours) * 60.0
        val minutes = minutesTotal.toInt()
        val seconds = ((minutesTotal - minutes) * 60.0).toInt()

        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, seconds)
        cal.set(Calendar.MILLISECOND, 0)

        if (offsetMinutes != 0) {
            cal.add(Calendar.MINUTE, offsetMinutes)
        }
        return cal.timeInMillis
    }

    private data class SolarCoordinates(val declination: Double, val equationOfTime: Double)

    private fun getSunPosition(jd: Double): SolarCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0

        val declination = asinDeg(sinDeg(e) * sinDeg(l))
        val equationOfTime = (q / 15.0) - fixHour(ra)

        return SolarCoordinates(declination, equationOfTime * 60.0)
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun getHourAngle(alpha: Double, latitude: Double, declination: Double): Double {
        val term = (-sinDeg(alpha) - sinDeg(latitude) * sinDeg(declination)) / (cosDeg(latitude) * cosDeg(declination))
        return if (term in -1.0..1.0) {
            acosDeg(term) / 15.0
        } else {
            Double.NaN
        }
    }

    private fun getAsrAngle(shadowFactor: Double, latitude: Double, declination: Double): Double {
        val angleRad = atan(1.0 / (shadowFactor + tan(abs(Math.toRadians(latitude - declination)))))
        return -Math.toDegrees(angleRad)
    }

    private fun fixHour(hour: Double): Double {
        var a = hour - 24.0 * floor(hour / 24.0)
        if (a < 0) a += 24.0
        return a
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun sinDeg(deg: Double): Double = sin(Math.toRadians(deg))
    private fun cosDeg(deg: Double): Double = cos(Math.toRadians(deg))
    private fun tanDeg(deg: Double): Double = tan(Math.toRadians(deg))
    private fun asinDeg(x: Double): Double = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double): Double = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atan2Deg(y: Double, x: Double): Double = Math.toDegrees(atan2(y, x))
}
