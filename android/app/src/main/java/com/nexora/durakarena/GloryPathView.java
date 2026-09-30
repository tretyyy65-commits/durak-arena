package com.nexora.durakarena;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class GloryPathView extends FrameLayout {

    private final Context context;
    private final int playerCups;
    private final Runnable onBack;

    // MATTE DURAK ARENA PALETTE
    private final int BG = Color.rgb(2, 8, 7);
    private final int PANEL = Color.rgb(4, 18, 14);
    private final int PANEL_CURRENT = Color.rgb(7, 29, 22);

    private final int GOLD = Color.rgb(211, 163, 76);
    private final int GOLD_LIGHT = Color.rgb(246, 211, 139);
    private final int TEXT = Color.rgb(239, 226, 193);
    private final int MUTED = Color.rgb(155, 149, 132);
    private final int LOCKED = Color.rgb(105, 103, 95);
    private final int LINE_DARK = Color.rgb(69, 60, 43);

    public GloryPathView(
            Context context,
            int playerCups,
            Runnable onBack
    ) {
        super(context);

        this.context = context;
        this.playerCups = Math.max(0, playerCups);
        this.onBack = onBack;

        build();
    }

    private void build() {

        setBackgroundColor(BG);

        // ==========================================
        // FIXED HEADER
        // ==========================================
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), 0, dp(12), 0);
        header.setBackground(solidPanel(BG, GOLD, 1, 0));

        TextView back = makeText(
                "‹",
                40,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );

        back.setClickable(true);
        back.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });

        header.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(54),
                        LayoutParams.MATCH_PARENT
                )
        );

        TextView headerTitle = makeText(
                "ШЛЯХ СЛАВИ",
                20,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );

        header.addView(
                headerTitle,
                new LinearLayout.LayoutParams(
                        0,
                        LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView cupsTop = makeText(
                "🏆 " + playerCups,
                12,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );

        header.addView(
                cupsTop,
                new LinearLayout.LayoutParams(
                        dp(65),
                        LayoutParams.MATCH_PARENT
                )
        );

        FrameLayout.LayoutParams headerLP =
                new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(68)
                );

        headerLP.gravity = Gravity.TOP;
        addView(header, headerLP);

        // ==========================================
        // SCROLL
        // ==========================================
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(
                dp(12),
                dp(18),
                dp(12),
                dp(55)
        );

        // ==========================================
        // INTRO
        // ==========================================
        TextView intro = makeText(
                "ВІД НОВАЧКА ДО ЛЕГЕНДИ",
                13,
                true,
                Gravity.CENTER,
                GOLD
        );

        content.addView(
                intro,
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(28)
                )
        );

        TextView intro2 = makeText(
                "Перемагай у рейтингових матчах та піднімайся вище",
                11,
                false,
                Gravity.CENTER,
                MUTED
        );

        LinearLayout.LayoutParams intro2LP =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(35)
                );

        intro2LP.bottomMargin = dp(12);
        content.addView(intro2, intro2LP);

        // ==========================================
        // CURRENT LEAGUE HERO
        // ==========================================
        int currentIndex =
                LeagueSystem.indexForCups(playerCups);

        LinearLayout hero = new LinearLayout(context);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        hero.setBackground(
                solidPanel(
                        PANEL_CURRENT,
                        GOLD_LIGHT,
                        2,
                        14
                )
        );

        TextView heroSmall = makeText(
                "ПОТОЧНА ЛІГА",
                10,
                true,
                Gravity.CENTER,
                GOLD
        );

        hero.addView(heroSmall);

        TextView heroLeague = makeText(
                LeagueSystem.nameForCups(playerCups),
                23,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );

        LinearLayout.LayoutParams heroLeagueLP =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(38)
                );

        hero.addView(heroLeague, heroLeagueLP);

        if (!LeagueSystem.isMaxLeague(playerCups)) {

            int start =
                    LeagueSystem.startForCups(playerCups);

            int next =
                    LeagueSystem.nextStartForCups(playerCups);

            int currentProgress =
                    playerCups - start;

            int totalProgress =
                    next - start;

            int remaining =
                    Math.max(0, next - playerCups);

            TextView heroProgressText = makeText(
                    "🏆 " + playerCups + " / " + next,
                    15,
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
            progress.setProgress(
                    Math.max(
                            0,
                            Math.min(
                                    currentProgress,
                                    totalProgress
                            )
                    )
            );

            progress.setProgressTintList(
                    ColorStateList.valueOf(GOLD)
            );

            progress.setProgressBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(22, 35, 30)
                    )
            );

            LinearLayout.LayoutParams progressLP =
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            dp(8)
                    );

            progressLP.topMargin = dp(10);
            progressLP.bottomMargin = dp(8);

            hero.addView(progress, progressLP);

            TextView remainingText = makeText(
                    "До " +
                            LeagueSystem.nextNameForCups(playerCups) +
                            " залишилось " +
                            remaining +
                            " кубків",
                    11,
                    false,
                    Gravity.CENTER,
                    MUTED
            );

            hero.addView(remainingText);

        } else {

            hero.addView(
                    makeText(
                            "🏆 " + playerCups + "  •  МАКСИМАЛЬНА ЛІГА",
                            13,
                            true,
                            Gravity.CENTER,
                            GOLD_LIGHT
                    )
            );
        }

        LinearLayout.LayoutParams heroLP =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                );

        heroLP.bottomMargin = dp(26);
        content.addView(hero, heroLP);

        // ==========================================
        // PATH TITLE
        // ==========================================
        TextView pathTitle = makeText(
                "ЛІГИ DURAK ARENA",
                12,
                true,
                Gravity.CENTER,
                GOLD
        );

        LinearLayout.LayoutParams pathTitleLP =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(35)
                );

        pathTitleLP.bottomMargin = dp(4);
        content.addView(pathTitle, pathTitleLP);

        // ==========================================
        // LEAGUE ROAD
        // НОВАЧОК -> ЛЕГЕНДА III
        // ==========================================
        for (int i = 0; i < LeagueSystem.NAMES.length; i++) {

            final boolean isCurrent =
                    i == currentIndex;

            final boolean completed =
                    i < currentIndex;

            final boolean locked =
                    i > currentIndex;

            LinearLayout card =
                    new LinearLayout(context);

            card.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            card.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            card.setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(12)
            );

            card.setBackground(
                    solidPanel(
                            isCurrent
                                    ? PANEL_CURRENT
                                    : PANEL,
                            isCurrent
                                    ? GOLD_LIGHT
                                    : GOLD,
                            isCurrent ? 2 : 1,
                            13
                    )
            );

            // --------------------------
            // EMBLEM PLACE
            // --------------------------
            FrameLayout emblemHolder =
                    new FrameLayout(context);

            GradientDrawable emblemBG =
                    new GradientDrawable();

            emblemBG.setShape(
                    GradientDrawable.OVAL
            );

            emblemBG.setColor(
                    Color.rgb(5, 15, 12)
            );

            emblemBG.setStroke(
                    dp(isCurrent ? 2 : 1),
                    isCurrent
                            ? GOLD_LIGHT
                            : GOLD
            );

            emblemHolder.setBackground(emblemBG);

            TextView emblem = makeText(
                    emblemFor(i),
                    28,
                    true,
                    Gravity.CENTER,
                    locked
                            ? LOCKED
                            : GOLD_LIGHT
            );

            emblemHolder.addView(
                    emblem,
                    new FrameLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                    )
            );

            card.addView(
                    emblemHolder,
                    new LinearLayout.LayoutParams(
                            dp(68),
                            dp(68)
                    )
            );

            // --------------------------
            // LEAGUE INFO
            // --------------------------
            LinearLayout info =
                    new LinearLayout(context);

            info.setOrientation(
                    LinearLayout.VERTICAL
            );

            info.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            info.setPadding(
                    dp(14),
                    0,
                    dp(4),
                    0
            );

            TextView leagueName = makeText(
                    LeagueSystem.NAMES[i],
                    isCurrent ? 18 : 16,
                    true,
                    Gravity.LEFT,
                    locked
                            ? MUTED
                            : GOLD_LIGHT
            );

            info.addView(leagueName);

            String requirement;

            if (i == 0) {
                requirement =
                        "ПОЧАТОК ШЛЯХУ";
            } else {
                requirement =
                        "🏆 " +
                        LeagueSystem.STARTS[i] +
                        " КУБКІВ";
            }

            info.addView(
                    makeText(
                            requirement,
                            11,
                            false,
                            Gravity.LEFT,
                            locked
                                    ? LOCKED
                                    : TEXT
                    )
            );

            String status;

            if (isCurrent) {

                status = "◆  ТИ ТУТ";

            } else if (completed) {

                status = "✓  ПРОЙДЕНО";

            } else {

                int need =
                        Math.max(
                                0,
                                LeagueSystem.STARTS[i]
                                        - playerCups
                        );

                status =
                        "🔒  ЩЕ " +
                        need +
                        " КУБКІВ";
            }

            TextView statusText = makeText(
                    status,
                    10,
                    isCurrent,
                    Gravity.LEFT,
                    isCurrent
                            ? GOLD_LIGHT
                            : (completed
                                ? GOLD
                                : LOCKED)
            );

            LinearLayout.LayoutParams statusLP =
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            dp(25)
                    );

            statusLP.topMargin = dp(3);

            info.addView(
                    statusText,
                    statusLP
            );

            card.addView(
                    info,
                    new LinearLayout.LayoutParams(
                            0,
                            LayoutParams.MATCH_PARENT,
                            1f
                    )
            );

            // --------------------------
            // RIGHT STATUS
            // --------------------------
            TextView right = makeText(
                    isCurrent
                            ? "ТИ\nТУТ"
                            : completed
                                ? "✓"
                                : "🔒",
                    isCurrent ? 11 : 17,
                    true,
                    Gravity.CENTER,
                    isCurrent
                            ? GOLD_LIGHT
                            : (completed
                                ? GOLD
                                : LOCKED)
            );

            card.addView(
                    right,
                    new LinearLayout.LayoutParams(
                            dp(48),
                            LayoutParams.MATCH_PARENT
                    )
            );

            if (locked) {
                card.setAlpha(0.78f);
            }

            LinearLayout.LayoutParams cardLP =
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            dp(104)
                    );

            content.addView(card, cardLP);

            // --------------------------
            // ROAD CONNECTOR
            // --------------------------
            if (i < LeagueSystem.NAMES.length - 1) {

                LinearLayout connector =
                        new LinearLayout(context);

                connector.setGravity(
                        Gravity.CENTER
                );

                View line =
                        new View(context);

                GradientDrawable lineBG =
                        new GradientDrawable();

                lineBG.setColor(
                        i < currentIndex
                                ? GOLD
                                : LINE_DARK
                );

                lineBG.setCornerRadius(dp(2));

                line.setBackground(lineBG);

                connector.addView(
                        line,
                        new LinearLayout.LayoutParams(
                                dp(3),
                                dp(30)
                        )
                );

                content.addView(
                        connector,
                        new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                dp(30)
                        )
                );
            }
        }

        // ==========================================
        // FINAL LEGEND MESSAGE
        // ==========================================
        TextView legendEnd = makeText(
                "♛  ВЕРШИНА ШЛЯХУ  ♛\nЛЕГЕНДА III",
                15,
                true,
                Gravity.CENTER,
                GOLD_LIGHT
        );

        LinearLayout.LayoutParams legendEndLP =
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(80)
                );

        legendEndLP.topMargin = dp(18);

        content.addView(
                legendEnd,
                legendEndLP
        );

        scroll.addView(content);

        FrameLayout.LayoutParams scrollLP =
                new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                );

        scrollLP.topMargin = dp(68);

        addView(scroll, scrollLP);
    }

    // ==========================================
    // TEMP EMBLEMS
    // ПІЗНІШЕ ЗАМІНИМО НА СПРАВЖНІ PNG/WEBP
    // ==========================================
    private String emblemFor(int index) {

        if (index <= 3) {
            return "♣";
        }

        if (index <= 6) {
            return "♠";
        }

        if (index <= 9) {
            return "★";
        }

        if (index <= 12) {
            return "♦";
        }

        return "♛";
    }

    private TextView makeText(
            String text,
            int size,
            boolean bold,
            int gravity,
            int color
    ) {

        TextView view =
                new TextView(context);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(gravity);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private GradientDrawable solidPanel(
            int fill,
            int stroke,
            int strokeWidth,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(fill);

        drawable.setCornerRadius(
                dp(radius)
        );

        if (strokeWidth > 0) {
            drawable.setStroke(
                    dp(strokeWidth),
                    stroke
            );
        }

        return drawable;
    }

    private int dp(int value) {

        return Math.round(
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
