package it.piccolegioie.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import java.util.Calendar

object Reminders {
    private const val channel = "daily_joy"
    fun allowed(context: Context) = (Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()

    fun schedule(context: Context) {
        val store = MemoryStore(context)
        val alarm = context.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(context, 1, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
        if (!store.reminderEnabled || !allowed(context)) return
        val at = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, store.reminderHour); set(Calendar.MINUTE, store.reminderMinute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        // A one-shot inexact alarm is rescheduled locally each day, preserving wall time across DST.
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.timeInMillis, pending)
    }

    fun notify(context: Context) {
        if (!allowed(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(channel, "Il tuo piccolo rituale", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 2, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(context, channel)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("C’è un piccolo momento da custodire?")
            .setContentText("Prenditi un minuto. Anche le cose semplici meritano un bigliettino.")
            .setContentIntent(open).setAutoCancel(true).build()
        manager.notify(7, notification)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (MemoryStore(context).reminderEnabled) { Reminders.notify(context); Reminders.schedule(context) }
    }
}
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED)) Reminders.schedule(context)
    }
}
