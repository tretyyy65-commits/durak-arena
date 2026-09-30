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
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Durak Arena league road.
 *
 * Important: rank art is the same lightweight drawable artwork already used by
 * the Profile screen (rank_novice, rank_player, rank_pro, rank_master,
 * rank_elite, rank_legend). No Blender/GLB/Filament rendering is used here.
 */
public class GloryPathView extends FrameLayout {

    private final Context context;
    private final int playerCups;
    private final Runnable onBack;

    private final int BG = Color.rgb(2, 8, 7);
    private final int PANEL = Color.rgb(4, 18, 14);
    private final int PANEL_CURRENT = Color.rgb(7, 29, 22);
    private final int GOLD = Color.rgb(211, 163, 76);
    private final int GOLD_LIGHT = Color.rgb(246, 211, 139);
    private final int TEXT = Color.rgb(239, 226, 193);
    private final int MUTED = Color.rgb(155, 149, 132);
    private final int LOCKED = Color.rgb(105, 103, 95);
    private final int LINE_DARK = Color.rgb(69, 60, 43);

    public GloryPathView(Context context, int playerCups, Runnable onBack) {
        super(context);
        this.context = context;
        this.playerCups = Math.max(0, playerCups);
        this.onBack = onBack;
        build();
    }

    private void build() {
        setBackgroundColor(BG);

        // =====================================================
        // FIXED HEADER
        // =====================================================
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), 0, dp(12), 0);
        header.setBackground(solidPanel(BG, GOLD, 1, 0));

        TextView back = makeText("‹", 40, true, Gravity.CENTER, GOLD_LIGHT);
        back.setClickable(true);
        back.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });
        header.addView(back, new LinearLayout.LayoutParams(dp(54), LayoutParams.MATCH_PARENT));

        TextView headerTitle = makeText("ШЛЯХ СЛАВИ", 20, true, Gravity.CENTER, GOLD_LIGHT);
        header.addView(headerTitle, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

        TextView cupsTop = makeText("🏆 " + playerCups, 12, true, Gravity.CENTER, GOLD_LIGHT);
        header.addView(cupsTop, new LinearLayout.LayoutParams(dp(65), LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams headerLP = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(68)
        );
        headerLP.gravity = Gravity.TOP;
        addView(header, headerLP);

        // =====================================================
        // SCROLL CONTENT
        // =====================================================
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(12), dp(18), dp(12), dp(55));

        TextView intro = makeText(
                "ВІД НОВАЧКА ДО ЛЕГЕНДИ",
                13,
                true,
                Gravity.CENTER,
                GOLD
        );
        content.addView(intro, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(28)));

        TextView intro2 = makeText(
                "Перемагай у рейтингових матчах та піднімайся вище",
                11,
                false,
                Gravity.CENTER,
                MUTED
        );
        LinearLayout.LayoutParams intro2LP = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(35)
        );
        intro2LP.bottomMargin = dp(12);
        content.addView(intro2, intro2LP);

        int currentIndex = LeagueSystem.indexForCups(playerCups);

        // =====================================================
        // CURRENT LEAGUE HERO — SAME BADGE AS PROFILE
        // =====================================================
        LinearLayout hero = new LinearLayout(context);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(16), dp(12), dp(16), dp(14));
        hero.setBackground(solidPanel(PANEL_CURRENT, GOLD_LIGHT, 2, 14));

        TextView heroSmall = makeText("ПОТОЧНА ЛІГА", 10, true, Gravity.CENTER, GOLD);
        hero.addView(heroSmall);

        FrameLayout heroBadge = makeRankBadge(playerCups, false, true);
        LinearLayout.LayoutParams heroBadgeLP = new LinearLayout.LayoutParams(dp(82), dp(82));
        heroBadgeLP.topMargin = dp(3);
        heroBadgeLP.bottomMargin = dp(2);
        hero.addView(heroBadge, heroBadgeLP);

        TextView heroLeague = makeText(
                LeagueSystem.nameForCups(playerCups),
                22,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );
        hero.addView(heroLeague, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(36)));

        if (!LeagueSystem.isMaxLeague(playerCups)) {
            int start = LeagueSystem.startForCups(playerCups);
            int next = LeagueSystem.nextStartForCups(playerCups);
            int currentProgress = playerCups - start;
            int totalProgress = next - start;
            int remaining = Math.max(0, next - playerCups);

            TextView heroProgressText = makeText(
                    "🏆 " + playerCups + " / " + next,
                    14,
                    true,
                    Gravity.CENTER,
                    TEXT
            );
            hero.addView(heroProgressText);

            ProgressBar progress = new ProgressBar(
                    context,
                    null,
                    android.R.attr.progressBarStyleHorizontal
            );
            progress.setMax(Math.max(1, totalProgress));
            progress.setProgress(Math.max(0, Math.min(currentProgress, totalProgress)));
            progress.setProgressTintList(ColorStateList.valueOf(GOLD));
            progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(22, 35, 30)));

            LinearLayout.LayoutParams progressLP = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dp(8)
            );
            progressLP.topMargin = dp(9);
            progressLP.bottomMargin = dp(7);
            hero.addView(progress, progressLP);

            TextView remainingText = makeText(
                    "До " + LeagueSystem.nextNameForCups(playerCups) +
                            " залишилось " + remaining + " кубків",
                    11,
                    false,
                    Gravity.CENTER,
                    MUTED
            );
            hero.addView(remainingText);
        } else {
            hero.addView(makeText(
                    "🏆 " + playerCups + "  •  МАКСИМАЛЬНА ЛІГА",
                    13,
                    true,
                    Gravity.CENTER,
                    GOLD_LIGHT
            ));
        }

        LinearLayout.LayoutParams heroLP = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        heroLP.bottomMargin = dp(24);
        content.addView(hero, heroLP);

        TextView pathTitle = makeText("ЛІГИ DURAK ARENA", 12, true, Gravity.CENTER, GOLD);
        LinearLayout.LayoutParams pathTitleLP = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(35)
        );
        pathTitleLP.bottomMargin = dp(4);
        content.addView(pathTitle, pathTitleLP);

        // =====================================================
        // LEAGUE ROAD
        // Each league automatically receives the badge that belongs to the
        // cup threshold of that league. This mirrors Profile badge switching.
        // =====================================================
        for (int i = 0; i < LeagueSystem.NAMES.length; i++) {
            final boolean isCurrent = i == currentIndex;
            final boolean completed = i < currentIndex;
            final boolean locked = i > currentIndex;
            final int leagueCups = Math.max(0, LeagueSystem.STARTS[i]);

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10), dp(10), dp(10), dp(10));
            card.setBackground(solidPanel(
                    isCurrent ? PANEL_CURRENT : PANEL,
                    isCurrent ? GOLD_LIGHT : GOLD,
                    isCurrent ? 2 : 1,
                    13
            ));

            // Real lightweight league badge (same rank_*.png/webp resource as Profile).
            FrameLayout badge = makeRankBadge(leagueCups, locked, isCurrent);
            card.addView(badge, new LinearLayout.LayoutParams(dp(76), dp(76)));

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setGravity(Gravity.CENTER_VERTICAL);
            info.setPadding(dp(12), 0, dp(4), 0);

            TextView leagueName = makeText(
                    LeagueSystem.NAMES[i],
                    isCurrent ? 17 : 15,
                    true,
                    Gravity.LEFT,
                    locked ? MUTED : GOLD_LIGHT
            );
            info.addView(leagueName);

            String requirement = i == 0
                    ? "ПОЧАТОК ШЛЯХУ"
                    : "🏆 " + LeagueSystem.STARTS[i] + " КУБКІВ";
            info.addView(makeText(
                    requirement,
                    11,
                    false,
                    Gravity.LEFT,
                    locked ? LOCKED : TEXT
            ));

            String status;
            if (isCurrent) {
                status = "◆  ТИ ТУТ";
            } else if (completed) {
                status = "✓  ПРОЙДЕНО";
            } else {
                int need = Math.max(0, LeagueSystem.STARTS[i] - playerCups);
                status = "🔒  ЩЕ " + need + " КУБКІВ";
            }

            TextView statusText = makeText(
                    status,
                    10,
                    isCurrent,
                    Gravity.LEFT,
                    isCurrent ? GOLD_LIGHT : (completed ? GOLD : LOCKED)
            );
            LinearLayout.LayoutParams statusLP = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dp(25)
            );
            statusLP.topMargin = dp(3);
            info.addView(statusText, statusLP);

            card.addView(info, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

            TextView right = makeText(
                    isCurrent ? "ТИ\nТУТ" : (completed ? "✓" : "🔒"),
                    isCurrent ? 11 : 17,
                    true,
                    Gravity.CENTER,
                    isCurrent ? GOLD_LIGHT : (completed ? GOLD : LOCKED)
            );
            card.addView(right, new LinearLayout.LayoutParams(dp(45), LayoutParams.MATCH_PARENT));

            if (locked) card.setAlpha(0.82f);

            content.addView(card, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dp(102)
            ));

            if (i < LeagueSystem.NAMES.length - 1) {
                LinearLayout connector = new LinearLayout(context);
                connector.setGravity(Gravity.CENTER);
                View line = new View(context);
                GradientDrawable lineBG = new GradientDrawable();
                lineBG.setColor(i < currentIndex ? GOLD : LINE_DARK);
                lineBG.setCornerRadius(dp(2));
                line.setBackground(lineBG);
                connector.addView(line, new LinearLayout.LayoutParams(dp(3), dp(26)));
                content.addView(connector, new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(26)
                ));
            }
        }

        TextView legendEnd = makeText(
                "♛  ВЕРШИНА ШЛЯХУ  ♛\nЛЕГЕНДА III",
                15,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );
        LinearLayout.LayoutParams legendEndLP = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                dp(80)
        );
        legendEndLP.topMargin = dp(18);
        content.addView(legendEnd, legendEndLP);

        scroll.addView(content);
        FrameLayout.LayoutParams scrollLP = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        scrollLP.topMargin = dp(68);
        addView(scroll, scrollLP);
    }

    /**
     * Returns the exact same major-rank asset family used by MainActivity's
     * profile screen. As cups change, both screens move to the next badge.
     */
    private String rankAssetSuffixForCups(int cups) {
        if (cups >= 5000) return "legend";
        if (cups >= 3500) return "elite";
        if (cups >= 2200) return "master";
        if (cups >= 1200) return "pro";
        if (cups >= 500) return "player";
        return "novice";
    }

    private FrameLayout makeRankBadge(int cups, boolean locked, boolean current) {
        FrameLayout holder = new FrameLayout(context);
        holder.setClipChildren(false);
        holder.setClipToPadding(false);

        String suffix = rankAssetSuffixForCups(cups);
        String resourceName = "rank_" + suffix;
        int id = context.getResources().getIdentifier(
                resourceName,
                "drawable",
                context.getPackageName()
        );

        if (id != 0) {
            ImageView icon = new ImageView(context);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setAdjustViewBounds(true);
            icon.setPadding(dp(2), dp(2), dp(2), dp(2));
            icon.setImageResource(id);
            icon.setContentDescription(resourceName);
            icon.setAlpha(locked ? 0.42f : 1.0f);
            holder.addView(icon, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
        } else {
            // Safe fallback if an asset is temporarily absent from a device build.
            TextView fallback = makeText(
                    fallbackMarkForSuffix(suffix),
                    current ? 34 : 30,
                    true,
                    Gravity.CENTER,
                    locked ? LOCKED : GOLD_LIGHT
            );
            holder.addView(fallback, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
        }

        return holder;
    }

    private String fallbackMarkForSuffix(String suffix) {
        switch (suffix) {
            case "legend": return "♛";
            case "elite": return "♦";
            case "master": return "★";
            case "pro": return "♠";
            case "player": return "♣";
            default: return "♠";
        }
    }

    private TextView makeText(String text, int size, boolean bold, int gravity, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(gravity);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable solidPanel(int fill, int stroke, int strokeWidth, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) drawable.setStroke(dp(strokeWidth), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
