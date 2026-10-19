package com.example.calculation

import com.example.model.Coordinates
import kotlin.math.*

object QiblaCalculator {

    // Coordinates of Kaaba in Makkah Al-Mukarramah
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the Qibla bearing in degrees (0..360, where 0 is True North, 90 is East).
     */
    fun calculateQiblaDirection(userCoordinates: Coordinates): Double {
        val latUserRad = Math.toRadians(userCoordinates.latitude)
        val latKaabaRad = Math.toRadians(KAABA_LATITUDE)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - userCoordinates.longitude)

        val y = sin(deltaLngRad)
        val x = cos(latUserRad) * tan(latKaabaRad) - sin(latUserRad) * cos(deltaLngRad)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)

        if (qiblaDeg < 0) {
            qiblaDeg += 360.0
        }
        return qiblaDeg
    }

    /**
     * Calculates distance to Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(userCoordinates: Coordinates): Double {
        val lat1Rad = Math.toRadians(userCoordinates.latitude)
        val lat2Rad = Math.toRadians(KAABA_LATITUDE)
        val deltaLatRad = Math.toRadians(KAABA_LATITUDE - userCoordinates.latitude)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - userCoordinates.longitude)

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLngRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Checks if current compass heading is aligned with Qibla within tolerance degrees.
     */
    fun isAligned(compassHeading: Float, qiblaBearing: Double, toleranceDeg: Float = 3.5f): Boolean {
        var diff = abs(compassHeading - qiblaBearing.toFloat())
        if (diff > 180f) {
            diff = 360f - diff
        }
        return diff <= toleranceDeg
    }
}
