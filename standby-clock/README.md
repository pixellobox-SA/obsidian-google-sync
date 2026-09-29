# Standby Clock for Android

A calm bedside and desk clock inspired by iPhone StandBy. While your phone charges
lying on its side, it shows the time and the current weather on a pure black
screen. Nothing else.

- Big, thin clock in Inter, with a 24-hour or 12-hour option
- Small weather card: temperature, condition icon, high and low, and town name
- Frosted-glass cards on black, which suits OLED screens
- Soft fade when the minute changes
- Night mode: after a set hour the screen dims and turns red/amber
- Free and open. No ads, no tracking, no analytics, no account

---

## How it works (in one paragraph)

The app is an Android **screen saver** (Android calls these "Daydreams"; developers
call them `DreamService`). Android itself starts a screen saver while the phone is
charging, so the app doesn't need to run in the background. When the phone is
upright (portrait) the screen stays black. When it's on its side (landscape) the
clock appears. Tap the screen or unplug the charger and the clock closes.

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
2. Under **Weather**, select **Allow approximate location** and allow it. You can
   type a city instead if you prefer not to share location.
3. Select **Open screen saver settings**.
4. Choose **Standby clock** as the screen saver.
5. Set **When to start** to **While charging**. (On some phones this is under the
   three-dot menu, or the options may be named "Daydream".)
6. Plug the phone in and lay it on its side. When the screen would normally turn off,
   the clock appears.

Select **Preview** in the app to see the clock at any time. Tap to close it.

> **Samsung phones:** some Samsung versions hide the screen saver menu. The
> **Open screen saver settings** button usually still opens it. If it doesn't, search
> for "Screen saver" in the Settings search bar.

> **Timing:** Android starts the screen saver when the screen would time out. To
> make it appear sooner, lower **Settings → Display → Screen timeout**.

---

## Settings

| Setting                  | What it does                                                        |
| ------------------------ | ------------------------------------------------------------------- |
| 24-hour clock            | Switches between 14:30 and 2:30 PM                                   |
| Show °F instead of °C    | Temperature units                                                    |
| Location                 | Approximate location, or a city you type                             |
| Night mode               | Between the start and end hours the screen dims and turns red/amber  |
| Only show in landscape   | When on, the screen stays black while the phone is upright           |

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
- **Location:** approximate location only (`ACCESS_COARSE_LOCATION`). Precise location
  is never requested.
- Everything else, including settings and the last weather result, stays on the phone.

## Battery and performance

- The clock updates once a minute, using Android's own minute "tick", with no
  per-second timer.
- Weather is downloaded at most every 30 minutes. The last result is saved and shown
  straight away next time.
- The screen is kept on only while the screen saver runs. After that, Android's
  normal screen timeout applies again.
- To avoid OLED burn-in, the layout moves a few pixels each minute.
- There are no heavy libraries: only Kotlin, Jetpack Compose and Android's built-in APIs.

---

## For developers

- Kotlin + Jetpack Compose, `minSdk 26` (Android 8.0), `targetSdk 35`
- Build from the command line: `./gradlew assembleDebug`
- Layout:

```
app/src/main/java/app/standbyclock/
  StandbyDreamService.kt   screen saver (DreamService) + minimal Compose lifecycle
  PreviewActivity.kt       full-screen preview, tap to close
  SettingsActivity.kt      launcher activity
  WindowHelpers.kt         immersive mode, brightness for night mode
  data/Prefs.kt            SharedPreferences wrapper
  data/LocationProvider.kt coarse location + reverse geocoding
  data/WeatherRepository.kt Open-Meteo fetch, 30-minute cache, city search
  ui/StandbyScreen.kt      the clock screen layout
  ui/Cards.kt              clock card, weather card
  ui/GlassCard.kt          frosted-glass container
  ui/WeatherIcon.kt        line-art weather icons drawn on a Canvas
  ui/Theme.kt              Inter font, day/night palettes
  ui/State.kt              minute clock, weather refresh loop
  ui/SettingsScreen.kt     settings UI
```

- Font: [Inter](https://rsms.me/inter/) by Rasmus Andersson, SIL Open Font License
  (see `FONT-LICENSE-Inter.txt`).
- Weather data: [Open-Meteo](https://open-meteo.com), CC BY 4.0.
