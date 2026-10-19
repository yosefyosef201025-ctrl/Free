package com.example.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0
)

data class City(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA
)
