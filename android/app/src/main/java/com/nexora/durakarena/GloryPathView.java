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
 * Premium single-screen Glory Path inspired by the approved Durak Arena mockup.
 * No long vertical list: the whole progression is visible as one cinematic scene.
 */
public class GloryPathView extends FrameLayout {

    private final Context context;
    private final int playerCups;
    private final Runnable onBack;

    private final int GOLD = Color.rgb(236, 176, 66);
    private final int GOLD_LIGHT = Color.rgb(255, 220, 132);
    private final int PANEL = Color.argb(222, 3, 18, 15);
    private final int PANEL_SOFT = Color.argb(190, 2, 14, 12);
    private final int TEXT = Color.rgb(246, 234, 204);
    private final int MUTED = Color.rgb(159, 158, 145);

    public GloryPathView(Context context, int playerCups, Runnable onBack) {
        super(context);
        this.context = context;
        this.playerCups = Math.max(0, playerCups);
        this.onBack = onBack;
        build();
    }

    private void build() {
        removeAllViews();
        setBackgroundColor(Color.rgb(1, 7, 6));
        setClipChildren(false);
        setClipToPadding(false);

        final int current = LeagueSystem.indexForCups(playerCups);
        final int next = Math.min(LeagueSystem.NAMES.length - 1, current + 1);

        // -----------------------------------------------------------------
        // CINEMATIC BACKGROUND
        // -----------------------------------------------------------------
        ImageView bg = new ImageView(context);
        int bgId = context.getResources().getIdentifier(
                "main_menu_background",
                "drawable",
                context.getPackageName()
        );
        if (bgId != 0) bg.setImageResource(bgId);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(bg, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        View shade = new View(context);
        shade.setBackgroundColor(Color.argb(72, 0, 10, 8));
        addView(shade, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        // -----------------------------------------------------------------
        // BACK BUTTON — top left, like approved mockup
        // -----------------------------------------------------------------
        TextView back = text("‹", 54, true, Gravity.CENTER, GOLD_LIGHT);
        back.setShadowLayer(dp(5), 0, 0, Color.BLACK);
        back.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });
        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(dp(58), dp(58));
        backLp.gravity = Gravity.TOP | Gravity.LEFT;
        backLp.leftMargin = dp(16);
        backLp.topMargin = dp(18);
        addView(back, backLp);

        // -----------------------------------------------------------------
        // INFO CIRCLE — top right
        // -----------------------------------------------------------------
        TextView infoButton = text("i", 34, true, Gravity.CENTER, GOLD_LIGHT);
        infoButton.setTypeface(Typeface.SERIF, Typeface.BOLD);
        infoButton.setBackground(circle(Color.argb(215, 5, 18, 16), GOLD_LIGHT, 2));
        FrameLayout.LayoutParams infoLp = new FrameLayout.LayoutParams(dp(58), dp(58));
        infoLp.gravity = Gravity.TOP | Gravity.RIGHT;
        infoLp.rightMargin = dp(18);
        infoLp.topMargin = dp(18);
        addView(infoButton, infoLp);

        // -----------------------------------------------------------------
        // SMALL REWARD/INFO PANEL — top right
        // -----------------------------------------------------------------
        LinearLayout miniPanel = new LinearLayout(context);
        miniPanel.setOrientation(LinearLayout.VERTICAL);
        miniPanel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        miniPanel.setPadding(dp(10), dp(7), dp(8), dp(7));
        miniPanel.setBackground(panel(PANEL_SOFT, GOLD, 2, 9));

        miniPanel.addView(miniIcon("nexora_trophy"), new LinearLayout.LayoutParams(dp(34), dp(34)));
        miniPanel.addView(miniIcon("nexora_events"), new LinearLayout.LayoutParams(dp(34), dp(34)));
        miniPanel.addView(miniIcon("nexora_leaders"), new LinearLayout.LayoutParams(dp(34), dp(34)));

        FrameLayout.LayoutParams miniLp = new FrameLayout.LayoutParams(dp(136), dp(118));
        miniLp.gravity = Gravity.TOP | Gravity.RIGHT;
        miniLp.rightMargin = dp(20);
        miniLp.topMargin = dp(88);
        addView(miniPanel, miniLp);

        // -----------------------------------------------------------------
        // CENTRAL CURRENT RANK EMBLEM
        // -----------------------------------------------------------------
        FrameLayout currentHolder = new FrameLayout(context);
        ImageView currentBadge = rankImage(current, false);
        currentHolder.addView(currentBadge, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        FrameLayout.LayoutParams currentLp = new FrameLayout.LayoutParams(dp(182), dp(182));
        currentLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        currentLp.topMargin = dp(222);
        addView(currentHolder, currentLp);

        // Left / right navigation chevrons
        TextView left = text("‹", 72, true, Gravity.CENTER, GOLD_LIGHT);
        TextView right = text("›", 72, true, Gravity.CENTER, GOLD_LIGHT);
        left.setShadowLayer(dp(4), 0, 0, Color.BLACK);
        right.setShadowLayer(dp(4), 0, 0, Color.BLACK);

        FrameLayout.LayoutParams leftLp = new FrameLayout.LayoutParams(dp(62), dp(84));
        leftLp.gravity = Gravity.TOP | Gravity.LEFT;
        leftLp.leftMargin = dp(46);
        leftLp.topMargin = dp(270);
        addView(left, leftLp);

        FrameLayout.LayoutParams rightLp = new FrameLayout.LayoutParams(dp(62), dp(84));
        rightLp.gravity = Gravity.TOP | Gravity.RIGHT;
        rightLp.rightMargin = dp(92);
        rightLp.topMargin = dp(270);
        addView(right, rightLp);

        // Next rank preview on the right
        if (next != current) {
            ImageView nextBadge = rankImage(next, true);
            FrameLayout.LayoutParams nextLp = new FrameLayout.LayoutParams(dp(86), dp(86));
            nextLp.gravity = Gravity.TOP | Gravity.RIGHT;
            nextLp.rightMargin = dp(16);
            nextLp.topMargin = dp(302);
            addView(nextBadge, nextLp);
        }

        // -----------------------------------------------------------------
        // PROGRESS BAR under current emblem
        // -----------------------------------------------------------------
        LinearLayout progressRow = new LinearLayout(context);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        progressRow.setPadding(dp(4), 0, dp(4), 0);

        ImageView trophyLeft = miniIcon("nexora_trophy");
        progressRow.addView(trophyLeft, new LinearLayout.LayoutParams(dp(32), dp(32)));

        ProgressBar bar = new ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        int start = LeagueSystem.startForCups(playerCups);
        int nextStart = LeagueSystem.nextStartForCups(playerCups);
        int span = Math.max(1, nextStart - start);
        int value = Math.max(0, Math.min(span, playerCups - start));

        bar.setMax(span);
        bar.setProgress(value);
        bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(45, 235, 80)));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(23, 28, 24)));
        bar.setBackground(panel(Color.argb(230, 2, 13, 11), GOLD_LIGHT, 2, 9));

        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(0, dp(24), 1f);
        barLp.leftMargin = dp(7);
        barLp.rightMargin = dp(7);
        progressRow.addView(bar, barLp);

        ImageView trophyRight = miniIcon("nexora_trophy");
        progressRow.addView(trophyRight, new LinearLayout.LayoutParams(dp(32), dp(32)));

        FrameLayout.LayoutParams progressLp = new FrameLayout.LayoutParams(dp(276), dp(38));
        progressLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        progressLp.topMargin = dp(404);
        addView(progressRow, progressLp);

        TextView currentName = text(
                LeagueSystem.NAMES[current],
                16,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );
        currentName.setShadowLayer(dp(3), 0, 0, Color.BLACK);
        FrameLayout.LayoutParams nameLp = new FrameLayout.LayoutParams(dp(220), dp(28));
        nameLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        nameLp.topMargin = dp(445);
        addView(currentName, nameLp);

        // -----------------------------------------------------------------
        // FIVE MAIN LEAGUE FAMILIES — one horizontal row
        // -----------------------------------------------------------------
        LinearLayout leagueRow = new LinearLayout(context);
        leagueRow.setOrientation(LinearLayout.HORIZONTAL);
        leagueRow.setGravity(Gravity.CENTER);
        leagueRow.setPadding(dp(8), dp(4), dp(8), dp(2));
        leagueRow.setBackground(Color.argb(82, 0, 8, 7));

        int currentFamily = current / 3;
        for (int family = 0; family < 5; family++) {
            leagueRow.addView(
                    familyCell(family, currentFamily, current),
                    new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            );
        }

        FrameLayout.LayoutParams leaguesLp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(148)
        );
        leaguesLp.gravity = Gravity.BOTTOM;
        leaguesLp.bottomMargin = dp(112);
        addView(leagueRow, leaguesLp);

        // -----------------------------------------------------------------
        // REWARDS BAR — 4 large premium icons at bottom
        // -----------------------------------------------------------------
        LinearLayout rewards = new LinearLayout(context);
        rewards.setOrientation(LinearLayout.HORIZONTAL);
        rewards.setGravity(Gravity.CENTER);
        rewards.setPadding(dp(8), dp(8), dp(8), dp(8));
        rewards.setBackground(panel(PANEL, GOLD, 2, 10));

        rewards.addView(rewardCell("nexora_coin"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_crystal"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_events"), new LinearLayout.LayoutParams(0, -1, 1f));
        rewards.addView(rewardCell("nexora_profile"), new LinearLayout.LayoutParams(0, -1, 1f));

        FrameLayout.LayoutParams rewardsLp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(96)
        );
        rewardsLp.gravity = Gravity.BOTTOM;
        rewardsLp.leftMargin = dp(14);
        rewardsLp.rightMargin = dp(14);
        rewardsLp.bottomMargin = dp(8);
        addView(rewards, rewardsLp);
    }

    private View familyCell(int family, int currentFamily, int currentIndex) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(2), dp(2), dp(2), dp(1));

        int representative = family * 3 + 1;
        ImageView badge = rankImage(representative, family > currentFamily);
        cell.addView(badge, new LinearLayout.LayoutParams(dp(55), dp(55)));

        LinearLayout tiers = new LinearLayout(context);
        tiers.setOrientation(LinearLayout.HORIZONTAL);
        tiers.setGravity(Gravity.CENTER);

        String[] numerals = {"I", "II", "III"};
        for (int tier = 0; tier < 3; tier++) {
            int index = family * 3 + tier;
            boolean active = index == currentIndex;
            boolean completed = index < currentIndex;
            int familyColor = displayFamilyColor(family);

            TextView t = text(
                    numerals[tier],
                    10,
                    true,
                    Gravity.CENTER,
                    active ? Color.WHITE : (completed ? GOLD_LIGHT : TEXT)
            );
            t.setBackground(panel(
                    active ? Color.argb(235, 10, 52, 31) : Color.argb(205, 8, 19, 17),
                    active ? familyColor : Color.argb(190, 135, 107, 58),
                    active ? 2 : 1,
                    4
            ));

            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(19), dp(22));
            tlp.leftMargin = dp(1);
            tlp.rightMargin = dp(1);
            tiers.addView(t, tlp);
        }
        cell.addView(tiers, new LinearLayout.LayoutParams(-2, dp(24)));

        TextView state = text(
                family < currentFamily ? "✓" : (family == currentFamily ? "●" : "🔒"),
                18,
                true,
                Gravity.CENTER,
                family < currentFamily ? GOLD_LIGHT :
                        (family == currentFamily ? Color.rgb(47, 239, 93) : MUTED)
        );
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(34), dp(28));
        slp.topMargin = dp(2);
        cell.addView(state, slp);

        return cell;
    }

    private View rewardCell(String drawableName) {
        FrameLayout box = new FrameLayout(context);
        box.setPadding(dp(5), dp(5), dp(5), dp(5));
        box.setBackground(panel(Color.argb(205, 2, 15, 13), Color.rgb(131, 96, 39), 1, 7));

        ImageView icon = miniIcon(drawableName);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(62), dp(62));
        lp.gravity = Gravity.CENTER;
        box.addView(icon, lp);
        return box;
    }

    private ImageView rankImage(int index, boolean dim) {
        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int id = context.getResources().getIdentifier(
                LeagueSystem.assetForIndex(index),
                "drawable",
                context.getPackageName()
        );

        if (id != 0) image.setImageResource(id);
        image.setAlpha(dim ? 0.68f : 1f);
        return image;
    }

    private ImageView miniIcon(String drawableName) {
        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int id = context.getResources().getIdentifier(
                drawableName,
                "drawable",
                context.getPackageName()
        );
        if (id != 0) image.setImageResource(id);
        return image;
    }

    private int displayFamilyColor(int family) {
        switch (family) {
            case 0: return Color.rgb(210, 126, 61);      // bronze novice
            case 1: return Color.rgb(45, 225, 103);     // green pro
            case 2: return Color.rgb(59, 155, 255);     // blue master
            case 3: return Color.rgb(190, 69, 255);     // purple elite
            default: return Color.rgb(255, 67, 58);     // red legend
        }
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
