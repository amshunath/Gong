package com.example.hourlygong

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import android.media.MediaPlayer
import android.app.Activity

class MainActivity : Activity() {
 private val prefs by lazy { getSharedPreferences("gong", MODE_PRIVATE) }
 private var testPlayer: MediaPlayer? = null
 private lateinit var status: TextView
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)
  if (requestCode == 42 && resultCode == RESULT_OK) {
   val uri = data?.data ?: return
   try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
   prefs.edit().putString("uri", uri.toString()).apply()
   status.text = "Sound selected"
  }
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 30, 28, 20); setBackgroundColor(Color.rgb(247,247,250)) }
  val scroll = ScrollView(this).apply { addView(root) }
  setContentView(scroll)
  fun label(text: String, size: Float = 18f): TextView = TextView(this).apply { this.text = text; textSize = size; setTextColor(Color.rgb(30,30,40)); setPadding(0,12,0,12) }
  root.addView(label("Hourly Gong", 30f))
  val toggle = Switch(this).apply { text = "Gong enabled"; textSize = 20f; isChecked = prefs.getBoolean("enabled", false) }
  root.addView(toggle)
  status = label(if (prefs.contains("uri")) "Sound selected" else "No sound selected", 15f)
  root.addView(status)
  root.addView(Button(this).apply { text = "Choose audio file"; setOnClickListener { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "audio/*" }, 42) } })
  root.addView(Button(this).apply { text = "Test sound"; setOnClickListener {
   val uri = prefs.getString("uri", null)
   if (uri == null) { Toast.makeText(this@MainActivity, "Choose an audio file first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
   try { testPlayer?.release(); testPlayer = MediaPlayer().apply { setDataSource(this@MainActivity, Uri.parse(uri)); setOnCompletionListener { it.release(); if (testPlayer === it) testPlayer = null }; prepare(); start() } }
   catch (_: Exception) { Toast.makeText(this@MainActivity, "Could not play file", Toast.LENGTH_LONG).show() }
  } })
  root.addView(label("Select the hours when the gong should play", 18f))
  val selected = prefs.getStringSet("hours", emptySet())!!.toMutableSet()
  val grid = GridLayout(this).apply { columnCount = 3 }
  for (hour in 0..23) {
   val name = "${if (hour % 12 == 0) 12 else hour % 12}:00 ${if (hour < 12) "AM" else "PM"}"
   val box = CheckBox(this).apply {
    text = name; textSize = 13f; isChecked = selected.contains(hour.toString())
    setOnCheckedChangeListener { _, checked ->
     if (checked) selected.add(hour.toString()) else selected.remove(hour.toString())
     prefs.edit().putStringSet("hours", selected.toSet()).apply(); Scheduler.update(this@MainActivity)
    }
   }
   grid.addView(box, GridLayout.LayoutParams().apply { width = 0; columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); height = ViewGroup.LayoutParams.WRAP_CONTENT })
  }
  root.addView(grid)
  toggle.setOnCheckedChangeListener { _, enabled ->
   prefs.edit().putBoolean("enabled", enabled).apply()
   if (enabled) requestAlarmPermissionIfNeeded()
   Scheduler.update(this)
  }
  root.addView(label("The gong plays at the start of each checked hour. Settings are saved automatically.", 14f))
  if (toggle.isChecked) requestAlarmPermissionIfNeeded()
 }
 private fun requestAlarmPermissionIfNeeded() {
  if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
   Toast.makeText(this, "Please allow Alarms & reminders for accurate hourly playback", Toast.LENGTH_LONG).show()
   startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
  }
 }
 override fun onResume() { super.onResume(); Scheduler.update(this) }
 override fun onDestroy() { testPlayer?.release(); super.onDestroy() }
}
