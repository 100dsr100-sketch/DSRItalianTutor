# DSR Apps widgets

Home-screen app launcher widgets for the Galaxy S22 Ultra in the same style as
the DSR power switch: a gold outlined rounded frame with a row of dark tiles,
each with an icon and label. Four widgets come in the one APK (**DSR Apps**,
**DSR Apps 2**, **3** and **4**); they differ only in their starting apps.

![preview](preview.png)

## Settings

Each widget has its own settings (opened when you add it, by long-pressing it
and tapping **Settings**, or by tapping an empty tile):

- **Icons per row**: 5 to 10 (default 7).
- **Icon colour**: **Gold** or **App colours**. Gold uses a hand-drawn icon for
  the apps below; for any other app it uses the app's Android 13+ themed-icon
  layer when it has one, otherwise a gold version made from the app's icon.
- **Apps**: tap a slot to pick from every installed app (web apps installed
  from Chrome included) or leave it empty; ▲ moves a slot up.

## Starting apps

| Widget     | Tiles |
|------------|-------|
| DSR Apps   | Keep, Drive, Photos, Samsung Gallery, Samsung Music, Samsung Health, My Files |
| DSR Apps 2 | Claude (opens the Claude app's Code tab, `claude://code`), Duolingo, Clock, Calculator, Translate, Anker Soundcore, Plex |
| DSR Apps 3 | CommBank, Binance, CommSec, Macquarie, CommSec Pocket, Messenger, Greater Bank |
| DSR Apps (empty) | Nothing – every tile starts empty; add it as many times as you like and fill each one from its settings |
| DSR Apps 4 | DSR Travel…, Dictation, DSR Media, DSR Researcher, DSR Sound…, DSR Travel Journal, DSR Notes |

The DSR web apps have no fixed package name, so DSR Apps 4 finds them by
home-screen name when the widget is first set up, and otherwise opens their
GitHub Pages address. Apps that aren't installed open their Play Store page.

## Battery and memory

The widgets are designed to cost nothing while they sit on the home screen:

- No update timer (`updatePeriodMillis="0"`), no services, alarms, wake locks,
  background listeners, network access or permissions.
- The widget is drawn only when it is added, its settings are saved, or the
  APK is updated. The home screen keeps the result; this app's process is not
  running the rest of the time.
- Taps are pending intents that the home screen fires straight at the target
  app, without starting this app.
- Built-in gold icons and apps' own icons are sent as resource references, so
  no image data is copied; only generated gold icons are small bitmaps.
- No libraries (plain Android framework); the APK is under 100 KB.

## Install

1. On the phone, open the repo's **Releases** → **DSR Apps widget (latest build)**
   and download `DSR-Apps-Widget.apk` (rebuilt by GitHub Actions on every push
   that touches this folder).
2. Open the APK and allow "Install unknown apps" for your browser when asked.
3. Long-press an empty spot on the home screen → **Widgets** → **DSR Apps**,
   and drag a widget onto the screen.

## Build locally

```
cd DSRAppsWidget
./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk
```
