package com.dsr.appswidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
import java.util.Locale;

/** One widget's saved setup: how many tiles, icon colour, and what each tile opens. */
final class WidgetConfig {

    static final int MIN_COUNT = 5;
    static final int MAX_COUNT = 10;

    private static final String PREFS = "widgets";

    /** What a tile opens. Exactly one of cls (an app screen) or url is normally set. */
    static final class Entry {
        String pkg;   // app package, may be null for a plain web link
        String cls;   // launcher activity, null for links / apps not installed yet
        String url;   // web link, opened in pkg if it handles it
        String label; // text under the icon
        String icon;  // built-in gold icon key (see Icons), may be null

        static Entry app(String pkg, String cls, String label) {
            Entry e = new Entry();
            e.pkg = pkg;
            e.cls = cls;
            e.label = shortLabel(label);
            e.icon = Icons.keyFor(pkg, label);
            return e;
        }
    }

    int count = 7;
    boolean gold = true;
    final Entry[] entries = new Entry[MAX_COUNT];

    /** The widget's saved setup, or its preset (resolved against installed apps and saved). */
    static WidgetConfig get(Context context, int widgetId, String provider) {
        WidgetConfig config = load(context, widgetId);
        if (config == null) {
            config = fromPreset(context, Presets.forProvider(provider));
            config.save(context, widgetId);
        }
        return config;
    }

    private static WidgetConfig load(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String apps = prefs.getString("apps_" + widgetId, null);
        if (apps == null) return null;
        WidgetConfig config = new WidgetConfig();
        config.count = prefs.getInt("count_" + widgetId, 7);
        config.gold = prefs.getBoolean("gold_" + widgetId, true);
        String[] lines = apps.split("\n", -1);
        for (int i = 0; i < MAX_COUNT && i < lines.length; i++) {
            String[] f = lines[i].split("\t", -1);
            if (f.length < 5) continue; // empty slot (the prefs file may pad it with spaces)
            Entry e = new Entry();
            e.pkg = orNull(f[0]);
            e.cls = orNull(f[1]);
            e.url = orNull(f[2]);
            e.label = orNull(f[3]);
            e.icon = orNull(f[4]);
            config.entries[i] = e;
        }
        return config;
    }

    void save(Context context, int widgetId) {
        StringBuilder apps = new StringBuilder();
        for (int i = 0; i < MAX_COUNT; i++) {
            if (i > 0) apps.append('\n');
            Entry e = entries[i];
            if (e == null) continue;
            apps.append(str(e.pkg)).append('\t').append(str(e.cls)).append('\t')
                    .append(str(e.url)).append('\t').append(str(e.label)).append('\t')
                    .append(str(e.icon));
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("count_" + widgetId, count)
                .putBoolean("gold_" + widgetId, gold)
                .putString("apps_" + widgetId, apps.toString())
                .apply();
    }

    static void delete(Context context, int widgetId) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .remove("count_" + widgetId)
                .remove("gold_" + widgetId)
                .remove("apps_" + widgetId)
                .apply();
    }

    private static WidgetConfig fromPreset(Context context, Presets.Tile[] tiles) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> launchers = null; // loaded only if a tile needs a lookup by name
        WidgetConfig config = new WidgetConfig();
        for (int i = 0; i < tiles.length; i++) {
            Presets.Tile t = tiles[i];
            Entry e = new Entry();
            e.label = t.label;
            e.icon = t.icon;
            e.url = t.url;
            for (String pkg : t.packages) {
                Intent launch = pm.getLaunchIntentForPackage(pkg);
                if (launch != null && launch.getComponent() != null) {
                    e.pkg = pkg;
                    e.cls = launch.getComponent().getClassName();
                    break;
                }
            }
            if (e.cls == null && t.names.length > 0) {
                if (launchers == null) launchers = launchers(pm);
                ComponentName c = findByName(pm, launchers, t.names);
                if (c != null) {
                    e.pkg = c.getPackageName();
                    e.cls = c.getClassName();
                    e.url = null; // the installed web app itself beats its web address
                }
            }
            if (e.pkg == null && t.packages.length > 0) e.pkg = t.packages[0];
            if (e.url != null && e.cls != null) e.cls = null; // link tiles open the link
            config.entries[i] = e;
        }
        return config;
    }

    static List<ResolveInfo> launchers(PackageManager pm) {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(query, 0);
    }

    /** Exact label match first, then "starts with". */
    private static ComponentName findByName(PackageManager pm, List<ResolveInfo> apps, String[] names) {
        String[] labels = new String[apps.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = apps.get(i).loadLabel(pm).toString().toLowerCase(Locale.ROOT);
        }
        for (boolean exact : new boolean[] {true, false}) {
            for (String name : names) {
                String wanted = name.toLowerCase(Locale.ROOT);
                for (int i = 0; i < labels.length; i++) {
                    if (exact ? labels[i].equals(wanted) : labels[i].startsWith(wanted)) {
                        return new ComponentName(apps.get(i).activityInfo.packageName,
                                apps.get(i).activityInfo.name);
                    }
                }
            }
        }
        return null;
    }

    /** "Samsung Health" -> "Health", "Anker soundcore" -> "Anker". */
    static String shortLabel(String label) {
        String s = label.trim();
        for (String prefix : new String[] {"DSR ", "Samsung ", "Google ", "Galaxy "}) {
            if (s.regionMatches(true, 0, prefix, 0, prefix.length()) && s.length() > prefix.length()) {
                s = s.substring(prefix.length()).trim();
            }
        }
        if (s.length() > 9 && s.indexOf(' ') > 0) s = s.substring(0, s.indexOf(' '));
        return s;
    }

    private static String orNull(String s) {
        return s.isEmpty() ? null : s;
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }
}
