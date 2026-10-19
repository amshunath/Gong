package com.example.hourlygong

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object Scheduler {
    private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(context, 1, Intent(context, GongReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun update(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java)
        manager.cancel(alarmIntent(context))
        val prefs = context.getSharedPreferences("gong", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val hours = prefs.getStringSet("hours", emptySet())!!.mapNotNull { it.toIntOrNull() }.filter { it in 0..23 }
        if (hours.isEmpty()) return
        val now = System.currentTimeMillis()
        val next = hours.map { hour ->
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        }.min()
        if (Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, alarmIntent(context))
    }
}
