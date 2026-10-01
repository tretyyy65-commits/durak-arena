package com.nexora.durakarena;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Dynamic Glory Path drawn over the approved casino reference artwork.
 *
 * The dealer/table atmosphere remains artwork, while every league-related part
 * is live UI: current badge, next badge, progress, tier buttons, colors and locks.
 * The same LeagueSystem is used by Profile so both screens stay synchronized.
 */
public class GloryPathView extends FrameLayout {

    private final Context context;
    private final int playerCups;
    private final Runnable onBack;

    private static final int GOLD = Color.rgb(239, 178, 64);
    private static final int GOLD_LIGHT = Color.rgb(255, 221, 132);
    private static final int PANEL = Color.argb(238, 2, 16, 13);
    private static final int PANEL_SOFT = Color.argb(220, 1, 12, 10);
    private static final int TEXT = Color.rgb(245, 235, 207);
    private static final int MUTED = Color.rgb(137, 143, 139);

    public GloryPathView(Context context, int playerCups, Runnable onBack) {
        super(context);
        this.context = context;
        this.playerCups = Math.max(0, playerCups);
        this.onBack = onBack;
        build();
    }

    private void build() {
        removeAllViews();
        setBackgroundColor(Color.BLACK);
        setClipChildren(false);
        setClipToPadding(false);

        final int currentIndex = LeagueSystem.indexForCups(playerCups);
        final int nextIndex = Math.min(LeagueSystem.NAMES.length - 1, currentIndex + 1);
        final int currentFamily = LeagueSystem.familyForIndex(currentIndex);
        final int leagueColor = LeagueSystem.familyColorForIndex(currentIndex);

        // -------------------------------------------------------------
        // APPROVED REFERENCE ARTWORK: dealer, casino, table and lighting.
        // Dynamic league UI is layered above this image.
        // -------------------------------------------------------------
        ImageView artwork = new ImageView(context);
        artwork.setBackgroundColor(Color.BLACK);
        artwork.setScaleType(ImageView.ScaleType.FIT_XY);
        artwork.setAdjustViewBounds(false);

        int artworkId = context.getResources().getIdentifier(
                "glory_path_reference",
                "drawable",
                context.getPackageName()
        );
        if (artworkId != 0) artwork.setImageResource(artworkId);
        addView(artwork, new FrameLayout.LayoutParams(-1, -1));

        // Soft masks hide the baked rank graphics while preserving the scene.
        View centerMask = new View(context);
        centerMask.setBackground(panel(Color.argb(214, 2, 25, 19), GOLD, 1, 34));
        addView(centerMask, new FrameLayout.LayoutParams(1, 1));

        View pathMask = new View(context);
        pathMask.setBackground(panel(PANEL_SOFT, Color.argb(175, 138, 99, 41), 1, 10));
        addView(pathMask, new FrameLayout.LayoutParams(1, 1));

        // -------------------------------------------------------------
        // CURRENT LEAGUE — live rank image + live name.
        // -------------------------------------------------------------
        FrameLayout currentBadgeHolder = new FrameLayout(context);
        currentBadgeHolder.setBackground(circle(Color.argb(210, 0, 11, 9), leagueColor, 2));
        currentBadgeHolder.setPadding(dp(4), dp(4), dp(4), dp(4));
        ImageView currentBadge = rankImage(currentIndex, 1f);
        currentBadgeHolder.addView(currentBadge, new FrameLayout.LayoutParams(-1, -1));
        addView(currentBadgeHolder, new FrameLayout.LayoutParams(1, 1));

        TextView currentName = text(
                LeagueSystem.nameForCups(playerCups),
                17,
                true,
                Gravity.CENTER,
                leagueColor
        );
        currentName.setShadowLayer(dp(4), 0, dp(1), Color.BLACK);
        addView(currentName, new FrameLayout.LayoutParams(1, 1));

        // Next rank preview on the right.
        FrameLayout nextBadgeHolder = new FrameLayout(context);
        nextBadgeHolder.setBackground(circle(Color.argb(210, 0, 11, 9),
                LeagueSystem.familyColorForIndex(nextIndex), 1));
        nextBadgeHolder.setPadding(dp(3), dp(3), dp(3), dp(3));
        nextBadgeHolder.addView(rankImage(nextIndex, 0.95f), new FrameLayout.LayoutParams(-1, -1));
        addView(nextBadgeHolder, new FrameLayout.LayoutParams(1, 1));

        // -------------------------------------------------------------
        // LIVE PATH PROGRESS — e.g. 765 / 800, automatically updated.
        // -------------------------------------------------------------
        FrameLayout progressBox = new FrameLayout(context);
        progressBox.setBackground(panel(Color.argb(245, 1, 14, 11), GOLD_LIGHT, 2, 10));

        ProgressBar progress = new ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleHorizontal
        );
        int displayTarget = Math.max(1, LeagueSystem.displayProgressTarget(playerCups));
        progress.setMax(displayTarget);
        progress.setProgress(Math.min(playerCups, displayTarget));
        progress.setProgressTintList(ColorStateList.valueOf(leagueColor));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(20, 30, 25)));
        progressBox.addView(progress, new FrameLayout.LayoutParams(-1, -1));

        TextView progressText = text(
                LeagueSystem.displayProgressValue(playerCups) + " / " + displayTarget,
                12,
                true,
                Gravity.CENTER,
                Color.WHITE
        );
        progressText.setShadowLayer(dp(3), 0, dp(1), Color.BLACK);
        progressBox.addView(progressText, new FrameLayout.LayoutParams(-1, -1));
        addView(progressBox, new FrameLayout.LayoutParams(1, 1));

        ImageView trophyLeft = icon("nexora_trophy", 1f);
        ImageView trophyRight = icon("nexora_trophy", 1f);
        addView(trophyLeft, new FrameLayout.LayoutParams(1, 1));
        addView(trophyRight, new FrameLayout.LayoutParams(1, 1));

        // Arrows remain code UI too.
        TextView leftArrow = text("‹", 58, true, Gravity.CENTER, GOLD_LIGHT);
        TextView rightArrow = text("›", 58, true, Gravity.CENTER, GOLD_LIGHT);
        leftArrow.setShadowLayer(dp(5), 0, dp(2), Color.BLACK);
        rightArrow.setShadowLayer(dp(5), 0, dp(2), Color.BLACK);
        addView(leftArrow, new FrameLayout.LayoutParams(1, 1));
        addView(rightArrow, new FrameLayout.LayoutParams(1, 1));

        // -------------------------------------------------------------
        // FIVE LEAGUE FAMILIES — live icons, tier I/II/III and state.
        // -------------------------------------------------------------
        LinearLayout familyRow = new LinearLayout(context);
        familyRow.setOrientation(LinearLayout.HORIZONTAL);
        familyRow.setGravity(Gravity.CENTER);
        familyRow.setPadding(dp(5), dp(5), dp(5), dp(3));
        familyRow.setBackground(panel(PANEL, GOLD, 1, 10));

        for (int family = 0; family < 5; family++) {
            final int f = family;
            familyRow.addView(
                    familyCell(f, currentFamily, currentIndex),
                    new LinearLayout.LayoutParams(0, -1, 1f)
            );
        }
        addView(familyRow, new FrameLayout.LayoutParams(1, 1));

        // -------------------------------------------------------------
        // REWARD ROW. Icons remain code assets so they can later change by season.
        // -------------------------------------------------------------
        LinearLayout rewards = new LinearLayout(context);
        rewards.setOrientation(LinearLayout.HORIZONTAL);
        rewards.setGravity(Gravity.CENTER);
        rewards.setPadding(dp(5), dp(5), dp(5), dp(5));
        rewards.setBackground(panel(PANEL, GOLD, 2, 10));
        rewards.addView(rewardCell("nexora_coin"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_crystal"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_events"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_profile"), new LinearLayout.LayoutParams(0, -1, 1f));
        addView(rewards, new FrameLayout.LayoutParams(1, 1));

        // Invisible back zone, preserving the exact visual reference.
        View backZone = new View(context);
        backZone.setBackgroundColor(Color.TRANSPARENT);
        backZone.setClickable(true);
        backZone.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });
        addView(backZone, new FrameLayout.LayoutParams(1, 1));

        // -------------------------------------------------------------
        // Responsive placement based on the approved 9:16 composition.
        // -------------------------------------------------------------
        addOnLayoutChangeListener((v, l, t, r, b, ol, ot, orr, ob) -> {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            place(centerMask, w, h, .335f, .305f, .345f, .255f);
            place(currentBadgeHolder, w, h, .354f, .318f, .292f, .210f);
            place(currentName, w, h, .315f, .490f, .370f, .035f);

            place(leftArrow, w, h, .245f, .385f, .080f, .075f);
            place(rightArrow, w, h, .692f, .385f, .080f, .075f);
            place(nextBadgeHolder, w, h, .790f, .405f, .170f, .125f);

            place(trophyLeft, w, h, .275f, .505f, .055f, .040f);
            place(progressBox, w, h, .335f, .510f, .365f, .034f);
            place(trophyRight, w, h, .760f, .505f, .055f, .040f);

            place(pathMask, w, h, .025f, .565f, .950f, .195f);
            place(familyRow, w, h, .020f, .575f, .960f, .185f);
            place(rewards, w, h, .035f, .772f, .930f, .160f);

            place(backZone, w, h, 0f, 0f, .15f, .11f);
        });
    }

    private View familyCell(int family, int currentFamily, int currentIndex) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(1), dp(1), dp(1), dp(1));

        int representativeIndex;
        if (family == currentFamily) {
            representativeIndex = currentIndex;
        } else {
            representativeIndex = family * 3 + 1;
        }

        float alpha = family > currentFamily ? 0.78f : 1f;
        ImageView badge = rankImage(representativeIndex, alpha);
        cell.addView(badge, new LinearLayout.LayoutParams(dp(60), dp(60)));

        TextView familyName = text(
                LeagueSystem.FAMILY_NAMES[family],
                9,
                true,
                Gravity.CENTER,
                LeagueSystem.familyColorForIndex(family * 3)
        );
        familyName.setSingleLine(true);
        cell.addView(familyName, new LinearLayout.LayoutParams(-1, dp(16)));

        LinearLayout tiers = new LinearLayout(context);
        tiers.setOrientation(LinearLayout.HORIZONTAL);
        tiers.setGravity(Gravity.CENTER);

        for (int tier = 0; tier < 3; tier++) {
            int index = family * 3 + tier;
            boolean active = index == currentIndex;
            boolean completed = index < currentIndex;
            boolean locked = index > currentIndex;
            int color = LeagueSystem.familyColorForIndex(index);

            TextView tierText = text(
                    new String[]{"I", "II", "III"}[tier],
                    10,
                    true,
                    Gravity.CENTER,
                    locked ? MUTED : (active ? Color.WHITE : GOLD_LIGHT)
            );
            tierText.setBackground(panel(
                    active ? Color.argb(245, 8, 38, 27) : Color.argb(232, 3, 18, 15),
                    active ? color : Color.argb(205, 134, 99, 44),
                    active ? 2 : 1,
                    4
            ));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(20), dp(22));
            tlp.leftMargin = dp(1);
            tlp.rightMargin = dp(1);
            tiers.addView(tierText, tlp);
        }
        cell.addView(tiers, new LinearLayout.LayoutParams(-2, dp(24)));

        TextView state = text(
                family < currentFamily ? "✓" : family == currentFamily ? "●" : "🔒",
                15,
                true,
                Gravity.CENTER,
                family < currentFamily ? GOLD_LIGHT
                        : family == currentFamily ? LeagueSystem.familyColorForIndex(currentIndex)
                        : MUTED
        );
        cell.addView(state, new LinearLayout.LayoutParams(dp(30), dp(24)));
        return cell;
    }

    private View rewardCell(String drawableName) {
        FrameLayout cell = new FrameLayout(context);
        cell.setBackground(panel(Color.argb(205, 2, 14, 12), Color.rgb(122, 87, 35), 1, 7));
        ImageView image = icon(drawableName, 1f);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(60), dp(60));
        lp.gravity = Gravity.CENTER;
        cell.addView(image, lp);
        return cell;
    }

    private ImageView rankImage(int index, float alpha) {
        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int id = context.getResources().getIdentifier(
                LeagueSystem.assetForIndex(index),
                "drawable",
                context.getPackageName()
        );
        if (id != 0) image.setImageResource(id);
        image.setAlpha(alpha);
        return image;
    }

    private ImageView icon(String drawableName, float alpha) {
        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int id = context.getResources().getIdentifier(
                drawableName,
                "drawable",
                context.getPackageName()
        );
        if (id != 0) image.setImageResource(id);
        image.setAlpha(alpha);
        return image;
    }

    private TextView text(String value, int size, boolean bold, int gravity, int color) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(gravity);
        view.setIncludeFontPadding(false);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable panel(int fill, int stroke, int width, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        if (width > 0) d.setStroke(dp(width), stroke);
        return d;
    }

    private GradientDrawable circle(int fill, int stroke, int width) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fill);
        if (width > 0) d.setStroke(dp(width), stroke);
        return d;
    }

    private void place(View view, int screenW, int screenH,
                       float x, float y, float width, float height) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.width = Math.max(1, Math.round(screenW * width));
        lp.height = Math.max(1, Math.round(screenH * height));
        lp.leftMargin = Math.round(screenW * x);
        lp.topMargin = Math.round(screenH * y);
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        view.setLayoutParams(lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
