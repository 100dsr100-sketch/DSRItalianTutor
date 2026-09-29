package com.dsr.appswidget;

/** DSR Apps 2: Claude, Duolingo, Clock, Calculator, Translate, Soundcore, Plex. */
public class AppsWidget2Provider extends BaseAppsWidget {

    @Override
    protected int layout() {
        return R.layout.apps_widget_2;
    }

    @Override
    protected int[] tiles() {
        return new int[] {
                R.id.tile_claude, R.id.tile_duolingo, R.id.tile_clock,
                R.id.tile_calculator, R.id.tile_translate, R.id.tile_soundcore, R.id.tile_plex,
        };
    }

    @Override
    protected String[][] packages() {
        // Clock and Calculator fall back to the Google versions if Samsung's are missing.
        return new String[][] {
                {"com.anthropic.claude"},
                {"com.duolingo"},
                {"com.sec.android.app.clockpackage", "com.google.android.deskclock"},
                {"com.sec.android.app.popupcalculator", "com.google.android.calculator"},
                {"com.google.android.apps.translate"},
                {"com.oceanwing.soundcore"},
                {"com.plexapp.android"},
        };
    }
}
