package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AlertMode
import com.example.model.CalculationMethod
import com.example.model.City
import com.example.model.Coordinates
import com.example.model.JuristicMethod
import com.example.model.PreAlertOption
import com.example.model.PrayerAlertConfig
import com.example.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("salat_app_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USE_GPS = "pref_use_gps"
        private const val KEY_SELECTED_CITY_ID = "pref_selected_city_id"
        private const val KEY_GPS_LAT = "pref_gps_lat"
        private const val KEY_GPS_LNG = "pref_gps_lng"
        private const val KEY_CALC_METHOD = "pref_calc_method"
        private const val KEY_JURISTIC_METHOD = "pref_juristic_method"
        private const val KEY_TASBIH_TOTAL = "pref_tasbih_total"
    }

    fun isUsingGps(): Boolean = prefs.getBoolean(KEY_USE_GPS, false)

    fun setUsingGps(useGps: Boolean) {
        prefs.edit().putBoolean(KEY_USE_GPS, useGps).apply()
    }

    fun getSelectedCity(): City {
        val cityId = prefs.getString(KEY_SELECTED_CITY_ID, CityDatabase.DEFAULT_CITY.id)
        return CityDatabase.CITIES.find { it.id == cityId } ?: CityDatabase.DEFAULT_CITY
    }

    fun setSelectedCity(city: City) {
        prefs.edit()
            .putString(KEY_SELECTED_CITY_ID, city.id)
            .putBoolean(KEY_USE_GPS, false)
            .apply()
    }

    fun getGpsCoordinates(): Coordinates? {
        if (!prefs.contains(KEY_GPS_LAT) || !prefs.contains(KEY_GPS_LNG)) return null
        val lat = prefs.getFloat(KEY_GPS_LAT, 0f).toDouble()
        val lng = prefs.getFloat(KEY_GPS_LNG, 0f).toDouble()
        return Coordinates(lat, lng)
    }

    fun setGpsCoordinates(lat: Double, lng: Double) {
        prefs.edit()
            .putFloat(KEY_GPS_LAT, lat.toFloat())
            .putFloat(KEY_GPS_LNG, lng.toFloat())
            .putBoolean(KEY_USE_GPS, true)
            .apply()
    }

    fun getCalculationMethod(): CalculationMethod {
        val name = prefs.getString(KEY_CALC_METHOD, null)
        return if (name != null) {
            try {
                CalculationMethod.valueOf(name)
            } catch (_: Exception) {
                getSelectedCity().calculationMethod
            }
        } else {
            getSelectedCity().calculationMethod
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
    }

    fun getJuristicMethod(): JuristicMethod {
        val name = prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.STANDARD.name)
        return try {
            JuristicMethod.valueOf(name ?: JuristicMethod.STANDARD.name)
        } catch (_: Exception) {
            JuristicMethod.STANDARD
        }
    }

    fun setJuristicMethod(method: JuristicMethod) {
        prefs.edit().putString(KEY_JURISTIC_METHOD, method.name).apply()
    }

    fun getAlertConfig(prayerType: PrayerType): PrayerAlertConfig {
        val enabledKey = "alert_enabled_${prayerType.name}"
        val modeKey = "alert_mode_${prayerType.name}"
        val preKey = "alert_pre_${prayerType.name}"

        val defaultMode = if (prayerType == PrayerType.SUNRISE) AlertMode.CHIME else AlertMode.ADHAN
        val isEnabled = prefs.getBoolean(enabledKey, true)
        val modeStr = prefs.getString(modeKey, defaultMode.name) ?: defaultMode.name
        val preStr = prefs.getString(preKey, PreAlertOption.NONE.name) ?: PreAlertOption.NONE.name

        val alertMode = try { AlertMode.valueOf(modeStr) } catch (_: Exception) { defaultMode }
        val preOption = try { PreAlertOption.valueOf(preStr) } catch (_: Exception) { PreAlertOption.NONE }

        return PrayerAlertConfig(
            prayerType = prayerType,
            isEnabled = isEnabled,
            alertMode = alertMode,
            preAlertOption = preOption
        )
    }

    fun setAlertConfig(config: PrayerAlertConfig) {
        val prayerType = config.prayerType
        prefs.edit()
            .putBoolean("alert_enabled_${prayerType.name}", config.isEnabled)
            .putString("alert_mode_${prayerType.name}", config.alertMode.name)
            .putString("alert_pre_${prayerType.name}", config.preAlertOption.name)
            .apply()
    }

    fun getMinuteOffset(prayerType: PrayerType): Int {
        return prefs.getInt("offset_minutes_${prayerType.name}", 0)
    }

    fun setMinuteOffset(prayerType: PrayerType, offset: Int) {
        prefs.edit().putInt("offset_minutes_${prayerType.name}", offset).apply()
    }

    fun isPrayerDoneToday(prayerType: PrayerType): Boolean {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return prefs.getBoolean("done_${todayStr}_${prayerType.name}", false)
    }

    fun setPrayerDoneToday(prayerType: PrayerType, isDone: Boolean) {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        prefs.edit().putBoolean("done_${todayStr}_${prayerType.name}", isDone).apply()
    }

    fun getTasbihTotal(): Int = prefs.getInt(KEY_TASBIH_TOTAL, 0)

    fun incrementTasbihTotal() {
        val current = getTasbihTotal()
        prefs.edit().putInt(KEY_TASBIH_TOTAL, current + 1).apply()
    }

    fun resetTasbihTotal() {
        prefs.edit().putInt(KEY_TASBIH_TOTAL, 0).apply()
    }
}
