# DSR Apps widgets

Two home-screen widgets for the Galaxy S22 Ultra in the same style as the DSR
power switch: a gold outlined rounded frame with seven dark tiles, each with a
gold icon and label. Both come in the one APK.

### DSR Apps

| Tile    | Opens                  | Package                          |
|---------|------------------------|----------------------------------|
| Keep    | Google Keep            | `com.google.android.keep`        |
| Drive   | Google Drive           | `com.google.android.apps.docs`   |
| Photos  | Google Photos          | `com.google.android.apps.photos` |
| Gallery | Samsung Gallery        | `com.sec.android.gallery3d`      |
| Music   | Samsung Music          | `com.sec.android.app.music`      |
| Health  | Samsung Health         | `com.sec.android.app.shealth`    |
| Files   | Samsung My Files       | `com.sec.android.app.myfiles`    |

### DSR Apps 2

| Tile      | Opens                               | Package                                                        |
|-----------|-------------------------------------|----------------------------------------------------------------|
| Claude    | Claude                              | `com.anthropic.claude`                                         |
| Duolingo  | Duolingo                            | `com.duolingo`                                                 |
| Clock     | Samsung Clock (else Google Clock)   | `com.sec.android.app.clockpackage`, `com.google.android.deskclock` |
| Calc      | Samsung Calculator (else Google)    | `com.sec.android.app.popupcalculator`, `com.google.android.calculator` |
| Translate | Google Translate                    | `com.google.android.apps.translate`                            |
| Anker     | Anker Soundcore                     | `com.oceanwing.soundcore`                                      |
| Plex      | Plex                                | `com.plexapp.android`                                          |

If an app isn't installed, its tile opens the Play Store page for it.

![preview](preview.png)

## Install

1. On the phone, open the repo's **Releases** → **DSR Apps widget (latest build)**
   and download `DSR-Apps-Widget.apk` (rebuilt by GitHub Actions on every push
   that touches this folder).
2. Open the APK and allow "Install unknown apps" for your browser when asked.
3. Long-press an empty spot on the home screen → **Widgets** → **DSR Apps** (or **DSR Apps 2**),
   and drag it onto the screen. It fills the full width (5 columns) by 1 row and can be resized.

## Build locally

```
cd DSRAppsWidget
./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk
```
