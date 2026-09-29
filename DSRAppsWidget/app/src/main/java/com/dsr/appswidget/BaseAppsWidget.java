package com.dsr.appswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * A row of 5-10 app tiles. All the work happens in {@link #update}, which runs only when the
 * widget is added, its settings are saved, or this app is updated: there is no timer, service
 * or background listener, and taps go straight from the home screen to the target app.
 */
public abstract class BaseAppsWidget extends AppWidgetProvider {

    static final int ICON_DP = 26;

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int id : widgetIds) update(context, manager, id);
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

        RemoteViews root = new RemoteViews(pkg, R.layout.widget_root);
        root.removeAllViews(R.id.row);
        for (int i = 0; i < config.count; i++) {
            WidgetConfig.Entry e = config.entries[i];
            RemoteViews tile = new RemoteViews(pkg, R.layout.tile);
            Icon icon = e == null ? null : icons.icon(e, config.gold);
            if (icon != null) {
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

    private static Intent tapIntent(Context context, WidgetConfig.Entry e, int widgetId) {
        PackageManager pm = context.getPackageManager();
        Intent intent = null;
        if (e != null && e.url != null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(e.url));
            if (e.pkg != null) {
                // Open the link inside its app if the app handles it, otherwise in the browser.
                intent.setPackage(e.pkg);
                if (pm.resolveActivity(intent, 0) == null) intent.setPackage(null);
            }
        } else if (e != null && e.pkg != null) {
            if (e.cls != null && exists(pm, e.pkg, e.cls)) {
                intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(e.pkg, e.cls);
            } else {
                intent = pm.getLaunchIntentForPackage(e.pkg);
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + e.pkg));
                }
            }
        }
        if (intent == null) {
            // Empty tile, or an app that can't be found: open this widget's settings.
            intent = ConfigActivity.intent(context, widgetId);
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }

    private static boolean exists(PackageManager pm, String pkg, String cls) {
        try {
            pm.getActivityInfo(new ComponentName(pkg, cls), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
