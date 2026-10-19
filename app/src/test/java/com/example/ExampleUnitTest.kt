package com.example

import com.example.calculation.HijriCalendarHelper
import com.example.calculation.PrayerTimeCalculator
import com.example.calculation.QiblaCalculator
import com.example.data.CityDatabase
import com.example.model.CalculationMethod
import com.example.model.Coordinates
import com.example.model.JuristicMethod
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {

    @Test
    fun testPrayerTimesCalculation_ChronologicalOrder() {
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("Asia/Riyadh")
            set(2026, Calendar.OCTOBER, 19, 12, 0, 0)
        }
        val makkah = Coordinates(21.4225, 39.8262)

        val times = PrayerTimeCalculator.calculate(
            calendar = calendar,
            coordinates = makkah,
            method = CalculationMethod.UMM_AL_QURA,
            juristic = JuristicMethod.STANDARD
        )

        // Verify chronological sequence: Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha
        assertTrue("Fajr must be before Sunrise", times.fajrMillis < times.sunriseMillis)
        assertTrue("Sunrise must be before Dhuhr", times.sunriseMillis < times.dhuhrMillis)
        assertTrue("Dhuhr must be before Asr", times.dhuhrMillis < times.asrMillis)
        assertTrue("Asr must be before Maghrib", times.asrMillis < times.maghribMillis)
        assertTrue("Maghrib must be before Isha", times.maghribMillis < times.ishaMillis)
    }

    @Test
    fun testQiblaCalculation_Accuracy() {
        // From Cairo (approx ~136 degrees)
        val cairo = Coordinates(30.0444, 31.2357)
        val qiblaCairo = QiblaCalculator.calculateQiblaDirection(cairo)
        assertTrue("Cairo Qibla should be between 130° and 140°", qiblaCairo in 130.0..140.0)

        // Distance from Cairo to Kaaba is ~1280 km
        val distanceCairo = QiblaCalculator.calculateDistanceToKaabaKm(cairo)
        assertTrue("Distance Cairo to Kaaba should be around 1200-1400 km", distanceCairo in 1200.0..1400.0)

        // Alignment check
        assertTrue("Compass at 135° should be aligned with Qibla at 135.5°", QiblaCalculator.isAligned(135f, 135.5))
        assertFalse("Compass at 0° should NOT be aligned with Qibla at 135.5°", QiblaCalculator.isAligned(0f, 135.5))
    }

    @Test
    fun testHijriCalendar_Conversion() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 19)
        }
        val hijriDate = HijriCalendarHelper.getHijriDate(calendar)

        assertTrue("Hijri day should be between 1 and 30", hijriDate.day in 1..30)
        assertTrue("Hijri month should be between 1 and 12", hijriDate.month in 1..12)
        assertTrue("Hijri year should be around 1448", hijriDate.year >= 1440)
        assertTrue("Formatted Arabic should contain year", hijriDate.formatArabic().contains("هـ"))
    }

    @Test
    fun testCityDatabase_Integrity() {
        assertFalse("Cities list must not be empty", CityDatabase.CITIES.isEmpty())
        val makkah = CityDatabase.CITIES.find { it.id == "sa_makkah" }
        assertNotNull("Makkah must be in database", makkah)
        assertEquals(CalculationMethod.UMM_AL_QURA, makkah?.calculationMethod)
    }
}
