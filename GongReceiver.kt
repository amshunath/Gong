package com.example.hourlygong

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GongReceiver : BroadcastReceiver() {
 override fun onReceive(context: Context, intent: Intent?) {
  Scheduler.update(context)
  val prefs = context.getSharedPreferences("gong", Context.MODE_PRIVATE)
  if (prefs.getBoolean("enabled", false) && prefs.getString("uri", null) != null) {
   try { context.startForegroundService(Intent(context, GongService::class.java)) } catch (_: Exception) { }
  }
 }
}
