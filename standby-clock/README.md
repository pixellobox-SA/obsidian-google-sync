# Standby Clock for Android

Version 0.6

A calm bedside and desk clock inspired by iPhone StandBy. Put your phone on a
wireless charger on its side and it opens by itself, showing the time and the
current weather over your wallpaper, StandBy-style. Nothing else.

- Big, left-aligned time and date in Inter, with a 24-hour or 12-hour option
- Two widgets on the right: a working analogue clock and the weather (town, icon,
  temperature, condition, high and low), on slightly transparent dark panels
- Your phone's wallpaper behind everything, or a photo you choose, a colour from a
  colour wheel (with brightness), or plain black
- Battery percentage with a gently "breathing" charging icon, and a greeting above the
  time (automatic "Good morning", or your own message)
- Swipe left for this month's **calendar** and your **to-do list**; swipe right for a
  **quote of the week** and six **quick actions** (apps or contacts)
- Optional **Do Not Disturb** while the clock is up, switched back when you lift the phone
- Tap the time to open your phone's own clock app (alarms, timers)
- Soft fade when the minute changes
- Night mode: after a set hour the colours change to your night colour (dark orange to
  white, soft beige by default); brightness is never lowered
- Choose your own text and clock colour, and show or hide the analogue clock
- Free and open. No ads, no tracking, no analytics, no account

---

## How it works (in one paragraph)

A small helper runs quietly in the background and waits. As soon as the phone is
**charging wirelessly** (cable charging is ignored) **and** standing **on its side**
(landscape), it switches the screen on and opens the clock, even over the lock
screen (your phone stays locked). The screen then stays on at your normal
brightness. Swipe left or right for the other pages. Lift the phone off the charger
or turn it upright and the clock closes. You can also close it with the back
gesture; it then won't reappear until you take the phone off the stand and put it
back.

It uses the motion sensor to tell "on its side" from "upright" or "lying flat",
but only while the phone is on the wireless charger. The rest of the time it just
waits for the charger and uses no noticeable battery.

---

## Quickest way: download a ready-made APK

GitHub builds the app automatically whenever its code changes.

1. On GitHub, open this repository's **Actions** tab and select **Standby Clock APK**.
2. Open the most recent run with a green tick.
3. Under **Artifacts**, select **standby-clock-apk** to download it. You need to be
   signed in to GitHub.
4. Unzip the download to get `standby-clock.apk`. Copy it to your phone and open it,
   allowing "Install unknown apps" when asked.
5. Continue with **Part 6: Turn it on** below.

To build the app yourself instead, follow Parts 1–5.

## Part 1: Install Android Studio (one time, about 20 minutes)

Android Studio is Google's free program for building Android apps. It runs on
Windows, macOS and Linux.

1. Go to **https://developer.android.com/studio** and select **Download Android Studio**.
2. Run the installer and accept the default options.
3. Open Android Studio. The setup wizard asks you to download the "Android SDK".
   Choose **Standard** and select **Next** until it finishes. The download is large,
   so this step can take a while.

## Part 2: Get the source code onto your computer

**Option A: download a ZIP (easiest)**

1. On the GitHub page for this repository, select the green **Code** button, then **Download ZIP**.
2. Unzip the file somewhere easy to find, such as your Desktop.
3. Inside the unzipped folder there is a folder called **`standby-clock`**. That
   folder is the Android project.

**Option B: if you use Git**

```bash
git clone <repository URL>
```

## Part 3: Open the project

1. In Android Studio, select **File → Open** (or **Open** on the welcome screen).
2. Pick the **`standby-clock`** folder (not the folder above it) and select **OK**.
3. If Android Studio asks whether to **Trust** the project, select **Trust Project**.
4. Wait. A progress bar at the bottom says something like "Gradle sync". The first
   time, Android Studio downloads the tools this project needs, which can take
   5–10 minutes. It's finished when the bar disappears and there are no red errors.

   - If Android Studio offers to **upgrade the Android Gradle Plugin**, you can safely
     select **Don't ask again for this project**. The project builds as it is.
   - If it says an **SDK platform is missing**, select the blue link in the message
     (for example "Install missing platform and sync project") and let it install.

