package com.dsr.appswidget;

/** DSR Apps: Keep, Drive, Photos, Gallery, Music, Health, Files. */
public class AppsWidgetProvider extends BaseAppsWidget {

    @Override
    protected int layout() {
        return R.layout.apps_widget;
    }

    @Override
    protected int[] tiles() {
        return new int[] {
                R.id.tile_keep, R.id.tile_drive, R.id.tile_photos,
                R.id.tile_gallery, R.id.tile_music, R.id.tile_health, R.id.tile_files,
        };
    }

    @Override
    protected String[][] packages() {
        return new String[][] {
                {"com.google.android.keep"},
                {"com.google.android.apps.docs"},
                {"com.google.android.apps.photos"},
                {"com.sec.android.gallery3d"},
                {"com.sec.android.app.music"},
                {"com.sec.android.app.shealth"},
                {"com.sec.android.app.myfiles"},
        };
    }
}
