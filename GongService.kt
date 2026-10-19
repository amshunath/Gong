package com.example.hourlygong

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper

class GongService : Service() {
 private var player: MediaPlayer? = null
 private val handler = Handler(Looper.getMainLooper())
 override fun onBind(intent: Intent?) = null
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  val channelId = "gong_playback"
  val manager = getSystemService(NotificationManager::class.java)
  manager.createNotificationChannel(NotificationChannel(channelId, "Gong playback", NotificationManager.IMPORTANCE_LOW))
  val notification = Notification.Builder(this, channelId).setContentTitle("Hourly Gong").setContentText("Playing scheduled gong").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).build()
  if (Build.VERSION.SDK_INT >= 29) startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(2, notification)
  player?.release()
  val uri = getSharedPreferences("gong", MODE_PRIVATE).getString("uri", null)
  if (uri == null) { stopSelf(startId); return START_NOT_STICKY }
  try {
   player = MediaPlayer().apply {
    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
    setDataSource(this@GongService, Uri.parse(uri))
    setOnCompletionListener { stopSelf(startId) }
    setOnErrorListener { _, _, _ -> stopSelf(startId); true }
    prepare(); start()
   }
   handler.postDelayed({ stopSelf(startId) }, 120_000)
  } catch (_: Exception) { stopSelf(startId) }
  return START_NOT_STICKY
 }
 override fun onDestroy() { handler.removeCallbacksAndMessages(null); player?.release(); player = null; super.onDestroy() }
}