## Part 4: Prepare your phone

You only need to do this once. It lets your computer install apps on the phone.

1. On the phone, open **Settings → About phone**.
2. Find **Build number** and tap it **seven times**. You'll see "You are now a developer".
   (On Samsung it's under **Settings → About phone → Software information**.)
3. Go back to **Settings → System → Developer options** (on some phones just
   **Settings → Developer options**) and turn on **USB debugging**.
4. Connect the phone to the computer with a USB cable. On the phone, select **Allow**
   when it asks "Allow USB debugging?".

## Part 5: Install the app

**Easiest way: press Run**

1. At the top of Android Studio, your phone's name appears in the device menu.
2. Select the green **▶ Run** button.
3. The app builds, installs and opens on your phone. Done.

**Or: build an APK file** (to copy to the phone yourself, or to keep)

1. Select **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   (In some versions it's **Build → Build APK(s)**.)
2. When it finishes, a small pop-up appears. Select **locate** to open the folder.
   The file is `app-debug.apk`, in `standby-clock/app/build/outputs/apk/debug/`.
3. Copy the APK to your phone (USB, Google Drive, email to yourself…), open it on the
   phone, and allow "Install unknown apps" when asked.

> **Tip:** for a smaller APK, open **Build → Select Build Variant…**, change
> `app` from **debug** to **release**, and build again. The file is then
> `app/build/outputs/apk/release/app-release.apk`. It's signed with your computer's
> debug key, which is fine for your own phones.

## Part 6: Turn it on

1. Open **Standby Clock** from your app list.
2. Under **Automatic start**, make sure **Open when charging wirelessly on its side**
   is on.
3. Next to "allow Display over other apps", select **Allow**. In the list that opens,
   find **Standby Clock**, turn the switch on, then go back. Without this, Android
   doesn't let the clock open by itself.
4. Next to "allow background use", select **Allow** and confirm. This stops the phone
   from switching the helper off to save battery.
5. Under **Weather**, select **Allow approximate location**, or type a city instead.
6. Put the phone on a wireless charger, standing on its side. After about a second
   the clock appears.

Select **Preview** in the app to see the clock at any time. Use the back gesture to close it.

> **Used an earlier version?** It worked as a screen saver. Go to
> **Settings → Display → Screen saver** and turn it off (or pick a different one),
> otherwise the old screen saver may still appear while charging.

> **Samsung and other phones with strict battery saving:** if the clock stops opening
> after a while, open **Settings → Apps → Standby Clock → Battery** and choose
> **Unrestricted**.

---

## Settings

| Setting                                    | What it does                                                          |
| ------------------------------------------ | --------------------------------------------------------------------- |
| Open when charging wirelessly on its side  | Turns automatic start on or off                                       |
| 24-hour clock                              | Switches between 14:30 and 2:30 PM                                     |
| Show analogue clock                        | Show or hide the analogue clock widget                                 |
| Text and clock colour                      | Your own colour (colour wheel + brightness) for text, hands and borders |
| Background                                 | Phone wallpaper, a photo, a colour from a colour wheel, or black       |
| Greeting                                   | Your name, and an optional custom message above the time               |
| Do Not Disturb                             | Priority mode while the clock is up (needs Do Not Disturb access)      |
| To-do list                                 | Add, tick and remove items (you can also tick them on the clock)       |
| Quick actions                              | Six tiles: each opens an app or a contact's card                       |
| Show °F instead of °C                      | Temperature units                                                      |
| Location                                   | Approximate location, or a city you type                               |
| Night mode                                 | Between the start and end hours the colours change (no dimming)        |
| Night colour                               | Slider from dark orange through soft beige to white                    |

Settings apply the next time the clock starts.

---

## Privacy

- **No ads, no analytics, no tracking, no account.** There is no code for any of it.
- **Weather:** the app asks [Open-Meteo](https://open-meteo.com) for the forecast.
  It sends only a latitude and longitude rounded to two decimals (about 1 km).
  Open-Meteo is free and needs no key or sign-up.
- **Town name:** Android's built-in geocoder turns the rounded location into a town
  name. On most phones this is provided by Google Play Services.
- **Typed city:** if you type a city, the name is sent to Open-Meteo's geocoding
  service to find its coordinates, and your phone's location isn't used at all.
- **Background:** the phone wallpaper is drawn by Android itself; the app never reads
  it. A photo you pick is copied into the app's private storage and never leaves the
  phone. The photo picker shares only that one photo, so no storage permission is needed.
- **Contacts:** quick-action contacts are chosen with Android's contact picker, which
  shares only that one contact. The app has no access to your address book.
- **Quotes, calendar, to-do list and battery** all come from the phone itself; nothing is
  downloaded for them. Quotes are built into the app and change every Monday.
- **Location:** approximate location only (`ACCESS_COARSE_LOCATION`). Precise location
  is never requested.
- Everything else, including settings and the last weather result, stays on the phone.

## Battery and performance

- The clock updates once a minute, using Android's own minute "tick", with no
  per-second timer.
- Weather is downloaded at most every 30 minutes. The last result is saved and shown
  straight away next time.
- The screen is kept on, at your normal brightness, only while the clock is open.
  After that, Android's normal screen timeout applies again.
- The background helper does nothing until wireless charging starts. The motion
  sensor is used only while the phone is on the wireless charger. Android shows a
  silent "Standby clock is ready" notification while the helper runs, and you can
  hide it in the notification settings.
- To avoid OLED burn-in, the layout moves a few pixels each minute.
- There are no heavy libraries: only Kotlin, Jetpack Compose and Android's built-in APIs.

---

## For developers

- Kotlin + Jetpack Compose, `minSdk 26` (Android 8.0), `targetSdk 35`
- Build from the command line: `./gradlew assembleDebug`
- Layout:

```
app/src/main/java/app/standbyclock/
  ChargeWatcherService.kt  foreground service: wireless charging + accelerometer → opens
                           the clock; boot receiver restarts it
  ClockActivity.kt         full-screen clock over the lock screen, tap to close
  SettingsActivity.kt      launcher activity
  WindowHelpers.kt         immersive mode, show-over-lock-screen, keep screen on
  data/Prefs.kt            SharedPreferences wrapper
  data/LocationProvider.kt coarse location + reverse geocoding
  data/WeatherRepository.kt Open-Meteo fetch, 30-minute cache, city search
  data/Background.kt        background mode + saving/loading the picked photo
  ui/StandbyScreen.kt      background, three-page pager and page dots
  ui/MainPage.kt           battery, greeting, time/date, widgets, day/night toggle
  ui/CalendarPage.kt       month calendar and to-do widgets
  ui/QuotePage.kt          quote of the week and quick-action tiles
  ui/SmallIcons.kt         breathing battery icon, moon toggle
  ui/SettingsLists.kt      to-do and quick-action editors
  data/Todos.kt, data/Shortcuts.kt, data/Quotes.kt, data/Dnd.kt
  ui/Cards.kt              big time and date, analogue clock and weather widgets
  ui/AnalogClock.kt        analogue clock face drawn on a Canvas
  ui/Widget.kt             translucent dark widget panel
  ui/WeatherIcon.kt        line-art weather icons drawn on a Canvas
  ui/Theme.kt              Inter font, day/night palettes
  ui/State.kt              minute clock, weather refresh loop
  ui/SettingsScreen.kt     settings UI
```

- Font: [Inter](https://rsms.me/inter/) by Rasmus Andersson, SIL Open Font License
  (see `FONT-LICENSE-Inter.txt`).
- Weather data: [Open-Meteo](https://open-meteo.com), CC BY 4.0.
