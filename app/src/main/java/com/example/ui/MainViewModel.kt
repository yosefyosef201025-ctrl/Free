package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AdhanPlayer
import com.example.calculation.HijriCalendarHelper
import com.example.calculation.PrayerTimeCalculator
import com.example.calculation.QiblaCalculator
import com.example.data.CityDatabase
import com.example.data.DhikrDatabase
import com.example.data.DhikrItem
import com.example.data.UserPreferencesRepository
import com.example.model.*
import com.example.notification.PrayerNotificationHelper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen(val arabicTitle: String) {
    PRAYER_TIMES("المواقيت"),
    QIBLA("القبلة"),
    ADHKAR("الأذكار"),
    SETTINGS("التنبيهات")
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.PRAYER_TIMES,
    val selectedCity: City = CityDatabase.DEFAULT_CITY,
    val isUsingGps: Boolean = false,
    val dateHijri: String = "",
    val dateGregorian: String = "",
    val prayerTimes: List<PrayerTimeItem> = emptyList(),
    val nextPrayer: PrayerTimeItem? = null,
    val remainingMillis: Long = 0L,
    val countdownProgress: Float = 0f,
    val completedPrayersCount: Int = 0,

    // Qibla state
    val compassHeading: Float = 0f,
    val qiblaBearing: Double = 0.0,
    val distanceKaabaKm: Double = 0.0,
    val isSensorAvailable: Boolean = false,

    // Audio & Alert state
    val isAudioPlaying: Boolean = false,
    val alertConfigInEdit: PrayerAlertConfig? = null,
    val isCityPickerVisible: Boolean = false,

    // Calculation settings
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val juristicMethod: JuristicMethod = JuristicMethod.STANDARD,
    val minuteOffsets: Map<PrayerType, Int> = emptyMap(),

    // Tasbih state
    val totalTasbihCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val repo = UserPreferencesRepository(application)
    private val adhanPlayer = AdhanPlayer(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    init {
        initializePreferences()
        setupSensors()
        startClockTicker()
        scheduleNotifications()
        observeAudioPlayback()
    }

    private fun initializePreferences() {
        val isGps = repo.isUsingGps()
        val city = repo.getSelectedCity()
        val method = repo.getCalculationMethod()
        val juristic = repo.getJuristicMethod()
        val offsets = PrayerType.entries.associateWith { repo.getMinuteOffset(it) }
        val tasbihTotal = repo.getTasbihTotal()

        val coords = if (isGps) {
            repo.getGpsCoordinates() ?: Coordinates(city.latitude, city.longitude)
        } else {
            Coordinates(city.latitude, city.longitude)
        }

        val qiblaBearing = QiblaCalculator.calculateQiblaDirection(coords)
        val distanceKaaba = QiblaCalculator.calculateDistanceToKaabaKm(coords)

        _uiState.value = _uiState.value.copy(
            selectedCity = city,
            isUsingGps = isGps,
            calculationMethod = method,
            juristicMethod = juristic,
            minuteOffsets = offsets,
            qiblaBearing = qiblaBearing,
            distanceKaabaKm = distanceKaaba,
            totalTasbihCount = tasbihTotal
        )

        refreshPrayerTimes()
    }

    private fun observeAudioPlayback() {
        viewModelScope.launch {
            adhanPlayer.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isAudioPlaying = playing)
            }
        }
    }

    private fun startClockTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                updateCountdownAndStatus()
                delay(1000)
            }
        }
    }

    fun refreshPrayerTimes() {
        val calendar = Calendar.getInstance()
        val hijriDate = HijriCalendarHelper.getHijriDate(calendar)
        val dateGregorian = HijriCalendarHelper.formatGregorianArabic(calendar)

        val coords = if (repo.isUsingGps()) {
            repo.getGpsCoordinates() ?: repo.getSelectedCity().let { Coordinates(it.latitude, it.longitude) }
        } else {
            repo.getSelectedCity().let { Coordinates(it.latitude, it.longitude) }
        }

        val calculated = PrayerTimeCalculator.calculate(
            calendar = calendar,
            coordinates = coords,
            method = _uiState.value.calculationMethod,
            juristic = _uiState.value.juristicMethod,
            minuteOffsets = _uiState.value.minuteOffsets
        )

        val items = PrayerType.entries.map { type ->
            val millis = calculated.getMillisFor(type)
            val config = repo.getAlertConfig(type)
            val isChecked = repo.isPrayerDoneToday(type)
            PrayerTimeItem(
                type = type,
                timeMillis = millis,
                timeFormatted = HijriCalendarHelper.formatTimeArabic(millis),
                alertConfig = config,
                isCheckedDone = isChecked
            )
        }

        val completedCount = items.filter { it.type != PrayerType.SUNRISE && it.isCheckedDone }.size

        _uiState.value = _uiState.value.copy(
            dateHijri = hijriDate.formatArabic(),
            dateGregorian = dateGregorian,
            prayerTimes = items,
            completedPrayersCount = completedCount
        )

        updateCountdownAndStatus()
    }

    private fun updateCountdownAndStatus() {
        val now = System.currentTimeMillis()
        val items = _uiState.value.prayerTimes
        if (items.isEmpty()) return

        // Find next prayer today
        var next = items.firstOrNull { it.timeMillis > now }
        var isTomorrow = false

        if (next == null) {
            // Next is Fajr of tomorrow
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val coords = if (repo.isUsingGps()) {
                repo.getGpsCoordinates() ?: repo.getSelectedCity().let { Coordinates(it.latitude, it.longitude) }
            } else {
                repo.getSelectedCity().let { Coordinates(it.latitude, it.longitude) }
            }
            val tomorrowCalculated = PrayerTimeCalculator.calculate(
                calendar = tomorrowCal,
                coordinates = coords,
                method = _uiState.value.calculationMethod,
                juristic = _uiState.value.juristicMethod,
                minuteOffsets = _uiState.value.minuteOffsets
            )
            val tomorrowFajrMillis = tomorrowCalculated.fajrMillis
            next = items.first().copy(
                timeMillis = tomorrowFajrMillis,
                timeFormatted = HijriCalendarHelper.formatTimeArabic(tomorrowFajrMillis)
            )
            isTomorrow = true
        }

        val remaining = (next.timeMillis - now).coerceAtLeast(0L)

        // Find previous prayer to calculate progress
        val previousTime = items.filter { it.timeMillis <= now }.maxOfOrNull { it.timeMillis }
            ?: (next.timeMillis - (4 * 3600 * 1000L))

        val totalInterval = (next.timeMillis - previousTime).coerceAtLeast(1L)
        val elapsed = (now - previousTime).coerceIn(0L, totalInterval)
        val progress = (elapsed.toFloat() / totalInterval.toFloat()).coerceIn(0f, 1f)

        val updatedItems = items.map { item ->
            item.copy(
                isNext = item.type == next.type && !isTomorrow
            )
        }

        _uiState.value = _uiState.value.copy(
            prayerTimes = updatedItems,
            nextPrayer = next,
            remainingMillis = remaining,
            countdownProgress = progress
        )
    }

    fun setScreen(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun selectCity(city: City) {
        repo.setSelectedCity(city)
        val coords = Coordinates(city.latitude, city.longitude)
        val qibla = QiblaCalculator.calculateQiblaDirection(coords)
        val dist = QiblaCalculator.calculateDistanceToKaabaKm(coords)

        _uiState.value = _uiState.value.copy(
            selectedCity = city,
            isUsingGps = false,
            qiblaBearing = qibla,
            distanceKaabaKm = dist
        )
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun setGpsLocation(lat: Double, lng: Double) {
        repo.setGpsCoordinates(lat, lng)
        val coords = Coordinates(lat, lng)
        val qibla = QiblaCalculator.calculateQiblaDirection(coords)
        val dist = QiblaCalculator.calculateDistanceToKaabaKm(coords)

        _uiState.value = _uiState.value.copy(
            isUsingGps = true,
            qiblaBearing = qibla,
            distanceKaabaKm = dist
        )
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun setCalculationMethod(method: CalculationMethod) {
        repo.setCalculationMethod(method)
        _uiState.value = _uiState.value.copy(calculationMethod = method)
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun setJuristicMethod(method: JuristicMethod) {
        repo.setJuristicMethod(method)
        _uiState.value = _uiState.value.copy(juristicMethod = method)
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun setMinuteOffset(prayerType: PrayerType, offset: Int) {
        repo.setMinuteOffset(prayerType, offset)
        val updatedOffsets = _uiState.value.minuteOffsets.toMutableMap().apply {
            put(prayerType, offset)
        }
        _uiState.value = _uiState.value.copy(minuteOffsets = updatedOffsets)
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun openAlertConfig(prayerType: PrayerType) {
        val config = repo.getAlertConfig(prayerType)
        _uiState.value = _uiState.value.copy(alertConfigInEdit = config)
    }

    fun closeAlertConfig() {
        _uiState.value = _uiState.value.copy(alertConfigInEdit = null)
        adhanPlayer.stop()
    }

    fun saveAlertConfig(config: PrayerAlertConfig) {
        repo.setAlertConfig(config)
        closeAlertConfig()
        refreshPrayerTimes()
        scheduleNotifications()
    }

    fun togglePrayerDone(prayerType: PrayerType) {
        val current = repo.isPrayerDoneToday(prayerType)
        repo.setPrayerDoneToday(prayerType, !current)
        refreshPrayerTimes()
    }

    fun toggleCityPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(isCityPickerVisible = show)
    }

    fun toggleAdhanAudioPreview(alertMode: AlertMode = AlertMode.ADHAN) {
        if (_uiState.value.isAudioPlaying) {
            adhanPlayer.stop()
        } else {
            adhanPlayer.play(alertMode, triggerVibration = true)
        }
    }

    fun onTasbihTap() {
        repo.incrementTasbihTotal()
        _uiState.value = _uiState.value.copy(totalTasbihCount = repo.getTasbihTotal())
        AdhanPlayer.vibrate(getApplication(), 40)
    }

    fun setManualCompassHeading(heading: Float) {
        _uiState.value = _uiState.value.copy(compassHeading = heading)
        if (QiblaCalculator.isAligned(heading, _uiState.value.qiblaBearing)) {
            AdhanPlayer.vibrate(getApplication(), 50)
        }
    }

    fun sendTestNotification() {
        PrayerNotificationHelper.showPrayerNotification(
            context = getApplication(),
            prayerType = _uiState.value.nextPrayer?.type ?: PrayerType.DHUHR,
            isPreAlert = false,
            alertMode = AlertMode.ADHAN
        )
    }

    private fun scheduleNotifications() {
        PrayerNotificationHelper.scheduleUpcomingPrayerAlarms(getApplication())
    }

    // Compass sensors
    private fun setupSensors() {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) {
            _uiState.value = _uiState.value.copy(isSensorAvailable = true)
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accelerometerSensor != null && magnetometerSensor != null) {
                _uiState.value = _uiState.value.copy(isSensorAvailable = true)
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                _uiState.value = _uiState.value.copy(isSensorAvailable = false)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f

            val wasAligned = QiblaCalculator.isAligned(_uiState.value.compassHeading, _uiState.value.qiblaBearing)
            val isNowAligned = QiblaCalculator.isAligned(azimuthDeg, _uiState.value.qiblaBearing)
            if (!wasAligned && isNowAligned) {
                AdhanPlayer.vibrate(getApplication(), 80)
            }

            _uiState.value = _uiState.value.copy(compassHeading = azimuthDeg)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                hasGeomagnetic = true
            }

            if (hasGravity && hasGeomagnetic) {
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravityValues, geomagneticValues)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuthDeg < 0) azimuthDeg += 360f
                    _uiState.value = _uiState.value.copy(compassHeading = azimuthDeg)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        sensorManager.unregisterListener(this)
        adhanPlayer.stop()
    }
}
