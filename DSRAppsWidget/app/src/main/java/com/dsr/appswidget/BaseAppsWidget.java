package com.dsr.appswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.RemoteViews;

/** A row of tiles; each tile opens the first installed package from its list. */
public abstract class BaseAppsWidget extends AppWidgetProvider {

    protected abstract int layout();

    protected abstract int[] tiles();

    /** Per tile: candidate packages, in order of preference. */
    protected abstract String[][] packages();

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layout());
        int[] tiles = tiles();
        String[][] packages = packages();
        for (int i = 0; i < tiles.length; i++) {
            views.setOnClickPendingIntent(tiles[i], launchIntent(context, packages[i], tiles[i]));
        }
        manager.updateAppWidget(widgetIds, views);
    }

    private static PendingIntent launchIntent(Context context, String[] candidates, int requestCode) {
        PackageManager pm = context.getPackageManager();
        Intent intent = null;
        for (String pkg : candidates) {
            intent = pm.getLaunchIntentForPackage(pkg);
            if (intent != null) break;
        }
        if (intent == null) {
            // App not installed: open its Play Store page instead.
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + candidates[0]));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
