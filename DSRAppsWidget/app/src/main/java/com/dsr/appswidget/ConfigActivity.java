package com.dsr.appswidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/** Settings for one widget: icons per row, icon colour, and the app on each tile. */
public class ConfigActivity extends Activity {

    private static final int GOLD = 0xFFE8B830;
    private static final int GOLD_DIM = 0xFF4A3D17;

    private int widgetId;
    private WidgetConfig config;
    private LinearLayout slots;
    private TextView countText;
    private IconRenderer icons;
    private volatile List<AppItem> apps; // loaded in the background for the picker

    private static final class AppItem {
        final String pkg, cls, label;
        Drawable icon; // loaded when first shown

        AppItem(String pkg, String cls, String label) {
            this.pkg = pkg;
            this.cls = cls;
            this.label = label;
        }
    }

    static Intent intent(Context context, int widgetId) {
        return new Intent(context, ConfigActivity.class)
                .setData(Uri.parse("dsrwidget://config/" + widgetId)) // one task per widget
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        setResult(RESULT_CANCELED, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId);
        if (info == null || !getPackageName().equals(info.provider.getPackageName())) {
            finish();
            return;
        }
        config = WidgetConfig.get(this, widgetId, info.provider.getClassName());
        icons = new IconRenderer(this, 32);
        buildUi();
        loadAppsInBackground();
    }

