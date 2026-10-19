package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.audio.AdhanPlayer
import com.example.model.AlertMode
import com.example.model.PrayerType
import com.example.notification.PrayerNotificationHelper

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            PrayerNotificationHelper.ACTION_PRAYER_ALARM -> {
                val prayerTypeName = intent.getStringExtra(PrayerNotificationHelper.EXTRA_PRAYER_TYPE)
                val isPreAlert = intent.getBooleanExtra(PrayerNotificationHelper.EXTRA_IS_PRE_ALERT, false)
                val alertModeName = intent.getStringExtra(PrayerNotificationHelper.EXTRA_ALERT_MODE)

                val prayerType = try {
                    PrayerType.valueOf(prayerTypeName ?: PrayerType.FAJR.name)
                } catch (_: Exception) {
                    PrayerType.FAJR
                }

                val alertMode = try {
                    AlertMode.valueOf(alertModeName ?: AlertMode.ADHAN.name)
                } catch (_: Exception) {
                    AlertMode.ADHAN
                }

                PrayerNotificationHelper.showPrayerNotification(
                    context = context,
                    prayerType = prayerType,
                    isPreAlert = isPreAlert,
                    alertMode = alertMode
                )

                // Reschedule next cycle
                PrayerNotificationHelper.scheduleUpcomingPrayerAlarms(context)
            }

            PrayerNotificationHelper.ACTION_STOP_ALARM -> {
                AdhanPlayer(context).stop()
            }

            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                PrayerNotificationHelper.scheduleUpcomingPrayerAlarms(context)
            }
        }
    }
}
