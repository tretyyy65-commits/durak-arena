package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class UiKit {
    private UiKit() {}

    public static final int BG = Color.rgb(2, 8, 6);
    public static final int PANEL = Color.rgb(6, 18, 13);
    public static final int PANEL_2 = Color.rgb(9, 27, 20);
    public static final int GOLD = Color.rgb(201, 154, 78);
    public static final int GOLD_LIGHT = Color.rgb(241, 215, 155);
    public static final int TEXT = Color.rgb(245, 241, 230);
    public static final int MUTED = Color.rgb(158, 169, 162);
    public static final int RED = Color.rgb(122, 31, 31);

    public static int dp(Context c, int value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable panel(Context c, int fill, int stroke, int strokeWidth, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(c, radius));
        if (strokeWidth > 0) d.setStroke(dp(c, strokeWidth), stroke);
        return d;
    }

    public static GradientDrawable gradient(Context c, int top, int bottom, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, bottom}
        );
        d.setCornerRadius(dp(c, radius));
        d.setStroke(dp(c, 1), stroke);
        return d;
    }

    public static TextView text(Context c, String value, int size, boolean bold, int gravity, int color) {
        TextView v = new TextView(c);
        v.setText(value == null ? "" : value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(gravity);
        v.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return v;
    }

    public static ImageView icon(Context c, String drawableName, String contentDescription) {
        ImageView v = new ImageView(c);
        v.setImageDrawable(new GameIconDrawable(drawableName));
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setContentDescription(contentDescription);
        v.setPadding(dp(c, 2), dp(c, 2), dp(c, 2), dp(c, 2));
        return v;
    }

    public static LinearLayout navButton(Context c, String drawableName, String title, boolean active) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(c, 3), dp(c, 5), dp(c, 3), dp(c, 4));
        box.setBackground(panel(
                c,
                active ? Color.rgb(14, 42, 30) : Color.argb(238, 4, 14, 10),
                active ? GOLD_LIGHT : Color.rgb(92, 76, 48),
                1,
                10
        ));

        ImageView icon = icon(c, drawableName, title);
        box.addView(icon, new LinearLayout.LayoutParams(dp(c, 27), dp(c, 27)));

        TextView t = text(c, title, 9, active, Gravity.CENTER, active ? GOLD_LIGHT : TEXT);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(c, 20)
        );
        box.addView(t, tlp);
        return box;
    }

    public static TextView button(Context c, String title, boolean primary) {
        TextView b = text(c, title, primary ? 18 : 15, true, Gravity.CENTER, GOLD_LIGHT);
        b.setClickable(true);
        b.setFocusable(true);
        b.setPadding(dp(c, 10), dp(c, 8), dp(c, 10), dp(c, 8));
        if (primary) {
            GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.rgb(30, 93, 63),
                            Color.rgb(17, 62, 43),
                            Color.rgb(7, 31, 22)
                    }
            );
            bg.setCornerRadius(dp(c, 18));
            bg.setStroke(dp(c, 2), GOLD);
            b.setBackground(bg);
        } else {
            b.setBackground(panel(c, Color.rgb(5, 19, 14), GOLD, 1, 12));
        }
        return b;
    }

    public static int familyOverlay(int cups) {
        return Color.argb(62, 5, 46, 30);
    }

    public static String arenaTitleForCups(int cups) {
        switch (LeagueSystem.familyForIndex(LeagueSystem.indexForCups(cups))) {
            case 0: return "ЗАЛ НОВАЧКІВ";
            case 1: return "СМАРАГДОВИЙ КЛУБ";
            case 2: return "СИНЯ ЗАЛА ПРОФІ";
            case 3: return "ІМПЕРСЬКА АРЕНА";
            default: return "ЗАЛА ЛЕГЕНД";
        }
    }

    public static FrameLayout imageRoot(Context c, int drawableRes, int cups, int dimAlpha) {
        FrameLayout root = new FrameLayout(c);
        root.setBackgroundColor(BG);

        ImageView bg = new ImageView(c);
        bg.setImageResource(drawableRes);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(bg, new FrameLayout.LayoutParams(-1, -1));

        android.view.View wash = new android.view.View(c);
        wash.setBackgroundColor(familyOverlay(cups));
        root.addView(wash, new FrameLayout.LayoutParams(-1, -1));

        android.view.View dim = new android.view.View(c);
        dim.setBackgroundColor(Color.argb(dimAlpha, 0, 0, 0));
        root.addView(dim, new FrameLayout.LayoutParams(-1, -1));
        return root;
    }
}
