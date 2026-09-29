package com.dsr.appswidget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Hand-drawn gold icons, looked up by key, app package or app name. */
final class Icons {

    private static final Map<String, Integer> BY_KEY = new HashMap<>();
    private static final Map<String, String> KEY_BY_PACKAGE = new HashMap<>();
    // Web apps installed from Chrome get random package names, so match those by name.
    private static final String[][] KEY_BY_NAME_PREFIX = {
            {"dsr travelc", "travelc"}, {"dictation", "dictation"}, {"dsr dictation", "dictation"},
            {"dsr media", "media"}, {"dsr online media", "media"}, {"dsr research", "research"},
            {"dsr sound", "sound"}, {"dsr travel journal", "journal"}, {"dsr journal", "journal"},
            {"dsr notes", "notes"},
    };

    static {
        key("keep", R.drawable.ic_keep, "com.google.android.keep");
        key("drive", R.drawable.ic_drive, "com.google.android.apps.docs");
        key("photos", R.drawable.ic_photos, "com.google.android.apps.photos");
        key("gallery", R.drawable.ic_gallery, "com.sec.android.gallery3d");
        key("music", R.drawable.ic_music, "com.sec.android.app.music");
        key("health", R.drawable.ic_health, "com.sec.android.app.shealth");
        key("files", R.drawable.ic_files, "com.sec.android.app.myfiles");
        key("claude", R.drawable.ic_claude, "com.anthropic.claude");
        key("duolingo", R.drawable.ic_duolingo, "com.duolingo");
        key("clock", R.drawable.ic_clock, "com.sec.android.app.clockpackage", "com.google.android.deskclock");
        key("calculator", R.drawable.ic_calculator,
                "com.sec.android.app.popupcalculator", "com.google.android.calculator");
        key("translate", R.drawable.ic_translate, "com.google.android.apps.translate");
        key("soundcore", R.drawable.ic_soundcore, "com.oceanwing.soundcore");
        key("plex", R.drawable.ic_plex, "com.plexapp.android");
        key("commbank", R.drawable.ic_commbank, "com.commbank.netbank");
        key("binance", R.drawable.ic_binance, "com.binance.dev");
        key("commsec", R.drawable.ic_commsec, "au.com.commsec.android.CommSec");
        key("macquarie", R.drawable.ic_macquarie, "au.com.macquarie.banking");
        key("pocket", R.drawable.ic_pocket, "au.com.commsec.android.CommSecPocket");
        key("messenger", R.drawable.ic_messenger, "com.facebook.orca");
        key("greater", R.drawable.ic_greater, "com.greater.Greater");
        key("travelc", R.drawable.ic_travelc);
        key("dictation", R.drawable.ic_dictation);
        key("media", R.drawable.ic_media);
        key("research", R.drawable.ic_research);
        key("sound", R.drawable.ic_sound);
        key("journal", R.drawable.ic_journal);
        key("notes", R.drawable.ic_notes);
    }

    private static void key(String key, int drawable, String... packages) {
        BY_KEY.put(key, drawable);
        for (String pkg : packages) KEY_BY_PACKAGE.put(pkg, key);
    }

    /** Drawable for a key, or 0. */
    static int drawable(String key) {
        Integer id = key == null ? null : BY_KEY.get(key);
        return id == null ? 0 : id;
    }

    /** Built-in icon key for an app, or null. */
    static String keyFor(String pkg, String label) {
        String key = KEY_BY_PACKAGE.get(pkg);
        if (key != null || label == null) return key;
        String name = label.toLowerCase(Locale.ROOT);
        for (String[] pair : KEY_BY_NAME_PREFIX) {
            if (name.startsWith(pair[0])) return pair[1];
        }
        return null;
    }

    private Icons() {}
}
