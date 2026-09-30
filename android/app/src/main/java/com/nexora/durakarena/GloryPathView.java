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

/** Lightweight league road. No Blender / GLB / Filament. */
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
        header.setBackground(panel(BG, GOLD, 1, 0));

        TextView back = text("‹", 40, true, Gravity.CENTER, GOLD_LIGHT);
        back.setOnClickListener(v -> { if (onBack != null) onBack.run(); });
        header.addView(back, new LinearLayout.LayoutParams(dp(54), LayoutParams.MATCH_PARENT));
        header.addView(text("ШЛЯХ СЛАВИ", 20, true, Gravity.CENTER, GOLD_LIGHT),
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        header.addView(text("🏆 " + playerCups, 12, true, Gravity.CENTER, GOLD_LIGHT),
                new LinearLayout.LayoutParams(dp(65), LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(68));
        hp.gravity = Gravity.TOP;
        addView(header, hp);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(12), dp(18), dp(12), dp(55));

        content.addView(text("ВІД НОВАЧКА ДО ЛЕГЕНДИ", 13, true, Gravity.CENTER, GOLD),
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(28)));
        LinearLayout.LayoutParams introLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(35));
        introLp.bottomMargin = dp(12);
        content.addView(text("Перемагай у рейтингових матчах та піднімайся вище", 11, false, Gravity.CENTER, MUTED), introLp);

        int currentIndex = LeagueSystem.indexForCups(playerCups);
        String currentName = LeagueSystem.nameForCups(playerCups);

        LinearLayout hero = new LinearLayout(context);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(16), dp(12), dp(16), dp(14));
        hero.setBackground(panel(PANEL_CURRENT, GOLD_LIGHT, 2, 14));
        hero.addView(text("ПОТОЧНА ЛІГА", 10, true, Gravity.CENTER, GOLD));
        FrameLayout heroBadge = makeLeagueBadge(currentName, false, true);
        LinearLayout.LayoutParams hb = new LinearLayout.LayoutParams(dp(96), dp(96));
        hb.topMargin = dp(3); hb.bottomMargin = dp(3);
        hero.addView(heroBadge, hb);
        hero.addView(text(currentName, 22, true, Gravity.CENTER, GOLD_LIGHT),
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(36)));

        if (!LeagueSystem.isMaxLeague(playerCups)) {
            int start = LeagueSystem.startForCups(playerCups);
            int next = LeagueSystem.nextStartForCups(playerCups);
            int total = Math.max(1, next - start);
            int progressValue = Math.max(0, Math.min(playerCups - start, total));
            hero.addView(text("🏆 " + playerCups + " / " + next, 14, true, Gravity.CENTER, TEXT));
            ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            progress.setMax(total); progress.setProgress(progressValue);
            progress.setProgressTintList(ColorStateList.valueOf(GOLD));
            progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(22,35,30)));
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(8));
            pp.topMargin = dp(9); pp.bottomMargin = dp(7);
            hero.addView(progress, pp);
            hero.addView(text("До " + LeagueSystem.nextNameForCups(playerCups) + " залишилось " + Math.max(0, next-playerCups) + " кубків",
                    11, false, Gravity.CENTER, MUTED));
        } else {
            hero.addView(text("🏆 " + playerCups + "  •  МАКСИМАЛЬНА ЛІГА", 13, true, Gravity.CENTER, GOLD_LIGHT));
        }
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        heroLp.bottomMargin = dp(24);
        content.addView(hero, heroLp);

        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(35));
        titleLp.bottomMargin = dp(4);
        content.addView(text("ЛІГИ DURAK ARENA", 12, true, Gravity.CENTER, GOLD), titleLp);

        for (int i=0; i<LeagueSystem.NAMES.length; i++) {
            boolean current = i == currentIndex;
            boolean completed = i < currentIndex;
            boolean locked = i > currentIndex;
            String leagueName = LeagueSystem.NAMES[i];

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10), dp(10), dp(10), dp(10));
            card.setBackground(panel(current ? PANEL_CURRENT : PANEL, current ? GOLD_LIGHT : GOLD, current ? 2 : 1, 13));

            card.addView(makeLeagueBadge(leagueName, locked, current), new LinearLayout.LayoutParams(dp(88), dp(88)));

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setGravity(Gravity.CENTER_VERTICAL);
            info.setPadding(dp(12),0,dp(4),0);
            info.addView(text(leagueName, current ? 17 : 15, true, Gravity.LEFT, locked ? MUTED : GOLD_LIGHT));
            String requirement = i == 0 ? "ПОЧАТОК ШЛЯХУ" : "🏆 " + LeagueSystem.STARTS[i] + " КУБКІВ";
            info.addView(text(requirement, 11, false, Gravity.LEFT, locked ? LOCKED : TEXT));
            String status = current ? "◆  ТИ ТУТ" : completed ? "✓  ПРОЙДЕНО" : "🔒  ЩЕ " + Math.max(0, LeagueSystem.STARTS[i]-playerCups) + " КУБКІВ";
            TextView st = text(status, 10, current, Gravity.LEFT, current ? GOLD_LIGHT : completed ? GOLD : LOCKED);
            LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(25));
            stLp.topMargin = dp(3); info.addView(st, stLp);
            card.addView(info, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
            card.addView(text(current ? "ТИ\nТУТ" : completed ? "✓" : "🔒", current ? 11 : 17, true, Gravity.CENTER,
                    current ? GOLD_LIGHT : completed ? GOLD : LOCKED), new LinearLayout.LayoutParams(dp(45), LayoutParams.MATCH_PARENT));
            if (locked) card.setAlpha(0.94f);
            content.addView(card, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(114)));

            if (i < LeagueSystem.NAMES.length-1) {
                LinearLayout connector = new LinearLayout(context); connector.setGravity(Gravity.CENTER);
                View line = new View(context); GradientDrawable l = new GradientDrawable();
                l.setColor(i < currentIndex ? GOLD : LINE_DARK); l.setCornerRadius(dp(2)); line.setBackground(l);
                connector.addView(line, new LinearLayout.LayoutParams(dp(3), dp(26)));
                content.addView(connector, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(26)));
            }
        }

        LinearLayout.LayoutParams endLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(80));
        endLp.topMargin = dp(18);
        content.addView(text("♛  ВЕРШИНА ШЛЯХУ  ♛\nЛЕГЕНДА III", 15, true, Gravity.CENTER, GOLD_LIGHT), endLp);
        scroll.addView(content);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        sp.topMargin = dp(68); addView(scroll, sp);
    }

    /**
     * Every league uses its own correct major-rank artwork. This deliberately
     * maps by league NAME, not cup count: NOVICE III must stay novice artwork
     * even if its threshold overlaps the next old rank-art threshold.
     */
    private String assetForLeague(String leagueName) {
        String n = leagueName == null ? "" : leagueName.toUpperCase();
        if (n.contains("ЛЕГЕН")) return "rank_legend";
        if (n.contains("ЕЛІТ")) return "rank_elite";
        if (n.contains("МАЙСТЕР")) return "rank_master";
        if (n.contains("ПРОФ")) return "rank_pro";
        if (n.contains("ГРАВ")) return "rank_player";
        return "rank_novice";
    }

    private int stage(String name) {
        String n = name == null ? "" : name.trim().toUpperCase();
        if (n.endsWith(" III")) return 3;
        if (n.endsWith(" II")) return 2;
        if (n.endsWith(" I")) return 1;
        return 1;
    }

    private int family(String name) {
        String n = name == null ? "" : name.toUpperCase();
        if (n.contains("ЛЕГЕН")) return 5;
        if (n.contains("ЕЛІТ")) return 4;
        if (n.contains("МАЙСТЕР")) return 3;
        if (n.contains("ПРОФ")) return 2;
        if (n.contains("ГРАВ")) return 1;
        return 0;
    }

    /**
     * Premium 2D badge progression. I, II and III are intentionally very
     * different: thicker double frame, larger art, more diamonds, side wings,
     * top star/crown and a Roman tier plate. Higher league families also use
     * brighter metal. This remains cheap Android UI, so it does not reintroduce
     * the Blender/Filament scrolling problem.
     */
    private FrameLayout makeLeagueBadge(String leagueName, boolean locked, boolean current) {
        int tier = stage(leagueName);
        int fam = family(leagueName);
        int accent = accentFor(fam, tier, current);

        FrameLayout holder = new FrameLayout(context);
        holder.setClipChildren(false); holder.setClipToPadding(false);
        GradientDrawable outer = new GradientDrawable();
        outer.setShape(GradientDrawable.OVAL);
        outer.setColor(Color.argb(current ? 44 : 20, 255, 190, 62));
        outer.setStroke(dp(tier == 1 ? 2 : tier == 2 ? 3 : 4), accent);
        holder.setBackground(outer);

        // II and III get a second inner metal ring, so NOVICE II can never look
        // like NOVICE I even though both share the same base rank_novice art.
        if (tier >= 2) {
            FrameLayout ring = new FrameLayout(context);
            GradientDrawable rd = new GradientDrawable(); rd.setShape(GradientDrawable.OVAL);
            rd.setColor(Color.TRANSPARENT); rd.setStroke(dp(tier == 3 ? 2 : 1), Color.argb(220, 255, 225, 145));
            ring.setBackground(rd);
            FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            int m = tier == 3 ? dp(7) : dp(9); rp.setMargins(m,m,m,m); holder.addView(ring,rp);
        }

        String resource = assetForLeague(leagueName);
        int id = context.getResources().getIdentifier(resource, "drawable", context.getPackageName());
        if (id != 0) {
            ImageView icon = new ImageView(context); icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setImageResource(id); icon.setAlpha(locked ? 0.72f : 1f);
            int pad = tier == 1 ? 8 : tier == 2 ? 4 : 1; icon.setPadding(dp(pad),dp(pad),dp(pad),dp(pad));
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            ip.setMargins(dp(3),dp(3),dp(3),dp(3)); holder.addView(icon,ip);
        } else {
            holder.addView(text(symbolForFamily(fam), 34, true, Gravity.CENTER, locked ? LOCKED : accent),
                    new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        // Distinct top evolution: I diamond, II star, III crown.
        String top = tier == 1 ? "◆" : tier == 2 ? "✦" : "♛";
        TextView crest = text(top, tier == 3 ? 15 : 12, true, Gravity.CENTER, locked ? Color.rgb(130,110,70) : GOLD_LIGHT);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(dp(28), dp(23)); cp.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL; cp.topMargin=dp(-6);
        holder.addView(crest,cp);

        // Side wings start at II and become stronger at III.
        if (tier >= 2) {
            TextView left = text(tier==3 ? "❮◆" : "◆", 9, true, Gravity.CENTER, locked ? LOCKED : accent);
            TextView right = text(tier==3 ? "◆❯" : "◆", 9, true, Gravity.CENTER, locked ? LOCKED : accent);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(28),dp(24)); lp.gravity=Gravity.CENTER_VERTICAL|Gravity.LEFT; lp.leftMargin=dp(-9);
            FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(dp(28),dp(24)); rp.gravity=Gravity.CENTER_VERTICAL|Gravity.RIGHT; rp.rightMargin=dp(-9);
            holder.addView(left,lp); holder.addView(right,rp);
        }

        String diamonds = tier==1 ? "◆" : tier==2 ? "◆ ◆" : "◆ ◆ ◆";
        TextView plate = text(diamonds + "   " + roman(tier), 8, true, Gravity.CENTER, locked ? Color.rgb(130,108,67) : accent);
        plate.setBackground(panel(Color.argb(235,2,10,8), accent, tier==3 ? 2 : 1, 8));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(tier==1?dp(46):tier==2?dp(60):dp(72),dp(19));
        pp.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL; pp.bottomMargin=dp(-3); holder.addView(plate,pp);
        return holder;
    }

    private int accentFor(int family, int tier, boolean current) {
        if (current) return Color.rgb(255,225,142);
        int boost = family * 7 + tier * 8;
        return Color.rgb(Math.min(255,180+boost), Math.min(220,125+boost), Math.min(125,42+boost/2));
    }

    private String roman(int tier) { return tier==3 ? "III" : tier==2 ? "II" : "I"; }
    private String symbolForFamily(int fam) {
        switch (fam) { case 5:return "♛"; case 4:return "♦"; case 3:return "★"; case 2:return "♠"; case 1:return "♣"; default:return "♠"; }
    }

    private TextView text(String s,int size,boolean bold,int gravity,int color) {
        TextView v=new TextView(context); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setGravity(gravity);
        if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }
    private GradientDrawable panel(int fill,int stroke,int width,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(radius)); if(width>0)d.setStroke(dp(width),stroke); return d;
    }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
