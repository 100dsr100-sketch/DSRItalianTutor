package com.dsr.appswidget;

/** Starting apps for each of the four widgets (used until the widget is configured). */
final class Presets {

    static final class Tile {
        final String label;
        final String icon;
        final String url;
        final String[] packages;
        final String[] names;

        Tile(String label, String icon, String url, String[] packages, String... names) {
            this.label = label;
            this.icon = icon;
            this.url = url;
            this.packages = packages;
            this.names = names;
        }
    }

    private static Tile app(String label, String icon, String... packages) {
        return new Tile(label, icon, null, packages);
    }

    private static Tile web(String label, String icon, String url, String... names) {
        return new Tile(label, icon, url, new String[0], names);
    }

    private static final String PAGES = "https://100dsr100-sketch.github.io/";

    static Tile[] forProvider(String className) {
        if (className.endsWith("AppsWidgetEmptyProvider")) return new Tile[0];   // all tiles empty
        if (className.endsWith("AppsWidget2Provider")) {
            return new Tile[] {
                    new Tile("Claude", "claude", WidgetConfig.CLAUDE_CODE, new String[] {WidgetConfig.CLAUDE}),
                    app("Duolingo", "duolingo", "com.duolingo"),
                    app("Clock", "clock", "com.sec.android.app.clockpackage", "com.google.android.deskclock"),
                    app("Calc", "calculator", "com.sec.android.app.popupcalculator", "com.google.android.calculator"),
                    app("Translate", "translate", "com.google.android.apps.translate"),
                    app("Anker", "soundcore", "com.oceanwing.soundcore"),
                    app("Plex", "plex", "com.plexapp.android"),
            };
        }
        if (className.endsWith("AppsWidget3Provider")) {
            return new Tile[] {
                    app("CommBank", "commbank", "com.commbank.netbank"),
                    app("Binance", "binance", "com.binance.dev"),
                    app("CommSec", "commsec", "au.com.commsec.android.CommSec"),
                    app("Macquarie", "macquarie", "au.com.macquarie.banking"),
                    app("Pocket", "pocket", "au.com.commsec.android.CommSecPocket"),
                    app("Messenger", "messenger", "com.facebook.orca"),
                    app("Greater", "greater", "com.greater.Greater"),
            };
        }
        if (className.endsWith("AppsWidget4Provider")) {
            return new Tile[] {
                    web("Travel", "travelc", null, "DSR Travelc"),
                    web("Dictation", "dictation", PAGES + "DSRDictation/", "Dictation", "DSR Dictation"),
                    web("Media", "media", PAGES + "DSROnlineMediaApp/", "DSR Media", "DSR Online Media"),
                    web("Research", "research", PAGES + "DSRResearcher/", "DSR Researcher"),
                    web("Sound", "sound", null, "DSR Sound"),
                    web("Journal", "journal", PAGES + "DSRTravelJournal/", "DSR Travel Journal", "DSR Journal"),
                    web("Notes", "notes", PAGES + "DSRNotes/", "DSR Notes"),
            };
        }
        return new Tile[] {
                app("Keep", "keep", "com.google.android.keep"),
                app("Drive", "drive", "com.google.android.apps.docs"),
                app("Photos", "photos", "com.google.android.apps.photos"),
                app("Gallery", "gallery", "com.sec.android.gallery3d"),
                app("Music", "music", "com.sec.android.app.music"),
                app("Health", "health", "com.sec.android.app.shealth"),
                app("Files", "files", "com.sec.android.app.myfiles"),
        };
    }

    private Presets() {}
}
