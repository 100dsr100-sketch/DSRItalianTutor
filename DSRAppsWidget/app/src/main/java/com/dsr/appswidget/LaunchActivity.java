package com.dsr.appswidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * Invisible trampoline for tiles whose app has no fixed package name (e.g. web apps
 * installed from Chrome). Finds an installed app by its home-screen name at tap time,
 * falling back to the web address.
 */
public class LaunchActivity extends Activity {

    private static final String EXTRA_NAMES = "names";
    private static final String EXTRA_URL = "url";

    /** @param names app labels to look for; exact matches win over prefix matches */
    static Intent intent(Context context, String url, String... names) {
        return new Intent(context, LaunchActivity.class)
                .putExtra(EXTRA_NAMES, names)
                .putExtra(EXTRA_URL, url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] names = getIntent().getStringArrayExtra(EXTRA_NAMES);
        String url = getIntent().getStringExtra(EXTRA_URL);

        Intent target = findApp(names);
        if (target == null && url != null) {
            target = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        if (target != null) {
            target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(target);
        } else {
            Toast.makeText(this, names[0] + " isn't installed", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private Intent findApp(String[] names) {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(query, 0);
        for (boolean exact : new boolean[] {true, false}) {
            for (String name : names) {
                String wanted = name.toLowerCase(Locale.ROOT);
                for (ResolveInfo app : apps) {
                    ActivityInfo info = app.activityInfo;
                    if (info.packageName.equals(getPackageName())) continue;
                    String label = app.loadLabel(pm).toString().toLowerCase(Locale.ROOT);
                    if (exact ? label.equals(wanted) : label.startsWith(wanted)) {
                        return new Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setClassName(info.packageName, info.name);
                    }
                }
            }
        }
        return null;
    }
}
