package com.nexora.durakarena;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class GloryPathV2View extends FrameLayout {
    private final Context context;
    private final int cups;
    private final Runnable onBack;
    private final SharedPreferences settings;

    public GloryPathV2View(Context context, int cups, Runnable onBack) {
        super(context);
        this.context = context;
        this.cups = Math.max(0, cups);
        this.onBack = onBack;
        this.settings = context.getSharedPreferences("durak_arena_settings", Context.MODE_PRIVATE);
        build();
    }

    private void build() {
        setBackgroundColor(UiKit.BG);
        int current = LeagueSystem.indexForCups(cups);

        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(UiKit.dp(context, 8), 0, UiKit.dp(context, 10), 0);
        header.setBackground(UiKit.panel(context, Color.rgb(5, 9, 13), UiKit.GOLD, 1, 0));

        TextView back = UiKit.text(context, "‹", 38, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        back.setClickable(true);
        back.setOnClickListener(v -> { if (onBack != null) onBack.run(); });
        header.addView(back, new LinearLayout.LayoutParams(UiKit.dp(context, 52), -1));

        TextView title = UiKit.text(context, tr("ШЛЯХ СЛАВИ", "GLORY PATH", "RUHMESPFAD", "CAMINO DE GLORIA"),
                20, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        header.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));

        TextView cupText = UiKit.text(context, "🏆 " + cups, 12, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        header.addView(cupText, new LinearLayout.LayoutParams(UiKit.dp(context, 74), -1));

        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, UiKit.dp(context, 64));
        addView(header, hp);

        ScrollView scroll = new ScrollView(context);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(UiKit.dp(context, 12), UiKit.dp(context, 14),
                UiKit.dp(context, 12), UiKit.dp(context, 36));

        LinearLayout hero = new LinearLayout(context);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(UiKit.dp(context, 12), UiKit.dp(context, 10),
                UiKit.dp(context, 12), UiKit.dp(context, 12));
        int accent = LeagueSystem.familyColorForIndex(current);
        hero.setBackground(UiKit.panel(context, Color.rgb(22, 16, 13), accent, 2, 14));

        hero.addView(UiKit.text(context, tr("ПОТОЧНА ЛІГА", "CURRENT LEAGUE", "AKTUELLE LIGA", "LIGA ACTUAL"),
                10, true, Gravity.CENTER, accent));
        ImageView currentBadge = badge(current);
        hero.addView(currentBadge, new LinearLayout.LayoutParams(UiKit.dp(context, 104), UiKit.dp(context, 104)));

        hero.addView(UiKit.text(context, LeagueSystem.NAMES[current], 21, true,
                Gravity.CENTER, UiKit.GOLD_LIGHT),
                new LinearLayout.LayoutParams(-1, UiKit.dp(context, 34)));

        if (!LeagueSystem.isMaxLeague(cups)) {
            int max = LeagueSystem.progressMaxForCups(cups);
            int progress = LeagueSystem.progressForCups(cups);

            TextView value = UiKit.text(context,
                    "🏆 " + cups + " / " + LeagueSystem.nextStartForCups(cups),
                    13, true, Gravity.CENTER, UiKit.TEXT);
            hero.addView(value);

            ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(max);
            bar.setProgress(progress);
            bar.setProgressTintList(ColorStateList.valueOf(accent));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(18, 27, 28)));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, UiKit.dp(context, 8));
            bp.topMargin = UiKit.dp(context, 7);
            bp.bottomMargin = UiKit.dp(context, 5);
            hero.addView(bar, bp);

            hero.addView(UiKit.text(context,
                    tr("До наступної ліги: ", "To next league: ", "Bis zur nächsten Liga: ", "Hasta la siguiente liga: ")
                            + Math.max(0, LeagueSystem.nextStartForCups(cups) - cups),
                    10, false, Gravity.CENTER, UiKit.MUTED));
        }

        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.bottomMargin = UiKit.dp(context, 18);
        content.addView(hero, heroLp);

        TextView pathTitle = UiKit.text(context,
                tr("ВІД НОВАЧКА ДО ЛЕГЕНДИ", "FROM NOVICE TO LEGEND", "VOM ANFÄNGER ZUR LEGENDE", "DE NOVATO A LEYENDA"),
                12, true, Gravity.CENTER, UiKit.GOLD);
        LinearLayout.LayoutParams ptlp = new LinearLayout.LayoutParams(-1, UiKit.dp(context, 38));
        content.addView(pathTitle, ptlp);

        for (int i = 0; i < LeagueSystem.NAMES.length; i++) {
            boolean isCurrent = i == current;
            boolean passed = i < current;
            boolean locked = i > current;
            int color = LeagueSystem.familyColorForIndex(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout rail = new LinearLayout(context);
            rail.setOrientation(LinearLayout.VERTICAL);
            rail.setGravity(Gravity.CENTER_HORIZONTAL);

            View topLine = new View(context);
            topLine.setBackgroundColor(i <= current ? color : Color.rgb(65, 57, 48));
            rail.addView(topLine, new LinearLayout.LayoutParams(UiKit.dp(context, 3), UiKit.dp(context, i == 0 ? 10 : 24)));

            FrameLayout dot = new FrameLayout(context);
            dot.setBackground(UiKit.panel(context, isCurrent ? color : UiKit.PANEL, color, 2, 20));
            TextView dotText = UiKit.text(context, isCurrent ? "◆" : passed ? "✓" : "•",
                    isCurrent ? 15 : 12, true, Gravity.CENTER,
                    isCurrent ? Color.WHITE : color);
            dot.addView(dotText, new FrameLayout.LayoutParams(-1, -1));
            rail.addView(dot, new LinearLayout.LayoutParams(UiKit.dp(context, 28), UiKit.dp(context, 28)));

            View bottomLine = new View(context);
            bottomLine.setBackgroundColor(i < current ? color : Color.rgb(65, 57, 48));
            rail.addView(bottomLine, new LinearLayout.LayoutParams(UiKit.dp(context, 3), UiKit.dp(context, i == LeagueSystem.NAMES.length - 1 ? 10 : 24)));

            row.addView(rail, new LinearLayout.LayoutParams(UiKit.dp(context, 42), UiKit.dp(context, 108)));

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(UiKit.dp(context, 10), UiKit.dp(context, 8),
                    UiKit.dp(context, 10), UiKit.dp(context, 8));
            card.setBackground(UiKit.panel(context,
                    isCurrent ? Color.rgb(36, 20, 17) : Color.rgb(7, 13, 18),
                    isCurrent ? color : Color.rgb(100, 76, 42),
                    isCurrent ? 2 : 1, 12));
            if (locked) card.setAlpha(0.72f);

            ImageView badge = badge(i);
            card.addView(badge, new LinearLayout.LayoutParams(UiKit.dp(context, 78), UiKit.dp(context, 78)));

            LinearLayout words = new LinearLayout(context);
            words.setOrientation(LinearLayout.VERTICAL);
            words.setPadding(UiKit.dp(context, 10), 0, UiKit.dp(context, 4), 0);

            TextView name = UiKit.text(context, LeagueSystem.NAMES[i], isCurrent ? 17 : 15,
                    true, Gravity.LEFT, isCurrent ? UiKit.GOLD_LIGHT : color);
            words.addView(name);

            String threshold = i == 0 ? tr("ПОЧАТОК ШЛЯХУ", "START OF PATH", "START", "INICIO")
                    : "🏆 " + LeagueSystem.STARTS[i] + " " + tr("КУБКІВ", "CUPS", "POKALE", "COPAS");
            words.addView(UiKit.text(context, threshold, 11, false, Gravity.LEFT, UiKit.TEXT));

            String status = isCurrent ? tr("● ТИ ТУТ", "● YOU ARE HERE", "● DU BIST HIER", "● ESTÁS AQUÍ")
                    : passed ? tr("✓ ПРОЙДЕНО", "✓ COMPLETED", "✓ ABGESCHLOSSEN", "✓ COMPLETADO")
                    : tr("🔒 ЩЕ ", "🔒 NEED ", "🔒 NOCH ", "🔒 FALTAN ")
                    + Math.max(0, LeagueSystem.STARTS[i] - cups);
            words.addView(UiKit.text(context, status, 10, isCurrent, Gravity.LEFT,
                    isCurrent ? UiKit.GOLD_LIGHT : UiKit.MUTED));

            card.addView(words, new LinearLayout.LayoutParams(0, -2, 1f));

            LinearLayout rewards = new LinearLayout(context);
            rewards.setOrientation(LinearLayout.VERTICAL);
            rewards.setGravity(Gravity.CENTER);
            rewards.addView(iconValue("nexora_coin", String.valueOf(GloryRewards.coins(i))));
            rewards.addView(iconValue("nexora_crystal", String.valueOf(GloryRewards.crystals(i))));
            card.addView(rewards, new LinearLayout.LayoutParams(UiKit.dp(context, 72), -1));

            row.addView(card, new LinearLayout.LayoutParams(0, UiKit.dp(context, 104), 1f));

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, UiKit.dp(context, 108));
            if (i % 2 == 1) {
                rowLp.leftMargin = UiKit.dp(context, 8);
            }
            content.addView(row, rowLp);
        }

        TextView end = UiKit.text(context, "♛  " + tr("ВЕРШИНА ШЛЯХУ — ЛЕГЕНДА III", "TOP OF THE PATH — LEGEND III",
                "SPITZE — LEGENDE III", "CIMA — LEYENDA III") + "  ♛",
                14, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        LinearLayout.LayoutParams endLp = new LinearLayout.LayoutParams(-1, UiKit.dp(context, 70));
        endLp.topMargin = UiKit.dp(context, 14);
        content.addView(end, endLp);

        scroll.addView(content);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -1);
        sp.topMargin = UiKit.dp(context, 64);
        addView(scroll, sp);
    }

    private ImageView badge(int index) {
        ImageView v = new ImageView(context);
        int id = context.getResources().getIdentifier(
                LeagueSystem.assetForIndex(index),
                "drawable",
                context.getPackageName()
        );
        if (id != 0) v.setImageResource(id);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setContentDescription(LeagueSystem.NAMES[index]);
        return v;
    }

    private LinearLayout iconValue(String iconName, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER);
        ImageView icon = UiKit.icon(context, iconName, value);
        row.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(context, 21), UiKit.dp(context, 21)));
        TextView t = UiKit.text(context, " " + value, 10, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        row.addView(t);
        return row;
    }

    private String tr(String uk, String en, String de, String es) {
        String lang = settings.getString("language", "uk");
        if ("en".equals(lang)) return en;
        if ("de".equals(lang)) return de;
        if ("es".equals(lang)) return es;
        return uk;
    }
}
