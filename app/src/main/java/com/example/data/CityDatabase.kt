package com.example.data

import com.example.model.CalculationMethod
import com.example.model.City

object CityDatabase {

    val CITIES: List<City> = listOf(
        // Saudi Arabia
        City("sa_makkah", "مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Saudi Arabia", 21.4225, 39.8262, 3.0, CalculationMethod.UMM_AL_QURA),
        City("sa_madinah", "المدينة المنورة", "Madinah", "المملكة العربية السعودية", "Saudi Arabia", 24.4686, 39.6142, 3.0, CalculationMethod.UMM_AL_QURA),
        City("sa_riyadh", "الرياض", "Riyadh", "المملكة العربية السعودية", "Saudi Arabia", 24.7136, 46.6753, 3.0, CalculationMethod.UMM_AL_QURA),
        City("sa_jeddah", "جدة", "Jeddah", "المملكة العربية السعودية", "Saudi Arabia", 21.5433, 39.1728, 3.0, CalculationMethod.UMM_AL_QURA),
        City("sa_dammam", "الدمام", "Dammam", "المملكة العربية السعودية", "Saudi Arabia", 26.4207, 50.0888, 3.0, CalculationMethod.UMM_AL_QURA),
        City("sa_abha", "أبها", "Abha", "المملكة العربية السعودية", "Saudi Arabia", 18.2164, 42.5053, 3.0, CalculationMethod.UMM_AL_QURA),

        // Egypt
        City("eg_cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, 2.0, CalculationMethod.EGYPTIAN),
        City("eg_alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187, 2.0, CalculationMethod.EGYPTIAN),
        City("eg_giza", "الجيزة", "Giza", "مصر", "Egypt", 30.0131, 31.2089, 2.0, CalculationMethod.EGYPTIAN),
        City("eg_aswan", "أسوان", "Aswan", "مصر", "Egypt", 24.0889, 32.8998, 2.0, CalculationMethod.EGYPTIAN),

        // UAE
        City("ae_dubai", "دبي", "Dubai", "الإمارات العربية المتحدة", "UAE", 25.2048, 55.2708, 4.0, CalculationMethod.DUBAI),
        City("ae_abudhabi", "أبوظبي", "Abu Dhabi", "الإمارات العربية المتحدة", "UAE", 24.4539, 54.3773, 4.0, CalculationMethod.DUBAI),
        City("ae_sharjah", "الشارقة", "Sharjah", "الإمارات العربية المتحدة", "UAE", 25.3573, 55.4033, 4.0, CalculationMethod.DUBAI),

        // Palestine
        City("ps_jerusalem", "القدس الشريف", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137, 2.0, CalculationMethod.MWL),
        City("ps_gaza", "غزة", "Gaza", "فلسطين", "Palestine", 31.5017, 34.4668, 2.0, CalculationMethod.MWL),

        // Jordan
        City("jo_amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, 3.0, CalculationMethod.MWL),
        City("jo_irbid", "إربد", "Irbid", "الأردن", "Jordan", 32.5568, 35.8469, 3.0, CalculationMethod.MWL),

        // Kuwait
        City("kw_kuwait", "مدينة الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, 3.0, CalculationMethod.KUWAIT),

        // Qatar
        City("qa_doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, 3.0, CalculationMethod.UMM_AL_QURA),

        // Bahrain
        City("bh_manama", "المنامة", "Manama", "البحرين", "Bahrain", 26.2285, 50.5860, 3.0, CalculationMethod.UMM_AL_QURA),

        // Oman
        City("om_muscat", "مسقط", "Muscat", "سلطنة عمان", "Oman", 23.5880, 58.3829, 4.0, CalculationMethod.UMM_AL_QURA),

        // Iraq
        City("iq_baghdad", "بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661, 3.0, CalculationMethod.MWL),
        City("iq_erbil", "أربيل", "Erbil", "العراق", "Iraq", 36.1901, 44.0091, 3.0, CalculationMethod.MWL),

        // Syria
        City("sy_damascus", "دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765, 3.0, CalculationMethod.MWL),

        // Lebanon
        City("lb_beirut", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018, 2.0, CalculationMethod.MWL),

        // Morocco
        City("ma_casablanca", "الدار البيضاء", "Casablanca", "المغرب", "Morocco", 33.5731, -7.5898, 1.0, CalculationMethod.MWL),
        City("ma_rabat", "الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, 1.0, CalculationMethod.MWL),
        City("ma_marrakech", "مراكش", "Marrakech", "المغرب", "Morocco", 31.6295, -7.9811, 1.0, CalculationMethod.MWL),

        // Algeria
        City("dz_algiers", "الجزائر العاصمة", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, 1.0, CalculationMethod.MWL),
        City("dz_oran", "وهران", "Oran", "الجزائر", "Algeria", 35.6987, -0.6349, 1.0, CalculationMethod.MWL),

        // Tunisia
        City("tn_tunis", "تونس العاصمة", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, 1.0, CalculationMethod.MWL),

        // Libya
        City("ly_tripoli", "طرابلس", "Tripoli", "ليبيا", "Libya", 32.8872, 13.1913, 2.0, CalculationMethod.MWL),

        // Sudan
        City("sd_khartoum", "الخرطوم", "Khartoum", "السودان", "Sudan", 15.5007, 32.5599, 2.0, CalculationMethod.MWL),

        // Turkey
        City("tr_istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, 3.0, CalculationMethod.MWL),
        City("tr_ankara", "أنقرة", "Ankara", "تركيا", "Turkey", 39.9334, 32.8597, 3.0, CalculationMethod.MWL),

        // Asia & Worldwide
        City("pk_karachi", "كراتشي", "Karachi", "باكستان", "Pakistan", 24.8607, 67.0011, 5.0, CalculationMethod.KARACHI),
        City("my_kuala", "كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, 8.0, CalculationMethod.MWL),
        City("id_jakarta", "جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, 7.0, CalculationMethod.MWL),
        City("gb_london", "لندن", "London", "المملكة المتحدة", "UK", 51.5074, -0.1278, 0.0, CalculationMethod.MWL),
        City("fr_paris", "باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, 1.0, CalculationMethod.MWL),
        City("de_berlin", "برلين", "Berlin", "ألمانيا", "Germany", 52.5200, 13.4050, 1.0, CalculationMethod.MWL),
        City("us_newyork", "نيويورك", "New York", "الولايات المتحدة", "USA", 40.7128, -74.0060, -5.0, CalculationMethod.ISNA),
        City("ca_toronto", "تورونتو", "Toronto", "كندا", "Canada", 43.6532, -79.3832, -5.0, CalculationMethod.ISNA)
    )

    val DEFAULT_CITY: City = CITIES[0] // Makkah Al-Mukarramah
}
