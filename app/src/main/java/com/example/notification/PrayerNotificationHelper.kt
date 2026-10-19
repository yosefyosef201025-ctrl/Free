package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.audio.AdhanPlayer
import com.example.calculation.PrayerTimeCalculator
import com.example.data.UserPreferencesRepository
import com.example.model.AlertMode
import com.example.model.PreAlertOption
import com.example.model.PrayerType
import com.example.receiver.PrayerAlarmReceiver
import java.util.Calendar

object PrayerNotificationHelper {

    const val CHANNEL_ADHAN = "channel_salat_adhan"
    const val CHANNEL_PRE_ALERT = "channel_salat_pre"

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_IS_PRE_ALERT = "extra_is_pre_alert"
    const val EXTRA_ALERT_MODE = "extra_alert_mode"

    const val ACTION_PRAYER_ALARM = "com.aistudio.salat.PRAYER_ALARM"
    const val ACTION_STOP_ALARM = "com.aistudio.salat.ACTION_STOP_ALARM"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN,
                "تنبيهات وأذان الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات وقت الصلاة وصوت الأذان"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val preChannel = NotificationChannel(
                CHANNEL_PRE_ALERT,
                "تنبيهات الاستعداد للصلاة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيه قبل دخول وقت الصلاة"
                enableVibration(true)
            }

            nm.createNotificationChannel(adhanChannel)
            nm.createNotificationChannel(preChannel)
        }
    }

    fun showPrayerNotification(
        context: Context,
        prayerType: PrayerType,
        isPreAlert: Boolean,
        alertMode: AlertMode
    ) {
        createNotificationChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = if (isPreAlert) {
            "اقتراب وقت صلاة ${prayerType.arabicName}"
        } else {
            "حان الآن وقت صلاة ${prayerType.arabicName}"
        }

        val text = if (isPreAlert) {
            "استعد للوضوء والصلاة، الوقت يقترب"
        } else {
            "حيّ على الصلاة، حيّ على الفلاح"
        }

        // Tap opens MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop sound action
        val stopIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            prayerType.ordinal + 500,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isPreAlert) CHANNEL_PRE_ALERT else CHANNEL_ADHAN

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف الصوت", stopPendingIntent)

        if (alertMode == AlertMode.VIBRATE || alertMode == AlertMode.ADHAN || alertMode == AlertMode.TAKBEER) {
            builder.setVibrate(longArrayOf(0, 800, 300, 800))
        }

        nm.notify(prayerType.ordinal + 100, builder.build())

        // Play the audio if configured
        if (!isPreAlert) {
            AdhanPlayer(context).play(alertMode, triggerVibration = false)
        } else {
            AdhanPlayer(context).play(AlertMode.CHIME, triggerVibration = false)
        }
    }

    /**
     * Reschedules next upcoming alarms based on current user preferences.
     */
    fun scheduleUpcomingPrayerAlarms(context: Context) {
        createNotificationChannels(context)
        val repo = UserPreferencesRepository(context)
        val coords = if (repo.isUsingGps()) {
            repo.getGpsCoordinates() ?: repo.getSelectedCity().let { com.example.model.Coordinates(it.latitude, it.longitude) }
        } else {
            repo.getSelectedCity().let { com.example.model.Coordinates(it.latitude, it.longitude) }
        }

        val calcMethod = repo.getCalculationMethod()
        val juristic = repo.getJuristicMethod()
        val offsets = PrayerType.entries.associateWith { repo.getMinuteOffset(it) }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // Check for today and tomorrow
        for (dayOffset in 0..1) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val times = PrayerTimeCalculator.calculate(cal, coords, calcMethod, juristic, offsets)

            PrayerType.entries.forEach { prayer ->
                val config = repo.getAlertConfig(prayer)
                if (config.isEnabled) {
                    val prayerTime = times.getMillisFor(prayer)

                    // 1. Exact Prayer Time Alarm
                    if (prayerTime > now) {
                        scheduleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            triggerTimeMillis = prayerTime,
                            requestCode = prayer.ordinal + (dayOffset * 10),
                            prayerType = prayer,
                            isPreAlert = false,
                            alertMode = config.alertMode
                        )
                    }

                    // 2. Pre-Prayer Alert if configured
                    if (config.preAlertOption != PreAlertOption.NONE) {
                        val preTime = prayerTime - (config.preAlertOption.minutes * 60 * 1000L)
                        if (preTime > now) {
                            scheduleAlarm(
                                context = context,
                                alarmManager = alarmManager,
                                triggerTimeMillis = preTime,
                                requestCode = prayer.ordinal + 100 + (dayOffset * 10),
                                prayerType = prayer,
                                isPreAlert = true,
                                alertMode = AlertMode.CHIME
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerTimeMillis: Long,
        requestCode: Int,
        prayerType: PrayerType,
        isPreAlert: Boolean,
        alertMode: AlertMode
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
            putExtra(EXTRA_IS_PRE_ALERT, isPreAlert)
            putExtra(EXTRA_ALERT_MODE, alertMode.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
    }
}
