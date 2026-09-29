package com.dsr.appswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.RemoteViews;

/** A row of tiles; each tile opens a link or the first installed package from its list. */
public abstract class BaseAppsWidget extends AppWidgetProvider {

    protected abstract int layout();

    protected abstract int[] tiles();

    /** Per tile: candidate packages, in order of preference. */
    protected String[][] packages() {
        return null;
    }

    /** Per tile: optional https link to open (in the tile's app if it handles it), or null. */
    protected String[] links() {
        return new String[tiles().length];
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layout());
        int[] tiles = tiles();
        for (int i = 0; i < tiles.length; i++) {
            Intent intent = tileIntent(context, i);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            views.setOnClickPendingIntent(tiles[i], PendingIntent.getActivity(context, tiles[i], intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        manager.updateAppWidget(widgetIds, views);
    }

    /** What tapping tile {@code i} launches. Defaults to {@link #packages()} and {@link #links()}. */
    protected Intent tileIntent(Context context, int i) {
        String link = links()[i];
        String[] candidates = packages()[i];
        return link != null ? linkIntent(context, link, candidates[0]) : appIntent(context, candidates);
    }

    /** Opens the link in the given app if it can handle it, otherwise in the browser. */
    private static Intent linkIntent(Context context, String link, String pkg) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setPackage(pkg);
        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
            intent.setPackage(null);
        }
        return intent;
    }

    private static Intent appIntent(Context context, String[] candidates) {
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
        return intent;
    }
}
