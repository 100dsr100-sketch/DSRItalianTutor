package com.dsr.appswidget;

import android.content.Context;
import android.content.Intent;

/** DSR Apps 4: the DSR web apps, found by home-screen name (they have no fixed package). */
public class AppsWidget4Provider extends BaseAppsWidget {

    private static final String PAGES = "https://100dsr100-sketch.github.io/";

    @Override
    protected int layout() {
        return R.layout.apps_widget_4;
    }

    @Override
    protected int[] tiles() {
        return new int[] {
                R.id.tile_travelc, R.id.tile_dictation, R.id.tile_media,
                R.id.tile_research, R.id.tile_sound, R.id.tile_journal, R.id.tile_notes,
        };
    }

    @Override
    protected Intent tileIntent(Context context, int i) {
        switch (i) {
            case 0: return LaunchActivity.intent(context, null, "DSR Travelc");
            case 1: return LaunchActivity.intent(context, PAGES + "DSRDictation/", "Dictation", "DSR Dictation");
            case 2: return LaunchActivity.intent(context, PAGES + "DSROnlineMediaApp/", "DSR Media", "DSR Online Media");
            case 3: return LaunchActivity.intent(context, PAGES + "DSRResearcher/", "DSR Researcher");
            case 4: return LaunchActivity.intent(context, null, "DSR Sound");
            case 5: return LaunchActivity.intent(context, PAGES + "DSRTravelJournal/", "DSR Travel Journal", "DSR Journal");
            default: return LaunchActivity.intent(context, PAGES + "DSRNotes/", "DSR Notes");
        }
    }
}
