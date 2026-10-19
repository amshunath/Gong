# Hourly Gong — Android Version 1

Native Kotlin Android Studio project. Supports Android 8.0+ (API 26+).

## Build and install
1. Open the `HourlyGong` folder in Android Studio (recent version supporting Android Gradle Plugin 8.7.3).
2. Allow Gradle sync and install any requested Android SDK components.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Transfer `app/build/outputs/apk/debug/app-debug.apk` to your Android phone and install it, allowing installs from that source if prompted.
5. Open the app, select an audio file, check desired hours, and enable the gong.
6. If prompted, allow **Alarms & reminders**. Some phones also require adjusting battery optimization settings.

## Notes
- Uses exact alarms where permission is granted, rescheduling the next selected hour after each alarm.
- Restores scheduling after reboot and system clock/timezone changes.
- Playback uses a media-playback foreground service and stops on completion or after 2 minutes.
- Android's Do Not Disturb, manufacturer power restrictions, and exact-alarm policies can affect sound delivery.
- This is source code, not a prebuilt APK. It has not been device-tested.
