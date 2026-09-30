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
 * Rank art stays lightweight: the same drawable family already used by the
 * Profile screen (rank_novice, rank_player, rank_pro, rank_master,
 * rank_elite, rank_legend). Each promotion is decorated progressively in code,
 * so I / II / III no longer look identical. No Blender/GLB/Filament is used.
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

        // CURRENT LEAGUE HERO
        LinearLayout hero = new LinearLayout(context);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(16), dp(12), dp(16), dp(14));
        hero.setBackground(solidPanel(PANEL_CURRENT, GOLD_LIGHT, 2, 14));

        TextView heroSmall = makeText("ПОТОЧНА ЛІГА", 10, true, Gravity.CENTER, GOLD);
        hero.addView(heroSmall);

        FrameLayout heroBadge = makeRankBadge(playerCups, false, true, currentIndex);
        LinearLayout.LayoutParams heroBadgeLP = new LinearLayout.LayoutParams(dp(92), dp(92));
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

        // LEAGUE ROAD
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

            // Same base artwork, but every I / II / III promotion gets a richer
            // frame and promotion marks so consecutive leagues are visibly unique.
            FrameLayout badge = makeRankBadge(leagueCups, locked, isCurrent, i);
            card.addView(badge, new LinearLayout.LayoutParams(dp(82), dp(82)));

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

            // Keep locked badges readable; previously the double alpha made them
            // almost black on the dark background.
            if (locked) card.setAlpha(0.92f);

            content.addView(card, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dp(108)
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

    private String rankAssetSuffixForCups(int cups) {
        if (cups >= 5000) return "legend";
        if (cups >= 3500) return "elite";
        if (cups >= 2200) return "master";
        if (cups >= 1200) return "pro";
        if (cups >= 500) return "player";
        return "novice";
    }

    /**
     * Builds a lightweight premium badge. The PNG/WEBP remains the base art,
     * while promotion I / II / III adds progressively stronger framing,
     * larger artwork and 1 / 2 / 3 gold diamonds. This makes every promotion
     * visibly better without bringing back expensive 3D models.
     */
    private FrameLayout makeRankBadge(int cups, boolean locked, boolean current, int leagueIndex) {
        FrameLayout holder = new FrameLayout(context);
        holder.setClipChildren(false);
        holder.setClipToPadding(false);

        String leagueName = "";
        if (leagueIndex >= 0 && leagueIndex < LeagueSystem.NAMES.length) {
            leagueName = LeagueSystem.NAMES[leagueIndex];
        }
        int stage = promotionStage(leagueName);
        int accent = promotionAccent(stage, current);

        GradientDrawable outerFrame = new GradientDrawable();
        outerFrame.setShape(GradientDrawable.OVAL);
        outerFrame.setColor(Color.argb(current ? 32 : 12, 255, 198, 78));
        outerFrame.setStroke(dp(current ? 3 : Math.max(1, stage + 1)), accent);
        holder.setBackground(outerFrame);

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
            int iconPadding = Math.max(1, 7 - stage * 2);
            icon.setPadding(dp(iconPadding), dp(iconPadding), dp(iconPadding), dp(iconPadding));
            icon.setImageResource(id);
            icon.setContentDescription(resourceName + "_stage_" + stage);
            icon.setAlpha(locked ? 0.66f : 1.0f);

            FrameLayout.LayoutParams iconLP = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            );
            iconLP.setMargins(dp(3), dp(3), dp(3), dp(3));
            holder.addView(icon, iconLP);
        } else {
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

        // Promotion diamonds: I = one, II = two, III = three.
        // Base league gets a small central diamond and the cleanest frame.
        String marks;
        if (stage == 3) marks = "◆ ◆ ◆";
        else if (stage == 2) marks = "◆ ◆";
        else if (stage == 1) marks = "◆";
        else marks = "·";

        TextView promotion = makeText(
                marks,
                stage >= 2 ? 9 : 10,
                true,
                Gravity.CENTER,
                locked ? Color.rgb(133, 111, 67) : accent
        );
        promotion.setPadding(dp(4), 0, dp(4), 0);
        promotion.setBackground(solidPanel(
                Color.argb(225, 2, 10, 8),
                locked ? Color.rgb(83, 72, 53) : accent,
                1,
                8
        ));

        FrameLayout.LayoutParams promotionLP = new FrameLayout.LayoutParams(
                stage == 3 ? dp(52) : (stage == 2 ? dp(42) : dp(30)),
                dp(18)
        );
        promotionLP.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        promotionLP.bottomMargin = dp(-2);
        holder.addView(promotion, promotionLP);

        // Highest sub-tier gets a small crown/star accent at the top, making
        // every III badge clearly feel like the completed version of that rank.
        if (stage == 3) {
            TextView crown = makeText(
                    "✦",
                    13,
                    true,
                    Gravity.CENTER,
                    locked ? Color.rgb(126, 104, 62) : GOLD_LIGHT
            );
            FrameLayout.LayoutParams crownLP = new FrameLayout.LayoutParams(dp(24), dp(22));
            crownLP.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            crownLP.topMargin = dp(-5);
            holder.addView(crown, crownLP);
        }

        return holder;
    }

    private int promotionStage(String leagueName) {
        if (leagueName == null) return 0;
        String n = leagueName.trim().toUpperCase();
        if (n.endsWith(" III")) return 3;
        if (n.endsWith(" II")) return 2;
        if (n.endsWith(" I")) return 1;
        return 0;
    }

    private int promotionAccent(int stage, boolean current) {
        if (current) return Color.rgb(255, 220, 126);
        switch (stage) {
            case 3: return Color.rgb(255, 211, 104);
            case 2: return Color.rgb(232, 181, 76);
            case 1: return Color.rgb(202, 147, 54);
            default: return Color.rgb(155, 108, 43);
        }
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
