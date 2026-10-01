package com.dsr.appswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.List;

/**
 * A row of 5-10 app tiles. All the work happens in {@link #update}, which runs only when the
 * widget is added, its settings are saved, or this app is updated: there is no timer, service
 * or background listener, and taps go straight from the home screen to the target app.
 */
public abstract class BaseAppsWidget extends AppWidgetProvider {

    private static final int GOLD = 0xFFE8B830;

    /** Size generated icons are drawn at: about the largest a tile shows them. */
    static final int ICON_DP = 44;

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int id : widgetIds) update(context, manager, id);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int widgetId,
            Bundle newOptions) {
        update(context, manager, widgetId); // resized: refit the icons
    }

    /**
     * Icon size in dp that fits a tile of this widget, or 0 to use the stretchy layout (Android
     * before 12, or the home screen hasn't reported the widget's size).
     */
    private static float fittedIconDp(AppWidgetManager manager, int widgetId, int count) {
        if (Build.VERSION.SDK_INT < 31) return 0;
        Bundle size = manager.getAppWidgetOptions(widgetId);
        // In portrait a widget is its minimum width by its maximum height.
        int width = size.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int height = size.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        if (width <= 0 || height <= 0) return 0;
        float tileWidth = (width - 10f) / count - 6f;   // frame padding, tile margins
        float tileHeight = height - 10f - 6f;
        float icon = Math.min(tileWidth - 8f, tileHeight - 8f - 18f); // tile padding, label space
        return icon >= 16f ? icon : 0;
    }

    @Override
    public void onDeleted(Context context, int[] widgetIds) {
        for (int id : widgetIds) WidgetConfig.delete(context, id);
    }

    static void update(Context context, AppWidgetManager manager, int widgetId) {
        AppWidgetProviderInfo info = manager.getAppWidgetInfo(widgetId);
        if (info == null) return;
        WidgetConfig config = WidgetConfig.get(context, widgetId, info.provider.getClassName());
        IconRenderer icons = new IconRenderer(context, ICON_DP);
        String pkg = context.getPackageName();

        float iconDp = fittedIconDp(manager, widgetId, config.count);

        RemoteViews root = new RemoteViews(pkg, R.layout.widget_root);
        root.removeAllViews(R.id.row);
        for (int i = 0; i < config.count; i++) {
            WidgetConfig.Entry e = config.entries[i];
            RemoteViews tile = new RemoteViews(pkg, iconDp > 0 ? R.layout.tile_fit : R.layout.tile);
            if (iconDp > 0) {
                tile.setViewLayoutWidth(R.id.icon, iconDp, TypedValue.COMPLEX_UNIT_DIP);
                tile.setViewLayoutHeight(R.id.icon, iconDp, TypedValue.COMPLEX_UNIT_DIP);
            }
            int builtIn = e == null ? 0 : icons.builtInResource(e, config.gold);
            Bitmap mask = e == null || builtIn != 0 || !config.gold ? null : icons.goldMask(e);
            Icon icon = e == null || builtIn != 0 || config.gold ? null : icons.icon(e, false);
            if (builtIn != 0) {
                tile.setImageViewResource(R.id.icon, builtIn);
            } else if (mask != null) {
                tile.setImageViewBitmap(R.id.icon, mask);
                tile.setInt(R.id.icon, "setColorFilter", GOLD);
            } else if (icon != null) {
                tile.setImageViewIcon(R.id.icon, icon);
            } else {
                tile.setImageViewResource(R.id.icon, e == null ? R.drawable.ic_add : R.drawable.ic_missing);
            }
            tile.setTextViewText(R.id.label, e == null || e.label == null ? "Add" : e.label);
            Intent intent = tapIntent(context, e, widgetId);
            tile.setOnClickPendingIntent(R.id.tile, PendingIntent.getActivity(context,
                    widgetId * WidgetConfig.MAX_COUNT + i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            root.addView(R.id.row, tile);
        }
        manager.updateAppWidget(widgetId, root);
    }

    /** Opens the app's home-screen entry, or null if the app isn't installed. */
    private static Intent launcherIntent(Context context, PackageManager pm, WidgetConfig.Entry e) {
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(e.pkg);
        List<ResolveInfo> entries = pm.queryIntentActivities(launcher, 0);
        if (entries.isEmpty()) return null;
        String cls = e.cls != null && WidgetConfig.activityExists(pm, e.pkg, e.cls)
                ? e.cls : entries.get(0).activityInfo.name;
        int allEntries = pm.queryIntentActivities(launcher, PackageManager.MATCH_DISABLED_COMPONENTS).size();
        if (allEntries > entries.size()) {
            // The app has switched-off home-screen entries it can swap in (Duolingo does this for
            // its seasonal icons), which can happen without the widget being redrawn. Such tiles
            // look up the app's current entry when tapped.
            return LaunchActivity.intent(context, e.pkg, cls);
        }
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(e.pkg, cls);
    }

    private static Intent tapIntent(Context context, WidgetConfig.Entry e, int widgetId) {
        PackageManager pm = context.getPackageManager();
        Intent intent = null;
        if (e != null && e.url != null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(e.url));
            if (e.pkg != null) {
                // Open the link inside its app if the app handles it. Otherwise open a web link
                // in the browser, or just open the app (e.g. claude:// on an older Claude app).
                intent.setPackage(e.pkg);
                if (pm.resolveActivity(intent, 0) == null) {
                    boolean web = e.url.startsWith("http");
                    intent = web ? intent.setPackage(null) : pm.getLaunchIntentForPackage(e.pkg);
                }
            }
        } else if (e != null && e.pkg != null) {
            intent = launcherIntent(context, pm, e);
            if (intent == null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + e.pkg));
            }
        }
        if (intent == null) {
            // Empty tile, or an app that can't be found: open this widget's settings.
            intent = ConfigActivity.intent(context, widgetId);
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
