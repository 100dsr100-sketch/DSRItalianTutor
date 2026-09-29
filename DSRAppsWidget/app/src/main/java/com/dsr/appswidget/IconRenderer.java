package com.dsr.appswidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

/**
 * A tile's icon, in gold or in the app's own colours. Built-in gold icons and apps' own icons
 * are passed as resource references (the home screen loads them itself, nothing is copied);
 * only gold versions of other apps are drawn into a small bitmap.
 */
final class IconRenderer {

    private static final int GOLD = 0xFFE8B830;

    private final Context context;
    private final PackageManager pm;
    private final int size;

    IconRenderer(Context context, int sizeDp) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.size = Math.round(sizeDp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * This app's own drawable to show, or 0. Widgets set these with setImageViewResource, which
     * the home screen loads through the widget's own resources (no package lookup needed).
     */
    int builtInResource(WidgetConfig.Entry e, boolean gold) {
        int builtIn = Icons.drawable(e.icon);
        if (builtIn == 0) return 0;
        return gold || appIconResource(e) == 0 ? builtIn : 0;
    }

    /** Null if there is nothing to show (app not installed and no built-in icon). */
    Icon icon(WidgetConfig.Entry e, boolean gold) {
        int builtIn = Icons.drawable(e.icon);
        if (gold && builtIn != 0) return Icon.createWithResource(context, builtIn);
        if (!gold) {
            int res = appIconResource(e);
            if (res != 0) return Icon.createWithResource(e.pkg, res);
            if (builtIn != 0) return Icon.createWithResource(context, builtIn);
        }
        Bitmap bitmap = render(e, gold);
        return bitmap == null ? null : Icon.createWithBitmap(bitmap);
    }

    private Bitmap render(WidgetConfig.Entry e, boolean gold) {
        Drawable app = appIcon(e);
        if (app == null) return null;
        if (gold) {
            if (Build.VERSION.SDK_INT >= 33 && app instanceof AdaptiveIconDrawable) {
                // Android 13+ "themed icon" layer: a one-colour glyph made for exactly this.
                Drawable mono = ((AdaptiveIconDrawable) app).getMonochrome();
                if (mono != null) {
                    mono = mono.mutate();
                    mono.setTint(GOLD);
                    return draw(mono, true);
                }
            }
            return goldify(draw(app, false));
        }
        return draw(app, false);
    }

    private int appIconResource(WidgetConfig.Entry e) {
        if (e.pkg == null) return 0;
        try {
            return e.cls != null
                    ? pm.getActivityInfo(new ComponentName(e.pkg, e.cls), 0).getIconResource()
                    : pm.getApplicationInfo(e.pkg, 0).icon;
        } catch (PackageManager.NameNotFoundException notInstalled) {
            return 0;
        }
    }

    private Drawable appIcon(WidgetConfig.Entry e) {
        if (e.pkg == null) return null;
        try {
            return e.cls != null
                    ? pm.getActivityIcon(new ComponentName(e.pkg, e.cls))
                    : pm.getApplicationIcon(e.pkg);
        } catch (PackageManager.NameNotFoundException notInstalled) {
            return null;
        }
    }

    /** @param adaptiveLayer true for a 108dp adaptive-icon layer whose visible part is the middle 72dp */
    private Bitmap draw(Drawable d, boolean adaptiveLayer) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        int inset = adaptiveLayer ? size / 4 : 0;
        d.setBounds(-inset, -inset, size + inset, size + inset);
        d.draw(new Canvas(bitmap));
        return bitmap;
    }

    /**
     * Turns a full-colour icon into a gold glyph: the most common colour is taken as the
     * background, and each pixel becomes gold in proportion to how much it differs from it.
     */
    private Bitmap goldify(Bitmap icon) {
        int n = size * size;
        int[] px = new int[n];
        icon.getPixels(px, 0, size, 0, 0, size, size);

        int[] buckets = new int[32];
        for (int p : px) {
            if ((p >>> 24) > 200) buckets[luma(p) >> 3]++;
        }
        int bg = 0;
        for (int i = 1; i < buckets.length; i++) {
            if (buckets[i] > buckets[bg]) bg = i;
        }
        int bgLuma = bg * 8 + 4;

        int[] glyph = new int[n];
        long glyphSum = 0, alphaSum = 0;
        for (int i = 0; i < n; i++) {
            int alpha = px[i] >>> 24;
            glyph[i] = Math.min(255, Math.max(0, Math.abs(luma(px[i]) - bgLuma) * 3 - 60)) * alpha / 255;
            glyphSum += glyph[i];
            alphaSum += alpha;
        }
        // Almost no contrasting detail (e.g. a one-colour logo): use the icon's outline instead.
        boolean silhouette = glyphSum * 16 < alphaSum;

        for (int i = 0; i < n; i++) {
            int a = silhouette ? px[i] >>> 24 : glyph[i];
            px[i] = (a << 24) | (GOLD & 0xFFFFFF);
        }
        icon.setPixels(px, 0, size, 0, 0, size, size);
        return icon;
    }

    private static int luma(int p) {
        return (((p >> 16) & 0xFF) * 77 + ((p >> 8) & 0xFF) * 150 + (p & 0xFF) * 29) >> 8;
    }
}