    private void buildUi() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(24), dp(20), dp(24));

        page.addView(text("Widget settings", 24, true));

        page.addView(heading("Icons per row"));
        LinearLayout stepper = new LinearLayout(this);
        stepper.setGravity(Gravity.CENTER_VERTICAL);
        Button minus = button("−");
        Button plus = button("+");
        countText = text("", 22, true);
        countText.setGravity(Gravity.CENTER);
        stepper.addView(minus);
        stepper.addView(countText, new LinearLayout.LayoutParams(dp(64), ViewGroup.LayoutParams.WRAP_CONTENT));
        stepper.addView(plus);
        minus.setOnClickListener(v -> setCount(config.count - 1));
        plus.setOnClickListener(v -> setCount(config.count + 1));
        page.addView(stepper);

        page.addView(heading("Icon colour"));
        RadioGroup colour = new RadioGroup(this);
        colour.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton gold = radio("Gold");
        RadioButton original = radio("App colours");
        colour.addView(gold);
        colour.addView(original);
        (config.gold ? gold : original).setChecked(true);
        colour.setOnCheckedChangeListener((g, checked) -> {
            config.gold = checked == gold.getId();
            refreshSlots();
        });
        page.addView(colour);

        page.addView(heading("Apps (tap to change)"));
        slots = new LinearLayout(this);
        slots.setOrientation(LinearLayout.VERTICAL);
        page.addView(slots);

        Button save = button("Save");
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        saveParams.topMargin = dp(20);
        page.addView(save, saveParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF050505);
        scroll.addView(page);
        setContentView(scroll);
        setCount(config.count);
    }

    private void setCount(int count) {
        config.count = Math.max(WidgetConfig.MIN_COUNT, Math.min(WidgetConfig.MAX_COUNT, count));
        countText.setText(String.valueOf(config.count));
        refreshSlots();
    }

    private void refreshSlots() {
        slots.removeAllViews();
        for (int i = 0; i < config.count; i++) {
            final int slot = i;
            WidgetConfig.Entry e = config.entries[i];

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(6), dp(8));
            row.setBackground(outline());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(6);

            ImageView icon = new ImageView(this);
            android.graphics.drawable.Icon tileIcon = e == null ? null : icons.icon(e, config.gold);
            if (tileIcon != null) icon.setImageIcon(tileIcon);
            else icon.setImageResource(e == null ? R.drawable.ic_add : R.drawable.ic_missing);
            row.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));

            TextView label = text((i + 1) + ".  " + (e == null ? "Empty" : e.label), 17, false);
            label.setPadding(dp(14), 0, 0, 0);
            row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            if (i > 0) {
                Button up = button("▲");
                up.setOnClickListener(v -> {
                    WidgetConfig.Entry t = config.entries[slot - 1];
                    config.entries[slot - 1] = config.entries[slot];
                    config.entries[slot] = t;
                    refreshSlots();
                });
                row.addView(up, new LinearLayout.LayoutParams(dp(48), dp(44)));
            }
            row.setOnClickListener(v -> pickApp(slot));
            slots.addView(row, rowParams);
        }
    }

    private void loadAppsInBackground() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<AppItem> list = new ArrayList<>();
            for (ResolveInfo r : WidgetConfig.launchers(pm)) {
                if (r.activityInfo.packageName.equals(getPackageName())) continue;
                list.add(new AppItem(r.activityInfo.packageName, r.activityInfo.name,
                        r.loadLabel(pm).toString()));
            }
            Collator collator = Collator.getInstance();
            list.sort((a, b) -> collator.compare(a.label, b.label));
            apps = list;
        }, "load-apps").start();
    }

    private void pickApp(int slot) {
        List<AppItem> list = apps;
        if (list == null) {
            Toast.makeText(this, "Loading apps…", Toast.LENGTH_SHORT).show();
            return;
        }
        PackageManager pm = getPackageManager();
        BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return list.size() + 1; }
            @Override public Object getItem(int i) { return i == 0 ? null : list.get(i - 1); }
            @Override public long getItemId(int i) { return i; }

            @Override
            public View getView(int i, View view, ViewGroup parent) {
                LinearLayout row = (LinearLayout) view;
                if (row == null) {
                    row = new LinearLayout(ConfigActivity.this);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(16), dp(10), dp(16), dp(10));
                    row.addView(new ImageView(ConfigActivity.this), new LinearLayout.LayoutParams(dp(36), dp(36)));
                    TextView t = text("", 17, false);
                    t.setPadding(dp(16), 0, 0, 0);
                    row.addView(t);
                }
                ImageView icon = (ImageView) row.getChildAt(0);
                TextView label = (TextView) row.getChildAt(1);
                if (i == 0) {
                    icon.setImageResource(R.drawable.ic_add);
                    label.setText("Empty tile");
                } else {
                    AppItem app = list.get(i - 1);
                    if (app.icon == null) {
                        try {
                            app.icon = pm.getActivityIcon(new android.content.ComponentName(app.pkg, app.cls));
                        } catch (PackageManager.NameNotFoundException gone) {
                            app.icon = pm.getDefaultActivityIcon();
                        }
                    }
                    icon.setImageDrawable(app.icon);
                    label.setText(app.label);
                }
                return row;
            }
        };
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Tile " + (slot + 1))
                .setAdapter(adapter, (d, which) -> {
                    if (which == 0) {
                        config.entries[slot] = null;
                    } else {
                        AppItem app = list.get(which - 1);
                        config.entries[slot] = WidgetConfig.Entry.app(app.pkg, app.cls, app.label);
                    }
                    refreshSlots();
                })
                .show();
    }

    private void save() {
        config.save(this, widgetId);
        BaseAppsWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }

    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(GOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    private TextView heading(String s) {
        TextView t = text(s, 15, true);
        t.setAlpha(0.75f);
        t.setPadding(0, dp(22), 0, dp(8));
        return t;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(GOLD);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setBackground(outline());
        b.setAllCaps(false);
        return b;
    }

    private RadioButton radio(String s) {
        RadioButton r = new RadioButton(this);
        r.setId(View.generateViewId());
        r.setText(s);
        r.setTextColor(GOLD);
        r.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        r.setButtonTintList(android.content.res.ColorStateList.valueOf(GOLD));
        r.setPadding(dp(4), 0, dp(20), 0);
        return r;
    }

    private GradientDrawable outline() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(0xFF0E0E0E);
        g.setStroke(dp(1), GOLD_DIM);
        g.setCornerRadius(dp(14));
        return g;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
