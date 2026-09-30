package com.dsr.appswidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

/**
 * Invisible step for tiles of apps that swap their home-screen entry (e.g. Duolingo's seasonal
 * icons): opens whichever entry the app has switched on at the moment of the tap.
 */
public class LaunchActivity extends Activity {

    private static final String EXTRA_PKG = "pkg";
    private static final String EXTRA_CLS = "cls";

    static Intent intent(Context context, String pkg, String cls) {
        return new Intent(context, LaunchActivity.class)
                .setData(Uri.parse("dsrwidget://launch/" + pkg)) // one pending intent per app
                .putExtra(EXTRA_PKG, pkg)
                .putExtra(EXTRA_CLS, cls);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String pkg = getIntent().getStringExtra(EXTRA_PKG);
        String cls = getIntent().getStringExtra(EXTRA_CLS);
        PackageManager pm = getPackageManager();
        Intent target;
        if (cls != null && WidgetConfig.activityExists(pm, pkg, cls)) {
            target = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(pkg, cls);
        } else {
            target = pm.getLaunchIntentForPackage(pkg);
            if (target == null) target = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
        }
        startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
        finish();
    }
}
