package com.dsr.appswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class AppsWidgetProvider extends AppWidgetProvider {

    // Tile view id -> package it opens.
    private static final int[] TILES = {
            R.id.tile_keep, R.id.tile_drive, R.id.tile_photos,
            R.id.tile_gallery, R.id.tile_music, R.id.tile_health,
    };
    private static final String[] PACKAGES = {
            "com.google.android.keep",
            "com.google.android.apps.docs",
            "com.google.android.apps.photos",
            "com.sec.android.gallery3d",
            "com.sec.android.app.music",
            "com.sec.android.app.shealth",
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.apps_widget);
        for (int i = 0; i < TILES.length; i++) {
            views.setOnClickPendingIntent(TILES[i], launchIntent(context, PACKAGES[i], i));
        }
        manager.updateAppWidget(widgetIds, views);
    }

    private static PendingIntent launchIntent(Context context, String pkg, int requestCode) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent == null) {
            // App not installed: open its Play Store page instead.
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
