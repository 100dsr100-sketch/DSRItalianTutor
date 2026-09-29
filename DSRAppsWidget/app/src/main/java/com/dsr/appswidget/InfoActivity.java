package com.dsr.appswidget;

import android.app.Activity;
import android.os.Bundle;

/** Launcher screen that just explains how to add the widget. */
public class InfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }
}
