package com.example.model

enum class PrayerType(val arabicName: String, val englishName: String, val defaultIconName: String) {
    FAJR("الفجر", "Fajr", "wb_twilight"),
    SUNRISE("الشروق", "Sunrise", "wb_sunny"),
    DHUHR("الظهر", "Dhuhr", "light_mode"),
    ASR("العصر", "Asr", "sunny"),
    MAGHRIB("المغرب", "Maghrib", "nightlight_round"),
    ISHA("العشاء", "Isha", "bedtime")
}

enum class AlertMode(val arabicName: String, val description: String) {
    ADHAN("الأذان الكامل", "تشغيل صوت الأذان كاملاً عند دخول الوقت"),
    TAKBEER("تكبيرات فقط", "تشغيل تكبيرات الأذان والتنبيه القصير"),
    CHIME("نغمة هادئة", "رنين روحاني لطيف عند دخول الوقت"),
    VIBRATE("اهتزاز فقط", "تنبيه بالاهتزاز دون إصدار صوت"),
    SILENT("صامت", "إشعار مرئي على الشاشة بدون صوت")
}

enum class PreAlertOption(val minutes: Int, val arabicLabel: String) {
    NONE(0, "في وقت الأذان تماماً"),
    MIN_5(5, "قبل 5 دقائق للاستعداد"),
    MIN_10(10, "قبل 10 دقائق"),
    MIN_15(15, "قبل 15 دقيقة"),
    MIN_30(30, "قبل 30 دقيقة")
}

enum class CalculationMethod(
    val arabicName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0
) {
    UMM_AL_QURA("أم القرى - مكة المكرمة", 18.5, 0.0, ishaIntervalMinutes = 90),
    EGYPTIAN("الهيئة المصرية العامة للمساحة", 19.5, 17.5),
    MWL("رابطة العالم الإسلامي", 18.0, 17.0),
    ISNA("أمريكا الشمالية (ISNA)", 15.0, 15.0),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0),
    DUBAI("دائرة الشؤون الإسلامية بدبي", 18.2, 18.2),
    KUWAIT("وزارة الأوقاف الكويتية", 18.0, 17.5)
}

enum class JuristicMethod(val arabicName: String, val shadowFactor: Double) {
    STANDARD("الشافعي، المالكي، الحنبلي (المثل)", 1.0),
    HANAFI("الحنفي (المثلين)", 2.0)
}

data class PrayerAlertConfig(
    val prayerType: PrayerType,
    val isEnabled: Boolean = true,
    val alertMode: AlertMode = AlertMode.ADHAN,
    val preAlertOption: PreAlertOption = PreAlertOption.NONE
)

data class PrayerTimeItem(
    val type: PrayerType,
    val timeMillis: Long,
    val timeFormatted: String,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val alertConfig: PrayerAlertConfig,
    val isCheckedDone: Boolean = false
)

data class PrayerSchedule(
    val dateGregorian: String,
    val dateHijri: String,
    val times: List<PrayerTimeItem>,
    val nextPrayer: PrayerTimeItem?,
    val timeRemainingMillis: Long,
    val progressToNext: Float
)
